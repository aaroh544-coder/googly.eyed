package com.example.bluey.animator

import androidx.compose.ui.geometry.Offset
import com.example.bluey.model.FaceState
import com.example.bluey.model.Mood
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

data class FaceFrame(
    val gaze: Offset,
    val mood: Mood,
    val talk: Float,
    val closed: Float,
    val breathe: Float,
    val time: Float,
    val pupil: Float,
    val browLift: Float,
    val browTilt: Float,
    val lean: Float,
    val hop: Float,
    val blush: Float,
    val squint: Float
)

class FaceAnimator {
    private var target = FaceState()
    private var lastPacketTime = -100.0

    private val gazeX = Spring(0f, stiffness = 260f, damping = 0.82f)
    private val gazeY = Spring(0f, stiffness = 260f, damping = 0.82f)
    private val pupil = Spring(1f, stiffness = 160f, damping = 0.5f)
    private val browLift = Spring(0f, stiffness = 180f, damping = 0.55f)
    private val browTilt = Spring(0f, stiffness = 150f, damping = 0.6f)
    private val lean = Spring(0f, stiffness = 60f, damping = 0.8f)
    private val hop = Spring(0f, stiffness = 260f, damping = 0.35f)
    private val blush = Spring(0.25f, stiffness = 40f, damping = 1f)
    private val squint = Spring(0f, stiffness = 150f, damping = 0.7f)

    private var talk = 0f
    private var mood = Mood.LISTENING
    private var moodChanged = 0.0
    private var blinkStart = -1.0
    private var doubleBlink = false
    private var nextBlink = 2.0
    private var lastTime: Double? = null
    private var nextSaccade = 0.0
    private var wanderTarget = Offset(0f, -0.3f)
    private var jitter = Offset.Zero
    private var nextJitter = 0.0
    private var nextHop = 6.0

    var touchGaze: Offset? = null
    var localMood: Mood? = null
    var localTalk: () -> Float = { 0f }
    var awake: Boolean = false

    fun receive(face: FaceState, atTime: Double) {
        target = face
        lastPacketTime = atTime
    }

    fun step(nowSeconds: Double): FaceFrame {
        val dt = min(nowSeconds - (lastTime ?: nowSeconds), 1.0 / 20.0).toFloat()
        lastTime = nowSeconds

        val live = (nowSeconds - lastPacketTime) < 2.5
        val wantedMood = localMood ?: if (live) target.mood else Mood.LISTENING
        if (wantedMood != mood) {
            mood = wantedMood
            moodChanged = nowSeconds
            blinkStart = nowSeconds
            if (mood != Mood.SLEEPY && mood != Mood.RESTING) {
                hop.velocity += 260f
                pupil.velocity += 3f
            }
        }

        // Where to look
        var want: Offset
        val currentTouch = touchGaze
        if (currentTouch != null) {
            want = currentTouch
        } else if (live) {
            want = Offset(target.gazeX, target.gazeY)
        } else {
            if (nowSeconds > nextSaccade) {
                wanderTarget = Offset(
                    Random.nextFloat() * 1.8f - 0.9f,
                    Random.nextFloat() * 1.3f - 0.9f
                )
                nextSaccade = nowSeconds + (0.6 + Random.nextDouble() * 1.6)
            }
            want = wanderTarget
        }

        if (mood == Mood.THINKING) {
            want = Offset(0.6f + 0.08f * sin(nowSeconds * 1.3).toFloat(), -0.85f)
        }

        if (nowSeconds > nextJitter) {
            val amount = if (!awake) 0.015f else when (mood) {
                Mood.TALKING -> 0.07f
                Mood.THINKING -> 0.05f
                else -> 0.02f
            }
            jitter = Offset(
                (Random.nextFloat() * 2f - 1f) * amount,
                (Random.nextFloat() * 2f - 1f) * amount
            )
            nextJitter = nowSeconds + (0.5 + Random.nextDouble() * 0.9)
        }

        gazeX.step(want.x + jitter.x, dt)
        gazeY.step(want.y + jitter.y, dt)

        val wantTalkLive = if (live) target.talk else {
            if (localMood == Mood.TALKING) {
                (0.5f + 0.5f * sin(nowSeconds * 19.0).toFloat() * sin(nowSeconds * 7.3).toFloat())
            } else 0f
        }
        val finalWantTalk = max(wantTalkLive, localTalk())
        talk += (finalWantTalk - talk) * min(1f, dt * 25f)

        var wantPupil = 1.0f
        var wantLift = 0.0f
        var wantTilt = 0.0f
        var wantBlush = 0.25f
        var wantSquint = 0.0f

        when (mood) {
            Mood.LISTENING -> {
                wantPupil = 1.12f
                wantLift = 10f
            }
            Mood.TALKING -> {
                wantPupil = 1.05f
                wantLift = 6f + talk * 18f
                wantTilt = 0.05f * sin(nowSeconds * 2.3).toFloat()
            }
            Mood.POINTING -> {
                wantPupil = 0.95f
                wantLift = -4f
                wantTilt = -0.14f
            }
            Mood.THINKING -> {
                wantPupil = 0.9f
                wantLift = 4f
                wantTilt = 0.22f
            }
            Mood.HAPPY -> {
                wantPupil = 1.2f
                wantLift = 16f
                wantBlush = 0.85f
                wantSquint = 1f
            }
            Mood.RESTING -> {
                wantPupil = 0.9f
                wantLift = -8f
                wantBlush = 0.15f
            }
            Mood.SLEEPY -> {
                wantPupil = 0.88f
                wantLift = -10f
                wantTilt = 0.12f
                wantBlush = 0.15f
            }
        }

        pupil.step(wantPupil, dt)
        browLift.step(wantLift, dt)
        browTilt.step(wantTilt, dt)
        blush.step(wantBlush, dt)
        squint.step(wantSquint, dt)
        lean.step(-gazeX.value * 0.045f, dt)

        if (nowSeconds > nextHop) {
            if (talk < 0.05f && mood != Mood.SLEEPY && mood != Mood.RESTING) {
                hop.velocity += (180f + Random.nextFloat() * 140f)
            }
            nextHop = nowSeconds + (7.0 + Random.nextDouble() * 7.0)
        }
        hop.step(0f, dt)

        // Blinking
        val drowsy = mood == Mood.SLEEPY
        if (nowSeconds > nextBlink) {
            blinkStart = nowSeconds
            doubleBlink = !drowsy && Random.nextDouble() < 0.25
            nextBlink = nowSeconds + if (drowsy) (1.6 + Random.nextDouble() * 1.4) else (2.0 + Random.nextDouble() * 3.0)
        }
        val p = (nowSeconds - blinkStart) / (if (drowsy) 0.7 else 0.15)
        var closed = if (p in 0.0..1.0) sin(PI * p).toFloat() else 0f
        if (doubleBlink && p in 1.3..2.3) {
            closed = sin(PI * (p - 1.3)).toFloat()
        }

        val breathe = sin(nowSeconds * (if (mood == Mood.RESTING) 1.1 else 1.8)).toFloat()

        return FaceFrame(
            gaze = Offset(gazeX.value, gazeY.value),
            mood = mood,
            talk = talk,
            closed = closed,
            breathe = breathe,
            time = nowSeconds.toFloat(),
            pupil = pupil.value,
            browLift = browLift.value,
            browTilt = browTilt.value,
            lean = lean.value,
            hop = hop.value,
            blush = blush.value,
            squint = max(0f, min(1f, squint.value))
        )
    }
}
