package com.example.bluey.audio

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine

actual class PlatformAudio actual constructor() {
    actual fun play(samples: ShortArray, sampleRate: Int) {
        val format = AudioFormat(sampleRate.toFloat(), 16, 1, true, false)
        val line = AudioSystem.getLine(DataLine.Info(SourceDataLine::class.java, format)) as SourceDataLine

        try {
            line.open(format)
            line.start()
            val bytes = ByteArray(samples.size * 2)
            for (i in samples.indices) {
                val value = samples[i].toInt()
                bytes[i * 2] = (value and 0xFF).toByte()
                bytes[i * 2 + 1] = ((value shr 8) and 0xFF).toByte()
            }
            line.write(bytes, 0, bytes.size)
            line.drain()
        } finally {
            line.stop()
            line.close()
        }
    }
}
