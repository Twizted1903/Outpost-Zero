package com.example

import com.example.game.audio.TacticalAudioEngine
import com.example.game.engine.DayNightCycle
import com.example.game.engine.GameSimulation
import com.example.game.model.AttachmentSlot
import com.example.game.model.BaseCustomLayout
import com.example.game.model.BaseModuleKind
import com.example.game.model.BaseStashState
import com.example.game.model.BaseUpgradeSpecs
import com.example.game.model.CameraViewBounds
import com.example.game.model.CraftOutputKind
import com.example.game.model.CraftingCatalog
import com.example.game.model.CraftingMaterial
import com.example.game.model.DogBreedResolver
import com.example.game.model.DoorStyle
import com.example.game.model.GameMapId
import com.example.game.model.HatStyle
import com.example.game.model.HeritageVaultCatalog
import com.example.game.model.InventoryItem
import com.example.game.model.LootType
import com.example.game.model.OperatorArchetype
import com.example.game.model.OperatorProfile
import com.example.game.model.OutbreakTimelineCatalog
import com.example.game.model.RivalOperatorEntity
import com.example.game.model.ScavAiState
import com.example.game.model.ScavKinshipMemoryStore
import com.example.game.model.ScavKinshipRoster
import com.example.game.model.ShirtStyle
import com.example.game.model.SlainScavRecord
import com.example.game.model.StationKind
import com.example.game.model.TerrainKind
import com.example.game.model.WeaponAttachmentCatalog
import com.example.game.model.WeaponEra
import com.example.game.model.WeaponId
import com.example.game.world.OakridgeMapBuilder
import com.example.game.world.OutpostZeroMapBuilder
import kotlin.math.hypot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private fun sampleOperator(
        kiaCount: Int = 1,
        isKia: Boolean = true,
        kiaTimestamp: Long = 1_000_000L
    ): OperatorProfile = OperatorProfile(
        id = "op-1",
        slotIndex = 0,
        callsign = "GHOST-1",
        fullName = "Marcus Cole",
        archetype = OperatorArchetype.BREACHER,
        skinHex = 0xFFD9A37C,
        shirtHex = 0xFF253023,
        vestHex = 0xFF364A33,
        packHex = 0xFF4F3F27,
        pantsLevel = 1,
        armorLevel = 0,
        dogFound = true,
        kiaCount = kiaCount,
        isKia = isKia,
        kiaTimestamp = kiaTimestamp,
        infirmaryUntil = 0L,
        safePocketItems = listOf(
            InventoryItem(uid = "safe-1", type = LootType.CD, name = "Track 01 CD", cdId = 1)
        ),
        backpackItems = listOf(
            InventoryItem(uid = "bag-1", type = LootType.FOOD, name = "Canned Beans")
        )
    )

    @Test
    fun buyBackFee_escalatesWithKiaCount() {
        assertEquals(150, sampleOperator(kiaCount = 0).currentBuyBackFee())
        assertEquals(150, sampleOperator(kiaCount = 1).currentBuyBackFee())
        assertEquals(300, sampleOperator(kiaCount = 2).currentBuyBackFee())
        assertEquals(450, sampleOperator(kiaCount = 3).currentBuyBackFee())
    }

    @Test
    fun remainingPermadeathMs_tracksTwentyFourHourWindow() {
        val kiaAt = 10_000_000L
        val op = sampleOperator(isKia = true, kiaTimestamp = kiaAt)
        val fourHoursMs = OperatorProfile.SCAVENGE_INTERVAL_MS
        val fullWindowMs = OperatorProfile.PERMADEATH_WINDOW_MS

        assertEquals(4L * 60L * 60L * 1000L, fourHoursMs)
        assertEquals(24L * 60L * 60L * 1000L, fullWindowMs)
        assertEquals(fullWindowMs - fourHoursMs, op.remainingPermadeathMs(kiaAt + fourHoursMs))
        assertEquals(0L, op.remainingPermadeathMs(kiaAt + fullWindowMs + 5_000L))
    }

    @Test
    fun outbreakTimeline_isLockedToShared2026PostOutbreakMoment() {
        val entries = OutbreakTimelineCatalog.entries
        assertEquals("Shared world timeline must have a single fixed node", 1, entries.size)
        val cur = OutbreakTimelineCatalog.currentEra
        assertTrue(cur.dateLabel.contains("2026"))
        assertTrue(cur.dateLabel.contains("POST-OUTBREAK"))
        assertFalse(cur.dateLabel.contains("2027"))
        assertTrue(cur.summary.contains("November 2026"))
    }

    @Test
    fun stairwellTraversal_transitionsFloorWithFadeAndAntiPingPongLatch() {
        val audio = TacticalAudioEngine().apply { muted = true }
        val sim = GameSimulation(audio)
        val oakridge = OakridgeMapBuilder.build(runSeed = 77L)
        val op = sampleOperator(kiaCount = 0, isKia = false, kiaTimestamp = 0L)
        sim.loadWorld(oakridge, op, listOf(op), resetSurvival = true)

        val multiFloorStair = oakridge.stairwells.first { sw -> 0 in sw.accessibleFloors && 1 in sw.accessibleFloors }
        assertEquals(0, sim.elevation)

        // Step onto UP side of stairwell on floor 0 -> moves to floor 1 and triggers floor fade-in
        sim.playerX = multiFloorStair.x + multiFloorStair.w * 0.25f
        sim.playerY = multiFloorStair.y + multiFloorStair.h * 0.5f
        sim.step(0.016f)
        assertEquals(1, sim.elevation)
        assertTrue("New floor should start fading in smoothly", sim.floorFadeAlpha < 0.2f)

        // Staying inside the stairwell must NOT ping-pong right back down
        sim.step(0.016f)
        assertEquals(1, sim.elevation)

        // Walk off the stairwell to reset latch, then step back onto DOWN half
        sim.playerX = multiFloorStair.x - 40f
        sim.playerY = multiFloorStair.y - 40f
        sim.step(0.05f)
        Thread.sleep(470L)

        sim.playerX = multiFloorStair.x + multiFloorStair.w * 0.75f
        sim.playerY = multiFloorStair.y + multiFloorStair.h * 0.5f
        sim.step(0.016f)
        assertEquals(0, sim.elevation)
    }

    @Test
    fun pvpKill_triggersLiveKillFeedCallbackWithKillerVictimAndWeapon() {
        val audio = TacticalAudioEngine().apply { muted = true }
        val sim = GameSimulation(audio)
        val oakridge = OakridgeMapBuilder.build(runSeed = 88L)
        val op = sampleOperator(kiaCount = 0, isKia = false, kiaTimestamp = 0L)
        val stash = BaseStashState(
            unlockedWeaponKeys = setOf(WeaponId.KNIFE.key, WeaponId.M4.key),
            ammoM4 = 60,
            weaponAmmoMap = mapOf(WeaponId.M4.key to 60)
        )
        sim.loadWorld(oakridge, op, listOf(op), resetSurvival = true, baseStash = stash)
        sim.currentWeapon = WeaponId.M4
        sim.playerX = 0f
        sim.playerY = 0f
        sim.playerAngle = 0f

        var capturedKiller: String? = null
        var capturedVictim: String? = null
        var capturedWeapon: WeaponId? = null
        sim.onPlayerKilledPlayer = { killer, victim, weapon, _ ->
            capturedKiller = killer
            capturedVictim = victim
            capturedWeapon = weapon
        }

        sim.rivalOperators.add(
            RivalOperatorEntity(
                id = "rival_test_1",
                uid = "rival_uid_1",
                callsign = "VIPER-7",
                x = 70f,
                y = 0f,
                hp = 10f,
                maxHp = 100f,
                elevation = 0,
                weapon = WeaponId.AK47
            )
        )

        sim.manualFireHeld = true
        repeat(6) { sim.step(0.016f) }

        assertEquals("GHOST-1", capturedKiller)
        assertEquals("VIPER-7", capturedVictim)
        assertEquals(WeaponId.M4, capturedWeapon)
    }

    @Test
    fun outpostZeroMap_isExpandedFencedCompoundSmallerThanOakridge() {
        val outpost = OutpostZeroMapBuilder.build(bunkLevel = 1, storageLevel = 1)
        val oakridge = OakridgeMapBuilder.build()

        val outBounds = outpost.worldBounds!!
        val oakBounds = oakridge.worldBounds!!
        assertTrue(outBounds.w >= 1960f)
        assertTrue(outBounds.h >= 1520f)
        assertTrue(outBounds.w < oakBounds.w)

        val fences = outpost.walls.filter { it.isFence }
        assertTrue("Perimeter fence segments must exist", fences.size >= 4)
        assertTrue("Outpost Zero must NOT have urban asphalt roads cutting through camp", outpost.roads.isEmpty())
        assertTrue("Outpost Zero must NOT have yellow city road stripes", outpost.roadMarkings.isEmpty())

        val stationKinds = outpost.stations.map { it.kind }.toSet()
        assertTrue(StationKind.LAPTOP in stationKinds)
        assertTrue(StationKind.WORKBENCH in stationKinds)
        assertTrue(StationKind.MARKET in stationKinds)
        assertTrue(StationKind.EXIT_GATE in stationKinds)
        assertTrue("Starter Outpost Zero must NOT have free Helipad before upgrade", StationKind.HELIPAD !in stationKinds)

        val upgradedOutpost = OutpostZeroMapBuilder.build(bunkLevel = 1, storageLevel = 1, helipadLevel = 1)
        assertTrue(StationKind.HELIPAD in upgradedOutpost.stations.map { it.kind })
    }

    @Test
    fun recruitAndUpgradeCosts_scaleSteeplyForSecondAndLaterOperators() {
        assertEquals(0, BaseUpgradeSpecs.recruitOperatorCredCost(0))
        assertTrue(BaseUpgradeSpecs.recruitOperatorMats(0).isEmpty())
        val secondCred = BaseUpgradeSpecs.recruitOperatorCredCost(1)
        val thirdCred = BaseUpgradeSpecs.recruitOperatorCredCost(2)
        assertTrue("2nd operator must have meaningful cost", secondCred >= 500)
        assertTrue("3rd operator must cost more than 2nd", thirdCred > secondCred)
        assertTrue(BaseUpgradeSpecs.recruitOperatorMats(1).isNotEmpty())
        assertTrue(BaseUpgradeSpecs.bunkUpgradeCredCost(2) > BaseUpgradeSpecs.bunkUpgradeCredCost(1))
    }

    @Test
    fun baseCustomLayout_movesLaptopBunksAndBuildingsInsideCompound() {
        val base = BaseCustomLayout.default(bunkLevel = 3, helipadLevel = 1)
        assertEquals(3, base.byKind(BaseModuleKind.BUNK).size)
        assertTrue(base.firstByKind(BaseModuleKind.HELIPAD) != null)

        val movedPieces = base.pieces.map { piece ->
            when (piece.kind) {
                BaseModuleKind.LAPTOP -> piece.copy(x = 240f, y = 320f)
                BaseModuleKind.BUNK -> if (piece.slotNumber == 2) piece.copy(x = -220f, y = 180f) else piece
                BaseModuleKind.BARRACKS -> piece.copy(x = 400f, y = -300f)
                else -> piece
            }
        }
        val customOutpost = OutpostZeroMapBuilder.build(
            bunkLevel = 3,
            storageLevel = 1,
            customLayout = BaseCustomLayout(movedPieces),
            helipadLevel = 1
        )
        val laptopStation = customOutpost.stations.first { it.kind == StationKind.LAPTOP }
        assertEquals(240f, laptopStation.x, 1f)
        assertTrue(customOutpost.stations.count { it.kind == StationKind.BUNK } == 3)
        assertTrue(customOutpost.stations.any { it.kind == StationKind.BUNK && kotlin.math.abs(it.x + 220f) < 5f })

        val scratchNoHeli = BaseCustomLayout.emptyScratch(bunkLevel = 2, helipadLevel = 0)
        val scratchMap = OutpostZeroMapBuilder.build(bunkLevel = 2, storageLevel = 1, customLayout = scratchNoHeli, helipadLevel = 0)
        assertEquals(0, scratchMap.buildings.size)
        assertTrue(scratchMap.stations.any { it.kind == StationKind.LAPTOP })
        assertTrue(scratchMap.stations.any { it.kind == StationKind.EXIT_GATE })
        assertTrue(scratchMap.stations.none { it.kind == StationKind.HELIPAD })
        assertEquals(2, scratchMap.stations.count { it.kind == StationKind.BUNK })

        val scratchWithHeli = BaseCustomLayout.emptyScratch(bunkLevel = 2, helipadLevel = 1)
        val scratchHeliMap = OutpostZeroMapBuilder.build(bunkLevel = 2, storageLevel = 1, customLayout = scratchWithHeli, helipadLevel = 1)
        assertTrue(scratchHeliMap.stations.any { it.kind == StationKind.HELIPAD })
    }

    @Test
    fun helipadUpgradeAndCustomOakridgeDrop_workCorrectly() {
        assertTrue(BaseUpgradeSpecs.helipadUpgradeCredCost(0) >= 300)
        assertTrue(BaseUpgradeSpecs.helipadUpgradeMats(0).isNotEmpty())
        assertTrue(BaseCustomLayout.default(bunkLevel = 1, helipadLevel = 0).firstByKind(BaseModuleKind.HELIPAD) == null)

        // Open street custom drop lands accurately on chosen coords
        val streetDrop = OakridgeMapBuilder.build(runSeed = 100L, customDropX = 320f, customDropY = -10f)
        assertEquals(320f, streetDrop.spawnX, 1f)
        assertEquals(-10f, streetDrop.spawnY, 1f)
        assertTrue(streetDrop.initialZombies.none { z ->
            z.elevation == 0 && kotlin.math.hypot(z.x - streetDrop.spawnX, z.y - streetDrop.spawnY) < 250f
        })

        // Custom drop requested on top of Plaza Tower snaps safely outdoors outside all building footprints
        val buildingDrop = OakridgeMapBuilder.build(runSeed = 100L, customDropX = 0f, customDropY = -450f)
        for (b in buildingDrop.buildings) {
            val inside = buildingDrop.spawnX in b.bounds.x..(b.bounds.x + b.bounds.w) &&
                buildingDrop.spawnY in b.bounds.y..(b.bounds.y + b.bounds.h)
            assertTrue("Drop must resolve outside ${b.name}", !inside)
        }
    }

    @Test
    fun mapsIncludePowerLinkedTvs() {
        val oakridge = OakridgeMapBuilder.build()
        val oakTvs = oakridge.props.filter { it.propType == "tv" }
        assertTrue("Oakridge houses & civic buildings should have TVs", oakTvs.size >= 5)
        assertTrue("Some Oakridge TVs should start powered ON", oakTvs.any { it.tvPoweredOn })

        val outpost = OutpostZeroMapBuilder.build()
        assertTrue("Outpost Zero should include CRT TVs", outpost.props.any { it.propType == "tv" })
    }

    @Test
    fun outpostZero_remainsCompletelySafeWithZeroHostiles() {
        val outpost = OutpostZeroMapBuilder.build()
        assertTrue("Outpost Zero must never spawn zombies", outpost.initialZombies.isEmpty())
        assertTrue("Outpost Zero must never spawn hostile scavs", outpost.initialScavs.isEmpty())
        assertTrue("Outpost Zero must never contain scav encampments", outpost.scavEncampments.isEmpty())

        val sim = GameSimulation(TacticalAudioEngine().apply { muted = true })
        val op = sampleOperator(kiaCount = 0, isKia = false, kiaTimestamp = 0L)
        sim.loadWorld(outpost, op, listOf(op), resetSurvival = true)
        sim.step(0.05f)
        assertTrue(sim.zombies.isEmpty())
        assertTrue(sim.rivalOperators.isEmpty())
    }

    @Test
    fun weaponsCatalog_hasExpandedRealGunsWithDetailedSpecs() {
        val firearms = WeaponId.entries.filter { !it.isMelee }
        assertTrue("Should have at least 35 real firearms", firearms.size >= 35)
        for (w in firearms) {
            assertTrue("${w.key} must have manufacturer", w.manufacturer.isNotBlank())
            assertTrue("${w.key} must have country of origin", w.countryOrigin.isNotBlank())
            assertTrue("${w.key} must have detailed description", w.gunDescription.length >= 25)
            assertTrue("${w.key} must have muzzle velocity", w.muzzleVelocityFps > 0)
            assertTrue("${w.key} must have effective range", w.effectiveRangeMeters > 0)
        }
    }

    @Test
    fun craftingMaterials_hasOverTwentyBaseItemsAndAllDropInOakridge() {
        assertTrue("Expected over 20 base crafting items", CraftingMaterial.entries.size >= 24)
        val oakridge = OakridgeMapBuilder.build(runSeed = 42L)
        val allWorldLoot = oakridge.lootContainers.flatMap { it.items } + oakridge.props.flatMap { it.lootItems }
        assertTrue("Oakridge must spawn base crafting materials", allWorldLoot.isNotEmpty())
        assertTrue(
            "Oakridge must spawn ONLY base crafting materials (no pre-built guns, mods, CDs, armor, or supplies)",
            allWorldLoot.all { it.type == LootType.MATERIAL && it.materialKey != null }
        )
        val worldMatKeys = allWorldLoot.mapNotNull { it.materialKey }.toSet()
        for (mat in CraftingMaterial.entries) {
            assertTrue("Oakridge should spawn ${mat.displayName}", mat.key in worldMatKeys)
        }
    }

    @Test
    fun craftingCatalog_coversAllNonStarterWeaponsAndAllAttachments() {
        val craftedWeaponKeys = CraftingCatalog.recipes
            .filter { it.outputKind == CraftOutputKind.WEAPON }
            .mapNotNull { it.weaponKey }
            .toSet()
        for (wpn in WeaponId.entries) {
            if (wpn == WeaponId.KNIFE) continue
            assertTrue("Weapon ${wpn.key} must be player-craftable at Workbench", wpn.key in craftedWeaponKeys)
        }

        val craftedAttachmentKeys = CraftingCatalog.recipes
            .filter { it.outputKind == CraftOutputKind.ATTACHMENT }
            .mapNotNull { it.attachmentKey }
            .toSet()
        for (att in WeaponAttachmentCatalog.all) {
            assertTrue("Attachment ${att.key} must be player-craftable at Workbench", att.key in craftedAttachmentKeys)
        }
    }

    @Test
    fun outfitSelection_updatesDogBreedAndRemovesNvgHats() {
        val redFlannelDog = DogBreedResolver.resolve(ShirtStyle.RED_FLANNEL)
        val forestFlannelDog = DogBreedResolver.resolve(ShirtStyle.FOREST_FLANNEL)
        assertEquals("German Shepherd", redFlannelDog.breed)
        assertEquals("Belgian Malinois", forestFlannelDog.breed)
        assertTrue("Shirt change should change dog coat color", redFlannelDog.coatColorHex != forestFlannelDog.coatColorHex)
        assertFalse(HatStyle.entries.any { it.name.contains("NVG", ignoreCase = true) || it.label.contains("NVG", ignoreCase = true) })
    }

    @Test
    fun ammoCrafting_isGatedBehindHqReloadingGear() {
        val defaultStash = BaseStashState()
        assertEquals(0, defaultStash.reloadingGearLevel)
        val ammoRecipes = CraftingCatalog.recipes.filter { it.outputKind == CraftOutputKind.AMMO }
        assertTrue("Should have ammo crafting recipes", ammoRecipes.size >= 6)
        assertTrue("All ammo recipes require reloading bench Lv.1+", ammoRecipes.all { it.requiredReloadingTier >= 1 })
        assertTrue("Reloading bench upgrade cost must be > 0", BaseUpgradeSpecs.reloadingUpgradeCredCost(0) > 0)
    }

    @Test
    fun weaponsRoster_hasNinetyNineTotalAndHistoricalCollectiblesByEra() {
        assertEquals(99, WeaponId.entries.size)
        assertEquals(8, WeaponId.entries.count { it.isMelee })
        assertEquals(91, WeaponId.entries.count { !it.isMelee })

        val collectibles = WeaponId.historicalCollectibles
        assertEquals(52, collectibles.size)
        assertEquals(6, WeaponId.collectiblesForEra(WeaponEra.FRONTIER).size)
        assertEquals(8, WeaponId.collectiblesForEra(WeaponEra.WWI).size)
        assertEquals(16, WeaponId.collectiblesForEra(WeaponEra.WWII).size)
        assertEquals(22, WeaponId.collectiblesForEra(WeaponEra.COLD_WAR).size)
        assertTrue("Expected rich era-authentic attachment catalog", WeaponAttachmentCatalog.all.size >= 50)
    }

    @Test
    fun attachments_enforceHistoricalEraAndPlatformCompatibility() {
        val modernHolo = WeaponAttachmentCatalog.fromKey("att_eotech_exps3")!!
        val modernCan = WeaponAttachmentCatalog.fromKey("att_surefire_rc2")!!
        val modernLaser = WeaponAttachmentCatalog.fromKey("att_peq15_laser")!!
        val brassScope = WeaponAttachmentCatalog.fromKey("att_malcolm_brass_scope")!!
        val lugerDrum = WeaponAttachmentCatalog.fromKey("att_luger_snail_drum")!!
        val tommyDrum = WeaponAttachmentCatalog.fromKey("att_thompson_50_drum")!!
        val cuttsComp = WeaponAttachmentCatalog.fromKey("att_cutts_compensator")!!

        // Era lock prevents modern optics/lasers/suppressors on Frontier/WWI/WWII antiques
        assertFalse(modernHolo.isCompatibleWith(WeaponId.COLT_SAA))
        assertFalse(modernCan.isCompatibleWith(WeaponId.M1_GARAND))
        assertFalse(modernLaser.isCompatibleWith(WeaponId.LUGER_P08))
        assertFalse(brassScope.isCompatibleWith(WeaponId.M4))

        // Period-correct platforms fit
        assertTrue(lugerDrum.isCompatibleWith(WeaponId.LUGER_P08))
        assertTrue(tommyDrum.isCompatibleWith(WeaponId.THOMPSON_M1928))
        assertTrue(cuttsComp.isCompatibleWith(WeaponId.THOMPSON_M1928))
        assertTrue(modernHolo.isCompatibleWith(WeaponId.M4))
        assertTrue(modernCan.isCompatibleWith(WeaponId.M4))

        // Effective stats apply magazine/suppression/range modifiers
        val m4Modded = WeaponAttachmentCatalog.computeEffectiveStats(
            WeaponId.M4,
            setOf("att_eotech_exps3", "att_surefire_rc2", "att_d60_drum")
        )
        assertTrue(m4Modded.suppressed)
        assertEquals(60, m4Modded.magSize)
        assertTrue(m4Modded.baseRange > WeaponId.M4.baseRange)
        assertTrue(m4Modded.noiseRadius < WeaponId.M4.noiseRadius)
    }

    @Test
    fun stashAttachmentEquippingHeritageVaultAndBunkGate_workAsDesigned() {
        val base = BaseStashState()
        val unowned = WeaponAttachmentCatalog.fromKey("att_thompson_50_drum")!!
        // Cannot equip unowned attachment
        assertEquals(base, base.withEquippedAttachment(WeaponId.THOMPSON_M1928, unowned))

        // Unlock & equip period attachment
        val withMod = base.withUnlockedAttachments(setOf("att_thompson_50_drum", "att_eotech_exps3"))
        val fitted = withMod.withEquippedAttachment(WeaponId.THOMPSON_M1928, unowned)
        assertTrue("att_thompson_50_drum" in fitted.equippedAttachmentsFor(WeaponId.THOMPSON_M1928))
        assertEquals(50, fitted.effectiveStatsFor(WeaponId.THOMPSON_M1928).magSize)

        // Unequip clears slot
        val cleared = fitted.withUnequippedSlot(WeaponId.THOMPSON_M1928, AttachmentSlot.MAGAZINE)
        assertTrue(cleared.equippedAttachmentsFor(WeaponId.THOMPSON_M1928).isEmpty())

        // Recording historic gun tracks both weapon unlock and Heritage Vault progress
        val withRelic = cleared.withRecordedWeapons(setOf(WeaponId.LUGER_P08.key, WeaponId.M4.key))
        assertTrue(WeaponId.LUGER_P08.key in withRelic.collectedHistoricKeys)
        assertFalse(WeaponId.M4.key in withRelic.collectedHistoricKeys)

        // Heritage milestones exist for all 4 historical eras and default bunk level = 1 slot
        for (era in WeaponEra.historicalEras) {
            val milestone = HeritageVaultCatalog.milestoneFor(era)
            assertTrue("Era $era should have milestone reward", milestone != null && milestone.rewardCred > 0)
        }
        assertEquals(1, base.maxRosterSlots)
        assertTrue(BaseUpgradeSpecs.bunkSlotsForLevel(2) > base.maxRosterSlots)
    }

    @Test
    fun delaineMetroMap_hasFullScaleDistrictsStevesMusicGaragesDoorsAndElevation() {
        val metro = OakridgeMapBuilder.build(runSeed = 2026L)
        val bounds = metro.worldBounds!!
        assertEquals(6400f, bounds.w, 1f)
        assertEquals(6400f, bounds.h, 1f)

        // Fictionalized DeLand metro landmarks + preserved Steve's Music
        val names = metro.buildings.map { it.name }
        assertTrue("Steve's Music must be in Downtown DeLaine", names.any { it.contains("STEVE'S MUSIC", ignoreCase = true) })
        assertTrue("Valen County Courthouse must exist", names.any { it.contains("VALEN COUNTY COURTHOUSE", ignoreCase = true) })
        assertTrue("Stanton University halls must exist", names.any { it.contains("STANTON", ignoreCase = true) || it.contains("FOUNDERS", ignoreCase = true) })
        assertTrue("Cobalt Springs State Park structures must exist", names.any { it.contains("COBALT SPRINGS", ignoreCase = true) })
        assertTrue("Attached garages must exist on residential homes", metro.props.any { it.propType == "garage_bench" } && metro.lootContainers.any { it.label.contains("Garage", ignoreCase = true) })
        assertTrue("Steve's Music guitar & drum props must exist", metro.props.any { it.propType == "guitar_wall" } && metro.props.any { it.propType == "drum_kit" })

        // Building-matched door styles & automatic sensor doors
        val doorStyles = metro.doors.map { it.doorStyle }.toSet()
        assertTrue(DoorStyle.HINGED_WOOD in doorStyles)
        assertTrue(DoorStyle.DOUBLE_SLIDING_GLASS in doorStyles)
        assertTrue(DoorStyle.GARAGE_ROLLUP in doorStyles)
        assertTrue(DoorStyle.STEEL_SECURITY in doorStyles)
        assertTrue("Commercial/mart sliding doors should use proximity auto-sensors", metro.doors.any { it.autoSensor && it.doorStyle == DoorStyle.DOUBLE_SLIDING_GLASS })

        // Non-flat outdoor terrain elevation: water basin, ridges/plazas, ramps, boardwalks/overpass
        assertTrue(metro.terrainZones.any { it.kind == TerrainKind.BASIN_WATER })
        assertTrue(metro.terrainZones.any { it.heightLevel > 0 })
        assertTrue(metro.terrainRamps.isNotEmpty())
        assertTrue(metro.elevatedWalkways.any { !it.isBoardwalk } && metro.elevatedWalkways.any { it.isBoardwalk })

        // Air-drop directly over Courthouse or Cobalt Springs boil snaps safely outside buildings & water
        val courthouseDrop = OakridgeMapBuilder.build(runSeed = 2026L, customDropX = -355f, customDropY = -360f)
        for (b in courthouseDrop.buildings) {
            val inside = courthouseDrop.spawnX in b.bounds.x..(b.bounds.x + b.bounds.w) &&
                courthouseDrop.spawnY in b.bounds.y..(b.bounds.y + b.bounds.h)
            assertFalse("Drop must resolve outside ${b.name}", inside)
        }
    }

    @Test
    fun dayNightCycle_advancesOneHourPerFiveRealMinutesAndKeepsOutdoorsBrightByDay() {
        assertEquals(300f, DayNightCycle.REAL_SECONDS_PER_GAME_HOUR, 0.01f)

        val audio = TacticalAudioEngine().apply { muted = true }
        val sim = GameSimulation(audio)
        val outpost = OutpostZeroMapBuilder.build()
        val delaine = OakridgeMapBuilder.build(runSeed = 99L)
        val op = sampleOperator(kiaCount = 0, isKia = false, kiaTimestamp = 0L)

        sim.loadWorld(outpost, op, listOf(op), resetSurvival = true)
        sim.jumpToWorldTimeHours(9.5f)
        // 6,000 ticks * 0.05s = 300 real seconds = 5 real minutes = +1.0 in-game hour
        repeat(6000) { sim.step(0.05f) }
        assertEquals(10.5f, sim.worldTimeHours, 0.02f)

        // Switching between Outpost Zero and DeLaine preserves world time seamlessly
        sim.loadWorld(delaine, op, listOf(op), resetSurvival = false)
        assertEquals(10.5f, sim.worldTimeHours, 0.02f)

        // Daytime outdoors stays completely clear (0 darkness cutout), even in DeLaine
        val middayOutdoors = DayNightCycle.computeAtmosphere(
            hours = 12.0f,
            mapId = GameMapId.OAKRIDGE_TOWN,
            insideBuilding = false,
            inBlackout = false,
            inFlicker = false,
            highVisibilityNight = false
        )
        assertEquals(0f, middayOutdoors.overlayAlpha, 0.001f)
        assertFalse(middayOutdoors.streetLightsActive)

        // Daytime blackout does not plunge outdoor sunlight into pitch black
        val middayBlackoutOutdoors = DayNightCycle.computeAtmosphere(
            hours = 12.0f,
            mapId = GameMapId.OAKRIDGE_TOWN,
            insideBuilding = false,
            inBlackout = true,
            inFlicker = false,
            highVisibilityNight = false
        )
        assertTrue(middayBlackoutOutdoors.overlayAlpha < 0.15f)

        // Nighttime enables streetlights and darkness mask cleanly
        val nightOutdoors = DayNightCycle.computeAtmosphere(
            hours = 22.5f,
            mapId = GameMapId.OAKRIDGE_TOWN,
            insideBuilding = false,
            inBlackout = false,
            inFlicker = false,
            highVisibilityNight = false
        )
        assertTrue(nightOutdoors.overlayAlpha >= 0.40f)
        assertTrue(nightOutdoors.streetLightsActive)
        assertEquals("10:30 PM", nightOutdoors.clockLabel12)
    }

    @Test
    fun cameraZoomAndVoxelDepth_areStrictlyClampedToSafeBounds() {
        assertEquals(CameraViewBounds.MIN_CAMERA_ZOOM, CameraViewBounds.clampZoom(0.15f), 0.001f)
        assertEquals(CameraViewBounds.MAX_CAMERA_ZOOM, CameraViewBounds.clampZoom(4.50f), 0.001f)
        assertEquals(1.05f, CameraViewBounds.clampZoom(1.05f), 0.001f)

        assertEquals(CameraViewBounds.MIN_VOXEL_DEPTH, CameraViewBounds.clampVoxelDepth(0.10f), 0.001f)
        assertEquals(CameraViewBounds.MAX_VOXEL_DEPTH, CameraViewBounds.clampVoxelDepth(3.00f), 0.001f)

        val sim = GameSimulation(TacticalAudioEngine().apply { muted = true })
        sim.setCameraZoomClamped(0.05f)
        assertEquals(CameraViewBounds.MIN_CAMERA_ZOOM, sim.cameraZoom, 0.001f)
        sim.setCameraZoomClamped(9.0f)
        assertEquals(CameraViewBounds.MAX_CAMERA_ZOOM, sim.cameraZoom, 0.001f)
        sim.setVoxelDepthClamped(0.2f)
        assertEquals(CameraViewBounds.MIN_VOXEL_DEPTH, sim.voxelDepthScale, 0.001f)
        sim.setVoxelDepthClamped(2.8f)
        assertEquals(CameraViewBounds.MAX_VOXEL_DEPTH, sim.voxelDepthScale, 0.001f)

        val outpost = OutpostZeroMapBuilder.build(bunkLevel = 1, storageLevel = 1)
        assertTrue("Outpost Zero should sit on a raised hilltop plateau", outpost.terrainZones.any { it.kind == TerrainKind.TERRACE_PLAZA && it.heightLevel > 0 })
        assertTrue("Outpost Zero South Gate should have a graded access ramp", outpost.terrainRamps.isNotEmpty())
        assertTrue("Outpost Zero should spawn elevated timber watchtowers", outpost.props.count { it.propType == "watchtower" } >= 2)
    }

    @Test
    fun delaine_populatesDistrictTieredInfectedAndSixScavEncampments() {
        val metro = OakridgeMapBuilder.build(runSeed = 77L)
        assertEquals(6, metro.scavEncampments.size)
        assertTrue("DeLaine must populate infected squads", metro.initialZombies.size >= 25)
        assertTrue("DeLaine must populate Scav encampment squads", metro.initialScavs.size >= 14)

        val namedIds = metro.initialScavs.map { it.scavId }.toSet()
        assertTrue("James Vance should patrol his Citrus Overpass post", "scav_james_vance" in namedIds)
        assertTrue("Elena Vance should defend her Westside camp", "scav_elena_vance" in namedIds)
        assertTrue("Maya Vance should defend Stanton Quad", "scav_maya_vance" in namedIds)

        // All outer tiers must contain higher-tier enemies than Tier 1
        assertTrue(metro.districtZones.any { it.dangerTier == 1 } && metro.districtZones.any { it.dangerTier >= 3 })
        assertTrue(metro.initialZombies.any { it.homeDistrictTier >= 3 })

        // No hostile should ambush immediately at player drop-zone (< 250f)
        assertTrue(metro.initialZombies.none { z -> z.elevation == 0 && hypot(z.x - metro.spawnX, z.y - metro.spawnY) < 250f })
        assertTrue(metro.initialScavs.none { s -> s.elevation == 0 && hypot(s.x - metro.spawnX, s.y - metro.spawnY) < 250f })
    }

    @Test
    fun nightBlackout_triggersFrenzySwarm_whileDayBlackoutDoesNot() {
        val delaine = OakridgeMapBuilder.build(runSeed = 101L)
        val sim = GameSimulation(TacticalAudioEngine().apply { muted = true })
        val op = sampleOperator(kiaCount = 0, isKia = false, kiaTimestamp = 0L)
        sim.loadWorld(delaine, op, listOf(op), resetSurvival = true)

        // Daytime blackout does NOT trigger Zombie Frenzy
        sim.jumpToWorldTimeHours(13.0f)
        val baseCount = sim.zombies.size
        sim.triggerEmergencyBlackout(durationMs = 10_000L)
        assertFalse("Daytime blackout should not trigger night Frenzy", sim.isFrenzyActive)
        assertEquals(baseCount, sim.zombies.size)

        // Nighttime blackout triggers Frenzy + spawns frenzied swarm infected
        sim.jumpToWorldTimeHours(22.5f)
        sim.triggerEmergencyBlackout(durationMs = 15_000L)
        assertTrue("Nighttime blackout must trigger Zombie Frenzy", sim.isFrenzyActive)
        assertTrue("Nighttime Frenzy must spawn frantic swarm infected", sim.zombies.any { it.isFrenzy })
    }

    @Test
    fun stealthKillWithoutWitnesses_keepsCampmatesUnalerted_whileWitnessOrLoudShotTriggersCoverFight() {
        val delaine = OakridgeMapBuilder.build(runSeed = 202L)
        val sim = GameSimulation(TacticalAudioEngine().apply { muted = true })
        val op = sampleOperator(kiaCount = 0, isKia = false, kiaTimestamp = 0L)
        sim.loadWorld(delaine, op, listOf(op), resetSurvival = true)

        // Isolate two Citrus Overpass campmates behind cover/separation to verify stealth vs witness rules
        val citrusScavs = sim.rivalOperators.filter { it.campId == "camp_citrus_overpass" }
        assertTrue(citrusScavs.size >= 2)
        val victim = citrusScavs[0]
        val teammate = citrusScavs[1]
        // Place teammate far enough across camp and facing away so stealth blade kill has zero witnesses
        teammate.x = victim.x + 360f
        teammate.y = victim.y
        teammate.angle = 0f
        teammate.aiState = ScavAiState.PATROL

        sim.grantWeapon(WeaponId.KNIFE)
        sim.playerX = victim.x - 16f
        sim.playerY = victim.y
        sim.playerAngle = 0f
        victim.hp = 5f
        sim.manualFireHeld = true
        sim.step(0.05f)
        sim.manualFireHeld = false

        assertFalse("Victim should be eliminated by knife", victim in sim.rivalOperators)
        assertEquals("Stealth blade kill without witnesses must leave distant campmate in PATROL", ScavAiState.PATROL, teammate.aiState)

        // Now place a witness within sight/earshot of an unsuppressed gunshot
        val nextTarget = sim.rivalOperators.first { it != teammate }
        teammate.x = nextTarget.x + 140f
        teammate.y = nextTarget.y
        teammate.angle = kotlin.math.PI.toFloat() // looking straight toward nextTarget & shooter
        sim.grantWeapon(WeaponId.AK47)
        sim.playerX = nextTarget.x - 36f
        sim.playerY = nextTarget.y
        sim.playerAngle = 0f
        sim.manualFireHeld = true
        sim.step(0.05f)
        sim.manualFireHeld = false

        assertTrue(
            "Loud gunshot / witness must trigger ENGAGE or TAKE_COVER",
            teammate.aiState == ScavAiState.ENGAGE || teammate.aiState == ScavAiState.TAKE_COVER
        )
    }

    @Test
    fun kinshipMemory_persistsSlainScavAndTriggersInvestigationInLaterRaid() {
        val record = SlainScavRecord(
            scavId = "scav_james_vance",
            displayName = "James Vance",
            campId = "camp_citrus_overpass",
            campName = "Citrus Overpass Post",
            districtName = "South Citrus Highway",
            killerCallsign = "GHOST-1",
            weaponKey = WeaponId.M4.key,
            killedAtMs = 123456789L,
            witnessedBySquad = false
        )
        val encoded = ScavKinshipMemoryStore.encode(listOf(record))
        val decoded = ScavKinshipMemoryStore.decode(encoded)
        assertEquals(1, decoded.size)
        assertEquals("scav_james_vance", decoded.first().scavId)
        assertEquals("James Vance", decoded.first().displayName)
        assertFalse(decoded.first().witnessedBySquad)

        // Check roster resolution for Elena (wife) & Maya (sister)
        val elenaInquiry = ScavKinshipRoster.resolveInquiriesForLivingScav("scav_elena_vance", decoded)
        val mayaInquiry = ScavKinshipRoster.resolveInquiriesForLivingScav("scav_maya_vance", decoded)
        assertTrue(elenaInquiry != null && elenaInquiry.relationshipToSlain == "husband")
        assertTrue(mayaInquiry != null && mayaInquiry.relationshipToSlain == "brother")

        // Loading a fresh DeLaine deployment with that memory removes James and alerts Elena & Maya
        val laterDelaine = OakridgeMapBuilder.build(runSeed = 999L)
        val sim = GameSimulation(TacticalAudioEngine().apply { muted = true })
        val op = sampleOperator(kiaCount = 0, isKia = false, kiaTimestamp = 0L)
        sim.loadWorld(laterDelaine, op, listOf(op), resetSurvival = true, slainScavMemory = decoded)

        assertTrue("Slain James Vance must stay dead in later raids", sim.rivalOperators.none { it.scavId == "scav_james_vance" })
        val elena = sim.rivalOperators.first { it.scavId == "scav_elena_vance" }
        val maya = sim.rivalOperators.first { it.scavId == "scav_maya_vance" }
        assertTrue(elena.isVengefulKin)
        assertEquals("James Vance", elena.kinInquiryTargetName)
        assertEquals("husband", elena.kinRelationship)
        assertEquals(ScavAiState.INVESTIGATE, elena.aiState)
        assertTrue(maya.isVengefulKin)
        assertEquals("brother", maya.kinRelationship)
    }
}
