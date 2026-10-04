package com.example.service

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

enum class AmbientSoundType(val displayName: String, val icon: String) {
    NONE("Off", "volume_off"),
    RAIN("Gentle Rain", "water_drop"),
    WHITE_NOISE("White Noise", "air"),
    WAVES("Ocean Waves", "tsunami"),
    LOFI_PULSE("Deep Lo-Fi Tone", "graphic_eq")
}

class SoundPlayer {

    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null
    private var ambientJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playCompletionChime() {
        scope.launch {
            try {
                val durationMs = 1200
                val numSamples = (sampleRate * durationMs / 1000)
                val buffer = ShortArray(numSamples)

                val freq1 = 528.0 // Harmonic frequency
                val freq2 = 1056.0

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    // Smooth exponential decay
                    val envelope = Math.exp(-3.5 * i / numSamples)
                    val s1 = sin(2 * PI * freq1 * time)
                    val s2 = 0.5 * sin(2 * PI * freq2 * time)
                    val sample = ((s1 + s2) / 1.5 * envelope * Short.MAX_VALUE).toInt()
                    buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val minBufSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(maxOf(minBufSize, buffer.size * 2))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                // Let it play, then release after duration
                kotlinx.coroutines.delay(durationMs.toLong() + 200)
                track.stop()
                track.release()
            } catch (_: Exception) {
                // Ignore audio play errors
            }
        }
    }

    fun startAmbient(type: AmbientSoundType) {
        stopAmbient()
        if (type == AmbientSoundType.NONE) return

        ambientJob = scope.launch {
            try {
                val bufferSize = sampleRate / 2 // 0.5 sec chunk
                val track = AudioTrack.Builder()
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
                    .setBufferSizeInBytes(bufferSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack = track
                track.play()

                val buffer = ShortArray(bufferSize)
                val random = Random()
                var lastOutput = 0.0
                var phase = 0.0

                while (isActive) {
                    when (type) {
                        AmbientSoundType.WHITE_NOISE -> {
                            for (i in 0 until bufferSize) {
                                val white = (random.nextDouble() * 2.0 - 1.0)
                                // Soft low-pass filter for cozy brownian/pink noise
                                lastOutput = (lastOutput * 0.95) + (white * 0.05)
                                buffer[i] = (lastOutput * 0.25 * Short.MAX_VALUE).toInt().toShort()
                            }
                        }
                        AmbientSoundType.RAIN -> {
                            for (i in 0 until bufferSize) {
                                val rand = random.nextDouble()
                                val drop = if (rand > 0.985) (random.nextDouble() * 2 - 1) * 0.7 else 0.0
                                val baseNoise = (random.nextDouble() * 2.0 - 1.0) * 0.08
                                lastOutput = (lastOutput * 0.88) + (baseNoise + drop) * 0.12
                                buffer[i] = (lastOutput * 0.4 * Short.MAX_VALUE).toInt().toShort()
                            }
                        }
                        AmbientSoundType.WAVES -> {
                            val swellFreq = 0.12 // 8-second wave period
                            for (i in 0 until bufferSize) {
                                phase += (2 * PI * swellFreq) / sampleRate
                                if (phase > 2 * PI) phase -= 2 * PI
                                val swell = (sin(phase) + 1.0) / 2.0 // 0 to 1
                                val white = (random.nextDouble() * 2.0 - 1.0)
                                lastOutput = (lastOutput * 0.92) + (white * 0.08)
                                val amplitude = (0.05 + swell * 0.25)
                                buffer[i] = (lastOutput * amplitude * Short.MAX_VALUE).toInt().toShort()
                            }
                        }
                        AmbientSoundType.LOFI_PULSE -> {
                            val toneFreq = 85.0 // Warm low hum
                            val pulseFreq = 0.25 // 4-second pulse
                            for (i in 0 until bufferSize) {
                                phase += (2 * PI * toneFreq) / sampleRate
                                if (phase > 2 * PI) phase -= 2 * PI
                                val pulsePhase = (phase * (pulseFreq / toneFreq)) % (2 * PI)
                                val mod = 0.6 + 0.4 * sin(pulsePhase)
                                val tone = sin(phase) * mod
                                val subtleNoise = (random.nextDouble() * 2.0 - 1.0) * 0.03
                                buffer[i] = ((tone * 0.18 + subtleNoise) * Short.MAX_VALUE).toInt().toShort()
                            }
                        }
                        AmbientSoundType.NONE -> break
                    }
                    track.write(buffer, 0, buffer.size)
                }
            } catch (_: Exception) {
                // Ignore audio stream errors
            }
        }
    }

    fun stopAmbient() {
        ambientJob?.cancel()
        ambientJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {
        }
        audioTrack = null
    }
}
