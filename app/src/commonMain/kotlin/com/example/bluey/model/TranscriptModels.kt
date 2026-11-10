package com.example.bluey.model

import com.example.bluey.platform.formatDate
import com.example.bluey.platform.nowMillis
import com.example.bluey.platform.randomId
import kotlinx.serialization.Serializable

@Serializable
enum class TranscriptKind {
    HEARD, ASKED, REPLY, REPORT, TRANSCRIPT, CODEX
}

@Serializable
data class TranscriptEntry(
    val id: String = randomId(),
    val kind: TranscriptKind,
    var text: String,
    val time: Long = nowMillis(),
    val itemID: String? = null,
    val speaker: String? = null
)

@Serializable
data class PendingChunk(
    val index: Int,
    val started: Long
)

@Serializable
data class BlueySession(
    val id: String = randomId(),
    val started: Long = nowMillis(),
    var ended: Long? = null,
    val entries: MutableList<TranscriptEntry> = mutableListOf(),
    var notesPath: String? = null,
    val pending: MutableList<PendingChunk>? = mutableListOf()
) {
    val questionCount: Int
        get() = entries.count { it.kind == TranscriptKind.ASKED }

    val summary: String
        get() {
            val first = entries.firstOrNull { it.kind == TranscriptKind.ASKED && it.text.isNotBlank() }
                ?: displayEntries.firstOrNull { it.kind == TranscriptKind.TRANSCRIPT || it.kind == TranscriptKind.HEARD }
            return first?.text ?: "Nothing said yet"
        }

    val durationSeconds: Long
        get() = ((ended ?: nowMillis()) - started) / 1000

    val displayEntries: List<TranscriptEntry>
        get() {
            val properUntil = entries.filter { it.kind == TranscriptKind.TRANSCRIPT }.maxOfOrNull { it.time } ?: 0L
            return entries
                .filter { it.text.isNotBlank() && !(it.kind == TranscriptKind.HEARD && it.time <= properUntil) }
                .sortedBy { it.time }
        }

    val transcribing: Int
        get() = pending?.size ?: 0

    val exportText: String
        get() {
            val lines = mutableListOf(
                "Bluey session, ${formatDate(started, "MMM d, h:mm a")}",
                ""
            )
            for (entry in displayEntries) {
                val who = when (entry.kind) {
                    TranscriptKind.TRANSCRIPT -> "Speaker ${entry.speaker ?: "?"}"
                    TranscriptKind.HEARD -> "You"
                    TranscriptKind.ASKED -> "You asked"
                    TranscriptKind.REPLY -> "Bluey"
                    TranscriptKind.REPORT -> "Research"
                    TranscriptKind.CODEX -> "Codex"
                }
                lines.add("[${formatDate(entry.time, "h:mm:ss a")}] $who: ${entry.text}")
            }
            return lines.joinToString("\n")
        }

    fun buildAgentPrompt(): String {
        val isoDay = formatDate(started, "yyyy-MM-dd")
        val root = "~/Documents/Bluey Notes"
        val path = notesPath ?: "$root/ (folder starting $isoDay)"
        return """
I recorded a meeting with my Bluey app on ${formatDate(started, "EEEE, MMMM d, yyyy 'at' h:mm a")}. The notes are on my Mac in $path

- notes.md: the full transcript with timestamps and speaker labels (A, B, … per five-minute chunk), plus my questions to Bluey, its replies and research reports. Start here.
- session.json: the same data, structured.
- audio/: the original recording in five-minute chunks.

All my sessions are in $root/, one folder per session (see README.md there). Read the notes, then help me with: 
        """.trimIndent()
    }
}
