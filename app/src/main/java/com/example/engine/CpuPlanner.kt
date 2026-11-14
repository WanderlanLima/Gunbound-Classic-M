package com.example.engine

import com.example.data.CpuDifficulty
import com.example.data.WeatherType
import kotlin.math.hypot
import kotlin.random.Random

data class CpuShotPlan(
    val angleDeg: Int,
    val power01: Float,
    val shotType: ShotType,
    val moveDir: Int
)

object CpuPlanner {

    fun searchShot(
        cpu: ActorState,
        target: ActorState,
        wind: Vec2,
        weather: WeatherType,
        tornadoX: Float?,
        terrainSnapshot: TerrainSnapshot,
        difficulty: CpuDifficulty
    ): CpuShotPlan {
        val shotType = when {
            cpu.ssCooldownTurns <= 0 && Random.nextFloat() < 0.25f -> ShotType.SS
            Random.nextFloat() < 0.65f -> ShotType.T2
            else -> ShotType.T1
        }

        var bestAngle = ((cpu.mobile.minAngle + cpu.mobile.maxAngle) / 2).coerceIn(cpu.mobile.minAngle, cpu.mobile.maxAngle)
        var bestPower = 0.55f
        var bestDist = Float.MAX_VALUE

        val minA = cpu.mobile.minAngle
        val maxA = cpu.mobile.maxAngle
        val angleStep = if (difficulty == CpuDifficulty.HARD) 4 else 6
        val powerSteps = if (difficulty == CpuDifficulty.HARD) 16 else 11

        var angle = minA
        while (angle <= maxA) {
            for (pIdx in 1..powerSteps) {
                val p = 0.16f + (pIdx.toFloat() / powerSteps) * 0.82f
                val impact = BallisticsEngine.predictShotImpact(
                    shooter = cpu,
                    target = target,
                    shotType = shotType,
                    angleDeg = angle,
                    power01 = p,
                    wind = wind,
                    weather = weather,
                    tornadoX = tornadoX,
                    terrain = terrainSnapshot
                )
                if (impact != null) {
                    val d = hypot(impact.x - target.x, impact.y - (target.y - 16f))
                    if (d < bestDist) {
                        bestDist = d
                        bestAngle = angle
                        bestPower = p
                    }
                }
            }
            angle += angleStep
        }

        // Apply difficulty jitter
        val jitteredAngle = (bestAngle + (Random.nextFloat() * 2f - 1f) * difficulty.angleJitter)
            .toInt()
            .coerceIn(minA, maxA)
        val jitteredPower = (bestPower + (Random.nextFloat() * 2f - 1f) * difficulty.powerJitter)
            .coerceIn(0.15f, 1.0f)

        val moveDir = if (Random.nextFloat() < 0.35f) {
            if (Random.nextBoolean()) 1 else -1
        } else 0

        return CpuShotPlan(
            angleDeg = jitteredAngle,
            power01 = jitteredPower,
            shotType = shotType,
            moveDir = moveDir
        )
    }
}
