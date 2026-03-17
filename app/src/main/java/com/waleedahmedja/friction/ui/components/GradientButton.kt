package com.waleedahmedja.friction.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waleedahmedja.friction.ui.accentGradient
import com.waleedahmedja.friction.ui.theme.FrictionTheme

@Composable
fun GradientButton(
    text    : String,
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
    enabled : Boolean  = true
) {
    val c = FrictionTheme.c

    var pressed by remember { mutableStateOf(false) }
    val scale   by animateFloatAsState(
        targetValue   = if (pressed) 0.97f else 1f,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
        label         = "btnScale"
    )

    val bg: Brush = if (enabled) accentGradient
                    else Brush.horizontalGradient(listOf(c.surface2, c.surface2))

    Box(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(bg)
            .pointerInput(enabled) {
                if (enabled) detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap   = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = text,
            style = TextStyle(
                fontSize   = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color      = if (enabled) c.btnText else c.textHint
            )
        )
    }
}
