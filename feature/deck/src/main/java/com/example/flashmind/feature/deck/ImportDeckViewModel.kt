package com.example.flashmind.feature.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashmind.core.domain.usecase.ImportDeckUseCase
import com.example.flashmind.core.model.ImportCardDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@HiltViewModel
class ImportDeckViewModel @Inject constructor(
    private val importDeckUseCase: ImportDeckUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportDeckUiState())
    val uiState: StateFlow<ImportDeckUiState> = _uiState.asStateFlow()

    fun onJsonChanged(value: String) {
        _uiState.value = _uiState.value.copy(json = value, error = null)
    }

    fun importDeck(onDone: () -> Unit) {
        val rawJson = _uiState.value.json.trim()
        if (rawJson.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Paste deck JSON first.")
            return
        }
        viewModelScope.launch {
            runCatching {
                val parsed = parseDeckJson(rawJson)
                importDeckUseCase(parsed.title, parsed.description, parsed.cards)
            }.onSuccess {
                onDone()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message ?: "Invalid deck JSON.")
            }
        }
    }

    private fun parseDeckJson(rawJson: String): ImportedDeckPayload {
        val root = JSONObject(rawJson)
        val cardsArray = root.optJSONArray("cards") ?: JSONArray()
        val cards = buildList {
            for (index in 0 until cardsArray.length()) {
                val card = cardsArray.getJSONObject(index)
                add(
                    ImportCardDraft(
                        front = card.getString("front"),
                        back = card.getString("back"),
                        pronunciation = card.optString("pronunciation").takeIf { it.isNotBlank() },
                        exampleSentence = card.optString("exampleSentence").takeIf { it.isNotBlank() },
                        imageUrl = card.optString("imageUrl").takeIf { it.isNotBlank() },
                        isStarred = card.optBoolean("isStarred", false),
                    ),
                )
            }
        }
        require(cards.isNotEmpty()) { "Deck must contain at least one card." }
        return ImportedDeckPayload(
            title = root.optString("title").ifBlank { "Imported deck" },
            description = root.optString("description"),
            cards = cards,
        )
    }
}

data class ImportDeckUiState(
    val json: String = "",
    val error: String? = null,
)

private data class ImportedDeckPayload(
    val title: String,
    val description: String,
    val cards: List<ImportCardDraft>,
)
