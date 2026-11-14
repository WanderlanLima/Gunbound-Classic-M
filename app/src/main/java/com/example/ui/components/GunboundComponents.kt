package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.AssetSpriteLoader
import com.example.data.GameCatalog
import com.example.data.PlayerProfile
import com.example.ui.theme.GbCyanPrimary
import com.example.ui.theme.GbGoldAccent
import com.example.ui.theme.GbNavyDeep
import com.example.ui.theme.GbNavySurface
import com.example.ui.theme.GbSlatePanel
import com.example.ui.theme.GbSteelBorder
import com.example.ui.theme.GbSteelHighlight
import com.example.ui.theme.GbTextPrimary
import com.example.ui.theme.GbTextSecondary

@Composable
fun GunboundBackdrop(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GbNavyDeep, GbNavySurface, Color(0xFF0A162B))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 48.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = Color(0x0D36D8FF),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = Color(0x0D36D8FF),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += step
            }
        }
        content()
    }
}

@Composable
fun GunboundPanel(
    modifier: Modifier = Modifier,
    title: String? = null,
    borderColor: Color = GbSteelBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = GbSlatePanel.copy(alpha = 0.92f),
        border = BorderStroke(1.5.dp, borderColor),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (title != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 4.dp, height = 16.dp)
                            .background(GbGoldAccent, RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = GbCyanPrimary
                    )
                }
            }
            content()
        }
    }
}

@Composable
fun ClassicSpriteFrame(
    loader: AssetSpriteLoader,
    assetPath: String,
    frameIndex: Int,
    totalFrames: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val bmp = remember(assetPath, frameIndex, totalFrames) {
        loader.loadFrame(assetPath, frameIndex, totalFrames)
    }
    if (bmp != null) {
        Image(
            bitmap = bmp,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            filterQuality = FilterQuality.Medium
        )
    }
}

@Composable
fun PlayerProfileHeader(
    profile: PlayerProfile,
    loader: AssetSpriteLoader,
    modifier: Modifier = Modifier,
    onEditClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .let { if (onEditClick != null) it.clickable(onClick = onEditClick) else it },
        shape = RoundedCornerShape(12.dp),
        color = GbSlatePanel,
        border = BorderStroke(1.5.dp, GbSteelHighlight)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GbNavyDeep)
                        .border(1.dp, GbGoldAccent, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ClassicSpriteFrame(
                        loader = loader,
                        assetPath = "classic/ranks/rank1.png",
                        frameIndex = profile.rankIndex,
                        totalFrames = 25,
                        contentDescription = GameCatalog.rankName(profile.rankIndex),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = profile.nickname,
                        style = MaterialTheme.typography.titleMedium,
                        color = GbTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${GameCatalog.rankName(profile.rankIndex)} · ${profile.xp} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = GbTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CurrencyPill(
                    label = "${profile.gold} G",
                    tint = GbGoldAccent,
                    icon = Icons.Filled.MonetizationOn
                )
                CurrencyPill(
                    label = "${profile.cash} C",
                    tint = GbCyanPrimary,
                    icon = Icons.Filled.Star
                )
            }
        }
    }
}

@Composable
fun CurrencyPill(
    label: String,
    tint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = GbNavyDeep,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = tint,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GunboundActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = GbGoldAccent,
    minHeight: Dp = 48.dp,
    enabled: Boolean = true,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Surface(
        modifier = modifier
            .height(minHeight)
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (enabled) accentColor.copy(alpha = 0.18f) else GbNavySurface,
        border = BorderStroke(1.5.dp, if (enabled) accentColor else GbSteelBorder.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (enabled) accentColor else GbTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) accentColor else GbTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
