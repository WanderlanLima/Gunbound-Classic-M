package com.example.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AimingMode
import com.example.data.AssetSpriteLoader
import com.example.data.GameCatalog
import com.example.data.GameOptions
import com.example.data.WeatherType
import com.example.engine.ActorState
import com.example.engine.BattleUiState
import com.example.engine.MatchController
import com.example.engine.MatchPhase
import com.example.engine.MatchResultSummary
import com.example.engine.ShotType
import com.example.engine.TerrainMap
import com.example.ui.components.GunboundActionButton
import com.example.ui.theme.GbCrimsonDanger
import com.example.ui.theme.GbCyanPrimary
import com.example.ui.theme.GbEmeraldHp
import com.example.ui.theme.GbGoldAccent
import com.example.ui.theme.GbNavyDeep
import com.example.ui.theme.GbNavySurface
import com.example.ui.theme.GbShieldBlue
import com.example.ui.theme.GbSlatePanel
import com.example.ui.theme.GbSteelBorder
import com.example.ui.theme.GbTeamBlue
import com.example.ui.theme.GbTeamRed
import com.example.ui.theme.GbTextPrimary
import com.example.ui.theme.GbTextSecondary
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun BattleScreen(
    state: BattleUiState?,
    controller: MatchController?,
    options: GameOptions,
    spriteLoader: AssetSpriteLoader,
    onToggleSfx: () -> Unit,
    onSetAimingMode: (AimingMode) -> Unit,
    onSetScrollSpeed: (Float) -> Unit,
    onToggleBgEffects: () -> Unit,
    onExitToRoom: () -> Unit
) {
    if (state == null || controller == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GbNavyDeep),
            contentAlignment = Alignment.Center
        ) {
            Text("Preparing Arena...", color = GbCyanPrimary)
        }
        return
    }

    var showOptionsDialog by remember { mutableStateOf(false) }
    var manualPanOffset by remember { mutableFloatStateOf(0f) }
    var dragOrigin by remember { mutableStateOf<Offset?>(null) }
    var dragCurrent by remember { mutableStateOf<Offset?>(null) }

    BackHandler {
        if (state.result != null) {
            onExitToRoom()
        } else {
            controller.togglePause()
        }
    }

    val mapSpec = remember(state.terrain.spec.index) { state.terrain.spec }
    val terrainImage = remember(state.terrainVersion) { state.terrain.bitmap.asImageBitmap() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GbNavyDeep)
    ) {
        // 1. Main Interactive Battlefield Canvas
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val viewW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
            val viewH = constraints.maxHeight.toFloat().coerceAtLeast(1f)
            val scale = (viewH / TerrainMap.WORLD_H).coerceAtLeast(viewW / TerrainMap.WORLD_W).coerceAtLeast(0.55f)
            val visibleWorldW = viewW / scale
            val maxCamX = (TerrainMap.WORLD_W - visibleWorldW).coerceAtLeast(0f)
            val targetCamX = (state.cameraFocus.x - visibleWorldW / 2f + manualPanOffset).coerceIn(0f, maxCamX)
            val visibleWorldH = viewH / scale
            val maxCamY = (TerrainMap.WORLD_H - visibleWorldH).coerceAtLeast(0f)
            val camY = (state.cameraFocus.y - visibleWorldH * 0.52f).coerceIn(0f, maxCamY)

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(options.aimingMode, state.phase, state.playerTurn, state.isPaused) {
                        detectDragGestures(
                            onDragStart = { start ->
                                if (options.aimingMode == AimingMode.DRAG &&
                                    state.playerTurn &&
                                    state.phase == MatchPhase.PLAYER_AIM &&
                                    !state.isPaused
                                ) {
                                    dragOrigin = start
                                    dragCurrent = start
                                }
                            },
                            onDragEnd = {
                                val o = dragOrigin
                                val c = dragCurrent
                                if (o != null && c != null && options.aimingMode == AimingMode.DRAG && state.playerTurn && state.phase == MatchPhase.PLAYER_AIM) {
                                    val dx = o.x - c.x
                                    val dy = o.y - c.y
                                    val dist = hypot(dx, dy)
                                    if (dist > 22f) {
                                        val power = (dist / 220f).coerceIn(0.1f, 1.0f)
                                        controller.commitDragShot(power)
                                    }
                                }
                                dragOrigin = null
                                dragCurrent = null
                            },
                            onDragCancel = {
                                dragOrigin = null
                                dragCurrent = null
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val o = dragOrigin
                                if (o != null && options.aimingMode == AimingMode.DRAG && state.playerTurn && state.phase == MatchPhase.PLAYER_AIM) {
                                    val curPt = (dragCurrent ?: o) + dragAmount
                                    dragCurrent = curPt
                                    // Pull-back slingshot vector
                                    val pullDx = o.x - curPt.x
                                    val pullDy = o.y - curPt.y
                                    val dist = hypot(pullDx, pullDy)
                                    if (dist > 14f) {
                                        val dirRight = pullDx >= 0f
                                        if (dirRight != state.player.facingRight) {
                                            controller.movePlayer(if (dirRight) 1 else -1)
                                        }
                                        val rawDeg = (atan2(-pullDy.toDouble(), kotlin.math.abs(pullDx).toDouble()) * 180.0 / PI).toInt()
                                        val power = (dist / 220f).coerceIn(0.08f, 1.0f)
                                        controller.updateDragAim(rawDeg, power)
                                    }
                                } else {
                                    manualPanOffset = (manualPanOffset - dragAmount.x * options.scrollSpeed / scale)
                                        .coerceIn(-TerrainMap.WORLD_W * 0.7f, TerrainMap.WORLD_W * 0.7f)
                                }
                            }
                        )
                    }
            ) {
                // Sky background
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(mapSpec.skyTop, mapSpec.skyBottom)
                    )
                )

                // Optional atmospheric stars/clouds
                if (options.showBackgroundEffects) {
                    for (i in 0 until 24) {
                        val sx = ((i * 197 + 43) % TerrainMap.WORLD_W.toInt()).toFloat() - targetCamX * 0.35f
                        val sy = ((i * 89 + 25) % 340).toFloat()
                        drawCircle(
                            color = Color.White.copy(alpha = 0.22f),
                            radius = if (i % 3 == 0) 2.5f else 1.4f,
                            center = Offset(sx * scale, sy * scale)
                        )
                    }
                }

                // Weather columns (Tornado / Force)
                state.tornadoX?.let { tx ->
                    val sx = (tx - targetCamX) * scale
                    drawRect(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0x5538BDF8), Color.Transparent),
                            startX = sx - 38f * scale,
                            endX = sx + 38f * scale
                        ),
                        topLeft = Offset(sx - 38f * scale, 0f),
                        size = Size(76f * scale, size.height)
                    )
                }
                state.forceBeamX?.let { fx ->
                    val sx = (fx - targetCamX) * scale
                    drawRect(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0x55F59E0B), Color.Transparent),
                            startX = sx - 60f * scale,
                            endX = sx + 60f * scale
                        ),
                        topLeft = Offset(sx - 60f * scale, 0f),
                        size = Size(120f * scale, size.height)
                    )
                }

                // Destructible terrain bitmap
                val dstLeft = (-targetCamX * scale).roundToInt()
                val dstTop = (-camY * scale).roundToInt()
                val dstW = (TerrainMap.WORLD_W * scale).roundToInt()
                val dstH = (TerrainMap.WORLD_H * scale).roundToInt()
                drawImage(
                    image = terrainImage,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(terrainImage.width, terrainImage.height),
                    dstOffset = IntOffset(dstLeft, dstTop),
                    dstSize = IntSize(dstW, dstH)
                )

                // Draw Actors (Player & CPU)
                if (state.cpu.isAlive) {
                    drawMobileActor(
                        actor = state.cpu,
                        isActiveTurn = !state.playerTurn && state.phase != MatchPhase.MATCH_OVER,
                        camX = targetCamX,
                        camY = camY,
                        scale = scale
                    )
                }
                if (state.player.isAlive) {
                    drawMobileActor(
                        actor = state.player,
                        isActiveTurn = state.playerTurn && state.phase != MatchPhase.MATCH_OVER,
                        camX = targetCamX,
                        camY = camY,
                        scale = scale
                    )
                }

                // Draw Projectiles & Trails
                state.projectiles.forEach { proj ->
                    if (proj.delayStartTicks > 0) return@forEach
                    if (proj.trail.size >= 2) {
                        val path = Path()
                        proj.trail.forEachIndexed { idx, pt ->
                            val sx = (pt.x - targetCamX) * scale
                            val sy = (pt.y - camY) * scale
                            if (idx == 0) path.moveTo(sx, sy) else path.lineTo(sx, sy)
                        }
                        drawPath(
                            path = path,
                            color = proj.color.copy(alpha = 0.55f),
                            style = Stroke(width = 3.5f * scale, cap = StrokeCap.Round)
                        )
                    }
                    val px = (proj.x - targetCamX) * scale
                    val py = (proj.y - camY) * scale
                    drawCircle(
                        color = proj.color.copy(alpha = 0.35f),
                        radius = 10f * scale,
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 5f * scale,
                        center = Offset(px, py)
                    )
                }

                // Draw Explosions & Orbital Beams
                state.explosions.forEach { fx ->
                    val progress = (fx.age / fx.maxAge).coerceIn(0f, 1f)
                    val ex = (fx.x - targetCamX) * scale
                    val ey = (fx.y - camY) * scale
                    fx.lightningBeamX?.let { lx ->
                        val bx = (lx - targetCamX) * scale
                        drawLine(
                            color = Color(0xFFBAE6FD).copy(alpha = 1f - progress),
                            start = Offset(bx, 0f),
                            end = Offset(bx, ey),
                            strokeWidth = (18f * (1f - progress) + 3f) * scale,
                            cap = StrokeCap.Round
                        )
                    }
                    drawCircle(
                        color = fx.color.copy(alpha = (1f - progress) * 0.7f),
                        radius = fx.radius * (0.4f + progress * 0.8f) * scale,
                        center = Offset(ex, ey)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = (1f - progress) * 0.9f),
                        radius = fx.radius * (0.25f + progress * 0.3f) * scale,
                        center = Offset(ex, ey)
                    )
                }

                // Draw Floating Damage / Status Text
                val textPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 20f * scale.coerceAtLeast(1f)
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                state.floatingTexts.forEach { ft ->
                    val alpha = (1f - ft.age / ft.maxAge).coerceIn(0f, 1f)
                    textPaint.color = ft.color.copy(alpha = alpha).toArgb()
                    drawContext.canvas.nativeCanvas.drawText(
                        ft.text,
                        (ft.x - targetCamX) * scale,
                        (ft.y - camY) * scale,
                        textPaint
                    )
                }

                // Drag Aiming Reticle Overlay
                val o = dragOrigin
                val c = dragCurrent
                if (o != null && c != null && options.aimingMode == AimingMode.DRAG) {
                    drawLine(
                        color = GbGoldAccent,
                        start = o,
                        end = c,
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = GbCyanPrimary.copy(alpha = 0.4f), radius = 26f, center = o, style = Stroke(3f))
                    drawCircle(color = GbGoldAccent, radius = 12f, center = c)
                }
            }
        }

        // 2. Top Tactical HUD Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActorMiniHudCard(
                    actor = state.player,
                    isActive = state.playerTurn,
                    accent = GbTeamRed,
                    modifier = Modifier.weight(1f)
                )

                // Wind + Weather + Timer Center Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GbNavyDeep.copy(alpha = 0.88f),
                    border = BorderStroke(1.5.dp, GbSteelBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val windMag = hypot(state.wind.x, state.wind.y).roundToInt()
                        val windAngle = atan2(state.wind.y.toDouble(), state.wind.x.toDouble()) * 180.0 / PI
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Canvas(modifier = Modifier.size(22.dp)) {
                                drawCircle(color = GbSlatePanel, radius = size.minDimension / 2f)
                                rotate(degrees = windAngle.toFloat()) {
                                    drawLine(
                                        color = GbCyanPrimary,
                                        start = Offset(4f, size.height / 2f),
                                        end = Offset(size.width - 4f, size.height / 2f),
                                        strokeWidth = 3.5f,
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        color = GbGoldAccent,
                                        start = Offset(size.width - 8f, size.height / 2f - 4f),
                                        end = Offset(size.width - 4f, size.height / 2f),
                                        strokeWidth = 3f
                                    )
                                    drawLine(
                                        color = GbGoldAccent,
                                        start = Offset(size.width - 8f, size.height / 2f + 4f),
                                        end = Offset(size.width - 4f, size.height / 2f),
                                        strokeWidth = 3f
                                    )
                                }
                            }
                            Text(
                                text = "$windMag",
                                style = MaterialTheme.typography.titleMedium,
                                color = GbCyanPrimary
                            )
                            Text(
                                text = "${state.turnSecondsLeft}s",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (state.turnSecondsLeft <= 7) GbCrimsonDanger else GbGoldAccent
                            )
                        }
                        Text(
                            text = state.weather.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = state.weather.badgeColor
                        )
                    }
                }

                ActorMiniHudCard(
                    actor = state.cpu,
                    isActive = !state.playerTurn,
                    accent = GbTeamBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            // Secondary HUD strip: Turn Order + Focus + Options + Pause
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GbNavyDeep.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, GbSteelBorder)
                ) {
                    val nextLabel = if (state.player.delay <= state.cpu.delay) "NEXT: YOU" else "NEXT: CPU"
                    Text(
                        text = state.statusBanner ?: "$nextLabel · DLY ${state.player.delay}/${state.cpu.delay}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GbTextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SmallHudIconButton(
                        icon = Icons.Filled.CenterFocusStrong,
                        desc = "Center camera",
                        tag = "battle_center_cam"
                    ) { manualPanOffset = 0f }
                    SmallHudIconButton(
                        icon = Icons.Filled.Settings,
                        desc = "Options",
                        tag = "battle_options_button"
                    ) { showOptionsDialog = true }
                    SmallHudIconButton(
                        icon = Icons.Filled.Pause,
                        desc = "Pause",
                        tag = "battle_pause_button"
                    ) { controller.togglePause() }
                }
            }
        }

        // 3. Bottom Control Deck
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            shape = RoundedCornerShape(14.dp),
            color = GbNavyDeep.copy(alpha = 0.90f),
            border = BorderStroke(1.5.dp, GbSteelBorder)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: Power Bar (with Last Power marker) + Fuel Gauge + Item Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Power Meter
                    Column(modifier = Modifier.weight(1.3f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "POWER ${(state.chargePower * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = GbGoldAccent
                            )
                            Text(
                                text = "FUEL ${state.player.moveFuel.roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = GbEmeraldHp
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(GbSlatePanel)
                                .border(1.dp, GbSteelBorder, RoundedCornerShape(7.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(state.chargePower.coerceIn(0f, 1f))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(GbCyanPrimary, GbGoldAccent, GbCrimsonDanger)
                                        )
                                    )
                            )
                            state.lastShotPower?.let { lastP ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(lastP.coerceIn(0.02f, 0.99f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .width(3.dp)
                                            .fillMaxHeight()
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                    }

                    // Active Turn Items Bar
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (state.weather == WeatherType.IGNORANCE) {
                            Text("LOCKED", style = MaterialTheme.typography.labelSmall, color = GbCrimsonDanger)
                        } else {
                            state.availableItems.forEachIndexed { idx, item ->
                                val bmp = remember(item.hdAsset) { spriteLoader.loadImageBitmap(item.hdAsset) }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GbSlatePanel)
                                        .border(1.dp, GbCyanPrimary.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                        .clickable(enabled = state.playerTurn && state.phase == MatchPhase.PLAYER_AIM) {
                                            controller.useItem(idx)
                                        }
                                        .testTag("use_item_$idx"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (bmp != null) {
                                        Image(bitmap = bmp, contentDescription = item.title, modifier = Modifier.size(28.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Row 2: Move + Angle + Shot Selector + Fire / Pass
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Move Left / Right
                    HudHoldButton(
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        desc = "Move Left",
                        tag = "move_left_button",
                        enabled = state.playerTurn && state.phase == MatchPhase.PLAYER_AIM
                    ) { controller.movePlayer(-1) }

                    HudHoldButton(
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        desc = "Move Right",
                        tag = "move_right_button",
                        enabled = state.playerTurn && state.phase == MatchPhase.PLAYER_AIM
                    ) { controller.movePlayer(1) }

                    // Angle Down / Up + Readout
                    HudHoldButton(
                        icon = Icons.Filled.KeyboardArrowDown,
                        desc = "Angle Down",
                        tag = "angle_down_button",
                        enabled = state.playerTurn && state.phase == MatchPhase.PLAYER_AIM
                    ) { controller.adjustAngle(-2) }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(38.dp)
                    ) {
                        Text(
                            text = "${state.player.aimAngleDeg}°",
                            style = MaterialTheme.typography.titleMedium,
                            color = GbCyanPrimary
                        )
                        Text("ANG", style = MaterialTheme.typography.labelSmall, color = GbTextSecondary, fontSize = 9.sp)
                    }

                    HudHoldButton(
                        icon = Icons.Filled.KeyboardArrowUp,
                        desc = "Angle Up",
                        tag = "angle_up_button",
                        enabled = state.playerTurn && state.phase == MatchPhase.PLAYER_AIM
                    ) { controller.adjustAngle(2) }

                    // T1 / T2 / SS Selector
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ShotType.entries.forEach { shot ->
                            val selected = state.selectedShot == shot
                            val locked = shot == ShotType.SS && state.player.ssCooldownTurns > 0
                            Surface(
                                modifier = Modifier
                                    .size(width = 36.dp, height = 44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = state.playerTurn && state.phase == MatchPhase.PLAYER_AIM && !locked) {
                                        controller.selectShotType(shot)
                                    }
                                    .testTag("shot_${shot.name}"),
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    locked -> GbNavySurface
                                    selected -> GbGoldAccent.copy(alpha = 0.25f)
                                    else -> GbSlatePanel
                                },
                                border = BorderStroke(
                                    1.5.dp,
                                    when {
                                        locked -> GbSteelBorder.copy(alpha = 0.3f)
                                        selected -> GbGoldAccent
                                        else -> GbSteelBorder
                                    }
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (locked) "${state.player.ssCooldownTurns}" else shot.label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = when {
                                            locked -> GbTextSecondary
                                            selected -> GbGoldAccent
                                            else -> GbTextPrimary
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Primary Fire (Slice hold-release OR Drag hint) + Pass
                    if (options.aimingMode == AimingMode.SLICE) {
                        val canFire = state.playerTurn && (state.phase == MatchPhase.PLAYER_AIM || state.phase == MatchPhase.CHARGING)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .pointerInput(canFire) {
                                    if (!canFire) return@pointerInput
                                    awaitPointerEventScope {
                                        while (true) {
                                            val down = awaitPointerEvent()
                                            if (down.changes.any { it.pressed }) {
                                                controller.startSliceCharge()
                                                while (true) {
                                                    val next = awaitPointerEvent()
                                                    if (next.changes.none { it.pressed }) {
                                                        controller.releaseSliceCharge()
                                                        break
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                .testTag("fire_slice_button"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (state.phase == MatchPhase.CHARGING) GbCrimsonDanger.copy(alpha = 0.35f)
                            else if (canFire) GbGoldAccent.copy(alpha = 0.25f)
                            else GbSlatePanel,
                            border = BorderStroke(
                                2.dp,
                                if (state.phase == MatchPhase.CHARGING) GbCrimsonDanger
                                else if (canFire) GbGoldAccent
                                else GbSteelBorder
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (state.phase == MatchPhase.CHARGING) "RELEASE!"
                                    else if (canFire) "HOLD FIRE"
                                    else "WAIT...",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (canFire) GbGoldAccent else GbTextSecondary
                                )
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = GbCyanPrimary.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, GbCyanPrimary)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (state.playerTurn) "DRAG SKY" else "CPU TURN",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GbCyanPrimary
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .height(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = state.playerTurn && state.phase == MatchPhase.PLAYER_AIM) {
                                controller.passTurn()
                            }
                            .testTag("pass_turn_button"),
                        shape = RoundedCornerShape(10.dp),
                        color = GbSlatePanel,
                        border = BorderStroke(1.dp, GbSteelBorder)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("PASS", style = MaterialTheme.typography.labelMedium, color = GbTextSecondary)
                        }
                    }
                }
            }
        }
    }

    // Classic Options Popup
    if (showOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showOptionsDialog = false },
            title = { Text("Battle Options") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sound Effects (SFX)")
                        Switch(checked = options.sfxEnabled, onCheckedChange = { onToggleSfx() })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Background Stars")
                        Switch(checked = options.showBackgroundEffects, onCheckedChange = { onToggleBgEffects() })
                    }
                    Column {
                        Text("Aiming Mode: ${options.aimingMode.label}", color = GbCyanPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AimingMode.entries.forEach { mode ->
                                GunboundActionButton(
                                    text = mode.label,
                                    onClick = { onSetAimingMode(mode) },
                                    accentColor = if (options.aimingMode == mode) GbGoldAccent else GbTextSecondary,
                                    minHeight = 40.dp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    Column {
                        Text("Camera Pan Speed: ${String.format("%.1f", options.scrollSpeed)}x")
                        Slider(
                            value = options.scrollSpeed,
                            onValueChange = onSetScrollSpeed,
                            valueRange = 0.5f..2.0f
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOptionsDialog = false }) { Text("Close") }
            }
        )
    }

    // Pause Dialog
    if (state.isPaused && state.result == null) {
        AlertDialog(
            onDismissRequest = { controller.togglePause() },
            title = { Text("Match Paused") },
            text = { Text("Charge is cancelled while paused. Resume battle or return to the 1v1 Room.") },
            confirmButton = {
                TextButton(onClick = { controller.togglePause() }) { Text("Resume") }
            },
            dismissButton = {
                TextButton(onClick = onExitToRoom) { Text("Leave Match", color = GbCrimsonDanger) }
            }
        )
    }

    // Classic WIN / LOSE Result Window
    state.result?.let { res ->
        MatchResultModal(
            summary = res,
            playerName = state.player.name,
            cpuName = state.cpu.name,
            onOk = onExitToRoom
        )
    }
}

private fun DrawScope.drawMobileActor(
    actor: ActorState,
    isActiveTurn: Boolean,
    camX: Float,
    camY: Float,
    scale: Float
) {
    val sx = (actor.x - camX) * scale
    val sy = (actor.y - camY) * scale

    // Active turn indicator ring
    if (isActiveTurn) {
        drawCircle(
            color = GbGoldAccent.copy(alpha = 0.35f),
            radius = 30f * scale,
            center = Offset(sx, sy - 14f * scale),
            style = Stroke(width = 2.5f * scale)
        )
    }

    // Active item aura
    if (actor.activeTurnItem != null) {
        drawCircle(
            color = GbCyanPrimary.copy(alpha = 0.25f),
            radius = 26f * scale,
            center = Offset(sx, sy - 14f * scale)
        )
    }

    // Aiming angle guide wedge for active player
    if (actor.isPlayer && isActiveTurn) {
        val rad = actor.effectiveWorldAngleDeg * (PI / 180.0)
        val guideLen = 58f * scale
        drawLine(
            color = GbGoldAccent.copy(alpha = 0.85f),
            start = Offset(sx, sy - 16f * scale),
            end = Offset(sx + (cos(rad) * guideLen).toFloat(), sy - 16f * scale + (sin(rad) * guideLen).toFloat()),
            strokeWidth = 2.5f * scale,
            cap = StrokeCap.Round
        )
    }

    // Mobile body & barrel rotated by slopeDeg
    rotate(degrees = actor.slopeDeg, pivot = Offset(sx, sy)) {
        val dir = if (actor.facingRight) 1f else -1f
        // Treads / hover glow
        if (actor.mobile.isHovering) {
            drawOval(
                color = actor.mobile.accentColor.copy(alpha = 0.45f),
                topLeft = Offset(sx - 18f * scale, sy - 6f * scale),
                size = Size(36f * scale, 8f * scale)
            )
        } else {
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(sx - 18f * scale, sy - 10f * scale),
                size = Size(36f * scale, 10f * scale),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f * scale, 5f * scale)
            )
            // Wheels
            for (wx in listOf(-12f, 0f, 12f)) {
                drawCircle(
                    color = Color(0xFF94A3B8),
                    radius = 3.5f * scale,
                    center = Offset(sx + wx * scale, sy - 5f * scale)
                )
            }
        }

        // Armored Hull
        drawRoundRect(
            color = actor.mobile.bodyColor,
            topLeft = Offset(sx - 16f * scale, sy - 22f * scale),
            size = Size(32f * scale, 14f * scale),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale, 6f * scale)
        )
        // Turret Dome
        drawCircle(
            color = actor.mobile.accentColor,
            radius = 9f * scale,
            center = Offset(sx + dir * 2f * scale, sy - 23f * scale)
        )
    }

    // Cannon Barrel
    val barrelRad = actor.effectiveWorldAngleDeg * (PI / 180.0)
    val bDir = if (actor.mobile.firesFromTail) -1f else 1f
    val bx0 = sx
    val by0 = sy - 20f * scale
    val bx1 = bx0 + (cos(barrelRad) * 22f * scale * bDir).toFloat()
    val by1 = by0 + (sin(barrelRad) * 22f * scale).toFloat()
    drawLine(
        color = Color(0xFFE2E8F0),
        start = Offset(bx0, by0),
        end = Offset(bx1, by1),
        strokeWidth = 5f * scale,
        cap = StrokeCap.Round
    )

    // Shield dome if shield > 0
    if (actor.shield > 0) {
        drawCircle(
            color = GbShieldBlue.copy(alpha = 0.25f),
            radius = 24f * scale,
            center = Offset(sx, sy - 14f * scale),
            style = Stroke(width = 2f * scale)
        )
    }

    // HP & Shield Bar underneath mobile
    val barW = 46f * scale
    val barX = sx - barW / 2f
    val barY = sy + 8f * scale
    drawRect(color = Color.Black.copy(alpha = 0.7f), topLeft = Offset(barX - 1f, barY - 1f), size = Size(barW + 2f, 7f * scale))
    val hpRatio = (actor.hp.toFloat() / actor.maxHp.coerceAtLeast(1)).coerceIn(0f, 1f)
    drawRect(
        color = if (actor.isPlayer) GbEmeraldHp else GbCrimsonDanger,
        topLeft = Offset(barX, barY),
        size = Size(barW * hpRatio, 5f * scale)
    )
    if (actor.maxShield > 0) {
        val shRatio = (actor.shield.toFloat() / actor.maxShield.coerceAtLeast(1)).coerceIn(0f, 1f)
        drawRect(
            color = GbShieldBlue,
            topLeft = Offset(barX, barY + 6f * scale),
            size = Size(barW * shRatio, 3f * scale)
        )
    }
}

@Composable
private fun ActorMiniHudCard(
    actor: ActorState,
    isActive: Boolean,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = GbNavyDeep.copy(alpha = 0.88f),
        border = BorderStroke(if (isActive) 2.dp else 1.dp, if (isActive) GbGoldAccent else accent.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = actor.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    maxLines = 1
                )
                Text(
                    text = actor.mobile.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = GbTextSecondary
                )
            }
            Text(
                text = "HP ${actor.hp}/${actor.maxHp}" + if (actor.maxShield > 0) " · SHD ${actor.shield}" else "",
                style = MaterialTheme.typography.labelSmall,
                color = GbTextPrimary
            )
        }
    }
}

@Composable
private fun SmallHudIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = CircleShape,
        color = GbNavyDeep.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, GbSteelBorder)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = desc, tint = GbCyanPrimary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun HudHoldButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    tag: String,
    enabled: Boolean,
    onStep: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(width = 40.dp, height = 44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onStep)
            .testTag(tag),
        shape = RoundedCornerShape(8.dp),
        color = if (enabled) GbSlatePanel else GbNavySurface,
        border = BorderStroke(1.dp, if (enabled) GbCyanPrimary.copy(alpha = 0.7f) else GbSteelBorder.copy(alpha = 0.4f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = desc,
                tint = if (enabled) GbTextPrimary else GbTextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun MatchResultModal(
    summary: MatchResultSummary,
    playerName: String,
    cpuName: String,
    onOk: () -> Unit
) {
    val accent = if (summary.playerWon) GbGoldAccent else GbCrimsonDanger
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = if (summary.playerWon) "WIN · VICTORY!" else "LOSE · DEFEAT",
                style = MaterialTheme.typography.headlineMedium,
                color = accent
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GbNavyDeep,
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(playerName, color = GbTeamRed, fontWeight = FontWeight.Bold)
                            Text(
                                text = "+${summary.xpEarned} P   +${summary.goldEarned} G",
                                color = GbGoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(cpuName, color = GbTeamBlue)
                            Text("0 P   0 G", color = GbTextSecondary)
                        }
                    }
                }
                Text(
                    text = "Damage Dealt: ${summary.damageDealt} · Turns: ${summary.turnsElapsed}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GbTextSecondary
                )
            }
        },
        confirmButton = {
            GunboundActionButton(
                text = "OK",
                onClick = onOk,
                accentColor = accent,
                minHeight = 44.dp,
                modifier = Modifier
                    .width(120.dp)
                    .testTag("result_ok_button")
            )
        }
    )
}
