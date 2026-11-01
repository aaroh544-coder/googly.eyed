package com.example.bluey.ui.companion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.bluey.model.Palette
import com.example.bluey.ui.face.BlobPath

@Composable
fun MiniBluey(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val body = Rect(0f, 0f, w, h)
            val blob = BlobPath.createBlobPath(body)

            drawPath(blob, color = Palette.Berry3)
            drawPath(blob, brush = BlobPath.createBlueberryGradient(body))

            // Two googly eyes
            val eyeRadius = w * 0.13f
            val pupilRadius = w * 0.06f
            val eyeCenters = listOf(
                Offset(w * 0.35f, h * 0.45f),
                Offset(w * 0.65f, h * 0.45f)
            )

            for (center in eyeCenters) {
                // Sclera
                drawCircle(color = Color.White, radius = eyeRadius, center = center)
                // Pupil with slight offset
                drawCircle(
                    color = Palette.Ink,
                    radius = pupilRadius,
                    center = Offset(center.x + w * 0.02f, center.y + w * 0.02f)
                )
            }
        }
    }
}
