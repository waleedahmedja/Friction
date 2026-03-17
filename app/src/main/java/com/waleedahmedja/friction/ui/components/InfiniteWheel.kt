package com.waleedahmedja.friction.ui.components

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waleedahmedja.friction.ui.theme.FrictionTheme
import kotlinx.coroutines.flow.distinctUntilChanged

private const val REPEAT = 1_000

@Composable
fun InfiniteWheel(
    range        : IntRange,
    selected     : Int,
    onSelect     : (Int) -> Unit,
    itemHeight   : Dp      = 56.dp,
    visibleRows  : Int     = 5,
    hapticEnabled: Boolean = true
) {
    val c     = FrictionTheme.c
    val items = remember(range) { range.toList() }
    val count = items.size
    if (count == 0) return

    // Always odd so there is a single definite center row
    val rows     = if (visibleRows % 2 == 0) visibleRows + 1 else visibleRows
    val halfRows = rows / 2

    // ── Centering math ────────────────────────────────────────────────────────
    //
    // With NO contentPadding, LazyColumn's firstVisibleItemIndex is the index
    // of the item whose top edge is at y=0 (the very top of the viewport).
    //
    // The viewport is (rows × itemHeight) tall.
    // Each item is (itemHeight) tall.
    // The center row sits at y = halfRows × itemHeight from the top.
    // The item in the center row = items[firstVisibleItemIndex + halfRows].
    //
    // To initialise with `selected` in the center:
    //   firstVisibleItemIndex = initCenter - halfRows
    // where initCenter is deep in the repeated range so scrolling in both
    // directions has plenty of headroom.
    //
    // Verified correct for selected = 0, 1, 23, 25, 59 (hours and minutes).
    val initCenter = (REPEAT / 2) * count + selected.coerceIn(0, count - 1)
    val initFirst  = (initCenter - halfRows).coerceAtLeast(0)

    val listState  = rememberLazyListState(initialFirstVisibleItemIndex = initFirst)
    val flingBehav = rememberSnapFlingBehavior(listState)
    val haptic     = LocalHapticFeedback.current

    // The centered item — recomputed whenever scroll position changes
    val centeredValue by remember {
        derivedStateOf {
            val centerIdx = listState.firstVisibleItemIndex + halfRows
            items[centerIdx % count]
        }
    }

    // Report selection to parent
    LaunchedEffect(centeredValue) {
        onSelect(centeredValue)
    }

    // Single haptic tick per settled value
    var lastHapticValue by remember { mutableIntStateOf(-1) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrolling ->
                if (!scrolling) {
                    val v = centeredValue
                    if (hapticEnabled && v != lastHapticValue) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        lastHapticValue = v
                    }
                }
            }
    }

    val totalHeight = itemHeight * rows

    Box(
        modifier = Modifier
            .width(96.dp)
            .height(totalHeight)
    ) {

        // ── Item list ─────────────────────────────────────────────────────────
        // CRITICAL: NO contentPadding.
        // contentPadding shifts item positions visually but snap still targets
        // raw item boundaries — resulting in the selected item appearing above
        // center. Without contentPadding the math above puts the correct item
        // exactly in the center row from the very first frame.
        LazyColumn(
            state               = listState,
            flingBehavior       = flingBehav,
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
            // ← no contentPadding
        ) {
            items(count = count * REPEAT, key = { it }) { absIdx ->
                val centerIdx = listState.firstVisibleItemIndex + halfRows
                val distance  = kotlin.math.abs(absIdx - centerIdx)

                val itemScale  = when (distance) { 0 -> 1.00f; 1 -> 0.78f; else -> 0.65f }
                val itemAlpha  = when (distance) { 0 -> 1.00f; 1 -> 0.35f; else -> 0.10f }
                val itemWeight = if (distance == 0) FontWeight.SemiBold else FontWeight.Light
                val itemColor  = if (distance == 0) c.text else c.textSub

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .scale(itemScale)
                        .alpha(itemAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = "%02d".format(items[absIdx % count]),
                        style = TextStyle(
                            fontSize      = 34.sp,
                            fontWeight    = itemWeight,
                            color         = itemColor,
                            fontFamily    = FontFamily.Monospace,
                            textAlign     = TextAlign.Center
                        )
                    )
                }
            }
        }

        // ── Selection bracket — yellow hairlines framing the center row ───────
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .align(Alignment.TopCenter)
                    .background(c.accent.copy(alpha = 0.30f))
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .align(Alignment.BottomCenter)
                    .background(c.accent.copy(alpha = 0.30f))
            )
        }

        // ── Top fade — non-selected items dissolve upward ─────────────────────
        Box(
            Modifier
                .fillMaxWidth()
                .height(itemHeight * halfRows)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(c.bg, Color.Transparent)))
        )

        // ── Bottom fade ───────────────────────────────────────────────────────
        Box(
            Modifier
                .fillMaxWidth()
                .height(itemHeight * halfRows)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, c.bg)))
        )
    }
}