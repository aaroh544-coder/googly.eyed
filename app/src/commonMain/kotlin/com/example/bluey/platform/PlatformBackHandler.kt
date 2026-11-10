package com.example.bluey.platform

import androidx.compose.runtime.Composable

/**
 * Handles the platform's system back gesture. Windows has no system back gesture, so the
 * desktop implementation does nothing.
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)
