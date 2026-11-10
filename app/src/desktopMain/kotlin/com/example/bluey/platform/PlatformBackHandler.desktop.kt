package com.example.bluey.platform

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Windows has no system back gesture; the UI exposes explicit Back buttons instead.
}
