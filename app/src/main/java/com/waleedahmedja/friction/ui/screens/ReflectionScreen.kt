package com.waleedahmedja.friction.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.waleedahmedja.friction.viewmodel.FrictionViewModel
import com.waleedahmedja.friction.ui.theme.*

@Composable
fun ReflectionScreen(vm: FrictionViewModel, onContinue: () -> Unit) {

    val message    by vm.reflectionMessage.collectAsStateWithLifecycle()
    val showPrompt by vm.showReflectionContinue.collectAsStateWithLifecycle()
    var gone       by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadReflectionMessage() }

    val navigate = {
        if (!gone && showPrompt) {
            gone = true
            vm.clearReflectionContinue()
            onContinue()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .pointerInput(showPrompt, gone) {
                if (showPrompt && !gone) detectTapGestures { navigate() }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = message.isNotBlank(),
                enter   = fadeIn(tween(600))
            ) {
                Text(
                    text      = message,
                    style     = TextStyle(
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Light,
                        color      = White,
                        lineHeight = 36.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(52.dp))

            AnimatedVisibility(
                visible = showPrompt,
                enter   = fadeIn(tween(400))
            ) {
                Text(
                    text  = "Tap anywhere to continue",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color    = White.copy(alpha = 0.25f)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}