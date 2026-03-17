package com.waleedahmedja.friction.ui

import androidx.compose.ui.graphics.Brush
import com.waleedahmedja.friction.ui.theme.AccentEnd
import com.waleedahmedja.friction.ui.theme.AccentStart

val accentGradient: Brush = Brush.horizontalGradient(
    colors = listOf(AccentStart, AccentEnd)
)