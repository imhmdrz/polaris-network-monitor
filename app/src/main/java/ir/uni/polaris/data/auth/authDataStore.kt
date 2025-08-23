package ir.uni.polaris.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.uni.polaris.model.AuthUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.authDataStore by preferencesDataStore("auth")

@Singleton
class AuthPrefs @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    private object Keys {
        val id = longPreferencesKey("id")
        val username = stringPreferencesKey("username")
        val token = stringPreferencesKey("token")
    }

    val tokenFlow: Flow<String?> = ctx.authDataStore.data.map { it[Keys.token] }
    val userFlow: Flow<AuthUser?> = ctx.authDataStore.data.map { p ->
        val token = p[Keys.token] ?: return@map null
        AuthUser(
            id = p[Keys.id] ?: -1,
            username = p[Keys.username] ?: "",
            accessToken = token
        )
    }

    suspend fun save(user: AuthUser) {
        ctx.authDataStore.edit {
            it[Keys.id] = user.id
            it[Keys.username] = user.username
            it[Keys.token] = user.accessToken
        }
    }

    suspend fun clear() {
        ctx.authDataStore.edit { it.clear() }
    }
}
