package com.example.bluey.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class ChirpSynthesizer(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    var volume: Float = 0.85f

    fun playFocusChirp() {
        playFrequencies(
            listOf(
                ChirpSegment(startFreq = 880f, endFreq = 1400f, durationMs = 90),
                ChirpSegment(startFreq = 1400f, endFreq = 2200f, durationMs = 120)
            )
        )
    }

    fun playHappyChirp() {
        playFrequencies(
            listOf(
                ChirpSegment(startFreq = 950f, endFreq = 1300f, durationMs = 70),
                ChirpSegment(startFreq = 1300f, endFreq = 1800f, durationMs = 80),
                ChirpSegment(startFreq = 1800f, endFreq = 2600f, durationMs = 110)
            )
        )
    }

    fun playAloneChirp() {
        playFrequencies(
            listOf(
                ChirpSegment(startFreq = 1200f, endFreq = 1950f, durationMs = 95)
            )
        )
    }

    private data class ChirpSegment(val startFreq: Float, val endFreq: Float, val durationMs: Int)

    private fun playFrequencies(segments: List<ChirpSegment>) {
        if (volume <= 0.01f) return

        scope.launch {
            try {
                val sampleRate = 44100
                val totalDurationMs = segments.sumOf { it.durationMs }
                val totalSamples = (sampleRate * totalDurationMs / 1000)
                val buffer = ShortArray(totalSamples)

                var sampleIndex = 0
                for (segment in segments) {
                    val segSamples = (sampleRate * segment.durationMs / 1000)
                    var phase = 0.0
                    for (i in 0 until segSamples) {
                        val progress = i.toFloat() / segSamples.toFloat()
                        val freq = segment.startFreq + (segment.endFreq - segment.startFreq) * progress
                        val phaseIncrement = 2.0 * PI * freq / sampleRate
                        phase += phaseIncrement

                        // Envelope: smooth attack & decay
                        val attack = (i.toFloat() / (segSamples * 0.15f)).coerceIn(0f, 1f)
                        val decay = ((segSamples - i).toFloat() / (segSamples * 0.25f)).coerceIn(0f, 1f)
                        val env = attack * decay

                        val sampleValue = (sin(phase) * env * volume * Short.MAX_VALUE * 0.7f).toInt()
                        if (sampleIndex < totalSamples) {
                            buffer[sampleIndex++] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        }
                    }
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
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
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                kotlinx.coroutines.delay(totalDurationMs.toLong() + 50)
                audioTrack.release()
            } catch (_: Exception) {
                // Ignore audio synthesis exceptions on background threads
            }
        }
    }
}
