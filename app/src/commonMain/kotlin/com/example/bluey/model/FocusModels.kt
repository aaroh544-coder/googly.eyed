package com.example.bluey.model

import com.example.bluey.platform.nowMillis
import com.example.bluey.platform.randomId
import com.example.bluey.platform.twoDigits
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Serializable
enum class FocusActivity(val rawValue: String, val focusName: String) {
    ON_TASK("on_task", "On task"),
    OFF_TASK("off_task", "Off task"),
    UNCERTAIN("uncertain", "Unknown"),
    UNAVAILABLE("unavailable", "Unavailable"),
    PAUSED("paused", "Paused");

    companion object {
        fun fromRaw(value: String): FocusActivity =
            entries.firstOrNull { it.rawValue == value } ?: UNCERTAIN
    }
}

@Serializable
data class FocusCheck(
    val id: String = randomId(),
    val elapsed: Double,
    val activity: FocusActivity,
    val probability: Double = 0.0,
    val confidence: Double = 0.0,
    val probabilities: Map<String, Double>? = null,
    val sampleDuration: Double? = null,
    val nudged: Boolean = false
) {
    fun estimate(forCategory: FocusActivity): Double? {
        if (activity !in listOf(FocusActivity.ON_TASK, FocusActivity.OFF_TASK, FocusActivity.UNCERTAIN)) return null
        if (forCategory !in listOf(FocusActivity.ON_TASK, FocusActivity.OFF_TASK, FocusActivity.UNCERTAIN)) return null

        val value = probabilities?.get(forCategory.rawValue)
            ?: if (forCategory == FocusActivity.OFF_TASK) probability else null
        return if (value != null && value.isFinite() && value in 0.0..1.0) value else null
    }
}

@Serializable
data class FocusSession(
    val id: String = randomId(),
    val task: String,
    val duration: Double = 1800.0,
    val startedAt: Long = nowMillis(),
    var elapsed: Double = 0.0,
    var status: Status = Status.RUNNING,
    var detail: String = "First check in 30 seconds",
    val checks: MutableList<FocusCheck> = mutableListOf(),
    var developerMode: Boolean? = false,
    var usedDeveloperMode: Boolean? = false,
    var realElapsed: Double? = 0.0
) {
    @Serializable
    enum class Status { RUNNING, PAUSED, ENDED }

    val isDeveloperMode: Boolean get() = developerMode == true
    val clockRate: Double get() = if (isDeveloperMode) 4.0 else 1.0
    val checkInterval: Double get() = if (isDeveloperMode) 10.0 else 30.0
    val observationDuration: Double get() = checkInterval * clockRate
    val realActiveElapsed: Double get() = realElapsed ?: elapsed
    val remaining: Double get() = max(0.0, duration - elapsed)

    companion object {
        fun formatClock(seconds: Double): String {
            val total = max(0, seconds.roundToInt()).toLong()
            return "${twoDigits(total / 60)}:${twoDigits(total % 60)}"
        }
    }

    fun advance(seconds: Double) {
        if (status != Status.RUNNING || !seconds.isFinite() || seconds <= 0) return
        val step = min(seconds, remaining / clockRate)
        realElapsed = realActiveElapsed + step
        elapsed = min(duration, elapsed + seconds * clockRate)
        if (elapsed >= duration) {
            status = Status.ENDED
            detail = "Time's up. Lovely bit of focus."
        }
    }

    fun pause(reason: String = "Paused") {
        if (status != Status.RUNNING) return
        status = Status.PAUSED
        detail = reason
        checks.add(FocusCheck(elapsed = elapsed, activity = FocusActivity.PAUSED))
    }

    fun resume() {
        if (status != Status.PAUSED) return
        status = Status.RUNNING
        detail = "Resumed. Next check in ${checkInterval.toInt()} seconds"
    }

    fun setDeveloperModeEnabled(enabled: Boolean) {
        developerMode = enabled
        if (enabled) usedDeveloperMode = true
        detail = "Next check in ${checkInterval.toInt()} seconds"
    }

    fun end() {
        status = Status.ENDED
        detail = "Session finished"
    }
}
