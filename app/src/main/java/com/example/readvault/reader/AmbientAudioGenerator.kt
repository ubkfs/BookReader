package com.example.readvault.reader

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Random
import kotlin.concurrent.thread
import kotlin.math.sin

enum class AmbientSoundType(val displayName: String) {
    OFF("Off"),
    RAIN("Gentle Rain"),
    OCEAN("Ocean Waves"),
    WHITE_NOISE("White Noise"),
    FOREST("Forest Breeze")
}

class AmbientAudioGenerator {
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var playThread: Thread? = null

    private val _currentSound = MutableStateFlow(AmbientSoundType.OFF)
    val currentSound = _currentSound.asStateFlow()

    private val sampleRate = 22050

    fun playSound(type: AmbientSoundType) {
        if (type == AmbientSoundType.OFF) {
            stop()
            return
        }

        stop()
        _currentSound.value = type
        isPlaying = true

        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ) * 2

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()

        playThread = thread(start = true, isDaemon = true) {
            val random = Random()
            val shortBuffer = ShortArray(bufferSize / 2)
            var phase = 0.0
            var wavePhase = 0.0
            var lastSample = 0.0

            while (isPlaying) {
                for (i in shortBuffer.indices) {
                    when (type) {
                        AmbientSoundType.WHITE_NOISE -> {
                            // Soft filtered white noise
                            val noise = (random.nextDouble() * 2.0 - 1.0)
                            lastSample = lastSample * 0.9 + noise * 0.1
                            shortBuffer[i] = (lastSample * 4000).toInt().coerceIn(-32768, 32767).toShort()
                        }
                        AmbientSoundType.RAIN -> {
                            // Low-pass filtered pink noise with droplet peaks
                            val rawNoise = (random.nextDouble() * 2.0 - 1.0)
                            lastSample = lastSample * 0.85 + rawNoise * 0.15
                            val droplet = if (random.nextInt(400) == 0) (random.nextDouble() * 8000) else 0.0
                            val sample = (lastSample * 5000 + droplet).toInt().coerceIn(-32768, 32767)
                            shortBuffer[i] = sample.toShort()
                        }
                        AmbientSoundType.OCEAN -> {
                            // Modulated noise with slow sinusoidal wave swell (0.15 Hz)
                            wavePhase += (2.0 * Math.PI * 0.15) / sampleRate
                            if (wavePhase > 2.0 * Math.PI) wavePhase -= 2.0 * Math.PI
                            val swell = (sin(wavePhase) * 0.5 + 0.5)

                            val rawNoise = (random.nextDouble() * 2.0 - 1.0)
                            lastSample = lastSample * 0.92 + rawNoise * 0.08
                            val sample = (lastSample * (2500 + swell * 6500)).toInt().coerceIn(-32768, 32767)
                            shortBuffer[i] = sample.toShort()
                        }
                        AmbientSoundType.FOREST -> {
                            // Soft warm breeze with gentle overtone
                            phase += (2.0 * Math.PI * 220.0) / sampleRate
                            val tone = sin(phase) * 600
                            val rawNoise = (random.nextDouble() * 2.0 - 1.0)
                            lastSample = lastSample * 0.94 + rawNoise * 0.06
                            val sample = (lastSample * 3500 + tone).toInt().coerceIn(-32768, 32767)
                            shortBuffer[i] = sample.toShort()
                        }
                        AmbientSoundType.OFF -> {
                            shortBuffer[i] = 0
                        }
                    }
                }
                audioTrack?.write(shortBuffer, 0, shortBuffer.size)
            }
        }
    }

    fun stop() {
        isPlaying = false
        _currentSound.value = AmbientSoundType.OFF
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        playThread = null
    }
}
