package com.example.bluey.animator

import kotlin.math.sqrt

class Spring(
    var value: Float,
    val stiffness: Float = 200f,
    val damping: Float = 0.7f
) {
    var velocity: Float = 0f

    fun step(target: Float, dt: Float) {
        val c = 2f * sqrt(stiffness) * damping
        velocity += (stiffness * (target - value) - c * velocity) * dt
        value += velocity * dt
    }
}
