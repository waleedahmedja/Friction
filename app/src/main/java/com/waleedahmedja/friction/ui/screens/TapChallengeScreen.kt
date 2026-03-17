package com.waleedahmedja.friction.ui.screens

// Navigation is driven by FrictionApp's postTapDestination observer.
// This screen does NOT observe postTapDestination itself — that would create
// a race condition with the ViewModel's routing logic.

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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.waleedahmedja.friction.ui.accentGradient
import com.waleedahmedja.friction.ui.theme.FrictionColors
import com.waleedahmedja.friction.ui.theme.FrictionTheme
import com.waleedahmedja.friction.viewmodel.FrictionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TapChallengeScreen(
    vm          : FrictionViewModel,
    onReflection: () -> Unit,
    onHome      : () -> Unit,
    onBack      : () -> Unit
) {
    val c             = FrictionTheme.c
    val tap           by vm.tap          .collectAsStateWithLifecycle()
    val hapticEnabled by vm.hapticEnabled.collectAsStateWithLifecycle()
    val haptic        = LocalHapticFeedback.current
    val scope         = rememberCoroutineScope()

    var navigated   by remember { mutableStateOf(false) }
    var showExitDlg by remember { mutableStateOf(false) }

    var pulseTarget by remember { mutableFloatStateOf(1f) }
    val pulseScale  by animateFloatAsState(
        targetValue   = pulseTarget,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "pulse"
    )

    // Only initialise the tap challenge if it hasn't been started yet (current == 0 and not done).
    // This means if the user leaves and returns mid-challenge, their progress is preserved —
    // which is what the exit sheet promises ("returning won't reset your progress").
    LaunchedEffect(Unit) {
        if (tap.current == 0 && !tap.done) {
            vm.initTap()
        }
    }

    // Once done, wait briefly then navigate
    LaunchedEffect(tap.done) {
        if (tap.done && !navigated) {
            navigated = true
            delay(400L)
            if (vm.reflectionOnExit.value) onReflection() else onHome()
        }
    }

    val progress = if (tap.required > 0) tap.current.toFloat() / tap.required else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .pointerInput(tap.done, showExitDlg) {
                if (!tap.done && !showExitDlg) {
                    detectTapGestures {
                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.registerTap()
                        scope.launch {
                            pulseTarget = 1.06f
                            delay(60)
                            pulseTarget = 1f
                        }
                    }
                }
            }
    ) {
        Column(
            modifier            = Modifier.fillMaxSize().padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // Top bar
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(44.dp).align(Alignment.CenterStart)
                        .pointerInput(Unit) { detectTapGestures { showExitDlg = true } },
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", style = TextStyle(fontSize = 22.sp, color = c.textHint))
                }
                Text(
                    "TAP CHALLENGE",
                    style    = TextStyle(
                        fontSize      = 11.sp,
                        fontWeight    = FontWeight.Medium,
                        color         = c.textHint,
                        letterSpacing = 2.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.weight(0.45f))

            // Big tap counter
            Text(
                text      = tap.current.toString(),
                style     = TextStyle(
                    fontSize   = 100.sp,
                    fontWeight = FontWeight.Bold,
                    color      = c.text
                ),
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth().scale(pulseScale)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "of ${tap.required} taps",
                style     = TextStyle(
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Light,
                    color      = c.textSub
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(36.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f).height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(c.text.copy(alpha = 0.10f))
            ) {
                val animProg by animateFloatAsState(
                    targetValue   = progress,
                    animationSpec = tween(120),
                    label         = "tapProgress"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animProg).fillMaxHeight()
                        .clip(RoundedCornerShape(1.dp))
                        .background(accentGradient)
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text      = if (tap.done) "Done." else "Keep tapping",
                style     = TextStyle(fontSize = 13.sp, color = c.textHint),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(1f))
        }

        if (showExitDlg) {
            TapExitSheet(
                c              = c,
                onReturnToLock = { showExitDlg = false; onBack() },
                onKeepTapping  = { showExitDlg = false }
            )
        }
    }
}

@Composable
private fun TapExitSheet(
    c             : FrictionColors,
    onReturnToLock: () -> Unit,
    onKeepTapping : () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg.copy(alpha = 0.94f))
            .pointerInput(Unit) { detectTapGestures { } }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(c.surface)
                .padding(horizontal = 24.dp, vertical = 36.dp)
                .pointerInput(Unit) { detectTapGestures { } },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Leave tap challenge?",
                style = TextStyle(
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = c.text
                )
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Your progress is saved. You can return and continue from where you left off.",
                style     = TextStyle(fontSize = 14.sp, color = c.textSub),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth().height(54.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(accentGradient)
                    .pointerInput(Unit) { detectTapGestures { onReturnToLock() } },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Return to Lock Screen",
                    style = TextStyle(
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = c.btnText
                    )
                )
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth().height(54.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(c.surface2)
                    .pointerInput(Unit) { detectTapGestures { onKeepTapping() } },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Keep Tapping",
                    style = TextStyle(fontSize = 16.sp, color = c.textSub)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
