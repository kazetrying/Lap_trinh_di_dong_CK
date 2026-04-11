package com.example.flashmind.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.flashmind.core.network.AuthRequestDto
import com.example.flashmind.core.network.FlashMindApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_session")

@Singleton
class AuthSessionStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: FlashMindApi,
) {
    val sessionFlow: Flow<AuthSession?> = context.authDataStore.data.map { prefs ->
        val currentEmail = prefs[CURRENT_EMAIL_KEY] ?: return@map null
        AuthSession(
            email = currentEmail,
            token = prefs[AUTH_TOKEN_KEY],
            isRemote = !prefs[AUTH_TOKEN_KEY].isNullOrBlank(),
        )
    }

    suspend fun register(email: String, password: String) {
        val response = runCatching {
            api.register(AuthRequestDto(email = email, password = password))
        }.getOrNull()
        context.authDataStore.edit { prefs ->
            prefs[REGISTERED_EMAIL_KEY] = email
            prefs[REGISTERED_PASSWORD_KEY] = password
            prefs[CURRENT_EMAIL_KEY] = response?.email ?: email
            prefs[AUTH_TOKEN_KEY] = response?.token ?: "offline-$email"
        }
        mirrorToken(response?.token ?: "offline-$email")
    }

    suspend fun login(email: String, password: String): Boolean {
        val remoteSession = runCatching {
            api.login(AuthRequestDto(email = email, password = password))
        }.getOrNull()
        if (remoteSession != null) {
            context.authDataStore.edit { prefs ->
                prefs[REGISTERED_EMAIL_KEY] = remoteSession.email
                prefs[REGISTERED_PASSWORD_KEY] = password
                prefs[CURRENT_EMAIL_KEY] = remoteSession.email
                prefs[AUTH_TOKEN_KEY] = remoteSession.token
            }
            mirrorToken(remoteSession.token)
            return true
        }
        var isValid = false
        context.authDataStore.edit { prefs ->
            val savedEmail = prefs[REGISTERED_EMAIL_KEY]
            val savedPassword = prefs[REGISTERED_PASSWORD_KEY]
            isValid = savedEmail == email && savedPassword == password
            if (isValid) {
                prefs[CURRENT_EMAIL_KEY] = email
                prefs[AUTH_TOKEN_KEY] = "offline-$email"
            }
        }
        if (isValid) {
            mirrorToken("offline-$email")
        }
        return isValid
    }

    suspend fun logout() {
        context.authDataStore.edit { prefs ->
            prefs.remove(CURRENT_EMAIL_KEY)
            prefs.remove(AUTH_TOKEN_KEY)
        }
        mirrorToken(null)
    }

    suspend fun hasRegisteredAccount(): Boolean {
        val prefs = context.authDataStore.data.first()
        return !prefs[REGISTERED_EMAIL_KEY].isNullOrBlank()
    }

    companion object {
        private const val AUTH_PREFS = "flashmind_auth_headers"
        private const val AUTH_TOKEN_PREF_KEY = "auth_token"
        private val REGISTERED_EMAIL_KEY = stringPreferencesKey("registered_email")
        private val REGISTERED_PASSWORD_KEY = stringPreferencesKey("registered_password")
        private val CURRENT_EMAIL_KEY = stringPreferencesKey("current_email")
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    private fun mirrorToken(token: String?) {
        context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(AUTH_TOKEN_PREF_KEY, token)
            .apply()
    }
}

data class AuthSession(
    val email: String,
    val token: String?,
    val isRemote: Boolean,
)
