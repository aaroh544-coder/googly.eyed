# Bluey · Blueberry Focus Friend (Android)

Bluey is an interactive blueberry focus companion rewritten in Kotlin and Jetpack Compose. Place your Android phone under your monitor: his googly eyes follow your touch, and he keeps you on task with periodic focus evaluations, speech bubbles, and friendly cartoon chirps.

## Features

- **Animated Blueberry Face**:
  - Organic cubic Bézier body silhouette with 150° blueberry gradient, soft radial highlight shine, and glossy sparkle.
  - Interactive googly eyes with damped spring physics responding to touch, saccades, and mood.
  - Expressive eyebrows, eyelid blinks (including double-blinks and sleepy droop), blushy cheeks, and mouth/tongue reactions.
  - Procedurally synthesized cartoon audio chirps via Android `AudioTrack`.

- **Focus Sessions & Timer Engine**:
  - Declarative task tracking with customizable durations (1–240 minutes).
  - Prominent countdown display with pause/resume and session management.
  - **Developer Mode**: 4× clock speed with 10-second check intervals and fast nudge testing.
  - Visual timeline bar displaying on-task (green), off-task (peach), and unchecked/unknown time (grey).
  - Inspection dialog for periodic observations, category probabilities, and decision confidence estimates.

- **Companion App & Voice History**:
  - Companion sidebar with Mini Bluey avatar and volume controls.
  - Voice session history with speaker diarization, questions asked, replies, research reports, and Codex notes.
  - **Copy Prompt for Agent**: Generates structured markdown prompt with session details ready to paste to LLMs/AI.
  - Pair Mac dialog with simulated QR scanning and local network discovery.

## Tech Stack

- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose with Material Design 3
- **Build System**: Android Gradle Plugin 9.1.1, Gradle 9.3.1 (Kotlin DSL)
- **Architecture**: MVVM with Kotlin Coroutines & StateFlow
- **Typography**: Bundled Fredoka, IBM Plex Sans, and IBM Plex Mono fonts
