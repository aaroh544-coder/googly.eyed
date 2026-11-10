package com.example.bluey.audio

/**
 * Plays 16-bit mono PCM samples. [play] blocks until the sound has finished playing.
 */
expect class PlatformAudio() {
    fun play(samples: ShortArray, sampleRate: Int)
}
