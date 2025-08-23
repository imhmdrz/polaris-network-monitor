package ir.uni.polaris.domain

import android.util.Log
import ir.uni.polaris.data.LocationDataSource
import ir.uni.polaris.data.TelephonyDataSource
import ir.uni.polaris.data.auth.AuthPrefs
import ir.uni.polaris.data.storage.MeasurementsDao
import ir.uni.polaris.model.Measurement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasurementsRepositoryImpl @Inject constructor(
    private val loc: LocationDataSource,
    private val tel: TelephonyDataSource,
    private val dao: MeasurementsDao,
    private val client: OkHttpClient,
    private val authPrefs: AuthPrefs
) : MeasurementsRepository {

    private var job: Job? = null
    override val measurements: Flow<List<Measurement>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun start(intervalMillis: Long) {
        if (job?.isActive == true) return
        job = CoroutineScope(Dispatchers.IO).launch {
            loc.locationFlow(intervalMillis).collect { geo ->
                val cell = runCatching { tel.snapshot() }.getOrNull()
                val m = Measurement(location = geo, cell = cell)
                dao.insert(m.toEntity())

                val userName = runCatching { authPrefs.userFlow.first()?.username }.getOrNull()
                if (!userName.isNullOrBlank()) {
                    postMeasurement(userName, m)
                }
            }
        }
    }

    private suspend fun postMeasurement(username: String, m: Measurement) =
        withContext(Dispatchers.IO) {
            val url = "https://polaris-backend-9blu.onrender.com/locations/AddNewLocation"
            val json = buildLocationJson(username, m)
            val req = Request.Builder()
                .url(url)
                .post(json.toRequestBody("application/json".toMediaType()))
                .addHeader("accept", "*/*")
                .addHeader("Content-Type", "application/json")
                .build()

            runCatching { client.newCall(req).execute().use { /* no-op */ } }
                .onFailure {
                    Log.d("mhmdrz" , "${it.message}")
                }
        }

    private fun buildLocationJson(username: String, m: Measurement): String {
        val c = m.cell
        fun JSONObject.putIfNotNull(key: String, value: Any?) =
            apply { if (value != null) put(key, value) }

        return JSONObject()
            .put("username", username)
            .put("lat", m.location.lat)
            .put("lng", m.location.lng)
            .put("timeMillis", m.location.timeMillis)
            .put("radioTech", c?.radioTech ?: "UNKNOWN")
            .putIfNotNull("generation", c?.generation)
            .putIfNotNull("pLMN", c?.pLMN)
            .putIfNotNull("mcc", c?.mcc)
            .putIfNotNull("mnc", c?.mnc)
            .putIfNotNull("lac", c?.lac)
            .putIfNotNull("rac", c?.rac)
            .putIfNotNull("tac", c?.tac)
            .putIfNotNull("tacLacRac", c?.tacLacRac)
            .putIfNotNull("cellId", c?.cellId)
            .putIfNotNull("arfcn", c?.arfcn)
            .putIfNotNull("uarfcn", c?.uarfcn)
            .putIfNotNull("earfcn", c?.earfcn)
            .putIfNotNull("nrarfcn", c?.nrarfcn)
            .putIfNotNull("band", c?.band)
            .putIfNotNull("downlinkFreqHz", c?.downlinkFreqHz)
            .putIfNotNull("rSRP", c?.rSRP)
            .putIfNotNull("rSRQ", c?.rSRQ)
            .putIfNotNull("rSCP", c?.rSCP)
            .putIfNotNull("ecNo", c?.ecNo)
            .putIfNotNull("rxLev", c?.rxLev)
            .putIfNotNull("sinr", c?.sinr)
            .toString()
    }

    override fun stop() {
        job?.cancel(); job = null
    }
}


