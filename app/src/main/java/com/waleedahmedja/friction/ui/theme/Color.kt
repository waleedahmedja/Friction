package com.waleedahmedja.friction.ui.theme

import androidx.compose.ui.graphics.Color

// ── Primary accent ────────────────────────────────────────────────────────────
// Apple Yellow — iOS Focus/Shortcuts yellow. Vibrant, Gen Z friendly, timeless.
val AccentYellow   = Color(0xFFFFD60A)
val AccentYellowDim = Color(0xFFFFD60A).copy(alpha = 0.15f)

// ── Legacy aliases (so existing code compiles without change) ─────────────────
val AccentStart = AccentYellow
val AccentEnd   = AccentYellow

// ── Surface palette — pure OLED dark ─────────────────────────────────────────
// #000 is pure OLED. Cards at #111 give depth without grey.
val Black       = Color(0xFF000000)
val Surface11   = Color(0xFF111111)   // cards
val Surface1A   = Color(0xFF1A1A1A)   // elevated cards
val Surface22   = Color(0xFF222222)   // inputs, secondary surface
val Divider     = Color(0xFF2A2A2A)

// ── Text ──────────────────────────────────────────────────────────────────────
val TextPrimary  = Color(0xFFF5F5F5)
val TextSecond   = Color(0xFF999999)
val TextHint     = Color(0xFF555555)

// ── Semantic ─────────────────────────────────────────────────────────────────
val Danger  = Color(0xFFFF453A)   // Apple red
val Success = Color(0xFF32D74B)   // Apple green
val BtnText = Color(0xFF000000)   // black text on yellow button

// ── Light mode (minimal — app is OLED-first) ─────────────────────────────────
val LightBg      = Color(0xFFF2F2F7)
val LightSurface = Color(0xFFFFFFFF)
val LightSurf2   = Color(0xFFE5E5EA)
val LightDivider = Color(0xFFD1D1D6)
val LightText    = Color(0xFF1C1C1E)
val LightTextSub = Color(0xFF6D6D72)
val LightTextHint= Color(0xFFAEAEB2)
val LightBtnText = Color(0xFF000000)
