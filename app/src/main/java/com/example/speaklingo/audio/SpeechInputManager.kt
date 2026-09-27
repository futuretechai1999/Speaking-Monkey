package com.example.speaklingo.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

/**
 * Real-time Speech-to-Text Manager leveraging Android's SpeechRecognizer API.
 * Supports streaming partial transcriptions, live audio amplitude (RMS dB),
 * and language selection for pronunciation and conversational practice.
 */
class SpeechInputManager(
    private val context: Context,
    private val onListeningChanged: (Boolean) -> Unit,
    private val onResultReceived: (String) -> Unit,
    private val onPartialResult: ((String) -> Unit)? = null,
    private val onRmsChanged: ((Float) -> Unit)? = null,
    private val onErrorReceived: (String) -> Unit = {}
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isCurrentlyActive = false

    fun startListening(languageCode: String = "en-US") {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onErrorReceived("Speech recognition service is not available on this device.")
            return
        }

        stopListening()

        mainHandler.post {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            isCurrentlyActive = true
                            onListeningChanged(true)
                        }

                        override fun onBeginningOfSpeech() {
                            isCurrentlyActive = true
                            onListeningChanged(true)
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            // Android rmsdB is typically between -2 dB (quiet) to 10+ dB (loud speech)
                            // Normalize to a 0.0f..1.0f range for Compose audio waveforms
                            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1.0f)
                            onRmsChanged?.invoke(normalized)
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            isCurrentlyActive = false
                            onListeningChanged(false)
                        }

                        override fun onError(error: Int) {
                            isCurrentlyActive = false
                            onListeningChanged(false)
                            onRmsChanged?.invoke(0.0f)
                            val message = when (error) {
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please speak clearly into the microphone."
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timed out. Please try speaking again."
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording issue. Please check microphone."
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                                SpeechRecognizer.ERROR_NETWORK,
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout. Offline speech recognition fallback."
                                SpeechRecognizer.ERROR_CLIENT -> "Client speech recognition reset."
                                else -> "Speech capture code: $error"
                            }
                            Log.w("SpeechInputManager", "SpeechRecognizer error: $error ($message)")
                            onErrorReceived(message)
                        }

                        override fun onResults(results: Bundle?) {
                            isCurrentlyActive = false
                            onListeningChanged(false)
                            onRmsChanged?.invoke(0.0f)

                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val finalSpeech = matches?.firstOrNull() ?: ""
                            if (finalSpeech.isNotBlank()) {
                                onPartialResult?.invoke(finalSpeech)
                                onResultReceived(finalSpeech)
                            } else {
                                onErrorReceived("No words recognized.")
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val partial = matches?.firstOrNull() ?: ""
                            if (partial.isNotBlank()) {
                                onPartialResult?.invoke(partial)
                                onResultReceived(partial)
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1800L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1400L)
                }

                speechRecognizer?.startListening(intent)
                isCurrentlyActive = true
                onListeningChanged(true)
            } catch (e: Exception) {
                Log.e("SpeechInputManager", "Failed to start listening", e)
                isCurrentlyActive = false
                onListeningChanged(false)
                onErrorReceived("Failed to start microphone: ${e.message}")
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                isCurrentlyActive = false
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
                speechRecognizer = null
                onListeningChanged(false)
                onRmsChanged?.invoke(0.0f)
            } catch (e: Exception) {
                Log.e("SpeechInputManager", "Error stopping recognizer", e)
            }
        }
    }

    fun destroy() {
        stopListening()
    }
}
