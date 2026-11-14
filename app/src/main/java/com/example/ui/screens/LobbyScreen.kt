package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AssetSpriteLoader
import com.example.data.GameCatalog
import com.example.data.PlayerProfile
import com.example.ui.components.ClassicSpriteFrame
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
import com.example.ui.theme.GbTextMuted
import com.example.ui.theme.GbTextPrimary
import com.example.ui.theme.GbTextSecondary

@Composable
fun LobbyScreen(
    profile: PlayerProfile,
    selectedChannel: Int,
    spriteLoader: AssetSpriteLoader,
    onSelectChannel: (Int) -> Unit,
    onEnterRoom: () -> Unit,
    onQuickStartMatch: () -> Unit,
    onOpenShop: () -> Unit,
    onUpdateNickname: (String) -> Unit,
    onResetProfile: () -> Unit
) {
    var showNicknameDialog by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    val selectedMobile = remember(profile.selectedMobileId) {
        GameCatalog.mobileById(profile.selectedMobileId)
    }
    val selectedMap = remember(profile.selectedMapIndex) {
        GameCatalog.mapByIndex(profile.selectedMapIndex)
    }

    GunboundBackdrop {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(14.dp)
        ) {
            val isWide = maxWidth >= 680.dp

            Column(
                modifier = Modifier
                    .widthIn(max = 980.dp)
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Branding Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.lobby_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = GbGoldAccent
                        )
                        Text(
                            text = stringResource(R.string.lobby_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = GbTextSecondary
                        )
                    }

                    IconButton(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier.testTag("lobby_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reset local profile",
                            tint = GbTextSecondary
                        )
                    }
                }

                // Player Rank / Currency / Name Header
                PlayerProfileHeader(
                    profile = profile,
                    loader = spriteLoader,
                    modifier = Modifier.testTag("profile_header_card"),
                    onEditClick = { showNicknameDialog = true }
                )

                // Classic Channel 1..8 Strip
                GunboundPanel(title = "Battle Channel ($selectedChannel / 8)") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (ch in 1..8) {
                            val active = ch == selectedChannel
                            Surface(
                                modifier = Modifier
                                    .size(width = 52.dp, height = 48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelectChannel(ch) }
                                    .testTag("channel_button_$ch"),
                                shape = RoundedCornerShape(8.dp),
                                color = if (active) GbCyanPrimary.copy(alpha = 0.22f) else GbNavyDeep,
                                border = BorderStroke(
                                    width = if (active) 2.dp else 1.dp,
                                    color = if (active) GbCyanPrimary else GbSteelBorder
                                )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    ClassicSpriteFrame(
                                        loader = spriteLoader,
                                        assetPath = "classic/lobby/b_gamechat_ch$ch.png",
                                        frameIndex = if (active) 4 else 0,
                                        totalFrames = 5,
                                        contentDescription = "Channel $ch",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "CH$ch",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (active) GbCyanPrimary else GbTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TrainingRoomListCard(
                            selectedMobileName = selectedMobile.name,
                            selectedMobileAsset = selectedMobile.buttonAsset,
                            selectedMap = selectedMap,
                            profile = profile,
                            spriteLoader = spriteLoader,
                            onEnterRoom = onEnterRoom,
                            modifier = Modifier.weight(1.3f)
                        )
                        LobbyActionsColumn(
                            profile = profile,
                            onEnterRoom = onEnterRoom,
                            onQuickStartMatch = onQuickStartMatch,
                            onOpenShop = onOpenShop,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    TrainingRoomListCard(
                        selectedMobileName = selectedMobile.name,
                        selectedMobileAsset = selectedMobile.buttonAsset,
                        selectedMap = selectedMap,
                        profile = profile,
                        spriteLoader = spriteLoader,
                        onEnterRoom = onEnterRoom
                    )
                    LobbyActionsColumn(
                        profile = profile,
                        onEnterRoom = onEnterRoom,
                        onQuickStartMatch = onQuickStartMatch,
                        onOpenShop = onOpenShop
                    )
                }

                // Classic Lobby Art Banner
                val lobbyBack = remember { spriteLoader.loadImageBitmap("classic/lobby/back.png") }
                if (lobbyBack != null) {
                    GunboundPanel(title = "Thor's Hammer Classic Roster") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                bitmap = lobbyBack,
                                contentDescription = "Classic Lobby Frame",
                                modifier = Modifier
                                    .size(width = 110.dp, height = 74.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, GbSteelBorder, RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                                filterQuality = FilterQuality.Medium
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "16 Classic Mobiles · 22 Map Sides",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GbTextPrimary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "20 Hz ballistics, destructible craters, dynamic wind & weather, 11 battle items, and local Avatar Shop progression.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GbTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNicknameDialog) {
        var draft by remember { mutableStateOf(profile.nickname) }
        AlertDialog(
            onDismissRequest = { showNicknameDialog = false },
            title = { Text("Commander Callsign") },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it.take(16) },
                    label = { Text("Nickname") },
                    singleLine = true,
                    modifier = Modifier.testTag("nickname_input")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateNickname(draft)
                        showNicknameDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showNicknameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Local Profile?") },
            text = { Text("Restores 1000 Gold, 1000 Cash, 0 XP, and clears owned avatars.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetProfile()
                        showResetConfirm = false
                    }
                ) { Text("Reset", color = GbCrimsonDanger) }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TrainingRoomListCard(
    selectedMobileName: String,
    selectedMobileAsset: String,
    selectedMap: com.example.data.MapSideSpec,
    profile: PlayerProfile,
    spriteLoader: AssetSpriteLoader,
    onEnterRoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    GunboundPanel(title = "Offline Training Room", modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onEnterRoom)
                .testTag("training_room_card"),
            shape = RoundedCornerShape(10.dp),
            color = GbNavyDeep,
            border = BorderStroke(1.5.dp, GbCyanPrimary.copy(alpha = 0.7f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ClassicSpriteFrame(
                    loader = spriteLoader,
                    assetPath = "classic/room/details/maps.png",
                    frameIndex = selectedMap.index,
                    totalFrames = 22,
                    contentDescription = selectedMap.displayName,
                    modifier = Modifier
                        .size(width = 96.dp, height = 60.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, GbGoldAccent.copy(alpha = 0.6f), RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#001 · 1v1 SOLO",
                            style = MaterialTheme.typography.labelLarge,
                            color = GbGoldAccent
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(GbEmeraldHp.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "READY",
                                style = MaterialTheme.typography.labelSmall,
                                color = GbEmeraldHp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = selectedMap.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = GbTextPrimary
                    )
                    Text(
                        text = "$selectedMobileName vs CPU (${profile.cpuDifficulty.label})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GbTextSecondary
                    )
                }
                ClassicSpriteFrame(
                    loader = spriteLoader,
                    assetPath = selectedMobileAsset,
                    frameIndex = 0,
                    totalFrames = 5,
                    contentDescription = selectedMobileName,
                    modifier = Modifier.size(width = 56.dp, height = 42.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Record: ${profile.wins}W - ${profile.losses}L",
                style = MaterialTheme.typography.labelMedium,
                color = GbTextSecondary
            )
            Text(
                text = "Attr Pts Avail: ${profile.availableAttributePoints}",
                style = MaterialTheme.typography.labelMedium,
                color = GbCyanPrimary
            )
        }
    }
}

@Composable
private fun LobbyActionsColumn(
    profile: PlayerProfile,
    onEnterRoom: () -> Unit,
    onQuickStartMatch: () -> Unit,
    onOpenShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    GunboundPanel(title = "Command Deck", modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            GunboundActionButton(
                text = stringResource(R.string.lobby_create_room),
                onClick = onEnterRoom,
                accentColor = GbCyanPrimary,
                leadingIcon = Icons.Filled.AddCircle,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_room_button")
            )
            GunboundActionButton(
                text = stringResource(R.string.lobby_join_room),
                onClick = onQuickStartMatch,
                accentColor = GbGoldAccent,
                leadingIcon = Icons.Filled.PlayArrow,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_match_button")
            )
            GunboundActionButton(
                text = "${stringResource(R.string.lobby_avatar_shop)} (${profile.ownedAvatars.size})",
                onClick = onOpenShop,
                accentColor = Color(0xFFA78BFA),
                leadingIcon = Icons.Filled.Checkroom,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("avatar_shop_button")
            )
        }
    }
}
