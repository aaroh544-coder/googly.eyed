package com.example.bluey.ui.focus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bluey.model.FocusActivity
import com.example.bluey.model.FocusSession
import com.example.bluey.model.Palette
import kotlin.math.max
import kotlin.math.min

@Composable
fun FocusTimelineView(
    session: FocusSession,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
    ) {
        // Background unchecked/unknown grey
        drawRect(color = Color.Gray.copy(alpha = 0.35f), size = size)

        val total = max(1.0, if (session.status == FocusSession.Status.ENDED) session.elapsed else session.duration)

        for (check in session.checks) {
            if (check.activity in listOf(FocusActivity.ON_TASK, FocusActivity.OFF_TASK)) {
                val span = check.sampleDuration ?: if (session.isDeveloperMode) 10.0 else 30.0
                val start = max(0.0, check.elapsed - span)
                val x = (start / total * size.width).toFloat()
                val w = (min(span, check.elapsed) / total * size.width).toFloat()

                val color = if (check.activity == FocusActivity.ON_TASK) {
                    Palette.TaskGreen
                } else {
                    Palette.TaskPeach
                }

                drawRect(
                    color = color,
                    topLeft = Offset(x, 0f),
                    size = Size(max(2f, w), size.height)
                )
            }
        }
    }
}
