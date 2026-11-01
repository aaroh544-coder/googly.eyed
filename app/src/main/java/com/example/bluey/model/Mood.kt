package com.example.bluey.model

import kotlinx.serialization.Serializable

@Serializable
enum class Mood(val title: String) {
    LISTENING("Listening"),
    RESTING("Resting"),
    THINKING("Thinking"),
    TALKING("Talking"),
    POINTING("Pointing"),
    HAPPY("Happy"),
    SLEEPY("Sleepy");

    companion object {
        fun fromString(value: String): Mood =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: LISTENING
    }
}

@Serializable
data class FaceState(
    val gazeX: Float = 0f,
    val gazeY: Float = 0f,
    val mood: Mood = Mood.LISTENING,
    val talk: Float = 0f
)
