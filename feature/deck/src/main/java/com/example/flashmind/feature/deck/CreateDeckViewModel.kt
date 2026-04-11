package com.example.flashmind.feature.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashmind.core.domain.usecase.CreateDeckUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CreateDeckViewModel @Inject constructor(
    private val createDeckUseCase: CreateDeckUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateDeckUiState())
    val uiState: StateFlow<CreateDeckUiState> = _uiState.asStateFlow()

    fun onTitleChanged(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun onDescriptionChanged(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun createDeck(onDone: () -> Unit) {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.value = state.copy(error = "Title is required")
            return
        }

        viewModelScope.launch {
            runCatching {
                createDeckUseCase(
                    title = state.title.trim(),
                    description = state.description.trim(),
                )
            }.onSuccess {
                onDone()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message)
            }
        }
    }
}

data class CreateDeckUiState(
    val title: String = "",
    val description: String = "",
    val error: String? = null,
)
