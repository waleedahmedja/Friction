<div align="center">

# F R I C T I O N

**A commitment-enforcement focus system for Android.**
Built with Jetpack Compose. Designed to make distraction require effort.

<br/>

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-2024.12-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
![API](https://img.shields.io/badge/Min_SDK-24-green?style=flat-square)
![License](https://img.shields.io/badge/License-Friction_v1.0-FFD60A?style=flat-square)
![Status](https://img.shields.io/badge/Status-V2_Active-brightgreen?style=flat-square)

<br/>

</div>

---

## The Idea

Most productivity apps try to motivate you.

Friction doesn't.

It introduces deliberate resistance between impulse and action. When you want to quit — when you reach for the phone, when the notification pulls you away — Friction makes you work for it.

Not impossible. Not punishing. Just enough friction to make you ask:

> *Do I actually want to break this?*

Nine times out of ten, you don't.

---

## What V2 Introduces

| Feature | Description |
|---|---|
| **App Blocking** | Blocks selected apps via Accessibility Service during active sessions |
| **Hard Commitment Mode** | Device Admin-backed sessions — prevents uninstall while locked |
| **Tap Challenge** | Friction gate before early exit — scales with session length |
| **Standby Screen** | OLED-first clock display with arc progress and Tsuki the bunny |
| **Reflection Screen** | Quiet moment of accountability after breaking a session |
| **Biometric Guard** | Optional fingerprint/face verification before early exit |
| **Session Persistence** | DataStore-backed — sessions survive process death and reboots |
| **OLED Burn-in Protection** | Micro pixel drift on the standby screen |

---

## How It Works

```
Set duration → Lock session → Session active
                                    │
                          Try to open blocked app?
                                    │
                          ┌─────────┴──────────┐
                          │ Tap Challenge gate │
                          └─────────┬──────────┘
                                    │
                     Complete it → Session ends intentionally
                     Fail / back  → Return to lock screen
                                    │
                          Timer expires naturally?
                                    │
                          Completion screen → Done
```

---

## Architecture

Friction follows clean MVVM with a strict layer separation.

```
app/
├── ui/
│   ├── screens/          # FocusScreen, StandbyScreen, TapChallengeScreen...
│   ├── components/       # InfiniteWheel, GradientButton
│   └── theme/            # Color, Type, Theme (FrictionColors token system)
├── viewmodel/
│   └── FrictionViewModel # Single ViewModel, StateFlow-driven
├── data/
│   ├── DataStoreManager  # All persisted settings + session state
│   └── BlockedAppsRepository
├── service/
│   ├── FrictionAccessibilityService   # Foreground app monitor
│   └── TapChallengeOverlayActivity    # Full-screen block overlay
├── admin/
│   └── FrictionDeviceAdminReceiver    # Hard Mode enforcement
└── receiver/
    └── BootReceiver                   # Session restore on reboot
```

**Key principles:**
- UI reads from `StateFlow` — never mutates ViewModel state directly
- All persistence is async via DataStore — no blocking main thread calls
- Accessibility Service uses a `SupervisorJob` scope — cancelled cleanly in `onDestroy`
- Session state survives process death via `init {}` DataStore restore

---

## Modes

### Normal Mode
Accessibility Service monitors foreground apps. Blocked apps trigger the Tap Challenge overlay immediately.

### Hard Commitment Mode
Activates Android Device Admin for the session duration.
- Prevents uninstalling Friction while locked
- Prevents disabling enforcement mid-session
- Admin rights released **automatically** when the session ends
- Outside a session: full user control

> Hard Mode is Play Store safe — Device Admin is only activated by explicit user action, held only for the session, and released automatically.

---

## Design

The UI is built around one idea: **restraint as craft**.

- Pure OLED black backgrounds (`#000000`) — every pixel off is battery saved
- Single accent colour: Apple Yellow `#FFD60A` — the same yellow iOS uses for Focus mode
- No gradients on interactive elements — solid fills only
- Typography: system monospace for the clock, system default elsewhere
- Animations: functional only — flip clock, arc progress, breathing dot
- Standby screen features Tsuki — a hand-drawn Canvas bunny who blinks, stretches, and dozes

---

## Built With

- **Kotlin 2.0.21** + Coroutines + Flow
- **Jetpack Compose** (BOM 2024.12.01) + Material3
- **Android DataStore** — async, coroutine-safe preferences
- **Accessibility Service** — foreground app detection
- **Device Admin API** — Hard Mode enforcement
- **BiometricPrompt** — optional exit gate
- **Compose Navigation** — single-activity, composable screens
- **MVVM** — `AndroidViewModel` + `StateFlow`

---

## Installation

**From Releases:**

→ [github.com/waleedahmedja/Friction/releases/tag/v2.0.0](https://github.com/waleedahmedja/Friction/releases/tag/v2.0.0)

Download the `.apk`, enable "Install from unknown sources" in your device settings, install.

**Build from source:**

```bash
git clone https://github.com/waleedahmedja/Friction.git
cd Friction
./gradlew assembleDebug
```

Requires Android Studio Ladybug or later, JDK 11+.

---

## Permissions

| Permission | Why |
|---|---|
| `BIND_ACCESSIBILITY_SERVICE` | Detects which app is in the foreground |
| `BIND_DEVICE_ADMIN` | Hard Commitment Mode enforcement |
| `RECEIVE_BOOT_COMPLETED` | Restores active sessions after reboot |
| `USE_BIOMETRIC` | Optional biometric exit gate |
| `FOREGROUND_SERVICE` | Future planned foreground enforcement |

Friction reads **only package names**. `canRetrieveWindowContent` is explicitly `false`. No screen content, no keystrokes, no data leaves the device.

---

## Roadmap

- [ ] Play Store deployment hardening
- [ ] Enhanced reflection insights
- [ ] Schedule-based auto-lock
- [ ] Widget for session status
- [ ] Per-app block scheduling

---

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a PR.

Friction has a strong design philosophy. Contributions that add complexity without adding clarity won't be merged. The bar is high intentionally.

---

## License

[Friction License v1.0](LICENSE) — free to use, study, modify, distribute. Attribution required. Public changes stay public.

---

<div align="center">

*Friction is calm. Friction is firm. Friction respects your decision to focus.*

**— waleedahmedja**

</div>
