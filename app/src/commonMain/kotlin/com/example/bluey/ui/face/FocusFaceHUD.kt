package com.example.bluey.ui.face

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluey.model.FocusActivity
import com.example.bluey.model.FocusSession
import com.example.bluey.model.Palette
import com.example.bluey.ui.theme.Fredoka
import com.example.bluey.ui.theme.IBMPlexMono
import com.example.bluey.ui.theme.IBMPlexSans

@Composable
fun FocusFaceHUD(
    session: FocusSession?,
    voiceState: VoiceState,
    developerMode: Boolean,
    nudgeText: String,
    companionConnected: Boolean,
    onOpenFocus: () -> Unit,
    onOpenCompanion: () -> Unit,
    onOpenPairing: () -> Unit,
    onPauseFocus: () -> Unit,
    onResumeFocus: () -> Unit,
    onEndFocus: () -> Unit,
    onTestChirp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Task info & status
            Column(modifier = Modifier.weight(1f, fill = false)) {
                val taskTitle = if (session != null && session.status != FocusSession.Status.ENDED) {
                    session.task
                } else {
                    "What's the mission?"
                }

                Text(
                    text = taskTitle,
                    fontFamily = Fredoka,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Status line
                val statusText = when {
                    !companionConnected -> "Pair your computer to focus together"
                    session == null || session.status == FocusSession.Status.ENDED -> "Hold Bluey and describe a task"
                    session.status == FocusSession.Status.PAUSED -> "Paused"
                    else -> {
                        val lastCheck = session.checks.lastOrNull { it.activity != FocusActivity.PAUSED }
                        if (lastCheck == null) {
                            "Focusing together"
                        } else {
                            when (lastCheck.activity) {
                                FocusActivity.OFF_TASK -> {
                                    val pct = (lastCheck.probability * 100).toInt()
                                    "Off task · $pct% estimate${if (lastCheck.nudged) " · nudged" else ""}"
                                }
                                FocusActivity.ON_TASK -> "On task"
                                FocusActivity.UNCERTAIN -> "Not sure yet · checking again"
                                else -> "Last check unavailable · trying again"
                            }
                        }
                    }
                }

                Text(
                    text = statusText,
                    fontFamily = IBMPlexMono,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                if (developerMode) {
                    Text(
                        text = "Developer Mode · 4× clock · checks every 10s",
                        fontFamily = IBMPlexMono,
                        fontSize = 10.sp,
                        color = Palette.WarningYellow.copy(alpha = 0.9f)
                    )
                }

                // Speech bubble nudge text from Bluey
                AnimatedVisibility(
                    visible = nudgeText.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text(
                            text = nudgeText,
                            fontFamily = Fredoka,
                            fontSize = 13.sp,
                            color = Palette.Ink,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Top-right action icons: Sound test + Bluey's App
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onTestChirp() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .testTag("sound_chirp_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Test chirp",
                        tint = Palette.Berry1,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { onOpenCompanion() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .testTag("open_bluey_app_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Open Bluey",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // BOTTOM BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Action buttons row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!companionConnected) {
                    HudIconButton(
                        icon = Icons.Default.QrCode,
                        contentDescription = "Pair computer",
                        testTag = "pair_mac_button",
                        onClick = { onOpenPairing() }
                    )
                } else if (session != null && session.status != FocusSession.Status.ENDED) {
                    if (session.status == FocusSession.Status.RUNNING) {
                        HudIconButton(
                            icon = Icons.Default.Pause,
                            contentDescription = "Pause focus",
                            testTag = "pause_focus_button",
                            onClick = { onPauseFocus() }
                        )
                    } else {
                        HudIconButton(
                            icon = Icons.Default.PlayArrow,
                            contentDescription = "Resume focus",
                            testTag = "resume_focus_button",
                            onClick = { onResumeFocus() }
                        )
                    }

                    HudIconButton(
                        icon = Icons.Default.Stop,
                        contentDescription = "End focus",
                        testTag = "end_focus_button",
                        onClick = { onEndFocus() }
                    )
                }

                HudIconButton(
                    icon = Icons.Default.TrackChanges,
                    contentDescription = "Focus sessions",
                    testTag = "focus_sessions_button",
                    onClick = { onOpenFocus() }
                )

                // Voice status indicator
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.09f)),
                    contentAlignment = Alignment.Center
                ) {
                    val (voiceIcon, voiceTint) = when (voiceState) {
                        VoiceState.ASLEEP -> Icons.Default.MicOff to Color.White.copy(alpha = 0.35f)
                        VoiceState.LISTENING, VoiceState.ASKING -> Icons.Default.Mic to Color.White
                        else -> Icons.Default.VolumeUp to Palette.SpeakingPink
                    }
                    Icon(
                        imageVector = voiceIcon,
                        contentDescription = "Voice status",
                        tint = voiceTint,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            // Big Countdown clock display
            val countdownSeconds = if (session != null && session.status != FocusSession.Status.ENDED) {
                session.remaining
            } else 1800.0

            Text(
                text = FocusSession.formatClock(countdownSeconds),
                fontFamily = Fredoka,
                fontWeight = FontWeight.SemiBold,
                fontSize = 68.sp,
                color = Color.White,
                letterSpacing = 2.sp,
                modifier = Modifier.testTag("focus_countdown_text")
            )
        }
    }
}

@Composable
private fun HudIconButton(
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.09f))
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(17.dp)
        )
    }
}
