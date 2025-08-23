package ir.uni.polaris.domain

import android.util.Log
import android.util.Patterns
import ir.uni.polaris.data.storage.TestsDao
import ir.uni.polaris.model.TestDefinition
import ir.uni.polaris.model.TestResult
import ir.uni.polaris.model.TestType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import toDomain
import toEntity
import java.net.InetAddress
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.system.measureTimeMillis


@Singleton
class NetworkTestsRepositoryImpl @Inject constructor(
    private val dao: TestsDao,
    private val client: OkHttpClient,
) : NetworkTestsRepository {
    private var scope: CoroutineScope? = null
    private val jobs = mutableMapOf<Long, Job>()

    override fun observeDefinitions(): Flow<List<TestDefinition>> =
        dao.observeDefinitions().map { list -> list.map { it.toDomain() } }

    override suspend fun upsertDefinition(def: TestDefinition): Long =
        dao.upsertDefinition(def.toEntity())

    override fun observeLatestResult(definitionId: Long): Flow<TestResult?> =
        dao.observeLatestResult(definitionId).map { it?.toDomain() }

    override fun start() {
        if (scope != null) return
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope?.launch {
            val enabled = dao.getEnabledDefinitionsOnce()
            if (enabled.isEmpty()) seedDefaults()
            Log.d("mhmdrz" , "start test")
            dao.getEnabledDefinitionsOnce().forEach { defE ->
                val def = defE.toDomain()
                val job = launch {
                    while (isActive && def.enabled) {
                        runCatching {
                            runOnce(def)
                        }.onFailure {
                            dao.insertResult(
                                TestResult(
                                    definitionId = def.id,
                                    timeMillis = System.currentTimeMillis(),
                                    value = Double.NaN,
                                    unit = when (def.type) {
                                        TestType.HTTP_UPLOAD -> "Mbps"
                                        else -> "ms"
                                    },
                                    ok = false,
                                    note = it.message
                                ).toEntity()
                            )
                        }
                        delay(max(1000L, def.intervalMillis))
                    }
                }
                jobs[def.id] = job
            }
        }
    }

    override fun stop() {
        jobs.values.forEach { it.cancel() }
        jobs.clear()
        scope?.cancel(); scope = null
    }

    private suspend fun runOnce(def: TestDefinition) {
        when (def.type) {
            TestType.HTTP_UPLOAD -> {
                val mbps = httpUploadMbps(def.target, def.payloadBytes ?: (25 * 1024))
                save(def, value = mbps, unit = "Mbps", ok = !mbps.isNaN())
            }

            TestType.HTTP_DOWNLOAD -> {
                val mbps = httpDownloadMbps(def.target)
                save(def, value = mbps, unit = "Mbps", ok = !mbps.isNaN())
            }

            TestType.PING -> {
                measurePingTime(def.target) { ms ->
                    save(def, value = ms.toDouble(), unit = "ms", ok = ms >= 0)
                }
            }

            TestType.DNS -> {
                val ms = dnsResolveMs(def.target)
                save(def, value = ms.toDouble(), unit = "ms", ok = ms >= 0)
            }

            TestType.WEB -> {
                val ms = webTtfbMs(def.target)
                save(def, value = ms.toDouble(), unit = "ms", ok = ms >= 0)
            }
        }
    }

    private suspend fun httpDownloadMbps(url: String): Double = withContext(Dispatchers.IO) {
        return@withContext try {
            val req = Request.Builder()
                .url(url)
                .get()
                .header("Cache-Control", "no-cache")
                .build()

            var bytesReadTotal = 0L
            val elapsedMs = measureTimeMillis {
                client.newCall(req).execute().use { resp ->
                    val body = resp.body ?: return@use
                    body.byteStream().use { ins ->
                        val buf = ByteArray(8 * 1024)
                        while (true) {
                            val n = ins.read(buf)
                            if (n == -1) break
                            bytesReadTotal += n
                        }
                    }
                }
            }
            if (elapsedMs <= 0 || bytesReadTotal == 0L) Double.NaN
            else (bytesReadTotal * 8.0) / (elapsedMs / 1000.0) / 1_000_000.0  // Mbps
        } catch (_: Throwable) {
            Double.NaN
        }
    }


    private suspend fun save(
        def: TestDefinition,
        value: Double,
        unit: String,
        ok: Boolean,
        note: String? = null
    ) {
        dao.insertResult(
            TestResult(
                definitionId = def.id,
                timeMillis = System.currentTimeMillis(),
                value = value,
                unit = unit,
                ok = ok,
                note = note
            ).toEntity()
        )
    }

    private suspend fun httpUploadMbps(url: String, sizeBytes: Int): Double =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val payload = ByteArray(sizeBytes) { 0x5A }
                val reqBody = payload.toRequestBody("application/octet-stream".toMediaType())
                val req = Request.Builder().url(url).post(reqBody).build()

                val elapsedMs = measureTimeMillis {
                    client.newCall(req).execute().use { resp ->

                    }
                }
                if (elapsedMs <= 0) Double.NaN else (sizeBytes * 8.0) / (elapsedMs / 1000.0) / 1_000_000.0
            } catch (_: Throwable) {
                Double.NaN
            }
        }

    private suspend fun measurePingTime(url: String, onResponse: suspend (Long) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).head().build()
        val startTime = System.nanoTime()
        client.newCall(request).execute().use { response ->
            val endTime = System.nanoTime()
            val responseTimeMs = (endTime - startTime) / 1_000_000

            println("Ping (HTTP) Response Time: $responseTimeMs ms")
            onResponse(responseTimeMs)
            response.close()
        }
    }

    private suspend fun dnsResolveMs(hostOrUrl: String): Long = withContext(Dispatchers.IO) {
        val host = hostFromInput(hostOrUrl) ?: return@withContext -1L
        try {
            val ms = measureTimeMillis {
                InetAddress.getAllByName(host)
            }
            ms
        } catch (_: Throwable) {
            -1L
        }
    }

    private suspend fun webTtfbMs(urlStr: String): Long = withContext(Dispatchers.IO) {
        return@withContext try {
            val req = Request.Builder().url(urlStr).get().build()
            val start = System.nanoTime()
            client.newCall(req).execute().use { resp ->

            }
            ((System.nanoTime() - start) / 1_000_000)
        } catch (_: Throwable) {
            -1L
        }
    }

    private fun hostFromInput(input: String): String? = try {
        if (Patterns.WEB_URL.matcher(input).matches() && input.contains("://")) {
            URL(input).host
        } else input
    } catch (_: Throwable) {
        null
    }

    private suspend fun seedDefaults() {
        val defs = listOf(
            TestDefinition(
                type = TestType.PING,
                target = "https://www.google.com",
                intervalMillis = 15_000
            ),
            TestDefinition(
                type = TestType.DNS,
                target = "https://www.google.com",
                intervalMillis = 20_000
            ),
            TestDefinition(
                type = TestType.WEB,
                target = "https://www.google.com",
                intervalMillis = 25_000
            ),
            TestDefinition(
                type = TestType.HTTP_UPLOAD,
                target = "https://httpbin.org/post",
                intervalMillis = 30_000,
                payloadBytes = 25 * 1024
            ),
            TestDefinition(
                type = TestType.HTTP_DOWNLOAD,
                target = "https://www.pexels.com/search/free%20download",
                intervalMillis = 30_000
            )
        )
        defs.forEach { upsertDefinition(it) }
    }
}
