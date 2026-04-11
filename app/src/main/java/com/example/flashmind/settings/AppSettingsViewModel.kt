package com.example.flashmind.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val repository: AppSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppSettingsUiState())
    val uiState: StateFlow<AppSettingsUiState> = _uiState.asStateFlow()

    init {
        repository.settingsFlow
            .onEach { settings ->
                _uiState.value = AppSettingsUiState(
                    useDarkTheme = settings.useDarkTheme,
                    reminderEnabled = settings.reminderEnabled,
                    reminderHour = settings.reminderHour,
                    reminderMinute = settings.reminderMinute,
                )
            }
            .launchIn(viewModelScope)
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkTheme(enabled) }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setReminderEnabled(enabled) }
    }

    fun shiftReminderHour(delta: Int) {
        val state = _uiState.value
        val nextHour = (state.reminderHour + delta).mod(24)
        viewModelScope.launch { repository.setReminderTime(nextHour, state.reminderMinute) }
    }

    fun shiftReminderMinute(delta: Int) {
        val state = _uiState.value
        val totalMinutes = (state.reminderHour * 60 + state.reminderMinute + delta).mod(24 * 60)
        val nextHour = totalMinutes / 60
        val nextMinute = totalMinutes % 60
        viewModelScope.launch { repository.setReminderTime(nextHour, nextMinute) }
    }
}

data class AppSettingsUiState(
    val useDarkTheme: Boolean = false,
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 19,
    val reminderMinute: Int = 0,
)
