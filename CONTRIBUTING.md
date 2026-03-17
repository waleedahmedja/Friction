# Contributing to Friction

Friction is a discipline enforcement system, not a productivity toy.

Before contributing, understand what this project is and what it refuses to be.

---

## Philosophy

Friction must remain:

- **Minimal** — every screen does one thing
- **Intentional** — no feature exists by accident
- **Serious** — no gamification, no streaks, no dopamine hooks
- **Performant** — smooth on mid-range devices, OLED-considerate

If your contribution adds complexity, it must add clarity by a greater margin.
If it adds a feature, that feature must serve the core premise: *making distraction require effort*.

**Non-negotiable removals:**
- No streak systems
- No push notifications encouraging the user to "come back"
- No analytics or usage tracking
- No cloud sync
- No social features

---

## Architecture Rules

Follow the established patterns. Do not introduce new ones without discussion.

- **MVVM strictly** — UI reads `StateFlow`, never mutates ViewModel state directly
- **DataStore only** — no SharedPreferences, no Room (unless session history is added with clear justification)
- **Coroutines + Flow** — no RxJava, no LiveData
- **Single ViewModel** — `FrictionViewModel` unless a screen has genuinely independent state
- **Kotlin only** — no Java files
- **Jetpack Compose only** — no XML layouts

New dependencies require justification in the PR description. If the same result can be achieved with what's already in the project, that approach wins.

---

## UI/UX Guidelines

The visual language is strict. Read it before touching any screen.

- **OLED-first dark mode** — backgrounds are `#000000`, not `#1A1A1A` or `#121212`
- **Single accent** — Apple Yellow `#FFD60A` only. No secondary accent colours.
- **No gradients on buttons** — solid fills, pill shape (`28.dp` radius), black text
- **Spacing** — generous. Minimum `16.dp` between elements. Breathe.
- **Typography** — system default. No custom font downloads.
- **Animations** — functional only. If removing the animation makes the UI clearer, remove it.
- **No shadows** — elevation is communicated through colour contrast, not drop shadows

If a design decision isn't covered here, the guiding question is: *does this look like it belongs in a premium system app, or does it look like it was built by an AI?*

---

## How to Contribute

1. **Fork** the repository
2. **Create a branch** — `feature/what-it-does` or `fix/what-it-fixes`
3. **Make your changes** — keep commits focused and clearly described
4. **Test on a real device** — the emulator is not sufficient for accessibility service and haptic behaviour
5. **Open a Pull Request** with:
   - A clear description of what changed and why
   - Screenshots or a screen recording if the change is visual
   - Confirmation that session persistence, tap challenge, and Hard Mode are unaffected

---

## Before Submitting

Verify:

- [ ] Sessions persist through process death (kill the app mid-session, reopen it)
- [ ] Tap Challenge fires correctly for a blocked app
- [ ] Hard Mode releases Device Admin when the session ends
- [ ] The UI runs without jank on a mid-range device (test on something other than a flagship)
- [ ] Dark mode looks correct
- [ ] No lint warnings introduced

---

## Bug Reports

Open an issue with:
- Device model and Android version
- Steps to reproduce
- Expected behaviour vs actual behaviour
- Logcat output if relevant (redact anything personal)

---

## Questions

Open a discussion, not an issue. Issues are for confirmed bugs and accepted feature work.

---

Friction respects discipline.
Contributions should too.

— **waleedahmedja**

