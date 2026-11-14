package com.example.engine

import androidx.compose.ui.graphics.Color
import com.example.data.AvatarItem
import com.example.data.BattleItemId
import com.example.data.FlightStyle
import com.example.data.GameCatalog
import com.example.data.PlayerProfile
import com.example.data.SoundFxEngine
import com.example.data.WeatherType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

enum class MatchPhase {
    PLAYER_AIM,
    CHARGING,
    PROJECTILE_FLIGHT,
    CPU_THINKING,
    MATCH_OVER
}

data class MatchResultSummary(
    val playerWon: Boolean,
    val xpEarned: Int,
    val goldEarned: Int,
    val damageDealt: Int,
    val turnsElapsed: Int
)

data class BattleUiState(
    val terrain: TerrainMap,
    val terrainVersion: Int,
    val player: ActorState,
    val cpu: ActorState,
    val playerTurn: Boolean,
    val phase: MatchPhase,
    val turnSecondsLeft: Int,
    val wind: Vec2,
    val weather: WeatherType,
    val tornadoX: Float?,
    val forceBeamX: Float?,
    val projectiles: List<ActiveProjectile>,
    val explosions: List<ExplosionFx>,
    val floatingTexts: List<FloatingTextFx>,
    val availableItems: List<BattleItemId>,
    val selectedShot: ShotType,
    val chargePower: Float,
    val lastShotPower: Float?,
    val cameraFocus: Vec2,
    val isPaused: Boolean = false,
    val result: MatchResultSummary? = null,
    val statusBanner: String? = null
)

class MatchController(
    private val profile: PlayerProfile,
    private val miramoGroundRows: List<Int>,
    private val soundFx: SoundFxEngine,
    private val scope: CoroutineScope,
    private val onMatchFinished: (MatchResultSummary) -> Unit
) {
    private val mapSpec = GameCatalog.mapByIndex(profile.selectedMapIndex)
    private val terrain = TerrainMap(mapSpec, miramoGroundRows)

    private var nextProjId = 1
    private var turnElapsedSeconds = 0
    private var totalTurns = 0
    private var playerDamageDealt = 0
    private var tickJob: Job? = null
    private var cpuJob: Job? = null
    private var resultRecorded = false

    private val _state: MutableStateFlow<BattleUiState>
    val state: StateFlow<BattleUiState>

    init {
        val playerMobile = GameCatalog.mobileById(profile.selectedMobileId)
        val cpuMobile = GameCatalog.opponentFor(playerMobile.id)

        // Aggregate avatar + attribute bonuses for player
        val equippedAvatars = listOfNotNull(
            profile.equippedHead,
            profile.equippedBody,
            profile.equippedGlasses,
            profile.equippedFlag
        ).mapNotNull { id -> GameCatalog.avatars.firstOrNull { it.id == id } }

        val atkBonus = profile.attributes.attack + equippedAvatars.sumOf { it.attackBonus }
        val defBonus = profile.attributes.defense + equippedAvatars.sumOf { it.defenseBonus }
        val hpBonus = profile.attributes.health + equippedAvatars.sumOf { it.healthBonus }
        val digBonus = profile.attributes.dig + equippedAvatars.sumOf { it.digBonus }
        val shdBonus = profile.attributes.shieldRegen + equippedAvatars.sumOf { it.shieldBonus }

        val spawnLeftX = Random.nextInt(190, 420).toFloat()
        val spawnRightX = Random.nextInt(1180, 1410).toFloat()
        val playerOnLeft = profile.playerTeamA
        val px = if (playerOnLeft) spawnLeftX else spawnRightX
        val cx = if (playerOnLeft) spawnRightX else spawnLeftX

        val py = terrain.findSurfaceY(px) ?: 500f
        val cy = terrain.findSurfaceY(cx) ?: 500f

        val playerMaxHp = (playerMobile.baseHp * (1f + hpBonus * 0.008f)).toInt()
        val playerMaxShield = playerMobile.baseShield + shdBonus * 3

        val playerActor = ActorState(
            id = "player",
            name = profile.nickname,
            isPlayer = true,
            isTeamA = profile.playerTeamA,
            mobile = playerMobile,
            x = px,
            y = py,
            facingRight = playerOnLeft,
            slopeDeg = terrain.slopeAngleDeg(px, py),
            aimAngleDeg = ((playerMobile.minAngle + playerMobile.maxAngle) / 2),
            hp = playerMaxHp,
            maxHp = playerMaxHp,
            shield = playerMaxShield,
            maxShield = playerMaxShield,
            delay = 0,
            attackMod = 1f + atkBonus * 0.008f,
            defenseMod = (1f - defBonus * 0.006f).coerceAtLeast(0.6f),
            digMod = 1f + digBonus * 0.007f,
            shieldRegenBonus = shdBonus,
            ssCooldownTurns = 2
        )

        val cpuActor = ActorState(
            id = "cpu",
            name = "CPU ${cpuMobile.name}",
            isPlayer = false,
            isTeamA = !profile.playerTeamA,
            mobile = cpuMobile,
            x = cx,
            y = cy,
            facingRight = !playerOnLeft,
            slopeDeg = terrain.slopeAngleDeg(cx, cy),
            aimAngleDeg = ((cpuMobile.minAngle + cpuMobile.maxAngle) / 2),
            hp = cpuMobile.baseHp,
            maxHp = cpuMobile.baseHp,
            shield = cpuMobile.baseShield,
            maxShield = cpuMobile.baseShield,
            delay = 0,
            ssCooldownTurns = 2
        )

        // Randomized opening turn per BATTLE_RULES_AUDIT
        val playerStarts = Random.nextBoolean()
        val initWind = randomWind()
        val initState = BattleUiState(
            terrain = terrain,
            terrainVersion = terrain.version,
            player = playerActor,
            cpu = cpuActor,
            playerTurn = playerStarts,
            phase = if (playerStarts) MatchPhase.PLAYER_AIM else MatchPhase.CPU_THINKING,
            turnSecondsLeft = 30,
            wind = initWind,
            weather = WeatherType.NORMAL,
            tornadoX = null,
            forceBeamX = null,
            projectiles = emptyList(),
            explosions = emptyList(),
            floatingTexts = emptyList(),
            availableItems = profile.equippedItems.toList(),
            selectedShot = ShotType.T1,
            chargePower = 0f,
            lastShotPower = null,
            cameraFocus = Vec2(if (playerStarts) px else cx, if (playerStarts) py else cy),
            statusBanner = if (playerStarts) "Your Turn!" else "${cpuActor.name}'s Turn"
        )
        _state = MutableStateFlow(initState)
        state = _state.asStateFlow()

        startLoop()
        if (!playerStarts) {
            triggerCpuTurn()
        }
    }

    fun dispose() {
        tickJob?.cancel()
        cpuJob?.cancel()
    }

    fun togglePause() {
        val cur = _state.value
        if (cur.phase == MatchPhase.MATCH_OVER) return
        _state.value = cur.copy(isPaused = !cur.isPaused)
    }

    fun movePlayer(dir: Int) {
        val cur = _state.value
        if (cur.isPaused || cur.phase != MatchPhase.PLAYER_AIM || !cur.playerTurn) return
        val p = cur.player
        val facingRight = dir > 0
        if (p.facingRight != facingRight) {
            _state.value = cur.copy(player = p.copy(facingRight = facingRight))
            return
        }
        if (p.moveFuel <= 0f) return
        val step = dir * (p.mobile.moveSpeed * 0.12f)
        val nx = (p.x + step).coerceIn(36f, TerrainMap.WORLD_W - 36f)
        val ny = terrain.findSurfaceY(nx, (p.y - 26f).coerceAtLeast(0f))
        if (ny != null && abs(ny - p.y) <= 24f) {
            val slope = terrain.slopeAngleDeg(nx, ny)
            val updated = p.copy(
                x = nx,
                y = ny,
                slopeDeg = slope,
                moveFuel = (p.moveFuel - 3.5f).coerceAtLeast(0f)
            )
            _state.value = cur.copy(player = updated, cameraFocus = Vec2(nx, ny))
        }
    }

    fun adjustAngle(delta: Int) {
        val cur = _state.value
        if (cur.isPaused || !cur.playerTurn || (cur.phase != MatchPhase.PLAYER_AIM && cur.phase != MatchPhase.CHARGING)) return
        val p = cur.player
        val newAngle = (p.aimAngleDeg + delta).coerceIn(p.mobile.minAngle, p.mobile.maxAngle)
        _state.value = cur.copy(player = p.copy(aimAngleDeg = newAngle))
    }

    fun selectShotType(shotType: ShotType) {
        val cur = _state.value
        if (cur.isPaused || cur.phase != MatchPhase.PLAYER_AIM || !cur.playerTurn) return
        if (shotType == ShotType.SS && cur.player.ssCooldownTurns > 0) return
        soundFx.playClick()
        _state.value = cur.copy(selectedShot = shotType)
    }

    fun useItem(slotIndex: Int) {
        val cur = _state.value
        if (cur.isPaused || !cur.playerTurn || cur.phase != MatchPhase.PLAYER_AIM) return
        if (cur.weather == WeatherType.IGNORANCE) {
            addFloatingText(cur.player.x, cur.player.y - 40f, "ITEM LOCKED!", Color(0xFFEF4444))
            return
        }
        val item = cur.availableItems.getOrNull(slotIndex) ?: return
        if (cur.player.activeTurnItem != null) return

        soundFx.playClick()
        val remaining = cur.availableItems.toMutableList().apply { removeAt(slotIndex) }
        var updatedPlayer = cur.player
        var updatedWind = cur.wind
        var banner: String? = "${item.title} Activated!"

        when (item) {
            BattleItemId.ENERGY_UP_1 -> {
                val heal = 180
                updatedPlayer = updatedPlayer.copy(
                    hp = (updatedPlayer.hp + heal).coerceAtMost(updatedPlayer.maxHp),
                    delay = updatedPlayer.delay + item.turnDelay
                )
                addFloatingText(updatedPlayer.x, updatedPlayer.y - 45f, "+$heal HP", Color(0xFF34D399))
            }
            BattleItemId.ENERGY_UP_2 -> {
                val heal = 320
                updatedPlayer = updatedPlayer.copy(
                    hp = (updatedPlayer.hp + heal).coerceAtMost(updatedPlayer.maxHp),
                    delay = updatedPlayer.delay + item.turnDelay
                )
                addFloatingText(updatedPlayer.x, updatedPlayer.y - 45f, "+$heal HP", Color(0xFF34D399))
            }
            BattleItemId.CHANGE_WIND -> {
                updatedWind = randomWind()
                updatedPlayer = updatedPlayer.copy(delay = updatedPlayer.delay + item.turnDelay)
                banner = "Wind Shifted!"
            }
            BattleItemId.BLOOD -> {
                val costHp = (updatedPlayer.maxHp * 0.15f).toInt().coerceAtLeast(1)
                updatedPlayer = updatedPlayer.copy(
                    hp = (updatedPlayer.hp - costHp).coerceAtLeast(1),
                    activeTurnItem = item
                )
                addFloatingText(updatedPlayer.x, updatedPlayer.y - 45f, "-$costHp HP (+50% ATK)", Color(0xFFEF4444))
            }
            else -> {
                updatedPlayer = updatedPlayer.copy(activeTurnItem = item)
            }
        }

        _state.value = _state.value.copy(
            player = updatedPlayer,
            availableItems = remaining,
            wind = updatedWind,
            statusBanner = banner
        )
    }

    fun startSliceCharge() {
        val cur = _state.value
        if (cur.isPaused || !cur.playerTurn || cur.phase != MatchPhase.PLAYER_AIM) return
        _state.value = cur.copy(phase = MatchPhase.CHARGING, chargePower = 0.04f)
    }

    fun releaseSliceCharge() {
        val cur = _state.value
        if (cur.phase != MatchPhase.CHARGING) return
        if (cur.isPaused) {
            _state.value = cur.copy(phase = MatchPhase.PLAYER_AIM, chargePower = 0f)
            return
        }
        firePlayerShot(cur.chargePower)
    }

    fun updateDragAim(angleDeg: Int, power01: Float) {
        val cur = _state.value
        if (cur.isPaused || !cur.playerTurn || cur.phase != MatchPhase.PLAYER_AIM) return
        val clampedAngle = angleDeg.coerceIn(cur.player.mobile.minAngle, cur.player.mobile.maxAngle)
        _state.value = cur.copy(
            player = cur.player.copy(aimAngleDeg = clampedAngle),
            chargePower = power01.coerceIn(0.05f, 1.0f)
        )
    }

    fun commitDragShot(power01: Float) {
        val cur = _state.value
        if (cur.isPaused || !cur.playerTurn || cur.phase != MatchPhase.PLAYER_AIM) return
        firePlayerShot(power01)
    }

    fun passTurn() {
        val cur = _state.value
        if (cur.isPaused || !cur.playerTurn || cur.phase != MatchPhase.PLAYER_AIM) return
        soundFx.playClick()
        val p = cur.player.copy(delay = cur.player.delay + 200, moveFuel = 100f, activeTurnItem = null)
        addFloatingText(p.x, p.y - 40f, "PASS (+200)", Color(0xFFA5B8D4))
        _state.value = cur.copy(player = p, chargePower = 0f)
        advanceTurn()
    }

    private fun firePlayerShot(power01: Float) {
        val cur = _state.value
        val p = cur.player
        val clamped = power01.coerceIn(0.06f, 1.0f)
        val shotType = cur.selectedShot
        val shotCost = when (shotType) {
            ShotType.T1 -> p.mobile.t1Delay
            ShotType.T2 -> p.mobile.t2Delay
            ShotType.SS -> p.mobile.ssDelay
        }
        val itemCost = p.activeTurnItem?.turnDelay ?: 0
        val newDelay = p.delay + shotCost + itemCost + turnElapsedSeconds * 10

        val projs = BallisticsEngine.spawnShotProjectiles(p, shotType, clamped, nextProjId)
        nextProjId += projs.size

        soundFx.playLaunch(heavy = shotType == ShotType.SS)

        val updatedPlayer = p.copy(
            delay = newDelay,
            moveFuel = 100f,
            activeTurnItem = null,
            ssCooldownTurns = if (shotType == ShotType.SS) 3 else (p.ssCooldownTurns - 1).coerceAtLeast(0)
        )

        _state.value = cur.copy(
            player = updatedPlayer,
            phase = MatchPhase.PROJECTILE_FLIGHT,
            projectiles = projs,
            chargePower = 0f,
            lastShotPower = clamped,
            selectedShot = if (shotType == ShotType.SS) ShotType.T1 else shotType,
            statusBanner = null
        )
    }

    private fun triggerCpuTurn() {
        cpuJob?.cancel()
        cpuJob = scope.launch {
            delay(900L)
            val cur = _state.value
            if (cur.phase == MatchPhase.MATCH_OVER) return@launch
            val snapshot = terrain.snapshot()
            val plan = withContext(Dispatchers.Default) {
                CpuPlanner.searchShot(
                    cpu = cur.cpu,
                    target = cur.player,
                    wind = cur.wind,
                    weather = cur.weather,
                    tornadoX = cur.tornadoX,
                    terrainSnapshot = snapshot,
                    difficulty = profile.cpuDifficulty
                )
            }

            //Wait if paused
            while (_state.value.isPaused) {
                delay(150L)
            }

            val latest = _state.value
            if (latest.phase == MatchPhase.MATCH_OVER) return@launch
            var cpuActor = latest.cpu

            // Optional CPU step movement
            if (plan.moveDir != 0) {
                val nx = (cpuActor.x + plan.moveDir * 32f).coerceIn(40f, TerrainMap.WORLD_W - 40f)
                val ny = terrain.findSurfaceY(nx, (cpuActor.y - 24f).coerceAtLeast(0f))
                if (ny != null && abs(ny - cpuActor.y) <= 20f) {
                    cpuActor = cpuActor.copy(x = nx, y = ny, slopeDeg = terrain.slopeAngleDeg(nx, ny))
                }
            }

            val faceRight = latest.player.x > cpuActor.x
            cpuActor = cpuActor.copy(
                facingRight = faceRight,
                aimAngleDeg = plan.angleDeg
            )

            val shotCost = when (plan.shotType) {
                ShotType.T1 -> cpuActor.mobile.t1Delay
                ShotType.T2 -> cpuActor.mobile.t2Delay
                ShotType.SS -> cpuActor.mobile.ssDelay
            }
            val newDelay = cpuActor.delay + shotCost + 20 // fixed 2s CPU think cost

            val projs = BallisticsEngine.spawnShotProjectiles(cpuActor, plan.shotType, plan.power01, nextProjId)
            nextProjId += projs.size
            soundFx.playLaunch(heavy = plan.shotType == ShotType.SS)

            _state.value = latest.copy(
                cpu = cpuActor.copy(
                    delay = newDelay,
                    ssCooldownTurns = if (plan.shotType == ShotType.SS) 3 else (cpuActor.ssCooldownTurns - 1).coerceAtLeast(0)
                ),
                phase = MatchPhase.PROJECTILE_FLIGHT,
                projectiles = projs,
                statusBanner = "${cpuActor.name} fires Shot ${plan.shotType.label}!"
            )
        }
    }

    private fun startLoop() {
        tickJob?.cancel()
        tickJob = scope.launch {
            var secondAccum = 0f
            while (true) {
                delay(50L) // 20 Hz fixed step
                val cur = _state.value
                if (cur.isPaused || cur.phase == MatchPhase.MATCH_OVER) continue

                // Advance explosions & floating text FX
                val updatedExplosions = cur.explosions
                    .map { it.copy(age = it.age + BallisticsEngine.DT) }
                    .filter { it.age < it.maxAge }
                val updatedTexts = cur.floatingTexts
                    .map { it.copy(age = it.age + BallisticsEngine.DT, y = it.y - 22f * BallisticsEngine.DT) }
                    .filter { it.age < it.maxAge }

                var nextState = cur.copy(
                    explosions = updatedExplosions,
                    floatingTexts = updatedTexts
                )

                // Slice charging: 0 -> 1.0 in ~2.5s (original 2-3s curve)
                if (nextState.phase == MatchPhase.CHARGING) {
                    val np = nextState.chargePower + 0.02f
                    if (np >= 1.0f) {
                        _state.value = nextState.copy(chargePower = 1.0f)
                        firePlayerShot(1.0f)
                        continue
                    } else {
                        nextState = nextState.copy(chargePower = np)
                    }
                }

                // Turn countdown timer during PLAYER_AIM
                if (nextState.phase == MatchPhase.PLAYER_AIM) {
                    secondAccum += BallisticsEngine.DT
                    if (secondAccum >= 1.0f) {
                        secondAccum -= 1.0f
                        turnElapsedSeconds++
                        val left = (nextState.turnSecondsLeft - 1).coerceAtLeast(0)
                        if (left <= 0) {
                            _state.value = nextState.copy(turnSecondsLeft = 0)
                            passTurn()
                            continue
                        } else {
                            nextState = nextState.copy(turnSecondsLeft = left)
                        }
                    }
                } else {
                    secondAccum = 0f
                }

                // Projectile simulation
                if (nextState.phase == MatchPhase.PROJECTILE_FLIGHT) {
                    nextState = stepProjectiles(nextState)
                }

                // Gravity check on actors (falling after terrain crater / bunge!)
                nextState = applyActorGravity(nextState)

                // Check Victory/Defeat
                if (!nextState.player.isAlive || !nextState.cpu.isAlive) {
                    val playerWon = nextState.player.isAlive && !nextState.cpu.isAlive
                    val xp = if (playerWon) 140 + (playerDamageDealt / 12) else 45 + (playerDamageDealt / 25)
                    val gold = if (playerWon) 280 + (playerDamageDealt / 8) else 95 + (playerDamageDealt / 20)
                    val summary = MatchResultSummary(
                        playerWon = playerWon,
                        xpEarned = xp,
                        goldEarned = gold,
                        damageDealt = playerDamageDealt,
                        turnsElapsed = totalTurns
                    )
                    if (playerWon) soundFx.playVictory() else soundFx.playDefeat()
                    _state.value = nextState.copy(
                        phase = MatchPhase.MATCH_OVER,
                        result = summary,
                        projectiles = emptyList()
                    )
                    if (!resultRecorded) {
                        resultRecorded = true
                        onMatchFinished(summary)
                    }
                    continue
                }

                // If all projectiles resolved, transition to next turn!
                if (nextState.phase == MatchPhase.PROJECTILE_FLIGHT && nextState.projectiles.isEmpty()) {
                    _state.value = nextState
                    advanceTurn()
                } else {
                    _state.value = nextState
                }
            }
        }
    }

    private fun stepProjectiles(cur: BattleUiState): BattleUiState {
        var player = cur.player
        var cpu = cur.cpu
        val activeList = cur.projectiles.toMutableList()
        val spawned = mutableListOf<ActiveProjectile>()
        val newExplosions = cur.explosions.toMutableList()
        val newTexts = cur.floatingTexts.toMutableList()
        var focus = cur.cameraFocus

        for (proj in activeList) {
            if (!proj.alive) continue
            if (proj.delayStartTicks > 0) {
                proj.delayStartTicks--
                continue
            }
            proj.ageTicks++

            // Turtle SS splits into 6 droplets near apex
            if (proj.mobile.id == 12 && proj.shotType == ShotType.SS && !proj.hasSplit && !proj.isChildSplit && proj.vy > 10f && proj.ageTicks >= 14) {
                proj.hasSplit = true
                proj.alive = false
                for (i in 0 until 6) {
                    val spreadVx = proj.vx + (i - 2.5f) * 55f
                    val spreadVy = proj.vy - 65f + (i % 2) * 30f
                    spawned.add(
                        ActiveProjectile(
                            id = nextProjId++,
                            ownerIsPlayer = proj.ownerIsPlayer,
                            mobile = proj.mobile,
                            shotType = proj.shotType,
                            x = proj.x, y = proj.y,
                            vx = spreadVx, vy = spreadVy,
                            damage = (proj.damage / 5.5f).toInt().coerceAtLeast(35),
                            craterRadius = proj.craterRadius * 0.65f,
                            color = Color(0xFF67E8F9),
                            isChildSplit = true
                        )
                    )
                }
                continue
            }

            // Crawler mine behavior (Raon T2)
            if (proj.crawlerTicksLeft > 0 && terrain.isSolidWorld(proj.x, proj.y + 3f)) {
                proj.crawlerTicksLeft--
                val targetX = if (proj.ownerIsPlayer) cpu.x else player.x
                val dir = if (targetX >= proj.x) 1f else -1f
                val nx = (proj.x + dir * 4.5f).coerceIn(20f, TerrainMap.WORLD_W - 20f)
                val ny = terrain.findSurfaceY(nx, (proj.y - 20f).coerceAtLeast(0f)) ?: proj.y
                proj.x = nx
                proj.y = ny - 3f
                proj.trail.add(Vec2(proj.x, proj.y))
                if (proj.trail.size > 16) proj.trail.removeAt(0)
                focus = Vec2(proj.x, proj.y)

                val hitTarget = checkActorDirectHit(proj.x, proj.y, player, cpu, proj.ageTicks, proj.ownerIsPlayer)
                if (hitTarget != null || proj.crawlerTicksLeft <= 0) {
                    detonate(proj, player, cpu, cur.weather, cur.forceBeamX, newExplosions, newTexts).also { (p, c) ->
                        player = p; cpu = c
                    }
                    proj.alive = false
                }
                continue
            }

            // Out of bounds
            if (proj.x < -80f || proj.x > TerrainMap.WORLD_W + 80f || proj.y > TerrainMap.WORLD_H + 60f) {
                proj.alive = false
                continue
            }

            val windScale = if (proj.mobile.flightStyle == FlightStyle.BOOMER_WIND) 1.75f else 1.0f

            if (proj.isBurrowed) {
                // Nak T2 climbs upward inside ground at 2x gravity and detonates on exiting into air
                proj.vy -= BallisticsEngine.GRAVITY * 1.9f * BallisticsEngine.DT
                proj.x += proj.vx * 0.7f * BallisticsEngine.DT
                proj.y += proj.vy * BallisticsEngine.DT
                proj.trail.add(Vec2(proj.x, proj.y))
                if (proj.trail.size > 16) proj.trail.removeAt(0)
                focus = Vec2(proj.x, proj.y)

                val hitActor = checkActorDirectHit(proj.x, proj.y, player, cpu, proj.ageTicks, proj.ownerIsPlayer)
                if (hitActor != null || !terrain.isSolidWorld(proj.x, proj.y)) {
                    detonate(proj, player, cpu, cur.weather, cur.forceBeamX, newExplosions, newTexts).also { (p, c) ->
                        player = p; cpu = c
                    }
                    proj.alive = false
                }
                continue
            }

            // Standard 20 Hz integration
            proj.vx += cur.wind.x * windScale * BallisticsEngine.DT
            proj.vy += (BallisticsEngine.GRAVITY + cur.wind.y * windScale) * BallisticsEngine.DT

            // Boomer SS hook curve
            if (proj.mobile.id == 6 && proj.shotType == ShotType.SS && proj.ageTicks > 12) {
                proj.vx -= (if (proj.vx > 0) 1f else -1f) * 140f * BallisticsEngine.DT
            }

            // Tornado deflection
            if (cur.weather == WeatherType.TORNADO && cur.tornadoX != null && abs(proj.x - cur.tornadoX) < 38f) {
                proj.vx *= 0.88f
                proj.vy -= 420f * BallisticsEngine.DT
            }

            proj.x += proj.vx * BallisticsEngine.DT
            proj.y += proj.vy * BallisticsEngine.DT

            // Turtle T2 / Mage T2 wave visual offset stored in trail
            val waveOffset = if (proj.mobile.flightStyle == FlightStyle.TURTLE_WAVE && proj.shotType == ShotType.T2 ||
                proj.mobile.flightStyle == FlightStyle.MAGE_HELIX && proj.shotType == ShotType.T2
            ) {
                val amp = if (proj.ageTicks <= 10) proj.ageTicks * 1.6f else (16f - (proj.ageTicks - 10) * 0.35f).coerceAtLeast(4f)
                sin(proj.ageTicks * 0.55f + proj.wavePhaseOffset) * amp
            } else 0f

            val drawX = proj.x
            val drawY = proj.y + waveOffset
            proj.trail.add(Vec2(drawX, drawY))
            if (proj.trail.size > 18) proj.trail.removeAt(0)
            focus = Vec2(drawX, drawY)

            // Direct actor hit check
            val hitActor = checkActorDirectHit(proj.x, proj.y, player, cpu, proj.ageTicks, proj.ownerIsPlayer)
            if (hitActor != null) {
                detonate(proj, player, cpu, cur.weather, cur.forceBeamX, newExplosions, newTexts).also { (p, c) ->
                    player = p; cpu = c
                }
                proj.alive = false
                continue
            }

            // Terrain collision
            if (!proj.isGhost && proj.ageTicks > 2 && terrain.isSolidWorld(proj.x, proj.y)) {
                if (proj.mobile.id == 3 && proj.shotType == ShotType.T2) {
                    // Nak T2 enters burrow!
                    proj.isBurrowed = true
                    proj.vy = -85f
                    continue
                }
                if (proj.bouncesLeft > 0) {
                    proj.bouncesLeft--
                    proj.vy = -abs(proj.vy) * 0.62f - 60f
                    proj.vx *= 0.78f
                    proj.y -= 6f
                    soundFx.playClick()
                    continue
                }
                if (proj.crawlerTicksLeft > 0) {
                    continue
                }
                if (proj.isTeleportBeacon) {
                    val safeY = terrain.findSurfaceY(proj.x, (proj.y - 16f).coerceAtLeast(0f)) ?: proj.y
                    if (proj.ownerIsPlayer) {
                        player = player.copy(x = proj.x.coerceIn(30f, TerrainMap.WORLD_W - 30f), y = safeY, slopeDeg = terrain.slopeAngleDeg(proj.x, safeY))
                        newTexts.add(FloatingTextFx(player.x, player.y - 36f, "WARP!", Color(0xFF38BDF8)))
                    }
                    proj.alive = false
                    continue
                }

                detonate(proj, player, cpu, cur.weather, cur.forceBeamX, newExplosions, newTexts).also { (p, c) ->
                    player = p; cpu = c
                }
                proj.alive = false
            }
        }

        val remaining = activeList.filter { it.alive } + spawned
        return cur.copy(
            terrainVersion = terrain.version,
            player = player,
            cpu = cpu,
            projectiles = remaining,
            explosions = newExplosions,
            floatingTexts = newTexts,
            cameraFocus = focus
        )
    }

    private fun checkActorDirectHit(
        px: Float,
        py: Float,
        player: ActorState,
        cpu: ActorState,
        ageTicks: Int,
        ownerIsPlayer: Boolean
    ): ActorState? {
        if (ageTicks > 3 || !ownerIsPlayer) {
            if (player.isAlive && hypot(px - player.x, py - (player.y - 18f)) <= 24f) return player
        }
        if (ageTicks > 3 || ownerIsPlayer) {
            if (cpu.isAlive && hypot(px - cpu.x, py - (cpu.y - 18f)) <= 24f) return cpu
        }
        return null
    }

    private fun detonate(
        proj: ActiveProjectile,
        playerIn: ActorState,
        cpuIn: ActorState,
        weather: WeatherType,
        forceBeamX: Float?,
        explosions: MutableList<ExplosionFx>,
        texts: MutableList<FloatingTextFx>
    ): Pair<ActorState, ActorState> {
        var player = playerIn
        var cpu = cpuIn

        terrain.carveCrater(proj.x, proj.y, proj.craterRadius)
        soundFx.playExplosion(large = proj.shotType == ShotType.SS || proj.craterRadius > 40f)

        val isOrbitalStrike = proj.isThunderCall ||
            weather == WeatherType.THOR ||
            weather == WeatherType.LIGHTNING ||
            (proj.mobile.flightStyle == FlightStyle.ORBITAL_BEAM && proj.shotType != ShotType.T1) ||
            (proj.mobile.flightStyle == FlightStyle.ADUKA_THOR && proj.shotType != ShotType.T1) ||
            (proj.mobile.flightStyle == FlightStyle.LIGHTNING_SKY && proj.shotType != ShotType.T1)

        explosions.add(
            ExplosionFx(
                x = proj.x,
                y = proj.y,
                radius = proj.craterRadius * 1.35f,
                color = proj.color,
                lightningBeamX = if (isOrbitalStrike) proj.x else null
            )
        )

        val splashRadius = proj.craterRadius * 1.95f
        val forceMult = if (weather == WeatherType.FORCE && forceBeamX != null && abs(proj.x - forceBeamX) < 160f) 1.5f else 1f
        val orbitalBonus = if (proj.isThunderCall || weather == WeatherType.THOR) 1.25f else 1f

        fun applyHit(actor: ActorState): ActorState {
            val d = hypot(proj.x - actor.x, proj.y - (actor.y - 16f))
            if (d > splashRadius) return actor
            val falloff = (1f - (d / splashRadius) * 0.65f).coerceIn(0.35f, 1f)
            val armorMult = if (actor.armorBroken) 1.2f else 1f
            val rawDmg = (proj.damage * falloff * forceMult * orbitalBonus * actor.defenseMod * armorMult).toInt().coerceAtLeast(8)

            // Mage SS EMP drains shield completely!
            val shieldCleared = if (proj.mobile.id == 1 && proj.shotType == ShotType.SS) 0 else (actor.shield - rawDmg).coerceAtLeast(0)
            val shieldAbsorbed = actor.shield - shieldCleared
            val hpDmg = (rawDmg - shieldAbsorbed).coerceAtLeast(0)
            val newHp = (actor.hp - hpDmg).coerceAtLeast(0)

            if (!actor.isPlayer && proj.ownerIsPlayer) {
                playerDamageDealt += rawDmg
            }
            val color = if (actor.isPlayer) Color(0xFFFF5C5C) else Color(0xFFFACC15)
            texts.add(FloatingTextFx(actor.x, actor.y - 42f, "-$rawDmg", color))
            val breakArmor = actor.armorBroken || (proj.mobile.id == 11 && proj.shotType == ShotType.T2)
            return actor.copy(hp = newHp, shield = shieldCleared, armorBroken = breakArmor)
        }

        player = applyHit(player)
        cpu = applyHit(cpu)
        return player to cpu
    }

    private fun applyActorGravity(cur: BattleUiState): BattleUiState {
        fun settle(actor: ActorState): ActorState {
            if (!actor.isAlive) return actor
            val surface = terrain.findSurfaceY(actor.x, (actor.y - 12f).coerceAtLeast(0f))
            return if (surface == null) {
                // Fell into void (Bunge!)
                val ny = actor.y + 18f
                if (ny > TerrainMap.WORLD_H + 30f) {
                    actor.copy(y = ny, hp = 0)
                } else {
                    actor.copy(y = ny)
                }
            } else if (surface > actor.y + 4f) {
                val ny = (actor.y + 12f).coerceAtMost(surface)
                actor.copy(y = ny, slopeDeg = terrain.slopeAngleDeg(actor.x, ny))
            } else {
                actor.copy(y = surface, slopeDeg = terrain.slopeAngleDeg(actor.x, surface))
            }
        }
        return cur.copy(player = settle(cur.player), cpu = settle(cur.cpu))
    }

    private fun advanceTurn() {
        val cur = _state.value
        if (cur.phase == MatchPhase.MATCH_OVER) return

        totalTurns++
        turnElapsedSeconds = 0

        // Shield regeneration per turn (Mage / Shield mobiles)
        fun regen(actor: ActorState): ActorState {
            if (!actor.isAlive || actor.maxShield <= 0) return actor
            val r = actor.mobile.shieldRegen + actor.shieldRegenBonus
            return actor.copy(shield = (actor.shield + r).coerceAtMost(actor.maxShield))
        }

        var p = regen(cur.player)
        var c = regen(cur.cpu)

        // Weather rotation every 3 turns
        var weather = cur.weather
        var tornadoX = cur.tornadoX
        var forceBeamX = cur.forceBeamX
        if (totalTurns % 3 == 0) {
            weather = WeatherType.entries.random()
            tornadoX = if (weather == WeatherType.TORNADO) Random.nextInt(450, 1150).toFloat() else null
            forceBeamX = if (weather == WeatherType.FORCE) Random.nextInt(500, 1100).toFloat() else null
        }

        // Moon heals both mobiles slightly
        if (weather == WeatherType.MOON) {
            p = p.copy(hp = (p.hp + 35).coerceAtMost(p.maxHp))
            c = c.copy(hp = (c.hp + 35).coerceAtMost(c.maxHp))
        }

        // Slight wind variation each turn
        val newWind = if (totalTurns % 2 == 0) randomWind() else cur.wind
        val playerNext = p.delay <= c.delay

        _state.value = cur.copy(
            player = p,
            cpu = c,
            playerTurn = playerNext,
            phase = if (playerNext) MatchPhase.PLAYER_AIM else MatchPhase.CPU_THINKING,
            turnSecondsLeft = 30,
            wind = newWind,
            weather = weather,
            tornadoX = tornadoX,
            forceBeamX = forceBeamX,
            chargePower = 0f,
            cameraFocus = Vec2(if (playerNext) p.x else c.x, if (playerNext) p.y else c.y),
            statusBanner = if (playerNext) "Your Turn!" else "${c.name}'s Turn"
        )

        if (!playerNext) {
            triggerCpuTurn()
        }
    }

    private fun addFloatingText(x: Float, y: Float, text: String, color: Color) {
        val cur = _state.value
        _state.value = cur.copy(
            floatingTexts = cur.floatingTexts + FloatingTextFx(x, y, text, color)
        )
    }

    private fun randomWind(): Vec2 {
        val angle = Random.nextFloat() * 2f * PI.toFloat()
        val strength = Random.nextFloat() * 26f
        return Vec2(cos(angle) * strength, sin(angle) * strength * 0.45f)
    }
}
