package com.example.engine

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.toArgb
import com.example.data.MapSideSpec
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

class TerrainSnapshot(
    val gridW: Int,
    val gridH: Int,
    val scale: Float,
    private val solid: BooleanArray
) {
    fun isSolidWorld(wx: Float, wy: Float): Boolean {
        val gx = (wx / scale).toInt()
        val gy = (wy / scale).toInt()
        if (gx < 0 || gx >= gridW || gy >= gridH) return false
        if (gy < 0) return false
        return solid[gy * gridW + gx]
    }
}

class TerrainMap(
    val spec: MapSideSpec,
    miramoGroundRows: List<Int>
) {
    companion object {
        const val WORLD_W = 1600f
        const val WORLD_H = 840f
        const val SCALE = 2f
        const val GRID_W = 800
        const val GRID_H = 420
    }

    private val solid = BooleanArray(GRID_W * GRID_H)
    val bitmap: Bitmap = Bitmap.createBitmap(GRID_W, GRID_H, Bitmap.Config.ARGB_8888)

    @Volatile
    var version: Int = 0
        private set

    init {
        buildMap(miramoGroundRows)
    }

    private fun buildMap(miramoGroundRows: List<Int>) {
        val fillBase = spec.terrainFill.toArgb()
        val topColor = spec.terrainTop.toArgb()
        val rimColor = spec.terrainRim.toArgb()

        val fillR = AndroidColor.red(fillBase)
        val fillG = AndroidColor.green(fillBase)
        val fillB = AndroidColor.blue(fillBase)

        val pixels = IntArray(GRID_W * GRID_H)

        // Build height profile & floating/bridge gaps per map side
        val topY = IntArray(GRID_W)
        val bottomY = IntArray(GRID_W) { GRID_H }

        for (gx in 0 until GRID_W) {
            val t = gx.toFloat() / GRID_W.toFloat()
            val worldIdx = spec.index / 2
            val isSideB = (spec.index % 2) == 1

            var surfaceY = when (worldIdx) {
                0 -> {
                    // Miramo Town: use authentic ground_rows from loading catalog if available
                    if (miramoGroundRows.isNotEmpty() && !isSideB) {
                        val rowIdx = (t * (miramoGroundRows.size - 1)).toInt().coerceIn(0, miramoGroundRows.lastIndex)
                        val rawRow = miramoGroundRows[rowIdx]
                        // catalog ground_rows are in 0..209 -> map to lower half of 420 grid
                        (180 + (rawRow - 105) * 1.45f).toInt().coerceIn(135, 395)
                    } else {
                        (245 - 48 * sin(t * PI * 2.1 + 0.4) - 24 * cos(t * PI * 5.0)).toInt()
                    }
                }
                1 -> { // Nirvana: floating platforms with central gap
                    val wave = 230 - (45 * sin(t * PI * 3.0)).toInt()
                    if (t in 0.43f..0.57f) 415 else wave
                }
                2 -> { // Sea of Hero: coastal islands
                    val ridge = 265 - (55 * sin(t * PI * 2.5 + if (isSideB) 1.0 else 0.0)).toInt()
                    if (t in 0.46f..0.54f) 416 else ridge
                }
                3 -> { // Adium Root: deep mossy bowl
                    (205 + 68 * sin(t * PI) + 22 * cos(t * PI * 6.0)).toInt()
                }
                4 -> { // Dummy Slope: steep asymmetric slope
                    val base = if (isSideB) 185f + t * 145f else 330f - t * 145f
                    (base + 22f * sin(t * PI * 4.0).toFloat()).toInt()
                }
                5 -> { // Meta Mine: stepped foundry decks
                    val step = when {
                        t < 0.25f -> 225
                        t < 0.42f -> 270
                        t < 0.58f -> 235
                        t < 0.75f -> 270
                        else -> 225
                    }
                    step + (if (isSideB) 15 else 0)
                }
                6 -> { // Cozy Tower: twin high towers
                    val leftTower = if (t in 0.16f..0.33f) 170 else 285
                    val rightTower = if (t in 0.67f..0.84f) 170 else 285
                    minOf(leftTower, rightTower) + (12 * sin(t * PI * 5.0)).toInt()
                }
                7 -> { // Cave: jagged peaks
                    (240 - 40 * sin(t * PI * 4.0) + 25 * cos(t * PI * 7.0)).toInt()
                }
                8 -> { // Dragon: volcanic spine
                    (230 - 52 * sin(t * PI * 2.0) - 24 * sin(t * PI * 7.0 + if (isSideB) 1.2 else 0.0)).toInt()
                }
                9 -> { // Starry Night: twin crescent hills
                    if (t in 0.44f..0.56f) 418 else (235 - 45 * cos(t * PI * 4.0)).toInt()
                }
                else -> { // Secret Area: proving grounds
                    (245 - 35 * sin(t * PI * 3.2) + 20 * sin(t * PI * 8.0)).toInt()
                }
            }

            // Keep edges safe for spawning unless intentional central pit
            if (t < 0.06f || t > 0.94f) {
                surfaceY = 395
            }
            topY[gx] = surfaceY.coerceIn(120, GRID_H - 10)
            // Floating bridge thickness for some worlds
            bottomY[gx] = if ((worldIdx == 1 || worldIdx == 6) && t in 0.15f..0.85f && topY[gx] < 260) {
                (topY[gx] + 95).coerceAtMost(GRID_H)
            } else {
                GRID_H
            }
        }

        for (gx in 0 until GRID_W) {
            val ty = topY[gx]
            val by = bottomY[gx]
            if (ty >= GRID_H - 12) continue
            for (gy in ty until by) {
                val idx = gy * GRID_W + gx
                solid[idx] = true
                val depth = gy - ty
                pixels[idx] = when {
                    depth < 3 -> rimColor
                    depth < 10 -> topColor
                    else -> {
                        val stripe = ((gx / 8 + gy / 6) % 3) * 10 - 10
                        val shade = (1f - (depth.toFloat() / 260f) * 0.35f).coerceIn(0.55f, 1f)
                        AndroidColor.argb(
                            255,
                            ((fillR + stripe) * shade).toInt().coerceIn(0, 255),
                            ((fillG + stripe) * shade).toInt().coerceIn(0, 255),
                            ((fillB + stripe) * shade).toInt().coerceIn(0, 255)
                        )
                    }
                }
            }
        }

        bitmap.setPixels(pixels, 0, GRID_W, 0, 0, GRID_W, GRID_H)
        version++
    }

    fun isSolidWorld(wx: Float, wy: Float): Boolean {
        val gx = (wx / SCALE).toInt()
        val gy = (wy / SCALE).toInt()
        if (gx < 0 || gx >= GRID_W || gy < 0 || gy >= GRID_H) return false
        return solid[gy * GRID_W + gx]
    }

    fun findSurfaceY(wx: Float, startWy: Float = 0f): Float? {
        val gx = (wx / SCALE).toInt().coerceIn(0, GRID_W - 1)
        val startGy = (startWy / SCALE).toInt().coerceIn(0, GRID_H - 1)
        for (gy in startGy until GRID_H) {
            if (solid[gy * GRID_W + gx]) {
                return gy * SCALE
            }
        }
        return null
    }

    fun slopeAngleDeg(wx: Float, wy: Float): Float {
        val leftY = findSurfaceY((wx - 10f).coerceIn(0f, WORLD_W - 1f), (wy - 30f).coerceAtLeast(0f)) ?: wy
        val rightY = findSurfaceY((wx + 10f).coerceIn(0f, WORLD_W - 1f), (wy - 30f).coerceAtLeast(0f)) ?: wy
        return (atan2((rightY - leftY).toDouble(), 20.0) * 180.0 / PI).toFloat().coerceIn(-45f, 45f)
    }

    fun carveCrater(worldX: Float, worldY: Float, worldRadius: Float) {
        val cx = (worldX / SCALE).roundToInt()
        val cy = (worldY / SCALE).roundToInt()
        val r = (worldRadius / SCALE).coerceAtLeast(4f)
        val rimR = r + 3.5f
        val rInt = rimR.toInt() + 1

        val x0 = (cx - rInt).coerceAtLeast(0)
        val x1 = (cx + rInt).coerceAtMost(GRID_W - 1)
        val y0 = (cy - rInt).coerceAtLeast(0)
        val y1 = (cy + rInt).coerceAtMost(GRID_H - 1)

        val w = x1 - x0 + 1
        val h = y1 - y0 + 1
        if (w <= 0 || h <= 0) return

        val patch = IntArray(w * h)
        bitmap.getPixels(patch, 0, w, x0, y0, w, h)

        for (py in y0..y1) {
            for (px in x0..x1) {
                val dist = hypot((px - cx).toFloat(), (py - cy).toFloat())
                val gridIdx = py * GRID_W + px
                val patchIdx = (py - y0) * w + (px - x0)
                if (dist <= r) {
                    solid[gridIdx] = false
                    patch[patchIdx] = AndroidColor.TRANSPARENT
                } else if (dist <= rimR && solid[gridIdx]) {
                    val col = patch[patchIdx]
                    if (AndroidColor.alpha(col) > 0) {
                        // Darken crater ring x0.45 per Gunbound Recomp spec
                        patch[patchIdx] = AndroidColor.argb(
                            255,
                            (AndroidColor.red(col) * 0.45f).toInt(),
                            (AndroidColor.green(col) * 0.45f).toInt(),
                            (AndroidColor.blue(col) * 0.45f).toInt()
                        )
                    }
                }
            }
        }

        bitmap.setPixels(patch, 0, w, x0, y0, w, h)
        version++
    }

    fun snapshot(): TerrainSnapshot = TerrainSnapshot(GRID_W, GRID_H, SCALE, solid.copyOf())
}
