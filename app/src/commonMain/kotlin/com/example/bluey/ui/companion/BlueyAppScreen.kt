package com.example.bluey.ui.companion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluey.model.BlueySession
import com.example.bluey.model.Palette
import com.example.bluey.platform.PlatformBackHandler
import com.example.bluey.platform.formatDate
import com.example.bluey.platform.formatDuration
import com.example.bluey.ui.face.VoiceState
import com.example.bluey.ui.theme.Fredoka
import com.example.bluey.ui.theme.IBMPlexMono
import com.example.bluey.ui.theme.IBMPlexSans

@Composable
fun BlueyAppScreen(
    sessions: List<BlueySession>,
    currentSessionId: String?,
    voiceState: VoiceState,
    connectedMacName: String?,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onToggleListening: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenPairing: () -> Unit,
    onClose: () -> Unit
) {
    var activeDetailSession by remember { mutableStateOf<BlueySession?>(null) }
    var selectedPortraitTab by remember { mutableIntStateOf(0) }

    PlatformBackHandler {
        if (activeDetailSession != null) {
            activeDetailSession = null
        } else {
            onClose()
        }
    }

    if (activeDetailSession != null) {
        SessionDetailScreen(
            session = activeDetailSession!!,
            onBack = { activeDetailSession = null }
        )
        return
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isCompact = maxWidth < 620.dp

            if (isCompact) {
                // Adaptive Portrait Layout for Phones
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Character bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Palette.Panel)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MiniBluey(size = 40.dp)
                            Column {
                                Text("Bluey", fontFamily = Fredoka, fontSize = 22.sp, color = Color.White)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (connectedMacName != null) Palette.TaskGreen else Palette.InkSoft.copy(alpha = 0.5f))
                                    )
                                    Text(
                                        text = connectedMacName ?: "Looking for your computer",
                                        fontFamily = IBMPlexSans,
                                        fontSize = 11.sp,
                                        color = Palette.InkSoft
                                    )
                                }
                            }
                        }

                        IconButtonBack(onClick = onClose)
                    }

                    TabRow(
                        selectedTabIndex = selectedPortraitTab,
                        containerColor = Palette.Panel,
                        contentColor = Palette.Berry2
                    ) {
                        Tab(
                            selected = selectedPortraitTab == 0,
                            onClick = { selectedPortraitTab = 0 },
                            text = { Text("Voice History (${sessions.size})", fontFamily = IBMPlexSans, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedPortraitTab == 1,
                            onClick = { selectedPortraitTab = 1 },
                            text = { Text("Companion Settings", fontFamily = IBMPlexSans, fontSize = 13.sp) }
                        )
                    }

                    if (selectedPortraitTab == 0) {
                        SessionListContent(
                            sessions = sessions,
                            currentSessionId = currentSessionId,
                            onSelectSession = { activeDetailSession = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(16.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp)
                        ) {
                            SidebarControls(
                                voiceState = voiceState,
                                connectedMacName = connectedMacName,
                                volume = volume,
                                onVolumeChange = onVolumeChange,
                                onToggleListening = onToggleListening,
                                onOpenFocus = onOpenFocus,
                                onOpenPairing = onOpenPairing,
                                onClose = onClose
                            )
                        }
                    }
                }
            } else {
                // Wide / Landscape Layout
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .width(300.dp)
                            .fillMaxHeight()
                            .background(Palette.Panel.copy(alpha = 0.85f))
                            .verticalScroll(rememberScrollState())
                            .padding(22.dp)
                    ) {
                        // Character header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MiniBluey(size = 54.dp)
                            Column {
                                Text(
                                    text = "Bluey",
                                    fontFamily = Fredoka,
                                    fontSize = 28.sp,
                                    color = Color.White
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (connectedMacName != null) Palette.TaskGreen else Palette.InkSoft.copy(alpha = 0.5f))
                                    )
                                    Text(
                                        text = connectedMacName ?: "Looking for your computer",
                                        fontFamily = IBMPlexSans,
                                        fontSize = 12.sp,
                                        color = Palette.InkSoft,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        SidebarControls(
                            voiceState = voiceState,
                            connectedMacName = connectedMacName,
                            volume = volume,
                            onVolumeChange = onVolumeChange,
                            onToggleListening = onToggleListening,
                            onOpenFocus = onOpenFocus,
                            onOpenPairing = onOpenPairing,
                            onClose = onClose
                        )
                    }

                    // Session list column
                    SessionListContent(
                        sessions = sessions,
                        currentSessionId = currentSessionId,
                        onSelectSession = { activeDetailSession = it },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SidebarControls(
    voiceState: VoiceState,
    connectedMacName: String?,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onToggleListening: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenPairing: () -> Unit,
    onClose: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = onOpenFocus,
            colors = ButtonDefaults.buttonColors(containerColor = Palette.Berry2),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("app_focus_sessions_button")
        ) {
            Icon(Icons.Default.TrackChanges, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Focus sessions", fontFamily = IBMPlexSans)
        }

        OutlinedButton(
            onClick = onOpenPairing,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("app_pair_mac_button")
        ) {
            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pair computer (QR)", fontFamily = IBMPlexSans)
        }

        val isListening = voiceState != VoiceState.ASLEEP
        Button(
            onClick = onToggleListening,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isListening) Color(0xFFE5547A) else Palette.Berry3
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("app_toggle_listening_button")
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isListening) "Stop listening" else "Start listening",
                fontFamily = Fredoka,
                fontSize = 16.sp
            )
        }

        Text(
            text = if (!isListening) {
                "He listens the whole session. Hold his face to ask something, let go and he answers."
            } else {
                "Listening. Hold his face to ask; use Focus sessions for the task and timer."
            },
            fontFamily = IBMPlexSans,
            fontSize = 11.sp,
            color = Palette.InkSoft
        )

        Divider(color = Color.White.copy(alpha = 0.08f))

        Column {
            Text(
                text = "CHIRP VOLUME",
                fontFamily = IBMPlexMono,
                fontSize = 11.sp,
                color = Palette.InkSoft
            )
            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = Palette.Berry1,
                    activeTrackColor = Palette.Berry2
                ),
                modifier = Modifier.testTag("volume_slider")
            )
        }

        OutlinedButton(
            onClick = onClose,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().testTag("app_back_to_face_button")
        ) {
            Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back to his face", fontFamily = IBMPlexSans, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SessionListContent(
    sessions: List<BlueySession>,
    currentSessionId: String?,
    onSelectSession: (BlueySession) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Voice history",
                fontFamily = Fredoka,
                fontSize = 24.sp,
                color = Color.White
            )
            Text(
                text = "${sessions.size}",
                fontFamily = IBMPlexMono,
                fontSize = 13.sp,
                color = Palette.InkSoft
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No sessions yet",
                        fontFamily = Fredoka,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Start one and your transcript and his replies show up here.",
                        fontFamily = IBMPlexSans,
                        fontSize = 13.sp,
                        color = Palette.InkSoft
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sessions, key = { it.id }) { session ->
                    SessionRowCard(
                        session = session,
                        isLive = session.id == currentSessionId,
                        onClick = { onSelectSession(session) }
                    )
                }
            }
        }
    }
}

@Composable
private fun IconButtonBack(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("app_portrait_back_button")
    ) {
        Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Face", fontFamily = IBMPlexSans, fontSize = 12.sp)
    }
}

@Composable
private fun SessionRowCard(
    session: BlueySession,
    isLive: Boolean,
    onClick: () -> Unit
) {
    val durationStr = formatDuration(session.durationSeconds)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("session_item_${session.id}"),
        colors = CardDefaults.cardColors(containerColor = Palette.Panel),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatDate(session.started, "MMM d, h:mm a"),
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    if (isLive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Palette.TaskGreen, RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                fontFamily = IBMPlexMono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = session.summary,
                    fontFamily = IBMPlexSans,
                    fontSize = 13.sp,
                    color = Palette.InkSoft,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = durationStr,
                    fontFamily = IBMPlexMono,
                    fontSize = 12.sp,
                    color = Palette.InkSoft
                )
                Text(
                    text = "${session.questionCount} asked",
                    fontFamily = IBMPlexMono,
                    fontSize = 11.sp,
                    color = Palette.Berry1
                )
            }
        }
    }
}
