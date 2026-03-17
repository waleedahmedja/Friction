package com.waleedahmedja.friction.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// FrictionColors — our own design token bag.
//
// @Stable tells the Compose compiler that none of its properties change their
// equals contract unexpectedly, so any composable that reads from this object
// can be skipped during recomposition when nothing inside has changed.
// ─────────────────────────────────────────────────────────────────────────────

@Stable
data class FrictionColors(
    val bg      : Color,
    val surface : Color,
    val surface2: Color,
    val divider : Color,
    val text    : Color,
    val textSub : Color,
    val textHint: Color,
    val btnText : Color,
    val accent  : Color,
    val danger  : Color,
    val isDark  : Boolean
)

// ── Singleton instances — created once at startup, never re-allocated ─────────
private val DarkColors = FrictionColors(
    bg       = Black,
    surface  = Surface11,
    surface2 = Surface22,
    divider  = Divider,
    text     = TextPrimary,
    textSub  = TextSecond,
    textHint = TextHint,
    btnText  = BtnText,
    accent   = AccentYellow,
    danger   = Danger,
    isDark   = true
)

private val LightColors = FrictionColors(
    bg       = LightBg,
    surface  = LightSurface,
    surface2 = LightSurf2,
    divider  = LightDivider,
    text     = LightText,
    textSub  = LightTextSub,
    textHint = LightTextHint,
    btnText  = LightBtnText,
    accent   = AccentYellow,
    danger   = Danger,
    isDark   = false
)

// ── CompositionLocal — access anywhere via FrictionTheme.c ───────────────────
// staticCompositionLocalOf is faster than compositionLocalOf for values that
// change rarely (only on system dark/light mode toggle).

private val LocalFrictionColors = staticCompositionLocalOf<FrictionColors> { DarkColors }

object FrictionTheme {
    val c: FrictionColors
        @Composable @ReadOnlyComposable
        get() = LocalFrictionColors.current
}

// ── Material3 color schemes — kept minimal, FrictionColors drives our UI ──────
private val DarkScheme = darkColorScheme(
    primary   = AccentYellow,
    background = Black,
    surface   = Surface11,
    onPrimary = BtnText,
    onSurface = TextPrimary,
    error     = Danger
)

private val LightScheme = lightColorScheme(
    primary   = AccentYellow,
    background = LightBg,
    surface   = LightSurface,
    onPrimary = LightBtnText,
    onSurface = LightText,
    error     = Danger
)

// ── Entry point — call this in MainActivity.setContent{} ─────────────────────
@Composable
fun FrictionAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content  : @Composable () -> Unit
) {
    val colors      = if (darkTheme) DarkColors else LightColors
    val colorScheme = if (darkTheme) DarkScheme  else LightScheme

    CompositionLocalProvider(LocalFrictionColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = FrictionTypography,
            content     = content
        )
    }
}