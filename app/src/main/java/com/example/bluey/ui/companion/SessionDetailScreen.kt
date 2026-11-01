package com.example.bluey.ui.companion

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluey.model.BlueySession
import com.example.bluey.model.Palette
import com.example.bluey.model.TranscriptEntry
import com.example.bluey.model.TranscriptKind
import com.example.bluey.ui.theme.Fredoka
import com.example.bluey.ui.theme.IBMPlexMono
import com.example.bluey.ui.theme.IBMPlexSans
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionDetailScreen(
    session: BlueySession,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .testTag("session_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        val dateFmt = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                        Text(
                            text = dateFmt.format(Date(session.started)),
                            fontFamily = Fredoka,
                            fontSize = 20.sp,
                            color = Color.White
                        )

                        val durationStr = "%d:%02d".format(session.durationSeconds / 60, session.durationSeconds % 60)
                        Text(
                            text = "$durationStr · ${session.questionCount} asked" +
                                if (session.transcribing > 0) " · transcribing ${session.transcribing} chunks…" else "",
                            fontFamily = IBMPlexMono,
                            fontSize = 11.sp,
                            color = Palette.InkSoft
                        )
                    }
                }

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Bluey Agent Prompt", session.buildAgentPrompt())
                        clipboard.setPrimaryClip(clip)
                        copied = true
                        Toast.makeText(context, "Prompt copied for agent!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.Berry3),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("copy_agent_prompt_button")
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy prompt",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (copied) "Copied" else "Copy prompt for agent",
                        fontFamily = IBMPlexSans,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transcript messages list
            val entries = session.displayEntries
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transcript entries yet in this session.",
                        fontFamily = IBMPlexSans,
                        fontSize = 14.sp,
                        color = Palette.InkSoft
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        EntryView(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryView(entry: TranscriptEntry) {
    val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())

    when (entry.kind) {
        TranscriptKind.TRANSCRIPT -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Speaker ${entry.speaker ?: "A"}",
                    fontFamily = IBMPlexMono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = getSpeakerColor(entry.speaker),
                    modifier = Modifier.width(76.dp)
                )
                Text(
                    text = entry.text,
                    fontFamily = IBMPlexSans,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = timeFmt.format(Date(entry.time)),
                    fontFamily = IBMPlexMono,
                    fontSize = 10.sp,
                    color = Palette.InkSoft.copy(alpha = 0.7f)
                )
            }
        }

        TranscriptKind.HEARD -> {
            Text(
                text = entry.text,
                fontFamily = IBMPlexSans,
                fontSize = 14.sp,
                color = Palette.InkSoft,
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
            )
        }

        TranscriptKind.ASKED -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Text(
                        text = "You asked",
                        fontFamily = IBMPlexMono,
                        fontSize = 10.sp,
                        color = Palette.Berry1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .background(Palette.Berry3, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = entry.text,
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        TranscriptKind.REPLY -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Bottom
            ) {
                MiniBluey(size = 30.dp, modifier = Modifier.padding(bottom = 2.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = entry.text,
                        fontFamily = Fredoka,
                        fontSize = 15.sp,
                        color = Palette.Ink
                    )
                }
            }
        }

        TranscriptKind.CODEX -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 36.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Code,
                    contentDescription = "Codex",
                    tint = Palette.Berry2,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = entry.text,
                    fontFamily = IBMPlexMono,
                    fontSize = 12.sp,
                    color = Palette.InkSoft
                )
            }
        }

        TranscriptKind.REPORT -> {
            var open by remember { mutableStateOf(false) }
            val lines = entry.text.split("\n\n")
            val title = lines.firstOrNull() ?: "Research"
            val bodyText = lines.drop(1).joinToString("\n\n")

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF0FF)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 36.dp, end = 24.dp)
                    .border(2.dp, Palette.Berry2, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { open = !open },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FindInPage, contentDescription = null, tint = Palette.Berry2)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(title, fontFamily = Fredoka, fontSize = 16.sp, color = Palette.Ink)
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Palette.Berry2),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (open) Icons.Default.Remove else Icons.Default.Add,
                                contentDescription = if (open) "Collapse" else "Expand",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    if (bodyText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = bodyText,
                            fontFamily = IBMPlexSans,
                            fontSize = 13.sp,
                            color = Palette.Ink,
                            maxLines = if (open) Int.MAX_VALUE else 3
                        )
                    }
                }
            }
        }
    }
}

private fun getSpeakerColor(speaker: String?): Color {
    val colors = listOf(
        Color(0xFF8FB3FF),
        Color(0xFF5BE49B),
        Color(0xFFFFB86B),
        Color(0xFFFF9AD0),
        Color(0xFFC79BFF),
        Color(0xFF6BE3E0)
    )
    val firstChar = speaker?.firstOrNull()?.code ?: return Palette.InkSoft
    return colors[firstChar % colors.size]
}
