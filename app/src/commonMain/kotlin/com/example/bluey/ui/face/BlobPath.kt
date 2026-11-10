package com.example.bluey.ui.face

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import com.example.bluey.model.Palette

object BlobPath {
    /**
     * The organic blueberry body outline matching CSS border-radius:
     * 52% 48% 46% 54% / 58% 56% 44% 42%
     */
    fun createBlobPath(r: Rect): Path {
        val w = r.width
        val h = r.height
        val tlW = 0.52f * w
        val tlH = 0.58f * h
        val trW = 0.48f * w
        val trH = 0.56f * h
        val brW = 0.46f * w
        val brH = 0.44f * h
        val blW = 0.54f * w
        val blH = 0.42f * h
        val k = 0.5523f

        return Path().apply {
            moveTo(r.left + tlW, r.top)
            lineTo(r.right - trW, r.top)
            cubicTo(
                r.right - trW * (1f - k), r.top,
                r.right, r.top + trH * (1f - k),
                r.right, r.top + trH
            )
            lineTo(r.right, r.bottom - brH)
            cubicTo(
                r.right, r.bottom - brH * (1f - k),
                r.right - brW * (1f - k), r.bottom,
                r.right - brW, r.bottom
            )
            lineTo(r.left + blW, r.bottom)
            cubicTo(
                r.left + blW * (1f - k), r.bottom,
                r.left, r.bottom - blH * (1f - k),
                r.left, r.bottom - blH
            )
            lineTo(r.left, r.top + tlH)
            cubicTo(
                r.left, r.top + tlH * (1f - k),
                r.left + tlW * (1f - k), r.top,
                r.left + tlW, r.top
            )
            close()
        }
    }

    /**
     * Blueberry crown on top of his head (SVG points from design in a 60x40 box).
     */
    fun createCrownPath(r: Rect): Path {
        val pts = listOf(
            30f to 4f,
            36f to 16f,
            52f to 12f,
            40f to 24f,
            46f to 36f,
            30f to 28f,
            14f to 36f,
            20f to 24f,
            8f to 12f,
            24f to 16f
        )
        return Path().apply {
            for ((i, pt) in pts.withIndex()) {
                val x = r.left + pt.first / 60f * r.width
                val y = r.top + pt.second / 40f * r.height
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
    }

    /**
     * Blueberry gradient at 150° from the design canvas.
     */
    fun createBlueberryGradient(r: Rect): Brush {
        return Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to Palette.Berry1,
                0.34f to Palette.Berry2,
                0.68f to Palette.Berry3,
                1.0f to Palette.Berry4
            ),
            start = Offset(r.left + 0.25f * r.width, r.top + 0.067f * r.height),
            end = Offset(r.left + 0.75f * r.width, r.top + 0.933f * r.height)
        )
    }

    /**
     * Soft radial shine highlight on top-left of the blueberry body.
     */
    fun createShineGradient(center: Offset, radius: Float): Brush {
        return Brush.radialGradient(
            colors = listOf(
                androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                androidx.compose.ui.graphics.Color.White.copy(alpha = 0f)
            ),
            center = center,
            radius = radius
        )
    }
}
