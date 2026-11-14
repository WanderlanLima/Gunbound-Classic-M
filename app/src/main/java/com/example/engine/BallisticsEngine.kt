package com.example.engine

import androidx.compose.ui.graphics.Color
import com.example.data.BattleItemId
import com.example.data.FlightStyle
import com.example.data.MobileSpec
import com.example.data.WeatherType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

enum class ShotType(val label: String) {
    T1("1"),
    T2("2"),
    SS("SS")
}

data class Vec2(val x: Float, val y: Float)

data class ActorState(
    val id: String,
    val name: String,
    val isPlayer: Boolean,
    val isTeamA: Boolean,
    val mobile: MobileSpec,
    val x: Float,
    val y: Float,
    val facingRight: Boolean,
    val slopeDeg: Float,
    val aimAngleDeg: Int, // Relative angle (e.g. 15..60)
    val hp: Int,
    val maxHp: Int,
    val shield: Int,
    val maxShield: Int,
    val delay: Int,
    val moveFuel: Float = 100f,
    val attackMod: Float = 1f,
    val defenseMod: Float = 1f,
    val digMod: Float = 1f,
    val shieldRegenBonus: Int = 0,
    val activeTurnItem: BattleItemId? = null,
    val ssCooldownTurns: Int = 0,
    val armorBroken: Boolean = false
) {
    val isAlive: Boolean get() = hp > 0
    val effectiveWorldAngleDeg: Float
        get() {
            val base = aimAngleDeg.toFloat()
            return if (facingRight) -base + slopeDeg else -(180f - base) + slopeDeg
        }
}

data class ActiveProjectile(
    val id: Int,
    val ownerIsPlayer: Boolean,
    val mobile: MobileSpec,
    val shotType: ShotType,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val damage: Int,
    val craterRadius: Float,
    val color: Color,
    var ageTicks: Int = 0,
    var delayStartTicks: Int = 0,
    var isBurrowed: Boolean = false,
    val isGhost: Boolean = false,
    val isTeleportBeacon: Boolean = false,
    val isThunderCall: Boolean = false,
    var bouncesLeft: Int = 0,
    var crawlerTicksLeft: Int = 0,
    var hasSplit: Boolean = false,
    val isChildSplit: Boolean = false,
    val wavePhaseOffset: Float = 0f,
    val trail: MutableList<Vec2> = mutableListOf(),
    var alive: Boolean = true
)

data class ExplosionFx(
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Color,
    val age: Float = 0f,
    val maxAge: Float = 0.55f,
    val lightningBeamX: Float? = null
)

data class FloatingTextFx(
    val x: Float,
    val y: Float,
    val text: String,
    val color: Color,
    val age: Float = 0f,
    val maxAge: Float = 1.25f
)

object BallisticsEngine {
    const val DT = 0.05f // 20 Hz fixed physics step
    const val GRAVITY = 285f
    const val MAX_LAUNCH_SPEED = 740f
    const val MIN_LAUNCH_SPEED = 110f

    fun computeMuzzle(actor: ActorState): Vec2 {
        val dir = if (actor.facingRight) 1f else -1f
        val fireDir = if (actor.mobile.firesFromTail) -dir else dir
        val rad = actor.effectiveWorldAngleDeg * (PI / 180.0).toFloat()
        val ox = actor.x + cos(rad) * 20f * (if (actor.mobile.firesFromTail) -1f else 1f)
        val oy = actor.y - 18f + sin(rad) * 14f
        return Vec2(ox + fireDir * 4f, oy)
    }

    fun spawnShotProjectiles(
        actor: ActorState,
        shotType: ShotType,
        power01: Float,
        nextIdStart: Int
    ): List<ActiveProjectile> {
        val mobile = actor.mobile
        val muzzle = computeMuzzle(actor)
        val clampedPower = power01.coerceIn(0.05f, 1.0f)
        val speed = MIN_LAUNCH_SPEED + (MAX_LAUNCH_SPEED - MIN_LAUNCH_SPEED) * clampedPower
        val angleRad = actor.effectiveWorldAngleDeg * (PI / 180.0)
        val baseVx = (cos(angleRad) * speed).toFloat()
        val baseVy = (sin(angleRad) * speed).toFloat()

        val item = actor.activeTurnItem
        val dmgMult = actor.attackMod * when (item) {
            BattleItemId.POWER_UP -> 1.30f
            BattleItemId.BLOOD -> 1.50f
            else -> 1f
        }
        val craterMult = actor.digMod * (if (item == BattleItemId.BUNGE_SHOT) 1.65f else 1f)

        val baseDmg = when (shotType) {
            ShotType.T1 -> mobile.t1Damage
            ShotType.T2 -> mobile.t2Damage
            ShotType.SS -> mobile.ssDamage
        }
        val baseCrater = when (shotType) {
            ShotType.T1 -> mobile.t1Crater
            ShotType.T2 -> mobile.t2Crater
            ShotType.SS -> mobile.ssCrater
        }

        val isTeleport = item == BattleItemId.TELEPORT || item == BattleItemId.TEAM_TELEPORT
        val isThunder = item == BattleItemId.THUNDER
        val list = mutableListOf<ActiveProjectile>()
        var pid = nextIdStart

        if (isTeleport) {
            list.add(
                ActiveProjectile(
                    id = pid++,
                    ownerIsPlayer = actor.isPlayer,
                    mobile = mobile,
                    shotType = shotType,
                    x = muzzle.x, y = muzzle.y, vx = baseVx, vy = baseVy,
                    damage = 0, craterRadius = 10f,
                    color = Color(0xFF38BDF8),
                    isTeleportBeacon = true
                )
            )
            return list
        }

        fun makeProj(
            vx: Float = baseVx,
            vy: Float = baseVy,
            dmg: Int = (baseDmg * dmgMult).toInt(),
            crater: Float = baseCrater * craterMult,
            delayTicks: Int = 0,
            color: Color = mobile.accentColor,
            isGhost: Boolean = false,
            bounces: Int = 0,
            crawlerTicks: Int = 0,
            phase: Float = 0f
        ) {
            list.add(
                ActiveProjectile(
                    id = pid++,
                    ownerIsPlayer = actor.isPlayer,
                    mobile = mobile,
                    shotType = shotType,
                    x = muzzle.x,
                    y = muzzle.y,
                    vx = vx,
                    vy = vy,
                    damage = dmg,
                    craterRadius = crater,
                    color = color,
                    delayStartTicks = delayTicks,
                    isThunderCall = isThunder,
                    isGhost = isGhost,
                    bouncesLeft = bounces,
                    crawlerTicksLeft = crawlerTicks,
                    wavePhaseOffset = phase
                )
            )
        }

        when {
            // Dual / Dual+ item
            item == BattleItemId.DUAL || item == BattleItemId.DUAL_PLUS -> {
                makeProj(dmg = (baseDmg * dmgMult).toInt(), delayTicks = 0)
                val secondDmg = if (item == BattleItemId.DUAL_PLUS) mobile.t2Damage else baseDmg
                makeProj(
                    vx = baseVx * 1.015f,
                    vy = baseVy * 0.99f,
                    dmg = (secondDmg * dmgMult * 0.92f).toInt(),
                    delayTicks = 6
                )
            }

            // Bigfoot multi-missiles
            mobile.id == 2 && shotType == ShotType.T1 -> {
                val spreads = listOf(-0.045f, -0.015f, 0.015f, 0.045f)
                spreads.forEachIndexed { idx, s ->
                    makeProj(
                        vx = baseVx * (1f + s),
                        vy = baseVy * (1f - s * 0.6f),
                        dmg = (baseDmg * dmgMult / 4f).toInt(),
                        crater = baseCrater * craterMult * 0.68f,
                        delayTicks = idx * 2
                    )
                }
            }
            mobile.id == 2 && shotType == ShotType.T2 -> {
                for (i in 0 until 6) {
                    val s = (i - 2.5f) * 0.025f
                    makeProj(
                        vx = baseVx * (1f + s),
                        vy = baseVy * (1f - s * 0.5f),
                        dmg = (baseDmg * dmgMult / 6f).toInt(),
                        crater = baseCrater * craterMult * 0.65f,
                        delayTicks = (i / 3) * 5 + (i % 3) * 1
                    )
                }
            }
            mobile.id == 2 && shotType == ShotType.SS -> {
                for (i in 0 until 8) {
                    val s = (i - 3.5f) * 0.024f
                    makeProj(
                        vx = baseVx * (1f + s),
                        vy = baseVy * (1f - s * 0.45f),
                        dmg = (baseDmg * dmgMult / 8f).toInt(),
                        crater = baseCrater * craterMult * 0.68f,
                        delayTicks = i * 2
                    )
                }
            }

            // Nak SS Ghost
            mobile.id == 3 && shotType == ShotType.SS -> {
                makeProj(isGhost = true, color = Color(0xFF4ADE80))
            }

            // Armor T2 Twin
            mobile.id == 4 && shotType == ShotType.T2 -> {
                makeProj(dmg = (baseDmg * dmgMult * 0.52f).toInt(), delayTicks = 0)
                makeProj(vx = baseVx * 1.02f, vy = baseVy * 0.98f, dmg = (baseDmg * dmgMult * 0.52f).toInt(), delayTicks = 5)
            }

            // Trico T2 3-way
            mobile.id == 5 && shotType == ShotType.T2 -> {
                listOf(-0.04f, 0f, 0.04f).forEach { s ->
                    makeProj(
                        vx = baseVx * (1f + s),
                        vy = baseVy * (1f - s),
                        dmg = (baseDmg * dmgMult / 3f).toInt(),
                        crater = baseCrater * craterMult * 0.72f
                    )
                }
            }

            // Boomer T2 3-boom
            mobile.id == 6 && shotType == ShotType.T2 -> {
                listOf(0, 3, 6).forEach { d ->
                    makeProj(dmg = (baseDmg * dmgMult / 3f).toInt(), crater = baseCrater * craterMult * 0.7f, delayTicks = d)
                }
            }

            // Raon T2 Twin crawlers
            mobile.id == 7 && shotType == ShotType.T2 -> {
                makeProj(dmg = (baseDmg * dmgMult * 0.52f).toInt(), crawlerTicks = 28, delayTicks = 0)
                makeProj(vx = baseVx * 1.03f, dmg = (baseDmg * dmgMult * 0.52f).toInt(), crawlerTicks = 28, delayTicks = 5)
            }

            // Mage T2 dual helix
            mobile.id == 1 && shotType == ShotType.T2 -> {
                makeProj(dmg = (baseDmg * dmgMult * 0.52f).toInt(), phase = 0f)
                makeProj(dmg = (baseDmg * dmgMult * 0.52f).toInt(), phase = PI.toFloat())
            }

            // Turtle T2 twin water wave
            mobile.id == 12 && shotType == ShotType.T2 -> {
                makeProj(dmg = (baseDmg * dmgMult * 0.52f).toInt(), phase = 0f, color = Color(0xFF22D3EE))
                makeProj(dmg = (baseDmg * dmgMult * 0.52f).toInt(), phase = PI.toFloat(), color = Color(0xFF67E8F9))
            }

            // Grub T2 4-ball bounce
            mobile.id == 13 && shotType == ShotType.T2 -> {
                for (i in 0 until 4) {
                    val s = (i - 1.5f) * 0.03f
                    makeProj(
                        vx = baseVx * (1f + s),
                        vy = baseVy,
                        dmg = (baseDmg * dmgMult / 4f).toInt(),
                        crater = baseCrater * craterMult * 0.65f,
                        bounces = 2,
                        delayTicks = i * 3
                    )
                }
            }

            // Dragon T2 4-volley / SS 5-meteor
            mobile.id == 14 && (shotType == ShotType.T2 || shotType == ShotType.SS) -> {
                val count = if (shotType == ShotType.SS) 5 else 4
                for (i in 0 until count) {
                    val s = (i - (count - 1) / 2f) * 0.025f
                    makeProj(
                        vx = baseVx * (1f + s),
                        vy = baseVy * (1f - s * 0.5f),
                        dmg = (baseDmg * dmgMult / count).toInt(),
                        crater = baseCrater * craterMult * 0.7f,
                        delayTicks = i * 3
                    )
                }
            }

            // Knight T2 5-fan / SS 8-rain
            mobile.id == 15 && (shotType == ShotType.T2 || shotType == ShotType.SS) -> {
                val count = if (shotType == ShotType.SS) 8 else 5
                for (i in 0 until count) {
                    val s = (i - (count - 1) / 2f) * 0.022f
                    makeProj(
                        vx = baseVx * (1f + s),
                        vy = baseVy * (1f - s * 0.4f),
                        dmg = (baseDmg * dmgMult / count).toInt(),
                        crater = baseCrater * craterMult * 0.68f,
                        delayTicks = i * 2
                    )
                }
            }

            else -> makeProj()
        }
        return list
    }

    /**
     * Closed deterministic simulation for CPU AI — shares exact ballistics with live combat.
     */
    fun predictShotImpact(
        shooter: ActorState,
        target: ActorState,
        shotType: ShotType,
        angleDeg: Int,
        power01: Float,
        wind: Vec2,
        weather: WeatherType,
        tornadoX: Float?,
        terrain: TerrainSnapshot
    ): Vec2? {
        val simShooter = shooter.copy(aimAngleDeg = angleDeg, activeTurnItem = null)
        val muzzle = computeMuzzle(simShooter)
        val speed = MIN_LAUNCH_SPEED + (MAX_LAUNCH_SPEED - MIN_LAUNCH_SPEED) * power01.coerceIn(0.05f, 1f)
        val angleRad = simShooter.effectiveWorldAngleDeg * (PI / 180.0)
        var x = muzzle.x
        var y = muzzle.y
        var vx = (cos(angleRad) * speed).toFloat()
        var vy = (sin(angleRad) * speed).toFloat()

        val windScale = if (shooter.mobile.flightStyle == FlightStyle.BOOMER_WIND) 1.75f else 1.0f

        for (tick in 0 until 220) {
            if (x < -100f || x > TerrainMap.WORLD_W + 100f || y > TerrainMap.WORLD_H + 80f) {
                return Vec2(x, y)
            }
            if (hypot(x - target.x, y - (target.y - 16f)) < 26f) {
                return Vec2(x, y)
            }
            if (tick > 2 && terrain.isSolidWorld(x, y)) {
                return Vec2(x, y)
            }
            vx += wind.x * windScale * DT
            vy += (GRAVITY + wind.y * windScale) * DT
            if (weather == WeatherType.TORNADO && tornadoX != null && kotlin.math.abs(x - tornadoX) < 38f) {
                vx *= 0.88f
                vy -= 420f * DT
            }
            x += vx * DT
            y += vy * DT
        }
        return Vec2(x, y)
    }
}
