package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class AssetSpriteLoader(private val context: Context) {

    private val fullBitmapCache = ConcurrentHashMap<String, Bitmap>()
    private val slicedCache = ConcurrentHashMap<String, ImageBitmap>()
    private var avatarCatalogJson: JSONObject? = null

    fun loadImageBitmap(assetPath: String): ImageBitmap? {
        return slicedCache.getOrPut("full:$assetPath") {
            loadRawBitmap(assetPath)?.asImageBitmap() ?: return null
        }
    }

    fun loadFrame(assetPath: String, frameIndex: Int, totalFrames: Int): ImageBitmap? {
        val safeIdx = frameIndex.coerceIn(0, (totalFrames - 1).coerceAtLeast(0))
        val key = "frame:$assetPath:$safeIdx:$totalFrames"
        slicedCache[key]?.let { return it }

        val full = loadRawBitmap(assetPath) ?: return null
        val frameW = (full.width / totalFrames.coerceAtLeast(1)).coerceAtLeast(1)
        val x = (safeIdx * frameW).coerceAtMost((full.width - frameW).coerceAtLeast(0))
        val cropW = frameW.coerceAtMost(full.width - x)
        if (cropW <= 0 || full.height <= 0) return null

        val sliced = Bitmap.createBitmap(full, x, 0, cropW, full.height).asImageBitmap()
        slicedCache[key] = sliced
        return sliced
    }

    fun loadRegion(assetPath: String, x: Int, y: Int, w: Int, h: Int): ImageBitmap? {
        val key = "reg:$assetPath:$x:$y:$w:$h"
        slicedCache[key]?.let { return it }
        val full = loadRawBitmap(assetPath) ?: return null
        val sx = x.coerceIn(0, (full.width - 1).coerceAtLeast(0))
        val sy = y.coerceIn(0, (full.height - 1).coerceAtLeast(0))
        val sw = w.coerceIn(1, (full.width - sx).coerceAtLeast(1))
        val sh = h.coerceIn(1, (full.height - sy).coerceAtLeast(1))
        val sliced = Bitmap.createBitmap(full, sx, sy, sw, sh).asImageBitmap()
        slicedCache[key] = sliced
        return sliced
    }

    fun loadAvatarSprite(item: AvatarItem): ImageBitmap? {
        val catalog = getAvatarCatalog()
        val pieces = catalog?.optJSONObject("pieces")
        val pieceObj = pieces?.optJSONObject(item.id)
        if (pieceObj != null) {
            val sheet = pieceObj.optString("sheet", item.sheet)
            val reg = pieceObj.optJSONArray("region")
            if (reg != null && reg.length() == 4) {
                return loadRegion(
                    "avatars/$sheet.png",
                    reg.getInt(0),
                    reg.getInt(1),
                    reg.getInt(2),
                    reg.getInt(3)
                )
            }
        }
        return loadRegion(
            "avatars/${item.sheet}.png",
            item.regionX,
            item.regionY,
            item.regionW,
            item.regionH
        )
    }

    fun loadLoadingGroundRows(): List<Int> {
        return try {
            val text = context.assets.open("classic/loading/catalog.json").bufferedReader().use { it.readText() }
            val json = JSONObject(text)
            val arr = json.optJSONArray("ground_rows") ?: return emptyList()
            List(arr.length()) { i -> arr.getInt(i) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun getAvatarCatalog(): JSONObject? {
        if (avatarCatalogJson != null) return avatarCatalogJson
        return try {
            val text = context.assets.open("avatars/catalog.json").bufferedReader().use { it.readText() }
            JSONObject(text).also { avatarCatalogJson = it }
        } catch (_: Exception) {
            null
        }
    }

    private fun loadRawBitmap(assetPath: String): Bitmap? {
        fullBitmapCache[assetPath]?.let { return it }
        return try {
            context.assets.open(assetPath).use { stream ->
                BitmapFactory.decodeStream(stream)?.also { bmp ->
                    fullBitmapCache[assetPath] = bmp
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}

@Composable
fun rememberAssetSpriteLoader(): AssetSpriteLoader {
    val context = LocalContext.current.applicationContext
    return remember(context) { AssetSpriteLoader(context) }
}
