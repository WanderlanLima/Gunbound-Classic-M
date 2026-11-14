package com.example.data

import androidx.compose.ui.graphics.Color

object GameCatalog {

    // All 16 classic GunBound Thor's Hammer mobiles preserving Gunbound Recomp IDs 1..16 (NO ID 0)
    val mobiles: List<MobileSpec> = listOf(
        MobileSpec(
            id = 1, name = "Mage", title = "Arcane Shield Tank",
            category = MobileCategory.SHIELD,
            baseHp = 730, baseShield = 270, shieldRegen = 30,
            moveSpeed = 62f, minAngle = 15, maxAngle = 60,
            t1Delay = 730 / 10 + 180, t2Delay = 920 / 10 + 230, ssDelay = 1280 / 10 + 310,
            t1Damage = 175, t2Damage = 255, ssDamage = 330,
            t1Crater = 26f, t2Crater = 32f, ssCrater = 46f,
            t1Label = "Arcane Bolt", t2Label = "Dual Helix Laser", ssLabel = "EMP Shield Breaker",
            buttonAsset = "classic/room/b_ready_mobile02.png", portraitIndex = 1,
            bodyColor = Color(0xFF8B5CF6), accentColor = Color(0xFF38BDF8),
            isHovering = true, flightStyle = FlightStyle.MAGE_HELIX
        ),
        MobileSpec(
            id = 2, name = "Bigfoot", title = "Heavy Missile Walker",
            category = MobileCategory.MECHANIC,
            baseHp = 1100, baseShield = 0, shieldRegen = 0,
            moveSpeed = 54f, minAngle = 10, maxAngle = 55,
            t1Delay = 250, t2Delay = 310, ssDelay = 420,
            t1Damage = 190, t2Damage = 270, ssDamage = 390,
            t1Crater = 34f, t2Crater = 40f, ssCrater = 54f,
            t1Label = "4-Bomb Spread", t2Label = "2x3 Missile Barrage", ssLabel = "8-Rocket Salvo",
            buttonAsset = "classic/room/b_ready_mobile05.png", portraitIndex = 4,
            bodyColor = Color(0xFFEA580C), accentColor = Color(0xFFFACC15),
            flightStyle = FlightStyle.MULTI_SPREAD
        ),
        MobileSpec(
            id = 3, name = "Nak", title = "Subterranean Burrower",
            category = MobileCategory.MECHANIC,
            baseHp = 1000, baseShield = 0, shieldRegen = 0,
            moveSpeed = 58f, minAngle = 10, maxAngle = 55,
            t1Delay = 240, t2Delay = 300, ssDelay = 410,
            t1Damage = 185, t2Damage = 295, ssDamage = 380,
            t1Crater = 30f, t2Crater = 38f, ssCrater = 42f,
            t1Label = "Tail Shell", t2Label = "Burrow Shot", ssLabel = "Ghost Pierce",
            buttonAsset = "classic/room/b_ready_mobile03.png", portraitIndex = 2,
            bodyColor = Color(0xFF16A34A), accentColor = Color(0xFF86EFAC),
            firesFromTail = true, flightStyle = FlightStyle.NAK_BURROW
        ),
        MobileSpec(
            id = 4, name = "Armor", title = "Classic Heavy Cannon",
            category = MobileCategory.MECHANIC,
            baseHp = 1050, baseShield = 0, shieldRegen = 0,
            moveSpeed = 55f, minAngle = 10, maxAngle = 55,
            t1Delay = 245, t2Delay = 305, ssDelay = 415,
            t1Damage = 195, t2Damage = 280, ssDamage = 400,
            t1Crater = 32f, t2Crater = 38f, ssCrater = 58f,
            t1Label = "Heavy Shell", t2Label = "Twin Armor Rocket", ssLabel = "Mega Bunker Buster",
            buttonAsset = "classic/room/b_ready_mobile01.png", portraitIndex = 0,
            bodyColor = Color(0xFF2563EB), accentColor = Color(0xFFF59E0B),
            flightStyle = FlightStyle.MULTI_SPREAD
        ),
        MobileSpec(
            id = 5, name = "Trico", title = "Tri-Volley Bio Walker",
            category = MobileCategory.BIONIC,
            baseHp = 1000, baseShield = 0, shieldRegen = 0,
            moveSpeed = 60f, minAngle = 15, maxAngle = 60,
            t1Delay = 240, t2Delay = 300, ssDelay = 410,
            t1Damage = 180, t2Damage = 310, ssDamage = 370,
            t1Crater = 28f, t2Crater = 34f, ssCrater = 50f,
            t1Label = "Cabbage Bomb", t2Label = "3-Way Spin Volley", ssLabel = "Giant Plasma Core",
            buttonAsset = "classic/room/b_ready_mobile04.png", portraitIndex = 3,
            bodyColor = Color(0xFF10B981), accentColor = Color(0xFFFBBF24),
            flightStyle = FlightStyle.MULTI_SPREAD
        ),
        MobileSpec(
            id = 6, name = "Boomer", title = "Wind-Master Boomerang",
            category = MobileCategory.BIONIC,
            baseHp = 950, baseShield = 0, shieldRegen = 0,
            moveSpeed = 64f, minAngle = 15, maxAngle = 80,
            t1Delay = 230, t2Delay = 290, ssDelay = 400,
            t1Damage = 200, t2Damage = 285, ssDamage = 395,
            t1Crater = 24f, t2Crater = 28f, ssCrater = 38f,
            t1Label = "Boomerang", t2Label = "Triple Boomerang", ssLabel = "Golden Hook",
            buttonAsset = "classic/room/b_ready_mobile06.png", portraitIndex = 5,
            bodyColor = Color(0xFFD97706), accentColor = Color(0xFFFEF08A),
            flightStyle = FlightStyle.BOOMER_WIND
        ),
        MobileSpec(
            id = 7, name = "Raon", title = "Mine Layer Drone",
            category = MobileCategory.MECHANIC,
            baseHp = 980, baseShield = 0, shieldRegen = 0,
            moveSpeed = 58f, minAngle = 15, maxAngle = 60,
            t1Delay = 245, t2Delay = 310, ssDelay = 420,
            t1Damage = 175, t2Damage = 265, ssDamage = 365,
            t1Crater = 28f, t2Crater = 34f, ssCrater = 52f,
            t1Label = "Acid Spike", t2Label = "Twin Crawler Mines", ssLabel = "Super Mine",
            buttonAsset = "classic/room/b_ready_mobile07.png", portraitIndex = 6,
            bodyColor = Color(0xFF64748B), accentColor = Color(0xFF22D3EE),
            flightStyle = FlightStyle.RAON_MINE
        ),
        MobileSpec(
            id = 8, name = "Lightning", title = "Storm Shield Hover",
            category = MobileCategory.SHIELD,
            baseHp = 750, baseShield = 220, shieldRegen = 25,
            moveSpeed = 64f, minAngle = 15, maxAngle = 65,
            t1Delay = 235, t2Delay = 295, ssDelay = 405,
            t1Damage = 175, t2Damage = 265, ssDamage = 360,
            t1Crater = 26f, t2Crater = 30f, ssCrater = 44f,
            t1Label = "Static Orb", t2Label = "Sky Lightning", ssLabel = "Tempest Strike",
            buttonAsset = "classic/room/b_ready_mobile08.png", portraitIndex = 7,
            bodyColor = Color(0xFF0EA5E9), accentColor = Color(0xFFFEF08A),
            isHovering = true, flightStyle = FlightStyle.LIGHTNING_SKY
        ),
        MobileSpec(
            id = 9, name = "J.D", title = "Gravity Shield Tank",
            category = MobileCategory.SHIELD,
            baseHp = 760, baseShield = 240, shieldRegen = 28,
            moveSpeed = 62f, minAngle = 15, maxAngle = 60,
            t1Delay = 240, t2Delay = 300, ssDelay = 410,
            t1Damage = 170, t2Damage = 250, ssDamage = 355,
            t1Crater = 30f, t2Crater = 38f, ssCrater = 50f,
            t1Label = "Plasma Ball", t2Label = "Gravity Pull", ssLabel = "Shockwave Ring",
            buttonAsset = "classic/room/b_ready_mobile09.png", portraitIndex = 8,
            bodyColor = Color(0xFF6366F1), accentColor = Color(0xFF34D399),
            isHovering = true, flightStyle = FlightStyle.STANDARD
        ),
        MobileSpec(
            id = 10, name = "A.Sate", title = "Orbital Laser Beacon",
            category = MobileCategory.SHIELD,
            baseHp = 700, baseShield = 230, shieldRegen = 25,
            moveSpeed = 65f, minAngle = 15, maxAngle = 65,
            t1Delay = 235, t2Delay = 295, ssDelay = 415,
            t1Damage = 180, t2Damage = 275, ssDamage = 385,
            t1Crater = 26f, t2Crater = 32f, ssCrater = 46f,
            t1Label = "Satellite Beam", t2Label = "Multi-Orbital", ssLabel = "Ion Cannon",
            buttonAsset = "classic/room/b_ready_mobile10.png", portraitIndex = 9,
            bodyColor = Color(0xFFE11D48), accentColor = Color(0xFF38BDF8),
            isHovering = true, flightStyle = FlightStyle.ORBITAL_BEAM
        ),
        MobileSpec(
            id = 11, name = "Ice", title = "Cryo Mammoth",
            category = MobileCategory.BIONIC,
            baseHp = 1120, baseShield = 0, shieldRegen = 0,
            moveSpeed = 52f, minAngle = 15, maxAngle = 55,
            t1Delay = 250, t2Delay = 310, ssDelay = 420,
            t1Damage = 175, t2Damage = 245, ssDamage = 350,
            t1Crater = 34f, t2Crater = 38f, ssCrater = 54f,
            t1Label = "Frost Boulder", t2Label = "Armor Break Ice", ssLabel = "Glacier Drop",
            buttonAsset = "classic/room/b_ready_mobile11.png", portraitIndex = 10,
            bodyColor = Color(0xFF06B6D4), accentColor = Color(0xFFE0F2FE),
            flightStyle = FlightStyle.STANDARD
        ),
        MobileSpec(
            id = 12, name = "Turtle", title = "Hydro Wave Artillery",
            category = MobileCategory.BIONIC,
            baseHp = 1060, baseShield = 0, shieldRegen = 0,
            moveSpeed = 54f, minAngle = 15, maxAngle = 60,
            t1Delay = 245, t2Delay = 305, ssDelay = 425,
            t1Damage = 185, t2Damage = 290, ssDamage = 390,
            t1Crater = 30f, t2Crater = 34f, ssCrater = 48f,
            t1Label = "Water Bubble", t2Label = "Twin Helix Wave", ssLabel = "6-Split Sky Burst",
            buttonAsset = "classic/room/b_ready_mobile12.png", portraitIndex = 11,
            bodyColor = Color(0xFF0D9488), accentColor = Color(0xFF67E8F9),
            flightStyle = FlightStyle.TURTLE_WAVE
        ),
        MobileSpec(
            id = 13, name = "Grub", title = "Ricochet Slime Worm",
            category = MobileCategory.BIONIC,
            baseHp = 1020, baseShield = 0, shieldRegen = 0,
            moveSpeed = 56f, minAngle = 15, maxAngle = 60,
            t1Delay = 240, t2Delay = 300, ssDelay = 415,
            t1Damage = 180, t2Damage = 285, ssCrater = 46f,
            t1Crater = 28f, t2Crater = 32f, ssDamage = 380,
            t1Label = "Slime Shot", t2Label = "4-Ball Ricochet", ssLabel = "Crusher Ball",
            buttonAsset = "classic/room/b_ready_mobile13.png", portraitIndex = 12,
            bodyColor = Color(0xFF84CC16), accentColor = Color(0xFFD9F99D),
            flightStyle = FlightStyle.GRUB_BOUNCE
        ),
        MobileSpec(
            id = 14, name = "Dragon", title = "Mythic Flame Drake",
            category = MobileCategory.BIONIC,
            baseHp = 980, baseShield = 0, shieldRegen = 0,
            moveSpeed = 62f, minAngle = 15, maxAngle = 65,
            t1Delay = 245, t2Delay = 305, ssDelay = 420,
            t1Damage = 190, t2Damage = 290, ssDamage = 410,
            t1Crater = 28f, t2Crater = 36f, ssCrater = 52f,
            t1Label = "Flame Spark", t2Label = "4-Breath Volley", ssLabel = "5-Meteor Dive",
            buttonAsset = "classic/room/b_ready_mobile_dragon.png", portraitIndex = 14,
            bodyColor = Color(0xFFDC2626), accentColor = Color(0xFFFBBF24),
            isHovering = true, flightStyle = FlightStyle.DRAGON_VOLLEY
        ),
        MobileSpec(
            id = 15, name = "Knight", title = "Mythic Ion Paladin",
            category = MobileCategory.MECHANIC,
            baseHp = 1050, baseShield = 0, shieldRegen = 0,
            moveSpeed = 58f, minAngle = 15, maxAngle = 65,
            t1Delay = 245, t2Delay = 305, ssDelay = 420,
            t1Damage = 190, t2Damage = 295, ssDamage = 415,
            t1Crater = 28f, t2Crater = 34f, ssCrater = 50f,
            t1Label = "Sword Beam", t2Label = "5-Sword Ion Fan", ssLabel = "8-Blade Rain",
            buttonAsset = "classic/room/b_ready_mobile_knight.png", portraitIndex = 15,
            bodyColor = Color(0xFFF59E0B), accentColor = Color(0xFFFEF3C7),
            isHovering = true, flightStyle = FlightStyle.KNIGHT_SWORD
        ),
        MobileSpec(
            id = 16, name = "Aduka", title = "Thor Satellite Buggy",
            category = MobileCategory.MECHANIC,
            baseHp = 1000, baseShield = 0, shieldRegen = 0,
            moveSpeed = 60f, minAngle = 15, maxAngle = 60,
            t1Delay = 240, t2Delay = 300, ssDelay = 415,
            t1Damage = 180, t2Damage = 275, ssDamage = 395,
            t1Crater = 28f, t2Crater = 30f, ssCrater = 44f,
            t1Label = "Volt Cannon", t2Label = "Thor Laser", ssLabel = "Thor Mega Beam",
            buttonAsset = "classic/room/b_ready_mobile_aduka.png", portraitIndex = 16,
            bodyColor = Color(0xFF7C3AED), accentColor = Color(0xFF38BDF8),
            flightStyle = FlightStyle.ADUKA_THOR
        )
    )

    fun mobileById(id: Int): MobileSpec = mobiles.firstOrNull { it.id == id } ?: mobiles[1] // Fallback Bigfoot (ID 2)

    fun opponentFor(playerMobileId: Int): MobileSpec = when (playerMobileId) {
        1 -> mobileById(2) // Mage vs Bigfoot
        2 -> mobileById(1) // Bigfoot vs Mage
        3 -> mobileById(1) // Nak vs Mage
        else -> mobiles[(playerMobileId + 3) % mobiles.size]
    }

    // All 22 classic map sides (11 worlds x A/B), Miramo Town A (index 0) is default
    val mapSides: List<MapSideSpec> = listOf(
        MapSideSpec(0, "Miramo Town", "A", Color(0xFF1E3A5F), Color(0xFF60A5FA), Color(0xFF4ADE80), Color(0xFF6B4F35), Color(0xFF86EFAC), 101, "Classic rolling sky bridges & grassy peaks."),
        MapSideSpec(1, "Miramo Town", "B", Color(0xFF1E1B4B), Color(0xFF818CF8), Color(0xFF34D399), Color(0xFF475569), Color(0xFF6EE7B7), 102, "Miramo evening ridges with steep valley drop."),
        MapSideSpec(2, "Nirvana", "A", Color(0xFF312E81), Color(0xFFA78BFA), Color(0xFF94A3B8), Color(0xFF334155), Color(0xFFCBD5E1), 201, "Floating stone arches above cloud seas."),
        MapSideSpec(3, "Nirvana", "B", Color(0xFF4C1D95), Color(0xFFC084FC), Color(0xFF64748B), Color(0xFF1E293B), Color(0xFF94A3B8), 202, "Dual high-altitude sanctum platforms."),
        MapSideSpec(4, "Sea of Hero", "A", Color(0xFF0C4A6E), Color(0xFF38BDF8), Color(0xFFFDE047), Color(0xFFB45309), Color(0xFFFEF08A), 301, "Golden coastal archipelagos & narrow bridges."),
        MapSideSpec(5, "Sea of Hero", "B", Color(0xFF082F49), Color(0xFF0284C7), Color(0xFFF59E0B), Color(0xFF78350F), Color(0xFFFCD34D), 302, "Sunset harbor pillars vulnerable to bunge."),
        MapSideSpec(6, "Adium Root", "A", Color(0xFF14532D), Color(0xFF4ADE80), Color(0xFF22C55E), Color(0xFF57534E), Color(0xFF86EFAC), 401, "Ancient giant canopy roots & mossy hollows."),
        MapSideSpec(7, "Adium Root", "B", Color(0xFF052E16), Color(0xFF16A34A), Color(0xFF10B981), Color(0xFF3F3F46), Color(0xFF6EE7B7), 402, "Deep forest basin with high flanking roots."),
        MapSideSpec(8, "Dummy Slope", "A", Color(0xFF1E293B), Color(0xFF64748B), Color(0xFFE2E8F0), Color(0xFF475569), Color(0xFFF8FAFC), 501, "Steep training dunes for precision lobbing."),
        MapSideSpec(9, "Dummy Slope", "B", Color(0xFF0F172A), Color(0xFF475569), Color(0xFFCBD5E1), Color(0xFF334155), Color(0xFFE2E8F0), 502, "Inverted slope ridge with exposed center."),
        MapSideSpec(10, "Meta Mine", "A", Color(0xFF27272A), Color(0xFF71717A), Color(0xFFA1A1AA), Color(0xFF3F3F46), Color(0xFFE4E4E7), 601, "Industrial steel foundry & mining platforms."),
        MapSideSpec(11, "Meta Mine", "B", Color(0xFF18181B), Color(0xFF52525B), Color(0xFFF59E0B), Color(0xFF27272A), Color(0xFFFBBF24), 602, "Deep quarry pit with twin steel walkways."),
        MapSideSpec(12, "Cozy Tower", "A", Color(0xFF1E3A8A), Color(0xFF93C5FD), Color(0xFF38BDF8), Color(0xFF1E293B), Color(0xFFBAE6FD), 701, "High-elevation twin towers for long-range arcs."),
        MapSideSpec(13, "Cozy Tower", "B", Color(0xFF172554), Color(0xFF60A5FA), Color(0xFF818CF8), Color(0xFF1E1B4B), Color(0xFFC7D2FE), 702, "Stepped spire bridges under starry winds."),
        MapSideSpec(14, "Cave", "A", Color(0xFF1C1917), Color(0xFF57534E), Color(0xFFA8A29E), Color(0xFF292524), Color(0xFFD6D3D1), 801, "Cavernous rock stalagmites & tight angles."),
        MapSideSpec(15, "Cave", "B", Color(0xFF0C0A09), Color(0xFF44403C), Color(0xFF78716C), Color(0xFF1C1917), Color(0xFFA8A29E), 802, "Crystal cavern floor with steep side ledges."),
        MapSideSpec(16, "Dragon", "A", Color(0xFF450A0A), Color(0xFFDC2626), Color(0xFFF97316), Color(0xFF451A03), Color(0xFFFDBA74), 901, "Volcanic caldera spine & lava-lit cliffs."),
        MapSideSpec(17, "Dragon", "B", Color(0xFF2E0505), Color(0xFF991B1B), Color(0xFFEF4444), Color(0xFF291408), Color(0xFFFCA5A5), 902, "Scorched dragon ridge with thin volcanic bridges."),
        MapSideSpec(18, "Starry Night", "A", Color(0xFF020617), Color(0xFF1E293B), Color(0xFF818CF8), Color(0xFF1E1B4B), Color(0xFFC7D2FE), 1001, "Moonlit floating hills under clear night sky."),
        MapSideSpec(19, "Starry Night", "B", Color(0xFF090D16), Color(0xFF312E81), Color(0xFF38BDF8), Color(0xFF0F172A), Color(0xFF7DD3FC), 1002, "Twin crescent ridges with wide central gap."),
        MapSideSpec(20, "Secret Area", "A", Color(0xFF064E3B), Color(0xFF0D9488), Color(0xFF2DD4BF), Color(0xFF134E4A), Color(0xFF99F6E4), 1101, "Experimental bio-mech proving grounds."),
        MapSideSpec(21, "Secret Area", "B", Color(0xFF042F2E), Color(0xFF115E59), Color(0xFF34D399), Color(0xFF064E3B), Color(0xFFA7F3D0), 1102, "Asymmetric tactical bunkers & crater slopes.")
    )

    fun mapByIndex(index: Int): MapSideSpec = mapSides.getOrElse(index) { mapSides[0] }

    // 15 classic Avatar Shop records using actual sheets in app/src/main/assets/avatars/
    val avatars: List<AvatarItem> = listOf(
        AvatarItem("0:0:0", "Basic Helmet", AvatarSlot.HEAD, 200, 0, "basic", 89, 127, 458, 389, defenseBonus = 3, healthBonus = 2),
        AvatarItem("0:1:0", "Basic Flight Suit", AvatarSlot.BODY, 250, 0, "basic", 730, 116, 447, 462, defenseBonus = 3, delayBonus = 2),
        AvatarItem("1:0:0", "Rookie Cap", AvatarSlot.HEAD, 300, 0, "basic", 64, 698, 485, 433, attackBonus = 3, popularityBonus = 2),
        AvatarItem("1:1:0", "Cadet Uniform", AvatarSlot.BODY, 350, 0, "basic", 730, 690, 440, 460, healthBonus = 4, defenseBonus = 2),

        AvatarItem("0:0:1", "Space Visor Helm", AvatarSlot.HEAD, 650, 320, "space", 121, 141, 390, 354, attackBonus = 5, shieldBonus = 4),
        AvatarItem("0:1:1", "Space Suit", AvatarSlot.BODY, 750, 360, "space", 768, 127, 387, 423, defenseBonus = 6, healthBonus = 4),
        AvatarItem("0:2:1", "Cyber Shades", AvatarSlot.GLASSES, 450, 220, "space", 119, 835, 405, 207, attackBonus = 4, delayBonus = 5),
        AvatarItem("2:3:1", "Orbital Crest Flag", AvatarSlot.FLAG, 500, 250, "space", 766, 756, 394, 365, popularityBonus = 6, shieldBonus = 4),

        AvatarItem("0:0:2", "Miner Hardhat", AvatarSlot.HEAD, 420, 0, "miner", 100, 120, 400, 380, digBonus = 6, defenseBonus = 4),
        AvatarItem("0:1:2", "Miner Overalls", AvatarSlot.BODY, 480, 0, "miner", 700, 120, 400, 430, healthBonus = 6, digBonus = 4),

        AvatarItem("0:0:3", "Punk Spikes", AvatarSlot.HEAD, 580, 290, "punk", 100, 120, 400, 380, attackBonus = 6, popularityBonus = 4),
        AvatarItem("0:1:3", "Punk Jacket", AvatarSlot.BODY, 620, 310, "punk", 700, 120, 400, 430, attackBonus = 5, delayBonus = 4),

        AvatarItem("0:0:138", "Kappa Mask", AvatarSlot.HEAD, 850, 420, "kappa", 100, 120, 400, 400, shieldBonus = 8, healthBonus = 6),
        AvatarItem("flag_gold", "Thor Pennant", AvatarSlot.FLAG, 550, 280, "space", 766, 756, 394, 365, attackBonus = 4, delayBonus = 4),
        AvatarItem("glass_tactical", "Tactical Rangefinder", AvatarSlot.GLASSES, 520, 260, "space", 119, 835, 405, 207, digBonus = 5, attackBonus = 4)
    )

    val rankNames: List<String> = listOf(
        "Little Chick", "Wooden Hammer", "Double Wooden", "Stone Axe", "Double Stone Axe",
        "Metal Axe", "Double Metal Axe", "Silver Axe", "Double Silver Axe", "Golden Axe",
        "Double Golden Axe", "Metal Battle Axe", "Golden Battle Axe", "Silver Battle Axe", "Sapphire Wand",
        "Ruby Wand", "Diamond Wand", "Blue Dragon", "Red Dragon", "Silver Dragon",
        "Ruby Knight", "Sapphire Knight", "Diamond Knight", "Crown Champion", "Thor's Hammer"
    )

    fun rankName(index: Int): String = rankNames.getOrElse(index.coerceIn(0, rankNames.lastIndex)) { "Little Chick" }
}
