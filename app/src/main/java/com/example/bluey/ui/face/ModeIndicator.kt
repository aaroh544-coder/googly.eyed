package com.example.bluey.ui.face

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bluey.model.Palette

enum class VoiceState {
    ASLEEP, WAKING, LISTENING, ASKING, THINKING, SPEAKING
}

@Composable
fun ModeIndicator(
    state: VoiceState,
    modifier: Modifier = Modifier
) {
    if (state == VoiceState.ASLEEP) return

    val targetColor = when (state) {
        VoiceState.ASLEEP -> Color.Transparent
        VoiceState.WAKING -> Palette.WarningYellow
        VoiceState.LISTENING, VoiceState.ASKING -> Color(0xFF4F8BFF)
        VoiceState.THINKING -> Palette.ThinkingPurple
        VoiceState.SPEAKING -> Palette.SpeakingPink
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 250),
        label = "modeColor"
    )

    val borderWidth = when (state) {
        VoiceState.ASLEEP -> 0.dp
        VoiceState.WAKING -> 6.dp
        VoiceState.LISTENING -> 8.dp
        VoiceState.ASKING -> 14.dp
        VoiceState.THINKING, VoiceState.SPEAKING -> 8.dp
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .border(
                width = borderWidth,
                color = animatedColor.copy(alpha = pulseAlpha),
                shape = RoundedCornerShape(28.dp)
            )
    )
}
