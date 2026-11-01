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
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.bluey.model.FocusActivity
import com.example.bluey.model.FocusCheck
import com.example.bluey.model.FocusSession
import com.example.bluey.model.Palette
import com.example.bluey.ui.theme.Fredoka
import com.example.bluey.ui.theme.IBMPlexMono
import com.example.bluey.ui.theme.IBMPlexSans

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusChecksDialog(
    session: FocusSession,
    initialCategory: String = "all",
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedCheck by remember { mutableStateOf<FocusCheck?>(null) }

    val filteredChecks = remember(session.checks, selectedCategory) {
        session.checks.reversed().filter {
            if (it.activity == FocusActivity.PAUSED) return@filter false
            when (selectedCategory) {
                "all" -> true
                "on_task" -> it.activity == FocusActivity.ON_TASK
                "off_task" -> it.activity == FocusActivity.OFF_TASK
                "uncertain" -> it.activity in listOf(FocusActivity.UNCERTAIN, FocusActivity.UNAVAILABLE)
                else -> true
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Palette.Panel
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Focus checks",
                        fontFamily = Fredoka,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_checks_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf(
                        "all" to "All",
                        "on_task" to "On task",
                        "off_task" to "Off task",
                        "uncertain" to "Unknown"
                    )
                    for ((catKey, catName) in categories) {
                        FilterChip(
                            selected = selectedCategory == catKey,
                            onClick = { selectedCategory = catKey },
                            label = { Text(catName, fontFamily = IBMPlexSans, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Palette.Berry2,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.08f),
                                labelColor = Palette.InkSoft
                            )
                        )
                    }
                }

                Text(
                    text = "Each check is a periodic observation. Developer Mode checks every 10 real seconds with a 4× clock. Category probabilities and overall decision confidence are separate model estimates, not guarantees.",
                    fontFamily = IBMPlexSans,
                    fontSize = 12.sp,
                    color = Palette.InkSoft,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 8.dp))

                if (filteredChecks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No checks recorded in this category yet. Unchecked time has no confidence estimate.",
                            fontFamily = IBMPlexSans,
                            fontSize = 14.sp,
                            color = Palette.InkSoft
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredChecks, key = { it.id }) { check ->
                            CheckRowCard(
                                check = check,
                                onClick = { selectedCheck = check }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedCheck?.let { check ->
        CheckDetailDialog(check = check, onDismiss = { selectedCheck = null })
    }
}

@Composable
private fun CheckRowCard(
    check: FocusCheck,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("check_card_${check.activity.rawValue}"),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = check.activity.focusName,
                    fontFamily = Fredoka,
                    fontSize = 18.sp,
                    color = when (check.activity) {
                        FocusActivity.ON_TASK -> Palette.TaskGreen
                        FocusActivity.OFF_TASK -> Palette.TaskPeach
                        else -> Palette.InkSoft
                    }
                )
                Text(
                    text = FocusSession.formatClock(check.elapsed),
                    fontFamily = IBMPlexMono,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            val probEstimate = check.estimate(check.activity)
            val subtext = if (probEstimate != null) {
                val pPct = (probEstimate * 100).toInt()
                val cPct = (check.confidence * 100).toInt()
                "Category probability $pPct% · confidence $cPct%"
            } else if (check.activity == FocusActivity.UNAVAILABLE) {
                "Check unavailable or stale · no valid estimate"
            } else {
                val cPct = (check.confidence * 100).toInt()
                "Category probability not saved · confidence $cPct%"
            }

            Text(
                text = subtext,
                fontFamily = IBMPlexSans,
                fontSize = 12.sp,
                color = Palette.InkSoft
            )

            if (check.nudged) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ChatBubbleOutline,
                        contentDescription = "Nudged",
                        tint = Palette.Berry1,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Bluey gave a gentle nudge after this check",
                        fontFamily = IBMPlexSans,
                        fontSize = 11.sp,
                        color = Palette.Berry1
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckDetailDialog(
    check: FocusCheck,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Palette.Panel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Check details", fontFamily = Fredoka, fontSize = 20.sp, color = Color.White)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                DetailItem(label = "Observation", value = check.activity.focusName)
                DetailItem(label = "Active session time", value = FocusSession.formatClock(check.elapsed))

                Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 10.dp))

                Text("Category probabilities", fontFamily = IBMPlexSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
                for (cat in listOf(FocusActivity.ON_TASK, FocusActivity.OFF_TASK, FocusActivity.UNCERTAIN)) {
                    val p = check.estimate(cat)
                    val valueStr = if (p != null) "${(p * 100).toInt()}%" else "Not saved"
                    DetailItem(label = cat.focusName, value = valueStr)
                }

                Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 10.dp))

                Text("Decision confidence", fontFamily = IBMPlexSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
                DetailItem(label = "Overall confidence", value = "${(check.confidence * 100).toInt()}%")
                Text(
                    text = "Confidence is reported separately by the Decisions API. It is not the probability of a particular category.",
                    fontFamily = IBMPlexSans,
                    fontSize = 11.sp,
                    color = Palette.InkSoft,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (check.nudged) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Bluey gave a gentle nudge after this check",
                        fontFamily = Fredoka,
                        fontSize = 13.sp,
                        color = Palette.Berry1
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontFamily = IBMPlexSans, fontSize = 13.sp, color = Palette.InkSoft)
        Text(value, fontFamily = IBMPlexMono, fontSize = 13.sp, color = Color.White)
    }
}
