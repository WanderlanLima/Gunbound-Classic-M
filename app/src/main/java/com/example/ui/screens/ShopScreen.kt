package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.AvatarItem
import com.example.data.AvatarSlot
import com.example.data.GameCatalog
import com.example.data.PlayerAttributes
import com.example.data.PlayerProfile
import com.example.ui.components.GunboundActionButton
import com.example.ui.components.GunboundBackdrop
import com.example.ui.components.GunboundPanel
import com.example.ui.components.PlayerProfileHeader
import com.example.ui.theme.GbCrimsonDanger
import com.example.ui.theme.GbCyanPrimary
import com.example.ui.theme.GbEmeraldHp
import com.example.ui.theme.GbGoldAccent
import com.example.ui.theme.GbNavyDeep
import com.example.ui.theme.GbSlatePanel
import com.example.ui.theme.GbSteelBorder
import com.example.ui.theme.GbTextPrimary
import com.example.ui.theme.GbTextSecondary

@Composable
fun ShopScreen(
    profile: PlayerProfile,
    previewAvatarId: String?,
    feedback: String?,
    spriteLoader: AssetSpriteLoader,
    onBack: () -> Unit,
    onBuy: (AvatarItem, Boolean) -> Unit,
    onToggleEquip: (AvatarItem) -> Unit,
    onSell: (AvatarItem) -> Unit,
    onPreview: (AvatarItem?) -> Unit,
    onSaveAttributes: (PlayerAttributes) -> Unit,
    onClearFeedback: () -> Unit
) {
    BackHandler(onBack = onBack)

    var activeSlot by remember { mutableStateOf<AvatarSlot?>(null) }
    var showAttrDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedback) {
        if (feedback != null) {
            snackbarHostState.showSnackbar(feedback)
            onClearFeedback()
        }
    }

    val filteredAvatars = remember(activeSlot) {
        if (activeSlot == null) GameCatalog.avatars else GameCatalog.avatars.filter { it.slot == activeSlot }
    }

    val equippedList = remember(profile.equippedHead, profile.equippedBody, profile.equippedGlasses, profile.equippedFlag) {
        listOfNotNull(profile.equippedHead, profile.equippedBody, profile.equippedGlasses, profile.equippedFlag)
            .mapNotNull { id -> GameCatalog.avatars.firstOrNull { it.id == id } }
    }
    val previewItem = remember(previewAvatarId) {
        GameCatalog.avatars.firstOrNull { it.id == previewAvatarId }
    }

    GunboundBackdrop {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 1040.dp)
                    .align(Alignment.TopCenter),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("shop_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GbCyanPrimary)
                        }
                        Text(
                            text = stringResource(R.string.shop_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = GbGoldAccent
                        )
                    }

                    GunboundActionButton(
                        text = "${stringResource(R.string.shop_attributes)} (${profile.availableAttributePoints})",
                        onClick = { showAttrDialog = true },
                        accentColor = GbCyanPrimary,
                        minHeight = 42.dp,
                        leadingIcon = Icons.Filled.Tune,
                        modifier = Modifier.testTag("attributes_button")
                    )
                }

                PlayerProfileHeader(profile = profile, loader = spriteLoader)

                // Equipped & Stat Summary
                GunboundPanel(title = if (previewItem != null) "Previewing: ${previewItem.name}" else "Equipped Loadout") {
                    val totalAtk = profile.attributes.attack + equippedList.sumOf { it.attackBonus }
                    val totalDef = profile.attributes.defense + equippedList.sumOf { it.defenseBonus }
                    val totalHp = profile.attributes.health + equippedList.sumOf { it.healthBonus }
                    val totalShd = profile.attributes.shieldRegen + equippedList.sumOf { it.shieldBonus }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AvatarSlot.entries.forEach { slot ->
                            val itemId = when (slot) {
                                AvatarSlot.HEAD -> if (previewItem?.slot == AvatarSlot.HEAD) previewItem.id else profile.equippedHead
                                AvatarSlot.BODY -> if (previewItem?.slot == AvatarSlot.BODY) previewItem.id else profile.equippedBody
                                AvatarSlot.GLASSES -> if (previewItem?.slot == AvatarSlot.GLASSES) previewItem.id else profile.equippedGlasses
                                AvatarSlot.FLAG -> if (previewItem?.slot == AvatarSlot.FLAG) previewItem.id else profile.equippedFlag
                            }
                            val item = GameCatalog.avatars.firstOrNull { it.id == itemId }
                            val bmp = remember(item?.id) { item?.let { spriteLoader.loadAvatarSprite(it) } }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = GbNavyDeep,
                                border = BorderStroke(1.dp, if (item != null) GbGoldAccent.copy(alpha = 0.7f) else GbSteelBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    if (bmp != null) {
                                        Image(
                                            bitmap = bmp,
                                            contentDescription = item?.name ?: slot.label,
                                            modifier = Modifier.size(32.dp),
                                            contentScale = ContentScale.Fit,
                                            filterQuality = FilterQuality.Medium
                                        )
                                    } else {
                                        Text(slot.label, style = MaterialTheme.typography.labelSmall, color = GbTextSecondary)
                                    }
                                    Text(
                                        text = item?.name ?: "Empty",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (item != null) GbGoldAccent else GbTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Active Bonuses -> ATK +$totalAtk · DEF +$totalDef · HP +$totalHp · SHD +$totalShd",
                        style = MaterialTheme.typography.labelSmall,
                        color = GbEmeraldHp
                    )
                }

                // Category Filter Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChipButton("All", activeSlot == null) { activeSlot = null }
                    AvatarSlot.entries.forEach { slot ->
                        FilterChipButton(slot.label, activeSlot == slot) { activeSlot = slot }
                    }
                }

                // Catalog Grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 165.dp),
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredAvatars, key = { it.id }) { item ->
                        val owned = item.id in profile.ownedAvatars
                        val equipped = item.id == profile.equippedHead ||
                            item.id == profile.equippedBody ||
                            item.id == profile.equippedGlasses ||
                            item.id == profile.equippedFlag
                        val isPreview = previewAvatarId == item.id
                        val bmp = remember(item.id) { spriteLoader.loadAvatarSprite(item) }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GbSlatePanel,
                            border = BorderStroke(
                                1.5.dp,
                                when {
                                    equipped -> GbEmeraldHp
                                    isPreview -> GbCyanPrimary
                                    else -> GbSteelBorder
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.slot.label.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GbCyanPrimary
                                    )
                                    if (equipped) {
                                        Text(
                                            text = "EQUIPPED",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GbEmeraldHp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else if (owned) {
                                        Text(
                                            text = "OWNED",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GbGoldAccent
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(84.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GbNavyDeep),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (bmp != null) {
                                        Image(
                                            bitmap = bmp,
                                            contentDescription = item.name,
                                            modifier = Modifier.size(72.dp),
                                            contentScale = ContentScale.Fit,
                                            filterQuality = FilterQuality.Medium
                                        )
                                    }
                                }

                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GbTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = buildStatLine(item),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GbTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (owned) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        GunboundActionButton(
                                            text = if (equipped) "Off" else "Equip",
                                            onClick = { onToggleEquip(item) },
                                            accentColor = if (equipped) GbTextSecondary else GbEmeraldHp,
                                            minHeight = 40.dp,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("equip_${item.id}")
                                        )
                                        GunboundActionButton(
                                            text = "Sell",
                                            onClick = { onSell(item) },
                                            accentColor = GbCrimsonDanger,
                                            minHeight = 40.dp,
                                            modifier = Modifier
                                                .weight(0.8f)
                                                .testTag("sell_${item.id}")
                                        )
                                    }
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        GunboundActionButton(
                                            text = "${item.goldPrice}G",
                                            onClick = { onBuy(item, false) },
                                            accentColor = GbGoldAccent,
                                            minHeight = 40.dp,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("buy_gold_${item.id}")
                                        )
                                        if (item.cashPrice > 0) {
                                            GunboundActionButton(
                                                text = "${item.cashPrice}C",
                                                onClick = { onBuy(item, true) },
                                                accentColor = GbCyanPrimary,
                                                minHeight = 40.dp,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("buy_cash_${item.id}")
                                            )
                                        } else {
                                            GunboundActionButton(
                                                text = if (isPreview) "Stop" else "Try",
                                                onClick = { onPreview(item) },
                                                accentColor = GbCyanPrimary,
                                                minHeight = 40.dp,
                                                modifier = Modifier.weight(0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (showAttrDialog) {
        AttributesModal(
            initial = profile.attributes,
            totalBudget = profile.totalAttributePoints,
            onSave = {
                onSaveAttributes(it)
                showAttrDialog = false
            },
            onDismiss = { showAttrDialog = false }
        )
    }
}

@Composable
private fun FilterChipButton(label: String, active: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (active) GbCyanPrimary.copy(alpha = 0.2f) else GbSlatePanel,
        border = BorderStroke(1.dp, if (active) GbCyanPrimary else GbSteelBorder)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (active) GbCyanPrimary else GbTextSecondary
            )
        }
    }
}

private fun buildStatLine(item: AvatarItem): String {
    val parts = mutableListOf<String>()
    if (item.attackBonus > 0) parts += "ATK+${item.attackBonus}"
    if (item.defenseBonus > 0) parts += "DEF+${item.defenseBonus}"
    if (item.healthBonus > 0) parts += "HP+${item.healthBonus}"
    if (item.delayBonus > 0) parts += "DLY+${item.delayBonus}"
    if (item.digBonus > 0) parts += "DIG+${item.digBonus}"
    if (item.shieldBonus > 0) parts += "SHD+${item.shieldBonus}"
    if (item.popularityBonus > 0) parts += "POP+${item.popularityBonus}"
    return if (parts.isEmpty()) "Cosmetic Gear" else parts.joinToString(" · ")
}

@Composable
private fun AttributesModal(
    initial: PlayerAttributes,
    totalBudget: Int,
    onSave: (PlayerAttributes) -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember { mutableStateOf(initial) }
    val remaining = (totalBudget - draft.spentPoints).coerceAtLeast(0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Commander Attributes ($remaining pts left)") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttrRow("Attack", draft.attack, remaining) { d -> draft = draft.copy(attack = (draft.attack + d).coerceIn(0, 50)) }
                AttrRow("Defense", draft.defense, remaining) { d -> draft = draft.copy(defense = (draft.defense + d).coerceIn(0, 50)) }
                AttrRow("Health", draft.health, remaining) { d -> draft = draft.copy(health = (draft.health + d).coerceIn(0, 50)) }
                AttrRow("Item Delay", draft.itemDelay, remaining) { d -> draft = draft.copy(itemDelay = (draft.itemDelay + d).coerceIn(0, 50)) }
                AttrRow("Dig / Crater", draft.dig, remaining) { d -> draft = draft.copy(dig = (draft.dig + d).coerceIn(0, 50)) }
                AttrRow("Shield Regen", draft.shieldRegen, remaining) { d -> draft = draft.copy(shieldRegen = (draft.shieldRegen + d).coerceIn(0, 50)) }
                AttrRow("Popularity", draft.popularity, remaining) { d -> draft = draft.copy(popularity = (draft.popularity + d).coerceIn(0, 50)) }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(draft) }) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AttrRow(label: String, value: Int, remaining: Int, onDelta: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$label: $value / 50", style = MaterialTheme.typography.bodyMedium, color = GbTextPrimary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (value > 0) onDelta(-1) }, enabled = value > 0) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease $label")
            }
            IconButton(onClick = { if (remaining > 0 && value < 50) onDelta(1) }, enabled = remaining > 0 && value < 50) {
                Icon(Icons.Filled.Add, contentDescription = "Increase $label")
            }
        }
    }
}
