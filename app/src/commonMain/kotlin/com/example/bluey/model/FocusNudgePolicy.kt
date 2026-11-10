package com.example.bluey.model

import kotlin.math.max

/**
 * Counts consecutive fresh off-task choices with majority probability.
 * The API's separate confidence score is diagnostic, not a second probability threshold.
 */
class FocusNudgePolicy(
    private val maximumGap: Double = 45.0,
    private val developerMode: Boolean = false
) {
    private val run = mutableListOf<FocusCheck>()
    private var lastNudge = Double.NEGATIVE_INFINITY

    fun reset() {
        run.clear()
    }

    fun observe(check: FocusCheck, ageSeconds: Double): Boolean {
        if (ageSeconds < 0 || ageSeconds > 20 || !check.elapsed.isFinite() ||
            check.activity != FocusActivity.OFF_TASK ||
            !check.probability.isFinite() || !check.confidence.isFinite() ||
            check.probability !in 0.5..1.0 || check.confidence !in 0.0..1.0
        ) {
            reset()
            return false
        }

        val last = run.lastOrNull()
        if (last != null) {
            if (check.elapsed <= last.elapsed) {
                reset()
                return false
            }
            if (check.elapsed - last.elapsed > maximumGap) {
                reset()
            }
        }

        run.add(check)

        val strength = run.map { it.probability }.sum() / run.size.toDouble()
        val minimum = if (developerMode) 40.0 else 120.0
        val maximum = if (developerMode) 80.0 else 240.0
        val required = if (strength >= 0.95) {
            minimum
        } else {
            maximum - max(0.0, strength - 0.5) / 0.45 * (maximum - minimum)
        }

        val firstElapsed = run.firstOrNull()?.elapsed ?: check.elapsed
        val durationSpan = check.elapsed - firstElapsed

        if (run.size >= 2 && durationSpan >= max(minimum, required) && check.elapsed - lastNudge >= 120.0) {
            lastNudge = check.elapsed
            reset()
            return true
        }

        return false
    }
}
