package com.waleedahmedja.friction.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

// ── Stable colour token bag ────────────────────────────────────────────────────
// @Stable lets Compose skip recomposition if nothing changed.
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

// ── Singleton token sets ───────────────────────────────────────────────────────
private val Dark = FrictionColors(
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

private val Light = FrictionColors(
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

// ── CompositionLocal ───────────────────────────────────────────────────────────
private val LocalFrictionColors = staticCompositionLocalOf { Dark }

object FrictionTheme {
    val c: FrictionColors
        @Composable @ReadOnlyComposable
        get() = LocalFrictionColors.current
}

// ── Material color schemes (minimal — we use FrictionColors for custom UI) ────
private val DarkScheme = darkColorScheme(
    primary   = AccentYellow,
    background= Black,
    surface   = Surface11,
    onPrimary = BtnText,
    onSurface = TextPrimary,
    error     = Danger
)

private val LightScheme = lightColorScheme(
    primary   = AccentYellow,
    background= LightBg,
    surface   = LightSurface,
    onPrimary = LightBtnText,
    onSurface = LightText,
    error     = Danger
)

// ── Theme entry point ─────────────────────────────────────────────────────────
@Composable
fun FrictionAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content  : @Composable () -> Unit
) {
    val colors      = if (darkTheme) Dark else Light
    val colorScheme = if (darkTheme) DarkScheme else LightScheme

    CompositionLocalProvider(LocalFrictionColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = FrictionTypography,
            content     = content
        )
    }
}
