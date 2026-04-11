package com.example.flashmind.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")

@Singleton
class AppSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val settingsFlow: Flow<AppSettings> = context.appSettingsDataStore.data.map { prefs ->
        AppSettings(
            useDarkTheme = prefs[DARK_MODE_KEY] ?: false,
            reminderEnabled = prefs[REMINDER_ENABLED_KEY] ?: true,
            reminderHour = prefs[REMINDER_HOUR_KEY] ?: 19,
            reminderMinute = prefs[REMINDER_MINUTE_KEY] ?: 0,
        )
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[REMINDER_ENABLED_KEY] = enabled
        }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[REMINDER_HOUR_KEY] = hour.coerceIn(0, 23)
            prefs[REMINDER_MINUTE_KEY] = minute.coerceIn(0, 59)
        }
    }

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val REMINDER_ENABLED_KEY = booleanPreferencesKey("reminder_enabled")
        private val REMINDER_HOUR_KEY = intPreferencesKey("reminder_hour")
        private val REMINDER_MINUTE_KEY = intPreferencesKey("reminder_minute")
    }
}

data class AppSettings(
    val useDarkTheme: Boolean,
    val reminderEnabled: Boolean,
    val reminderHour: Int,
    val reminderMinute: Int,
)
