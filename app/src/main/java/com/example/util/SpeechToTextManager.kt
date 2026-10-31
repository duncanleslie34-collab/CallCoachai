package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

private const val TAG = "SpeechToTextManager"

sealed class VoiceInputState {
    object Idle : VoiceInputState()
    object Initializing : VoiceInputState()
    data class Listening(val rmsDb: Float = 0f, val partialText: String = "") : VoiceInputState()
    data class Success(val recognizedText: String) : VoiceInputState()
    data class Error(val message: String) : VoiceInputState()
}

class SpeechToTextManager(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListeningInternal = false

    var onStateChanged: ((VoiceInputState) -> Unit)? = null
    var onPartialTranscript: ((String) -> Unit)? = null
    var onFinalTranscript: ((String) -> Unit)? = null

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening() {
        mainHandler.post {
            try {
                if (!isAvailable()) {
                    val msg = "Speech recognition service is not available on this device."
                    Log.w(TAG, msg)
                    onStateChanged?.invoke(VoiceInputState.Error(msg))
                    return@post
                }

                // Clean previous instance if any
                cleanupRecognizer()

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createListener())
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    putExtra("android.speech.extra.DICTATION_MODE", true)
                }

                isListeningInternal = true
                onStateChanged?.invoke(VoiceInputState.Listening(rmsDb = 0f, partialText = ""))
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting speech recognition", e)
                isListeningInternal = false
                onStateChanged?.invoke(VoiceInputState.Error(e.localizedMessage ?: "Failed to start microphone"))
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                if (isListeningInternal) {
                    speechRecognizer?.stopListening()
                    isListeningInternal = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping speech recognition", e)
            }
        }
    }

    fun cancel() {
        mainHandler.post {
            try {
                isListeningInternal = false
                speechRecognizer?.cancel()
                onStateChanged?.invoke(VoiceInputState.Idle)
            } catch (e: Exception) {
                Log.e(TAG, "Error canceling speech recognition", e)
            }
        }
    }

    fun destroy() {
        mainHandler.post {
            cleanupRecognizer()
            onStateChanged = null
            onPartialTranscript = null
            onFinalTranscript = null
        }
    }

    private fun cleanupRecognizer() {
        try {
            isListeningInternal = false
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying speech recognizer", e)
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                onStateChanged?.invoke(VoiceInputState.Listening(rmsDb = 0f, partialText = ""))
            }

            override fun onBeginningOfSpeech() {
                // Agent started speaking
            }

            override fun onRmsChanged(rmsdB: Float) {
                if (isListeningInternal) {
                    onStateChanged?.invoke(VoiceInputState.Listening(rmsDb = rmsdB, partialText = ""))
                }
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListeningInternal = false
            }

            override fun onError(error: Int) {
                isListeningInternal = false
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Check microphone."
                    SpeechRecognizer.ERROR_CLIENT -> "Client speech recognition error."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                    SpeechRecognizer.ERROR_NETWORK -> "Network error during speech processing."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout."
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please speak closer to microphone."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy. Please try again."
                    SpeechRecognizer.ERROR_SERVER -> "Server error. Try again."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard. Try speaking louder."
                    else -> "Voice recognition error ($error)"
                }
                Log.w(TAG, "Speech recognition error: $errorMsg ($error)")
                onStateChanged?.invoke(VoiceInputState.Error(errorMsg))
            }

            override fun onResults(results: Bundle?) {
                isListeningInternal = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognized = matches?.firstOrNull()?.trim() ?: ""
                if (recognized.isNotEmpty()) {
                    onFinalTranscript?.invoke(recognized)
                    onStateChanged?.invoke(VoiceInputState.Success(recognized))
                } else {
                    onStateChanged?.invoke(VoiceInputState.Idle)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull()?.trim() ?: ""
                if (partial.isNotEmpty()) {
                    onPartialTranscript?.invoke(partial)
                    onStateChanged?.invoke(VoiceInputState.Listening(rmsDb = 5f, partialText = partial))
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
