# Privacy Policy

**Effective date:** March 2026
**Last updated:** March 2026

Friction is a commitment enforcement system designed for voluntary personal use. This policy explains how information is — and is not — handled.

---

## Short Version

Friction collects nothing. Stores nothing remotely. Sends nothing anywhere. Everything stays on your device.

---

## 1. Data Collection

Friction does not collect, store, transmit, or sell any personal data.

The app does not collect:
- Personal identifiers of any kind
- Contact information
- Location data
- Usage analytics or crash reports
- Advertising identifiers
- Biometric data
- Cloud backups
- Keystrokes or screen content

All focus session data (duration, blocked apps, settings) is stored locally on your device using Android DataStore. Nothing leaves your device.

---

## 2. Accessibility Service

Friction uses Android's Accessibility Service to detect which application is in the foreground during an active session. This is used solely to enforce the app block list.

Friction does **not**:
- Read screen content
- Record keystrokes
- Monitor text input or clipboard
- Track browsing behaviour
- Transmit accessibility data externally

`canRetrieveWindowContent` is explicitly set to `false` in the service configuration. Only package names are read, and only while a session is active.

---

## 3. Device Administrator (Hard Commitment Mode)

When Hard Commitment Mode is enabled by the user, Friction activates Android Device Admin permissions for the duration of the session.

This is used strictly to prevent uninstalling the app mid-session. It is not used for remote device management, surveillance, or any purpose beyond session enforcement.

Device Admin rights are released automatically when the session ends. The user can also revoke them at any time from device settings — doing so ends the session.

---

## 4. Biometric Authentication

If enabled, biometric verification is used to confirm intentional session termination.

Biometric data is processed entirely by Android's system-level secure APIs (BiometricPrompt). Friction does not access, store, or transmit any biometric information.

---

## 5. Local Storage

All app data is stored locally via Android DataStore. This includes:
- Session settings (duration, difficulty, grace period)
- Blocked and allowed app lists
- UI preferences

Uninstalling the app removes all of this data permanently.

---

## 6. Third-Party Services

Friction uses none. No analytics SDKs, no advertising SDKs, no tracking frameworks, no cloud databases. The app operates entirely offline.

---

## 7. Children's Privacy

Friction does not knowingly collect data from anyone, including children under 13. Since no personal data is collected from any user, no special processing applies.

---

## 8. Ethical Use

Friction is designed for voluntary self-discipline. It must not be used to:
- Monitor or surveil another person
- Control someone's device without their informed consent
- Restrict access to a device coercively

The app is intended solely for personal, voluntary use.

---

## 9. Changes

This policy may be updated to reflect new features or legal requirements. The "Last Updated" date above will reflect any changes. Significant changes will be noted in the release notes.

---

## 10. Contact

If you have questions about this policy, open a GitHub Issue at:
https://github.com/waleedahmedja/Score247

---

*Friction is built on transparency, discipline, and user control. Your data is yours.*
