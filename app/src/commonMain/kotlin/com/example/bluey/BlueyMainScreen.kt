package com.example.bluey

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.bluey.ui.companion.BlueyAppScreen
import com.example.bluey.ui.companion.PairingDialog
import com.example.bluey.ui.face.FaceCanvas
import com.example.bluey.ui.face.FocusFaceHUD
import com.example.bluey.ui.face.ModeIndicator
import com.example.bluey.ui.focus.FocusDashboardDialog
import com.example.bluey.viewmodel.BlueyViewModel

@Composable
fun BlueyMainScreen(viewModel: BlueyViewModel) {
    val currentFocus by viewModel.currentFocus.collectAsState()
    val focusHistory by viewModel.focusHistory.collectAsState()
    val developerMode by viewModel.developerMode.collectAsState()
    val nudgeText by viewModel.nudgeText.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val companionConnected by viewModel.companionConnected.collectAsState()
    val connectedMacName by viewModel.connectedMacName.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val voiceSessions by viewModel.voiceSessions.collectAsState()
    val currentVoiceSessionId by viewModel.currentVoiceSessionId.collectAsState()

    var showFocusDashboard by remember { mutableStateOf(false) }
    var showCompanionApp by remember { mutableStateOf(false) }
    var showPairingDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Living animated face with organic spring physics
        FaceCanvas(
            animator = viewModel.animator,
            onDoubleTap = { viewModel.toggleListening() },
            onHoldStart = { viewModel.beginAsk() },
            onHoldEnd = { viewModel.endAsk() }
        )

        // Pulsing border indicating voice status
        ModeIndicator(state = voiceState)

        // Top mission title, bottom timer, action icons
        FocusFaceHUD(
            session = currentFocus,
            voiceState = voiceState,
            developerMode = developerMode,
            nudgeText = nudgeText,
            companionConnected = companionConnected,
            onOpenFocus = { showFocusDashboard = true },
            onOpenCompanion = { showCompanionApp = true },
            onOpenPairing = { showPairingDialog = true },
            onPauseFocus = { viewModel.pauseFocus() },
            onResumeFocus = { viewModel.resumeFocus() },
            onEndFocus = { viewModel.endFocus() },
            onTestChirp = { viewModel.chirpSynthesizer.playFocusChirp() }
        )

        // Focus Dashboard Modal
        if (showFocusDashboard) {
            FocusDashboardDialog(
                currentSession = currentFocus,
                history = focusHistory,
                developerMode = developerMode,
                onToggleDeveloperMode = { viewModel.setDeveloperMode(it) },
                onTestNudge = { viewModel.testNudge() },
                onStartFocus = { task, minutes -> viewModel.startFocus(task, minutes) },
                onPauseFocus = { viewModel.pauseFocus() },
                onResumeFocus = { viewModel.resumeFocus() },
                onEndFocus = { viewModel.endFocus() },
                onDismiss = { showFocusDashboard = false }
            )
        }

        // Bluey App & Voice Session History Modal
        if (showCompanionApp) {
            BlueyAppScreen(
                sessions = voiceSessions,
                currentSessionId = currentVoiceSessionId,
                voiceState = voiceState,
                connectedMacName = connectedMacName,
                volume = volume,
                onVolumeChange = { viewModel.setVolume(it) },
                onToggleListening = { viewModel.toggleListening() },
                onOpenFocus = {
                    showCompanionApp = false
                    showFocusDashboard = true
                },
                onOpenPairing = {
                    showCompanionApp = false
                    showPairingDialog = true
                },
                onClose = { showCompanionApp = false }
            )
        }

        // Pairing Modal
        if (showPairingDialog) {
            PairingDialog(
                connected = companionConnected,
                currentMacName = connectedMacName,
                onPairSuccess = { macName -> viewModel.pairMac(macName) },
                onDisconnect = { viewModel.disconnectCompanion() },
                onDismiss = { showPairingDialog = false }
            )
        }
    }
}
