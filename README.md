# Gunbound Recomp (Android)

An offline 1v1 classic artillery strategy game for **Android**, built with **Kotlin**, **Jetpack Compose**, and **Material Design 3**, rewritten from the `WanderlanLima/gunbound-M` Gunbound Recomp project (inspired by **GunBound Thor's Hammer**).

## Implemented Features

- **Classic Lobby**:
  - 8 selectable channels (`Channel 1`–`Channel 8`) with themed classic button sprites.
  - Room card preview with map side thumbnail, `Create Room` / `Quick Match` navigation, editable pilot nickname, and local profile reset.
  - Persistent player profile (Gold, Cash, XP, rank tier, owned & equipped avatars, attribute points, and audio/aiming options) stored locally via Jetpack DataStore.
- **1v1 Training Room**:
  - Team A / Team B side switcher.
  - All **16 classic mobiles** (`1 Mage` through `16 Aduka`, excluding removed ID `0`) plus `Random`, displaying attack/defense/dig/mobility stats and native room portraits.
  - **22 classic map sides** across 11 worlds (with **Miramo Town A** as default) and per-side weather/ambient presets.
  - **6-slot Battle Item Bar** selectable from **11 classic battle items** (`Dual`, `Dual+`, `Teleport`, `Team TP`, `Thunder`, `Energy 1`, `Energy 2`, `Power Up`, `Wind Shift`, `Bunge Shot`, `Blood`).
  - CPU difficulty selector (`Easy`, `Normal`, `Hard`).
- **Battle Arena & Physics**:
  - Destructible 2D terrain with crater carving, wind/gravity physics (20 Hz fixed step), and **7 weather types** (`Clear`, `Wind`, `Thor`, `Force`, `Lightning`, `Tornado`, `Moon`).
  - TurnTable delay-based turn order (pass costs 200 delay; aiming time contributes to turn delay).
  - **Slice** (hold-and-release 3-second charge bar) and **Drag** (pull-back touch/drag) aiming modes, plus `T1` / `T2` / `SS` shot selectors, left/right movement fuel, and item activation.
  - CPU opponent sharing the closed deterministic `BallisticsEngine.predictShotImpact` solver with difficulty-scaled angle/power jitter.
  - Synthesized Web/Audio-free `AudioTrack` SFX engine (`SoundFxEngine`), Pause & Options dialogs, and **WIN / LOSE** result overlay awarding Gold and XP.
- **Avatar Shop**:
  - 15 classic avatar items across `Head`, `Body`, `Glasses`, and `Flag` slots.
  - Buy with Gold or Cash, live **Preview**, Equip/Unequip, Sell, and **Attribute Point** allocation (`Attack`, `Defense`, `Energy`, `Dig`, `Shield Regen`).

## Architecture

- `com.example.data`: `GameModels`, `GameCatalog`, `ProfileRepository` (DataStore), `AssetSpriteLoader`, `SoundFxEngine`
- `com.example.engine`: `TerrainMap`, `BallisticsEngine`, `CpuPlanner`, `MatchController`
- `com.example.ui`: `MainViewModel`, `GunboundNavGraph`, `theme/` (`Chakra Petch`, `Plus Jakarta Sans`, `JetBrains Mono`), `components/`, `screens/` (`LobbyScreen`, `RoomScreen`, `LoadingScreen`, `BattleScreen`, `ShopScreen`)

## Building

Built using Gradle Kotlin DSL (`compileSdk = 36`, `minSdk = 26`, `targetSdk = 36`) and Jetpack Compose BOM `2024.09.00`:

```bash
gradle :app:assembleDebug
```
