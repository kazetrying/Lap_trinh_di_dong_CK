package com.example.flashcardapp.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TtsHelper(context: Context) {

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var pendingText: String? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Kiểm tra xem máy có hỗ trợ tiếng Anh không (ưu tiên vì bạn học tiếng Anh)
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.getDefault()
                }
                isReady = true
                Log.d("TTS", "TTS Initialized successfully")
                pendingText?.let { speak(it) }
                pendingText = null
            } else {
                Log.e("TTS", "TTS Initialization failed")
            }
        }
    }

    fun speak(text: String) {
        if (!isReady) {
            pendingText = text
            return
        }
        
        if (text.isNotBlank()) {
            // ✅ TỰ ĐỘNG CHỌN NGÔN NGỮ: 
            // Nếu text không chứa dấu tiếng Việt thì dùng tiếng Anh, ngược lại dùng tiếng Việt
            val hasVietnameseSigns = text.any { it in "àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ" }
            if (hasVietnameseSigns) {
                tts?.language = Locale("vi", "VN")
            } else {
                tts?.language = Locale.US
            }

            tts?.stop()
            // Tăng âm lượng và tốc độ đọc một chút cho rõ
            tts?.setSpeechRate(0.9f) 
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_id")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}