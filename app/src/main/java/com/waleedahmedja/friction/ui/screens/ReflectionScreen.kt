package com.waleedahmedja.friction.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.waleedahmedja.friction.ui.accentGradient
import com.waleedahmedja.friction.ui.theme.FrictionTheme
import com.waleedahmedja.friction.viewmodel.FrictionViewModel

@Composable
fun ReflectionScreen(
    vm       : FrictionViewModel,
    onContinue: () -> Unit
) {
    val c       = FrictionTheme.c
    val message by vm.reflectionMessage      .collectAsStateWithLifecycle()
    val showBtn by vm.showReflectionContinue .collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.loadReflectionMessage() }

    Box(
        modifier         = Modifier.fillMaxSize().background(c.bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Thin gold rule above
            Box(
                Modifier.width(28.dp).height(0.5.dp)
                    .background(c.text.copy(alpha = 0.15f))
            )

            Spacer(Modifier.height(32.dp))

            // Reflection message — no input field, just a quote
            AnimatedVisibility(
                visible = message.isNotBlank(),
                enter   = fadeIn() + slideInVertically { it / 4 }
            ) {
                Text(
                    text      = message,
                    style     = TextStyle(
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Light,
                        fontStyle  = FontStyle.Italic,
                        color      = c.text,
                        lineHeight = 30.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(48.dp))

            // Continue button — fades in after delay
            AnimatedVisibility(
                visible = showBtn,
                enter   = fadeIn()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(accentGradient)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                vm.clearReflectionContinue()
                                onContinue()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Continue",
                        style = TextStyle(
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = c.btnText
                        )
                    )
                }
            }
        }
    }
}
