package com.waleedahmedja.friction.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.waleedahmedja.friction.ui.theme.AccentYellow
import com.waleedahmedja.friction.ui.theme.FrictionTheme
import com.waleedahmedja.friction.viewmodel.FrictionViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Date/time formatters — allocated once at file level, never inside a composable
private val clockHourFmt = SimpleDateFormat("HH",         Locale.getDefault())
private val clockMinFmt  = SimpleDateFormat("mm",         Locale.getDefault())
private val clockDateFmt = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
private val endsAtFmt    = SimpleDateFormat("h:mm a",     Locale.getDefault())

// Tsuki's mood states — drives which animation plays
private enum class TsukiMood { IDLE, STRETCHING, DOZING }

// ─────────────────────────────────────────────────────────────────────────────
// StandbyScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StandbyScreen(
    vm    : FrictionViewModel,
    onBack: () -> Unit
) {
    val c    = FrictionTheme.c
    val lock by vm.lock.collectAsStateWithLifecycle()

    // Clock ticks once per second
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) { delay(1_000L); nowMs = System.currentTimeMillis() }
    }

    val hourStr = remember(nowMs) { clockHourFmt.format(Date(nowMs)) }
    val minStr  = remember(nowMs) { clockMinFmt .format(Date(nowMs)) }
    val dateStr = remember(nowMs) { clockDateFmt.format(Date(nowMs)) }

    val totalMs   = lock.durationMs.coerceAtLeast(1L)
    val remainMs  = lock.remainingMs.coerceAtLeast(0L)
    val progress  = (1f - remainMs.toFloat() / totalMs).coerceIn(0f, 1f)
    val endsAtStr = remember(lock.endsAtMs) { endsAtFmt.format(Date(lock.endsAtMs)) }

    val remH   = (remainMs / 3_600_000L).toInt()
    val remM   = ((remainMs % 3_600_000L) / 60_000L).toInt()
    val remS   = ((remainMs % 60_000L) / 1_000L).toInt()
    val remStr = if (remH > 0) "%d:%02d:%02d".format(remH, remM, remS)
                 else          "%02d:%02d".format(remM, remS)

    // OLED burn-in prevention — micro pixel drift every 30 s
    var driftX by remember { mutableFloatStateOf(0f) }
    var driftY by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val offsets = listOf(0f to 0f, 4f to -2f, -3f to 4f, 2f to 3f, -4f to -3f, 1f to -4f)
        var idx = 0
        while (true) {
            delay(30_000L)
            idx    = (idx + 1) % offsets.size
            driftX = offsets[idx].first
            driftY = offsets[idx].second
        }
    }

    // Breathing yellow dot animation
    val breathAnim = rememberInfiniteTransition(label = "breath")
    val dotAlpha by breathAnim.animateFloat(
        initialValue  = 0.25f,
        targetValue   = 0.85f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2_500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    // Tsuki mood sequencer
    var tsukiMood by remember { mutableStateOf(TsukiMood.IDLE) }
    LaunchedEffect(Unit) {
        val phases = listOf(
            TsukiMood.IDLE       to 14_000L,
            TsukiMood.STRETCHING to  5_000L,
            TsukiMood.IDLE       to 12_000L,
            TsukiMood.DOZING     to  7_000L
        )
        var i = 0
        while (true) {
            val (mood, duration) = phases[i % phases.size]
            tsukiMood = mood
            delay(duration)
            i++
        }
    }

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(c.bg)
            .pointerInput(Unit) { detectTapGestures { onBack() } },
        contentAlignment = Alignment.Center
    ) {

        // Clock column with OLED drift offset
        Column(
            modifier            = Modifier.offset(driftX.dp, driftY.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text  = dateStr.uppercase(),
                style = TextStyle(
                    fontSize      = 10.sp,
                    fontWeight    = FontWeight.Light,
                    letterSpacing = 3.sp,
                    color         = c.text.copy(alpha = 0.30f),
                    fontFamily    = FontFamily.Monospace
                )
            )

            Spacer(Modifier.height(20.dp))

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FlipDigitPair(text = hourStr)

                // Blinking colon dots
                val colonAlpha = if (remember(nowMs) { (nowMs / 1_000L) % 2L == 0L }) 0.75f else 0.15f
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(50))
                                .alpha(colonAlpha)
                                .background(c.text.copy(alpha = 0.7f))
                        )
                    }
                }

                FlipDigitPair(text = minStr)
            }

            Spacer(Modifier.height(28.dp))

            Box(contentAlignment = Alignment.Center) {
                ArcProgressRing(
                    progress    = progress,
                    sizeDp      = 88.dp,
                    accentColor = AccentYellow,
                    trackColor  = c.text.copy(alpha = 0.08f),
                    strokeDp    = 3.dp
                )
                Text(
                    text  = remStr,
                    style = TextStyle(
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Light,
                        color      = c.textSub,
                        fontFamily = FontFamily.Monospace,
                        textAlign  = TextAlign.Center
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text  = "ends $endsAtStr",
                style = TextStyle(
                    fontSize      = 10.sp,
                    letterSpacing = 1.5.sp,
                    color         = c.textHint,
                    fontFamily    = FontFamily.Monospace
                )
            )

            Spacer(Modifier.height(28.dp))

            // Breathing yellow dot — indicates session is live
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(RoundedCornerShape(50))
                    .alpha(dotAlpha)
                    .background(AccentYellow)
            )
        }

        // Tsuki — lower-right, clearly visible, non-intrusive
        TsukiContainer(mood = tsukiMood)

        // Barely-visible tap hint
        Text(
            text     = "tap to return",
            style    = TextStyle(
                fontSize      = 9.sp,
                letterSpacing = 0.8.sp,
                color         = c.textHint.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )
    }
}

// Tsuki wrapper — extracted to BoxScope so .align() is in scope
@Composable
private fun BoxScope.TsukiContainer(mood: TsukiMood) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 32.dp, bottom = 64.dp)
            .size(76.dp)
    ) {
        TsukiCharacter(mood = mood)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TSUKI CHARACTER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TsukiCharacter(mood: TsukiMood) {

    var blinking by remember { mutableStateOf(false) }
    LaunchedEffect(mood) {
        while (mood == TsukiMood.IDLE) {
            delay(4_000L); blinking = true; delay(120L); blinking = false
        }
    }

    val idleTrans = rememberInfiniteTransition(label = "tsukiBob")
    val bobOffset by idleTrans.animateFloat(
        initialValue  = 0f,
        targetValue   = -2.5f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1_600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    val stretchTrans = rememberInfiniteTransition(label = "tsukiStretch")
    val armLift by stretchTrans.animateFloat(
        initialValue  = 0f,
        targetValue   = if (mood == TsukiMood.STRETCHING) -50f else 0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "armLift"
    )
    val stretchHeadTilt by stretchTrans.animateFloat(
        initialValue  = 0f,
        targetValue   = if (mood == TsukiMood.STRETCHING) 7f else 0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stretchTilt"
    )

    val dozeTrans = rememberInfiniteTransition(label = "tsukiDoze")
    val headNod by dozeTrans.animateFloat(
        initialValue  = 0f,
        targetValue   = if (mood == TsukiMood.DOZING) 20f else 0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1_900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "headNod"
    )
    val zRiseOffset by dozeTrans.animateFloat(
        initialValue  = 0f,
        targetValue   = if (mood == TsukiMood.DOZING) -18f else 0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2_400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "zRise"
    )
    val zOpacity by dozeTrans.animateFloat(
        initialValue  = 0f,
        targetValue   = if (mood == TsukiMood.DOZING) 0.8f else 0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2_400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "zOpacity"
    )

    val eyesClosed = blinking || mood == TsukiMood.DOZING
    val headTilt   = if (mood == TsukiMood.DOZING) headNod else stretchHeadTilt

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawTsukiBody(
            cx          = size.width / 2f,
            groundY     = size.height + bobOffset,
            eyesClosed  = eyesClosed,
            armLiftDeg  = armLift,
            headTiltDeg = headTilt,
            zRiseOffset = zRiseOffset,
            zOpacity    = zOpacity,
            scale       = size.width / 76f
        )
    }
}

private fun DrawScope.drawTsukiBody(
    cx          : Float,
    groundY     : Float,
    eyesClosed  : Boolean,
    armLiftDeg  : Float,
    headTiltDeg : Float,
    zRiseOffset : Float,
    zOpacity    : Float,
    scale       : Float
) {
    val s          = scale
    val bodyWhite  = Color(0xFFF5EFE6)
    val earPink    = Color(0xFFFFB3C1)
    val eyeBrown   = Color(0xFF2D1B0E)
    val cheekPink  = Color(0xFFFFB3C1).copy(alpha = 0.40f)
    val noseColor  = Color(0xFFFFAABB)
    val dropShadow = Color.Black.copy(alpha = 0.10f)

    val bodyRadius = 18f * s
    val bodyY      = groundY - bodyRadius - 2f * s
    val headRadius = 13.5f * s
    val headY      = bodyY - bodyRadius - headRadius + 5f * s

    drawOval(
        color   = dropShadow,
        topLeft = Offset(cx - bodyRadius * 0.9f, groundY - 3f * s),
        size    = Size(bodyRadius * 1.8f, 5f * s)
    )

    val shoulderY = bodyY - bodyRadius * 0.25f
    val armWidth  = 9f * s
    val armHeight = 6f * s

    rotate(degrees = armLiftDeg, pivot = Offset(cx - bodyRadius + 2f * s, shoulderY)) {
        drawRoundRect(
            color        = bodyWhite,
            topLeft      = Offset(cx - bodyRadius - 7f * s, shoulderY - armHeight / 2f),
            size         = Size(armWidth, armHeight),
            cornerRadius = CornerRadius(3f * s)
        )
    }
    rotate(degrees = -armLiftDeg, pivot = Offset(cx + bodyRadius - 2f * s, shoulderY)) {
        drawRoundRect(
            color        = bodyWhite,
            topLeft      = Offset(cx + bodyRadius - 2f * s, shoulderY - armHeight / 2f),
            size         = Size(armWidth, armHeight),
            cornerRadius = CornerRadius(3f * s)
        )
    }

    drawCircle(color = bodyWhite, radius = bodyRadius, center = Offset(cx, bodyY))
    drawCircle(
        color  = Color(0xFFFFF5EC),
        radius = bodyRadius * 0.48f,
        center = Offset(cx, bodyY + bodyRadius * 0.12f)
    )

    val neckPivot = Offset(cx, headY + headRadius * 0.85f)
    rotate(degrees = headTiltDeg, pivot = neckPivot) {

        val earWidth  = 5.5f * s
        val earHeight = 18f * s
        val earTopY   = headY - headRadius - earHeight + 3f * s

        for (side in listOf(-1f, 1f)) {
            val earCx = cx + side * headRadius * 0.55f
            drawRoundRect(
                color        = bodyWhite,
                topLeft      = Offset(earCx - earWidth / 2f, earTopY),
                size         = Size(earWidth, earHeight),
                cornerRadius = CornerRadius(earWidth / 2f)
            )
            drawRoundRect(
                color        = earPink,
                topLeft      = Offset(earCx - earWidth * 0.25f, earTopY + earHeight * 0.08f),
                size         = Size(earWidth * 0.55f, earHeight * 0.7f),
                cornerRadius = CornerRadius(earWidth * 0.28f)
            )
        }

        drawCircle(color = bodyWhite, radius = headRadius, center = Offset(cx, headY))

        val eyeY     = headY - headRadius * 0.08f
        val eyeSpacX = headRadius * 0.33f

        if (eyesClosed) {
            for (side in listOf(-1f, 1f)) {
                drawArc(
                    color      = eyeBrown,
                    startAngle = 195f,
                    sweepAngle = 150f,
                    useCenter  = false,
                    topLeft    = Offset(cx + side * eyeSpacX - 3.5f * s, eyeY - 3f * s),
                    size       = Size(7f * s, 5.5f * s),
                    style      = Stroke(width = 1.4f * s, cap = StrokeCap.Round)
                )
            }
        } else {
            for (side in listOf(-1f, 1f)) {
                drawCircle(
                    color  = eyeBrown,
                    radius = 2.8f * s,
                    center = Offset(cx + side * eyeSpacX, eyeY)
                )
                drawCircle(
                    color  = Color.White.copy(alpha = 0.65f),
                    radius = 0.85f * s,
                    center = Offset(cx + side * eyeSpacX + 1.1f * s, eyeY - 1.1f * s)
                )
            }
        }

        drawOval(
            color   = noseColor,
            topLeft = Offset(cx - 2f * s, headY + headRadius * 0.16f),
            size    = Size(4f * s, 2.8f * s)
        )

        for (side in listOf(-1f, 1f)) {
            drawCircle(
                color  = cheekPink,
                radius = 3.8f * s,
                center = Offset(cx + side * headRadius * 0.56f, headY + headRadius * 0.22f)
            )
        }

        if (zOpacity > 0f) {
            val zLeft  = cx + headRadius * 0.75f
            val zTop   = headY - headRadius * 1.1f + zRiseOffset
            val zSize  = 6.5f * s * (0.5f + zOpacity * 0.5f)
            val zColor = AccentYellow.copy(alpha = zOpacity * 0.75f)
            val sw     = 1.5f * s
            drawLine(zColor, Offset(zLeft, zTop),         Offset(zLeft + zSize, zTop),         sw, StrokeCap.Round)
            drawLine(zColor, Offset(zLeft + zSize, zTop), Offset(zLeft, zTop + zSize),         sw, StrokeCap.Round)
            drawLine(zColor, Offset(zLeft, zTop + zSize), Offset(zLeft + zSize, zTop + zSize), sw, StrokeCap.Round)
        }
    }

    for (side in listOf(-1f, 1f)) {
        drawRoundRect(
            color        = bodyWhite,
            topLeft      = Offset(cx + side * bodyRadius * 0.05f - 6f * s, groundY - 5f * s),
            size         = Size(11f * s, 6f * s),
            cornerRadius = CornerRadius(3.5f * s)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FLIP DIGIT PAIR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FlipDigitPair(text: String) {
    val c  = FrictionTheme.c
    // In dark mode use the classic dark card; in light mode use a light surface
    val cardBg     = if (c.isDark) Color(0xFF1C1C1C) else c.surface
    val digitColor = c.text
    val d1 = text.getOrElse(0) { '0' }.toString()
    val d2 = text.getOrElse(1) { '0' }.toString()
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        FlipCard(digit = d1, cardBg = cardBg, digitColor = digitColor)
        FlipCard(digit = d2, cardBg = cardBg, digitColor = digitColor)
    }
}

@Composable
private fun FlipCard(digit: String, cardBg: Color, digitColor: Color) {
    var displayed      by remember { mutableStateOf(digit) }
    var flipInProgress by remember { mutableStateOf(false) }

    LaunchedEffect(digit) {
        if (digit != displayed && !flipInProgress) {
            flipInProgress = true
            delay(75L)
            displayed = digit
            delay(75L)
            flipInProgress = false
        }
    }

    val rotX by animateFloatAsState(
        targetValue   = if (flipInProgress) -90f else 0f,
        animationSpec = tween(durationMillis = 75, easing = FastOutSlowInEasing),
        label         = "flipCardRotX"
    )

    Box(
        modifier = Modifier
            .width(52.dp)
            .height(70.dp)
            .graphicsLayer(
                rotationX      = rotX,
                cameraDistance = 14f * 160f
            )
    ) {
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .background(cardBg),
            contentAlignment = Alignment.Center
        ) {
            // Centre crease — the fold line of a Solari board card
            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(0.5.dp)
                    .background(digitColor.copy(alpha = 0.15f))
            )
            Text(
                text  = displayed,
                style = TextStyle(
                    fontSize      = 44.sp,
                    fontWeight    = FontWeight.Bold,
                    fontFamily    = FontFamily.Monospace,
                    color         = digitColor,
                    textAlign     = TextAlign.Center,
                    lineHeight    = 70.sp
                )
            )
        }

        // Top-half gloss
        Box(
            modifier = Modifier
                .width(52.dp)
                .height(35.dp)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(Color.White.copy(alpha = 0.022f))
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ARC PROGRESS RING
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ArcProgressRing(
    progress    : Float,
    sizeDp      : Dp,
    accentColor : Color,
    trackColor  : Color,
    strokeDp    : Dp
) {
    val animated by animateFloatAsState(
        targetValue   = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label         = "arcProgress"
    )

    Canvas(modifier = Modifier.size(sizeDp)) {
        val strokePx  = strokeDp.toPx()
        val inset     = strokePx / 2f
        val arcBounds = Size(size.width - strokePx, size.height - strokePx)
        val topLeft   = Offset(inset, inset)

        drawArc(
            color      = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter  = false,
            topLeft    = topLeft,
            size       = arcBounds,
            style      = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
        if (animated > 0f) {
            drawArc(
                color      = accentColor,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcBounds,
                style      = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
    }
}
