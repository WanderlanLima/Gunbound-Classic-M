package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.gunboundDataStore: DataStore<Preferences> by preferencesDataStore(name = "gunbound_recomp_profile")

sealed interface ShopActionResult {
    data class Success(val message: String) : ShopActionResult
    data class Error(val message: String) : ShopActionResult
}

class ProfileRepository(private val context: Context) {

    private val profileKey = stringPreferencesKey("profile_v2_json")
    private val optionsKey = stringPreferencesKey("options_v1_json")

    val profileFlow: Flow<PlayerProfile> = context.gunboundDataStore.data.map { prefs ->
        decodeProfile(prefs[profileKey])
    }

    val optionsFlow: Flow<GameOptions> = context.gunboundDataStore.data.map { prefs ->
        decodeOptions(prefs[optionsKey])
    }

    suspend fun updateSelection(
        mobileId: Int? = null,
        mapIndex: Int? = null,
        playerTeamA: Boolean? = null,
        cpuDifficulty: CpuDifficulty? = null,
        nickname: String? = null
    ) {
        context.gunboundDataStore.edit { prefs ->
            val current = decodeProfile(prefs[profileKey])
            val updated = current.copy(
                selectedMobileId = mobileId ?: current.selectedMobileId,
                selectedMapIndex = mapIndex ?: current.selectedMapIndex,
                playerTeamA = playerTeamA ?: current.playerTeamA,
                cpuDifficulty = cpuDifficulty ?: current.cpuDifficulty,
                nickname = nickname?.trim()?.take(16)?.ifEmpty { "Commander" } ?: current.nickname
            )
            prefs[profileKey] = encodeProfile(updated)
        }
    }

    suspend fun updateEquippedItems(items: List<BattleItemId>) {
        // Enforce classic 6-slot weight limit
        val valid = mutableListOf<BattleItemId>()
        var slotsUsed = 0
        for (item in items) {
            if (slotsUsed + item.slots <= 6) {
                valid.add(item)
                slotsUsed += item.slots
            }
        }
        context.gunboundDataStore.edit { prefs ->
            val current = decodeProfile(prefs[profileKey])
            prefs[profileKey] = encodeProfile(current.copy(equippedItems = valid))
        }
    }

    suspend fun updateOptions(transform: (GameOptions) -> GameOptions) {
        context.gunboundDataStore.edit { prefs ->
            val current = decodeOptions(prefs[optionsKey])
            prefs[optionsKey] = encodeOptions(transform(current))
        }
    }

    suspend fun recordMatchResult(won: Boolean, goldEarned: Int, xpEarned: Int) {
        context.gunboundDataStore.edit { prefs ->
            val current = decodeProfile(prefs[profileKey])
            val updated = current.copy(
                gold = (current.gold + goldEarned).coerceAtLeast(0),
                xp = (current.xp + xpEarned).coerceAtLeast(0),
                wins = current.wins + if (won) 1 else 0,
                losses = current.losses + if (!won) 1 else 0
            )
            prefs[profileKey] = encodeProfile(updated)
        }
    }

    suspend fun buyAvatarItem(item: AvatarItem, useCash: Boolean): ShopActionResult {
        var result: ShopActionResult = ShopActionResult.Error("Purchase failed.")
        context.gunboundDataStore.edit { prefs ->
            val current = decodeProfile(prefs[profileKey])
            if (item.id in current.ownedAvatars) {
                result = ShopActionResult.Error("Already owned.")
                return@edit
            }
            if (useCash) {
                if (item.cashPrice <= 0) {
                    result = ShopActionResult.Error("Cash purchase unavailable.")
                    return@edit
                }
                if (current.cash < item.cashPrice) {
                    result = ShopActionResult.Error("Not enough Cash.")
                    return@edit
                }
                val updated = equipOnProfile(
                    current.copy(
                        cash = current.cash - item.cashPrice,
                        ownedAvatars = current.ownedAvatars + item.id
                    ),
                    item
                )
                prefs[profileKey] = encodeProfile(updated)
                result = ShopActionResult.Success("Purchased & equipped ${item.name}!")
            } else {
                if (current.gold < item.goldPrice) {
                    result = ShopActionResult.Error("Not enough Gold.")
                    return@edit
                }
                val updated = equipOnProfile(
                    current.copy(
                        gold = current.gold - item.goldPrice,
                        ownedAvatars = current.ownedAvatars + item.id
                    ),
                    item
                )
                prefs[profileKey] = encodeProfile(updated)
                result = ShopActionResult.Success("Purchased & equipped ${item.name}!")
            }
        }
        return result
    }

    suspend fun toggleEquipAvatar(item: AvatarItem) {
        context.gunboundDataStore.edit { prefs ->
            val current = decodeProfile(prefs[profileKey])
            if (item.id !in current.ownedAvatars) return@edit
            val isEquipped = when (item.slot) {
                AvatarSlot.HEAD -> current.equippedHead == item.id
                AvatarSlot.BODY -> current.equippedBody == item.id
                AvatarSlot.GLASSES -> current.equippedGlasses == item.id
                AvatarSlot.FLAG -> current.equippedFlag == item.id
            }
            val updated = if (isEquipped) {
                when (item.slot) {
                    AvatarSlot.HEAD -> current.copy(equippedHead = null)
                    AvatarSlot.BODY -> current.copy(equippedBody = null)
                    AvatarSlot.GLASSES -> current.copy(equippedGlasses = null)
                    AvatarSlot.FLAG -> current.copy(equippedFlag = null)
                }
            } else {
                equipOnProfile(current, item)
            }
            prefs[profileKey] = encodeProfile(updated)
        }
    }

    suspend fun sellAvatarItem(item: AvatarItem): ShopActionResult {
        var result: ShopActionResult = ShopActionResult.Error("Item not owned.")
        context.gunboundDataStore.edit { prefs ->
            val current = decodeProfile(prefs[profileKey])
            if (item.id !in current.ownedAvatars) return@edit
            val refund = (item.goldPrice * 0.6f).toInt().coerceAtLeast(50)
            val updated = current.copy(
                gold = current.gold + refund,
                ownedAvatars = current.ownedAvatars - item.id,
                equippedHead = if (current.equippedHead == item.id) null else current.equippedHead,
                equippedBody = if (current.equippedBody == item.id) null else current.equippedBody,
                equippedGlasses = if (current.equippedGlasses == item.id) null else current.equippedGlasses,
                equippedFlag = if (current.equippedFlag == item.id) null else current.equippedFlag
            )
            prefs[profileKey] = encodeProfile(updated)
            result = ShopActionResult.Success("Sold ${item.name} for $refund G.")
        }
        return result
    }

    suspend fun saveAttributes(newAttrs: PlayerAttributes) {
        context.gunboundDataStore.edit { prefs ->
            val current = decodeProfile(prefs[profileKey])
            val clamped = PlayerAttributes(
                attack = newAttrs.attack.coerceIn(0, 50),
                defense = newAttrs.defense.coerceIn(0, 50),
                health = newAttrs.health.coerceIn(0, 50),
                itemDelay = newAttrs.itemDelay.coerceIn(0, 50),
                dig = newAttrs.dig.coerceIn(0, 50),
                shieldRegen = newAttrs.shieldRegen.coerceIn(0, 50),
                popularity = newAttrs.popularity.coerceIn(0, 50)
            )
            if (clamped.spentPoints <= current.totalAttributePoints) {
                prefs[profileKey] = encodeProfile(current.copy(attributes = clamped))
            }
        }
    }

    suspend fun resetProfile() {
        context.gunboundDataStore.edit { prefs ->
            prefs[profileKey] = encodeProfile(PlayerProfile())
        }
    }

    private fun equipOnProfile(profile: PlayerProfile, item: AvatarItem): PlayerProfile = when (item.slot) {
        AvatarSlot.HEAD -> profile.copy(equippedHead = item.id)
        AvatarSlot.BODY -> profile.copy(equippedBody = item.id)
        AvatarSlot.GLASSES -> profile.copy(equippedGlasses = item.id)
        AvatarSlot.FLAG -> profile.copy(equippedFlag = item.id)
    }

    private fun encodeProfile(p: PlayerProfile): String {
        val obj = JSONObject()
        obj.put("nickname", p.nickname)
        obj.put("gold", p.gold)
        obj.put("cash", p.cash)
        obj.put("xp", p.xp)
        obj.put("wins", p.wins)
        obj.put("losses", p.losses)
        obj.put("mobileId", p.selectedMobileId)
        obj.put("mapIndex", p.selectedMapIndex)
        obj.put("teamA", p.playerTeamA)
        obj.put("difficulty", p.cpuDifficulty.name)
        obj.put("items", JSONArray(p.equippedItems.map { it.name }))
        obj.put("ownedAvatars", JSONArray(p.ownedAvatars.toList()))
        obj.put("head", p.equippedHead ?: "")
        obj.put("body", p.equippedBody ?: "")
        obj.put("glasses", p.equippedGlasses ?: "")
        obj.put("flag", p.equippedFlag ?: "")
        obj.put("attr_atk", p.attributes.attack)
        obj.put("attr_def", p.attributes.defense)
        obj.put("attr_hp", p.attributes.health)
        obj.put("attr_dly", p.attributes.itemDelay)
        obj.put("attr_dig", p.attributes.dig)
        obj.put("attr_shd", p.attributes.shieldRegen)
        obj.put("attr_pop", p.attributes.popularity)
        return obj.toString()
    }

    private fun decodeProfile(raw: String?): PlayerProfile {
        if (raw.isNullOrBlank()) return PlayerProfile()
        return try {
            val obj = JSONObject(raw)
            val itemsArr = obj.optJSONArray("items")
            val items = if (itemsArr != null) {
                (0 until itemsArr.length()).mapNotNull { idx ->
                    runCatching { BattleItemId.valueOf(itemsArr.getString(idx)) }.getOrNull()
                }
            } else {
                PlayerProfile().equippedItems
            }
            val ownedArr = obj.optJSONArray("ownedAvatars")
            val owned = if (ownedArr != null) {
                (0 until ownedArr.length()).map { ownedArr.getString(it) }.toSet()
            } else {
                emptySet()
            }
            PlayerProfile(
                nickname = obj.optString("nickname", "Commander").ifBlank { "Commander" },
                gold = obj.optInt("gold", 1000),
                cash = obj.optInt("cash", 1000),
                xp = obj.optInt("xp", 0),
                wins = obj.optInt("wins", 0),
                losses = obj.optInt("losses", 0),
                selectedMobileId = obj.optInt("mobileId", 2).coerceIn(1, 16),
                selectedMapIndex = obj.optInt("mapIndex", 0).coerceIn(0, 21),
                playerTeamA = obj.optBoolean("teamA", true),
                cpuDifficulty = runCatching {
                    CpuDifficulty.valueOf(obj.optString("difficulty", CpuDifficulty.NORMAL.name))
                }.getOrDefault(CpuDifficulty.NORMAL),
                equippedItems = items,
                ownedAvatars = owned,
                equippedHead = obj.optString("head", "").ifBlank { null },
                equippedBody = obj.optString("body", "").ifBlank { null },
                equippedGlasses = obj.optString("glasses", "").ifBlank { null },
                equippedFlag = obj.optString("flag", "").ifBlank { null },
                attributes = PlayerAttributes(
                    attack = obj.optInt("attr_atk", 0),
                    defense = obj.optInt("attr_def", 0),
                    health = obj.optInt("attr_hp", 0),
                    itemDelay = obj.optInt("attr_dly", 0),
                    dig = obj.optInt("attr_dig", 0),
                    shieldRegen = obj.optInt("attr_shd", 0),
                    popularity = obj.optInt("attr_pop", 0)
                )
            )
        } catch (_: Exception) {
            PlayerProfile()
        }
    }

    private fun encodeOptions(o: GameOptions): String = JSONObject().apply {
        put("sfx", o.sfxEnabled)
        put("aim", o.aimingMode.name)
        put("scroll", o.scrollSpeed.toDouble())
        put("bg", o.showBackgroundEffects)
    }.toString()

    private fun decodeOptions(raw: String?): GameOptions {
        if (raw.isNullOrBlank()) return GameOptions()
        return try {
            val o = JSONObject(raw)
            GameOptions(
                sfxEnabled = o.optBoolean("sfx", true),
                aimingMode = runCatching {
                    AimingMode.valueOf(o.optString("aim", AimingMode.SLICE.name))
                }.getOrDefault(AimingMode.SLICE),
                scrollSpeed = o.optDouble("scroll", 1.0).toFloat().coerceIn(0.5f, 2.0f),
                showBackgroundEffects = o.optBoolean("bg", true)
            )
        } catch (_: Exception) {
            GameOptions()
        }
    }
}
