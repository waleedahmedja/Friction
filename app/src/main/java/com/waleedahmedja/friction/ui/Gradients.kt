package com.waleedahmedja.friction.ui

import androidx.compose.ui.graphics.Brush
import com.waleedahmedja.friction.ui.theme.AccentYellow

// ── Pre-allocated brushes (never create Brush inside a composable body) ────────
// Yellow pill button — solid, no gradient, maximum contrast
val accentGradient: Brush = Brush.horizontalGradient(
    colors = listOf(AccentYellow, AccentYellow)
)

// Slightly warmer left→right for focus button hero
val accentGradientWarm: Brush = Brush.horizontalGradient(
    colors = listOf(AccentYellow, AccentYellow.copy(alpha = 0.85f))
)
