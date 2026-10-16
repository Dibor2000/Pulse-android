package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class AudioNewsReader(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (_: Exception) {
            tts = null
        }
    }

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentArticleId = MutableStateFlow<String?>(null)
    val currentArticleId: StateFlow<String?> = _currentArticleId.asStateFlow()

    override fun onInit(status: Int) {
        try {
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                val ruLocale = Locale("ru", "RU")
                val result = tts?.setLanguage(ruLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to default locale
                    tts?.language = Locale.getDefault()
                }
                tts?.setSpeechRate(1.0f)
                tts?.setPitch(1.0f)

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isPlaying.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isPlaying.value = false
                        _currentArticleId.value = null
                    }

                    override fun onError(utteranceId: String?) {
                        _isPlaying.value = false
                        _currentArticleId.value = null
                    }
                })
            }
        } catch (_: Exception) {}
    }

    fun speak(articleId: String, textToSpeak: String) {
        if (!isInitialized || tts == null) return
        stop()
        _currentArticleId.value = articleId
        _isPlaying.value = true
        val params = HashMap<String, String>()
        params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = articleId
        @Suppress("DEPRECATION")
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params)
    }

    fun stop() {
        if (tts != null) {
            tts?.stop()
        }
        _isPlaying.value = false
        _currentArticleId.value = null
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
