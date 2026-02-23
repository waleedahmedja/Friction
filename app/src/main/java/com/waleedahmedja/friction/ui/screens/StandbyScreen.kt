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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waleedahmedja.friction.viewmodel.LockState
import com.waleedahmedja.friction.ui.accentGradient
import com.waleedahmedja.friction.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun StandbyScreen(
    lock        : LockState,
    hh          : Int,
    mm          : Int,
    ss          : Int,
    onReturn    : () -> Unit,
    onEndSession: () -> Unit
) {
    var overlayVisible by remember { mutableStateOf(false) }

    LaunchedEffect(overlayVisible) {
        if (overlayVisible) {
            delay(3_000L)
            overlayVisible = false
        }
    }

    val breathScale by rememberInfiniteTransition(label = "breath").animateFloat(
        initialValue  = 1.00f,
        targetValue   = 1.01f,
        animationSpec = infiniteRepeatable(
            animation  = tween(4_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    val rem      = lock.remainingMs.coerceAtLeast(0L)
    val remH     = (rem / 3_600_000L).toInt()
    val remM     = ((rem % 3_600_000L) / 60_000L).toInt()
    val remLabel = when {
        remH > 0 && remM > 0 -> "${remH}h ${remM}m"
        remH > 0              -> "${remH}h"
        else                  -> "${remM}m"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .pointerInput(Unit) {
                detectTapGestures { overlayVisible = !overlayVisible }
            }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

            // LEFT 65% — massive countdown
            Box(
                modifier         = Modifier
                    .fillMaxHeight()
                    .weight(0.65f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text     = "%02d:%02d:%02d".format(hh, mm, ss),
                    style    = TextStyle(
                        fontSize   = 80.sp,
                        fontWeight = FontWeight.Bold,
                        color      = White,
                        fontFamily = FontFamily.Monospace
                    ),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.scale(breathScale)
                )
            }

            // RIGHT 35% — session info
            Column(
                modifier              = Modifier
                    .fillMaxHeight()
                    .weight(0.35f)
                    .padding(end = 40.dp),
                verticalArrangement   = Arrangement.Center,
                horizontalAlignment   = Alignment.Start
            ) {
                Text(
                    text  = "FOCUS SESSION",
                    style = TextStyle(
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.Medium,
                        color         = White.copy(alpha = 0.30f),
                        letterSpacing = 2.sp
                    )
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text  = "Time Remaining",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color    = White.copy(alpha = 0.40f)
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = remLabel,
                    style = TextStyle(
                        fontSize   = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = White
                    )
                )
                Spacer(Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(White.copy(alpha = 0.10f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(lock.progressFraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(1.dp))
                            .background(accentGradient)
                    )
                }
                Spacer(Modifier.height(24.dp))
                if (lock.sessionEndTime.isNotBlank()) {
                    Text(
                        text  = "Ends at ${lock.sessionEndTime}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color    = White.copy(alpha = 0.30f)
                        )
                    )
                }
            }
        }

        if (overlayVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Black.copy(alpha = 0.75f))
                    .pointerInput(Unit) { detectTapGestures { overlayVisible = false } }
            ) {
                Column(
                    modifier            = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(240.dp)
                            .height(54.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(accentGradient)
                            .pointerInput(Unit) { detectTapGestures { onReturn() } },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Return to Portrait",
                            style = TextStyle(
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = Black
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(240.dp)
                            .height(54.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0xFF2C2C2E))
                            .pointerInput(Unit) { detectTapGestures { onEndSession() } },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "End Session",
                            style = TextStyle(
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color      = White.copy(alpha = 0.60f)
                            )
                        )
                    }
                }
            }
        }
    }
}