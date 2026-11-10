package com.example.bluey

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.example.bluey.ui.theme.BlueyTheme
import com.example.bluey.viewmodel.BlueyViewModel
import java.awt.Dimension

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Bluey",
        state = rememberWindowState(size = DpSize(960.dp, 720.dp))
    ) {
        LaunchedEffect(Unit) {
            window.minimumSize = Dimension(480, 420)
        }

        BlueyTheme {
            BlueyMainScreen(viewModel = remember { BlueyViewModel() })
        }
    }
}
