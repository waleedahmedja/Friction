package com.waleedahmedja.friction.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.waleedahmedja.friction.ui.components.GradientButton
import com.waleedahmedja.friction.ui.theme.FrictionTheme
import com.waleedahmedja.friction.viewmodel.FrictionViewModel

@Composable
fun ReflectionScreen(vm: FrictionViewModel, onContinue: () -> Unit) {
    val c            = FrictionTheme.c
    val message      by vm.reflectionMessage     .collectAsStateWithLifecycle()
    val showContinue by vm.showReflectionContinue.collectAsStateWithLifecycle()
    var note         by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadReflectionMessage() }

    Box(modifier = Modifier.fillMaxSize().background(c.bg).pointerInput(Unit) { detectTapGestures { } }) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Spacer(Modifier.weight(0.4f))
            Text("Session Ended", style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = c.text),
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            AnimatedVisibility(visible = message.isNotBlank(), enter = fadeIn(tween(800)), exit = fadeOut(tween(400))) {
                Text(message, style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Light,
                    color = c.textSub, lineHeight = 26.sp), textAlign = TextAlign.Center)
            }
            Spacer(Modifier.weight(1f))
            AnimatedVisibility(visible = showContinue,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 2 }) {
                GradientButton(text = "Continue", onClick = { vm.clearReflectionContinue(); onContinue() })
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}