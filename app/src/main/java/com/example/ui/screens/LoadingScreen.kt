package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AssetSpriteLoader
import com.example.data.GameCatalog
import com.example.data.PlayerProfile
import com.example.ui.components.ClassicSpriteFrame
import com.example.ui.components.GunboundBackdrop
import com.example.ui.components.GunboundPanel
import com.example.ui.theme.GbCyanPrimary
import com.example.ui.theme.GbGoldAccent
import com.example.ui.theme.GbNavyDeep
import com.example.ui.theme.GbSteelBorder
import com.example.ui.theme.GbTeamBlue
import com.example.ui.theme.GbTeamRed
import com.example.ui.theme.GbTextPrimary
import com.example.ui.theme.GbTextSecondary
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    profile: PlayerProfile,
    spriteLoader: AssetSpriteLoader,
    onReadyComplete: () -> Unit
) {
    var targetProgress by remember { mutableFloatStateOf(0f) }
    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1600),
        label = "loading_progress"
    )

    val mapSpec = remember(profile.selectedMapIndex) { GameCatalog.mapByIndex(profile.selectedMapIndex) }
    val playerMobile = remember(profile.selectedMobileId) { GameCatalog.mobileById(profile.selectedMobileId) }
    val cpuMobile = remember(profile.selectedMobileId) { GameCatalog.opponentFor(profile.selectedMobileId) }
    val miramoPreview = remember { spriteLoader.loadImageBitmap("classic/loading/miramo_town.png") }

    LaunchedEffect(Unit) {
        targetProgress = 1f
        delay(1750L)
        onReadyComplete()
    }

    GunboundBackdrop {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
                .clickable(onClick = onReadyComplete),
            contentAlignment = Alignment.Center
        ) {
            GunboundPanel(
                title = stringResource(R.string.loading_title),
                modifier = Modifier.widthIn(max = 680.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = mapSpec.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = GbGoldAccent
                    )
                    Text(
                        text = mapSpec.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GbTextSecondary
                    )

                    if (mapSpec.index == 0 && miramoPreview != null) {
                        Image(
                            bitmap = miramoPreview,
                            contentDescription = mapSpec.displayName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.5.dp, GbCyanPrimary.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            filterQuality = FilterQuality.Medium
                        )
                    } else {
                        ClassicSpriteFrame(
                            loader = spriteLoader,
                            assetPath = "classic/room/details/maps.png",
                            frameIndex = mapSpec.index,
                            totalFrames = 22,
                            contentDescription = mapSpec.displayName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.5.dp, GbCyanPrimary.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GbNavyDeep,
                            border = BorderStroke(1.dp, GbTeamRed)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ClassicSpriteFrame(
                                    loader = spriteLoader,
                                    assetPath = playerMobile.buttonAsset,
                                    frameIndex = 4,
                                    totalFrames = 5,
                                    contentDescription = playerMobile.name,
                                    modifier = Modifier.size(width = 48.dp, height = 36.dp)
                                )
                                Column {
                                    Text(profile.nickname, style = MaterialTheme.typography.labelLarge, color = GbTextPrimary)
                                    Text(playerMobile.name, style = MaterialTheme.typography.labelSmall, color = GbTeamRed)
                                }
                            }
                        }

                        Text("VS", style = MaterialTheme.typography.titleLarge, color = GbGoldAccent)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GbNavyDeep,
                            border = BorderStroke(1.dp, GbTeamBlue)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("CPU (${profile.cpuDifficulty.label})", style = MaterialTheme.typography.labelLarge, color = GbTextPrimary)
                                    Text(cpuMobile.name, style = MaterialTheme.typography.labelSmall, color = GbTeamBlue)
                                }
                                ClassicSpriteFrame(
                                    loader = spriteLoader,
                                    assetPath = cpuMobile.buttonAsset,
                                    frameIndex = 4,
                                    totalFrames = 5,
                                    contentDescription = cpuMobile.name,
                                    modifier = Modifier.size(width = 48.dp, height = 36.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Classic progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(GbNavyDeep)
                            .border(1.dp, GbSteelBorder, RoundedCornerShape(9.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .background(GbCyanPrimary)
                        )
                    }
                    Text(
                        text = "Loading Arena ${(progress * 100).toInt()}% · Tap to deploy immediately",
                        style = MaterialTheme.typography.labelSmall,
                        color = GbTextSecondary
                    )
                }
            }
        }
    }
}
