package com.example.readvault.reader

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking = _isSpeaking.asStateFlow()

    private val _currentSentenceIndex = MutableStateFlow(0)
    val currentSentenceIndex = _currentSentenceIndex.asStateFlow()

    private val _rate = MutableStateFlow(1.0f)
    val rate = _rate.asStateFlow()

    private val _pitch = MutableStateFlow(1.0f)
    val pitch = _pitch.asStateFlow()

    private var sentences: List<String> = emptyList()
    private var currentIndex = 0

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(_rate.value)
                tts?.setPitch(_pitch.value)
                isInitialized = true
                setupListener()
            }
        }
    }

    private fun setupListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                if (currentIndex + 1 < sentences.size) {
                    currentIndex++
                    _currentSentenceIndex.value = currentIndex
                    speakNext()
                } else {
                    _isSpeaking.value = false
                    currentIndex = 0
                    _currentSentenceIndex.value = 0
                }
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }
        })
    }

    fun speakText(text: String, startIndex: Int = 0) {
        if (!isInitialized) return
        stop()

        sentences = text.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
        if (sentences.isEmpty()) return

        currentIndex = startIndex.coerceIn(0, sentences.size - 1)
        _currentSentenceIndex.value = currentIndex
        speakNext()
    }

    private fun speakNext() {
        if (currentIndex < sentences.size) {
            val sentence = sentences[currentIndex]
            val params = android.os.Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "readvault_sentence_$currentIndex")
            tts?.speak(sentence, TextToSpeech.QUEUE_FLUSH, params, "readvault_sentence_$currentIndex")
            _isSpeaking.value = true
        }
    }

    fun pause() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun resume() {
        if (sentences.isNotEmpty() && currentIndex < sentences.size) {
            speakNext()
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        currentIndex = 0
        _currentSentenceIndex.value = 0
    }

    fun setSpeechRate(rate: Float) {
        _rate.value = rate.coerceIn(0.5f, 2.5f)
        tts?.setSpeechRate(_rate.value)
    }

    fun setSpeechPitch(pitch: Float) {
        _pitch.value = pitch.coerceIn(0.5f, 2.0f)
        tts?.setPitch(_pitch.value)
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
