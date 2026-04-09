package com.example.flashcardapp.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsHelper(context: Context) {

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var pendingText: String? = null

    // ← Dùng applicationContext thay vì context trực tiếp
    private val appContext = context.applicationContext

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(appContext) { status ->
            android.util.Log.d("TTS", "onInit called, status=$status")
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.forLanguageTag("vi-VN")
                android.util.Log.d("TTS", "Init OK")
                isReady = true
                pendingText?.let {
                    tts?.speak(it, TextToSpeech.QUEUE_FLUSH, null, "tts_1")
                }
                pendingText = null
            } else {
                android.util.Log.e("TTS", "Init FAILED status=$status")
            }
        }
    }

    fun speak(text: String) {
        android.util.Log.d("TTS", "speak() isReady=$isReady text=$text")
        if (!isReady) {
            pendingText = text
            return
        }
        if (text.isNotBlank()) {
            tts?.stop()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_1")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}