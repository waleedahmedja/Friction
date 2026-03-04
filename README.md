
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-green)
![License](https://img.shields.io/badge/License-MIT-yellow)

# FRICTION

A commitment-enforcement focus system built with Jetpack Compose.
Designed to introduce intentional resistance between impulse and action.

---

## ✨ Philosophy

Most productivity apps try to motivate you.

Friction enforces discipline.

It does not reward.
It does not gamify.
It does not persuade.

It adds deliberate effort between you and distraction.

Quitting should require intention.

---

## 🧠 What Makes V2 Different

Friction evolves the original system into a serious commitment tool.

It introduces:

• Controlled app blocking
• Hard Commitment mode (session-based Device Admin)
• System-integrated light & dark theme
• OLED-safe standby design
• Polished Apple-level minimal UI

This is not a productivity tracker.
It is a discipline mechanism.

---

## 📱 Core Features

* Infinite smooth focus duration wheel
* Persistent focus sessions (DataStore-backed)
* App blocking via Accessibility Service
* TapChallenge enforcement before early exit
* Reflection screen when breaking a session
* Completion screen on natural finish
* Normal & Hard Commitment modes
* Device Admin activated only during session (Play Store safe)
* Pixel burn prevention (micro UI drift)
* Fully built with Jetpack Compose

---

## 🔒 How It Works

1. Set your focus duration.
2. Start a session.
3. Blocked apps trigger TapChallenge.
4. Pass → session ends intentionally.
5. Fail → returned to lock screen.
6. Timer completes → session ends cleanly.

Friction ensures quitting is harder than continuing.

---

## 🎨 Design Principles

* Minimal
* Spacious
* Serious
* System-native
* No visual noise
* No dopamine tricks

Light and Dark mode automatically follow device settings.

The interface is inspired by the restraint and clarity of modern system apps.

---

## ⚙ Modes

### Normal Mode

Accessibility-based enforcement.

### Hard Commitment Mode

* Activates Device Admin during session
* Prevents uninstall during session
* Prevents disabling enforcement mid-session
* Automatically releases when session completes

Outside a session → full user control restored.

---

## 🛠 Built With

* Kotlin
* Jetpack Compose
* Material 3 (custom restrained styling)
* StateFlow
* Android DataStore
* Accessibility Service
* Device Admin API
* MVVM Architecture

---

## 🏗 Architecture

Friction follows clean architecture principles:

* UI Layer — Jetpack Compose
* Domain Logic — Session Engine
* State Management — StateFlow
* Data Layer — DataStore
* Enforcement Layer — Accessibility + Device Admin
* Navigation — Compose Navigation

Sessions persist across app restarts.
Enforcement resumes automatically if interrupted.

---

## 🚀 Installation

Check Releases:

[https://github.com/waleedahmedja/Friction/releases/tag/v2.0.0](https://github.com/waleedahmedja/Friction/releases/tag/v2.0.0)

Or clone the repository and build locally.

---

## 📄 License

This project is licensed under the MIT License.
See the LICENSE file for details.

---

## 🧭 Roadmap

* Biometric exit verification
* Adaptive TapChallenge difficulty
* Enhanced reflection insights
* Performance refinements
* Play Store deployment hardening

---

> Friction is calm.
> Friction is firm.
> Friction respects your decision to focus.

— waleedahmedja



