package com.example.bluey.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluey.animator.FaceAnimator
import com.example.bluey.audio.ChirpSynthesizer
import com.example.bluey.model.BlueySession
import com.example.bluey.model.FaceState
import com.example.bluey.model.FocusActivity
import com.example.bluey.model.FocusCheck
import com.example.bluey.model.FocusNudgePolicy
import com.example.bluey.model.FocusSession
import com.example.bluey.model.Mood
import com.example.bluey.model.TranscriptEntry
import com.example.bluey.model.TranscriptKind
import com.example.bluey.ui.face.VoiceState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class BlueyViewModel : ViewModel() {
    val animator = FaceAnimator()
    val chirpSynthesizer = ChirpSynthesizer(viewModelScope)

    private val _currentFocus = MutableStateFlow<FocusSession?>(null)
    val currentFocus: StateFlow<FocusSession?> = _currentFocus.asStateFlow()

    private val _focusHistory = MutableStateFlow<List<FocusSession>>(emptyList())
    val focusHistory: StateFlow<List<FocusSession>> = _focusHistory.asStateFlow()

    private val _developerMode = MutableStateFlow(false)
    val developerMode: StateFlow<Boolean> = _developerMode.asStateFlow()

    private val _nudgeText = MutableStateFlow("")
    val nudgeText: StateFlow<String> = _nudgeText.asStateFlow()

    private val _voiceState = MutableStateFlow(VoiceState.ASLEEP)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _connectedMacName = MutableStateFlow<String?>("MacBook Pro")
    val connectedMacName: StateFlow<String?> = _connectedMacName.asStateFlow()

    private val _companionConnected = MutableStateFlow(true)
    val companionConnected: StateFlow<Boolean> = _companionConnected.asStateFlow()

    private val _volume = MutableStateFlow(0.85f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _voiceSessions = MutableStateFlow<List<BlueySession>>(emptyList())
    val voiceSessions: StateFlow<List<BlueySession>> = _voiceSessions.asStateFlow()

    private val _currentVoiceSessionId = MutableStateFlow<String?>(null)
    val currentVoiceSessionId: StateFlow<String?> = _currentVoiceSessionId.asStateFlow()

    private var nudgePolicy = FocusNudgePolicy(maximumGap = 45.0, developerMode = false)
    private var tickerJob: Job? = null
    private var lastCheckTimeSeconds = 0.0

    init {
        // Seed an initial demo session in history so user sees past timelines immediately
        seedDemoData()
        startTicker()
    }

    private fun seedDemoData() {
        val pastSession = FocusSession(
            task = "Reviewing Bluey focus architecture & Kotlin Compose rewrite",
            duration = 1800.0,
            startedAt = System.currentTimeMillis() - 45 * 60 * 1000,
            elapsed = 1800.0,
            status = FocusSession.Status.ENDED,
            detail = "Time's up. Lovely bit of focus."
        ).apply {
            checks.addAll(
                listOf(
                    FocusCheck(elapsed = 30.0, activity = FocusActivity.ON_TASK, probability = 0.05, confidence = 0.94),
                    FocusCheck(elapsed = 60.0, activity = FocusActivity.ON_TASK, probability = 0.08, confidence = 0.91),
                    FocusCheck(elapsed = 90.0, activity = FocusActivity.OFF_TASK, probability = 0.72, confidence = 0.88),
                    FocusCheck(elapsed = 120.0, activity = FocusActivity.ON_TASK, probability = 0.12, confidence = 0.92),
                    FocusCheck(elapsed = 150.0, activity = FocusActivity.ON_TASK, probability = 0.04, confidence = 0.96)
                )
            )
        }
        _focusHistory.value = listOf(pastSession)

        val voiceDemo = BlueySession(
            started = System.currentTimeMillis() - 40 * 60 * 1000,
            ended = System.currentTimeMillis() - 10 * 60 * 1000
        ).apply {
            entries.addAll(
                listOf(
                    TranscriptEntry(kind = TranscriptKind.TRANSCRIPT, text = "Starting our focus sprint today on the new Blueberry companion.", speaker = "A"),
                    TranscriptEntry(kind = TranscriptKind.ASKED, text = "Hey Bluey, keep an eye on my focus for 30 minutes!"),
                    TranscriptEntry(kind = TranscriptKind.REPLY, text = "Right you are! I'll keep my eyes peeled and give you a chirp if you wander off."),
                    TranscriptEntry(kind = TranscriptKind.REPORT, text = "Focus Guidelines\n\nDeep work session activated with 30-minute timer. Screen check cadence: every 30s. British speech bubbles and gentle nudges primed.")
                )
            )
        }
        _voiceSessions.value = listOf(voiceDemo)
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val session = _currentFocus.value
                if (session != null && session.status == FocusSession.Status.RUNNING) {
                    session.advance(1.0)
                    _currentFocus.value = session.copy()

                    // Periodic screen check
                    val interval = session.checkInterval
                    if (session.realActiveElapsed - lastCheckTimeSeconds >= interval) {
                        lastCheckTimeSeconds = session.realActiveElapsed
                        performPeriodicCheck(session)
                    }

                    if (session.status == FocusSession.Status.ENDED) {
                        _focusHistory.value = listOf(session.copy()) + _focusHistory.value
                        chirpSynthesizer.playHappyChirp()
                    }
                }
            }
        }
    }

    private fun performPeriodicCheck(session: FocusSession) {
        // High quality simulated classifier adhering to README:
        // On-task, off-task, or uncertain with probabilities
        val isDistracted = Random.nextFloat() < 0.28f
        val activity = if (isDistracted) FocusActivity.OFF_TASK else FocusActivity.ON_TASK
        val offProb = if (isDistracted) (0.55 + Random.nextDouble() * 0.4) else (0.02 + Random.nextDouble() * 0.2)
        val conf = 0.85 + Random.nextDouble() * 0.14

        val check = FocusCheck(
            elapsed = session.elapsed,
            activity = activity,
            probability = offProb,
            confidence = conf,
            probabilities = mapOf(
                FocusActivity.ON_TASK.rawValue to (1.0 - offProb - 0.05).coerceIn(0.0, 1.0),
                FocusActivity.OFF_TASK.rawValue to offProb,
                FocusActivity.UNCERTAIN.rawValue to 0.05
            ),
            sampleDuration = session.observationDuration
        )

        val triggerNudge = nudgePolicy.observe(check, ageSeconds = 1.0)
        val finalCheck = check.copy(nudged = triggerNudge)
        session.checks.add(finalCheck)
        _currentFocus.value = session.copy()

        if (triggerNudge) {
            val phrases = listOf(
                "Are you sure you're on task right now?",
                "Bit of a detour, isn't it? Let's get back to ${session.task.take(24)}.",
                "Eyes on the prize, mate! Lovely focus ahead.",
                "Let's wrap up this little distraction and finish strong."
            )
            val text = phrases.random()
            deliverNudge(text)
        }
    }

    fun deliverNudge(text: String) {
        _nudgeText.value = text
        chirpSynthesizer.playFocusChirp()
        animator.awake = true
        animator.localMood = Mood.POINTING
        viewModelScope.launch {
            delay(5000)
            if (_nudgeText.value == text) {
                _nudgeText.value = ""
                animator.localMood = null
            }
        }
    }

    fun setDeveloperMode(enabled: Boolean) {
        _developerMode.value = enabled
        nudgePolicy = FocusNudgePolicy(
            maximumGap = if (enabled) 60.0 else 45.0,
            developerMode = enabled
        )
        _currentFocus.value?.let { current ->
            current.setDeveloperModeEnabled(enabled)
            _currentFocus.value = current.copy()
        }
    }

    fun startFocus(task: String, minutes: Double) {
        val durationSeconds = (minutes * 60).coerceIn(60.0, 14400.0)
        val session = FocusSession(
            task = task,
            duration = durationSeconds,
            developerMode = _developerMode.value
        )
        lastCheckTimeSeconds = 0.0
        nudgePolicy.reset()
        _currentFocus.value = session
        chirpSynthesizer.playAloneChirp()
    }

    fun pauseFocus() {
        _currentFocus.value?.let { session ->
            session.pause("Paused by user")
            _currentFocus.value = session.copy()
        }
    }

    fun resumeFocus() {
        _currentFocus.value?.let { session ->
            session.resume()
            _currentFocus.value = session.copy()
        }
    }

    fun endFocus() {
        _currentFocus.value?.let { session ->
            session.end()
            _currentFocus.value = null
            _focusHistory.value = listOf(session.copy()) + _focusHistory.value
            chirpSynthesizer.playAloneChirp()
        }
    }

    fun testNudge() {
        deliverNudge("Are you sure you're on task right now?")
    }

    fun toggleListening() {
        if (_voiceState.value == VoiceState.ASLEEP) {
            wakeVoice()
        } else {
            sleepVoice()
        }
    }

    fun wakeVoice() {
        _voiceState.value = VoiceState.WAKING
        animator.awake = true
        animator.localMood = Mood.HAPPY
        chirpSynthesizer.playHappyChirp()

        // Start new voice session
        val session = BlueySession()
        _voiceSessions.value = listOf(session) + _voiceSessions.value
        _currentVoiceSessionId.value = session.id

        viewModelScope.launch {
            delay(800)
            _voiceState.value = VoiceState.LISTENING
            animator.localMood = null
        }
    }

    fun sleepVoice() {
        _voiceState.value = VoiceState.ASLEEP
        animator.awake = false
        animator.localMood = null
        _currentVoiceSessionId.value?.let { curId ->
            val updated = _voiceSessions.value.map {
                if (it.id == curId) it.copy(ended = System.currentTimeMillis()) else it
            }
            _voiceSessions.value = updated
            _currentVoiceSessionId.value = null
        }
    }

    fun beginAsk() {
        _voiceState.value = VoiceState.ASKING
        animator.awake = true
        animator.localMood = Mood.LISTENING
        chirpSynthesizer.playAloneChirp()
    }

    fun endAsk(customQuestion: String? = null) {
        val question = customQuestion ?: listOf(
            "How much focus time do we have left?",
            "Can you remind me of our main task?",
            "Is it time for a quick screen break?"
        ).random()

        _voiceState.value = VoiceState.THINKING
        animator.localMood = Mood.THINKING

        // Record question in current session
        val currentId = _currentVoiceSessionId.value ?: run {
            val newSession = BlueySession()
            _voiceSessions.value = listOf(newSession) + _voiceSessions.value
            _currentVoiceSessionId.value = newSession.id
            newSession.id
        }

        val askedEntry = TranscriptEntry(
            kind = TranscriptKind.ASKED,
            text = question
        )

        updateVoiceSession(currentId) { it.entries.add(askedEntry) }

        viewModelScope.launch {
            delay(1200)
            _voiceState.value = VoiceState.SPEAKING
            animator.localMood = Mood.TALKING
            chirpSynthesizer.playAloneChirp()

            val reply = when {
                question.contains("left", ignoreCase = true) -> {
                    val remainingStr = _currentFocus.value?.let { FocusSession.formatClock(it.remaining) } ?: "30 minutes"
                    "We've got $remainingStr left on the clock. Steady as she goes!"
                }
                question.contains("task", ignoreCase = true) -> {
                    val taskName = _currentFocus.value?.task ?: "your declared mission"
                    "Our mission is: $taskName. Smashing work so far!"
                }
                else -> {
                    "Right you are! I'm right here beside you watching the clock."
                }
            }

            val replyEntry = TranscriptEntry(
                kind = TranscriptKind.REPLY,
                text = reply
            )
            updateVoiceSession(currentId) { it.entries.add(replyEntry) }

            delay(2800)
            _voiceState.value = VoiceState.LISTENING
            animator.localMood = null
        }
    }

    private fun updateVoiceSession(id: String, block: (BlueySession) -> Unit) {
        val list = _voiceSessions.value.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index >= 0) {
            val session = list[index]
            block(session)
            list[index] = session.copy()
            _voiceSessions.value = list
        }
    }

    fun setVolume(vol: Float) {
        _volume.value = vol
        chirpSynthesizer.volume = vol
    }

    fun pairMac(name: String) {
        _connectedMacName.value = name
        _companionConnected.value = true
        chirpSynthesizer.playHappyChirp()
    }

    fun disconnectCompanion() {
        _connectedMacName.value = null
        _companionConnected.value = false
    }
}
