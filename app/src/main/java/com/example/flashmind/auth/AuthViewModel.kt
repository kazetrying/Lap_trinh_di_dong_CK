package com.example.flashmind.auth

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
class AuthViewModel @Inject constructor(
    private val authSessionStore: AuthSessionStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        authSessionStore.sessionFlow
            .onEach { session ->
                _uiState.value = _uiState.value.copy(
                    ready = true,
                    isAuthenticated = session != null,
                    currentUserEmail = session?.email,
                    isRemoteSession = session?.isRemote == true,
                )
            }
            .launchIn(viewModelScope)
    }

    fun onEmailChanged(value: String) {
        _uiState.value = _uiState.value.copy(email = value, error = null)
    }

    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, error = null)
    }

    fun switchMode(isRegisterMode: Boolean) {
        _uiState.value = _uiState.value.copy(
            isRegisterMode = isRegisterMode,
            error = null,
        )
    }

    fun submit() {
        val state = _uiState.value
        val email = state.email.trim()
        val password = state.password
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = state.copy(error = "Email và mật khẩu là bắt buộc.")
            return
        }
        viewModelScope.launch {
            if (state.isRegisterMode) {
                if (password.length < 6) {
                    _uiState.value = _uiState.value.copy(error = "Mật khẩu phải có ít nhất 6 ký tự.")
                    return@launch
                }
                if (password != state.confirmPassword) {
                    _uiState.value = _uiState.value.copy(error = "Mật khẩu xác nhận không khớp.")
                    return@launch
                }
                authSessionStore.register(email, password)
                _uiState.value = _uiState.value.copy(password = "", confirmPassword = "", error = null)
            } else {
                val success = authSessionStore.login(email, password)
                if (!success) {
                    _uiState.value = _uiState.value.copy(error = "Email hoặc mật khẩu không đúng.")
                } else {
                    _uiState.value = _uiState.value.copy(password = "", confirmPassword = "", error = null)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authSessionStore.logout()
        }
    }
}

data class AuthUiState(
    val ready: Boolean = false,
    val isAuthenticated: Boolean = false,
    val currentUserEmail: String? = null,
    val isRemoteSession: Boolean = false,
    val isRegisterMode: Boolean = false,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val error: String? = null,
)
