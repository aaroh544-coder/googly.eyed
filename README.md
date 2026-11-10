# Bluey · Blueberry Focus Friend (Android + Windows)

Bluey is an interactive blueberry focus companion written in Kotlin and Compose Multiplatform. Place the app under your monitor: his googly eyes follow your cursor or finger, and he keeps you on task with periodic focus evaluations, speech bubbles, and friendly cartoon chirps.

The UI, animations, timer engine, and session history are shared between an **Android** app and a **Windows** desktop app.

## Features

- **Animated Blueberry Face**:
  - Organic cubic Bézier body silhouette with 150° blueberry gradient, soft radial highlight shine, and glossy sparkle.
  - Interactive googly eyes with damped spring physics responding to touch, saccades, and mood.
  - Expressive eyebrows, eyelid blinks (including double-blinks and sleepy droop), blushy cheeks, and mouth/tongue reactions.
  - Procedurally synthesized cartoon audio chirps (Android `AudioTrack`, Java Sound on Windows).

- **Focus Sessions & Timer Engine**:
  - Declarative task tracking with customizable durations (1–240 minutes).
  - Prominent countdown display with pause/resume and session management.
  - **Developer Mode**: 4× clock speed with 10-second check intervals and fast nudge testing.
  - Visual timeline bar displaying on-task (green), off-task (peach), and unchecked/unknown time (grey).
  - Inspection dialog for periodic observations, category probabilities, and decision confidence estimates.

- **Companion App & Voice History**:
  - Companion sidebar with Mini Bluey avatar and volume controls.
  - Voice session history with speaker diarization, questions asked, replies, research reports, and Codex notes.
  - **Copy Prompt for Agent**: Generates structured markdown prompt with session details ready to paste to LLMs/AI (uses the platform clipboard).
  - Pair computer dialog with simulated QR scanning and local network discovery.

## Supported platforms

| Platform | Target | Output |
| --- | --- | --- |
| Android 8.0+ (API 26) | `androidTarget` | `app/build/outputs/apk/debug/app-debug.apk` |
| Windows 10/11 (64-bit) | `jvm("desktop")` | desktop app image / MSI installer |

## Prebuilt releases

Pushing a `v*` tag (or running the **Release** workflow manually from the Actions tab) builds both
apps on GitHub runners and attaches them to a GitHub release:

| Asset | Contents |
| --- | --- |
| `bluey-debug.apk` | debug-signed Android app (API 26+) |
| `Bluey-windows-x64.zip` | `Bluey/Bluey.exe` with a bundled 64-bit JRE, built by `jpackage` on Windows — unzip and run |

The workflow lives in `.github/workflows/release.yml`.

## Tech Stack

- **Language**: Kotlin 2.2.10 (Kotlin Multiplatform)
- **UI Framework**: Compose Multiplatform 1.9.3 with Material Design 3
- **Build System**: Android Gradle Plugin 9.1.1, Gradle 9.3.1 (Kotlin DSL)
- **Architecture**: MVVM with Kotlin Coroutines & StateFlow (multiplatform lifecycle ViewModel)
- **Typography**: Bundled Fredoka, IBM Plex Sans, and IBM Plex Mono fonts (shared Compose resources)

## Requirements

- JDK 21
- Android SDK with platform 36 and build-tools 36 (for the Android app)
- Windows 10/11 and the [WiX Toolset v3](https://wixtoolset.org/) only if you want to build the `.msi` installer

Set the Android SDK location in `local.properties`:

```properties
sdk.dir=C\:\\Users\\you\\AppData\\Local\\Android\\Sdk
```

or point the `ANDROID_HOME` environment variable at it.

## Build and run

### Windows (desktop)

Run the app straight from the sources:

```bat
gradlew.bat :app:run
```

Build a self-contained application image (a runnable folder with a bundled JRE), then start it:

```bat
gradlew.bat :app:createDistributable
app\build\compose\binaries\main\app\Bluey\Bluey.exe
```

Build the Windows `.msi` installer (requires WiX):

```bat
gradlew.bat :app:packageMsi
```

The installer is written to `app\build\compose\binaries\main\msi\`.

### Android

```bat
gradlew.bat :app:assembleDebug
```

The APK lands in `app\build\outputs\apk\debug\`. Install it with `adb install app\build\outputs\apk\debug\app-debug.apk`.

A release build (`gradlew.bat :app:assembleRelease`) is unsigned unless a `debug.keystore` is present in the repository root, in which case that keystore is used for signing.

On macOS or Linux, use `./gradlew` with the same task names. The desktop app then runs with the host platform's launcher.

## Project layout

```
app/src/
  commonMain/kotlin/      shared UI, face animation, focus engine, view model
  commonMain/composeResources/font/   Fredoka and IBM Plex fonts
  androidMain/kotlin/     MainActivity, AudioTrack playback, Android date/clipboard plumbing
  androidMain/res/        launcher icons, strings, manifest resources
  androidMain/AndroidManifest.xml
  desktopMain/kotlin/     Windows entry point (Main.kt), Java Sound playback
```

Platform-specific behaviour (clock, date formatting, PCM audio output, system back gesture) is declared with `expect`/`actual` in `app/src/*Main/kotlin/com/example/bluey/platform` and `.../audio`.
