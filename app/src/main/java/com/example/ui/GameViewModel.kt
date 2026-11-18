package com.example.ui

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.cloud.KillFeedEntry
import com.example.data.cloud.MarketListing
import com.example.data.cloud.MarketRepository
import com.example.data.db.OutpostRepository
import com.example.game.audio.TacticalAudioEngine
import com.example.game.engine.GameSimulation
import com.example.game.engine.HudSnapshot
import com.example.game.engine.LootSearchResult
import com.example.game.model.ArmorTier
import com.example.game.model.AttachmentSlot
import com.example.game.model.BaseCustomLayout
import com.example.game.model.BaseModuleKind
import com.example.game.model.BaseStashState
import com.example.game.model.BaseUpgradeSpecs
import com.example.game.model.CdCatalog
import com.example.game.model.CraftOutputKind
import com.example.game.model.CraftingMaterial
import com.example.game.model.CraftingRecipe
import com.example.game.model.DeploymentZone
import com.example.game.model.DogBreedResolver
import com.example.game.model.GameMapId
import com.example.game.model.GameSettingsState
import com.example.game.model.GlassesStyle
import com.example.game.model.GlovesStyle
import com.example.game.model.HatStyle
import com.example.game.model.HeritageVaultCatalog
import com.example.game.model.HqMissionCatalog
import com.example.game.model.InventoryItem
import com.example.game.model.LootType
import com.example.game.model.OperatorArchetype
import com.example.game.model.OperatorProfile
import com.example.game.model.OperatorSex
import com.example.game.model.PantsStyle
import com.example.game.model.PlacedBasePiece
import com.example.game.model.ScavKinshipMemoryStore
import com.example.game.model.ShirtStyle
import com.example.game.model.ShoeStyle
import com.example.game.model.SkinToneOption
import com.example.game.model.SlainScavRecord
import com.example.game.model.StationKind
import com.example.game.model.SurvivorCatalog
import com.example.game.model.TaskGoalKind
import com.example.game.model.WeaponAttachmentCatalog
import com.example.game.model.WeaponEra
import com.example.game.model.WeaponId
import com.example.game.world.OakridgeMapBuilder
import com.example.game.world.OutpostZeroMapBuilder
import kotlin.math.hypot
import kotlin.math.roundToInt
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class ActiveModal {
    NONE,
    CHARACTER_CREATOR,
    LAPTOP,
    HELIPAD,
    NPC_DIALOGUE,
    WORKBENCH,
    MARKET,
    CD_BINDER,
    BACKPACK,
    LOOT_CONTAINER,
    EXTRACTION_SUMMARY,
    OPERATOR_DOWN,
    OPS_HUB,
    SETTINGS,
    RESCUE_ANIMATION,
    PERMADEATH_REPORT,
    BASE_BUILDER
}

data class ExtractionSummary(
    val exitName: String,
    val kills: Int,
    val salvagedCredits: Int,
    val extractedFood: Int,
    val extractedWater: Int,
    val extractedMeds: Int,
    val extractedMaterials: Int,
    val newCdsCount: Int
)

data class RescueMissionAnimState(
    val operator: OperatorProfile,
    val feePaid: Int,
    val zoneTitle: String
)

data class PermadeathReport(
    val callsign: String,
    val roleLabel: String,
    val stolenItemNames: List<String>,
    val recoveredBagItems: List<InventoryItem>,
    val recoveredSafeItems: List<InventoryItem>,
    val recoveredCred: Int,
    val recoveredCds: Int
)

class GameViewModel(
    private val repo: OutpostRepository,
    private val marketRepo: MarketRepository,
    val currentUserId: String,
    private val prefs: SharedPreferences? = null
) : ViewModel() {

    val audio = TacticalAudioEngine()
    val simulation = GameSimulation(audio)

    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash.asStateFlow()

    private val _settings = MutableStateFlow(loadSettingsFromPrefs())
    val settings: StateFlow<GameSettingsState> = _settings.asStateFlow()

    private val _hud = MutableStateFlow(HudSnapshot())
    val hud: StateFlow<HudSnapshot> = _hud.asStateFlow()

    private val _roster = MutableStateFlow<List<OperatorProfile>>(emptyList())
    val roster: StateFlow<List<OperatorProfile>> = _roster.asStateFlow()

    private val _stash = MutableStateFlow(BaseStashState())
    val stash: StateFlow<BaseStashState> = _stash.asStateFlow()

    private val _collectedCds = MutableStateFlow<Set<Int>>(emptySet())
    val collectedCds: StateFlow<Set<Int>> = _collectedCds.asStateFlow()

    private val _activeModal = MutableStateFlow(ActiveModal.NONE)
    val activeModal: StateFlow<ActiveModal> = _activeModal.asStateFlow()

    private val _activeLoot = MutableStateFlow<LootSearchResult?>(null)
    val activeLoot: StateFlow<LootSearchResult?> = _activeLoot.asStateFlow()

    private val _activeNpcId = MutableStateFlow<String?>(null)
    val activeNpcId: StateFlow<String?> = _activeNpcId.asStateFlow()

    private val _selectedZone = MutableStateFlow(DeploymentZone.OAKRIDGE_HIGHWAY)
    val selectedZone: StateFlow<DeploymentZone> = _selectedZone.asStateFlow()

    private val _customDropCoords = MutableStateFlow<Pair<Float, Float>?>(null)
    val customDropCoords: StateFlow<Pair<Float, Float>?> = _customDropCoords.asStateFlow()

    private val _laptopInitialTab = MutableStateFlow(0)
    val laptopInitialTab: StateFlow<Int> = _laptopInitialTab.asStateFlow()

    private val _extractionSummary = MutableStateFlow<ExtractionSummary?>(null)
    val extractionSummary: StateFlow<ExtractionSummary?> = _extractionSummary.asStateFlow()

    private val _rescueMission = MutableStateFlow<RescueMissionAnimState?>(null)
    val rescueMission: StateFlow<RescueMissionAnimState?> = _rescueMission.asStateFlow()

    private val _permadeathReport = MutableStateFlow<PermadeathReport?>(null)
    val permadeathReport: StateFlow<PermadeathReport?> = _permadeathReport.asStateFlow()

    private val _playingCdId = MutableStateFlow<Int?>(null)
    val playingCdId: StateFlow<Int?> = _playingCdId.asStateFlow()

    private val _marketBusy = MutableStateFlow(false)
    val marketBusy: StateFlow<Boolean> = _marketBusy.asStateFlow()

    private val _buildModeActive = MutableStateFlow(false)
    val buildModeActive: StateFlow<Boolean> = _buildModeActive.asStateFlow()

    private val _selectedBuildPieceId = MutableStateFlow<String?>(null)
    val selectedBuildPieceId: StateFlow<String?> = _selectedBuildPieceId.asStateFlow()

    private val _pendingBuildKind = MutableStateFlow<BaseModuleKind?>(null)
    val pendingBuildKind: StateFlow<BaseModuleKind?> = _pendingBuildKind.asStateFlow()

    private val _pendingBuildRot = MutableStateFlow(0)
    val pendingBuildRot: StateFlow<Int> = _pendingBuildRot.asStateFlow()

    private val isSignedIn: Boolean get() = currentUserId.isNotBlank()

    private val _localKillFeed = MutableStateFlow<List<KillFeedEntry>>(emptyList())

    val liveKillFeed: StateFlow<List<KillFeedEntry>> = if (isSignedIn) {
        combine(
            marketRepo.observeLiveKillFeed(maxItems = 12)
                .map { remote ->
                    remote.map { ev ->
                        KillFeedEntry(
                            id = ev.id,
                            killerCallsign = ev.killerCallsign,
                            victimCallsign = ev.victimCallsign,
                            weaponName = ev.weaponName,
                            weaponKey = ev.weaponKey,
                            zoneName = ev.zoneName,
                            timestampMs = ev.createdAt?.toDate()?.time ?: System.currentTimeMillis()
                        )
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    emit(emptyList())
                },
            _localKillFeed
        ) { remote, local ->
            (local + remote)
                .distinctBy { "${it.killerCallsign}|${it.victimCallsign}|${it.weaponKey}|${it.timestampMs / 2500L}" }
                .sortedByDescending { it.timestampMs }
                .take(5)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )
    } else {
        _localKillFeed
            .map { it.take(5) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )
    }

    val activeListingsState: StateFlow<MarketUiState> = if (isSignedIn) {
        marketRepo.observeActiveListings()
            .map<List<MarketListing>, MarketUiState> { MarketUiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                emit(MarketUiState.Error(rootError.message ?: "Failed to load active market listings."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = MarketUiState.Loading
            )
    } else {
        flowOf<MarketUiState>(MarketUiState.Success(emptyList())).stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MarketUiState.Success(emptyList())
        )
    }

    val myListingsState: StateFlow<MarketUiState> = if (isSignedIn) {
        marketRepo.observeMyListings(currentUserId)
            .map<List<MarketListing>, MarketUiState> { MarketUiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                emit(MarketUiState.Error(rootError.message ?: "Failed to load your market listings."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = MarketUiState.Loading
            )
    } else {
        flowOf<MarketUiState>(MarketUiState.Success(emptyList())).stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MarketUiState.Success(emptyList())
        )
    }

    private var firstBootChecked = false
    private var cloudInitialized = false
    private var lastBaseConsumeMs = 0L
    private var lastBaseBunkLevel = 1
    private var lastBaseStorageLevel = 1
    private var lastBaseHelipadLevel = 0
    private var lastBaseLayout: BaseCustomLayout? = null
    private var lastDownedCheckMs = 0L
    private var evaluatingDowned = false

    init {
        applySettingsToEngine(_settings.value)
        simulation.onOperatorDowned = { onOperatorDownedInTown() }
        simulation.onStationTriggered = { kind -> handleBaseStation(kind) }
        simulation.onTalkNpc = { npcId ->
            _activeNpcId.value = npcId
            _activeModal.value = ActiveModal.NPC_DIALOGUE
        }
        simulation.onLootOpened = { result ->
            _activeLoot.value = result
            _activeModal.value = ActiveModal.LOOT_CONTAINER
        }
        simulation.onExtractTriggered = { exitName -> handleExtraction(exitName) }
        simulation.onZombieKilled = { incrementTaskProgress(TaskGoalKind.KILL_ZOMBIES, 1) }
        simulation.onContainerLooted = { incrementTaskProgress(TaskGoalKind.SEARCH_LOOT, 1) }
        simulation.onPlayerKilledPlayer = { killer, victim, weapon, zone ->
            recordPlayerKillEvent(killer, victim, weapon, zone)
        }
        simulation.onNamedScavKilled = { record ->
            val updated = ScavKinshipMemoryStore.upsertRecord(loadSlainScavMemory(), record)
            saveSlainScavMemory(updated)
        }
        simulation.onBaseNeedsConsume = { eat, drink ->
            viewModelScope.launch {
                val cur = _stash.value
                val nextFood = if (eat && cur.foodStock > 0) cur.foodStock - 1 else cur.foodStock
                val nextWater = if (drink && cur.waterStock > 0) cur.waterStock - 1 else cur.waterStock
                if (nextFood != cur.foodStock || nextWater != cur.waterStock) {
                    repo.updateBaseStash(cur.copy(foodStock = nextFood, waterStock = nextWater))
                }
            }
        }

        // Start immediately inside sealed Outpost Zero compound
        val initialLayout = _stash.value.baseLayout.ensureInfrastructure(_stash.value.bunkLevel, _stash.value.helipadLevel)
        simulation.buildLayoutPieces = initialLayout.pieces
        val ozMap = OutpostZeroMapBuilder.build(
            bunkLevel = _stash.value.bunkLevel,
            storageLevel = _stash.value.storageLevel,
            customLayout = initialLayout,
            helipadLevel = _stash.value.helipadLevel
        )
        simulation.loadWorld(ozMap, null, emptyList(), resetSurvival = true, baseStash = _stash.value)

        viewModelScope.launch {
            repo.ensureInitialized()
            combine(
                repo.operatorsFlow,
                repo.baseStashFlow,
                repo.collectedCdIdsFlow
            ) { ops, st, cds ->
                Triple(ops, st, cds)
            }.collect { (ops, st, cds) ->
                _roster.value = ops
                _stash.value = st
                _collectedCds.value = cds

                val chosen = ops.firstOrNull { it.id == st.activeOperatorId && !it.isKia && !it.isPermadead }
                    ?: ops.firstOrNull { !it.isKia && !it.isPermadead }

                simulation.rosterOperators = ops
                simulation.downedFriendlyBeacons = ops.filter { it.isKia && !it.isPermadead }
                simulation.activeOperator = chosen
                val ensuredLayout = st.baseLayout.ensureInfrastructure(st.bunkLevel, st.helipadLevel)
                simulation.buildLayoutPieces = ensuredLayout.pieces
                if (simulation.world?.mapId == GameMapId.OUTPOST_ZERO) {
                    if (st.bunkLevel != lastBaseBunkLevel ||
                        st.storageLevel != lastBaseStorageLevel ||
                        st.helipadLevel != lastBaseHelipadLevel ||
                        ensuredLayout != lastBaseLayout
                    ) {
                        lastBaseBunkLevel = st.bunkLevel
                        lastBaseStorageLevel = st.storageLevel
                        lastBaseHelipadLevel = st.helipadLevel
                        lastBaseLayout = ensuredLayout
                        val prevX = simulation.playerX
                        val prevY = simulation.playerY
                        val refreshedMap = OutpostZeroMapBuilder.build(st.bunkLevel, st.storageLevel, ensuredLayout, st.helipadLevel)
                        simulation.loadWorld(refreshedMap, chosen, ops, resetSurvival = false, baseStash = st)
                        simulation.playerX = prevX
                        simulation.playerY = prevY
                        simulation.nudgePlayerClearOfWalls()
                    } else {
                        simulation.rebuildBaseNpcs(ops, chosen?.id)
                        simulation.syncBaseArmory(st, chosen, pickBestWeapon = false)
                    }
                }
                if (chosen != null && st.activeOperatorId != chosen.id) {
                    repo.updateBaseStash(st.copy(activeOperatorId = chosen.id))
                }

                if (isSignedIn && !cloudInitialized) {
                    cloudInitialized = true
                    val seedCallsign = chosen?.callsign ?: "SURVIVOR"
                    marketRepo.ensureUserStash(currentUserId, seedCallsign, st).onSuccess { cloud ->
                        val merged = cloud.toBaseStashState(
                            activeOperatorId = chosen?.id ?: st.activeOperatorId,
                            foodStock = st.foodStock,
                            waterStock = st.waterStock,
                            medsStock = st.medsStock
                        )
                        if (merged != st) {
                            repo.updateBaseStash(merged)
                        }
                    }
                }

                if (!firstBootChecked) {
                    firstBootChecked = true
                    lastBaseBunkLevel = st.bunkLevel
                    lastBaseStorageLevel = st.storageLevel
                    lastBaseHelipadLevel = st.helipadLevel
                    if (chosen != null) {
                        simulation.maxHp = chosen.maxHp.toFloat()
                        simulation.hp = chosen.maxHp.toFloat()
                        simulation.syncBaseArmory(st, chosen, pickBestWeapon = true)
                    }
                    evaluateDownedOperators(System.currentTimeMillis())
                }
            }
        }

        if (isSignedIn) {
            viewModelScope.launch {
                marketRepo.observeUserStash(currentUserId)
                    .catch { error ->
                        val firestoreError = generateSequence(error) { it.cause }
                            .filterIsInstance<FirebaseFirestoreException>()
                            .firstOrNull()
                        if (firestoreError == null && error is CancellationException) throw error
                    }
                    .collect { cloud ->
                        if (cloud != null) {
                            val cur = _stash.value
                            val synced = cloud.toBaseStashState(
                                activeOperatorId = cur.activeOperatorId,
                                foodStock = cur.foodStock,
                                waterStock = cur.waterStock,
                                medsStock = cur.medsStock
                            )
                            if (synced != cur) {
                                repo.updateBaseStash(synced)
                                simulation.syncBaseArmory(synced, simulation.activeOperator, pickBestWeapon = false)
                            }
                        }
                    }
            }
        }
    }

    fun enterGameFromSplash() {
        _showSplash.value = false
        evaluateDownedOperators(System.currentTimeMillis())
        val living = _roster.value.any { !it.isKia && !it.isPermadead }
        if (!living && _roster.value.isEmpty() && _activeModal.value == ActiveModal.NONE) {
            _activeModal.value = ActiveModal.CHARACTER_CREATOR
        }
    }

    fun returnToSplashScreen() {
        audio.stopCdPreview()
        _playingCdId.value = null
        _activeModal.value = ActiveModal.NONE
        toggleBaseBuildMode(false)
        _showSplash.value = true
    }

    fun updateSettings(transform: (GameSettingsState) -> GameSettingsState) {
        val next = transform(_settings.value)
        _settings.value = next
        saveSettingsToPrefs(next)
        applySettingsToEngine(next)
    }

    fun setCameraZoom(zoom: Float) {
        val clamped = com.example.game.model.CameraViewBounds.clampZoom(zoom)
        updateSettings { it.copy(cameraZoom = clamped) }
    }

    fun adjustCameraZoom(delta: Float) {
        setCameraZoom(_settings.value.cameraZoom + delta)
    }

    private fun applySettingsToEngine(st: GameSettingsState) {
        audio.muted = st.muteAudio
        if (st.muteAudio) {
            audio.stopCdPreview()
            _playingCdId.value = null
        }
        simulation.highVisibilityNight = st.highVisibilityNight
        simulation.setAmbientWind(st.ambientWind)
        simulation.autoFire = st.autoFireDefault
        simulation.setCameraZoomClamped(st.cameraZoom)
        simulation.setVoxelDepthClamped(st.voxelDepthScale)
    }

    private fun loadSettingsFromPrefs(): GameSettingsState {
        val p = prefs ?: return GameSettingsState()
        return GameSettingsState(
            muteAudio = p.getBoolean("mute_audio", false),
            autoFireDefault = p.getBoolean("auto_fire_default", true),
            showMinimap = p.getBoolean("show_minimap", true),
            rotateMinimap = p.getBoolean("rotate_minimap", false),
            ambientWind = p.getBoolean("ambient_wind", true),
            showTaskTrackers = p.getBoolean("show_task_trackers", true),
            highVisibilityNight = p.getBoolean("high_vis_night", false),
            cameraZoom = com.example.game.model.CameraViewBounds.clampZoom(
                p.getFloat("camera_zoom", com.example.game.model.CameraViewBounds.DEFAULT_CAMERA_ZOOM)
            ),
            voxelDepthScale = com.example.game.model.CameraViewBounds.clampVoxelDepth(
                p.getFloat("voxel_depth_scale", com.example.game.model.CameraViewBounds.DEFAULT_VOXEL_DEPTH)
            )
        )
    }

    private fun saveSettingsToPrefs(st: GameSettingsState) {
        prefs?.edit()?.apply {
            putBoolean("mute_audio", st.muteAudio)
            putBoolean("auto_fire_default", st.autoFireDefault)
            putBoolean("show_minimap", st.showMinimap)
            putBoolean("rotate_minimap", st.rotateMinimap)
            putBoolean("ambient_wind", st.ambientWind)
            putBoolean("show_task_trackers", st.showTaskTrackers)
            putBoolean("high_vis_night", st.highVisibilityNight)
            putFloat("camera_zoom", com.example.game.model.CameraViewBounds.clampZoom(st.cameraZoom))
            putFloat("voxel_depth_scale", com.example.game.model.CameraViewBounds.clampVoxelDepth(st.voxelDepthScale))
            apply()
        }
    }

    private fun loadSlainScavMemory(): List<SlainScavRecord> {
        val raw = prefs?.getString("slain_scav_memory_v1", null) ?: return emptyList()
        return ScavKinshipMemoryStore.decode(raw)
    }

    private fun saveSlainScavMemory(records: List<SlainScavRecord>) {
        prefs?.edit()?.putString("slain_scav_memory_v1", ScavKinshipMemoryStore.encode(records))?.apply()
    }

    fun tickFrame(dt: Float) {
        if (_showSplash.value) return
        val now = System.currentTimeMillis()
        if (now - lastDownedCheckMs > 1500L) {
            lastDownedCheckMs = now
            evaluateDownedOperators(now)
        }
        if (_activeModal.value == ActiveModal.NONE) {
            _hud.value = simulation.step(dt)
        }
    }

    fun openModal(modal: ActiveModal) {
        if (modal == ActiveModal.CHARACTER_CREATOR &&
            _roster.value.isNotEmpty() &&
            _roster.value.size >= _stash.value.maxRosterSlots
        ) {
            simulation.showToast("NO OPEN BUNK (${_roster.value.size}/${_stash.value.maxRosterSlots}) — BUY BUNK UPGRADE FIRST")
            _laptopInitialTab.value = 2
            _activeModal.value = ActiveModal.LAPTOP
            return
        }
        if (modal == ActiveModal.LAPTOP) {
            _laptopInitialTab.value = 0
        }
        _activeModal.value = modal
    }

    fun openLaptopTab(tab: Int) {
        _laptopInitialTab.value = tab.coerceIn(0, 3)
        _activeModal.value = ActiveModal.LAPTOP
    }

    fun closeModal() {
        _activeModal.value = ActiveModal.NONE
        audio.stopCdPreview()
        _playingCdId.value = null
    }

    fun selectDeploymentZone(zone: DeploymentZone) {
        _selectedZone.value = zone
    }

    fun createOperator(
        callsign: String,
        fullName: String,
        archetype: OperatorArchetype,
        sex: OperatorSex,
        skinTone: SkinToneOption,
        shirtStyle: ShirtStyle,
        pantsStyle: PantsStyle,
        shoeStyle: ShoeStyle,
        hatStyle: HatStyle,
        glassesStyle: GlassesStyle,
        glovesStyle: GlovesStyle
    ) {
        val current = _roster.value
        val curStash = _stash.value
        val maxBunks = curStash.maxRosterSlots
        if (current.size >= maxBunks) {
            simulation.showToast("BARRACKS FULL ($maxBunks/$maxBunks BUNKS) — UPGRADE BUNKS AT LAPTOP")
            return
        }
        val credCost = BaseUpgradeSpecs.recruitOperatorCredCost(current.size)
        val matsCost = BaseUpgradeSpecs.recruitOperatorMats(current.size)
        if (curStash.credits < credCost || !curStash.canAffordMats(matsCost)) {
            simulation.showToast("NEED $credCost CRED & REQUIRED MATERIALS TO RECRUIT")
            return
        }

        val usedSlots = current.map { it.slotIndex }.toSet()
        val nextSlot = (0 until maxBunks).firstOrNull { it !in usedSlots } ?: current.size
        val startingPants = curStash.maxPantsLevel.coerceIn(1, 4)
        val startingArmor = curStash.maxArmorLevel.coerceIn(0, 4)
        val dogMatch = DogBreedResolver.resolve(
            shirt = shirtStyle,
            pants = pantsStyle,
            hat = hatStyle,
            glasses = glassesStyle,
            gloves = glovesStyle
        )
        val newOp = OperatorProfile(
            id = UUID.randomUUID().toString(),
            slotIndex = nextSlot,
            callsign = callsign.trim().uppercase().ifBlank { "ROOKIE-${nextSlot + 1}" },
            fullName = fullName.trim().ifBlank { "DeLaine Survivor" },
            archetype = archetype,
            skinHex = skinTone.colorHex,
            shirtHex = shirtStyle.primaryHex,
            vestHex = archetype.defaultVestHex,
            packHex = archetype.defaultPackHex,
            pantsLevel = startingPants,
            armorLevel = startingArmor,
            dogFound = true,
            kiaCount = 0,
            isKia = false,
            kiaTimestamp = 0L,
            infirmaryUntil = 0L,
            safePocketItems = emptyList(),
            backpackItems = emptyList(),
            sex = sex,
            skinTone = skinTone,
            shirtStyle = shirtStyle,
            pantsStyle = pantsStyle,
            shoeStyle = shoeStyle,
            hatStyle = hatStyle,
            glassesStyle = glassesStyle,
            glovesStyle = glovesStyle,
            pairedDogBreed = dogMatch.breed,
            pairedDogCoatHex = dogMatch.coatColorHex
        )

        var nextStash = matsCost.entries.fold(
            curStash.copy(credits = curStash.credits - credCost)
        ) { acc, (m, amt) -> acc.withMaterialDelta(m, -amt, clampToStorageCap = false) }

        val shouldActivate = nextStash.activeOperatorId.isBlank() || simulation.activeOperator == null
        if (shouldActivate) {
            nextStash = nextStash.copy(activeOperatorId = newOp.id)
        }

        viewModelScope.launch {
            repo.saveOperator(newOp)
            if (credCost > 0 || matsCost.isNotEmpty() || shouldActivate) {
                repo.updateBaseStash(nextStash)
                syncStashToCloud(nextStash, newOp.callsign)
            }
            if (shouldActivate) {
                simulation.maxHp = newOp.maxHp.toFloat()
                simulation.hp = newOp.maxHp.toFloat()
                simulation.hunger = 88f
                simulation.thirst = 88f
                simulation.syncBaseArmory(nextStash, newOp, pickBestWeapon = true)
            }
            _activeModal.value = ActiveModal.NONE
            simulation.showToast("${newOp.callsign} & ${dogMatch.breed.uppercase()} CHECKED INTO BUNK #${nextSlot + 1}")
        }
    }

    fun selectActiveOperator(operatorId: String) {
        val op = _roster.value.firstOrNull { it.id == operatorId && !it.isKia && !it.isPermadead } ?: return
        val npcRuntime = simulation.baseNpcs.firstOrNull { it.operatorId == op.id }
        viewModelScope.launch {
            val st = _stash.value.copy(activeOperatorId = op.id)
            repo.updateBaseStash(st)
            simulation.activeOperator = op
            simulation.maxHp = op.maxHp.toFloat()
            simulation.hp = simulation.hp.coerceIn(25f, op.maxHp.toFloat())
            if (npcRuntime != null) {
                simulation.hunger = npcRuntime.hunger.coerceIn(20f, 100f)
                simulation.thirst = npcRuntime.thirst.coerceIn(20f, 100f)
            }
            simulation.syncBaseArmory(st, op, pickBestWeapon = true)
            simulation.rebuildBaseNpcs(_roster.value, op.id)
            _activeModal.value = ActiveModal.NONE
            simulation.showToast("ACTIVE OPERATOR: ${op.callsign}")
        }
    }

    fun deployToOakridge(zone: DeploymentZone = _selectedZone.value, customDrop: Pair<Float, Float>? = null) {
        val active = simulation.activeOperator ?: run {
            _activeModal.value = ActiveModal.CHARACTER_CREATOR
            return
        }
        toggleBaseBuildMode(false)
        val hasHeli = _stash.value.hasHelipad
        val effectiveZone = if (hasHeli) zone else DeploymentZone.OAKRIDGE_HIGHWAY
        val effectiveCustomDrop = if (hasHeli) customDrop else null
        _selectedZone.value = effectiveZone
        _customDropCoords.value = effectiveCustomDrop
        val readyOp = if (active.dogFound) active else active.copy(dogFound = true)
        if (!active.dogFound) {
            persistActiveOp(readyOp)
        }
        val townMap = OakridgeMapBuilder.build(
            runSeed = System.currentTimeMillis(),
            zone = effectiveZone,
            customDropX = effectiveCustomDrop?.first,
            customDropY = effectiveCustomDrop?.second
        )
        val slainMemory = loadSlainScavMemory()
        _activeModal.value = ActiveModal.NONE
        simulation.loadWorld(
            map = townMap,
            operator = readyOp,
            roster = _roster.value,
            resetSurvival = false,
            baseStash = _stash.value,
            slainScavMemory = slainMemory
        )
        val dropToast = when {
            !hasHeli -> "DELAINE SOUTH GATE PATROL WITH ${readyOp.dogBreed.uppercase()}"
            effectiveCustomDrop != null -> "PRECISION AIR-DROP AT (${townMap.spawnX.toInt()}, ${townMap.spawnY.toInt()}) WITH ${readyOp.dogBreed.uppercase()}"
            else -> "HELIPAD DROP AT ${effectiveZone.title.substringAfter("// ").trim()} WITH ${readyOp.dogBreed.uppercase()}"
        }
        simulation.showToast(dropToast)
    }

    fun returnToOutpostBase() {
        val living = _roster.value.firstOrNull { it.id == _stash.value.activeOperatorId && !it.isKia && !it.isPermadead }
            ?: _roster.value.firstOrNull { !it.isKia && !it.isPermadead }
        val st = _stash.value
        val ensuredLayout = st.baseLayout.ensureInfrastructure(st.bunkLevel, st.helipadLevel)
        lastBaseBunkLevel = st.bunkLevel
        lastBaseStorageLevel = st.storageLevel
        lastBaseHelipadLevel = st.helipadLevel
        lastBaseLayout = ensuredLayout
        simulation.buildLayoutPieces = ensuredLayout.pieces
        val ozMap = OutpostZeroMapBuilder.build(st.bunkLevel, st.storageLevel, ensuredLayout, st.helipadLevel)
        // Preserve post-raid vitals (never conjure water/food/HP from nowhere — walk to Cantine & Infirmary)
        simulation.hp = simulation.hp.coerceIn(15f, (living?.maxHp ?: 100).toFloat())
        simulation.loadWorld(ozMap, living, _roster.value, resetSurvival = false, baseStash = st)
        _activeModal.value = if (living == null && _roster.value.any { it.isKia && !it.isPermadead }) {
            ActiveModal.LAPTOP
        } else {
            ActiveModal.NONE
        }
    }

    private fun handleBaseStation(kind: StationKind) {
        when (kind) {
            StationKind.LAPTOP -> _activeModal.value = ActiveModal.LAPTOP
            StationKind.WORKBENCH -> _activeModal.value = ActiveModal.WORKBENCH
            StationKind.MARKET -> _activeModal.value = ActiveModal.MARKET
            StationKind.CD_RACK -> _activeModal.value = ActiveModal.CD_BINDER
            StationKind.HELIPAD, StationKind.EXIT_GATE -> {
                if (simulation.activeOperator == null) {
                    _activeModal.value = ActiveModal.CHARACTER_CREATOR
                } else {
                    _activeModal.value = ActiveModal.HELIPAD
                }
            }
            StationKind.STORAGE_DEPOT -> {
                val op = simulation.activeOperator
                val carried = (op?.backpackItems.orEmpty() + op?.safePocketItems.orEmpty())
                    .filter { it.type == LootType.MATERIAL }
                    .sumOf { it.quantity.coerceAtLeast(1) }
                if (carried > 0) {
                    depositCarriedMaterials()
                } else {
                    val st = _stash.value
                    simulation.showToast("DEPOT LV.${st.storageLevel} • CAP ${st.storageMaterialCap}/MAT • BAG ${st.backpackSlotCap} SLOTS")
                }
            }
            StationKind.MESS_FOOD -> {
                val cur = _stash.value
                if (cur.foodStock > 0) {
                    simulation.hunger = (simulation.hunger + 40f).coerceAtMost(100f)
                    simulation.showToast("ATE CANTINE RATION (+40 FOOD)")
                    viewModelScope.launch { repo.updateBaseStash(cur.copy(foodStock = cur.foodStock - 1)) }
                } else {
                    simulation.showToast("CANTINE STOCKPILE EMPTY — CRAFT RATIONS")
                }
            }
            StationKind.WATER_COOLER -> {
                val cur = _stash.value
                if (cur.waterStock > 0) {
                    simulation.thirst = (simulation.thirst + 45f).coerceAtMost(100f)
                    simulation.showToast("DRANK FILTERED WATER (+45 WATER)")
                    viewModelScope.launch { repo.updateBaseStash(cur.copy(waterStock = cur.waterStock - 1)) }
                } else {
                    simulation.showToast("WATER STOCKPILE EMPTY — CRAFT CANTEENS")
                }
            }
            StationKind.BUNK -> {
                val now = System.currentTimeMillis()
                val restCap = simulation.maxHp * 0.75f
                if (now - lastBaseConsumeMs < 15_000L) {
                    simulation.showToast("RESTED RECENTLY — VISIT INFIRMARY FOR FULL TREATMENT")
                } else if (simulation.hp >= restCap) {
                    simulation.showToast("BUNK REST MAX 75% HP — USE INFIRMARY MEDS FOR FULL HEAL")
                } else {
                    lastBaseConsumeMs = now
                    simulation.hp = (simulation.hp + 12f).coerceAtMost(restCap)
                    simulation.showToast("RESTED IN BARRACKS BUNK (+12 HP)")
                }
            }
            StationKind.INFIRMARY_BED -> {
                val cur = _stash.value
                if (simulation.hp < simulation.maxHp && cur.medsStock > 0) {
                    simulation.hp = simulation.maxHp
                    simulation.showToast("PATCHED UP AT INFIRMARY (FULL HP)")
                    viewModelScope.launch { repo.updateBaseStash(cur.copy(medsStock = cur.medsStock - 1)) }
                } else {
                    simulation.showToast("INFIRMARY READY — MED STOCK: ${cur.medsStock}")
                }
            }
        }
    }

    fun depositCarriedMaterials() {
        val op = simulation.activeOperator ?: return
        var curStash = _stash.value
        var deposited = 0
        val keptSafe = mutableListOf<InventoryItem>()
        val keptBag = mutableListOf<InventoryItem>()

        for (item in op.safePocketItems) {
            val mat = if (item.type == LootType.MATERIAL) CraftingMaterial.fromKey(item.materialKey) else null
            if (mat != null) {
                val qty = item.quantity.coerceAtLeast(1)
                curStash = curStash.withMaterialDelta(mat, qty)
                deposited += qty
            } else {
                keptSafe.add(item)
            }
        }
        for (item in op.backpackItems) {
            val mat = if (item.type == LootType.MATERIAL) CraftingMaterial.fromKey(item.materialKey) else null
            if (mat != null) {
                val qty = item.quantity.coerceAtLeast(1)
                curStash = curStash.withMaterialDelta(mat, qty)
                deposited += qty
            } else {
                keptBag.add(item)
            }
        }

        if (deposited <= 0) {
            simulation.showToast("NO CARRIED MATERIALS TO DEPOSIT")
            return
        }

        val updatedOp = op.copy(safePocketItems = keptSafe, backpackItems = keptBag)
        simulation.activeOperator = updatedOp
        viewModelScope.launch {
            repo.saveOperator(updatedOp)
            repo.updateBaseStash(curStash)
            syncStashToCloud(curStash, updatedOp.callsign)
            audio.playLootPickup()
            simulation.showToast("DEPOSITED +$deposited MATERIALS (CAP ${curStash.storageMaterialCap}/MAT)")
        }
    }

    fun craftRecipe(recipe: CraftingRecipe) {
        val cur = _stash.value
        if (recipe.requiredReloadingTier > cur.reloadingGearLevel) {
            simulation.showToast("REQUIRES HQ RELOADING GEAR LV.${recipe.requiredReloadingTier} — UPGRADE AT TERMINAL")
            return
        }
        if (!cur.canAfford(recipe)) {
            simulation.showToast("INSUFFICIENT MATERIALS FOR ${recipe.name.uppercase()}")
            return
        }

        var nextStash = recipe.costs.entries.fold(cur) { acc, (mat, needed) ->
            acc.withMaterialDelta(mat, -needed, clampToStorageCap = false)
        }
        var updatedOp = simulation.activeOperator
        val bagCap = nextStash.backpackSlotCap

        when (recipe.outputKind) {
            CraftOutputKind.WEAPON -> {
                val wKey = recipe.weaponKey ?: "knife"
                if (wKey !in nextStash.unlockedWeaponKeys) {
                    nextStash = addAmmoToStash(nextStash, wKey, recipe.ammoAmount).withRecordedWeapons(setOf(wKey))
                } else if (updatedOp != null && updatedOp.backpackItems.size < bagCap) {
                    val w = WeaponId.fromKey(wKey)
                    val spareItem = InventoryItem(
                        uid = "craft_wpn_${UUID.randomUUID().toString().take(8)}",
                        type = LootType.WEAPON,
                        name = w.displayName,
                        detail = "${w.caliber} • Crafted Receiver",
                        value = w.damage * 4 + 100,
                        weaponKey = w.key
                    )
                    updatedOp = updatedOp.copy(backpackItems = updatedOp.backpackItems + spareItem)
                } else {
                    nextStash = addAmmoToStash(nextStash, wKey, recipe.ammoAmount)
                }
            }
            CraftOutputKind.ATTACHMENT -> {
                val attKey = recipe.attachmentKey
                if (!attKey.isNullOrBlank()) {
                    nextStash = nextStash.withUnlockedAttachments(setOf(attKey))
                }
            }
            CraftOutputKind.AMMO -> {
                nextStash = addAmmoToStash(nextStash, recipe.weaponKey, recipe.ammoAmount)
            }
            CraftOutputKind.PANTS -> {
                val lvl = (recipe.pantsLevel ?: 2).coerceIn(1, 4)
                if (lvl > nextStash.maxPantsLevel) {
                    nextStash = nextStash.copy(maxPantsLevel = lvl)
                    if (updatedOp != null && updatedOp.pantsLevel < lvl) {
                        updatedOp = updatedOp.copy(pantsLevel = lvl)
                    }
                } else if (updatedOp != null && updatedOp.backpackItems.size < bagCap) {
                    val sparePants = InventoryItem(
                        uid = "craft_pants_${UUID.randomUUID().toString().take(8)}",
                        type = LootType.PANTS,
                        name = recipe.name,
                        detail = recipe.detail,
                        value = lvl * 90,
                        pantsLevel = lvl
                    )
                    updatedOp = updatedOp.copy(backpackItems = updatedOp.backpackItems + sparePants)
                }
            }
            CraftOutputKind.ARMOR -> {
                val lvl = (recipe.armorLevel ?: 1).coerceIn(1, 4)
                if (lvl > nextStash.maxArmorLevel) {
                    nextStash = nextStash.copy(maxArmorLevel = lvl)
                }
                if (updatedOp != null && updatedOp.armorLevel < lvl) {
                    updatedOp = updatedOp.copy(armorLevel = lvl)
                } else if (updatedOp != null && updatedOp.backpackItems.size < bagCap) {
                    val spareArmor = InventoryItem(
                        uid = "craft_armor_${UUID.randomUUID().toString().take(8)}",
                        type = LootType.ARMOR,
                        name = recipe.name,
                        detail = recipe.detail,
                        value = lvl * 120,
                        armorLevel = lvl
                    )
                    updatedOp = updatedOp.copy(backpackItems = updatedOp.backpackItems + spareArmor)
                }
            }
            CraftOutputKind.CONSUMABLE -> {
                val cType = recipe.consumableType ?: LootType.MEDKIT
                if (updatedOp != null && updatedOp.backpackItems.size < bagCap) {
                    val craftedItem = InventoryItem(
                        uid = "craft_${cType.name.lowercase()}_${UUID.randomUUID().toString().take(8)}",
                        type = cType,
                        name = recipe.name,
                        detail = recipe.detail,
                        value = recipe.consumableValue
                    )
                    updatedOp = updatedOp.copy(backpackItems = updatedOp.backpackItems + craftedItem)
                } else {
                    nextStash = when (cType) {
                        LootType.MEDKIT -> nextStash.copy(medsStock = nextStash.medsStock + 1)
                        LootType.FOOD -> nextStash.copy(foodStock = nextStash.foodStock + 1)
                        LootType.WATER -> nextStash.copy(waterStock = nextStash.waterStock + 1)
                        else -> nextStash
                    }
                }
            }
        }

        nextStash = applyTaskProgressToStash(nextStash, TaskGoalKind.CRAFT_GEAR, 1)

        viewModelScope.launch {
            if (updatedOp != null && updatedOp != simulation.activeOperator) {
                repo.saveOperator(updatedOp)
                simulation.activeOperator = updatedOp
            }
            repo.updateBaseStash(nextStash)
            syncStashToCloud(nextStash, updatedOp?.callsign ?: "SURVIVOR")
            simulation.syncBaseArmory(nextStash, updatedOp, pickBestWeapon = true)
            audio.playLootPickup()
            simulation.showToast("CRAFTED: ${recipe.name.uppercase()}")
        }
    }

    // --- Base Upgrades (Barracks Bunks & Storage Depot) ---

    fun upgradeBunk() {
        val cur = _stash.value
        if (cur.bunkLevel >= BaseUpgradeSpecs.MAX_LEVEL) return
        val credCost = BaseUpgradeSpecs.bunkUpgradeCredCost(cur.bunkLevel)
        val matsCost = BaseUpgradeSpecs.bunkUpgradeMats(cur.bunkLevel)
        if (cur.credits < credCost || !cur.canAffordMats(matsCost)) {
            simulation.showToast("NEED $credCost CRED & REQUIRED MATERIALS FOR BUNK UPGRADE")
            return
        }
        var next = matsCost.entries.fold(cur.copy(credits = cur.credits - credCost, bunkLevel = cur.bunkLevel + 1)) { acc, (m, amt) ->
            acc.withMaterialDelta(m, -amt, clampToStorageCap = false)
        }
        next = applyTaskProgressToStash(next, TaskGoalKind.UPGRADE_BASE, 1)
        viewModelScope.launch {
            repo.updateBaseStash(next)
            syncStashToCloud(next, simulation.activeOperator?.callsign ?: "SURVIVOR")
            audio.playLootPickup()
            simulation.showToast("BARRACKS UPGRADED TO LV.${next.bunkLevel} (${next.maxRosterSlots} OPERATOR SPOTS)")
        }
    }

    fun upgradeStorage() {
        val cur = _stash.value
        if (cur.storageLevel >= BaseUpgradeSpecs.MAX_LEVEL) return
        val credCost = BaseUpgradeSpecs.storageUpgradeCredCost(cur.storageLevel)
        val matsCost = BaseUpgradeSpecs.storageUpgradeMats(cur.storageLevel)
        if (cur.credits < credCost || !cur.canAffordMats(matsCost)) {
            simulation.showToast("NEED $credCost CRED & REQUIRED MATERIALS FOR STORAGE UPGRADE")
            return
        }
        var next = matsCost.entries.fold(cur.copy(credits = cur.credits - credCost, storageLevel = cur.storageLevel + 1)) { acc, (m, amt) ->
            acc.withMaterialDelta(m, -amt, clampToStorageCap = false)
        }
        next = applyTaskProgressToStash(next, TaskGoalKind.UPGRADE_BASE, 1)
        viewModelScope.launch {
            repo.updateBaseStash(next)
            syncStashToCloud(next, simulation.activeOperator?.callsign ?: "SURVIVOR")
            audio.playLootPickup()
            simulation.showToast("STORAGE UPGRADED TO LV.${next.storageLevel} (CAP ${next.storageMaterialCap} • ${next.backpackSlotCap} BAG)")
        }
    }

    fun upgradeHelipad() {
        val cur = _stash.value
        if (cur.helipadLevel >= BaseUpgradeSpecs.MAX_HELIPAD_LEVEL) return
        val credCost = BaseUpgradeSpecs.helipadUpgradeCredCost(cur.helipadLevel)
        val matsCost = BaseUpgradeSpecs.helipadUpgradeMats(cur.helipadLevel)
        if (cur.credits < credCost || !cur.canAffordMats(matsCost)) {
            simulation.showToast("NEED $credCost CRED & REQUIRED MATERIALS FOR HELIPAD")
            return
        }
        val upgradedLayout = cur.baseLayout.ensureInfrastructure(cur.bunkLevel, BaseUpgradeSpecs.MAX_HELIPAD_LEVEL)
        var next = matsCost.entries.fold(
            cur.copy(
                credits = cur.credits - credCost,
                helipadLevel = BaseUpgradeSpecs.MAX_HELIPAD_LEVEL,
                baseLayout = upgradedLayout
            )
        ) { acc, (m, amt) ->
            acc.withMaterialDelta(m, -amt, clampToStorageCap = false)
        }
        next = applyTaskProgressToStash(next, TaskGoalKind.UPGRADE_BASE, 1)
        viewModelScope.launch {
            repo.updateBaseStash(next)
            syncStashToCloud(next, simulation.activeOperator?.callsign ?: "SURVIVOR")
            audio.playLootPickup()
            simulation.showToast("EAST HELIPAD BUILT — PRECISION DROP ANYWHERE UNLOCKED!")
        }
    }

    fun upgradeReloadingGear() {
        val cur = _stash.value
        if (cur.reloadingGearLevel >= BaseUpgradeSpecs.MAX_RELOADING_GEAR_LEVEL) return
        val credCost = BaseUpgradeSpecs.reloadingUpgradeCredCost(cur.reloadingGearLevel)
        val matsCost = BaseUpgradeSpecs.reloadingUpgradeMats(cur.reloadingGearLevel)
        if (cur.credits < credCost || !cur.canAffordMats(matsCost)) {
            simulation.showToast("NEED $credCost CRED & REQUIRED MATERIALS FOR RELOADING GEAR")
            return
        }
        var next = matsCost.entries.fold(
            cur.copy(credits = cur.credits - credCost, reloadingGearLevel = cur.reloadingGearLevel + 1)
        ) { acc, (m, amt) ->
            acc.withMaterialDelta(m, -amt, clampToStorageCap = false)
        }
        next = applyTaskProgressToStash(next, TaskGoalKind.UPGRADE_BASE, 1)
        viewModelScope.launch {
            repo.updateBaseStash(next)
            syncStashToCloud(next, simulation.activeOperator?.callsign ?: "SURVIVOR")
            audio.playLootPickup()
            simulation.showToast("HQ RELOADING GEAR UPGRADED TO LV.${next.reloadingGearLevel}!")
        }
    }

    // --- HQ Missions & Survivor Favors ---

    fun acceptFavor(favorId: String) {
        val favor = SurvivorCatalog.favorById(favorId) ?: return
        val cur = _stash.value
        val next = cur.copy(
            activeFavorId = favor.id,
            activeFavorProgress = 0
        )
        viewModelScope.launch {
            repo.updateBaseStash(next)
            simulation.showToast("ACCEPTED FAVOR FROM ${favor.npcName.uppercase()}: ${favor.title.uppercase()}")
        }
    }

    fun claimFavorReward() {
        val cur = _stash.value
        val favor = SurvivorCatalog.favorById(cur.activeFavorId) ?: return
        if (cur.activeFavorProgress < favor.targetCount) return
        var next = cur.copy(
            credits = cur.credits + favor.rewardCred,
            activeFavorId = "",
            activeFavorProgress = 0,
            completedFavorIds = cur.completedFavorIds + favor.id
        )
        favor.rewardMats.forEach { (mat, qty) ->
            next = next.withMaterialDelta(mat, qty)
        }
        viewModelScope.launch {
            repo.updateBaseStash(next)
            syncStashToCloud(next, simulation.activeOperator?.callsign ?: "SURVIVOR")
            audio.playLootPickup()
            simulation.showToast("FAVOR REWARDED: +${favor.rewardCred} CRED")
        }
    }

    fun claimMissionReward() {
        val cur = _stash.value
        val mission = HqMissionCatalog.byId(cur.activeMissionId)
        if (mission.id in cur.completedMissionIds || cur.activeMissionProgress < mission.targetCount) return

        val nextCompleted = cur.completedMissionIds + mission.id
        val nextMission = HqMissionCatalog.missions.firstOrNull { it.id !in nextCompleted } ?: mission
        var next = cur.copy(
            credits = cur.credits + mission.rewardCred,
            completedMissionIds = nextCompleted,
            activeMissionId = nextMission.id,
            activeMissionProgress = 0
        )
        mission.rewardMats.forEach { (mat, qty) ->
            next = next.withMaterialDelta(mat, qty)
        }
        viewModelScope.launch {
            repo.updateBaseStash(next)
            syncStashToCloud(next, simulation.activeOperator?.callsign ?: "SURVIVOR")
            audio.playLootPickup()
            simulation.showToast("HQ MISSION CLEARED: +${mission.rewardCred} CRED")
        }
    }

    private fun incrementTaskProgress(kind: TaskGoalKind, amount: Int = 1) {
        viewModelScope.launch {
            val cur = _stash.value
            val updated = applyTaskProgressToStash(cur, kind, amount)
            if (updated != cur) {
                repo.updateBaseStash(updated)
            }
        }
    }

    private fun applyTaskProgressToStash(stash: BaseStashState, kind: TaskGoalKind, amount: Int): BaseStashState {
        var next = stash
        val mission = HqMissionCatalog.byId(stash.activeMissionId)
        if (mission.id !in stash.completedMissionIds && mission.goalKind == kind) {
            next = next.copy(
                activeMissionProgress = (stash.activeMissionProgress + amount).coerceAtMost(mission.targetCount)
            )
        }
        val favor = SurvivorCatalog.favorById(stash.activeFavorId)
        if (favor != null && favor.id !in stash.completedFavorIds && favor.goalKind == kind) {
            next = next.copy(
                activeFavorProgress = (stash.activeFavorProgress + amount).coerceAtMost(favor.targetCount)
            )
        }
        return next
    }

    // --- Live Market ---

    fun buyMarketListing(listing: MarketListing) {
        if (!isSignedIn) {
            simulation.showToast("LIVE MARKET PAUSED WHILE SIGN-IN IS DISABLED")
            return
        }
        if (_marketBusy.value) return
        if (listing.sellerId == currentUserId) {
            simulation.showToast("CANNOT BUY YOUR OWN LISTING")
            return
        }
        if (_stash.value.credits < listing.priceCredits) {
            simulation.showToast("NEED ${listing.priceCredits} CRED TO PURCHASE")
            return
        }

        _marketBusy.value = true
        viewModelScope.launch {
            val result = marketRepo.purchaseListing(listing.id)
            result.onSuccess { outcome ->
                var nextStash = _stash.value.copy(credits = outcome.updatedBuyerCredits)
                var updatedOp = simulation.activeOperator
                val bagCap = nextStash.backpackSlotCap

                when (listing.category) {
                    "WEAPON" -> {
                        val wk = listing.weaponKey
                        if (wk.isNotBlank() && wk !in nextStash.unlockedWeaponKeys) {
                            nextStash = nextStash.withRecordedWeapons(setOf(wk))
                        } else if (updatedOp != null && updatedOp.backpackItems.size < bagCap) {
                            val spare = InventoryItem(
                                uid = "mkt_wpn_${UUID.randomUUID().toString().take(8)}",
                                type = LootType.WEAPON,
                                name = listing.itemName,
                                detail = listing.itemDetail,
                                value = listing.value.toInt(),
                                weaponKey = wk
                            )
                            updatedOp = updatedOp.copy(backpackItems = updatedOp.backpackItems + spare)
                        }
                    }
                    "PANTS" -> {
                        val lvl = listing.pantsLevel.toInt().coerceIn(1, 4)
                        if (lvl > nextStash.maxPantsLevel) {
                            nextStash = nextStash.copy(maxPantsLevel = lvl)
                        }
                        if (updatedOp != null && lvl > updatedOp.pantsLevel) {
                            updatedOp = updatedOp.copy(pantsLevel = lvl)
                        } else if (updatedOp != null && updatedOp.backpackItems.size < bagCap) {
                            val spare = InventoryItem(
                                uid = "mkt_pants_${UUID.randomUUID().toString().take(8)}",
                                type = LootType.PANTS,
                                name = listing.itemName,
                                detail = listing.itemDetail,
                                value = listing.value.toInt(),
                                pantsLevel = lvl
                            )
                            updatedOp = updatedOp.copy(backpackItems = updatedOp.backpackItems + spare)
                        }
                    }
                    "ARMOR" -> {
                        val lvl = listing.value.toInt().coerceIn(1, 4)
                        if (lvl > nextStash.maxArmorLevel) {
                            nextStash = nextStash.copy(maxArmorLevel = lvl)
                        }
                        if (updatedOp != null && lvl > updatedOp.armorLevel) {
                            updatedOp = updatedOp.copy(armorLevel = lvl)
                        } else if (updatedOp != null && updatedOp.backpackItems.size < bagCap) {
                            val spare = InventoryItem(
                                uid = "mkt_armor_${UUID.randomUUID().toString().take(8)}",
                                type = LootType.ARMOR,
                                name = listing.itemName,
                                detail = listing.itemDetail,
                                value = lvl * 120,
                                armorLevel = lvl
                            )
                            updatedOp = updatedOp.copy(backpackItems = updatedOp.backpackItems + spare)
                        }
                    }
                    "MATERIAL" -> {
                        CraftingMaterial.fromKey(listing.materialKey)?.let { mat ->
                            nextStash = nextStash.withMaterialDelta(mat, listing.quantity.toInt().coerceAtLeast(1))
                        }
                    }
                    "CD" -> {
                        if (listing.cdId > 0L) {
                            repo.recordCollectedCd(listing.cdId.toInt(), "Live Market")
                        }
                    }
                    "MEDKIT", "FOOD", "WATER" -> {
                        val lType = runCatching { LootType.valueOf(listing.category) }.getOrDefault(LootType.MEDKIT)
                        if (updatedOp != null && updatedOp.backpackItems.size < bagCap) {
                            val bought = InventoryItem(
                                uid = "mkt_sup_${UUID.randomUUID().toString().take(8)}",
                                type = lType,
                                name = listing.itemName,
                                detail = listing.itemDetail,
                                value = listing.value.toInt()
                            )
                            updatedOp = updatedOp.copy(backpackItems = updatedOp.backpackItems + bought)
                        } else {
                            nextStash = when (lType) {
                                LootType.MEDKIT -> nextStash.copy(medsStock = nextStash.medsStock + 1)
                                LootType.FOOD -> nextStash.copy(foodStock = nextStash.foodStock + 1)
                                LootType.WATER -> nextStash.copy(waterStock = nextStash.waterStock + 1)
                                else -> nextStash
                            }
                        }
                    }
                }

                if (updatedOp != null && updatedOp != simulation.activeOperator) {
                    repo.saveOperator(updatedOp)
                    simulation.activeOperator = updatedOp
                }
                repo.updateBaseStash(nextStash)
                simulation.syncBaseArmory(nextStash, updatedOp, pickBestWeapon = true)
                audio.playLootPickup()
                simulation.showToast("PURCHASED ${listing.itemName.uppercase()} FOR ${listing.priceCredits} CRED")
            }.onFailure { err ->
                simulation.showToast(err.message ?: "PURCHASE FAILED")
            }
            _marketBusy.value = false
        }
    }

    fun createMarketListing(candidate: SellableMarketCandidate, quantity: Int, priceCredits: Int) {
        if (!isSignedIn) {
            simulation.showToast("LIVE MARKET PAUSED WHILE SIGN-IN IS DISABLED")
            return
        }
        if (_marketBusy.value) return
        val op = simulation.activeOperator
        val callsign = op?.callsign ?: "SURVIVOR"

        _marketBusy.value = true
        viewModelScope.launch {
            val res = marketRepo.createListing(
                sellerCallsign = callsign,
                category = candidate.category,
                itemName = candidate.itemName,
                itemDetail = candidate.itemDetail,
                rarityLabel = candidate.rarityLabel,
                weaponKey = candidate.weaponKey,
                materialKey = candidate.materialKey,
                cdId = candidate.cdId,
                pantsLevel = candidate.pantsLevel,
                value = candidate.value,
                quantity = quantity,
                priceCredits = priceCredits
            )
            res.onSuccess {
                var nextStash = _stash.value
                var updatedOp = op
                when (candidate.source) {
                    MarketSellSource.STASH_MATERIAL -> {
                        CraftingMaterial.fromKey(candidate.materialKey)?.let { mat ->
                            nextStash = nextStash.withMaterialDelta(mat, -quantity, clampToStorageCap = false)
                        }
                    }
                    MarketSellSource.STASH_WEAPON -> {
                        val remainingKeys = (nextStash.unlockedWeaponKeys - candidate.weaponKey).ifEmpty { setOf(WeaponId.KNIFE.key) }
                        nextStash = nextStash.copy(unlockedWeaponKeys = remainingKeys)
                    }
                    MarketSellSource.STASH_PANTS -> {
                        nextStash = nextStash.copy(maxPantsLevel = 1)
                        if (updatedOp != null && updatedOp.pantsLevel > 1) {
                            updatedOp = updatedOp.copy(pantsLevel = 1)
                        }
                    }
                    MarketSellSource.BACKPACK_ITEM -> {
                        if (updatedOp != null && candidate.inventoryItemUid != null) {
                            updatedOp = updatedOp.copy(
                                backpackItems = updatedOp.backpackItems.filterNot { it.uid == candidate.inventoryItemUid }
                            )
                        }
                    }
                    MarketSellSource.SAFE_ITEM -> {
                        if (updatedOp != null && candidate.inventoryItemUid != null) {
                            updatedOp = updatedOp.copy(
                                safePocketItems = updatedOp.safePocketItems.filterNot { it.uid == candidate.inventoryItemUid }
                            )
                        }
                    }
                    MarketSellSource.COLLECTED_CD -> {
                        if (candidate.cdId > 0) {
                            repo.removeCollectedCd(candidate.cdId)
                        }
                    }
                }

                if (updatedOp != null && updatedOp != op) {
                    repo.saveOperator(updatedOp)
                    simulation.activeOperator = updatedOp
                }
                repo.updateBaseStash(nextStash)
                syncStashToCloud(nextStash, callsign)
                simulation.syncBaseArmory(nextStash, updatedOp, pickBestWeapon = true)
                simulation.showToast("LISTED ${candidate.itemName.uppercase()} FOR $priceCredits CRED")
            }.onFailure { err ->
                simulation.showToast(err.message ?: "COULD NOT POST LISTING")
            }
            _marketBusy.value = false
        }
    }

    fun cancelMarketListing(listing: MarketListing) {
        if (!isSignedIn) {
            simulation.showToast("LIVE MARKET PAUSED WHILE SIGN-IN IS DISABLED")
            return
        }
        if (_marketBusy.value) return
        _marketBusy.value = true
        viewModelScope.launch {
            marketRepo.cancelListing(listing.id).onSuccess {
                var nextStash = _stash.value
                var updatedOp = simulation.activeOperator
                val bagCap = nextStash.backpackSlotCap

                when (listing.category) {
                    "MATERIAL" -> {
                        CraftingMaterial.fromKey(listing.materialKey)?.let { mat ->
                            nextStash = nextStash.withMaterialDelta(mat, listing.quantity.toInt().coerceAtLeast(1))
                        }
                    }
                    "WEAPON" -> {
                        val wk = listing.weaponKey
                        if (wk.isNotBlank() && wk !in nextStash.unlockedWeaponKeys) {
                            nextStash = nextStash.withRecordedWeapons(setOf(wk))
                        } else if (updatedOp != null && updatedOp.backpackItems.size < bagCap) {
                            updatedOp = updatedOp.copy(
                                backpackItems = updatedOp.backpackItems + listing.toInventoryItemPreview()
                            )
                        }
                    }
                    "PANTS" -> {
                        val lvl = listing.pantsLevel.toInt().coerceIn(1, 4)
                        if (lvl > nextStash.maxPantsLevel) {
                            nextStash = nextStash.copy(maxPantsLevel = lvl)
                        }
                        if (updatedOp != null && lvl > updatedOp.pantsLevel) {
                            updatedOp = updatedOp.copy(pantsLevel = lvl)
                        } else if (updatedOp != null && updatedOp.backpackItems.size < bagCap) {
                            updatedOp = updatedOp.copy(
                                backpackItems = updatedOp.backpackItems + listing.toInventoryItemPreview()
                            )
                        }
                    }
                    "CD" -> {
                        if (listing.cdId > 0L) {
                            repo.recordCollectedCd(listing.cdId.toInt(), "Market Return")
                        }
                    }
                    "MEDKIT", "FOOD", "WATER", "ARMOR" -> {
                        if (updatedOp != null && updatedOp.backpackItems.size < bagCap) {
                            updatedOp = updatedOp.copy(
                                backpackItems = updatedOp.backpackItems + listing.toInventoryItemPreview()
                            )
                        }
                    }
                }

                if (updatedOp != null && updatedOp != simulation.activeOperator) {
                    repo.saveOperator(updatedOp)
                    simulation.activeOperator = updatedOp
                }
                repo.updateBaseStash(nextStash)
                syncStashToCloud(nextStash, updatedOp?.callsign ?: "SURVIVOR")
                simulation.syncBaseArmory(nextStash, updatedOp, pickBestWeapon = true)
                simulation.showToast("CANCELLED LISTING — ITEM RETURNED")
            }.onFailure { err ->
                simulation.showToast(err.message ?: "CANCEL FAILED")
            }
            _marketBusy.value = false
        }
    }

    fun claimMarketPayout(listing: MarketListing) {
        if (!isSignedIn) {
            simulation.showToast("LIVE MARKET PAUSED WHILE SIGN-IN IS DISABLED")
            return
        }
        if (_marketBusy.value) return
        _marketBusy.value = true
        viewModelScope.launch {
            marketRepo.claimSalePayout(listing.id).onSuccess { payout ->
                val cur = _stash.value
                repo.updateBaseStash(cur.copy(credits = cur.credits + payout))
                audio.playLootPickup()
                simulation.showToast("CLAIMED +$payout CRED FROM LIVE MARKET")
            }.onFailure { err ->
                simulation.showToast(err.message ?: "CLAIM FAILED")
            }
            _marketBusy.value = false
        }
    }

    // --- Inventory & Looting ---

    fun takeLootItem(item: InventoryItem, toSafePocket: Boolean) {
        val op = simulation.activeOperator ?: return
        val source = _activeLoot.value ?: return
        val bagCap = _stash.value.backpackSlotCap

        if (item.type == LootType.WEAPON && item.weaponKey != null) {
            val wId = WeaponId.fromKey(item.weaponKey)
            simulation.grantWeapon(wId)
            audio.playLootPickup()
            consumeFromLootSource(source, item)
            return
        }

        if (item.type == LootType.PANTS && item.pantsLevel != null) {
            if (item.pantsLevel > op.pantsLevel) {
                val updated = op.copy(pantsLevel = item.pantsLevel)
                persistActiveOp(updated)
                simulation.showToast("EQUIPPED LV.${item.pantsLevel} PANTS (${item.pantsLevel} SAFE POCKETS)")
                audio.playLootPickup()
                consumeFromLootSource(source, item)
                return
            }
        }

        if (item.type == LootType.ARMOR && item.armorLevel != null) {
            val tier = ArmorTier.fromLevel(item.armorLevel)
            if (item.armorLevel > op.armorLevel || (item.armorLevel == op.armorLevel && simulation.armorHp < tier.maxShieldHp * 0.8f)) {
                val updated = op.copy(armorLevel = item.armorLevel)
                simulation.armorTier = tier
                simulation.maxArmorHp = tier.maxShieldHp
                simulation.armorHp = tier.maxShieldHp
                persistActiveOp(updated)
                simulation.showToast("EQUIPPED ${tier.displayName.uppercase()}")
                audio.playLootPickup()
                consumeFromLootSource(source, item)
                return
            }
        }

        if (toSafePocket) {
            if (op.safePocketItems.size >= op.safePocketCapacity) {
                simulation.showToast("SAFE POCKETS FULL (MAX ${op.safePocketCapacity})")
                return
            }
            val updated = op.copy(safePocketItems = op.safePocketItems + item)
            persistActiveOp(updated)
        } else {
            if (op.backpackItems.size >= bagCap) {
                simulation.showToast("BACKPACK FULL ($bagCap SLOTS)")
                return
            }
            val updated = op.copy(backpackItems = op.backpackItems + item)
            persistActiveOp(updated)
        }

        audio.playLootPickup()
        consumeFromLootSource(source, item)
    }

    private fun consumeFromLootSource(source: LootSearchResult, item: InventoryItem) {
        simulation.removeLootItemFromWorld(source.sourceId, source.isVehicle, item.uid)
        val remaining = source.items.filterNot { it.uid == item.uid }
        _activeLoot.value = source.copy(items = remaining)
    }

    fun moveItemToSafePocket(item: InventoryItem) {
        val op = simulation.activeOperator ?: return
        if (op.safePocketItems.size >= op.safePocketCapacity) {
            simulation.showToast("SAFE POCKETS FULL")
            return
        }
        val updated = op.copy(
            backpackItems = op.backpackItems.filterNot { it.uid == item.uid },
            safePocketItems = op.safePocketItems + item
        )
        persistActiveOp(updated)
    }

    fun moveItemToBackpack(item: InventoryItem) {
        val op = simulation.activeOperator ?: return
        val bagCap = _stash.value.backpackSlotCap
        if (op.backpackItems.size >= bagCap) {
            simulation.showToast("BACKPACK FULL ($bagCap SLOTS)")
            return
        }
        val updated = op.copy(
            safePocketItems = op.safePocketItems.filterNot { it.uid == item.uid },
            backpackItems = op.backpackItems + item
        )
        persistActiveOp(updated)
    }

    fun useInventoryItem(item: InventoryItem, fromSafePocket: Boolean) {
        val op = simulation.activeOperator ?: return
        when (item.type) {
            LootType.FOOD -> simulation.hunger = (simulation.hunger + item.value).coerceAtMost(100f)
            LootType.WATER -> simulation.thirst = (simulation.thirst + item.value).coerceAtMost(100f)
            LootType.MEDKIT -> simulation.hp = (simulation.hp + item.value).coerceAtMost(simulation.maxHp)
            LootType.PANTS -> {
                val lvl = item.pantsLevel ?: return
                if (lvl <= op.pantsLevel) return
                val updated = op.copy(
                    pantsLevel = lvl,
                    backpackItems = if (!fromSafePocket) op.backpackItems.filterNot { it.uid == item.uid } else op.backpackItems,
                    safePocketItems = if (fromSafePocket) op.safePocketItems.filterNot { it.uid == item.uid } else op.safePocketItems
                )
                persistActiveOp(updated)
                audio.playLootPickup()
                return
            }
            LootType.ARMOR -> {
                val lvl = (item.armorLevel ?: 1).coerceIn(1, 4)
                val tier = ArmorTier.fromLevel(lvl)
                if (lvl < op.armorLevel && simulation.armorHp >= tier.maxShieldHp) return
                val updated = op.copy(
                    armorLevel = lvl,
                    backpackItems = if (!fromSafePocket) op.backpackItems.filterNot { it.uid == item.uid } else op.backpackItems,
                    safePocketItems = if (fromSafePocket) op.safePocketItems.filterNot { it.uid == item.uid } else op.safePocketItems
                )
                simulation.armorTier = tier
                simulation.maxArmorHp = tier.maxShieldHp
                simulation.armorHp = tier.maxShieldHp
                persistActiveOp(updated)
                audio.playLootPickup()
                simulation.showToast("EQUIPPED ${tier.displayName.uppercase()}")
                return
            }
            else -> return
        }
        val updated = op.copy(
            backpackItems = if (!fromSafePocket) op.backpackItems.filterNot { it.uid == item.uid } else op.backpackItems,
            safePocketItems = if (fromSafePocket) op.safePocketItems.filterNot { it.uid == item.uid } else op.safePocketItems
        )
        persistActiveOp(updated)
        audio.playLootPickup()
    }

    fun dropInventoryItem(item: InventoryItem, fromSafePocket: Boolean) {
        val op = simulation.activeOperator ?: return
        val updated = op.copy(
            backpackItems = if (!fromSafePocket) op.backpackItems.filterNot { it.uid == item.uid } else op.backpackItems,
            safePocketItems = if (fromSafePocket) op.safePocketItems.filterNot { it.uid == item.uid } else op.safePocketItems
        )
        persistActiveOp(updated)
        simulation.showToast("DROPPED ${item.name.uppercase()}")
    }

    fun equipWeaponFromInspect(weapon: WeaponId) {
        if (weapon in simulation.unlockedWeapons) {
            simulation.currentWeapon = weapon
            simulation.reloading = false
            simulation.showToast("EQUIPPED ${weapon.shortName}")
        }
    }

    fun equipWeaponAttachment(weapon: WeaponId, attachmentKey: String) {
        val att = WeaponAttachmentCatalog.fromKey(attachmentKey) ?: return
        val cur = _stash.value
        if (att.key !in cur.ownedAttachmentKeys) {
            simulation.showToast("FIND OR CRAFT ${att.shortName} FIRST")
            return
        }
        if (!att.isCompatibleWith(weapon)) {
            simulation.showToast("${att.shortName} IS NOT ERA/PLATFORM COMPATIBLE WITH ${weapon.shortName}")
            return
        }
        val next = cur.withEquippedAttachment(weapon, att)
        viewModelScope.launch {
            repo.updateBaseStash(next)
            syncStashToCloud(next, simulation.activeOperator?.callsign ?: "SURVIVOR")
            simulation.syncBaseArmory(next, simulation.activeOperator, pickBestWeapon = false)
            audio.playLootPickup()
            simulation.showToast("FITTED ${att.shortName} ON ${weapon.shortName}")
        }
    }

    fun unequipWeaponSlot(weapon: WeaponId, slot: AttachmentSlot) {
        val next = _stash.value.withUnequippedSlot(weapon, slot)
        if (next == _stash.value) return
        viewModelScope.launch {
            repo.updateBaseStash(next)
            syncStashToCloud(next, simulation.activeOperator?.callsign ?: "SURVIVOR")
            simulation.syncBaseArmory(next, simulation.activeOperator, pickBestWeapon = false)
            simulation.showToast("REMOVED ${slot.shortLabel} FROM ${weapon.shortName}")
        }
    }

    fun claimHeritageEraMilestone(era: WeaponEra) {
        val cur = _stash.value
        val eraGuns = HeritageVaultCatalog.collectiblesFor(era)
        if (eraGuns.isEmpty() || era.name in cur.claimedEraMilestones) return
        if (!eraGuns.all { it.key in cur.collectedHistoricKeys }) {
            simulation.showToast("COLLECT ALL ${eraGuns.size} ${era.label.uppercase()} GUNS FIRST")
            return
        }
        val reward = HeritageVaultCatalog.milestoneFor(era) ?: return
        var next = cur.copy(
            credits = cur.credits + reward.rewardCred,
            claimedEraMilestones = cur.claimedEraMilestones + era.name
        )
        reward.rewardMats.forEach { (mat, qty) ->
            next = next.withMaterialDelta(mat, qty, clampToStorageCap = false)
        }
        viewModelScope.launch {
            repo.updateBaseStash(next)
            syncStashToCloud(next, simulation.activeOperator?.callsign ?: "SURVIVOR")
            audio.playLootPickup()
            simulation.showToast("HERITAGE VAULT // +${reward.rewardCred} CRED (${reward.plaqueTitle.uppercase()})")
        }
    }

    private fun handleExtraction(exitName: String) {
        val op = simulation.activeOperator ?: return
        var creditsEarned = simulation.kills * 15
        var foodGain = 0
        var waterGain = 0
        var medsGain = 0
        var materialsGain = 0
        val materialDeltas = mutableMapOf<CraftingMaterial, Int>()
        val newCdIds = mutableListOf<Int>()
        val keptSafe = mutableListOf<InventoryItem>()
        val keptBag = mutableListOf<InventoryItem>()
        var bestArmorLvl = op.armorLevel
        var bestPantsLvl = op.pantsLevel
        val extractedWeapons = mutableSetOf<String>()
        val extractedAttachments = mutableSetOf<String>()

        for (item in op.safePocketItems) {
            when (item.type) {
                LootType.CD -> item.cdId?.let { newCdIds.add(it) }
                LootType.SALVAGE -> creditsEarned += item.value
                LootType.WEAPON -> item.weaponKey?.let { extractedWeapons.add(it) }
                LootType.ATTACHMENT -> item.attachmentKey?.let { extractedAttachments.add(it) }
                LootType.MATERIAL -> {
                    val mat = CraftingMaterial.fromKey(item.materialKey)
                    val qty = item.quantity.coerceAtLeast(1)
                    if (mat != null) {
                        materialDeltas[mat] = (materialDeltas[mat] ?: 0) + qty
                        materialsGain += qty
                    }
                }
                else -> keptSafe.add(item)
            }
        }
        for (item in op.backpackItems) {
            when (item.type) {
                LootType.CD -> item.cdId?.let { newCdIds.add(it) }
                LootType.SALVAGE -> creditsEarned += item.value
                LootType.FOOD -> foodGain++
                LootType.WATER -> waterGain++
                LootType.MEDKIT -> medsGain++
                LootType.WEAPON -> item.weaponKey?.let { extractedWeapons.add(it) }
                LootType.ATTACHMENT -> item.attachmentKey?.let { extractedAttachments.add(it) }
                LootType.PANTS -> bestPantsLvl = maxOf(bestPantsLvl, item.pantsLevel ?: 1)
                LootType.ARMOR -> bestArmorLvl = maxOf(bestArmorLvl, item.armorLevel ?: 0)
                LootType.MATERIAL -> {
                    val mat = CraftingMaterial.fromKey(item.materialKey)
                    val qty = item.quantity.coerceAtLeast(1)
                    if (mat != null) {
                        materialDeltas[mat] = (materialDeltas[mat] ?: 0) + qty
                        materialsGain += qty
                    }
                }
            }
        }

        viewModelScope.launch {
            newCdIds.forEach { cdId -> repo.recordCollectedCd(cdId, exitName) }
            val runWeapons = simulation.unlockedWeapons.map { it.key }.toSet() + extractedWeapons
            var nextStash = _stash.value.copy(
                credits = _stash.value.credits + creditsEarned,
                foodStock = _stash.value.foodStock + foodGain,
                waterStock = _stash.value.waterStock + waterGain,
                medsStock = _stash.value.medsStock + medsGain,
                maxPantsLevel = maxOf(_stash.value.maxPantsLevel, bestPantsLvl),
                maxArmorLevel = maxOf(_stash.value.maxArmorLevel, bestArmorLvl)
            ).withRecordedWeapons(runWeapons).withUnlockedAttachments(extractedAttachments)
            for (w in WeaponId.entries) {
                if (!w.isMelee) {
                    val remain = (simulation.magAmmo[w] ?: 0) + (simulation.reserveAmmo[w] ?: 0)
                    nextStash = nextStash.withWeaponAmmoSet(w, remain)
                }
            }
            materialDeltas.forEach { (mat, qty) ->
                nextStash = nextStash.withMaterialDelta(mat, qty)
            }
            nextStash = applyTaskProgressToStash(nextStash, TaskGoalKind.EXTRACT_RUNS, 1)

            repo.updateBaseStash(nextStash)
            syncStashToCloud(nextStash, op.callsign)
            repo.saveOperator(
                op.copy(
                    pantsLevel = bestPantsLvl,
                    armorLevel = bestArmorLvl,
                    safePocketItems = keptSafe,
                    backpackItems = keptBag
                )
            )
        }

        _extractionSummary.value = ExtractionSummary(
            exitName = exitName,
            kills = simulation.kills,
            salvagedCredits = creditsEarned,
            extractedFood = foodGain,
            extractedWater = waterGain,
            extractedMeds = medsGain,
            extractedMaterials = materialsGain,
            newCdsCount = newCdIds.size
        )
        _activeModal.value = ActiveModal.EXTRACTION_SUMMARY
    }

    private fun onOperatorDownedInTown() {
        val op = simulation.activeOperator ?: return
        val updated = op.copy(
            isKia = true,
            kiaCount = op.kiaCount + 1,
            kiaTimestamp = System.currentTimeMillis(),
            downedAtX = simulation.playerX,
            downedAtY = simulation.playerY,
            downedZoneId = _selectedZone.value.id,
            downedElevation = simulation.elevation,
            stolenItemNames = emptyList(),
            scavengeChecksDone = 0,
            isPermadead = false
        )
        simulation.activeOperator = updated
        viewModelScope.launch {
            repo.saveOperator(updated)
        }
        _activeModal.value = ActiveModal.OPERATOR_DOWN
    }

    fun buyBackKiaOperator(operatorId: String) {
        val op = _roster.value.firstOrNull { it.id == operatorId && it.isKia && !it.isPermadead } ?: return
        val fee = op.currentBuyBackFee()
        val cur = _stash.value
        if (cur.credits < fee) {
            simulation.showToast("NEED $fee CRED TO SEND RESCUE TEAM FOR ${op.callsign}")
            return
        }
        val nextStash = cur.copy(credits = cur.credits - fee, activeOperatorId = op.id)
        val zone = DeploymentZone.fromId(op.downedZoneId)
        _rescueMission.value = RescueMissionAnimState(
            operator = op,
            feePaid = fee,
            zoneTitle = zone.title
        )
        viewModelScope.launch {
            repo.updateBaseStash(nextStash)
            syncStashToCloud(nextStash, op.callsign)
            _activeModal.value = ActiveModal.RESCUE_ANIMATION
        }
    }

    fun completeRescueMission() {
        val mission = _rescueMission.value ?: return
        val rescued = mission.operator.copy(
            isKia = false,
            kiaTimestamp = 0L,
            scavengeChecksDone = 0,
            stolenItemNames = emptyList(),
            isPermadead = false
        )
        _rescueMission.value = null
        viewModelScope.launch {
            repo.saveOperator(rescued)
            simulation.activeOperator = rescued
            simulation.maxHp = rescued.maxHp.toFloat()
            simulation.hp = (rescued.maxHp * 0.65f)
            simulation.hunger = 55f
            simulation.thirst = 48f
            returnToOutpostBase()
            simulation.showToast("CSAR EXTRACTED ${rescued.callsign} — HEAD TO CANTINE & INFIRMARY")
        }
    }

    fun dismissPermadeathReport() {
        _permadeathReport.value = null
        val hasLiving = _roster.value.any { !it.isKia && !it.isPermadead }
        _activeModal.value = if (!hasLiving && _roster.value.isEmpty()) {
            ActiveModal.CHARACTER_CREATOR
        } else {
            ActiveModal.NONE
        }
    }

    fun evaluateDownedOperators(
        nowMs: Long = System.currentTimeMillis(),
        overrideOps: List<OperatorProfile>? = null
    ) {
        if (evaluatingDowned) return
        val sourceOps = overrideOps ?: _roster.value
        val downedList = sourceOps.filter { it.isKia && !it.isPermadead && it.kiaTimestamp > 0L }
        if (downedList.isEmpty()) return

        evaluatingDowned = true
        viewModelScope.launch {
            try {
                for (op in downedList) {
                    val elapsed = (nowMs - op.kiaTimestamp).coerceAtLeast(0L)
                    if (elapsed >= OperatorProfile.PERMADEATH_WINDOW_MS) {
                        var nextStash = _stash.value
                        var credGain = 0
                        var cdsGain = 0
                        var livingOp = sourceOps.firstOrNull { it.id != op.id && !it.isKia && !it.isPermadead }
                        val bagCap = nextStash.backpackSlotCap
                        val recoveredBag = op.backpackItems
                        val recoveredSafe = op.safePocketItems
                        val remainingTransfer = mutableListOf<InventoryItem>()

                        suspend fun processRecoveredItem(item: InventoryItem) {
                            when (item.type) {
                                LootType.CD -> {
                                    item.cdId?.let {
                                        repo.recordCollectedCd(it, "24h Body Recovery")
                                        cdsGain++
                                    }
                                }
                                LootType.SALVAGE -> {
                                    credGain += item.value
                                }
                                LootType.MATERIAL -> {
                                    CraftingMaterial.fromKey(item.materialKey)?.let { mat ->
                                        nextStash = nextStash.withMaterialDelta(mat, item.quantity.coerceAtLeast(1), clampToStorageCap = false)
                                    }
                                }
                                LootType.WEAPON -> {
                                    item.weaponKey?.let { wk ->
                                        nextStash = nextStash.withRecordedWeapons(setOf(wk))
                                    }
                                }
                                LootType.ATTACHMENT -> {
                                    item.attachmentKey?.let { ak ->
                                        nextStash = nextStash.withUnlockedAttachments(setOf(ak))
                                    }
                                }
                                LootType.PANTS -> {
                                    val lvl = item.pantsLevel ?: 1
                                    if (lvl > nextStash.maxPantsLevel) nextStash = nextStash.copy(maxPantsLevel = lvl)
                                }
                                LootType.ARMOR -> {
                                    val lvl = item.armorLevel ?: 0
                                    if (lvl > nextStash.maxArmorLevel) nextStash = nextStash.copy(maxArmorLevel = lvl)
                                }
                                LootType.FOOD, LootType.WATER, LootType.MEDKIT -> {
                                    val curLiving = livingOp
                                    if (curLiving != null && curLiving.backpackItems.size + remainingTransfer.size < bagCap) {
                                        remainingTransfer.add(item)
                                    } else {
                                        nextStash = when (item.type) {
                                            LootType.FOOD -> nextStash.copy(foodStock = nextStash.foodStock + 1)
                                            LootType.WATER -> nextStash.copy(waterStock = nextStash.waterStock + 1)
                                            LootType.MEDKIT -> nextStash.copy(medsStock = nextStash.medsStock + 1)
                                            else -> nextStash
                                        }
                                    }
                                }
                            }
                        }

                        for (it in recoveredSafe) processRecoveredItem(it)
                        for (it in recoveredBag) processRecoveredItem(it)

                        if (credGain > 0) {
                            nextStash = nextStash.copy(credits = nextStash.credits + credGain)
                        }
                        if (livingOp != null && remainingTransfer.isNotEmpty()) {
                            val updatedLiving = livingOp.copy(backpackItems = livingOp.backpackItems + remainingTransfer)
                            repo.saveOperator(updatedLiving)
                            livingOp = updatedLiving
                        }
                        if (nextStash.activeOperatorId == op.id) {
                            nextStash = nextStash.copy(activeOperatorId = livingOp?.id ?: "")
                        }
                        repo.updateBaseStash(nextStash)
                        syncStashToCloud(nextStash, livingOp?.callsign ?: "SURVIVOR")
                        repo.deleteOperator(op.id)

                        if (_rescueMission.value?.operator?.id == op.id) {
                            _rescueMission.value = null
                        }
                        if (simulation.activeOperator?.id == op.id) {
                            simulation.activeOperator = livingOp
                            if (simulation.world?.mapId == GameMapId.OAKRIDGE_TOWN) {
                                returnToOutpostBase()
                            }
                        }

                        _permadeathReport.value = PermadeathReport(
                            callsign = op.callsign,
                            roleLabel = "${op.archetype.label} (${op.dogBreed})",
                            stolenItemNames = op.stolenItemNames,
                            recoveredBagItems = recoveredBag,
                            recoveredSafeItems = recoveredSafe,
                            recoveredCred = credGain,
                            recoveredCds = cdsGain
                        )
                        _activeModal.value = ActiveModal.PERMADEATH_REPORT
                    } else {
                        val targetBuckets = (elapsed / OperatorProfile.SCAVENGE_INTERVAL_MS).toInt().coerceIn(0, 5)
                        if (targetBuckets > op.scavengeChecksDone) {
                            val bag = op.backpackItems.toMutableList()
                            val stolen = op.stolenItemNames.toMutableList()
                            var newlyStolen: String? = null
                            for (b in op.scavengeChecksDone until targetBuckets) {
                                val chance = 0.65f
                                if (bag.isNotEmpty() && kotlin.random.Random.nextFloat() < chance) {
                                    val idx = kotlin.random.Random.nextInt(bag.size)
                                    val taken = bag.removeAt(idx)
                                    stolen.add(taken.name)
                                    newlyStolen = taken.name
                                }
                            }
                            val updatedDowned = op.copy(
                                backpackItems = bag,
                                stolenItemNames = stolen,
                                scavengeChecksDone = targetBuckets
                            )
                            repo.saveOperator(updatedDowned)
                            if (simulation.activeOperator?.id == updatedDowned.id) {
                                simulation.activeOperator = updatedDowned
                            }
                            if (newlyStolen != null) {
                                simulation.showToast("SCAVENGERS STOLE ${newlyStolen.uppercase()} FROM ${op.callsign}'S BAG!")
                            }
                        }
                    }
                }
            } finally {
                evaluatingDowned = false
            }
        }
    }

    fun playCdTrack(cdId: Int) {
        if (_playingCdId.value == cdId) {
            audio.stopCdPreview()
            _playingCdId.value = null
        } else {
            _playingCdId.value = cdId
            audio.playCdPreview(CdCatalog.getById(cdId))
        }
    }

    private fun persistActiveOp(updated: OperatorProfile) {
        simulation.activeOperator = updated
        viewModelScope.launch { repo.saveOperator(updated) }
    }

    private fun addAmmoToStash(stash: BaseStashState, weaponKey: String?, amount: Int): BaseStashState {
        if (weaponKey.isNullOrBlank() || amount <= 0) return stash
        val w = WeaponId.entries.firstOrNull { it.key == weaponKey } ?: return stash
        return stash.withWeaponAmmoDelta(w, amount)
    }

    private suspend fun syncStashToCloud(stash: BaseStashState, callsign: String) {
        if (!isSignedIn) return
        marketRepo.saveUserStash(currentUserId, callsign, stash)
    }

    fun recordPlayerKillEvent(
        killerCallsign: String,
        victimCallsign: String,
        weapon: WeaponId,
        zoneName: String
    ) {
        val safeKiller = killerCallsign.trim().ifBlank { "OPERATOR" }.take(24)
        val safeVictim = victimCallsign.trim().ifBlank { "RIVAL" }.take(24)
        val now = System.currentTimeMillis()
        val entry = KillFeedEntry(
            id = "local_${now}_${UUID.randomUUID().toString().take(6)}",
            killerCallsign = safeKiller,
            victimCallsign = safeVictim,
            weaponName = weapon.displayName,
            weaponKey = weapon.key,
            zoneName = zoneName,
            timestampMs = now
        )
        _localKillFeed.value = (listOf(entry) + _localKillFeed.value).take(16)
        simulation.showToast("$safeKiller [${weapon.shortName}] ➔ $safeVictim")

        if (isSignedIn) {
            viewModelScope.launch {
                marketRepo.publishKillFeedEvent(
                    killerCallsign = safeKiller,
                    victimId = "rival_${safeVictim.lowercase()}",
                    victimCallsign = safeVictim,
                    weaponKey = weapon.key,
                    weaponName = weapon.displayName,
                    zoneName = zoneName
                )
            }
        }
    }

    // --- Outpost Zero Base Builder (Move / Place / Rotate / Delete Whole Base) ---

    fun toggleBaseBuildMode(enable: Boolean? = null) {
        val inBase = simulation.world?.mapId == GameMapId.OUTPOST_ZERO
        val next = if (!inBase) false else (enable ?: !_buildModeActive.value)
        _buildModeActive.value = next
        simulation.buildModeActive = next
        if (!next) {
            _selectedBuildPieceId.value = null
            simulation.selectedBuildPieceId = null
            _pendingBuildKind.value = null
            if (_activeModal.value == ActiveModal.BASE_BUILDER) {
                _activeModal.value = ActiveModal.NONE
            }
        } else {
            val ensured = _stash.value.baseLayout.ensureInfrastructure(_stash.value.bunkLevel, _stash.value.helipadLevel)
            simulation.buildLayoutPieces = ensured.pieces
            simulation.showToast("BASE BUILDER: TAP ANY OBJECT TO MOVE, OR OPEN CATALOG")
        }
    }

    fun openBaseBuilderCatalog() {
        if (simulation.world?.mapId != GameMapId.OUTPOST_ZERO) {
            simulation.showToast("RETURN TO OUTPOST ZERO TO CUSTOMIZE BASE")
            return
        }
        if (!_buildModeActive.value) {
            toggleBaseBuildMode(true)
        }
        _activeModal.value = ActiveModal.BASE_BUILDER
    }

    fun selectExistingPieceForMove(pieceId: String) {
        if (simulation.world?.mapId != GameMapId.OUTPOST_ZERO) return
        if (!_buildModeActive.value) {
            _buildModeActive.value = true
            simulation.buildModeActive = true
        }
        val ensured = _stash.value.baseLayout.ensureInfrastructure(_stash.value.bunkLevel, _stash.value.helipadLevel)
        val target = ensured.pieces.firstOrNull { it.id == pieceId } ?: return
        _pendingBuildKind.value = null
        _selectedBuildPieceId.value = target.id
        simulation.selectedBuildPieceId = target.id
        _activeModal.value = ActiveModal.NONE
        val label = if (target.kind == BaseModuleKind.BUNK) "BUNK #${target.slotNumber}" else target.kind.label.uppercase()
        simulation.showToast("SELECTED $label — TAP MAP TO PLACE")
    }

    fun selectKindToBuildOrMove(kind: BaseModuleKind) {
        if (simulation.world?.mapId != GameMapId.OUTPOST_ZERO) return
        if (kind == BaseModuleKind.HELIPAD && _stash.value.helipadLevel <= 0) {
            simulation.showToast("BUY HELIPAD UPGRADE AT HQ TERMINAL FIRST")
            return
        }
        if (!_buildModeActive.value) {
            _buildModeActive.value = true
            simulation.buildModeActive = true
        }
        val ensured = _stash.value.baseLayout.ensureInfrastructure(_stash.value.bunkLevel, _stash.value.helipadLevel)
        if (kind.uniqueCore) {
            val existing = ensured.firstByKind(kind)
            if (existing != null) {
                selectExistingPieceForMove(existing.id)
                return
            }
        }
        _selectedBuildPieceId.value = null
        simulation.selectedBuildPieceId = null
        _pendingBuildKind.value = kind
        _pendingBuildRot.value = 0
        _activeModal.value = ActiveModal.NONE
        simulation.showToast("TAP COMPOUND MAP TO PLACE ${kind.label.uppercase()}")
    }

    fun rotateSelectedOrPendingPiece() {
        if (_pendingBuildKind.value != null) {
            _pendingBuildRot.value = (_pendingBuildRot.value + 90) % 360
            simulation.showToast("ROTATION: ${_pendingBuildRot.value}°")
            return
        }
        val selId = _selectedBuildPieceId.value ?: run {
            simulation.showToast("TAP A PLACED OBJECT FIRST TO ROTATE")
            return
        }
        val cur = _stash.value
        val ensured = cur.baseLayout.ensureInfrastructure(cur.bunkLevel, cur.helipadLevel)
        val nextPieces = ensured.pieces.map { piece ->
            if (piece.id == selId) piece.copy(rotationDeg = (piece.rotationDeg + 90) % 360) else piece
        }
        persistBaseLayout(cur.copy(baseLayout = BaseCustomLayout(nextPieces)), "ROTATED 90°")
    }

    fun deleteSelectedPiece() {
        val selId = _selectedBuildPieceId.value ?: run {
            simulation.showToast("TAP A PLACED OBJECT FIRST TO REMOVE")
            return
        }
        val cur = _stash.value
        val ensured = cur.baseLayout.ensureInfrastructure(cur.bunkLevel, cur.helipadLevel)
        val target = ensured.pieces.firstOrNull { it.id == selId } ?: return
        if (target.kind == BaseModuleKind.LAPTOP ||
            target.kind == BaseModuleKind.HELIPAD ||
            target.kind == BaseModuleKind.BUNK
        ) {
            simulation.showToast("${target.kind.label.uppercase()} IS ESSENTIAL — TAP MAP TO MOVE IT")
            return
        }
        val updatedLayout = BaseCustomLayout(ensured.pieces.filterNot { it.id == selId })
        _selectedBuildPieceId.value = null
        simulation.selectedBuildPieceId = null
        persistBaseLayout(cur.copy(baseLayout = updatedLayout), "REMOVED ${target.kind.label.uppercase()}")
    }

    fun clearBaseToScratch() {
        val cur = _stash.value
        val scratch = BaseCustomLayout.emptyScratch(cur.bunkLevel, cur.helipadLevel)
        _selectedBuildPieceId.value = null
        simulation.selectedBuildPieceId = null
        _pendingBuildKind.value = null
        _activeModal.value = ActiveModal.NONE
        persistBaseLayout(cur.copy(baseLayout = scratch), "CLEARED BASE TO OPEN COMPOUND — BUILD YOUR OWN!")
    }

    fun resetBaseToDefault() {
        val cur = _stash.value
        val def = BaseCustomLayout.default(cur.bunkLevel, cur.helipadLevel)
        _selectedBuildPieceId.value = null
        simulation.selectedBuildPieceId = null
        _pendingBuildKind.value = null
        _activeModal.value = ActiveModal.NONE
        persistBaseLayout(cur.copy(baseLayout = def), "RESTORED DEFAULT OUTPOST ZERO LAYOUT")
    }

    fun onBuildCanvasTap(screenX: Float, screenY: Float, canvasWidth: Float, canvasHeight: Float) {
        if (!_buildModeActive.value || _activeModal.value != ActiveModal.NONE) return
        if (simulation.world?.mapId != GameMapId.OUTPOST_ZERO) return

        val zoom = simulation.cameraZoom.coerceIn(
            com.example.game.model.CameraViewBounds.MIN_CAMERA_ZOOM,
            com.example.game.model.CameraViewBounds.MAX_CAMERA_ZOOM
        )
        val rawX = simulation.playerX + (screenX - canvasWidth / 2f) / zoom
        val rawY = simulation.playerY + (screenY - canvasHeight / 2f) / zoom
        val snapX = ((rawX / 20f).roundToInt() * 20f).coerceIn(-920f, 920f)
        val snapY = ((rawY / 20f).roundToInt() * 20f).coerceIn(-700f, 700f)

        val cur = _stash.value
        val ensured = cur.baseLayout.ensureInfrastructure(cur.bunkLevel, cur.helipadLevel)
        val pending = _pendingBuildKind.value

        if (pending != null) {
            val credCost = pending.credCost
            val scrapCost = pending.scrapCost
            if (cur.credits < credCost || cur.scrapMetal < scrapCost) {
                simulation.showToast("NEED $credCost CRED & $scrapCost SCRAP FOR ${pending.label.uppercase()}")
                return
            }
            val newPiece = PlacedBasePiece(
                id = "custom_${pending.name.lowercase()}_${UUID.randomUUID().toString().take(6)}",
                kind = pending,
                x = snapX,
                y = snapY,
                rotationDeg = _pendingBuildRot.value
            )
            var nextStash = cur.copy(
                credits = cur.credits - credCost,
                helipadLevel = if (pending == BaseModuleKind.HELIPAD) maxOf(cur.helipadLevel, 1) else cur.helipadLevel,
                baseLayout = BaseCustomLayout(ensured.pieces + newPiece)
            )
            if (scrapCost > 0) {
                nextStash = nextStash.withMaterialDelta(CraftingMaterial.SCRAP_METAL, -scrapCost, clampToStorageCap = false)
            }
            if (pending.uniqueCore) {
                _pendingBuildKind.value = null
                _selectedBuildPieceId.value = newPiece.id
                simulation.selectedBuildPieceId = newPiece.id
            }
            persistBaseLayout(nextStash, "PLACED ${pending.label.uppercase()}")
            return
        }

        val selId = _selectedBuildPieceId.value
        if (selId != null) {
            val currentSel = ensured.pieces.firstOrNull { it.id == selId }
            if (currentSel != null && hypot(rawX - currentSel.x, rawY - currentSel.y) < 26f) {
                _selectedBuildPieceId.value = null
                simulation.selectedBuildPieceId = null
                simulation.showToast("RELEASED SELECTION")
                return
            }
            val movedPieces = ensured.pieces.map { p ->
                if (p.id == selId) p.copy(x = snapX, y = snapY) else p
            }
            persistBaseLayout(cur.copy(baseLayout = BaseCustomLayout(movedPieces)), "MOVED OBJECT TO (${snapX.toInt()}, ${snapY.toInt()})")
            return
        }

        // Hit-test existing pieces, prioritizing smaller stations/bunks/props over large building shells
        val hit = ensured.pieces
            .sortedBy { it.kind.width * it.kind.height }
            .firstOrNull { p ->
                val rot = ((p.rotationDeg % 360) + 360) % 360
                val swap = rot == 90 || rot == 270
                val halfW = (if (swap) p.kind.height else p.kind.width) / 2f + 18f
                val halfH = (if (swap) p.kind.width else p.kind.height) / 2f + 18f
                rawX in (p.x - halfW)..(p.x + halfW) && rawY in (p.y - halfH)..(p.y + halfH)
            }

        if (hit != null) {
            _selectedBuildPieceId.value = hit.id
            simulation.selectedBuildPieceId = hit.id
            val label = if (hit.kind == BaseModuleKind.BUNK) "BUNK #${hit.slotNumber}" else hit.kind.label.uppercase()
            simulation.showToast("GRABBED $label — TAP MAP TO PLACE")
        } else {
            simulation.showToast("TAP ANY BUILDING, BUNK, LAPTOP OR STATION TO MOVE IT")
        }
    }

    private fun persistBaseLayout(nextStash: BaseStashState, toast: String?) {
        val ensured = nextStash.baseLayout.ensureInfrastructure(nextStash.bunkLevel, nextStash.helipadLevel)
        val finalized = nextStash.copy(baseLayout = ensured)
        simulation.buildLayoutPieces = ensured.pieces
        viewModelScope.launch {
            repo.updateBaseStash(finalized)
            syncStashToCloud(finalized, simulation.activeOperator?.callsign ?: "SURVIVOR")
            audio.playLootPickup()
            if (!toast.isNullOrBlank()) {
                simulation.showToast(toast)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audio.stopCdPreview()
    }
}
