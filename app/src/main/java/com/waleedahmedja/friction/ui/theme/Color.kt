package com.waleedahmedja.friction.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Friction Design System — Color Tokens
//
// OLED-first. Every background is true black (#000000) so pixels are physically
// off — saves battery on AMOLED and maximises contrast.
//
// Single accent: Apple Yellow #FFD60A — the same yellow Apple uses in iOS Focus
// mode. Premium, legible on black, Gen Z familiar, not neon.
// ─────────────────────────────────────────────────────────────────────────────

// ── Primary accent ────────────────────────────────────────────────────────────
val AccentYellow = Color(0xFFFFD60A)   // iOS Focus yellow

// Legacy aliases — kept for files that still reference the old gradient pair
val AccentStart  = AccentYellow
val AccentEnd    = AccentYellow

// ── Dark mode surfaces ────────────────────────────────────────────────────────
val Black      = Color(0xFF000000)   // true OLED black — app background
val Surface11  = Color(0xFF111111)   // card background — barely lifted from black
val Surface1A  = Color(0xFF1A1A1A)   // elevated cards, flip-clock faces
val Surface22  = Color(0xFF222222)   // inputs, secondary buttons, chips
val Divider    = Color(0xFF2A2A2A)   // hairline dividers

// ── Dark mode text ────────────────────────────────────────────────────────────
val TextPrimary = Color(0xFFF5F5F5)   // main labels
val TextSecond  = Color(0xFF999999)   // supporting copy, subtitles
val TextHint    = Color(0xFF555555)   // placeholders, inactive states
val BtnText     = Color(0xFF000000)   // text ON the yellow button — black for contrast

// ── Semantic ──────────────────────────────────────────────────────────────────
val Danger  = Color(0xFFFF453A)   // Apple red  — destructive actions
val Success = Color(0xFF32D74B)   // Apple green — confirmations (used sparingly)

// ── Light mode surfaces ───────────────────────────────────────────────────────
// Light mode is secondary in this app but should look clean and iOS-native.
val LightBg       = Color(0xFFF2F2F7)   // iOS grouped table background
val LightSurface  = Color(0xFFFFFFFF)   // card white
val LightSurf2    = Color(0xFFE5E5EA)   // secondary surface
val LightDivider  = Color(0xFFD1D1D6)   // divider lines
val LightText     = Color(0xFF1C1C1E)   // primary text
val LightTextSub  = Color(0xFF6D6D72)   // secondary text
val LightTextHint = Color(0xFFAEAEB2)   // hints
val LightBtnText  = Color(0xFF000000)   // button text on yellow