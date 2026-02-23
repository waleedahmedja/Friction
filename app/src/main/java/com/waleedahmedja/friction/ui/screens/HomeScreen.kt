package com.waleedahmedja.friction.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import com.waleedahmedja.friction.viewmodel.FrictionViewModel
import com.waleedahmedja.friction.ui.GradientButton
import com.waleedahmedja.friction.ui.InfiniteWheel
import com.waleedahmedja.friction.ui.theme.*

@Composable
fun HomeScreen(vm: FrictionViewModel, onStart: () -> Unit) {

    val hours        by vm.hours.collectAsStateWithLifecycle()
    val minutes      by vm.minutes.collectAsStateWithLifecycle()
    val totalMinutes by vm.totalMinutes.collectAsStateWithLifecycle()
    val enabled       = totalMinutes > 0

    val timeLabel = when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0                -> "${hours}h"
        minutes > 0              -> "${minutes}m"
        else                     -> "—"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))

            // FOCUS TIME label
            Text(
                text  = "FOCUS TIME",
                style = TextStyle(
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Medium,
                    color         = White.copy(alpha = 0.60f),
                    letterSpacing = 2.sp
                )
            )

            Spacer(Modifier.height(16.dp))

            // Large animated time preview
            AnimatedContent(
                targetState    = timeLabel,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label          = "time_label"
            ) { label ->
                Text(
                    text      = label,
                    style     = TextStyle(
                        fontSize   = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color      = White
                    ),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(40.dp))

            // Wheel row — labels 8dp above their wheel
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.Top
            ) {
                // Hours
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text  = "HR",
                        style = TextStyle(
                            fontSize      = 11.sp,
                            fontWeight    = FontWeight.Medium,
                            color         = White.copy(alpha = 0.40f),
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    InfiniteWheel(
                        range    = 0..24,
                        selected = hours,
                        onSelect = vm::setHours
                    )
                }

                Spacer(Modifier.width(40.dp))

                // Minutes
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text  = "MIN",
                        style = TextStyle(
                            fontSize      = 11.sp,
                            fontWeight    = FontWeight.Medium,
                            color         = White.copy(alpha = 0.40f),
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    InfiniteWheel(
                        range    = 0..59,
                        selected = minutes,
                        onSelect = vm::setMinutes
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            GradientButton(
                text    = "Start Lock",
                onClick = { vm.startLock(); onStart() },
                enabled = enabled
            )

            Spacer(Modifier.height(48.dp))
        }

        Text(
            text     = "waleedahmedja",
            style    = TextStyle(
                fontSize = 11.sp,
                color    = White.copy(alpha = 0.10f)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}