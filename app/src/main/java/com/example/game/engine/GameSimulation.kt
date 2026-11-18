package com.example.game.engine

import com.example.game.audio.TacticalAudioEngine
import com.example.game.model.AmbientZoneKind
import com.example.game.model.ArmorTier
import com.example.game.model.BaseNpcRuntime
import com.example.game.model.BaseStashState
import com.example.game.model.BulletEntity
import com.example.game.model.CraftingMaterial
import com.example.game.model.DamagePopup
import com.example.game.model.DeadBodyMarker
import com.example.game.model.DoorStyle
import com.example.game.model.EffectiveWeaponStats
import com.example.game.model.GameMapId
import com.example.game.model.InventoryItem
import com.example.game.model.LootContainer
import com.example.game.model.LootType
import com.example.game.model.NpcNeedState
import com.example.game.model.OperatorProfile
import com.example.game.model.ParticleFx
import com.example.game.model.PlacedBasePiece
import com.example.game.model.PropObj
import com.example.game.model.RivalOperatorEntity
import com.example.game.model.ScavAiState
import com.example.game.model.ScavKinshipMemoryStore
import com.example.game.model.ScavKinshipRoster
import com.example.game.model.SlainScavRecord
import com.example.game.model.StationKind
import com.example.game.model.SurvivorCatalog
import com.example.game.model.TerrainKind
import com.example.game.model.WeaponAttachmentCatalog
import com.example.game.model.WeaponId
import com.example.game.model.WorldMapData
import com.example.game.model.ZombieEntity
import com.example.game.model.ZombieVariant
import com.example.game.world.DeLaineEnemyAndCampBuilder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class GameSimulation(
    private val audio: TacticalAudioEngine
) {
    var world: WorldMapData? = null
        private set

    var activeOperator: OperatorProfile? = null
    var rosterOperators: List<OperatorProfile> = emptyList()

    // Player live physics & combat state
    var playerX = 0f
    var playerY = 0f
    val playerRadius = 12f
    var playerAngle = -PI.toFloat() / 2f
    var moveRatio = 0f
    var stepTimer = 0f
    var hp = 100f
    var maxHp = 100f
    var armorTier = ArmorTier.NONE
    var armorHp = 0f
    var maxArmorHp = 0f
    var hunger = 92f
    var thirst = 92f
    var kills = 0

    var elevation = 0
    var prevElevation = 0
    var floorFadeAlpha = 1f
    var terrainHeight = 0
    var raidStartMs = System.currentTimeMillis()

    var currentWeapon = WeaponId.KNIFE
    val unlockedWeapons = mutableListOf(WeaponId.KNIFE)
    var weaponAttachments: Map<String, Set<String>> = emptyMap()
    val magAmmo = WeaponId.entries.associateWith { 0 }.toMutableMap()
    val reserveAmmo = WeaponId.entries.associateWith { 0 }.toMutableMap()
    var reloading = false
    var reloadStartMs = 0L
    var autoFire = true

    fun effectiveStatsFor(weapon: WeaponId = currentWeapon): EffectiveWeaponStats =
        WeaponAttachmentCatalog.computeEffectiveStats(weapon, weaponAttachments[weapon.key].orEmpty())
    var lastShotMs = 0L
    var targetAcquireMs = 0f
    var hasTargetLock = false
    var manualFireHeld = false

    // Melee swing animation & sweeping hitbox state
    var meleeSwingStartMs = 0L
    var meleeSwingDurationMs = 240L
    var meleeSwingDir = 1f
    var meleeSwingWeapon = WeaponId.KNIFE
    private val meleeHitZombieIds = mutableSetOf<Int>()

    val isSwingingMelee: Boolean
        get() = System.currentTimeMillis() - meleeSwingStartMs < meleeSwingDurationMs

    val meleeSwingProgress: Float
        get() = ((System.currentTimeMillis() - meleeSwingStartMs).toFloat() / meleeSwingDurationMs.toFloat())
            .coerceIn(0f, 1f)

    var currentRange = WeaponId.KNIFE.baseRange
    var currentHalfFov = WeaponId.KNIFE.baseHalfFov
    val ambientRadius = 112f

    // Rare power outages, rare flickers & Nighttime Zombie Frenzy
    var nextBlackoutAtMs = 0L
    var blackoutUntilMs = 0L
    var rareFlickerUntilMs = 0L
    var nextRareFlickerMs = 0L
    var frenzyUntilMs = 0L
    private var nextFrenzyDripMs = 0L
    private var nextFrenzyZombieId = 50_000
    private var wasInBlackout = false
    private var wasInFlicker = false
    private var lowNeedsToastCooldownMs = 0L

    val isFrenzyActive: Boolean
        get() = world?.mapId == GameMapId.OAKRIDGE_TOWN && System.currentTimeMillis() < frenzyUntilMs

    // Base Stockpile mirrors (prevents free hydration/food from nowhere)
    var baseFoodStock = 3
    var baseWaterStock = 3
    var baseMedsStock = 1

    // User Settings & Base Builder Mode
    var ambientWindEnabled = true
    var highVisibilityNight = false
    var cameraZoom = com.example.game.model.CameraViewBounds.DEFAULT_CAMERA_ZOOM
        private set
    var voxelDepthScale = com.example.game.model.CameraViewBounds.DEFAULT_VOXEL_DEPTH
        private set

    fun setCameraZoomClamped(zoom: Float): Float {
        val clamped = com.example.game.model.CameraViewBounds.clampZoom(zoom)
        cameraZoom = clamped
        return clamped
    }

    fun setVoxelDepthClamped(depth: Float): Float {
        val clamped = com.example.game.model.CameraViewBounds.clampVoxelDepth(depth)
        voxelDepthScale = clamped
        return clamped
    }

    fun setAmbientWind(enabled: Boolean) {
        ambientWindEnabled = enabled
        if (!enabled) {
            particles.removeAll { it.isLeaf || it.isPaper }
        }
    }

    var worldTimeHours = DayNightCycle.DEFAULT_START_HOUR
        private set
    var insideBuilding = false
    var buildModeActive = false
    var buildLayoutPieces: List<PlacedBasePiece> = emptyList()
    var selectedBuildPieceId: String? = null

    // Active runtime entities
    val zombies = mutableListOf<ZombieEntity>()
    val rivalOperators = mutableListOf<RivalOperatorEntity>()
    val deadBodyMarkers = mutableListOf<DeadBodyMarker>()
    val dynamicLootDrops = mutableListOf<LootContainer>()
    val bullets = mutableListOf<BulletEntity>()
    val particles = mutableListOf<ParticleFx>()
    val muzzleFlashes = mutableListOf<ParticleFx>()
    val damagePopups = mutableListOf<DamagePopup>()
    val baseNpcs = mutableListOf<BaseNpcRuntime>()
    var downedFriendlyBeacons: List<OperatorProfile> = emptyList()

    // Active operator's solo K-9 companion (deploys in Outpost Zero AND Oakridge!)
    var dogX = 35f
    var dogY = 24f
    var dogAngle = 0f
    var dogStepTimer = 0f
    var dogBarkUntilMs = 0L
    private var dogAttackCooldownMs = 0L
    private var dogAlertToastCooldownMs = 0L

    val leftStick = StickInput()
    val rightStick = StickInput()
    var keyMoveX = 0f
    var keyMoveY = 0f

    private var ambientSpawnAccum = 0f
    private var alarmBumpCooldownMs = 0L
    private var stairLatchActive = false
    private var lastStairTransitionMs = 0L
    private var lastZoneName = "DELAINE STREETS"
    private var toastText = ""
    private var toastUntilMs = 0L
    private var radioCalloutText = ""
    private var radioCalloutUntilMs = 0L
    private var lastPlayerShotMs = 0L
    private var lastPlayerLoudShotMs = 0L
    private var activeSlainMemory: List<SlainScavRecord> = emptyList()
    private val rng = Random(System.currentTimeMillis())

    var onOperatorDowned: (() -> Unit)? = null
    var onStationTriggered: ((StationKind) -> Unit)? = null
    var onTalkNpc: ((String) -> Unit)? = null
    var onLootOpened: ((LootSearchResult) -> Unit)? = null
    var onExtractTriggered: ((String) -> Unit)? = null
    var onBaseNeedsConsume: ((eat: Boolean, drink: Boolean) -> Unit)? = null
    var onZombieKilled: (() -> Unit)? = null
    var onContainerLooted: (() -> Unit)? = null
    var onPlayerKilledPlayer: ((killerCallsign: String, victimCallsign: String, weapon: WeaponId, zoneName: String) -> Unit)? = null
    var onNamedScavKilled: ((SlainScavRecord) -> Unit)? = null

    fun loadWorld(
        map: WorldMapData,
        operator: OperatorProfile?,
        roster: List<OperatorProfile>,
        resetSurvival: Boolean,
        baseStash: BaseStashState = BaseStashState(),
        slainScavMemory: List<SlainScavRecord> = activeSlainMemory
    ) {
        world = map
        activeOperator = operator
        rosterOperators = roster
        activeSlainMemory = slainScavMemory
        playerX = map.spawnX
        playerY = map.spawnY
        playerAngle = -PI.toFloat() / 2f
        elevation = 0
        prevElevation = 0
        floorFadeAlpha = 1f
        terrainHeight = computeTerrainHeight(map, playerX, playerY)
        raidStartMs = System.currentTimeMillis()
        stairLatchActive = false
        lastStairTransitionMs = 0L
        reloading = false
        targetAcquireMs = 0f
        hasTargetLock = false
        meleeSwingStartMs = 0L
        meleeHitZombieIds.clear()
        bullets.clear()
        particles.clear()
        muzzleFlashes.clear()
        damagePopups.clear()
        zombies.clear()
        rivalOperators.clear()
        deadBodyMarkers.clear()
        dynamicLootDrops.clear()
        lastPlayerShotMs = 0L
        lastPlayerLoudShotMs = 0L
        radioCalloutText = ""
        radioCalloutUntilMs = 0L

        if (map.mapId == GameMapId.OAKRIDGE_TOWN) {
            zombies.addAll(map.initialZombies.map { it.copy() })
            rivalOperators.addAll(map.initialScavs.map { it.copy() })
            applySlainScavKinship(map, slainScavMemory)
        }

        if (resetSurvival) {
            maxHp = (operator?.maxHp ?: 100).toFloat()
            hp = maxHp
            armorTier = operator?.armorTier ?: ArmorTier.NONE
            maxArmorHp = armorTier.maxShieldHp
            armorHp = maxArmorHp
            hunger = 94f
            thirst = 94f
            kills = 0
            syncBaseArmory(baseStash, operator, pickBestWeapon = true)
        } else {
            syncBaseArmory(baseStash, operator, pickBestWeapon = false)
        }

        val now = System.currentTimeMillis()
        blackoutUntilMs = 0L
        rareFlickerUntilMs = 0L
        frenzyUntilMs = 0L
        nextFrenzyDripMs = 0L
        wasInBlackout = false
        wasInFlicker = false
        // Very low blackout rate: long wait before first check + low trigger roll
        nextBlackoutAtMs = now + rng.nextLong(140_000L, 260_000L)
        nextRareFlickerMs = now + rng.nextLong(22_000L, 48_000L)

        dogX = playerX + 30f
        dogY = playerY + 18f

        rebuildBaseNpcs(roster, operator?.id)
    }

    private fun applySlainScavKinship(w: WorldMapData, slainMemory: List<SlainScavRecord>) {
        if (w.mapId != GameMapId.OAKRIDGE_TOWN || slainMemory.isEmpty()) return
        val slainIds = slainMemory.map { it.scavId }.toSet()
        // Remove previously slain named individuals so they don't magically resurrect unremarked
        rivalOperators.removeAll { it.scavId in slainIds }
        // Surviving kin in other encampments pick up investigation / vengeance trails
        for (scav in rivalOperators) {
            val inquiry = ScavKinshipRoster.resolveInquiriesForLivingScav(scav.scavId, slainMemory) ?: continue
            scav.isVengefulKin = true
            scav.kinInquiryTargetName = inquiry.slainRecord.displayName
            scav.kinRelationship = inquiry.relationshipToSlain
            scav.kinDeathDistrict = inquiry.slainRecord.districtName
            scav.kinSawKiller = inquiry.slainRecord.witnessedBySquad
            scav.aiState = ScavAiState.INVESTIGATE
            scav.investigateTimer = 45f
            scav.alertLevel = if (inquiry.slainRecord.witnessedBySquad) 0.9f else 0.45f
        }
    }

    fun syncBaseArmory(
        baseStash: BaseStashState,
        operator: OperatorProfile?,
        pickBestWeapon: Boolean = false
    ) {
        activeOperator = operator ?: activeOperator
        baseFoodStock = baseStash.foodStock
        baseWaterStock = baseStash.waterStock
        baseMedsStock = baseStash.medsStock
        weaponAttachments = baseStash.weaponAttachmentsMap
        unlockedWeapons.clear()
        unlockedWeapons.add(WeaponId.KNIFE)

        baseStash.unlockedWeaponKeys.forEach { wk ->
            val w = WeaponId.entries.firstOrNull { it.key == wk }
            if (w != null && w !in unlockedWeapons) unlockedWeapons.add(w)
        }
        operator?.backpackItems?.forEach { item ->
            item.weaponKey?.let { wk ->
                val w = WeaponId.entries.firstOrNull { it.key == wk }
                if (w != null && w !in unlockedWeapons) unlockedWeapons.add(w)
            }
        }

        for (w in WeaponId.entries) {
            if (w.isMelee) {
                magAmmo[w] = 0
                reserveAmmo[w] = 0
            } else {
                val cap = effectiveStatsFor(w).magSize
                val total = baseStash.ammoForWeapon(w).coerceAtLeast(0)
                val mag = min(cap, total)
                val res = (total - mag).coerceIn(0, max(w.reserveMax, total))
                magAmmo[w] = mag
                reserveAmmo[w] = res
            }
        }

        val nextArmor = operator?.armorTier ?: ArmorTier.NONE
        val prevMax = maxArmorHp
        armorTier = nextArmor
        maxArmorHp = nextArmor.maxShieldHp
        if (world?.mapId == GameMapId.OUTPOST_ZERO || maxArmorHp > prevMax) {
            armorHp = maxArmorHp
        } else {
            armorHp = armorHp.coerceIn(0f, maxArmorHp)
        }

        if (pickBestWeapon || currentWeapon !in unlockedWeapons) {
            val readyFirearm = unlockedWeapons.lastOrNull { w ->
                !w.isMelee && ((magAmmo[w] ?: 0) + (reserveAmmo[w] ?: 0)) > 0
            }
            val bestMelee = unlockedWeapons.lastOrNull { it.isMelee }
            currentWeapon = readyFirearm ?: bestMelee ?: WeaponId.KNIFE
        }
    }

    fun rebuildBaseNpcs(roster: List<OperatorProfile>, activeId: String?) {
        val w = world ?: return
        if (w.mapId != GameMapId.OUTPOST_ZERO) {
            baseNpcs.clear()
            return
        }
        val offDutyIds = roster.filter { it.id != activeId && !it.isKia && !it.isPermadead }.map { it.id }.toSet()
        baseNpcs.removeAll { it.operatorId !in offDutyIds }

        val waypoints = w.patrolWaypoints.ifEmpty {
            listOf(-180f to -90f, 180f to -90f, 180f to 90f, -180f to 90f)
        }
        var idx = baseNpcs.size
        for (op in roster) {
            if (op.id !in offDutyIds) continue
            if (baseNpcs.any { it.operatorId == op.id }) continue
            val wp = waypoints[idx % waypoints.size]
            baseNpcs.add(
                BaseNpcRuntime(
                    operatorId = op.id,
                    x = wp.first,
                    y = wp.second,
                    angle = rng.nextFloat() * 6.28f,
                    hunger = 52f + rng.nextFloat() * 40f,
                    thirst = 52f + rng.nextFloat() * 40f,
                    energy = 48f + rng.nextFloat() * 45f,
                    patrolIdx = idx % max(1, w.patrolWaypoints.size),
                    statusTag = "PATROL"
                )
            )
            idx++
        }
    }

    private fun onPowerFlickered(w: WorldMapData) {
        for (p in w.props) {
            if (p.propType == "tv" && p.tvPoweredOn) {
                p.tvPoweredOn = false
                p.tvKilledByFlicker = true
            }
        }
    }

    private fun onPowerLost(w: WorldMapData) {
        for (p in w.props) {
            if (p.propType == "tv") {
                p.tvPoweredOn = false
            }
        }
    }

    private fun onPowerRestored(w: WorldMapData) {
        var turnedOnCount = 0
        for (p in w.props) {
            if (p.propType == "tv") {
                val restoreChance = if (p.tvKilledByFlicker) 0.28f else 0.55f
                if (rng.nextFloat() < restoreChance) {
                    p.tvPoweredOn = true
                    p.tvKilledByFlicker = false
                    turnedOnCount++
                } else {
                    p.tvPoweredOn = false
                }
            }
        }
        if (turnedOnCount > 0) {
            showToast("POWER RESTORED — $turnedOnCount TV(s) FLICKERED BACK ON")
        }
    }

    fun nudgePlayerClearOfWalls() {
        val w = world ?: return
        if (canEntityMoveTo(w, playerX, playerY, playerRadius, elevation, false, 0L)) return
        val radii = listOf(28f, 56f, 90f, 140f, 200f)
        for (rad in radii) {
            for (deg in 0 until 360 step 45) {
                val a = deg * PI.toFloat() / 180f
                val tx = playerX + cos(a) * rad
                val ty = playerY + sin(a) * rad
                if (canEntityMoveTo(w, tx, ty, playerRadius, elevation, false, 0L)) {
                    playerX = tx
                    playerY = ty
                    return
                }
            }
        }
        playerX = w.spawnX
        playerY = w.spawnY
    }

    fun showToast(msg: String) {
        toastText = msg
        toastUntilMs = System.currentTimeMillis() + 2600L
    }

    fun showRadioCallout(msg: String, durationMs: Long = 3800L) {
        radioCalloutText = msg
        radioCalloutUntilMs = System.currentTimeMillis() + durationMs
    }

    fun triggerEmergencyBlackout(durationMs: Long = 15_000L, now: Long = System.currentTimeMillis()) {
        val w = world ?: return
        if (w.mapId != GameMapId.OAKRIDGE_TOWN) return
        blackoutUntilMs = now + durationMs
        nextBlackoutAtMs = blackoutUntilMs + rng.nextLong(180_000L, 320_000L)
        onPowerLost(w)
        wasInBlackout = true

        val phase = DayNightCycle.phaseForHour(worldTimeHours)
        if (phase.isNight) {
            frenzyUntilMs = max(blackoutUntilMs, now + 22_000L)
            nextFrenzyDripMs = now + 3_400L
            spawnFrenzySwarmBurst(w, count = 7)
            dogBarkUntilMs = now + 1_200L
            showToast("NIGHT BLACKOUT — INFECTED FRENZY!")
            showRadioCallout("COMMS // Grid failed in the dark — frenzied infected are swarming!", 4500L)
        } else {
            showToast("POWER GRID DROP — EMERGENCY BLACKOUT")
        }
    }

    private fun spawnFrenzySwarmBurst(w: WorldMapData, count: Int) {
        if (w.mapId != GameMapId.OAKRIDGE_TOWN) return
        val distTier = currentDistrictDangerTier(w)
        var spawned = 0
        var attempts = 0
        while (spawned < count && attempts < count * 8) {
            attempts++
            val ang = rng.nextFloat() * 2f * PI.toFloat()
            val rad = 285f + rng.nextFloat() * 165f
            val sx = (playerX + cos(ang) * rad).coerceIn(-3120f, 3120f)
            val sy = (playerY + sin(ang) * rad).coerceIn(-3120f, 3120f)
            if (!canEntityMoveTo(w, sx, sy, 12f, elevation, checkAlarms = false, now = 0L)) continue
            val variant = if (rng.nextFloat() < 0.58f) ZombieVariant.SPRINTER else ZombieVariant.WALKER
            val z = DeLaineEnemyAndCampBuilder.createZombie(
                id = nextFrenzyZombieId++,
                x = sx,
                y = sy,
                elevation = elevation,
                variant = variant,
                tier = max(distTier, 2),
                angle = atan2(playerY - sy, playerX - sx),
                isFrenzy = true
            )
            z.alertTargetX = playerX
            z.alertTargetY = playerY
            z.alertTimer = 18f
            zombies.add(z)
            spawned++
        }
    }

    private fun currentDistrictDangerTier(w: WorldMapData): Int {
        if (w.mapId == GameMapId.OUTPOST_ZERO) return 0
        val inRedoubt = w.scavEncampments.firstOrNull {
            hypot(playerX - it.centerX, playerY - it.centerY) <= it.radius + 80f
        }
        if (inRedoubt != null) return inRedoubt.dangerTier
        return w.districtZones.firstOrNull {
            playerX in it.bounds.x..(it.bounds.x + it.bounds.w) &&
                playerY in it.bounds.y..(it.bounds.y + it.bounds.h)
        }?.dangerTier ?: 1
    }

    fun grantWeapon(weapon: WeaponId) {
        val cap = if (weapon.isMelee) 0 else effectiveStatsFor(weapon).magSize
        if (weapon !in unlockedWeapons) {
            unlockedWeapons.add(weapon)
            if (!weapon.isMelee) {
                magAmmo[weapon] = cap
                reserveAmmo[weapon] = weapon.reserveMax
            }
            currentWeapon = weapon
            showToast("EQUIPPED: ${weapon.displayName}")
        } else if (!weapon.isMelee) {
            reserveAmmo[weapon] = (reserveAmmo[weapon] ?: 0) + cap * 2
            showToast("+AMMO FOR ${weapon.shortName}")
        } else {
            currentWeapon = weapon
            showToast("SELECTED: ${weapon.shortName}")
        }
    }

    fun swapWeapon() {
        if (unlockedWeapons.isEmpty()) return
        val idx = unlockedWeapons.indexOf(currentWeapon)
        currentWeapon = unlockedWeapons[(idx + 1) % unlockedWeapons.size]
        reloading = false
    }

    fun toggleAutoFire() {
        autoFire = !autoFire
    }

    fun triggerReload() {
        if (reloading || currentWeapon.isMelee) return
        val cap = effectiveStatsFor(currentWeapon).magSize
        val mag = magAmmo[currentWeapon] ?: 0
        val res = reserveAmmo[currentWeapon] ?: 0
        if (mag >= cap || res <= 0) return

        reloading = true
        reloadStartMs = System.currentTimeMillis()
        audio.playReload()
    }

    fun jumpToWorldTimeHours(hours: Float) {
        worldTimeHours = DayNightCycle.normalizeHours(hours)
        val clock = DayNightCycle.formatClock12(worldTimeHours)
        val phase = DayNightCycle.phaseForHour(worldTimeHours).label
        showToast("TIME SET TO $clock ($phase)")
    }

    fun transitionFloor(targetFloor: Int) {
        if (elevation == targetFloor) return
        prevElevation = elevation
        elevation = targetFloor
        floorFadeAlpha = 0f
        audio.playStairs()
    }

    fun step(dtRaw: Float): HudSnapshot {
        val dt = dtRaw.coerceIn(0.001f, 0.08f)
        val now = System.currentTimeMillis()
        val w = world ?: return HudSnapshot()

        // 0. Continuous 24-hour day/night clock (5 real minutes = 1 in-game hour)
        worldTimeHours = DayNightCycle.normalizeHours(
            worldTimeHours + dt / DayNightCycle.REAL_SECONDS_PER_GAME_HOUR
        )

        // 1. Floor cross-fade (~0.48s smooth fade when walking between stairwell floors)
        if (floorFadeAlpha < 1f) {
            floorFadeAlpha = min(1f, floorFadeAlpha + dt * 2.1f)
        }

        // 2. Smooth door swing + sliding glass auto-sensor + roll-up garage door animation
        for (d in w.doors) {
            if (d.doorStyle == DoorStyle.DOUBLE_SLIDING_GLASS && d.autoSensor && d.floor == elevation) {
                val jammed = d.jammedByPropId != null && w.props.any { it.id == d.jammedByPropId && !it.cleared }
                if (!jammed) {
                    val midX = d.hingeX + cos(d.baseAngle) * (d.length * 0.5f)
                    val midY = d.hingeY + sin(d.baseAngle) * (d.length * 0.5f)
                    val nearActor = hypot(playerX - midX, playerY - midY) <= 98f ||
                        rivalOperators.any { it.elevation == d.floor && hypot(it.x - midX, it.y - midY) <= 95f }
                    val farActor = hypot(playerX - midX, playerY - midY) > 132f &&
                        rivalOperators.none { it.elevation == d.floor && hypot(it.x - midX, it.y - midY) <= 132f }
                    if (nearActor && !d.open) {
                        d.open = true
                        d.targetAngle = d.baseAngle + d.openOffset
                    } else if (farActor && d.open) {
                        d.open = false
                        d.targetAngle = d.baseAngle
                    }
                }
            }
            val targetSlide = if (d.open) 1f else 0f
            val slideDiff = targetSlide - d.slideProgress
            if (abs(slideDiff) > 0.005f) {
                d.slideProgress += slideDiff * min(1f, dt * 6.5f)
            } else {
                d.slideProgress = targetSlide
            }
            val diff = d.targetAngle - d.angle
            if (abs(diff) > 0.008f) {
                d.angle += diff * min(1f, dt * 11f)
            } else {
                d.angle = d.targetAngle
            }
        }

        // 3. Very rare grid blackouts (low trigger rate), flickers & Nighttime Zombie Frenzy
        if (w.mapId == GameMapId.OAKRIDGE_TOWN) {
            if (blackoutUntilMs == 0L && now >= nextBlackoutAtMs) {
                if (rng.nextFloat() < 0.34f) {
                    triggerEmergencyBlackout(durationMs = rng.nextLong(12_000L, 18_000L), now = now)
                } else {
                    nextBlackoutAtMs = now + rng.nextLong(110_000L, 190_000L)
                }
            } else if (blackoutUntilMs > 0L && now >= blackoutUntilMs) {
                blackoutUntilMs = 0L
                wasInBlackout = false
                showToast("TOWN POWER RESTORED")
                onPowerRestored(w)
            }
            if (now < frenzyUntilMs && now >= nextFrenzyDripMs) {
                nextFrenzyDripMs = now + 3_500L
                if (zombies.count { it.isFrenzy } < 14) {
                    spawnFrenzySwarmBurst(w, count = 2)
                }
            } else if (frenzyUntilMs > 0L && now >= frenzyUntilMs) {
                frenzyUntilMs = 0L
                showToast("FRENZY WAVE SUBSIDING")
            }
            if (now >= nextRareFlickerMs && blackoutUntilMs == 0L) {
                rareFlickerUntilMs = now + 420L
                nextRareFlickerMs = now + rng.nextLong(22_000L, 46_000L)
            }
        } else {
            if (wasInBlackout) {
                onPowerRestored(w)
            }
            blackoutUntilMs = 0L
            frenzyUntilMs = 0L
            wasInBlackout = false
        }
        val inFlickerNow = now < rareFlickerUntilMs && blackoutUntilMs == 0L
        if (inFlickerNow && !wasInFlicker) {
            onPowerFlickered(w)
        }
        wasInFlicker = inFlickerNow

        // 4. Reload timer
        val statsNow = effectiveStatsFor(currentWeapon)
        if (reloading) {
            if (now - reloadStartMs >= statsNow.reloadTimeMs) {
                val cap = statsNow.magSize
                val mag = magAmmo[currentWeapon] ?: 0
                val res = reserveAmmo[currentWeapon] ?: 0
                val take = min(cap - mag, res)
                magAmmo[currentWeapon] = mag + take
                reserveAmmo[currentWeapon] = res - take
                reloading = false
            }
        }

        // 5. Survival hunger/thirst decay (walk to Cantine/Infirmary in Outpost Zero — never free from nowhere)
        val decayMul = if (w.mapId == GameMapId.OAKRIDGE_TOWN) 1f else 0.25f
        hunger = max(0f, hunger - dt * 0.28f * decayMul)
        thirst = max(0f, thirst - dt * 0.36f * decayMul)
        if (hunger <= 0f || thirst <= 0f) {
            if (w.mapId == GameMapId.OAKRIDGE_TOWN) {
                hp = max(0f, hp - dt * 1.4f)
            } else if (hp > 25f) {
                hp = max(25f, hp - dt * 0.45f)
            }
        }
        if (w.mapId == GameMapId.OUTPOST_ZERO && now >= lowNeedsToastCooldownMs && (thirst < 30f || hunger < 30f)) {
            lowNeedsToastCooldownMs = now + 18_000L
            if (thirst < 30f) {
                showToast("THIRSTY — WALK TO CANTINE WATER COOLER")
            } else {
                showToast("HUNGRY — WALK TO CANTINE RATION COUNTER")
            }
        }

        // 6. Player Movement (Left Stick or Keyboard) with strict perimeter bounds
        var mx = if (leftStick.active) leftStick.vx else keyMoveX
        var my = if (leftStick.active) leftStick.vy else keyMoveY
        val mLen = hypot(mx, my)
        if (mLen > 1f) {
            mx /= mLen
            my /= mLen
        }
        moveRatio = hypot(mx, my)
        val speedMul = activeOperator?.speedMul ?: 1f
        val stepDist = 3.65f * speedMul * (dt * 60f)

        val nx = playerX + mx * stepDist
        val ny = playerY + my * stepDist
        if (canEntityMoveTo(w, nx, playerY, playerRadius, elevation, checkAlarms = true, now = now) &&
            canTraverseTerrain(w, playerX, playerY, nx, playerY, elevation)
        ) {
            playerX = nx
        }
        if (canEntityMoveTo(w, playerX, ny, playerRadius, elevation, checkAlarms = true, now = now) &&
            canTraverseTerrain(w, playerX, playerY, playerX, ny, elevation)
        ) {
            playerY = ny
        }
        if (moveRatio > 0.12f) stepTimer += dt * moveRatio * 9f
        terrainHeight = if (elevation == 0) computeTerrainHeight(w, playerX, playerY) else 0

        // 7. Aim Angle (Right stick overrides movement direction)
        if (rightStick.active && hypot(rightStick.vx, rightStick.vy) > 0.14f) {
            playerAngle = atan2(rightStick.vy, rightStick.vx)
        } else if (moveRatio > 0.16f && !rightStick.active) {
            playerAngle = atan2(my, mx)
        }

        // 8. Dynamic Weapon Aim Cone (Movement bloom vs Stillness focus + High-Ground Elevation Sight Bonus)
        var targetRange = statsNow.baseRange - moveRatio * statsNow.moveRangePenalty
        var targetFov = statsNow.baseHalfFov + moveRatio * statsNow.moveFovBloom
        if (moveRatio < 0.12f) {
            targetRange += statsNow.stillRangeBonus
            targetFov -= statsNow.stillFovTighten
        }
        if (elevation == 0 && terrainHeight > 0) {
            targetRange *= (1f + terrainHeight * 0.16f)
        }
        currentRange += (targetRange - currentRange) * min(1f, dt * 9f)
        currentHalfFov += (targetFov - currentHalfFov) * min(1f, dt * 9f)

        // 9. Roof fade smoothly when approaching a building/door so entry doors & interiors are visible
        val outdoorDistrict = if (w.mapId == GameMapId.OUTPOST_ZERO) {
            "OUTPOST ZERO COMPOUND"
        } else {
            w.districtZones.firstOrNull {
                playerX in it.bounds.x..(it.bounds.x + it.bounds.w) &&
                    playerY in it.bounds.y..(it.bounds.y + it.bounds.h)
            }?.name ?: "DELAINE STREETS"
        }
        var currentBuildingName = outdoorDistrict
        var activeBuildingId = -1
        for (b in w.buildings) {
            val closeX = max(b.bounds.x, min(playerX, b.bounds.x + b.bounds.w))
            val closeY = max(b.bounds.y, min(playerY, b.bounds.y + b.bounds.h))
            val distToRect = hypot(playerX - closeX, playerY - closeY)
            val inside = distToRect <= 2f
            if (inside) {
                activeBuildingId = b.id
                currentBuildingName = b.name
            }
            val inRoofOverhang = playerX in (b.bounds.x - 24f)..(b.bounds.x + b.bounds.w + 24f) &&
                playerY in (b.bounds.y - 54f)..(b.bounds.y + b.bounds.h + 24f)
            val nearDoor = w.doors.any { d ->
                d.buildingId == b.id && d.floor == elevation && hypot(playerX - d.hingeX, playerY - d.hingeY) <= 135f
            }
            val targetAlpha = when {
                inside || (elevation != 0 && activeBuildingId == b.id) -> 0f
                nearDoor || inRoofOverhang || distToRect <= 46f -> 0.08f
                distToRect < 135f -> (0.08f + ((distToRect - 46f) / 89f) * 0.92f).coerceIn(0.08f, 1f)
                else -> 1f
            }
            b.roofAlpha += (targetAlpha - b.roofAlpha) * min(1f, dt * 10f)
        }

        insideBuilding = activeBuildingId != -1
        lastZoneName = currentBuildingName

        // 9B. Stairwells-Only Traversal (no elevation modal — walk up/down stairs to smoothly fade into next floor)
        handleStairwellTraversal(w, now)

        // 10. Combat: Sweeping Melee + Firearm Target-Lock / Manual Fire (Zombies & Rival Operators)
        if (w.mapId == GameMapId.OAKRIDGE_TOWN) {
            var targetInCone = false
            fun checkTargetInCone(tx: Float, ty: Float, tr: Float): Boolean {
                val dist = hypot(tx - playerX, ty - playerY)
                if (currentWeapon.isMelee) {
                    val reach = currentWeapon.meleeReach + tr + 6f
                    if (dist <= 34f) return true
                    if (dist <= reach) {
                        val angToTarget = atan2(ty - playerY, tx - playerX)
                        val diff = atan2(sin(angToTarget - playerAngle), cos(angToTarget - playerAngle))
                        if (abs(diff) <= currentWeapon.meleeArcRad * 0.92f) return true
                    }
                } else if (dist < currentRange) {
                    val angToTarget = atan2(ty - playerY, tx - playerX)
                    val diff = atan2(sin(angToTarget - playerAngle), cos(angToTarget - playerAngle))
                    if (abs(diff) < currentHalfFov * 0.88f) return true
                }
                return false
            }

            for (z in zombies) {
                if (z.elevation == elevation && checkTargetInCone(z.x, z.y, z.radius)) {
                    targetInCone = true
                    break
                }
            }
            if (!targetInCone) {
                for (r in rivalOperators) {
                    if (r.elevation == elevation && checkTargetInCone(r.x, r.y, r.radius)) {
                        targetInCone = true
                        break
                    }
                }
            }
            val stickPullFire = rightStick.active && hypot(rightStick.vx, rightStick.vy) > 0.74f
            if (manualFireHeld || stickPullFire) {
                hasTargetLock = targetInCone
                tryFireWeapon(w, now)
            } else if (autoFire && targetInCone) {
                targetAcquireMs += dt * 1000f
                hasTargetLock = true
                val lockDelay = if (currentWeapon.isMelee) 35f else 115f
                if (targetAcquireMs >= lockDelay) {
                    tryFireWeapon(w, now)
                }
            } else {
                targetAcquireMs = 0f
                hasTargetLock = false
            }
        } else {
            // Allow manual melee practice swing inside Outpost Zero!
            if (manualFireHeld && currentWeapon.isMelee) {
                tryFireWeapon(w, now)
            }
            targetAcquireMs = 0f
            hasTargetLock = false
        }

        // Active melee swing hitbox sweep across multiple frames for crisp reliable hits
        if (isSwingingMelee) {
            evaluateMeleeHitbox(w)
        }

        // 11. Zombies & Rival Operators AI + Wall/Door Collision
        updateZombies(w, dt)
        updateRivalOperators(w, dt, now)

        // 12. Bullets & Particles
        updateBulletsAndParticles(w, dt)

        // 13. Solo K-9 Companion (deploys in BOTH Outpost Zero & Oakridge!) & Base Crew AI
        updateDogCompanion(w, dt, now)
        if (w.mapId == GameMapId.OUTPOST_ZERO) {
            updateBaseNpcs(w, dt)
        }

        // 14. Ambient Wind Particles
        updateAmbientWind(w, dt)

        // 15. Context Prompts & Floor Label
        val prompt = findContextPrompt(w)
        val activeBuilding = if (activeBuildingId != -1) w.buildings.firstOrNull { it.id == activeBuildingId } else null

        val floorLabel = when {
            elevation == -1 -> "B1"
            elevation == 0 -> "1F"
            activeBuilding?.roofFloorIndex == elevation -> "ROOF"
            else -> "${elevation}F"
        }

        val blackoutLeft = if (blackoutUntilMs > now) ((blackoutUntilMs - now) / 1000L).toInt() + 1 else 0
        val frenzyActiveNow = w.mapId == GameMapId.OAKRIDGE_TOWN && now < frenzyUntilMs
        val frenzyLeft = if (frenzyActiveNow) ((frenzyUntilMs - now) / 1000L).toInt() + 1 else 0
        val alertScavCount = if (w.mapId == GameMapId.OAKRIDGE_TOWN) {
            rivalOperators.count {
                it.aiState == ScavAiState.ENGAGE ||
                    it.aiState == ScavAiState.TAKE_COVER ||
                    it.aiState == ScavAiState.RETREAT
            }
        } else 0
        val focusPct = (100 - moveRatio * 65f).toInt().coerceIn(25, 100)
        val focusLabel = when {
            moveRatio < 0.12f -> "ZOOMED"
            moveRatio < 0.65f -> "STABLE"
            else -> "BLOOM"
        }
        val timePhase = DayNightCycle.phaseForHour(worldTimeHours)

        return HudSnapshot(
            mapId = w.mapId,
            zoneName = currentBuildingName,
            floorLabel = floorLabel,
            hp = hp,
            maxHp = maxHp,
            armorHp = armorHp,
            maxArmorHp = maxArmorHp,
            armorTier = armorTier,
            hunger = hunger,
            thirst = thirst,
            weapon = currentWeapon,
            availableWeapons = unlockedWeapons.toList(),
            magAmmo = magAmmo[currentWeapon] ?: 0,
            reserveAmmo = reserveAmmo[currentWeapon] ?: 0,
            reloading = reloading,
            autoFire = autoFire,
            meleeSwingActive = isSwingingMelee,
            focusLabel = focusLabel,
            focusPct = focusPct,
            kills = kills,
            playerX = playerX,
            playerY = playerY,
            playerAngle = playerAngle,
            elevation = elevation,
            terrainHeight = terrainHeight,
            raidElapsedSecs = ((now - raidStartMs) / 1000L).toInt().coerceAtLeast(0),
            worldTimeHours = worldTimeHours,
            timeClockLabel = DayNightCycle.formatClock12(worldTimeHours),
            timePhaseLabel = timePhase.label,
            isNightTime = timePhase.isNight,
            insideBuilding = insideBuilding,
            powerGridOutage = blackoutUntilMs > now,
            blackoutSecsLeft = blackoutLeft,
            contextPrompt = prompt,
            openExtractions = w.extractionZones.filter { it.isOpen },
            statusToast = if (now < toastUntilMs) toastText else "",
            equippedAttachmentKeys = weaponAttachments[currentWeapon.key].orEmpty(),
            effectiveStats = statsNow,
            frenzyActive = frenzyActiveNow,
            frenzySecsLeft = frenzyLeft,
            activeScavAlertCount = alertScavCount,
            dangerTier = currentDistrictDangerTier(w),
            radioCallout = if (now < radioCalloutUntilMs) radioCalloutText else ""
        )
    }

    fun performContextAction() {
        val w = world ?: return
        val prompt = findContextPrompt(w)
        when (prompt.kind) {
            ContextPromptKind.DOOR -> {
                val d = w.doors.firstOrNull { it.id == prompt.targetId } ?: return
                if (d.jammedByPropId != null) {
                    val jamProp = w.props.firstOrNull { it.id == d.jammedByPropId && !it.cleared }
                    if (jamProp != null) {
                        showToast("DOOR JAMMED BY FALLEN CRATE INSIDE!")
                        audio.playDoor(staleCreak = false)
                        return
                    }
                }
                d.open = !d.open
                d.targetAngle = if (d.open) d.baseAngle + d.openOffset else d.baseAngle
                audio.playDoor(d.isStale)
                if (d.isStale) {
                    d.isStale = false
                    repeat(8) {
                        particles.add(
                            ParticleFx(
                                x = d.hingeX + cos(d.baseAngle) * d.length * 0.5f + (rng.nextFloat() - 0.5f) * 20f,
                                y = d.hingeY + sin(d.baseAngle) * d.length * 0.5f + (rng.nextFloat() - 0.5f) * 14f,
                                vx = (rng.nextFloat() - 0.5f) * 22f,
                                vy = 10f + rng.nextFloat() * 18f,
                                life = 0.75f,
                                maxLife = 0.75f,
                                size = 4f + rng.nextFloat() * 3f,
                                colorHex = 0xFFD6CFC2
                            )
                        )
                    }
                }
            }
            ContextPromptKind.CLEAR_CRATE -> {
                val p = w.props.firstOrNull { it.id == prompt.targetId } ?: return
                p.cleared = true
                showToast("CLEARED FALLEN CRATE — DOOR UNBLOCKED")
                audio.playDoor(staleCreak = true)
            }
            ContextPromptKind.SEARCH_VEHICLE -> {
                val v = w.props.firstOrNull { it.id == prompt.targetId } ?: return
                if (v.hasAlarm && v.alarmActiveUntil < System.currentTimeMillis()) {
                    triggerCarAlarm(v)
                }
                val firstSearch = !v.searched
                v.searched = true
                if (firstSearch) onContainerLooted?.invoke()
                onLootOpened?.invoke(
                    LootSearchResult(
                        title = v.vehicleKind?.label ?: "Vehicle Trunk",
                        items = v.lootItems.toList(),
                        sourceId = v.id,
                        isVehicle = true
                    )
                )
            }
            ContextPromptKind.LOOT_CONTAINER -> {
                val c = (w.lootContainers.asSequence() + dynamicLootDrops.asSequence())
                    .firstOrNull { it.id == prompt.targetId } ?: return
                val firstSearch = !c.searched
                c.searched = true
                if (firstSearch) onContainerLooted?.invoke()
                onLootOpened?.invoke(
                    LootSearchResult(
                        title = c.label,
                        items = c.items.toList(),
                        sourceId = c.id,
                        isVehicle = false
                    )
                )
            }
            ContextPromptKind.TALK_NPC -> {
                onTalkNpc?.invoke(prompt.targetId)
            }
            ContextPromptKind.STATION -> {
                val st = w.stations.firstOrNull { it.id == prompt.targetId } ?: return
                onStationTriggered?.invoke(st.kind)
            }
            ContextPromptKind.EXTRACT -> {
                val ez = w.extractionZones.firstOrNull { it.id == prompt.targetId } ?: return
                onExtractTriggered?.invoke(ez.name)
            }
            ContextPromptKind.NONE -> Unit
        }
    }

    fun removeLootItemFromWorld(sourceId: String, isVehicle: Boolean, itemUid: String) {
        val w = world ?: return
        if (isVehicle) {
            w.props.firstOrNull { it.id == sourceId }?.lootItems?.removeAll { it.uid == itemUid }
        } else {
            w.lootContainers.firstOrNull { it.id == sourceId }?.items?.removeAll { it.uid == itemUid }
            dynamicLootDrops.firstOrNull { it.id == sourceId }?.items?.removeAll { it.uid == itemUid }
        }
    }

    private fun tryFireWeapon(w: WorldMapData, now: Long) {
        if (reloading) return
        val stats = effectiveStatsFor(currentWeapon)
        if (now - lastShotMs < stats.fireDelayMs) return

        val damageAmount = stats.damage

        if (currentWeapon.isMelee) {
            lastShotMs = now
            lastPlayerShotMs = now
            meleeSwingStartMs = now
            meleeSwingDurationMs = (stats.fireDelayMs - 20L).coerceIn(190L, 320L)
            meleeSwingDir = -meleeSwingDir
            meleeSwingWeapon = currentWeapon
            meleeHitZombieIds.clear()
            audio.playWeapon(currentWeapon, isSuppressedOverride = true)
            emitNoise(stats.noiseRadius, playerX, playerY, fromPlayer = true, loudGunshot = false)
            evaluateMeleeHitbox(w)
            return
        }

        val mag = magAmmo[currentWeapon] ?: 0
        if (mag <= 0) {
            triggerReload()
            return
        }
        magAmmo[currentWeapon] = mag - 1

        lastShotMs = now
        lastPlayerShotMs = now
        if (!stats.suppressed) {
            lastPlayerLoudShotMs = now
        }
        audio.playWeapon(currentWeapon, isSuppressedOverride = stats.suppressed)
        emitNoise(stats.noiseRadius, playerX, playerY, fromPlayer = true, loudGunshot = !stats.suppressed)

        val mx = playerX + cos(playerAngle) * currentWeapon.gunLength
        val my = playerY + sin(playerAngle) * currentWeapon.gunLength
        muzzleFlashes.add(
            ParticleFx(mx, my, 0f, 0f, 0.08f, 0.08f, if (stats.suppressed) 6f else 11f, currentWeapon.tracerColorHex)
        )

        val shooterCallsign = activeOperator?.callsign ?: "OPERATOR"
        repeat(currentWeapon.pelletCount) {
            val spread = playerAngle + (rng.nextFloat() - 0.5f) * stats.spreadRad
            bullets.add(
                BulletEntity(
                    x = mx,
                    y = my,
                    vx = cos(spread) * currentWeapon.bulletSpeed,
                    vy = sin(spread) * currentWeapon.bulletSpeed,
                    rangeLeft = currentRange,
                    damage = damageAmount,
                    elevation = elevation,
                    colorHex = currentWeapon.tracerColorHex,
                    fromRival = false,
                    shooterCallsign = shooterCallsign,
                    weaponUsed = currentWeapon
                )
            )
        }
    }

    private fun evaluateMeleeHitbox(w: WorldMapData) {
        if (w.mapId != GameMapId.OAKRIDGE_TOWN) return
        val dmg = meleeSwingWeapon.damage.toFloat()
        val reach = meleeSwingWeapon.meleeReach
        val halfArc = meleeSwingWeapon.meleeArcRad
        val now = System.currentTimeMillis()

        val iter = zombies.listIterator()
        while (iter.hasNext()) {
            val z = iter.next()
            if (z.elevation != elevation || z.id in meleeHitZombieIds) continue
            val dx = z.x - playerX
            val dy = z.y - playerY
            val dist = hypot(dx, dy)
            val ang = atan2(dy, dx)
            val diff = abs(atan2(sin(ang - playerAngle), cos(ang - playerAngle)))
            if (dist <= 34f || (dist <= reach + z.radius && diff <= halfArc)) {
                meleeHitZombieIds.add(z.id)
                z.hp -= dmg
                z.hitFlashUntilMs = now + 95L
                spawnDamagePopup(z.x, z.y - 24f, dmg.toInt(), critical = dmg >= 55f)
                // Knockback away from player without pushing into solid walls
                val kb = meleeSwingWeapon.knockback * if (z.variant == ZombieVariant.ARMORED) 0.45f else 1f
                if (kb > 0f && dist > 1f) {
                    val kx = (dx / dist) * (kb * 0.5f)
                    val ky = (dy / dist) * (kb * 0.5f)
                    repeat(2) {
                        if (canEntityMoveTo(w, z.x + kx, z.y, z.radius, z.elevation, false, 0L)) z.x += kx
                        if (canEntityMoveTo(w, z.x, z.y + ky, z.radius, z.elevation, false, 0L)) z.y += ky
                    }
                }
                spawnHitSparks(z.x, z.y, 0xFFEF4444)
                if (z.hp <= 0f) {
                    iter.remove()
                    kills++
                    onZombieKilled?.invoke()
                }
            }
        }

        val rivalIter = rivalOperators.listIterator()
        while (rivalIter.hasNext()) {
            val r = rivalIter.next()
            val rivalHashId = -r.id.hashCode()
            if (r.elevation != elevation || rivalHashId in meleeHitZombieIds) continue
            val dx = r.x - playerX
            val dy = r.y - playerY
            val dist = hypot(dx, dy)
            val ang = atan2(dy, dx)
            val diff = abs(atan2(sin(ang - playerAngle), cos(ang - playerAngle)))
            if (dist <= 34f || (dist <= reach + r.radius && diff <= halfArc)) {
                meleeHitZombieIds.add(rivalHashId)
                r.hp -= dmg
                r.hitFlashUntilMs = now + 95L
                spawnDamagePopup(r.x, r.y - 26f, dmg.toInt(), critical = dmg >= 55f)
                spawnHitSparks(r.x, r.y, 0xFFEF4444)
                onScavDamaged(w, r, playerX, playerY, fromPlayer = true, now = now)
                if (r.hp <= 0f) {
                    rivalIter.remove()
                    handleScavKilled(w, r, byPlayer = true, weaponUsed = meleeSwingWeapon, now = now)
                }
            }
        }
    }

    private fun triggerCarAlarm(car: PropObj) {
        val now = System.currentTimeMillis()
        car.hasAlarm = false
        car.alarmActiveUntil = now + 12_000L
        audio.playCarAlarmBurst()
        emitNoise(520f, car.x + car.w / 2f, car.y + car.h / 2f, fromPlayer = false, loudGunshot = true)
        showToast("CAR ALARM TRIGGERED! VEHICLE FLOODLIGHTS FLASHING!")
    }

    private fun emitNoise(
        radius: Float,
        nx: Float,
        ny: Float,
        fromPlayer: Boolean = false,
        loudGunshot: Boolean = true
    ) {
        val w = world ?: return
        val effectiveZombieRadius = if (isFrenzyActive) radius * 1.45f else radius
        for (z in zombies) {
            if (z.elevation != elevation) continue
            if (hypot(z.x - nx, z.y - ny) <= effectiveZombieRadius) {
                z.alertTargetX = nx
                z.alertTargetY = ny
                z.alertTimer = if (isFrenzyActive) 12.0f else 6.0f
            }
        }
        if (fromPlayer && w.mapId == GameMapId.OAKRIDGE_TOWN) {
            val now = System.currentTimeMillis()
            for (r in rivalOperators) {
                if (r.elevation != elevation) continue
                val d = hypot(r.x - nx, r.y - ny)
                if (d <= radius) {
                    r.lastKnownThreatX = nx
                    r.lastKnownThreatY = ny
                    if (loudGunshot) {
                        // Loud shot triggers camp/squad fight or cover maneuver
                        if (r.aiState == ScavAiState.PATROL || r.aiState == ScavAiState.INVESTIGATE) {
                            enterCombatOrCover(w, r, nx, ny, now, callout = "GUNFIRE! TAKE COVER!")
                        }
                    } else if (r.aiState == ScavAiState.PATROL) {
                        // Suppressed/melee faint snap inside short earshot -> investigate instead of instant full agro
                        r.aiState = ScavAiState.INVESTIGATE
                        r.investigateTimer = 10f
                        r.calloutText = "? Check that sound..."
                        r.calloutUntilMs = now + 2200L
                    }
                }
            }
        }
    }

    private fun updateZombies(w: WorldMapData, dt: Float) {
        val frenzyNow = isFrenzyActive
        for (z in zombies) {
            if (z.elevation != elevation) continue
            val distToPlayer = hypot(playerX - z.x, playerY - z.y)

            val baseSight = when (z.variant) {
                ZombieVariant.SPRINTER -> 150f
                ZombieVariant.ARMORED -> 125f
                ZombieVariant.WALKER -> 132f
            }
            val sightRange = if (frenzyNow || z.isFrenzy) baseSight * 1.65f else baseSight
            val closeAwareness = if (frenzyNow || z.isFrenzy) 88f else 48f
            val seesPlayer = distToPlayer <= closeAwareness ||
                (distToPlayer <= sightRange && hasLineOfSight(w, z.x, z.y, playerX, playerY, elevation))

            // Infected can also lock onto closer fighting Scavs (3-way chaos!)
            var targetScav: RivalOperatorEntity? = null
            if (!seesPlayer || distToPlayer > 90f) {
                var bestScavDist = if (seesPlayer) distToPlayer * 0.72f else 155f
                for (r in rivalOperators) {
                    if (r.elevation != elevation) continue
                    val ds = hypot(r.x - z.x, r.y - z.y)
                    if (ds < bestScavDist && hasLineOfSight(w, z.x, z.y, r.x, r.y, elevation)) {
                        bestScavDist = ds
                        targetScav = r
                    }
                }
            }

            val tx: Float
            val ty: Float
            val chase: Boolean

            if (targetScav != null) {
                tx = targetScav.x
                ty = targetScav.y
                chase = true
            } else if (seesPlayer) {
                tx = playerX
                ty = playerY
                z.alertTargetX = playerX
                z.alertTargetY = playerY
                z.alertTimer = if (frenzyNow) 8.5f else 4.2f
                chase = true
            } else if (z.alertTimer > 0f && z.alertTargetX != null && z.alertTargetY != null) {
                tx = z.alertTargetX!!
                ty = z.alertTargetY!!
                z.alertTimer -= dt
                chase = hypot(tx - z.x, ty - z.y) > 20f
            } else if (frenzyNow && z.isFrenzy) {
                // Frenzied horde relentlessly pushes toward player sector during blackout storm
                tx = playerX
                ty = playerY
                chase = distToPlayer > 20f
            } else {
                tx = z.x
                ty = z.y
                chase = false
            }

            if (chase) {
                z.angle = atan2(ty - z.y, tx - z.x)
                val frenzyBoost = if (frenzyNow || z.isFrenzy) 1.34f else 1f
                val step = z.speed * frenzyBoost * (dt * 60f)
                val vx = cos(z.angle) * step
                val vy = sin(z.angle) * step
                if (canEntityMoveTo(w, z.x + vx, z.y, z.radius, z.elevation, false, 0L)) z.x += vx
                if (canEntityMoveTo(w, z.x, z.y + vy, z.radius, z.elevation, false, 0L)) z.y += vy

                if (targetScav != null && hypot(targetScav.x - z.x, targetScav.y - z.y) < 23f) {
                    val baseDps = when (z.variant) {
                        ZombieVariant.ARMORED -> 24f
                        ZombieVariant.SPRINTER -> 19f
                        ZombieVariant.WALKER -> 15f
                    }
                    targetScav.hp -= baseDps * dt
                    targetScav.hitFlashUntilMs = System.currentTimeMillis() + 80L
                    onScavDamaged(w, targetScav, z.x, z.y, fromPlayer = false, now = System.currentTimeMillis())
                } else if (distToPlayer < 23f) {
                    val baseDps = when (z.variant) {
                        ZombieVariant.ARMORED -> 26f
                        ZombieVariant.SPRINTER -> 21f
                        ZombieVariant.WALKER -> 17f
                    }
                    val rawDmg = baseDps * dt * (if (frenzyNow) 1.12f else 1f)
                    if (armorHp > 0f) {
                        val mitigated = rawDmg * (1f - armorTier.mitigation * 0.5f)
                        val absorbed = min(armorHp, mitigated * 0.72f)
                        armorHp = max(0f, armorHp - absorbed)
                        hp = max(0f, hp - max(0f, mitigated - absorbed))
                    } else {
                        hp = max(0f, hp - rawDmg)
                    }
                    if (hp <= 0f) {
                        onOperatorDowned?.invoke()
                        return
                    }
                }
            }
        }
        // Clean up any scavs killed by zombies
        val now = System.currentTimeMillis()
        val scavIter = rivalOperators.listIterator()
        while (scavIter.hasNext()) {
            val r = scavIter.next()
            if (r.hp <= 0f) {
                scavIter.remove()
                handleScavKilled(w, r, byPlayer = false, weaponUsed = r.weapon, now = now)
            }
        }
    }

    private fun updateRivalOperators(w: WorldMapData, dt: Float, now: Long) {
        if (w.mapId != GameMapId.OAKRIDGE_TOWN) return
        val phase = DayNightCycle.phaseForHour(worldTimeHours)
        val inBlackout = now < blackoutUntilMs
        val baseVisualRange = when {
            inBlackout -> 128f
            phase.isNight -> 172f
            else -> 245f
        }

        for (r in rivalOperators) {
            if (r.elevation != elevation) continue

            // 1. Check nearby undiscovered fallen squadmate bodies
            if (r.aiState == ScavAiState.PATROL) {
                val foundBody = deadBodyMarkers.firstOrNull { m ->
                    !m.discoveredByCamp &&
                        m.elevation == r.elevation &&
                        hypot(m.x - r.x, m.y - r.y) <= 165f &&
                        hasLineOfSight(w, r.x, r.y, m.x, m.y, r.elevation)
                }
                if (foundBody != null) {
                    foundBody.discoveredByCamp = true
                    r.aiState = ScavAiState.INVESTIGATE
                    r.lastKnownThreatX = foundBody.x
                    r.lastKnownThreatY = foundBody.y
                    r.investigateTimer = 15f
                    r.calloutText = "${foundBody.displayName} is down! Sweeping!"
                    r.calloutUntilMs = now + 3200L
                    showRadioCallout("SCAV COMMS // ${r.displayName}: Found ${foundBody.displayName} down — sweep perimeter!")
                    for (ally in rivalOperators) {
                        if (ally.id != r.id && ally.campId == r.campId && ally.aiState == ScavAiState.PATROL) {
                            ally.aiState = ScavAiState.INVESTIGATE
                            ally.lastKnownThreatX = foundBody.x
                            ally.lastKnownThreatY = foundBody.y
                            ally.investigateTimer = 14f
                        }
                    }
                }
            }

            // 2. Player line-of-sight + directional stealth cone check
            val distToPlayer = hypot(playerX - r.x, playerY - r.y)
            val effectiveSight = baseVisualRange +
                (if (r.aiState == ScavAiState.ENGAGE || r.aiState == ScavAiState.TAKE_COVER) 55f else 0f) +
                (if (r.isVengefulKin) 40f else 0f)
            val angToPlayer = atan2(playerY - r.y, playerX - r.x)
            val angleDiff = abs(atan2(sin(angToPlayer - r.angle), cos(angToPlayer - r.angle)))
            val inSightCone = distToPlayer <= 42f ||
                angleDiff <= 1.08f ||
                r.aiState == ScavAiState.ENGAGE ||
                r.aiState == ScavAiState.TAKE_COVER ||
                r.aiState == ScavAiState.RETREAT
            val seesPlayer = distToPlayer <= effectiveSight &&
                inSightCone &&
                hasLineOfSight(w, r.x, r.y, playerX, playerY, r.elevation)

            // 3. Nearest aggressive Zombie threatening this Scav
            var nearestZombie: ZombieEntity? = null
            var nearestZDist = 195f
            for (z in zombies) {
                if (z.elevation != r.elevation) continue
                val dz = hypot(z.x - r.x, z.y - r.y)
                if (dz < nearestZDist && hasLineOfSight(w, r.x, r.y, z.x, z.y, r.elevation)) {
                    nearestZDist = dz
                    nearestZombie = z
                }
            }

            // 4. Kinship confrontation & Witness-triggered escalation
            if (seesPlayer) {
                r.lastKnownThreatX = playerX
                r.lastKnownThreatY = playerY
                val playerRecentlyShot = (now - lastPlayerShotMs) < 1600L

                if (r.isVengefulKin && r.aiState != ScavAiState.ENGAGE && r.aiState != ScavAiState.TAKE_COVER && r.aiState != ScavAiState.RETREAT) {
                    if (r.kinSawKiller) {
                        val relMsg = "${activeOperator?.callsign ?: "YOU"}! You killed my ${r.kinRelationship} ${r.kinInquiryTargetName}! Drop 'em!"
                        showRadioCallout("${r.displayName} (${r.campName}): \"$relMsg\"")
                        alertCampAndNearbyScavs(w, r.campId, playerX, playerY, r.elevation, relMsg, now)
                    } else if (distToPlayer < 120f || playerRecentlyShot) {
                        val relMsg = "Wait... you were in ${r.kinDeathDistrict}! That was my ${r.kinRelationship} ${r.kinInquiryTargetName} — KILLER!"
                        showRadioCallout("${r.displayName}: \"$relMsg\"")
                        alertCampAndNearbyScavs(w, r.campId, playerX, playerY, r.elevation, relMsg, now)
                    } else if (now > r.calloutUntilMs) {
                        r.aiState = ScavAiState.INVESTIGATE
                        r.investigateTimer = 14f
                        r.calloutText = "Seen my ${r.kinRelationship} ${r.kinInquiryTargetName}?"
                        r.calloutUntilMs = now + 3400L
                        showRadioCallout("${r.displayName} (${r.campName}): \"Nobody's heard from my ${r.kinRelationship} ${r.kinInquiryTargetName} in ${r.kinDeathDistrict}...\"")
                    }
                } else if (r.aiState == ScavAiState.PATROL || r.aiState == ScavAiState.INVESTIGATE) {
                    // Witness spots player firing or trespassing aggressively -> triggers squad fight!
                    if (playerRecentlyShot || distToPlayer <= 165f || r.aiState == ScavAiState.INVESTIGATE) {
                        alertCampAndNearbyScavs(
                            w = w,
                            campId = r.campId,
                            threatX = playerX,
                            threatY = playerY,
                            elev = r.elevation,
                            callout = "HOSTILE SPOTTED! TAKE COVER!",
                            now = now
                        )
                    }
                }
            }

            // 5. Low-HP Retreat transition
            if (r.hp < r.maxHp * 0.26f &&
                (r.aiState == ScavAiState.ENGAGE || r.aiState == ScavAiState.TAKE_COVER)
            ) {
                r.aiState = ScavAiState.RETREAT
                r.calloutText = "Pinned down—falling back!"
                r.calloutUntilMs = now + 2200L
            }

            // 6. Pick primary combat target (closest lethal threat: player vs zombie)
            val fightPlayer = (r.aiState == ScavAiState.ENGAGE || r.aiState == ScavAiState.TAKE_COVER || r.aiState == ScavAiState.RETREAT) && seesPlayer
            val targetZom = if (nearestZombie != null && (!fightPlayer || nearestZDist < distToPlayer * 0.85f || nearestZDist < 65f)) {
                nearestZombie
            } else null

            val threatX = targetZom?.x ?: r.lastKnownThreatX ?: playerX
            val threatY = targetZom?.y ?: r.lastKnownThreatY ?: playerY

            when (r.aiState) {
                ScavAiState.TAKE_COVER -> {
                    val cx = r.coverX
                    val cy = r.coverY
                    if (cx == null || cy == null) {
                        val found = findTacticalCoverPosition(w, r, threatX, threatY)
                        if (found != null) {
                            r.coverX = found.first
                            r.coverY = found.second
                            r.coverTimer = 1.6f + rng.nextFloat() * 0.9f
                        } else {
                            r.aiState = ScavAiState.ENGAGE
                        }
                    } else {
                        val dCover = hypot(cx - r.x, cy - r.y)
                        if (dCover > 13f) {
                            r.inCover = false
                            moveScavToward(w, r, cx, cy, speed = 2.48f, dt = dt)
                        } else {
                            r.inCover = true
                            r.angle = atan2(threatY - r.y, threatX - r.x)
                            r.coverTimer -= dt
                            // If enemy flanked into clear sight and cover timer ran low, peek/re-engage or shoot zombie rush
                            if (targetZom != null && nearestZDist < 95f) {
                                fireScavBurst(w, r, targetZom.x, targetZom.y, now)
                            }
                            if (r.coverTimer <= 0f) {
                                r.inCover = false
                                r.aiState = ScavAiState.ENGAGE
                                r.burstShotsLeft = 2 + (r.dangerTier.coerceIn(1, 4) / 2)
                            }
                        }
                    }
                }

                ScavAiState.ENGAGE -> {
                    r.inCover = false
                    val tx = targetZom?.x ?: if (seesPlayer) playerX else r.lastKnownThreatX
                    val ty = targetZom?.y ?: if (seesPlayer) playerY else r.lastKnownThreatY

                    if (tx != null && ty != null) {
                        val dThreat = hypot(tx - r.x, ty - r.y)
                        r.angle = atan2(ty - r.y, tx - r.x)
                        val canHit = targetZom != null || (seesPlayer && dThreat <= 275f)
                        if (canHit) {
                            if (r.burstShotsLeft <= 0 && now >= r.burstPauseUntilMs) {
                                // Duck back into nearby tactical cover after finishing a burst!
                                val nextCover = findTacticalCoverPosition(w, r, tx, ty)
                                if (nextCover != null) {
                                    r.coverX = nextCover.first
                                    r.coverY = nextCover.second
                                    r.coverTimer = 1.35f + rng.nextFloat() * 0.8f
                                    r.aiState = ScavAiState.TAKE_COVER
                                    r.calloutText = "Moving to cover!"
                                    r.calloutUntilMs = now + 1500L
                                } else {
                                    r.burstShotsLeft = 2
                                    r.burstPauseUntilMs = now + 650L
                                }
                            } else {
                                // Keep smart tactical engagement band: strafe or advance into ideal range
                                when {
                                    dThreat < 95f -> {
                                        val backX = r.x - cos(r.angle) * 60f
                                        val backY = r.y - sin(r.angle) * 60f
                                        moveScavToward(w, r, backX, backY, speed = 1.95f, dt = dt, faceTarget = false)
                                    }
                                    dThreat > 195f -> {
                                        moveScavToward(w, r, tx, ty, speed = 2.15f, dt = dt, faceTarget = false)
                                    }
                                    else -> {
                                        val strafeSign = if ((r.id.hashCode() and 1) == 0) 1f else -1f
                                        val strafeAng = r.angle + strafeSign * (PI.toFloat() * 0.5f)
                                        moveScavToward(
                                            w, r,
                                            r.x + cos(strafeAng) * 40f,
                                            r.y + sin(strafeAng) * 40f,
                                            speed = 1.45f, dt = dt, faceTarget = false
                                        )
                                    }
                                }
                                fireScavBurst(w, r, tx, ty, now)
                            }
                        } else {
                            // Lost LOS: advance toward last known threat to flank
                            moveScavToward(w, r, tx, ty, speed = 2.05f, dt = dt)
                            if (hypot(tx - r.x, ty - r.y) < 22f) {
                                r.aiState = ScavAiState.INVESTIGATE
                                r.investigateTimer = 8f
                            }
                        }
                    } else {
                        r.aiState = ScavAiState.INVESTIGATE
                        r.investigateTimer = 7f
                    }
                }

                ScavAiState.RETREAT -> {
                    r.inCover = false
                    val retreatCover = findTacticalCoverPosition(w, r, threatX, threatY, preferFarther = true)
                    if (retreatCover != null) {
                        moveScavToward(w, r, retreatCover.first, retreatCover.second, speed = 2.55f, dt = dt, faceTarget = false)
                        r.angle = atan2(threatY - r.y, threatX - r.x)
                        if (seesPlayer && now - r.lastShotMs >= 1100L) {
                            fireScavBurst(w, r, playerX, playerY, now)
                        }
                        if (hypot(retreatCover.first - r.x, retreatCover.second - r.y) < 16f) {
                            r.coverX = retreatCover.first
                            r.coverY = retreatCover.second
                            r.coverTimer = 2.2f
                            r.aiState = ScavAiState.TAKE_COVER
                        }
                    } else {
                        val awayAng = atan2(r.y - threatY, r.x - threatX)
                        moveScavToward(w, r, r.x + cos(awayAng) * 140f, r.y + sin(awayAng) * 140f, speed = 2.35f, dt = dt)
                    }
                }

                ScavAiState.INVESTIGATE -> {
                    r.inCover = false
                    if (targetZom != null) {
                        r.angle = atan2(targetZom.y - r.y, targetZom.x - r.x)
                        fireScavBurst(w, r, targetZom.x, targetZom.y, now)
                    }
                    val ix = r.lastKnownThreatX
                    val iy = r.lastKnownThreatY
                    r.investigateTimer -= dt
                    if (ix != null && iy != null && hypot(ix - r.x, iy - r.y) > 22f) {
                        moveScavToward(w, r, ix, iy, speed = 1.72f, dt = dt)
                    } else {
                        // Scan side-to-side around patrol perimeter
                        r.angle += sin((now % 3200L) / 3200f * 2f * PI.toFloat()) * dt * 1.4f
                    }
                    if (r.investigateTimer <= 0f) {
                        r.lastKnownThreatX = null
                        r.lastKnownThreatY = null
                        if (!r.isVengefulKin) {
                            r.aiState = ScavAiState.PATROL
                        } else {
                            // Vengeful kin keep sweeping home perimeter
                            r.investigateTimer = 24f
                            r.patrolTargetX = r.homeX + (rng.nextFloat() - 0.5f) * 170f
                            r.patrolTargetY = r.homeY + (rng.nextFloat() - 0.5f) * 170f
                            r.lastKnownThreatX = r.patrolTargetX
                            r.lastKnownThreatY = r.patrolTargetY
                        }
                    }
                }

                ScavAiState.PATROL -> {
                    r.inCover = false
                    if (targetZom != null) {
                        r.angle = atan2(targetZom.y - r.y, targetZom.x - r.x)
                        fireScavBurst(w, r, targetZom.x, targetZom.y, now)
                    } else {
                        val homeDist = hypot(r.homeX - r.x, r.homeY - r.y)
                        val pDist = hypot(r.patrolTargetX - r.x, r.patrolTargetY - r.y)
                        if (homeDist > 210f) {
                            r.patrolTargetX = r.homeX + (rng.nextFloat() - 0.5f) * 90f
                            r.patrolTargetY = r.homeY + (rng.nextFloat() - 0.5f) * 90f
                            moveScavToward(w, r, r.patrolTargetX, r.patrolTargetY, speed = 1.48f, dt = dt)
                        } else if (pDist < 16f) {
                            r.patrolTargetX = r.homeX + (rng.nextFloat() - 0.5f) * 150f
                            r.patrolTargetY = r.homeY + (rng.nextFloat() - 0.5f) * 150f
                        } else {
                            moveScavToward(w, r, r.patrolTargetX, r.patrolTargetY, speed = 1.38f, dt = dt)
                        }
                    }
                }
            }
        }
    }

    private fun moveScavToward(
        w: WorldMapData,
        r: RivalOperatorEntity,
        tx: Float,
        ty: Float,
        speed: Float,
        dt: Float,
        faceTarget: Boolean = true
    ) {
        val ang = atan2(ty - r.y, tx - r.x)
        if (faceTarget) r.angle = ang
        val step = speed * (dt * 60f)
        val vx = cos(ang) * step
        val vy = sin(ang) * step
        var moved = false
        if (canEntityMoveTo(w, r.x + vx, r.y, r.radius, r.elevation, false, 0L)) {
            r.x += vx
            moved = true
        }
        if (canEntityMoveTo(w, r.x, r.y + vy, r.radius, r.elevation, false, 0L)) {
            r.y += vy
            moved = true
        }
        if (!moved) {
            // Slide around obstacle corners cleanly
            for (offset in listOf(PI.toFloat() * 0.45f, -PI.toFloat() * 0.45f)) {
                val sa = ang + offset
                val sx = cos(sa) * step
                val sy = sin(sa) * step
                if (canEntityMoveTo(w, r.x + sx, r.y + sy, r.radius, r.elevation, false, 0L)) {
                    r.x += sx
                    r.y += sy
                    moved = true
                    break
                }
            }
        }
        if (moved) r.stepTimer += dt * (speed * 4.2f)
    }

    private fun fireScavBurst(
        w: WorldMapData,
        r: RivalOperatorEntity,
        tx: Float,
        ty: Float,
        now: Long
    ) {
        val fireIntervalMs = (r.weapon.fireDelayMs * 2L).coerceIn(380L, 1450L)
        if (now - r.lastShotMs < fireIntervalMs) return
        r.lastShotMs = now
        if (r.burstShotsLeft > 0) r.burstShotsLeft--
        if (r.burstShotsLeft == 0) r.burstPauseUntilMs = now + 700L

        r.angle = atan2(ty - r.y, tx - r.x)
        audio.playWeapon(r.weapon, isSuppressedOverride = r.weapon.suppressed)
        if (!r.weapon.suppressed) {
            // Loud Scav gunfire draws nearby zombies toward the firefight!
            emitNoise(260f, r.x, r.y, fromPlayer = false, loudGunshot = false)
        }
        val mx = r.x + cos(r.angle) * r.weapon.gunLength
        val my = r.y + sin(r.angle) * r.weapon.gunLength
        muzzleFlashes.add(ParticleFx(mx, my, 0f, 0f, 0.08f, 0.08f, 8f, r.weapon.tracerColorHex))
        val spread = r.angle + (rng.nextFloat() - 0.5f) * 0.16f
        bullets.add(
            BulletEntity(
                x = mx,
                y = my,
                vx = cos(spread) * (r.weapon.bulletSpeed * 0.72f),
                vy = sin(spread) * (r.weapon.bulletSpeed * 0.72f),
                rangeLeft = 290f,
                damage = (r.weapon.damage * (0.28f + r.dangerTier * 0.04f)).toInt().coerceIn(7, 24),
                elevation = r.elevation,
                colorHex = 0xFFEF4444,
                fromRival = true,
                shooterCallsign = r.displayName,
                weaponUsed = r.weapon
            )
        )
    }

    private fun findTacticalCoverPosition(
        w: WorldMapData,
        r: RivalOperatorEntity,
        threatX: Float,
        threatY: Float,
        preferFarther: Boolean = false
    ): Pair<Float, Float>? {
        val candidates = mutableListOf<Pair<Float, Float>>()
        val awayAng = atan2(r.y - threatY, r.x - threatX)

        // Check nearby sandbags, vehicles, crates & wall corners within 280px
        for (p in w.props) {
            if (p.floor != r.elevation || p.cleared) continue
            if (p.propType !in listOf("sandbag", "vehicle", "storage_crate_extra", "storage_crate_heavy", "tree", "jersey_barrier")) continue
            val pcx = p.x + p.w * 0.5f
            val pcy = p.y + p.h * 0.5f
            if (hypot(pcx - r.x, pcy - r.y) > 270f) continue
            // Opposite side of prop from threat
            val ux = pcx - threatX
            val uy = pcy - threatY
            val uLen = hypot(ux, uy).coerceAtLeast(1f)
            val off = max(p.w, p.h) * 0.5f + r.radius + 10f
            candidates.add((pcx + (ux / uLen) * off) to (pcy + (uy / uLen) * off))
        }
        for (wall in w.walls) {
            if (wall.floor != r.elevation || wall.isFence) continue
            val wcx = wall.x + wall.w * 0.5f
            val wcy = wall.y + wall.h * 0.5f
            if (hypot(wcx - r.x, wcy - r.y) > 260f) continue
            val ux = wcx - threatX
            val uy = wcy - threatY
            val uLen = hypot(ux, uy).coerceAtLeast(1f)
            candidates.add((wcx + (ux / uLen) * 26f) to (wcy + (uy / uLen) * 26f))
        }
        // Fallback tactical lateral/retreat ring samples around scav
        for (deg in listOf(-60f, -30f, 0f, 30f, 60f, 110f, -110f)) {
            val a = awayAng + deg * PI.toFloat() / 180f
            candidates.add((r.x + cos(a) * 95f) to (r.y + sin(a) * 95f))
        }

        var bestPoint: Pair<Float, Float>? = null
        var bestScore = Float.NEGATIVE_INFINITY
        for ((cx, cy) in candidates) {
            if (!canEntityMoveTo(w, cx, cy, r.radius, r.elevation, false, 0L)) continue
            if (!canTraverseTerrain(w, r.x, r.y, cx, cy, r.elevation)) continue
            val distFromSelf = hypot(cx - r.x, cy - r.y)
            val distFromThreat = hypot(cx - threatX, cy - threatY)
            if (distFromThreat < 75f) continue
            val breaksLos = !hasLineOfSight(w, cx, cy, threatX, threatY, r.elevation)
            val propShielded = !breaksLos && w.props.any { p ->
                p.floor == r.elevation && !p.cleared &&
                    segmentIntersectsRect(cx, cy, threatX, threatY, p.x, p.y, p.w, p.h)
            }
            if (!breaksLos && !propShielded && !preferFarther) continue

            var score = if (breaksLos) 140f else if (propShielded) 95f else 20f
            score -= distFromSelf * 0.35f
            score += if (preferFarther) distFromThreat * 0.45f else (220f - abs(distFromThreat - 165f)) * 0.25f
            if (score > bestScore) {
                bestScore = score
                bestPoint = cx to cy
            }
        }
        return bestPoint
    }

    private fun onScavDamaged(
        w: WorldMapData,
        r: RivalOperatorEntity,
        attackerX: Float,
        attackerY: Float,
        fromPlayer: Boolean,
        now: Long
    ) {
        r.lastKnownThreatX = attackerX
        r.lastKnownThreatY = attackerY
        if (fromPlayer) {
            val cover = findTacticalCoverPosition(w, r, attackerX, attackerY)
            if (r.hp < r.maxHp * 0.26f) {
                r.aiState = ScavAiState.RETREAT
            } else if (cover != null) {
                r.coverX = cover.first
                r.coverY = cover.second
                r.coverTimer = 1.4f + rng.nextFloat() * 0.7f
                r.aiState = ScavAiState.TAKE_COVER
                r.calloutText = "Taking fire — get to cover!"
                r.calloutUntilMs = now + 2200L
            } else {
                r.aiState = ScavAiState.ENGAGE
                r.burstShotsLeft = 3
            }
            // If player shot was loud OR nearby ally sees scav hit -> alert squadmates
            val shotWasLoud = (now - lastPlayerLoudShotMs) < 900L
            if (shotWasLoud) {
                alertCampAndNearbyScavs(w, r.campId, attackerX, attackerY, r.elevation, "${r.displayName} is hit! Engage!", now)
            }
        }
    }

    private fun enterCombatOrCover(
        w: WorldMapData,
        r: RivalOperatorEntity,
        threatX: Float,
        threatY: Float,
        now: Long,
        callout: String
    ) {
        r.lastKnownThreatX = threatX
        r.lastKnownThreatY = threatY
        r.alertLevel = 1f
        val cov = findTacticalCoverPosition(w, r, threatX, threatY)
        if (cov != null) {
            r.coverX = cov.first
            r.coverY = cov.second
            r.coverTimer = 1.35f + rng.nextFloat() * 0.75f
            r.aiState = ScavAiState.TAKE_COVER
        } else {
            r.aiState = ScavAiState.ENGAGE
            r.burstShotsLeft = 3
        }
        if (r.calloutText.isBlank() || now > r.calloutUntilMs) {
            r.calloutText = callout
            r.calloutUntilMs = now + 2600L
        }
        alertCampAndNearbyScavs(
            w = w,
            campId = r.campId,
            threatX = threatX,
            threatY = threatY,
            elev = r.elevation,
            callout = callout,
            now = now
        )
    }

    private fun alertCampAndNearbyScavs(
        w: WorldMapData,
        campId: String,
        threatX: Float,
        threatY: Float,
        elev: Int,
        callout: String,
        now: Long
    ) {
        for ((idx, ally) in rivalOperators.withIndex()) {
            if (ally.elevation != elev) continue
            val sameCamp = campId.isNotBlank() && ally.campId == campId
            val nearby = hypot(ally.x - threatX, ally.y - threatY) <= 340f
            if (!sameCamp && !nearby) continue
            ally.lastKnownThreatX = threatX
            ally.lastKnownThreatY = threatY
            ally.alertLevel = 1f
            if (ally.aiState == ScavAiState.PATROL || ally.aiState == ScavAiState.INVESTIGATE) {
                val takeCover = idx % 2 == 1
                if (takeCover) {
                    val cov = findTacticalCoverPosition(w, ally, threatX, threatY)
                    if (cov != null) {
                        ally.coverX = cov.first
                        ally.coverY = cov.second
                        ally.coverTimer = 1.4f + rng.nextFloat() * 0.8f
                        ally.aiState = ScavAiState.TAKE_COVER
                    } else {
                        ally.aiState = ScavAiState.ENGAGE
                        ally.burstShotsLeft = 3
                    }
                } else {
                    ally.aiState = ScavAiState.ENGAGE
                    ally.burstShotsLeft = 3
                }
                if (ally.calloutText.isBlank() || now > ally.calloutUntilMs) {
                    ally.calloutText = callout
                    ally.calloutUntilMs = now + 2600L
                }
            }
        }
    }

    private fun hasWitnessToScavKill(w: WorldMapData, victim: RivalOperatorEntity, now: Long): Boolean {
        val playerUsedLoudFirearm = (now - lastPlayerLoudShotMs) < 900L
        val weaponNoise = effectiveStatsFor(currentWeapon).noiseRadius
        for (other in rivalOperators) {
            if (other.id == victim.id || other.elevation != victim.elevation) continue
            val dToVictim = hypot(other.x - victim.x, other.y - victim.y)
            val dToPlayer = hypot(other.x - playerX, other.y - playerY)
            // Already in open combat with this camp -> heard/noticed
            if (other.aiState == ScavAiState.ENGAGE || other.aiState == ScavAiState.TAKE_COVER) return true
            // Heard unsuppressed gunshot
            if (playerUsedLoudFirearm && dToPlayer <= max(weaponNoise, 280f)) return true
            // Visual LOS onto either victim dropping or killer striking
            if (dToVictim <= 300f && hasLineOfSight(w, other.x, other.y, victim.x, victim.y, victim.elevation)) {
                val ang = atan2(victim.y - other.y, victim.x - other.x)
                val diff = abs(atan2(sin(ang - other.angle), cos(ang - other.angle)))
                if (diff <= 1.25f || dToVictim <= 75f) return true
            }
            if (dToPlayer <= 280f && hasLineOfSight(w, other.x, other.y, playerX, playerY, victim.elevation)) {
                val ang = atan2(playerY - other.y, playerX - other.x)
                val diff = abs(atan2(sin(ang - other.angle), cos(ang - other.angle)))
                if (diff <= 1.15f || dToPlayer <= 65f) return true
            }
        }
        return false
    }

    private fun handleScavKilled(
        w: WorldMapData,
        victim: RivalOperatorEntity,
        byPlayer: Boolean,
        weaponUsed: WeaponId,
        now: Long
    ) {
        deadBodyMarkers.add(
            DeadBodyMarker(
                id = "body_${victim.scavId}_$now",
                scavId = victim.scavId,
                displayName = victim.displayName,
                campId = victim.campId,
                districtName = lastZoneName,
                x = victim.x,
                y = victim.y,
                elevation = victim.elevation,
                diedAtMs = now,
                killedByPlayer = byPlayer
            )
        )
        // Drop scav tactical pouch with crafting materials & ammo
        val dropMats = listOf(
            CraftingMaterial.GUNPOWDER,
            CraftingMaterial.BRASS_CASINGS,
            CraftingMaterial.MEDICAL_HERBS,
            CraftingMaterial.KEVLAR_WEAVE,
            CraftingMaterial.FORGED_RECEIVER
        )
        val picked = dropMats[rng.nextInt(dropMats.size)]
        dynamicLootDrops.add(
            LootContainer(
                id = "scav_bag_${victim.id}_$now",
                x = victim.x + 8f,
                y = victim.y + 4f,
                floor = victim.elevation,
                label = "${victim.displayName}'s Pack",
                items = mutableListOf(
                    InventoryItem(
                        uid = "scav_drop_${victim.id}_${now}_1",
                        type = LootType.MATERIAL,
                        name = "${picked.displayName} x2",
                        detail = "Salvaged from ${victim.displayName}",
                        value = 2,
                        materialKey = picked.key,
                        quantity = 2
                    ),
                    InventoryItem(
                        uid = "scav_drop_${victim.id}_${now}_2",
                        type = LootType.MATERIAL,
                        name = "${CraftingMaterial.SCRAP_METAL.displayName} x3",
                        detail = victim.campName.ifBlank { "DeLaine Scav Kit" },
                        value = 3,
                        materialKey = CraftingMaterial.SCRAP_METAL.key,
                        quantity = 3
                    )
                )
            )
        )

        if (!byPlayer) return
        kills++
        val killerCallsign = activeOperator?.callsign ?: "OPERATOR"
        val witnessed = hasWitnessToScavKill(w, victim, now)
        if (witnessed) {
            alertCampAndNearbyScavs(
                w = w,
                campId = victim.campId,
                threatX = playerX,
                threatY = playerY,
                elev = victim.elevation,
                callout = "${victim.displayName} IS DOWN! OPEN FIRE!",
                now = now
            )
        } else {
            showToast("STEALTH ELIMINATION — ${victim.displayName.uppercase()} (NO WITNESSES)")
        }

        val record = SlainScavRecord(
            scavId = victim.scavId,
            displayName = victim.displayName,
            campId = victim.campId,
            campName = victim.campName,
            districtName = lastZoneName,
            killerCallsign = killerCallsign,
            weaponKey = weaponUsed.key,
            killedAtMs = now,
            witnessedBySquad = witnessed
        )
        activeSlainMemory = ScavKinshipMemoryStore.upsertRecord(activeSlainMemory, record)
        onNamedScavKilled?.invoke(record)
        onPlayerKilledPlayer?.invoke(killerCallsign, victim.callsign, weaponUsed, lastZoneName)
    }

    private fun updateBulletsAndParticles(w: WorldMapData, dt: Float) {
        val now = System.currentTimeMillis()
        val bIter = bullets.listIterator()
        while (bIter.hasNext()) {
            val b = bIter.next()
            b.x += b.vx
            b.y += b.vy
            b.rangeLeft -= hypot(b.vx, b.vy)

            var hitWall = false
            for (wall in w.walls) {
                if (wall.floor == b.elevation && !wall.isWindow && !wall.isFence &&
                    b.x in wall.x..(wall.x + wall.w) && b.y in wall.y..(wall.y + wall.h)
                ) {
                    hitWall = true
                    break
                }
            }

            var hitActor = false
            if (!hitWall && !b.fromRival) {
                val zIter = zombies.listIterator()
                while (zIter.hasNext()) {
                    val z = zIter.next()
                    if (z.elevation == b.elevation && hypot(b.x - z.x, b.y - z.y) < z.radius + 6f) {
                        z.hp -= b.damage
                        z.hitFlashUntilMs = now + 95L
                        z.alertTargetX = playerX
                        z.alertTargetY = playerY
                        z.alertTimer = 5f
                        hitActor = true
                        spawnHitSparks(b.x, b.y, 0xFFEF4444)
                        spawnDamagePopup(z.x, z.y - 24f, b.damage, critical = b.damage >= 55)
                        if (z.hp <= 0f) {
                            zIter.remove()
                            kills++
                            onZombieKilled?.invoke()
                        }
                        break
                    }
                }
                if (!hitActor) {
                    val rIter = rivalOperators.listIterator()
                    while (rIter.hasNext()) {
                        val r = rIter.next()
                        if (r.elevation == b.elevation && hypot(b.x - r.x, b.y - r.y) < r.radius + 6f) {
                            r.hp -= b.damage
                            r.hitFlashUntilMs = now + 95L
                            hitActor = true
                            spawnHitSparks(b.x, b.y, 0xFFEF4444)
                            spawnDamagePopup(r.x, r.y - 26f, b.damage, critical = b.damage >= 55)
                            onScavDamaged(w, r, playerX, playerY, fromPlayer = true, now = now)
                            if (r.hp <= 0f) {
                                rIter.remove()
                                val weap = b.weaponUsed ?: currentWeapon
                                handleScavKilled(w, r, byPlayer = true, weaponUsed = weap, now = now)
                            }
                            break
                        }
                    }
                }
            } else if (!hitWall && b.fromRival) {
                // Scav bullets can also hit infected rushing their perimeter (3-way firefight)
                val zIter = zombies.listIterator()
                while (zIter.hasNext()) {
                    val z = zIter.next()
                    if (z.elevation == b.elevation && hypot(b.x - z.x, b.y - z.y) < z.radius + 5f) {
                        z.hp -= b.damage
                        z.hitFlashUntilMs = now + 90L
                        hitActor = true
                        spawnHitSparks(b.x, b.y, 0xFFEF4444)
                        if (z.hp <= 0f) {
                            zIter.remove()
                        }
                        break
                    }
                }
                if (!hitActor && elevation == b.elevation && hypot(b.x - playerX, b.y - playerY) < playerRadius + 5f) {
                    hitActor = true
                    spawnHitSparks(b.x, b.y, 0xFFEF4444)
                    val rawDmg = b.damage.toFloat()
                    if (armorHp > 0f) {
                        val mitigated = rawDmg * (1f - armorTier.mitigation * 0.5f)
                        val absorbed = min(armorHp, mitigated * 0.72f)
                        armorHp = max(0f, armorHp - absorbed)
                        hp = max(0f, hp - max(0f, mitigated - absorbed))
                    } else {
                        hp = max(0f, hp - rawDmg)
                    }
                    if (hp <= 0f) {
                        val killer = b.shooterCallsign.ifBlank { "SCAV" }
                        val victim = activeOperator?.callsign ?: "OPERATOR"
                        val weap = b.weaponUsed ?: WeaponId.AK47
                        onPlayerKilledPlayer?.invoke(killer, victim, weap, lastZoneName)
                        onOperatorDowned?.invoke()
                    }
                }
            }

            if (hitWall) spawnHitSparks(b.x, b.y, 0xFFFBBF24)
            if (hitWall || hitActor || b.rangeLeft <= 0f) bIter.remove()
        }

        val pIter = particles.listIterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.rot += dt * 1.8f
            p.life -= dt
            if (p.life <= 0f) pIter.remove()
        }

        val mIter = muzzleFlashes.listIterator()
        while (mIter.hasNext()) {
            val m = mIter.next()
            m.life -= dt
            if (m.life <= 0f) mIter.remove()
        }

        val popIter = damagePopups.listIterator()
        while (popIter.hasNext()) {
            val pop = popIter.next()
            pop.y -= dt * 34f
            pop.life -= dt
            if (pop.life <= 0f) popIter.remove()
        }
    }

    private fun spawnDamagePopup(x: Float, y: Float, dmg: Int, critical: Boolean) {
        if (damagePopups.size > 24) damagePopups.removeAt(0)
        damagePopups.add(
            DamagePopup(
                x = x + (rng.nextFloat() - 0.5f) * 10f,
                y = y,
                amount = dmg,
                life = 0.65f,
                maxLife = 0.65f,
                isCrit = critical
            )
        )
    }

    private fun updateDogCompanion(w: WorldMapData, dt: Float, now: Long) {
        val op = activeOperator ?: return

        // In Oakridge, solo operator's K-9 detects close threats, barks to warn, and nips nearby zombies
        var targetZombie: ZombieEntity? = null
        if (w.mapId == GameMapId.OAKRIDGE_TOWN) {
            var bestDist = 155f
            for (z in zombies) {
                if (z.elevation != elevation) continue
                val d = hypot(z.x - playerX, z.y - playerY)
                if (d < bestDist && hasLineOfSight(w, dogX, dogY, z.x, z.y, elevation)) {
                    bestDist = d
                    targetZombie = z
                }
            }
        }

        if (targetZombie != null) {
            if (now >= dogAlertToastCooldownMs) {
                dogBarkUntilMs = now + 950L
                dogAlertToastCooldownMs = now + 9500L
                showToast("${op.dogBreed.uppercase()} BARKS — INFECTED NEARBY!")
            }
            val dzDist = hypot(targetZombie.x - dogX, targetZombie.y - dogY)
            dogAngle = atan2(targetZombie.y - dogY, targetZombie.x - dogX)
            if (dzDist > 20f) {
                val dStep = min(dzDist, 3.25f * (dt * 60f))
                val nx = dogX + cos(dogAngle) * dStep
                val ny = dogY + sin(dogAngle) * dStep
                if (canEntityMoveTo(w, nx, dogY, 8f, elevation, false, 0L)) dogX = nx
                if (canEntityMoveTo(w, dogX, ny, 8f, elevation, false, 0L)) dogY = ny
                dogStepTimer += dt * 10f
            }
            if (dzDist <= 30f && now >= dogAttackCooldownMs) {
                dogAttackCooldownMs = now + 1750L
                dogBarkUntilMs = now + 500L
                targetZombie.hp -= 16f
                spawnHitSparks(targetZombie.x, targetZombie.y, 0xFFEF4444)
                if (targetZombie.hp <= 0f) {
                    zombies.remove(targetZombie)
                    kills++
                    onZombieKilled?.invoke()
                }
            }
            return
        }

        // Default heel follow behind/beside active operator
        val goalX = playerX - cos(playerAngle) * 32f + sin(playerAngle) * 14f
        val goalY = playerY - sin(playerAngle) * 32f - cos(playerAngle) * 14f
        val dDist = hypot(goalX - dogX, goalY - dogY)
        if (dDist > 12f) {
            dogAngle = atan2(goalY - dogY, goalX - dogX)
            val dStep = min(dDist, 3.2f * (dt * 60f))
            val nx = dogX + cos(dogAngle) * dStep
            val ny = dogY + sin(dogAngle) * dStep
            if (canEntityMoveTo(w, nx, dogY, 8f, elevation, false, 0L)) dogX = nx
            if (canEntityMoveTo(w, dogX, ny, 8f, elevation, false, 0L)) dogY = ny
            dogStepTimer += dt * 9f
        }
    }

    private fun updateBaseNpcs(w: WorldMapData, dt: Float) {
        // Non-player operators wander around Outpost Zero to physically fetch water, food, rest, or patrol
        for (npc in baseNpcs) {
            npc.hunger = max(0f, npc.hunger - dt * 0.65f)
            npc.thirst = max(0f, npc.thirst - dt * 0.85f)
            npc.energy = max(0f, npc.energy - dt * 0.45f)
            npc.stateTimer -= dt

            if (npc.stateTimer <= 0f) {
                when {
                    npc.thirst < 38f -> {
                        val st = w.stations.firstOrNull { it.kind == StationKind.WATER_COOLER }
                        npc.state = NpcNeedState.GOING_TO_WATER
                        npc.targetX = (st?.x ?: 165f) + (rng.nextFloat() - 0.5f) * 20f
                        npc.targetY = (st?.y ?: -545f) + (rng.nextFloat() - 0.5f) * 20f
                        npc.stateTimer = 16f
                        npc.statusTag = if (baseWaterStock > 0) "GET WATER" else "NEED WATER"
                    }
                    npc.hunger < 40f -> {
                        val st = w.stations.firstOrNull { it.kind == StationKind.MESS_FOOD }
                        npc.state = NpcNeedState.GOING_TO_FOOD
                        npc.targetX = (st?.x ?: 62f) + (rng.nextFloat() - 0.5f) * 20f
                        npc.targetY = (st?.y ?: -552f) + (rng.nextFloat() - 0.5f) * 20f
                        npc.stateTimer = 16f
                        npc.statusTag = if (baseFoodStock > 0) "GET FOOD" else "NEED FOOD"
                    }
                    npc.energy < 34f -> {
                        val st = w.stations.firstOrNull { it.kind == StationKind.BUNK }
                        npc.state = NpcNeedState.GOING_TO_SLEEP
                        npc.targetX = (st?.x ?: -610f) + (rng.nextFloat() - 0.5f) * 24f
                        npc.targetY = (st?.y ?: -405f) + (rng.nextFloat() - 0.5f) * 24f
                        npc.stateTimer = 18f
                        npc.statusTag = "REST"
                    }
                    npc.state != NpcNeedState.FENCE_PATROL && rng.nextFloat() < 0.42f && w.patrolWaypoints.isNotEmpty() -> {
                        npc.state = NpcNeedState.FENCE_PATROL
                        val wp = w.patrolWaypoints[npc.patrolIdx % w.patrolWaypoints.size]
                        npc.targetX = wp.first
                        npc.targetY = wp.second
                        npc.patrolIdx++
                        npc.stateTimer = 15f
                        npc.statusTag = "PATROL"
                    }
                    rng.nextFloat() < 0.25f -> {
                        val st = w.stations.firstOrNull { it.kind == StationKind.WORKBENCH }
                        npc.state = NpcNeedState.CHECKING_WORKBENCH
                        npc.targetX = st?.x ?: -650f
                        npc.targetY = st?.y ?: 340f
                        npc.stateTimer = 10f
                        npc.statusTag = "ARMORY"
                    }
                    rng.nextFloat() < 0.25f -> {
                        val st = w.stations.firstOrNull { it.kind == StationKind.INFIRMARY_BED }
                        npc.state = NpcNeedState.VISITING_INFIRMARY
                        npc.targetX = st?.x ?: 550f
                        npc.targetY = st?.y ?: -385f
                        npc.stateTimer = 10f
                        npc.statusTag = "MED BAY"
                    }
                    rng.nextFloat() < 0.30f -> {
                        val st = w.stations.firstOrNull { it.kind == StationKind.CD_RACK }
                        npc.state = NpcNeedState.BROWSING_CDS
                        npc.targetX = st?.x ?: 72f
                        npc.targetY = st?.y ?: -76f
                        npc.stateTimer = 9f
                        npc.statusTag = "CD RACK"
                    }
                    else -> {
                        npc.state = NpcNeedState.ROAMING
                        npc.targetX = (rng.nextFloat() - 0.5f) * 760f
                        npc.targetY = (rng.nextFloat() - 0.5f) * 580f
                        npc.stateTimer = 9f
                        npc.statusTag = ""
                    }
                }
            }

            val dist = hypot(npc.targetX - npc.x, npc.targetY - npc.y)
            if (dist > 18f) {
                npc.angle = atan2(npc.targetY - npc.y, npc.targetX - npc.x)
                val step = 1.55f * (dt * 60f)
                val vx = cos(npc.angle) * step
                val vy = sin(npc.angle) * step
                val prevX = npc.x
                val prevY = npc.y
                if (canEntityMoveTo(w, npc.x + vx, npc.y, 11f, 0, false, 0L)) npc.x += vx
                if (canEntityMoveTo(w, npc.x, npc.y + vy, 11f, 0, false, 0L)) npc.y += vy
                if (hypot(npc.x - prevX, npc.y - prevY) < step * 0.18f) {
                    // Slide around interior building corners/doorframes; reroute if still wedged
                    val slideAngles = listOf(
                        npc.angle + PI.toFloat() * 0.48f,
                        npc.angle - PI.toFloat() * 0.48f,
                        npc.angle + PI.toFloat() * 0.72f,
                        npc.angle - PI.toFloat() * 0.72f
                    )
                    var slid = false
                    for (sa in slideAngles) {
                        val sx = npc.x + cos(sa) * step
                        val sy = npc.y + sin(sa) * step
                        if (canEntityMoveTo(w, sx, sy, 11f, 0, false, 0L)) {
                            npc.x = sx
                            npc.y = sy
                            slid = true
                            break
                        }
                    }
                    if (!slid) {
                        npc.stateTimer = min(npc.stateTimer, 0.25f)
                    }
                }
                npc.stepTimer += dt * 7f
            } else {
                when (npc.state) {
                    NpcNeedState.GOING_TO_WATER -> {
                        if (npc.thirst < 95f) {
                            if (baseWaterStock > 0) {
                                baseWaterStock--
                                npc.thirst = 98f
                                npc.statusTag = "DRANK"
                                onBaseNeedsConsume?.invoke(false, true)
                            } else {
                                npc.statusTag = "NO WATER!"
                                npc.stateTimer = min(npc.stateTimer, 2.5f)
                            }
                        }
                    }
                    NpcNeedState.GOING_TO_FOOD -> {
                        if (npc.hunger < 95f) {
                            if (baseFoodStock > 0) {
                                baseFoodStock--
                                npc.hunger = 98f
                                npc.statusTag = "ATE"
                                onBaseNeedsConsume?.invoke(true, false)
                            } else {
                                npc.statusTag = "NO FOOD!"
                                npc.stateTimer = min(npc.stateTimer, 2.5f)
                            }
                        }
                    }
                    NpcNeedState.GOING_TO_SLEEP -> {
                        npc.energy = min(100f, npc.energy + dt * 16f)
                        npc.statusTag = "RESTING"
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun updateAmbientWind(w: WorldMapData, dt: Float) {
        if (!ambientWindEnabled || elevation != 0) return
        ambientSpawnAccum += dt
        if (ambientSpawnAccum < 0.24f || particles.size > 48) return
        ambientSpawnAccum = 0f

        val zone = w.ambientZones.firstOrNull {
            playerX in it.bounds.x..(it.bounds.x + it.bounds.w) &&
                playerY in it.bounds.y..(it.bounds.y + it.bounds.h)
        } ?: return

        val sx = playerX + (rng.nextFloat() - 0.5f) * 760f
        val sy = playerY + (rng.nextFloat() - 0.5f) * 520f
        if (zone.kind == AmbientZoneKind.WOODS) {
            particles.add(
                ParticleFx(
                    x = sx,
                    y = sy,
                    vx = 28f + rng.nextFloat() * 24f,
                    vy = 16f + rng.nextFloat() * 14f,
                    life = 3.2f,
                    maxLife = 3.2f,
                    size = 4.5f,
                    colorHex = if (rng.nextBoolean()) 0xFF65A30D else 0xFFD97706,
                    isLeaf = true,
                    rot = rng.nextFloat() * 6.28f
                )
            )
        } else if (rng.nextFloat() < 0.55f) {
            particles.add(
                ParticleFx(
                    x = sx,
                    y = sy,
                    vx = 42f + rng.nextFloat() * 30f,
                    vy = -6f + rng.nextFloat() * 18f,
                    life = 3.6f,
                    maxLife = 3.6f,
                    size = 9f,
                    colorHex = 0xFFE5E7EB,
                    isPaper = true,
                    rot = rng.nextFloat() * 6.28f
                )
            )
        }
    }

    private fun spawnHitSparks(x: Float, y: Float, colorHex: Long) {
        repeat(4) {
            particles.add(
                ParticleFx(
                    x = x,
                    y = y,
                    vx = (rng.nextFloat() - 0.5f) * 140f,
                    vy = (rng.nextFloat() - 0.5f) * 140f,
                    life = 0.18f,
                    maxLife = 0.18f,
                    size = 3.5f,
                    colorHex = colorHex
                )
            )
        }
    }

    private fun computeTerrainHeight(w: WorldMapData, x: Float, y: Float): Int {
        for (ew in w.elevatedWalkways) {
            if (x in ew.x..(ew.x + ew.w) && y in ew.y..(ew.y + ew.h)) return ew.heightLevel
        }
        var bestHeight = 0
        var matched = false
        for (tz in w.terrainZones) {
            if (x in tz.x..(tz.x + tz.w) && y in tz.y..(tz.y + tz.h)) {
                if (!matched || tz.kind == TerrainKind.BASIN_WATER || abs(tz.heightLevel) > abs(bestHeight)) {
                    bestHeight = tz.heightLevel
                    matched = true
                }
            }
        }
        return bestHeight
    }

    private fun canTraverseTerrain(
        w: WorldMapData,
        fromX: Float,
        fromY: Float,
        toX: Float,
        toY: Float,
        elev: Int
    ): Boolean {
        if (elev != 0) return true
        val toWalkway = w.elevatedWalkways.firstOrNull { toX in it.x..(it.x + it.w) && toY in it.y..(it.y + it.h) }
        val fromWalkway = w.elevatedWalkways.firstOrNull { fromX in it.x..(it.x + it.w) && fromY in it.y..(it.y + it.h) }
        // Deep spring/river water blocks movement unless walking on a boardwalk or pier above it
        if (toWalkway == null) {
            val inWater = w.terrainZones.any {
                it.kind == TerrainKind.BASIN_WATER && toX in it.x..(it.x + it.w) && toY in it.y..(it.y + it.h)
            }
            if (inWater) return false
        }
        val toH = toWalkway?.heightLevel ?: computeTerrainHeight(w, toX, toY)
        val fromH = fromWalkway?.heightLevel ?: computeTerrainHeight(w, fromX, fromY)
        if (toH == fromH) return true

        // Allow smooth ramp/stairs traversal between connected height levels
        val onRamp = w.terrainRamps.any { r ->
            val fromIn = fromX in (r.x - 6f)..(r.x + r.w + 6f) && fromY in (r.y - 6f)..(r.y + r.h + 6f)
            val toIn = toX in (r.x - 6f)..(r.x + r.w + 6f) && toY in (r.y - 6f)..(r.y + r.h + 6f)
            (fromIn || toIn) &&
                ((fromH == r.fromHeight || fromH == r.toHeight || fromIn) &&
                    (toH == r.fromHeight || toH == r.toHeight || toIn))
        }
        if (onRamp) return true

        // Allow gentle natural sandy ridges (+1) from ground without trapping players on hill edges
        val isSandyRidge = w.terrainZones.any {
            it.kind == TerrainKind.SANDY_RIDGE &&
                ((toX in it.x..(it.x + it.w) && toY in it.y..(it.y + it.h)) ||
                    (fromX in it.x..(it.x + it.w) && fromY in it.y..(it.y + it.h)))
        }
        if (isSandyRidge && abs(toH - fromH) <= 1) return true
        return false
    }

    private fun canEntityMoveTo(
        w: WorldMapData,
        nx: Float,
        ny: Float,
        radius: Float,
        elev: Int,
        checkAlarms: Boolean,
        now: Long
    ): Boolean {
        w.worldBounds?.let { b ->
            if (nx - radius < b.x || nx + radius > b.x + b.w || ny - radius < b.y || ny + radius > b.y + b.h) {
                return false
            }
        }
        for (wall in w.walls) {
            if (wall.floor == elev && circleRectCollide(nx, ny, radius, wall.x, wall.y, wall.w, wall.h)) return false
        }
        for (d in w.doors) {
            if (d.floor == elev && !d.open && d.slideProgress < 0.65f) {
                val tipX = d.hingeX + cos(d.angle) * d.length
                val tipY = d.hingeY + sin(d.angle) * d.length
                if (circleSegmentCollide(nx, ny, radius + d.thickness * 0.4f, d.hingeX, d.hingeY, tipX, tipY)) {
                    return false
                }
            }
        }
        for (p in w.props) {
            if (p.floor == elev && !p.cleared && circleRectCollide(nx, ny, radius, p.x, p.y, p.w, p.h)) {
                if (checkAlarms && p.propType == "vehicle" && p.hasAlarm && now > alarmBumpCooldownMs) {
                    alarmBumpCooldownMs = now + 900L
                    triggerCarAlarm(p)
                }
                return false
            }
        }
        return true
    }

    private fun findContextPrompt(w: WorldMapData): ContextPrompt {
        // 1. Extraction zone check
        for (ez in w.extractionZones) {
            if (ez.isOpen && elevation == 0 && hypot(playerX - ez.x, playerY - ez.y) <= ez.radius) {
                return ContextPrompt(ContextPromptKind.EXTRACT, "EXTRACT: ${ez.name}", ez.id)
            }
        }
        // 2. Nearest interactive Station OR Talkable Survivor NPC in Outpost Zero
        if (elevation == 0) {
            var bestDist = 54f
            var bestPrompt: ContextPrompt? = null
            for (npc in w.survivorNpcs) {
                val d = hypot(playerX - npc.x, playerY - npc.y)
                if (d <= bestDist) {
                    bestDist = d
                    val name = SurvivorCatalog.npcById(npc.npcId).name.uppercase()
                    bestPrompt = ContextPrompt(ContextPromptKind.TALK_NPC, "TALK: $name", npc.npcId)
                }
            }
            for (st in w.stations) {
                val d = hypot(playerX - st.x, playerY - st.y)
                if (d <= bestDist) {
                    bestDist = d
                    bestPrompt = ContextPrompt(ContextPromptKind.STATION, st.label, st.id)
                }
            }
            if (bestPrompt != null) return bestPrompt
        }
        // 3. Clear fallen crate blocking a double door
        for (p in w.props) {
            if (p.isClearableBox && !p.cleared && p.floor == elevation) {
                val cx = p.x + p.w / 2f
                val cy = p.y + p.h / 2f
                if (hypot(playerX - cx, playerY - cy) <= 54f) {
                    return ContextPrompt(ContextPromptKind.CLEAR_CRATE, "MOVE FALLEN CRATE", p.id)
                }
            }
        }
        // 4. Nearest door
        for (d in w.doors) {
            if (d.floor == elevation) {
                val midX = d.hingeX + cos(d.baseAngle) * (d.length * 0.5f)
                val midY = d.hingeY + sin(d.baseAngle) * (d.length * 0.5f)
                if (hypot(playerX - midX, playerY - midY) <= 56f) {
                    val label = when {
                        d.jammedByPropId != null && w.props.any { it.id == d.jammedByPropId && !it.cleared } -> "DOOR JAMMED"
                        d.doorStyle == DoorStyle.GARAGE_ROLLUP -> if (d.open) "CLOSE GARAGE DOOR" else "OPEN GARAGE DOOR"
                        d.doorStyle == DoorStyle.DOUBLE_SLIDING_GLASS -> if (d.open) "CLOSE SLIDING DOOR" else "OPEN SLIDING DOOR"
                        d.doorStyle == DoorStyle.STEEL_SECURITY -> if (d.open) "CLOSE SECURITY DOOR" else "OPEN SECURITY DOOR"
                        d.open -> "CLOSE DOOR"
                        else -> "OPEN DOOR"
                    }
                    return ContextPrompt(ContextPromptKind.DOOR, label, d.id)
                }
            }
        }
        // 5. Loot containers (world + dropped scav packs)
        for (c in w.lootContainers) {
            if (c.floor == elevation && !c.searched && hypot(playerX - c.x, playerY - c.y) <= 56f) {
                return ContextPrompt(ContextPromptKind.LOOT_CONTAINER, "SEARCH ${c.label.uppercase()}", c.id)
            }
        }
        for (c in dynamicLootDrops) {
            if (c.floor == elevation && !c.searched && hypot(playerX - c.x, playerY - c.y) <= 56f) {
                return ContextPrompt(ContextPromptKind.LOOT_CONTAINER, "SEARCH ${c.label.uppercase()}", c.id)
            }
        }
        // 6. Searchable Vehicles
        for (p in w.props) {
            if (p.propType == "vehicle" && p.vehicleKind != null && !p.searched && p.floor == elevation) {
                val cx = p.x + p.w / 2f
                val cy = p.y + p.h / 2f
                if (hypot(playerX - cx, playerY - cy) <= 64f) {
                    return ContextPrompt(ContextPromptKind.SEARCH_VEHICLE, "SEARCH ${p.vehicleKind.label.uppercase()}", p.id)
                }
            }
        }
        return ContextPrompt()
    }

    private fun handleStairwellTraversal(w: WorldMapData, now: Long) {
        val activeStair = w.stairwells.firstOrNull { sw ->
            elevation in sw.accessibleFloors &&
                playerX in (sw.x - 2f)..(sw.x + sw.w + 2f) &&
                playerY in (sw.y - 2f)..(sw.y + sw.h + 2f)
        }
        if (activeStair == null) {
            val nearAny = w.stairwells.any { sw ->
                elevation in sw.accessibleFloors &&
                    playerX in (sw.x - 6f)..(sw.x + sw.w + 6f) &&
                    playerY in (sw.y - 6f)..(sw.y + sw.h + 6f)
            }
            if (!nearAny) {
                stairLatchActive = false
            }
            return
        }
        if (stairLatchActive || now - lastStairTransitionMs < 450L) return

        val sortedFloors = activeStair.accessibleFloors.sorted()
        val curIdx = sortedFloors.indexOf(elevation)
        if (curIdx < 0) return

        val higherFloor = sortedFloors.getOrNull(curIdx + 1)
        val lowerFloor = sortedFloors.getOrNull(curIdx - 1)

        val targetFloor: Int? = when {
            higherFloor != null && lowerFloor != null -> {
                val goUp = if (activeStair.w >= activeStair.h) {
                    playerX < activeStair.x + activeStair.w * 0.5f
                } else {
                    playerY < activeStair.y + activeStair.h * 0.5f
                }
                if (goUp) higherFloor else lowerFloor
            }
            higherFloor != null -> higherFloor
            lowerFloor != null -> lowerFloor
            else -> null
        }

        if (targetFloor != null && targetFloor != elevation) {
            val wentUp = targetFloor > elevation
            val building = w.buildings.firstOrNull { it.id == activeStair.buildingId }
            val nextLabel = when {
                targetFloor == -1 -> "B1"
                targetFloor == 0 -> "1F"
                building?.roofFloorIndex == targetFloor -> "ROOF"
                else -> "${targetFloor}F"
            }
            transitionFloor(targetFloor)
            stairLatchActive = true
            lastStairTransitionMs = now
            showToast(if (wentUp) "▲ STAIRS UP TO $nextLabel" else "▼ STAIRS DOWN TO $nextLabel")
        }
    }

    private fun hasLineOfSight(w: WorldMapData, x1: Float, y1: Float, x2: Float, y2: Float, elev: Int): Boolean {
        for (wall in w.walls) {
            if (wall.floor == elev && !wall.isWindow && !wall.isFence) {
                if (segmentIntersectsRect(x1, y1, x2, y2, wall.x, wall.y, wall.w, wall.h)) return false
            }
        }
        for (d in w.doors) {
            if (d.floor == elev && !d.open && d.slideProgress < 0.65f && d.doorStyle != DoorStyle.DOUBLE_SLIDING_GLASS) {
                val tipX = d.hingeX + cos(d.angle) * d.length
                val tipY = d.hingeY + sin(d.angle) * d.length
                if (segmentsIntersect(x1, y1, x2, y2, d.hingeX, d.hingeY, tipX, tipY)) return false
            }
        }
        return true
    }

    private fun segmentIntersectsRect(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        rx: Float,
        ry: Float,
        rw: Float,
        rh: Float
    ): Boolean {
        val minX = min(x1, x2)
        val maxX = max(x1, x2)
        val minY = min(y1, y2)
        val maxY = max(y1, y2)
        if (maxX < rx || minX > rx + rw || maxY < ry || minY > ry + rh) return false
        return segmentsIntersect(x1, y1, x2, y2, rx, ry, rx + rw, ry) ||
            segmentsIntersect(x1, y1, x2, y2, rx + rw, ry, rx + rw, ry + rh) ||
            segmentsIntersect(x1, y1, x2, y2, rx + rw, ry + rh, rx, ry + rh) ||
            segmentsIntersect(x1, y1, x2, y2, rx, ry + rh, rx, ry)
    }

    private fun segmentsIntersect(
        ax: Float,
        ay: Float,
        bx: Float,
        by: Float,
        cx: Float,
        cy: Float,
        dx: Float,
        dy: Float
    ): Boolean {
        fun ccw(px: Float, py: Float, qx: Float, qy: Float, rx: Float, ry: Float): Float =
            (qy - py) * (rx - qx) - (qx - px) * (ry - qy)
        val d1 = ccw(cx, cy, dx, dy, ax, ay)
        val d2 = ccw(cx, cy, dx, dy, bx, by)
        val d3 = ccw(ax, ay, bx, by, cx, cy)
        val d4 = ccw(ax, ay, bx, by, dx, dy)
        return ((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
            ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))
    }

    private fun circleRectCollide(cx: Float, cy: Float, r: Float, rx: Float, ry: Float, rw: Float, rh: Float): Boolean {
        val closeX = max(rx, min(cx, rx + rw))
        val closeY = max(ry, min(cy, ry + rh))
        return hypot(cx - closeX, cy - closeY) < r
    }

    private fun circleSegmentCollide(cx: Float, cy: Float, r: Float, x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        val dx = x2 - x1
        val dy = y2 - y1
        val lenSq = dx * dx + dy * dy
        var t = if (lenSq > 0f) ((cx - x1) * dx + (cy - y1) * dy) / lenSq else 0f
        t = t.coerceIn(0f, 1f)
        return hypot(cx - (x1 + t * dx), cy - (y1 + t * dy)) < r
    }
}
