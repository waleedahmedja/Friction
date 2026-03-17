package com.waleedahmedja.friction.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.waleedahmedja.friction.ui.components.GradientButton
import com.waleedahmedja.friction.ui.theme.FrictionTheme
import com.waleedahmedja.friction.viewmodel.FrictionViewModel
import kotlinx.coroutines.delay

@Composable
fun CompletionScreen(
    vm    : FrictionViewModel,
    onHome: () -> Unit
) {
    val c          = FrictionTheme.c
    val completion by vm.completion.collectAsStateWithLifecycle()

    // Staggered entrance — each element fades in after the previous
    var showTitle    by remember { mutableStateOf(false) }
    var showDuration by remember { mutableStateOf(false) }
    var showMessage  by remember { mutableStateOf(false) }
    var showButton   by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200L); showTitle    = true
        delay(600L); showDuration = true
        delay(500L); showMessage  = true
        delay(800L); showButton   = true
    }

    Box(
        modifier = Modifier.fillMaxSize().background(c.bg)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(0.4f))

            // "Focus Complete"
            AnimatedVisibility(
                visible = showTitle,
                enter   = fadeIn(tween(700)) + slideInVertically(tween(700)) { it / 3 }
            ) {
                Text(
                    "Focus Complete",
                    style     = TextStyle(
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = c.text
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(20.dp))

            // Duration label — e.g. "1 hr 30 min"
            AnimatedVisibility(
                visible = showDuration && completion.durationLabel.isNotBlank(),
                enter   = fadeIn(tween(600))
            ) {
                Text(
                    completion.durationLabel,
                    style     = TextStyle(
                        fontSize   = 17.sp,
                        fontWeight = FontWeight.Light,
                        color      = c.textSub
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(40.dp))

            // Completion message — the first line (e.g. "You stayed.")
            // The message from CompletionMessages.getForIndex() is formatted as:
            //   "You stayed.\n1 hr 30 min of uninterrupted focus."
            // We show the first line as the hero quote and the second as a subtitle.
            AnimatedVisibility(
                visible = showMessage && completion.message.isNotBlank(),
                enter   = fadeIn(tween(700))
            ) {
                val lines       = completion.message.lines()
                val quoteLine   = lines.getOrNull(0) ?: ""
                val subtitleLine = lines.getOrNull(1) ?: ""

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text      = quoteLine,
                        style     = TextStyle(
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Light,
                            color      = c.text,
                            lineHeight = 30.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    if (subtitleLine.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text      = subtitleLine,
                            style     = TextStyle(
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.Light,
                                color      = c.textSub
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            AnimatedVisibility(
                visible = showButton,
                enter   = fadeIn(tween(600))
            ) {
                GradientButton(
                    text    = "Done",
                    onClick = { vm.clearCompletion(); onHome() }
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}
