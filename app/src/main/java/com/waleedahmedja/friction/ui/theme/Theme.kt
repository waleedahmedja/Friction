package com.waleedahmedja.friction.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FrictionColors = darkColorScheme(
    primary      = AccentStart,
    background   = Black,
    surface      = DeepGray,
    onPrimary    = Black,
    onBackground = White,
    onSurface    = White,
    secondary    = AccentEnd,
    outline      = DividerGray
)

@Composable
fun FrictionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FrictionColors,
        content     = content
    )
}