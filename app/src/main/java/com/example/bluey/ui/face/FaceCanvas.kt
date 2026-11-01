package com.example.bluey.ui.face

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotateRad
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import com.example.bluey.animator.FaceAnimator
import com.example.bluey.animator.FaceFrame
import com.example.bluey.model.Mood
import com.example.bluey.model.Palette
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

private const val DESIGN_WIDTH = 844f
private const val DESIGN_HEIGHT = 390f

@Composable
fun FaceCanvas(
    animator: FaceAnimator,
    modifier: Modifier = Modifier,
    onDoubleTap: () -> Unit = {},
    onHoldStart: () -> Unit = {},
    onHoldEnd: () -> Unit = {}
) {
    var frameTimeSeconds by remember { mutableFloatStateOf(0f) }

    // High performance frame loop for physics-driven organic animation
    LaunchedEffect(Unit) {
        val startNanos = System.nanoTime()
        while (true) {
            withFrameNanos { nowNanos ->
                frameTimeSeconds = ((nowNanos - startNanos) / 1_000_000_000.0).toFloat()
            }
        }
    }

    val currentFrame = remember(frameTimeSeconds) {
        animator.step(frameTimeSeconds.toDouble())
    }

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onDoubleTap()
                    },
                    onPress = {
                        val pressStart = System.currentTimeMillis()
                        val released = tryAwaitRelease()
                        val pressDuration = System.currentTimeMillis() - pressStart
                        if (released && pressDuration >= 280) {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            onHoldEnd()
                        } else if (!released) {
                            onHoldEnd()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val gx = (offset.x / size.width.toFloat() * 2f - 1f).coerceIn(-1f, 1f)
                        val gy = (offset.y / size.height.toFloat() * 2f - 1f).coerceIn(-1f, 1f)
                        animator.touchGaze = Offset(gx, gy)
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        onHoldStart()
                    },
                    onDrag = { change, _ ->
                        val gx = (change.position.x / size.width.toFloat() * 2f - 1f).coerceIn(-1f, 1f)
                        val gy = (change.position.y / size.height.toFloat() * 2f - 1f).coerceIn(-1f, 1f)
                        animator.touchGaze = Offset(gx, gy)
                    },
                    onDragEnd = {
                        animator.touchGaze = null
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        onHoldEnd()
                    },
                    onDragCancel = {
                        animator.touchGaze = null
                        onHoldEnd()
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawFace(currentFrame, size)
        }
    }
}

private fun DrawScope.drawFace(f: FaceFrame, canvasSize: Size) {
    drawRect(color = Color.Black, size = canvasSize)

    val scaleFactor = max(canvasSize.width / DESIGN_WIDTH, canvasSize.height / DESIGN_HEIGHT)
    val transX = (canvasSize.width - DESIGN_WIDTH * scaleFactor) / 2f
    val transY = canvasSize.height - DESIGN_HEIGHT * scaleFactor

    translate(left = transX, top = transY) {
        scale(scaleFactor, scaleFactor, pivot = Offset.Zero) {
            val bounce = -f.talk * 16f + f.breathe * 3f - f.hop
            val body = Rect(12f, 44f + bounce, 12f + 820f, 44f + bounce + 700f)
            val pivot = Offset(body.center.x, body.bottom)

            translate(left = pivot.x, top = pivot.y) {
                rotateRad(radians = f.lean, pivot = Offset.Zero) {
                    val stretch = 1f + (f.hop / 900f).coerceIn(-0.03f, 0.03f)
                    scale(2f - stretch, stretch, pivot = Offset.Zero) {
                        translate(left = -pivot.x, top = -pivot.y) {
                            renderBlueberryBody(f, body)
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.renderBlueberryBody(f: FaceFrame, body: Rect) {
    val blob = BlobPath.createBlobPath(body)

    // Base body fill
    drawPath(blob, color = Palette.Berry3)
    drawPath(blob, brush = BlobPath.createBlueberryGradient(body))

    // Radial shine
    clipPath(blob) {
        val shineCenter = Offset(body.left + 246f, body.top + 126f)
        drawPath(blob, brush = BlobPath.createShineGradient(shineCenter, 243f))

        // Glossy highlight sparkle
        drawOval(
            color = Color.White.copy(alpha = 0.35f),
            topLeft = Offset(body.left + 190f, body.top + 40f),
            size = Size(46f, 22f)
        )
    }

    // Crown on top of head
    val crownRect = Rect(
        body.left + 368f,
        body.top - 20f - f.browLift * 0.3f,
        body.left + 368f + 84f,
        body.top - 20f - f.browLift * 0.3f + 56f
    )
    drawPath(BlobPath.createCrownPath(crownRect), color = Palette.Nose)

    val eyeY = body.top + 157f
    val eyes = listOf(
        Offset(body.left + 280f, eyeY),
        Offset(body.left + 540f, eyeY)
    )
    val squash = 1f - f.talk * 0.12f

    // Cheeks
    for ((i, c) in eyes.withIndex()) {
        val side = if (i == 0) -1f else 1f
        val cheek = Rect(c.x - 45f + side * 62f, c.y + 96f, c.x - 45f + side * 62f + 90f, c.y + 96f + 42f)
        drawOval(
            color = Palette.CheekBlush.copy(alpha = (0.25f + 0.45f * f.blush).coerceIn(0f, 1f)),
            topLeft = Offset(cheek.left, cheek.top),
            size = Size(cheek.width, cheek.height)
        )
    }

    // Eyebrows & Eyes
    for ((i, c) in eyes.withIndex()) {
        val side = if (i == 0) -1f else 1f

        // Eyebrows
        val browY = c.y - 112f - (f.browLift.coerceIn(-10f, 18f)) * 0.7f
        val browRot = f.browTilt * side * -1f + if (f.mood == Mood.THINKING && i == 1) -0.25f else 0f
        rotateRad(browRot, pivot = Offset(c.x + side * 6f, browY)) {
            val arch = Path().apply {
                moveTo(c.x + side * 6f - 38f, browY + 6f)
                quadraticBezierTo(
                    c.x + side * 6f, browY - 12f,
                    c.x + side * 6f + 38f, browY + 6f
                )
            }
            drawPath(
                arch,
                color = Palette.Nose,
                style = Stroke(width = 14f, cap = StrokeCap.Round)
            )
        }

        // Eyes
        when {
            f.mood == Mood.RESTING -> {
                // Peacefully sleeping arc
                val shut = Path().apply {
                    moveTo(c.x - 62f, c.y + 6f)
                    quadraticBezierTo(c.x, c.y + 52f, c.x + 62f, c.y + 6f)
                }
                drawPath(shut, color = Palette.Ink, style = Stroke(width = 18f, cap = StrokeCap.Round))
            }
            f.mood == Mood.HAPPY && f.squint > 0.6f -> {
                // Cheerful squint arc
                val arc = Path().apply {
                    moveTo(c.x - 60f, c.y + 34f)
                    quadraticBezierTo(c.x, c.y - 54f, c.x + 60f, c.y + 34f)
                }
                drawPath(arc, color = Palette.Ink, style = Stroke(width = 24f, cap = StrokeCap.Round))
            }
            else -> {
                val open = max(0.06f, 1f - f.closed) * squash
                val whiteRect = Rect(c.x - 95f, c.y - 95f * open, c.x + 95f, c.y + 95f * open)
                val whitePath = Path().apply { addOval(whiteRect) }

                drawPath(whitePath, color = Color.White)

                if (open > 0.2f) {
                    var g = f.gaze
                    val len = hypot(g.x, g.y)
                    if (len > 1f) g = Offset(g.x / len, g.y / len)

                    val reach = 50f
                    val r = 46f * f.pupil
                    var pupilPos = Offset(c.x + g.x * reach - side * 3f, c.y + g.y * reach * open)
                    if (f.mood == Mood.SLEEPY) {
                        pupilPos = Offset(pupilPos.x, max(pupilPos.y, c.y) + 28f)
                    }

                    clipPath(whitePath) {
                        // Pupil
                        drawCircle(color = Palette.Ink, radius = r, center = pupilPos)

                        // Catchlights
                        drawOval(
                            color = Color.White,
                            topLeft = Offset(pupilPos.x - r * 0.58f, pupilPos.y - r * 0.72f),
                            size = Size(r * 0.56f, r * 0.56f)
                        )
                        drawOval(
                            color = Color.White.copy(alpha = 0.85f),
                            topLeft = Offset(pupilPos.x + r * 0.28f, pupilPos.y + r * 0.22f),
                            size = Size(r * 0.24f, r * 0.24f)
                        )

                        // Squint lower lid
                        if (f.squint > 0.02f) {
                            val lidRect = Rect(
                                c.x - 110f,
                                c.y + 95f * open - 70f * f.squint,
                                c.x + 110f,
                                c.y + 95f * open - 70f * f.squint + 160f
                            )
                            val lidPath = Path().apply { addOval(lidRect) }
                            drawPath(lidPath, brush = BlobPath.createBlueberryGradient(body))
                        }

                        // Sleepy droopy lid
                        if (f.mood == Mood.SLEEPY) {
                            val droop = 0.5f + 0.06f * sin(f.time * 1.3f)
                            val droopHeight = whiteRect.height * droop + 10f
                            val droopRect = Rect(c.x - 100f, whiteRect.top - 10f, c.x + 100f, whiteRect.top - 10f + droopHeight)
                            drawRect(brush = BlobPath.createBlueberryGradient(body), topLeft = Offset(droopRect.left, droopRect.top), size = Size(droopRect.width, droopRect.height))
                            drawLine(
                                color = Palette.Nose.copy(alpha = 0.7f),
                                start = Offset(c.x - 92f, whiteRect.top + whiteRect.height * droop),
                                end = Offset(c.x + 92f, whiteRect.top + whiteRect.height * droop),
                                strokeWidth = 8f,
                                cap = StrokeCap.Round
                            )
                        }

                        // Pointing focus lid
                        if (f.mood == Mood.POINTING) {
                            val focusRect = Rect(c.x - 100f, whiteRect.top - 20f, c.x + 100f, whiteRect.top + 20f)
                            drawRect(brush = BlobPath.createBlueberryGradient(body), topLeft = Offset(focusRect.left, focusRect.top), size = Size(focusRect.width, focusRect.height))
                        }
                    }
                }
            }
        }
    }

    // Thinking dots
    if (f.mood == Mood.THINKING) {
        for (idx in 0..2) {
            val wobble = sin(f.time * 3f + idx) * 4f
            val dotRect = Rect(
                body.left + 690f + idx * 26f,
                body.top + 40f - idx * 12f + wobble,
                body.left + 690f + idx * 26f + 16f,
                body.top + 40f - idx * 12f + wobble + 16f
            )
            drawOval(
                color = Color.White.copy(alpha = (1f - idx * 0.3f).coerceIn(0f, 1f)),
                topLeft = Offset(dotRect.left, dotRect.top),
                size = Size(dotRect.width, dotRect.height)
            )
        }
    }

    // Mouth / Nose
    val noseW = 60f - f.talk * 8f
    val noseH = 38f + f.talk * 34f
    val noseLeft = body.left + 410f - noseW / 2f
    val noseTop = body.top + 272f - f.talk * 6f

    if (f.mood == Mood.HAPPY && f.talk < 0.1f) {
        val smile = Path().apply {
            moveTo(noseLeft - 8f, noseTop + 8f)
            quadraticBezierTo(
                noseLeft + noseW / 2f, noseTop + 58f,
                noseLeft + noseW + 8f, noseTop + 8f
            )
            close()
        }
        drawPath(smile, color = Palette.Nose)
    } else {
        drawOval(
            color = Palette.Nose,
            topLeft = Offset(noseLeft, noseTop),
            size = Size(noseW, noseH)
        )
        if (f.talk > 0.25f) {
            val tongueW = noseW * 0.56f
            val tongueH = noseH * 0.3f
            drawOval(
                color = Palette.Tongue.copy(alpha = 0.9f),
                topLeft = Offset(noseLeft + (noseW - tongueW) / 2f, noseTop + noseH - tongueH * 1.2f),
                size = Size(tongueW, tongueH)
            )
        }
    }
}
