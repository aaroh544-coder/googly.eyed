package com.example.bluey.ui.focus

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.bluey.model.FocusSession
import com.example.bluey.model.Palette
import com.example.bluey.ui.theme.Fredoka
import com.example.bluey.ui.theme.IBMPlexMono
import com.example.bluey.platform.formatDate
import com.example.bluey.ui.theme.IBMPlexSans

@Composable
fun FocusDashboardDialog(
    currentSession: FocusSession?,
    history: List<FocusSession>,
    developerMode: Boolean,
    onToggleDeveloperMode: (Boolean) -> Unit,
    onTestNudge: () -> Unit,
    onStartFocus: (task: String, minutes: Double) -> Unit,
    onPauseFocus: () -> Unit,
    onResumeFocus: () -> Unit,
    onEndFocus: () -> Unit,
    onDismiss: () -> Unit
) {
    var taskInput by remember { mutableStateOf("") }
    var minutesInput by remember { mutableDoubleStateOf(30.0) }
    var inspectingSessionCategory by remember { mutableStateOf<Pair<FocusSession, String>?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Top header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "bluey · focus friend",
                        fontFamily = Fredoka,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.testTag("back_to_bluey_button")
                    ) {
                        Text("Back to Bluey", fontFamily = IBMPlexSans)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Developer Mode Section
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Palette.Panel),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Developer Mode",
                                            fontFamily = Fredoka,
                                            fontSize = 18.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "4× clock and screen checks every 10 real seconds. Off-task checks nudge after ~20–30s. Applies immediately.",
                                            fontFamily = IBMPlexSans,
                                            fontSize = 12.sp,
                                            color = Palette.InkSoft,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                    Switch(
                                        checked = developerMode,
                                        onCheckedChange = { onToggleDeveloperMode(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Palette.Berry2
                                        ),
                                        modifier = Modifier.testTag("developer_mode_switch")
                                    )
                                }

                                if (developerMode) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = onTestNudge,
                                        colors = ButtonDefaults.buttonColors(containerColor = Palette.Berry3),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("test_nudge_button")
                                    ) {
                                        Text("Test a nudge", fontFamily = IBMPlexSans, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Active or Finished Session Card
                    if (currentSession != null && currentSession.status != FocusSession.Status.ENDED) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Palette.Panel),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = currentSession.task,
                                        fontFamily = Fredoka,
                                        fontSize = 22.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "${formatDate(currentSession.startedAt, "MMM d, h:mm a")} · ${FocusSession.formatClock(currentSession.elapsed)} active",
                                        fontFamily = IBMPlexMono,
                                        fontSize = 12.sp,
                                        color = Palette.InkSoft
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { inspectingSessionCategory = currentSession to "all" }
                                    ) {
                                        FocusTimelineView(session = currentSession)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Category inspection buttons
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        CategoryLegend("On task", Palette.TaskGreen) {
                                            inspectingSessionCategory = currentSession to "on_task"
                                        }
                                        CategoryLegend("Off task", Palette.TaskPeach) {
                                            inspectingSessionCategory = currentSession to "off_task"
                                        }
                                        CategoryLegend("Unknown", Color.Gray) {
                                            inspectingSessionCategory = currentSession to "uncertain"
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = currentSession.detail,
                                        fontFamily = IBMPlexSans,
                                        fontSize = 13.sp,
                                        color = Palette.InkSoft
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Button(
                                            onClick = {
                                                if (currentSession.status == FocusSession.Status.RUNNING) onPauseFocus() else onResumeFocus()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Palette.Berry2),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.testTag("dashboard_toggle_pause_button")
                                        ) {
                                            Text(if (currentSession.status == FocusSession.Status.RUNNING) "Pause" else "Resume")
                                        }
                                        OutlinedButton(
                                            onClick = onEndFocus,
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.testTag("dashboard_end_focus_button")
                                        ) {
                                            Text("End focus")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Start Focus Section
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Palette.Panel),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "A little company. A little focus.",
                                        fontFamily = Fredoka,
                                        fontSize = 24.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Hold Bluey and say what you're working on, or type it here.",
                                        fontFamily = IBMPlexSans,
                                        fontSize = 13.sp,
                                        color = Palette.InkSoft,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )

                                    OutlinedTextField(
                                        value = taskInput,
                                        onValueChange = { taskInput = it },
                                        placeholder = { Text("What are we working on?", color = Palette.InkSoft) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Palette.Berry2,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("focus_task_input")
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Minutes Stepper
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "${minutesInput.toInt()} minutes",
                                            fontFamily = IBMPlexMono,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { if (minutesInput > 5) minutesInput -= 5.0 },
                                                modifier = Modifier.testTag("decrease_minutes_button")
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White)
                                            }
                                            IconButton(
                                                onClick = { if (minutesInput < 240) minutesInput += 5.0 },
                                                modifier = Modifier.testTag("increase_minutes_button")
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = {
                                            if (taskInput.isNotBlank()) {
                                                onStartFocus(taskInput.trim(), minutesInput)
                                                taskInput = ""
                                            }
                                        },
                                        enabled = taskInput.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Palette.Berry2,
                                            disabledContainerColor = Palette.Berry2.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("start_focus_button")
                                    ) {
                                        Text("Start focus", fontFamily = Fredoka, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Explanatory info
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "While focusing, Bluey periodically checks your active task every ${if (developerMode) 10 else 30} real seconds. Brief detours are fine. Stillness isn't distraction.",
                                fontFamily = IBMPlexSans,
                                fontSize = 12.sp,
                                color = Palette.InkSoft
                            )
                            Text(
                                text = "Screenshots aren't saved by Bluey. Pause, end, or disconnect stops monitoring. Resume is always manual.",
                                fontFamily = IBMPlexSans,
                                fontSize = 12.sp,
                                color = Palette.InkSoft
                            )
                        }
                    }

                    // History Section
                    item {
                        Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = "Your focus sessions",
                            fontFamily = Fredoka,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                        if (history.isEmpty()) {
                            Text(
                                text = "Your first timeline will appear here when you finish.",
                                fontFamily = IBMPlexSans,
                                fontSize = 13.sp,
                                color = Palette.InkSoft,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    items(history, key = { it.id }) { session ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = session.task,
                                    fontFamily = Fredoka,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "${formatDate(session.startedAt, "MMM d, h:mm a")} · ${FocusSession.formatClock(session.elapsed)} active",
                                    fontFamily = IBMPlexMono,
                                    fontSize = 11.sp,
                                    color = Palette.InkSoft
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { inspectingSessionCategory = session to "all" }
                                ) {
                                    FocusTimelineView(session = session)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    inspectingSessionCategory?.let { (session, cat) ->
        FocusChecksDialog(
            session = session,
            initialCategory = cat,
            onDismiss = { inspectingSessionCategory = null }
        )
    }
}

@Composable
private fun CategoryLegend(name: String, color: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(name, fontFamily = IBMPlexSans, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
    }
}
