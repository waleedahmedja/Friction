package com.waleedahmedja.friction.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.waleedahmedja.friction.viewmodel.FrictionViewModel
import com.waleedahmedja.friction.ui.accentGradient
import com.waleedahmedja.friction.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TapChallengeScreen(
    vm    : FrictionViewModel,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val tap    by vm.tap.collectAsStateWithLifecycle()
    val haptic  = LocalHapticFeedback.current
    val scope   = rememberCoroutineScope()
    var navigated  by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    var pulseTarget by remember { mutableFloatStateOf(1f) }
    val pulseScale  by animateFloatAsState(
        targetValue   = pulseTarget,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "pulse"
    )

    LaunchedEffect(Unit) { vm.initTap() }

    LaunchedEffect(tap.done) {
        if (tap.done && !navigated) {
            navigated = true
            delay(600L)
            onDone()
        }
    }

    val progress = if (tap.required > 0) tap.current.toFloat() / tap.required else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .pointerInput(tap.done, showDialog) {
                if (!tap.done && !showDialog) {
                    detectTapGestures {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        vm.registerTap()
                        scope.launch {
                            pulseTarget = 1.05f
                            delay(70)
                            pulseTarget = 1f
                        }
                    }
                }
            }
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // Top bar
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .align(Alignment.CenterStart)
                        .pointerInput(Unit) { detectTapGestures { showDialog = true } },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "←",
                        style = TextStyle(
                            fontSize = 22.sp,
                            color    = White.copy(alpha = 0.70f)
                        )
                    )
                }
                Text(
                    text     = "TAP CHALLENGE",
                    style    = TextStyle(
                        fontSize      = 11.sp,
                        fontWeight    = FontWeight.Medium,
                        color         = White.copy(alpha = 0.40f),
                        letterSpacing = 2.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.weight(0.45f))

            // Large tap count
            Text(
                text      = tap.current.toString(),
                style     = TextStyle(
                    fontSize   = 100.sp,
                    fontWeight = FontWeight.Bold,
                    color      = White
                ),
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .fillMaxWidth()
                    .scale(pulseScale)
            )

            Spacer(Modifier.height(12.dp))

            // "of N taps"
            Text(
                text  = "of ${tap.required} taps",
                style = TextStyle(
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Light,
                    color      = White.copy(alpha = 0.60f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(36.dp))

            // Thin progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(White.copy(alpha = 0.10f))
            ) {
                val animProg by animateFloatAsState(
                    targetValue   = progress,
                    animationSpec = tween(150),
                    label         = "prog"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animProg)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(1.dp))
                        .background(accentGradient)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text  = if (tap.done) "Done." else "Keep tapping",
                style = TextStyle(
                    fontSize = 14.sp,
                    color    = White.copy(alpha = 0.35f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(1f))
        }

        if (showDialog) {
            TapExitDialog(
                onReturnToLock = { showDialog = false; onBack() },
                onKeepTapping  = { showDialog = false }
            )
        }
    }
}

@Composable
private fun TapExitDialog(
    onReturnToLock: () -> Unit,
    onKeepTapping : () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black.copy(alpha = 0.92f))
            .pointerInput(Unit) { detectTapGestures { /* consume */ } }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xFF1C1C1E))
                .padding(horizontal = 24.dp, vertical = 36.dp)
                .pointerInput(Unit) { detectTapGestures { /* consume */ } },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text  = "Leave tap challenge?",
                style = TextStyle(
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = White
                )
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text      = "Returning to the lock will not reset your progress.",
                style     = TextStyle(
                    fontSize = 14.sp,
                    color    = White.copy(alpha = 0.50f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Primary: Return to Lock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(accentGradient)
                    .pointerInput(Unit) { detectTapGestures { onReturnToLock() } },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Return to Lock",
                    style = TextStyle(
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Black
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            // Secondary: Keep Tapping
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF2C2C2E))
                    .pointerInput(Unit) { detectTapGestures { onKeepTapping() } },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Keep Tapping",
                    style = TextStyle(
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color      = White.copy(alpha = 0.70f)
                    )
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}