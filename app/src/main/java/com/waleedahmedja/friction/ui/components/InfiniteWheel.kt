package com.waleedahmedja.friction.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waleedahmedja.friction.ui.theme.Black
import com.waleedahmedja.friction.ui.theme.White
import kotlin.math.abs

private const val MULTIPLIER = 1_000

@Composable
fun InfiniteWheel(
    range       : IntRange,
    selected    : Int,
    onSelect    : (Int) -> Unit,
    itemHeight  : Dp  = 52.dp,
    visibleRows : Int = 5
) {
    val items = remember(range) { range.toList() }
    val count = items.size
    val total = count * MULTIPLIER

    // With contentPadding top = itemHeight * 2, the LazyColumn's
    // firstVisibleItemIndex = 0 means the first REAL item is visually
    // at position 2 (centre). So centreIdx = firstVisibleItemIndex + 0,
    // NOT + visibleRows/2. The padding handles the visual offset.
    val halfRows = visibleRows / 2  // = 2

    // Initial scroll: centre the selected value.
    // firstVisibleItemIndex should be (selectedIdx - 0) because
    // the top padding already pushes items down by halfRows visually.
    val initIdx = remember(count, selected) {
        val mid         = (MULTIPLIER / 2) * count
        val selectedPos = selected.coerceIn(0, (count - 1).coerceAtLeast(0))
        (mid + selectedPos).coerceAtLeast(0)
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initIdx)
    val snap      = rememberSnapFlingBehavior(lazyListState = listState)
    val haptic    = LocalHapticFeedback.current
    var lastSnap  by remember { mutableIntStateOf(-1) }

    // The selected item is the one at firstVisibleItemIndex
    // because contentPadding top = halfRows * itemHeight makes
    // that item appear at the vertical centre of the wheel.
    val centreIdx by remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }

    // Report selection when scroll settles
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress && count > 0) {
            val real = items[centreIdx.coerceIn(0, total - 1) % count]
            if (real != selected) onSelect(real)
        }
    }

    // Haptic per new centre
    LaunchedEffect(centreIdx) {
        if (centreIdx != lastSnap && count > 0) {
            lastSnap = centreIdx
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val wheelHeight = itemHeight * visibleRows

    Box(
        modifier = Modifier
            .width(100.dp)
            .height(wheelHeight)
    ) {
        // Top indicator line — top edge of centre cell
        // Centre cell starts at halfRows * itemHeight from top of wheel
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = itemHeight * halfRows)
                .fillMaxWidth()
                .height(0.5.dp)
                .background(White.copy(alpha = 0.25f))
        )
        // Bottom indicator line — bottom edge of centre cell
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = itemHeight * (halfRows + 1))
                .fillMaxWidth()
                .height(0.5.dp)
                .background(White.copy(alpha = 0.25f))
        )

        LazyColumn(
            state          = listState,
            flingBehavior  = snap,
            modifier       = Modifier.fillMaxSize(),
            // Top padding pushes item[0] down to the centre row visually.
            // Bottom padding allows scrolling item[last] up to centre row.
            contentPadding = PaddingValues(
                top    = itemHeight * halfRows,
                bottom = itemHeight * halfRows
            )
        ) {
            items(total) { idx ->
                val value = if (count > 0) items[idx % count] else 0
                // Distance from the currently centred item
                val dist  = abs(idx - centreIdx)

                val targetAlpha = if (dist == 0) 1.00f else 0.35f
                val targetScale = if (dist == 0) 1.00f else 0.88f

                val alpha by animateFloatAsState(
                    targetValue   = targetAlpha,
                    animationSpec = tween(150, easing = FastOutLinearInEasing),
                    label         = "a$idx"
                )
                val scale by animateFloatAsState(
                    targetValue   = targetScale,
                    animationSpec = tween(150, easing = FastOutLinearInEasing),
                    label         = "s$idx"
                )

                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .alpha(alpha)
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = value.toString().padStart(2, '0'),
                        style = TextStyle(
                            fontSize   = 32.sp,
                            fontWeight = if (dist == 0) FontWeight.SemiBold
                            else FontWeight.Light,
                            color      = White,
                            textAlign  = TextAlign.Center
                        )
                    )
                }
            }
        }

        // Top gradient — hides items above selection naturally
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * halfRows.toFloat())
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(listOf(Black, Color.Transparent))
                )
        )
        // Bottom gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * halfRows.toFloat())
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Black))
                )
        )
    }
}