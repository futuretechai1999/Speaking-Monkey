package com.example.speaklingo.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs

/**
 * VoiceRecorderManager: Records real microphone audio into 16kHz 16-bit Mono PCM WAV format.
 * Generates standard WAV audio bytes and Base64 encoding ready for multimodal Gemini audio analysis
 * (gemini-3.5-transcribe and gemini-3.5-flash).
 */
class VoiceRecorderManager(
    private val onAmplitudeChanged: ((Float) -> Unit)? = null
) {
    companion object {
        private const val TAG = "VoiceRecorderManager"
        const val SAMPLE_RATE = 16000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val pcmOutputStream = ByteArrayOutputStream()
    private var isRecording = false

    val isCurrentlyRecording: Boolean
        get() = isRecording

    @SuppressLint("MissingPermission")
    fun startRecording(coroutineScope: CoroutineScope) {
        if (isRecording) return

        try {
            val minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            ).coerceAtLeast(2048)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                minBufferSize * 2
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed")
                return
            }

            pcmOutputStream.reset()
            audioRecord?.startRecording()
            isRecording = true

            recordingJob = coroutineScope.launch(Dispatchers.IO) {
                val buffer = ByteArray(minBufferSize)
                while (isActive && isRecording) {
                    val readBytes = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (readBytes > 0) {
                        pcmOutputStream.write(buffer, 0, readBytes)

                        // Calculate live amplitude for waveform UI
                        var sum = 0L
                        for (i in 0 until readBytes step 2) {
                            if (i + 1 < readBytes) {
                                val sample = (buffer[i].toInt() and 0xFF) or (buffer[i + 1].toInt() shl 8)
                                sum += abs(sample.toShort().toInt())
                            }
                        }
                        val avg = sum / (readBytes / 2).coerceAtLeast(1)
                        val normalized = (avg / 6000f).coerceIn(0.05f, 1.0f)
                        onAmplitudeChanged?.invoke(normalized)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting voice recording", e)
            isRecording = false
        }
    }

    fun stopRecording(): ByteArray? {
        if (!isRecording) return null
        isRecording = false
        recordingJob?.cancel()
        recordingJob = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping AudioRecord", e)
        } finally {
            audioRecord = null
            onAmplitudeChanged?.invoke(0.0f)
        }

        val rawPcm = pcmOutputStream.toByteArray()
        if (rawPcm.isEmpty()) return null

        return addWavHeader(rawPcm, SAMPLE_RATE, 1, 16)
    }

    fun stopRecordingBase64(): String? {
        val wavBytes = stopRecording() ?: return null
        return Base64.encodeToString(wavBytes, Base64.NO_WRAP)
    }

    /**
     * Packages raw PCM audio data into a valid RIFF/WAVE file header.
     */
    private fun addWavHeader(
        pcmData: ByteArray,
        sampleRate: Int,
        channels: Short,
        bitsPerSample: Short
    ): ByteArray {
        val totalAudioLen = pcmData.size
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8

        val header = ByteBuffer.allocate(44).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            // RIFF chunk descriptor
            put('R'.code.toByte()); put('I'.code.toByte()); put('F'.code.toByte()); put('F'.code.toByte())
            putInt(totalDataLen)
            put('W'.code.toByte()); put('A'.code.toByte()); put('V'.code.toByte()); put('E'.code.toByte())

            // "fmt " sub-chunk
            put('f'.code.toByte()); put('m'.code.toByte()); put('t'.code.toByte()); put(' '.code.toByte())
            putInt(16) // Subchunk1Size for PCM
            putShort(1) // AudioFormat 1 = PCM
            putShort(channels)
            putInt(sampleRate)
            putInt(byteRate)
            putShort((channels * bitsPerSample / 8).toShort()) // BlockAlign
            putShort(bitsPerSample)

            // "data" sub-chunk
            put('d'.code.toByte()); put('a'.code.toByte()); put('t'.code.toByte()); put('a'.code.toByte())
            putInt(totalAudioLen)
        }.array()

        val wavStream = ByteArrayOutputStream(header.size + pcmData.size)
        wavStream.write(header)
        wavStream.write(pcmData)
        return wavStream.toByteArray()
    }
}
