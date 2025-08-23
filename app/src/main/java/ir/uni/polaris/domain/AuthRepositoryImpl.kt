package ir.uni.polaris.domain

import ir.uni.polaris.model.AuthUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val client: OkHttpClient
) : AuthRepository {

    private val base = "https://polaris-backend-9blu.onrender.com"
    private val jsonMedia = "application/json".toMediaType()

    override suspend fun login(username: String, password: String): Result<AuthUser> =
        post("$base/users/login", username, password, true)

    override suspend fun register(username: String, password: String): Result<AuthUser> =
        post("$base/users/RegisterNewUser", username, password, false)

    private suspend fun post(url: String, username: String, password: String, isLogin: Boolean): Result<AuthUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                val bodyJson = if (isLogin) {
                    JSONObject()
                        .put("username", username)
                        .put("password", password)
                        .toString()
                        .toRequestBody(jsonMedia)
                } else {
                    JSONObject()
                        .put("username", username)
                        .put("password", password)
                        .put("phone", "+1234567890")
                        .toString()
                        .toRequestBody(jsonMedia)
                }






                val req = Request.Builder()
                    .url(url)
                    .post(bodyJson)
                    .addHeader("accept", "*/*")
                    .addHeader("Content-Type", "application/json")
                    .build()

                client.newCall(req).execute().use { resp ->
                    val str = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) error("HTTP ${resp.code}: $str")

                    val obj = JSONObject(str)
                    val user = AuthUser(
                        id = obj.optLong("id"),
                        username = obj.optString("username"),
                        accessToken = obj.optString("access_token")
                    )
                    if (user.accessToken.isBlank()) error("Invalid response: access_token missing")
                    user
                }
            }
        }
}
