package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AimingMode
import com.example.data.AssetSpriteLoader
import com.example.data.AvatarItem
import com.example.data.BattleItemId
import com.example.data.CpuDifficulty
import com.example.data.GameCatalog
import com.example.data.GameOptions
import com.example.data.PlayerAttributes
import com.example.data.PlayerProfile
import com.example.data.ProfileRepository
import com.example.data.ShopActionResult
import com.example.data.SoundFxEngine
import com.example.engine.BattleUiState
import com.example.engine.MatchController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfileRepository(application)
    val spriteLoader = AssetSpriteLoader(application)
    val soundFx = SoundFxEngine()

    val profile: StateFlow<PlayerProfile> = repository.profileFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlayerProfile())

    val options: StateFlow<GameOptions> = repository.optionsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, GameOptions())

    private val _selectedChannel = MutableStateFlow(1)
    val selectedChannel: StateFlow<Int> = _selectedChannel.asStateFlow()

    private val _shopFeedback = MutableStateFlow<String?>(null)
    val shopFeedback: StateFlow<String?> = _shopFeedback.asStateFlow()

    private val _previewAvatarId = MutableStateFlow<String?>(null)
    val previewAvatarId: StateFlow<String?> = _previewAvatarId.asStateFlow()

    private var activeMatchController: MatchController? = null
    private val _battleState = MutableStateFlow<BattleUiState?>(null)
    val battleState: StateFlow<BattleUiState?> = _battleState.asStateFlow()

    init {
        viewModelScope.launch {
            options.collect { opt ->
                soundFx.enabled = opt.sfxEnabled
            }
        }
    }

    fun selectChannel(channel: Int) {
        soundFx.playClick()
        _selectedChannel.value = channel.coerceIn(1, 8)
    }

    fun selectMobile(mobileId: Int) {
        soundFx.playClick()
        viewModelScope.launch {
            repository.updateSelection(mobileId = mobileId)
        }
    }

    fun selectRandomMobile() {
        soundFx.playClick()
        val picked = GameCatalog.mobiles.random().id
        viewModelScope.launch {
            repository.updateSelection(mobileId = picked)
        }
    }

    fun cycleMap(delta: Int) {
        soundFx.playClick()
        val size = GameCatalog.mapSides.size
        val next = (profile.value.selectedMapIndex + delta + size) % size
        viewModelScope.launch {
            repository.updateSelection(mapIndex = next)
        }
    }

    fun selectMap(index: Int) {
        soundFx.playClick()
        viewModelScope.launch {
            repository.updateSelection(mapIndex = index.coerceIn(0, GameCatalog.mapSides.lastIndex))
        }
    }

    fun toggleTeamSide() {
        soundFx.playClick()
        viewModelScope.launch {
            repository.updateSelection(playerTeamA = !profile.value.playerTeamA)
        }
    }

    fun selectDifficulty(diff: CpuDifficulty) {
        soundFx.playClick()
        viewModelScope.launch {
            repository.updateSelection(cpuDifficulty = diff)
        }
    }

    fun updateNickname(name: String) {
        viewModelScope.launch {
            repository.updateSelection(nickname = name)
        }
    }

    fun updateEquippedItems(items: List<BattleItemId>) {
        soundFx.playClick()
        viewModelScope.launch {
            repository.updateEquippedItems(items)
        }
    }

    fun toggleSfx() {
        viewModelScope.launch {
            repository.updateOptions { it.copy(sfxEnabled = !it.sfxEnabled) }
        }
    }

    fun setAimingMode(mode: AimingMode) {
        soundFx.playClick()
        viewModelScope.launch {
            repository.updateOptions { it.copy(aimingMode = mode) }
        }
    }

    fun setScrollSpeed(speed: Float) {
        viewModelScope.launch {
            repository.updateOptions { it.copy(scrollSpeed = speed.coerceIn(0.5f, 2.0f)) }
        }
    }

    fun toggleBackgroundEffects() {
        soundFx.playClick()
        viewModelScope.launch {
            repository.updateOptions { it.copy(showBackgroundEffects = !it.showBackgroundEffects) }
        }
    }

    fun buyAvatar(item: AvatarItem, useCash: Boolean) {
        soundFx.playClick()
        viewModelScope.launch {
            when (val res = repository.buyAvatarItem(item, useCash)) {
                is ShopActionResult.Success -> _shopFeedback.value = res.message
                is ShopActionResult.Error -> _shopFeedback.value = res.message
            }
        }
    }

    fun toggleEquipAvatar(item: AvatarItem) {
        soundFx.playClick()
        viewModelScope.launch {
            repository.toggleEquipAvatar(item)
        }
    }

    fun sellAvatar(item: AvatarItem) {
        soundFx.playClick()
        viewModelScope.launch {
            when (val res = repository.sellAvatarItem(item)) {
                is ShopActionResult.Success -> _shopFeedback.value = res.message
                is ShopActionResult.Error -> _shopFeedback.value = res.message
            }
        }
    }

    fun previewAvatar(item: AvatarItem?) {
        soundFx.playClick()
        _previewAvatarId.value = if (_previewAvatarId.value == item?.id) null else item?.id
    }

    fun clearShopFeedback() {
        _shopFeedback.value = null
    }

    fun saveAttributes(attrs: PlayerAttributes) {
        soundFx.playClick()
        viewModelScope.launch {
            repository.saveAttributes(attrs)
        }
    }

    fun resetProfile() {
        soundFx.playClick()
        viewModelScope.launch {
            repository.resetProfile()
        }
    }

    fun startNewMatch() {
        activeMatchController?.dispose()
        val rows = spriteLoader.loadLoadingGroundRows()
        val controller = MatchController(
            profile = profile.value,
            miramoGroundRows = rows,
            soundFx = soundFx,
            scope = viewModelScope,
            onMatchFinished = { summary ->
                viewModelScope.launch {
                    repository.recordMatchResult(
                        won = summary.playerWon,
                        goldEarned = summary.goldEarned,
                        xpEarned = summary.xpEarned
                    )
                }
            }
        )
        activeMatchController = controller
        viewModelScope.launch {
            controller.state.collect { st ->
                _battleState.value = st
            }
        }
    }

    fun endMatchCleanup() {
        activeMatchController?.dispose()
        activeMatchController = null
        _battleState.value = null
    }

    fun controller(): MatchController? = activeMatchController

    override fun onCleared() {
        activeMatchController?.dispose()
        super.onCleared()
    }
}
