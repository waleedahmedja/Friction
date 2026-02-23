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
import kotlinx.coroutines.delay

@Composable
fun CompletionScreen(vm: FrictionViewModel, onHome: () -> Unit) {

    val completion  by vm.completion.collectAsStateWithLifecycle()
    var showMessage by remember { mutableStateOf(false) }
    var gone        by remember { mutableStateOf(false) }

    val navigate = {
        if (!gone) {
            gone = true
            vm.clearCompletion()
            onHome()
        }
    }

    LaunchedEffect(completion.visible) {
        if (completion.visible) {
            delay(400L)
            showMessage = true
            delay(2_500L)
            navigate()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .pointerInput(showMessage) {
                if (showMessage) detectTapGestures { navigate() }
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = showMessage,
            enter   = fadeIn(tween(700))
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 44.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text      = completion.message,
                    style     = TextStyle(
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Light,
                        color      = White,
                        lineHeight = 36.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(48.dp))

                Text(
                    text  = "Tap to continue",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color    = White.copy(alpha = 0.25f)
                    )
                )
            }
        }
    }
}
