package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AssetSpriteLoader
import com.example.data.BattleItemId
import com.example.data.CpuDifficulty
import com.example.data.GameCatalog
import com.example.data.PlayerProfile
import com.example.ui.components.ClassicSpriteFrame
import com.example.ui.components.GunboundActionButton
import com.example.ui.components.GunboundBackdrop
import com.example.ui.components.GunboundPanel
import com.example.ui.theme.GbCrimsonDanger
import com.example.ui.theme.GbCyanPrimary
import com.example.ui.theme.GbEmeraldHp
import com.example.ui.theme.GbGoldAccent
import com.example.ui.theme.GbNavyDeep
import com.example.ui.theme.GbShieldBlue
import com.example.ui.theme.GbSlatePanel
import com.example.ui.theme.GbSteelBorder
import com.example.ui.theme.GbTeamBlue
import com.example.ui.theme.GbTeamRed
import com.example.ui.theme.GbTextPrimary
import com.example.ui.theme.GbTextSecondary

@Composable
fun RoomScreen(
    profile: PlayerProfile,
    spriteLoader: AssetSpriteLoader,
    onBack: () -> Unit,
    onSelectMobile: (Int) -> Unit,
    onSelectRandomMobile: () -> Unit,
    onCycleMap: (Int) -> Unit,
    onSelectMap: (Int) -> Unit,
    onToggleTeam: () -> Unit,
    onSelectDifficulty: (CpuDifficulty) -> Unit,
    onUpdateItems: (List<BattleItemId>) -> Unit,
    onStartMatch: () -> Unit
) {
    BackHandler(onBack = onBack)

    var showMapPicker by remember { mutableStateOf(false) }
    var showItemPicker by remember { mutableStateOf(false) }

    val playerMobile = remember(profile.selectedMobileId) {
        GameCatalog.mobileById(profile.selectedMobileId)
    }
    val cpuMobile = remember(profile.selectedMobileId) {
        GameCatalog.opponentFor(profile.selectedMobileId)
    }
    val mapSpec = remember(profile.selectedMapIndex) {
        GameCatalog.mapByIndex(profile.selectedMapIndex)
    }

    GunboundBackdrop {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(12.dp)
        ) {
            val isWide = maxWidth >= 700.dp

            Column(
                modifier = Modifier
                    .widthIn(max = 1040.dp)
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("room_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Lobby",
                                tint = GbCyanPrimary
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.room_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = GbGoldAccent
                            )
                            Text(
                                text = "Mode: SOLO 1v1 · Training",
                                style = MaterialTheme.typography.labelSmall,
                                color = GbTextSecondary
                            )
                        }
                    }

                    GunboundActionButton(
                        text = if (profile.playerTeamA) "Team A" else "Team B",
                        onClick = onToggleTeam,
                        accentColor = if (profile.playerTeamA) GbTeamRed else GbTeamBlue,
                        leadingIcon = Icons.Filled.SwapHoriz,
                        modifier = Modifier.testTag("switch_team_button")
                    )
                }

                // Player vs CPU Matchup Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CombatantSlotCard(
                        title = profile.nickname,
                        subtitle = "MASTER · ${GameCatalog.rankName(profile.rankIndex)}",
                        teamLabel = if (profile.playerTeamA) "TEAM A" else "TEAM B",
                        teamColor = if (profile.playerTeamA) GbTeamRed else GbTeamBlue,
                        mobile = playerMobile,
                        rankIndex = profile.rankIndex,
                        spriteLoader = spriteLoader,
                        modifier = Modifier.weight(1f)
                    )
                    CombatantSlotCard(
                        title = "CPU Bot",
                        subtitle = "${profile.cpuDifficulty.label} AI",
                        teamLabel = if (profile.playerTeamA) "TEAM B" else "TEAM A",
                        teamColor = if (profile.playerTeamA) GbTeamBlue else GbTeamRed,
                        mobile = cpuMobile,
                        rankIndex = 0,
                        spriteLoader = spriteLoader,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MobileSelectorPanel(
                            selectedMobileId = profile.selectedMobileId,
                            spriteLoader = spriteLoader,
                            onSelectMobile = onSelectMobile,
                            onSelectRandom = onSelectRandomMobile,
                            modifier = Modifier.weight(1.35f)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MapAndDifficultyPanel(
                                mapSpec = mapSpec,
                                difficulty = profile.cpuDifficulty,
                                spriteLoader = spriteLoader,
                                onCycleMap = onCycleMap,
                                onOpenMapGrid = { showMapPicker = true },
                                onSelectDifficulty = onSelectDifficulty
                            )
                            BattleItemsBarPanel(
                                equipped = profile.equippedItems,
                                spriteLoader = spriteLoader,
                                onEditItems = { showItemPicker = true }
                            )
                        }
                    }
                } else {
                    MobileSelectorPanel(
                        selectedMobileId = profile.selectedMobileId,
                        spriteLoader = spriteLoader,
                        onSelectMobile = onSelectMobile,
                        onSelectRandom = onSelectRandomMobile
                    )
                    MapAndDifficultyPanel(
                        mapSpec = mapSpec,
                        difficulty = profile.cpuDifficulty,
                        spriteLoader = spriteLoader,
                        onCycleMap = onCycleMap,
                        onOpenMapGrid = { showMapPicker = true },
                        onSelectDifficulty = onSelectDifficulty
                    )
                    BattleItemsBarPanel(
                        equipped = profile.equippedItems,
                        spriteLoader = spriteLoader,
                        onEditItems = { showItemPicker = true }
                    )
                }

                // Start Match CTA
                GunboundActionButton(
                    text = "${stringResource(R.string.room_start_game)} 1v1 BATTLE",
                    onClick = onStartMatch,
                    accentColor = GbEmeraldHp,
                    minHeight = 54.dp,
                    leadingIcon = Icons.Filled.PlayArrow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start_game_button")
                )
            }
        }
    }

    if (showMapPicker) {
        MapGridDialog(
            selectedIndex = profile.selectedMapIndex,
            spriteLoader = spriteLoader,
            onSelect = {
                onSelectMap(it)
                showMapPicker = false
            },
            onDismiss = { showMapPicker = false }
        )
    }

    if (showItemPicker) {
        ItemLoadoutDialog(
            initialItems = profile.equippedItems,
            spriteLoader = spriteLoader,
            onSave = {
                onUpdateItems(it)
                showItemPicker = false
            },
            onDismiss = { showItemPicker = false }
        )
    }
}

@Composable
private fun CombatantSlotCard(
    title: String,
    subtitle: String,
    teamLabel: String,
    teamColor: Color,
    mobile: com.example.data.MobileSpec,
    rankIndex: Int,
    spriteLoader: AssetSpriteLoader,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = GbSlatePanel,
        border = BorderStroke(1.5.dp, teamColor.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    ClassicSpriteFrame(
                        loader = spriteLoader,
                        assetPath = "classic/ranks/rank1.png",
                        frameIndex = rankIndex,
                        totalFrames = 25,
                        contentDescription = subtitle,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = GbTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .background(teamColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(teamLabel, style = MaterialTheme.typography.labelSmall, color = teamColor, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = GbTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClassicSpriteFrame(
                    loader = spriteLoader,
                    assetPath = mobile.buttonAsset,
                    frameIndex = 4,
                    totalFrames = 5,
                    contentDescription = mobile.name,
                    modifier = Modifier
                        .size(width = 62.dp, height = 46.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, mobile.bodyColor, RoundedCornerShape(6.dp))
                )
                Column {
                    Text(
                        text = "${mobile.name} (#${mobile.id})",
                        style = MaterialTheme.typography.titleMedium,
                        color = GbGoldAccent,
                        maxLines = 1
                    )
                    Text(
                        text = "HP ${mobile.baseHp}" + if (mobile.baseShield > 0) " + ${mobile.baseShield} SHD" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (mobile.baseShield > 0) GbShieldBlue else GbEmeraldHp
                    )
                    Text(
                        text = "${mobile.category.label} · ${mobile.minAngle}°–${mobile.maxAngle}°",
                        style = MaterialTheme.typography.labelSmall,
                        color = GbTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileSelectorPanel(
    selectedMobileId: Int,
    spriteLoader: AssetSpriteLoader,
    onSelectMobile: (Int) -> Unit,
    onSelectRandom: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selected = GameCatalog.mobileById(selectedMobileId)
    GunboundPanel(title = "Select Mobile (${selected.name})", modifier = modifier) {
        // 4 columns x 4 rows + Random button
        val rows = GameCatalog.mobiles.chunked(4)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            rows.forEach { rowMobiles ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowMobiles.forEach { mob ->
                        val active = mob.id == selectedMobileId
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(62.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectMobile(mob.id) }
                                .testTag("mobile_btn_${mob.id}"),
                            shape = RoundedCornerShape(8.dp),
                            color = if (active) mob.bodyColor.copy(alpha = 0.25f) else GbNavyDeep,
                            border = BorderStroke(
                                if (active) 2.dp else 1.dp,
                                if (active) GbGoldAccent else GbSteelBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                ClassicSpriteFrame(
                                    loader = spriteLoader,
                                    assetPath = mob.buttonAsset,
                                    frameIndex = if (active) 4 else 0,
                                    totalFrames = 5,
                                    contentDescription = mob.name,
                                    modifier = Modifier.size(width = 48.dp, height = 34.dp)
                                )
                                Text(
                                    text = mob.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) GbGoldAccent else GbTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${selected.title} · S1: ${selected.t1Label} · S2: ${selected.t2Label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GbTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "SS: ${selected.ssLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GbCyanPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(8.dp))
                GunboundActionButton(
                    text = "Random",
                    onClick = onSelectRandom,
                    accentColor = GbCyanPrimary,
                    minHeight = 40.dp,
                    leadingIcon = Icons.Filled.Casino,
                    modifier = Modifier.testTag("mobile_random_button")
                )
            }
        }
    }
}

@Composable
private fun MapAndDifficultyPanel(
    mapSpec: com.example.data.MapSideSpec,
    difficulty: CpuDifficulty,
    spriteLoader: AssetSpriteLoader,
    onCycleMap: (Int) -> Unit,
    onOpenMapGrid: () -> Unit,
    onSelectDifficulty: (CpuDifficulty) -> Unit
) {
    GunboundPanel(title = "Arena & CPU") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { onCycleMap(-1) },
                modifier = Modifier.testTag("map_prev_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Map", tint = GbCyanPrimary)
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenMapGrid)
                    .testTag("map_preview_card"),
                shape = RoundedCornerShape(8.dp),
                color = GbNavyDeep,
                border = BorderStroke(1.dp, GbSteelBorder)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ClassicSpriteFrame(
                        loader = spriteLoader,
                        assetPath = "classic/room/details/maps.png",
                        frameIndex = mapSpec.index,
                        totalFrames = 22,
                        contentDescription = mapSpec.displayName,
                        modifier = Modifier
                            .size(width = 88.dp, height = 54.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mapSpec.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = GbGoldAccent,
                            maxLines = 1
                        )
                        Text(
                            text = mapSpec.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = GbTextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(Icons.Filled.Map, contentDescription = "All 22 maps", tint = GbCyanPrimary, modifier = Modifier.size(18.dp))
                }
            }
            IconButton(
                onClick = { onCycleMap(1) },
                modifier = Modifier.testTag("map_next_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Map", tint = GbCyanPrimary)
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("CPU:", style = MaterialTheme.typography.labelMedium, color = GbTextSecondary)
            CpuDifficulty.entries.forEach { diff ->
                val active = diff == difficulty
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectDifficulty(diff) }
                        .testTag("diff_${diff.name}"),
                    shape = RoundedCornerShape(8.dp),
                    color = if (active) GbGoldAccent.copy(alpha = 0.22f) else GbNavyDeep,
                    border = BorderStroke(1.dp, if (active) GbGoldAccent else GbSteelBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = diff.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (active) GbGoldAccent else GbTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BattleItemsBarPanel(
    equipped: List<BattleItemId>,
    spriteLoader: AssetSpriteLoader,
    onEditItems: () -> Unit
) {
    val usedSlots = equipped.sumOf { it.slots }
    GunboundPanel(title = "Battle Items ($usedSlots / 6 Slots)") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (equipped.isEmpty()) {
                    Text(
                        text = "No battle items equipped",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GbTextSecondary
                    )
                } else {
                    equipped.forEach { item ->
                        val bmp = remember(item.hdAsset) { spriteLoader.loadImageBitmap(item.hdAsset) }
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GbNavyDeep)
                                .border(1.dp, GbCyanPrimary.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(34.dp),
                                    contentScale = ContentScale.Fit,
                                    filterQuality = FilterQuality.Medium
                                )
                            }
                        }
                    }
                }
            }
            GunboundActionButton(
                text = "Items",
                onClick = onEditItems,
                accentColor = GbCyanPrimary,
                minHeight = 42.dp,
                leadingIcon = Icons.Filled.Backpack,
                modifier = Modifier.testTag("select_items_button")
            )
        }
    }
}

@Composable
private fun MapGridDialog(
    selectedIndex: Int,
    spriteLoader: AssetSpriteLoader,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Classic Map Side (22)") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(GameCatalog.mapSides) { map ->
                    val active = map.index == selectedIndex
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(map.index) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (active) GbCyanPrimary.copy(alpha = 0.2f) else GbNavyDeep,
                        border = BorderStroke(1.5.dp, if (active) GbGoldAccent else GbSteelBorder)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            ClassicSpriteFrame(
                                loader = spriteLoader,
                                assetPath = "classic/room/details/maps.png",
                                frameIndex = map.index,
                                totalFrames = 22,
                                contentDescription = map.displayName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = map.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) GbGoldAccent else GbTextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun ItemLoadoutDialog(
    initialItems: List<BattleItemId>,
    spriteLoader: AssetSpriteLoader,
    onSave: (List<BattleItemId>) -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember { mutableStateOf(initialItems) }
    val slotsUsed = draft.sumOf { it.slots }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Battle Items ($slotsUsed / 6 Slots)") },
        text = {
            Column(
                modifier = Modifier
                    .height(380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Equipped bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GbNavyDeep, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (draft.isEmpty()) {
                        Text("Tap items below to equip (max 6 slots)", style = MaterialTheme.typography.labelSmall, color = GbTextSecondary)
                    } else {
                        draft.forEachIndexed { idx, item ->
                            val bmp = remember(item.hdAsset) { spriteLoader.loadImageBitmap(item.hdAsset) }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GbSlatePanel)
                                    .border(1.dp, GbGoldAccent, RoundedCornerShape(6.dp))
                                    .clickable {
                                        draft = draft.toMutableList().apply { removeAt(idx) }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (bmp != null) {
                                    Image(bitmap = bmp, contentDescription = "Remove ${item.title}", modifier = Modifier.size(30.dp))
                                }
                            }
                        }
                    }
                }

                // Catalog of 11 items
                BattleItemId.entries.forEach { item ->
                    val canAdd = slotsUsed + item.slots <= 6
                    val bmp = remember(item.hdAsset) { spriteLoader.loadImageBitmap(item.hdAsset) }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = canAdd) { draft = draft + item },
                        shape = RoundedCornerShape(8.dp),
                        color = if (canAdd) GbSlatePanel else GbNavyDeep.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, if (canAdd) GbSteelBorder else GbSteelBorder.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (bmp != null) {
                                Image(bitmap = bmp, contentDescription = item.title, modifier = Modifier.size(36.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.title} (${item.slots} slot${if (item.slots > 1) "s" else ""})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (canAdd) GbGoldAccent else GbTextSecondary
                                )
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GbTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(draft) }) { Text("Save Loadout") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
