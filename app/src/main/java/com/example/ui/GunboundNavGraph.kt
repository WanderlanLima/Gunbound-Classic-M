package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.BattleScreen
import com.example.ui.screens.LoadingScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.RoomScreen
import com.example.ui.screens.ShopScreen

object Routes {
    const val LOBBY = "lobby"
    const val ROOM = "room"
    const val SHOP = "shop"
    const val LOADING = "loading"
    const val BATTLE = "battle"
}

@Composable
fun GunboundApp(vm: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val profile by vm.profile.collectAsStateWithLifecycle()
    val options by vm.options.collectAsStateWithLifecycle()
    val selectedChannel by vm.selectedChannel.collectAsStateWithLifecycle()
    val shopFeedback by vm.shopFeedback.collectAsStateWithLifecycle()
    val previewAvatarId by vm.previewAvatarId.collectAsStateWithLifecycle()
    val battleState by vm.battleState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Routes.LOBBY
    ) {
        composable(Routes.LOBBY) {
            LobbyScreen(
                profile = profile,
                selectedChannel = selectedChannel,
                spriteLoader = vm.spriteLoader,
                onSelectChannel = vm::selectChannel,
                onEnterRoom = { navController.navigate(Routes.ROOM) },
                onQuickStartMatch = { navController.navigate(Routes.LOADING) },
                onOpenShop = { navController.navigate(Routes.SHOP) },
                onUpdateNickname = vm::updateNickname,
                onResetProfile = vm::resetProfile
            )
        }

        composable(Routes.ROOM) {
            RoomScreen(
                profile = profile,
                spriteLoader = vm.spriteLoader,
                onBack = { navController.popBackStack() },
                onSelectMobile = vm::selectMobile,
                onSelectRandomMobile = vm::selectRandomMobile,
                onCycleMap = vm::cycleMap,
                onSelectMap = vm::selectMap,
                onToggleTeam = vm::toggleTeamSide,
                onSelectDifficulty = vm::selectDifficulty,
                onUpdateItems = vm::updateEquippedItems,
                onStartMatch = { navController.navigate(Routes.LOADING) }
            )
        }

        composable(Routes.SHOP) {
            ShopScreen(
                profile = profile,
                previewAvatarId = previewAvatarId,
                feedback = shopFeedback,
                spriteLoader = vm.spriteLoader,
                onBack = { navController.popBackStack() },
                onBuy = vm::buyAvatar,
                onToggleEquip = vm::toggleEquipAvatar,
                onSell = vm::sellAvatar,
                onPreview = vm::previewAvatar,
                onSaveAttributes = vm::saveAttributes,
                onClearFeedback = vm::clearShopFeedback
            )
        }

        composable(Routes.LOADING) {
            LoadingScreen(
                profile = profile,
                spriteLoader = vm.spriteLoader,
                onReadyComplete = {
                    vm.startNewMatch()
                    navController.navigate(Routes.BATTLE) {
                        popUpTo(Routes.LOADING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.BATTLE) {
            BattleScreen(
                state = battleState,
                controller = vm.controller(),
                options = options,
                spriteLoader = vm.spriteLoader,
                onToggleSfx = vm::toggleSfx,
                onSetAimingMode = vm::setAimingMode,
                onSetScrollSpeed = vm::setScrollSpeed,
                onToggleBgEffects = vm::toggleBackgroundEffects,
                onExitToRoom = {
                    vm.endMatchCleanup()
                    if (!navController.popBackStack(Routes.ROOM, inclusive = false)) {
                        navController.navigate(Routes.ROOM) {
                            popUpTo(Routes.LOBBY)
                        }
                    }
                }
            )
        }
    }
}
