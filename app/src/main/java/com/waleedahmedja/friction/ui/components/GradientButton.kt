package com.waleedahmedja.friction.ui

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waleedahmedja.friction.ui.theme.AccentEnd
import com.waleedahmedja.friction.ui.theme.AccentStart
import com.waleedahmedja.friction.ui.theme.Black

val accentGradient = Brush.horizontalGradient(listOf(AccentStart, AccentEnd))

@Composable
fun GradientButton(
    text    : String,
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
    enabled : Boolean  = true
) {
    var pressed by remember { mutableStateOf(false) }
    val scale   by animateFloatAsState(
        targetValue   = if (pressed) 0.97f else 1f,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
        label         = "btn_scale"
    )

    val bgBrush = if (enabled) accentGradient
    else Brush.horizontalGradient(listOf(Color(0xFF2C2C2E), Color(0xFF2C2C2E)))

    Box(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(bgBrush)
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            pressed = true
                            tryAwaitRelease()
                            pressed = false
                        },
                        onTap = { onClick() }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = text,
            style = TextStyle(
                fontSize   = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color      = if (enabled) Black else Color(0xFF636366)
            )
        )
    }
}