package com.example.data

import androidx.compose.ui.graphics.Color

enum class MobileCategory(val label: String) {
    MECHANIC("Machine"),
    SHIELD("Shield"),
    BIONIC("Bionic")
}

enum class FlightStyle {
    STANDARD,
    MULTI_SPREAD,
    MAGE_HELIX,
    NAK_BURROW,
    BOOMER_WIND,
    RAON_MINE,
    LIGHTNING_SKY,
    ORBITAL_BEAM,
    TURTLE_WAVE,
    GRUB_BOUNCE,
    DRAGON_VOLLEY,
    KNIGHT_SWORD,
    ADUKA_THOR
}

data class MobileSpec(
    val id: Int, // Preserved 1..16 (no ID 0!)
    val name: String,
    val title: String,
    val category: MobileCategory,
    val baseHp: Int,
    val baseShield: Int,
    val shieldRegen: Int,
    val moveSpeed: Float,
    val minAngle: Int,
    val maxAngle: Int,
    val t1Delay: Int,
    val t2Delay: Int,
    val ssDelay: Int,
    val t1Damage: Int,
    val t2Damage: Int,
    val ssDamage: Int,
    val t1Crater: Float,
    val t2Crater: Float,
    val ssCrater: Float,
    val t1Label: String,
    val t2Label: String,
    val ssLabel: String,
    val buttonAsset: String,
    val portraitIndex: Int,
    val bodyColor: Color,
    val accentColor: Color,
    val firesFromTail: Boolean = false,
    val isHovering: Boolean = false,
    val flightStyle: FlightStyle = FlightStyle.STANDARD
)

enum class WeatherType(val label: String, val badgeColor: Color) {
    NORMAL("Normal Wind", Color(0xFF36D8FF)),
    FORCE("Force (2x Power)", Color(0xFFF59E0B)),
    TORNADO("Tornado", Color(0xFF60A5FA)),
    LIGHTNING("Lightning", Color(0xFFFACC15)),
    THOR("Thor Satellite", Color(0xFF38BDF8)),
    MOON("Moon (Heal)", Color(0xFFA78BFA)),
    IGNORANCE("Ignorance (No Items)", Color(0xFFEF4444))
}

data class MapSideSpec(
    val index: Int, // 0..21 matching maps.png frames
    val worldName: String,
    val side: String, // "A" or "B"
    val skyTop: Color,
    val skyBottom: Color,
    val terrainTop: Color,
    val terrainFill: Color,
    val terrainRim: Color,
    val styleSeed: Int,
    val description: String
) {
    val displayName: String get() = "$worldName $side"
}

enum class BattleItemId(
    val title: String,
    val slots: Int,
    val rawCost: Int,
    val description: String,
    val hdAsset: String,
    val catalogAsset: String
) {
    DUAL("Dual", 2, 600, "Fire two shots in one turn (+delay).", "items/ItemDual.png", "classic/room/polish/catalog_dual.png"),
    DUAL_PLUS("Dual+", 2, 750, "Fire Shot 1 followed by Shot 2.", "items/ItemDualPlus.png", "classic/room/polish/catalog_dual_plus.png"),
    TELEPORT("Teleport", 2, 650, "Fire a beacon to warp to impact point.", "items/ItemTeleport.png", "classic/room/polish/catalog_teleport.png"),
    TEAM_TELEPORT("Team TP", 2, 850, "Precision warp beacon with shield restore.", "items/ItemTeamTeleport.png", "classic/room/polish/catalog_team_teleport.png"),
    THUNDER("Thunder", 2, 700, "Call a lightning bolt on impact.", "items/ItemThunder.png", "classic/room/polish/catalog_thunder.png"),
    ENERGY_UP_1("Energy 1", 1, 300, "Restore +180 HP immediately.", "items/ItemEnergyUp1.png", "classic/room/polish/catalog_energy_up_1.png"),
    ENERGY_UP_2("Energy 2", 2, 550, "Restore +320 HP immediately.", "items/ItemEnergyUp2.png", "classic/room/polish/catalog_energy_up_2.png"),
    POWER_UP("Power Up", 1, 350, "Boost shot damage by +30% this turn.", "items/ItemPowerUp.png", "classic/room/polish/catalog_power_up.png"),
    CHANGE_WIND("Wind Shift", 1, 250, "Randomize wind speed and direction.", "items/ItemChangeWind.png", "classic/room/polish/catalog_change_wind.png"),
    BUNGE_SHOT("Bunge Shot", 1, 350, "Expand terrain crater radius by +65%.", "items/ItemBungeShot.png", "classic/room/polish/catalog_bunge_shot.png"),
    BLOOD("Blood", 1, 300, "Sacrifice 15% HP for +50% shot damage.", "items/ItemBlood.png", "classic/room/polish/catalog_blood.png");

    val turnDelay: Int get() = (rawCost / 7.5f).toInt()
}

enum class AvatarSlot(val label: String) {
    HEAD("Head"),
    BODY("Body"),
    GLASSES("Glasses"),
    FLAG("Flag")
}

data class AvatarItem(
    val id: String,
    val name: String,
    val slot: AvatarSlot,
    val goldPrice: Int,
    val cashPrice: Int,
    val sheet: String,
    val regionX: Int,
    val regionY: Int,
    val regionW: Int,
    val regionH: Int,
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val healthBonus: Int = 0,
    val delayBonus: Int = 0,
    val digBonus: Int = 0,
    val shieldBonus: Int = 0,
    val popularityBonus: Int = 0
)

enum class CpuDifficulty(val label: String, val angleJitter: Float, val powerJitter: Float) {
    EASY("Easy", 8.5f, 0.11f),
    NORMAL("Normal", 4.0f, 0.055f),
    HARD("Hard", 1.3f, 0.02f)
}

enum class AimingMode(val label: String) {
    SLICE("Slice"),
    DRAG("Drag")
}

data class PlayerAttributes(
    val attack: Int = 0,
    val defense: Int = 0,
    val health: Int = 0,
    val itemDelay: Int = 0,
    val dig: Int = 0,
    val shieldRegen: Int = 0,
    val popularity: Int = 0
) {
    val spentPoints: Int get() = attack + defense + health + itemDelay + dig + shieldRegen + popularity
}

data class PlayerProfile(
    val nickname: String = "Commander",
    val gold: Int = 1000,
    val cash: Int = 1000,
    val xp: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val selectedMobileId: Int = 2, // Bigfoot default (ID 2)
    val selectedMapIndex: Int = 0, // Miramo Town A default
    val playerTeamA: Boolean = true,
    val cpuDifficulty: CpuDifficulty = CpuDifficulty.NORMAL,
    val equippedItems: List<BattleItemId> = listOf(
        BattleItemId.DUAL,
        BattleItemId.ENERGY_UP_1,
        BattleItemId.POWER_UP,
        BattleItemId.TELEPORT
    ),
    val ownedAvatars: Set<String> = emptySet(),
    val equippedHead: String? = null,
    val equippedBody: String? = null,
    val equippedGlasses: String? = null,
    val equippedFlag: String? = null,
    val attributes: PlayerAttributes = PlayerAttributes()
) {
    val rankIndex: Int
        get() = when {
            xp < 100 -> 0
            xp < 300 -> 1
            xp < 600 -> 2
            xp < 1000 -> 3
            xp < 1500 -> 4
            xp < 2200 -> 5
            xp < 3000 -> 6
            xp < 4000 -> 7
            xp < 5500 -> 8
            xp < 7500 -> 9
            else -> (10 + (xp - 7500) / 2500).coerceAtMost(24)
        }

    val totalAttributePoints: Int get() = ((rankIndex + 1) * 10).coerceAtMost(180)
    val availableAttributePoints: Int get() = (totalAttributePoints - attributes.spentPoints).coerceAtLeast(0)
}

data class GameOptions(
    val sfxEnabled: Boolean = true,
    val aimingMode: AimingMode = AimingMode.SLICE,
    val scrollSpeed: Float = 1.0f,
    val showBackgroundEffects: Boolean = true
)
