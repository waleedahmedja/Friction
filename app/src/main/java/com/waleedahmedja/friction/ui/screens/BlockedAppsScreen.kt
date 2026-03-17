package com.waleedahmedja.friction.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.waleedahmedja.friction.ui.theme.AccentYellow
import com.waleedahmedja.friction.ui.theme.FrictionColors
import com.waleedahmedja.friction.ui.theme.FrictionTheme
import com.waleedahmedja.friction.viewmodel.AppInfo
import com.waleedahmedja.friction.viewmodel.FrictionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ─────────────────────────────────────────────────────────────────────────────
// BlockedAppsScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BlockedAppsScreen(
    vm    : FrictionViewModel,
    onBack: () -> Unit
) {
    val c       = FrictionTheme.c
    val allApps by vm.installedApps  .collectAsStateWithLifecycle()
    val blocked by vm.blockedPackages.collectAsStateWithLifecycle()
    val allowed by vm.allowedPackages.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }
    var tab   by remember { mutableIntStateOf(0) } // 0 = All apps, 1 = Allow list

    LaunchedEffect(Unit) { vm.loadInstalledApps() }

    val filtered = remember(allApps, query) {
        if (query.isBlank()) allApps
        else allApps.filter { app ->
            app.label.contains(query, ignoreCase = true) ||
            app.packageName.contains(query, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(c.bg)) {

        Spacer(Modifier.height(52.dp))

        // ── Top bar ───────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.CenterStart)
                    .pointerInput(Unit) { detectTapGestures { onBack() } },
                contentAlignment = Alignment.Center
            ) {
                Text("←", style = TextStyle(fontSize = 22.sp, color = c.textHint))
            }
            Text(
                text     = if (tab == 0) "BLOCKED APPS" else "ALLOW LIST",
                style    = TextStyle(
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Medium,
                    color         = c.textHint,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Blocked count chip ────────────────────────────────────────────────
        if (blocked.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AccentYellow.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    "${blocked.size} blocked",
                    style = TextStyle(
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color      = AccentYellow
                    )
                )
            }
            Spacer(Modifier.height(10.dp))
        } else {
            Spacer(Modifier.height(4.dp))
        }

        // ── Tab switcher ──────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All Apps", "Allow List").forEachIndexed { idx, label ->
                val selected = tab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected) AccentYellow.copy(alpha = 0.13f) else c.surface
                        )
                        .pointerInput(idx) { detectTapGestures { tab = idx } }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        style = TextStyle(
                            fontSize   = 14.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color      = if (selected) AccentYellow else c.textSub
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Search field ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(c.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value         = query,
                onValueChange = { query = it },
                textStyle     = TextStyle(fontSize = 15.sp, color = c.text),
                cursorBrush   = SolidColor(AccentYellow),
                singleLine    = true,
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            "Search apps…",
                            style = TextStyle(fontSize = 15.sp, color = c.textHint)
                        )
                    }
                    inner()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── App list ──────────────────────────────────────────────────────────
        if (tab == 0) {
            AppList(
                c            = c,
                apps         = filtered,
                selectedPkgs = blocked,
                emptyMsg     = when {
                    allApps.isEmpty()      -> "Loading apps…"
                    query.isNotBlank()     -> "No apps match \"$query\""
                    else                   -> "No apps found"
                },
                isBlockList  = true,
                onToggle     = { vm.toggleBlocked(it) }
            )
        } else {
            // Allow list — only shows apps that are already blocked
            val blockedApps = remember(filtered, blocked) {
                filtered.filter { it.packageName in blocked }
            }
            AppList(
                c            = c,
                apps         = blockedApps,
                selectedPkgs = allowed,
                emptyMsg     = if (blocked.isEmpty()) "Block some apps first to add them here."
                               else "No blocked apps match \"$query\"",
                isBlockList  = false,
                onToggle     = { vm.toggleAllowed(it) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AppList
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AppList(
    c           : FrictionColors,
    apps        : List<AppInfo>,
    selectedPkgs: Set<String>,
    emptyMsg    : String,
    isBlockList : Boolean,
    onToggle    : (String) -> Unit
) {
    if (apps.isEmpty()) {
        Box(
            modifier         = Modifier.fillMaxWidth().padding(top = 64.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(emptyMsg, style = TextStyle(fontSize = 14.sp, color = c.textHint))
        }
        return
    }

    LazyColumn(
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(apps, key = { it.packageName }) { app ->
            val isSelected = app.packageName in selectedPkgs
            AppRow(
                c          = c,
                app        = app,
                isSelected = isSelected,
                isBlockList = isBlockList,
                onToggle   = { onToggle(app.packageName) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AppRow — single app item with icon, name, package, checkbox
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AppRow(
    c          : FrictionColors,
    app        : AppInfo,
    isSelected : Boolean,
    isBlockList: Boolean,
    onToggle   : () -> Unit
) {
    val context = LocalContext.current

    // Load icon asynchronously on IO, cache by package name.
    // null while loading — we show a placeholder square instead.
    var icon by remember(app.packageName) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(app.packageName) {
        if (icon != null) return@LaunchedEffect   // already loaded
        icon = withContext(Dispatchers.IO) {
            try {
                val drawable = context.packageManager.getApplicationIcon(app.packageName)
                drawable.toImageBitmap(48)
            } catch (e: Exception) {
                null   // app uninstalled between load and display — safe to ignore
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) AccentYellow.copy(alpha = 0.08f) else c.surface
            )
            .pointerInput(app.packageName) {
                detectTapGestures { onToggle() }
            }
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ── App icon ──────────────────────────────────────────────────────────
        Box(
            modifier         = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(c.surface2),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Image(
                    bitmap      = icon!!,
                    contentDescription = app.label,
                    modifier    = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                )
            }
            // While icon is loading the surface2 background acts as placeholder
        }

        // ── App name + package ────────────────────────────────────────────────
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = app.label,
                style    = TextStyle(
                    fontSize   = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color      = c.text
                ),
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text     = app.packageName,
                style    = TextStyle(fontSize = 11.sp, color = c.textHint),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // ── Checkbox ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isSelected) AccentYellow else c.surface2
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Text(
                    text  = if (isBlockList) "✕" else "✓",
                    style = TextStyle(
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color      = c.btnText
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Drawable → ImageBitmap
// Converts any Drawable (BitmapDrawable, AdaptiveIconDrawable, VectorDrawable)
// to a fixed-size ImageBitmap that Compose can render.
// ─────────────────────────────────────────────────────────────────────────────

private fun Drawable.toImageBitmap(sizePx: Int): ImageBitmap {
    // Fast path — if it's already a BitmapDrawable, just scale it
    if (this is BitmapDrawable && bitmap != null) {
        val scaled = Bitmap.createScaledBitmap(bitmap, sizePx, sizePx, true)
        return scaled.asImageBitmap()
    }

    // General path — draw the Drawable onto a Canvas backed by a Bitmap
    // This handles AdaptiveIconDrawable (Android 8+) and VectorDrawable
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, sizePx, sizePx)
    draw(canvas)
    return bitmap.asImageBitmap()
}
