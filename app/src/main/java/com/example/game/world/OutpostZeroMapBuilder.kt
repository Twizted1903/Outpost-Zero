package com.example.game.world

import com.example.game.model.AmbientZone
import com.example.game.model.AmbientZoneKind
import com.example.game.model.BaseCustomLayout
import com.example.game.model.BaseModuleKind
import com.example.game.model.GameMapId
import com.example.game.model.GroundPatch
import com.example.game.model.InteractiveStation
import com.example.game.model.NpcSurvivorPlacement
import com.example.game.model.PropObj
import com.example.game.model.RectF
import com.example.game.model.StationKind
import com.example.game.model.StreetLightObj
import com.example.game.model.TerrainKind
import com.example.game.model.TerrainRamp
import com.example.game.model.TerrainZone
import com.example.game.model.WallSegment
import com.example.game.model.WorldMapData

object OutpostZeroMapBuilder {

    fun build(
        bunkLevel: Int = 1,
        storageLevel: Int = 1,
        customLayout: BaseCustomLayout? = null,
        helipadLevel: Int = 0
    ): WorldMapData {
        val acc = MutableWorldAccum()
        val safeBunkLevel = bunkLevel.coerceIn(1, 5)
        val safeStorageLevel = storageLevel.coerceIn(1, 5)
        val safeHelipadLevel = helipadLevel.coerceIn(0, 1)
        val layout = (customLayout ?: BaseCustomLayout.default(safeBunkLevel, safeHelipadLevel))
            .ensureInfrastructure(safeBunkLevel, safeHelipadLevel)

        // Fortified Hilltop Plateau Perimeter overlooking the misty DeLaine valley
        val fL = -1000f
        val fR = 1000f
        val fT = -780f
        val fB = 780f

        // Splash-matched smoky teal-pine valley surround + elevated plateau compound ground
        acc.groundPatches.add(GroundPatch(-1280f, -1040f, 2560f, 2140f, 0xFF172621))
        acc.groundPatches.add(GroundPatch(fL, fT, fR - fL, fB - fT, 0xFF243034, "OUTPOST ZERO COMPOUND"))
        acc.groundPatches.add(GroundPatch(-920f, -700f, 1840f, 1340f, 0xFF2D383D))

        // Rocky Hilltop Bluff Escarpment (Southern ridge lip dropping into the misty DeLaine approach road)
        acc.terrainZones.add(
            TerrainZone(
                x = -950f,
                y = -720f,
                w = 1900f,
                h = 1360f,
                heightLevel = 1,
                kind = TerrainKind.TERRACE_PLAZA,
                colorHex = 0xFF2D383C,
                label = "OUTPOST BLUFF PLATEAU"
            )
        )
        // Southern Cliff Drop-off flanking the fortified South Gate ramp
        acc.terrainZones.add(
            TerrainZone(
                x = -990f,
                y = 640f,
                w = 870f,
                h = 128f,
                heightLevel = 0,
                kind = TerrainKind.LIMESTONE_BANK,
                colorHex = 0xFF1D2927,
                label = "SOUTHWEST LIMESTONE BLUFF"
            )
        )
        acc.terrainZones.add(
            TerrainZone(
                x = 120f,
                y = 640f,
                w = 870f,
                h = 128f,
                heightLevel = 0,
                kind = TerrainKind.LIMESTONE_BANK,
                colorHex = 0xFF1D2927,
                label = "SOUTHEAST LIMESTONE BLUFF"
            )
        )

        // Graded Stone & Gravel Access Ramp climbing up through the South Gate
        acc.terrainRamps.add(
            TerrainRamp(
                x = -92f,
                y = 618f,
                w = 184f,
                h = 162f,
                fromHeight = 0,
                toHeight = 1,
                isStairs = false
            )
        )

        // Packed Earth & Crushed Gravel Outpost Footpaths (ZERO city asphalt roads or yellow lane stripes!)
        // Main gravel spine climbing from South Gate through Central HQ Plaza up to Cantine
        acc.groundPatches.add(GroundPatch(-54f, -490f, 108f, 1270f, 0xFF3A332A, "CAMP_TRAIL"))
        // Valley approach dirt trail winding down outside South Gate ramp
        acc.groundPatches.add(GroundPatch(-46f, 780f, 92f, 260f, 0xFF2D2821, "CAMP_TRAIL"))
        // Cross-camp gravel paths linking Barracks, Armory, Infirmary & East Wing
        acc.groundPatches.add(GroundPatch(-460f, -54f, 920f, 108f, 0xFF3A332A, "CAMP_TRAIL"))
        acc.groundPatches.add(GroundPatch(-475f, -310f, 84f, 620f, 0xFF352F27, "CAMP_TRAIL"))
        acc.groundPatches.add(GroundPatch(395f, -310f, 84f, 560f, 0xFF352F27, "CAMP_TRAIL"))
        // Narrow inner perimeter dirt patrol track along the timber palisade wall
        acc.groundPatches.add(GroundPatch(-890f, -695f, 1780f, 44f, 0xFF2B2721, "CAMP_TRAIL"))
        acc.groundPatches.add(GroundPatch(-890f, 610f, 1780f, 44f, 0xFF2B2721, "CAMP_TRAIL"))
        acc.groundPatches.add(GroundPatch(-905f, -695f, 44f, 1349f, 0xFF2B2721, "CAMP_TRAIL"))
        acc.groundPatches.add(GroundPatch(861f, -695f, 44f, 1349f, 0xFF2B2721, "CAMP_TRAIL"))

        // Drifting coastal mist & windblown pine needles outside the hilltop compound
        acc.ambientZones.add(AmbientZone(RectF(-1280f, -1040f, 2560f, 2140f), AmbientZoneKind.WOODS))

        // Fortified Timber Palisade & Steel-Braced Perimeter Fence (matches splash compound walls)
        val palisadeDark = 0xFF4A3728
        val palisadeLog = 0xFF6B503B
        acc.walls.add(WallSegment(fL, fT, fR - fL, 14f, 0, palisadeDark, palisadeLog, isFence = true))
        acc.walls.add(WallSegment(fL, fT, 14f, fB - fT, 0, palisadeDark, palisadeLog, isFence = true))
        acc.walls.add(WallSegment(fR - 14f, fT, 14f, fB - fT, 0, palisadeDark, palisadeLog, isFence = true))
        acc.walls.add(WallSegment(fL, fB - 14f, 890f, 14f, 0, palisadeDark, palisadeLog, isFence = true))
        acc.walls.add(WallSegment(-110f, fB - 16f, 220f, 16f, 0, 0xFF614223, 0xFFE5A024, isFence = true))
        acc.walls.add(WallSegment(110f, fB - 14f, 890f, 14f, 0, palisadeDark, palisadeLog, isFence = true))

        // Fortified Sandbag Gate Chicanes & South Foot-Patrol Gate
        acc.props.add(PropObj("oz_sb_gate_l", -198f, 688f, 88f, 20f, 0, 0xFF7D6B51, 0.45f, "sandbag"))
        acc.props.add(PropObj("oz_sb_gate_r", 110f, 688f, 88f, 20f, 0, 0xFF7D6B51, 0.45f, "sandbag"))
        acc.stations.add(
            InteractiveStation(
                id = "st_exit_gate",
                kind = StationKind.EXIT_GATE,
                x = 0f,
                y = 706f,
                label = if (safeHelipadLevel >= 1) "SOUTH GATE // FOOT EXFIL" else "SOUTH GATE // DEPLOY TO DELAINE",
                colorHex = 0xFFF59E0B
            )
        )

        // Patrol waypoints along ridge perimeter road
        acc.patrolWaypoints.addAll(
            listOf(
                -330f to -30f,
                310f to -30f,
                310f to 160f,
                -330f to 160f,
                -700f to -590f,
                700f to -590f,
                700f to 540f,
                -700f to 540f
            )
        )

        val survivorNpcs = mutableListOf<NpcSurvivorPlacement>()

        // Build all placed Buildings, Stations, Bought Bunks & Custom Structures from BaseCustomLayout
        for (piece in layout.pieces) {
            val rot = ((piece.rotationDeg % 360) + 360) % 360
            val swapAxes = rot == 90 || rot == 270
            when (piece.kind) {
                BaseModuleKind.BARRACKS -> {
                    val barracksBp = MultiFloorBlueprint(
                        accum = acc,
                        cx = piece.x,
                        cy = piece.y,
                        w = 340f,
                        h = 410f,
                        rotDeg = rot,
                        name = "TIMBER BARRACKS (LV.$safeBunkLevel)",
                        extColorHex = 0xFF564232,
                        roofColorHex = 0xFF28312E,
                        accessibleFloors = listOf(0),
                        miniColorHex = 0xFF34D399
                    )
                    acc.groundPatches.add(GroundPatch(piece.x - 186f, piece.y - 220f, 372f, 440f, 0xFF342E27, "CAMP_TRAIL"))
                    barracksBp.addFloor(0, -162f, -197f, 324f, 394f, 0xFF5D4633, 0xFF473424)
                    barracksBp.addWall(0, -170f, -205f, 170f, -205f, listOf(WallFeature(-60f, 64f, isWindow = true), WallFeature(60f, 64f, isWindow = true)))
                    barracksBp.addWall(0, -170f, 205f, 170f, 205f, listOf(WallFeature(0f, 80f, isDoubleDoor = true, swingDir = 1f, startOpen = true)))
                    barracksBp.addWall(0, -170f, -205f, -170f, 205f, listOf(WallFeature(-80f, 60f, isWindow = true), WallFeature(80f, 60f, isWindow = true)))
                    barracksBp.addWall(0, 170f, -205f, 170f, 205f, listOf(WallFeature(0f, 96f, isDoubleDoor = true, swingDir = 1f, startOpen = true)))
                    barracksBp.addProp(0, 56f, -35f, 76f, 42f, 0xFF6B4C35, 0.44f, "mess_table")
                    barracksBp.addProp(0, 62f, -155f, 38f, 16f, 0xFF1E293B, 0.58f, "tv", tvPoweredOn = true)
                    for (i in 0 until 5) {
                        val by = -172f + i * 72f
                        barracksBp.addFloor(0, -150f, by, 96f, 40f, 0xFF3A2D22, 0xFF2A2018)
                    }
                    acc.streetLights.add(StreetLightObj(piece.x + 190f, piece.y + 20f))
                }

                BaseModuleKind.ARMORY -> {
                    val armoryBp = MultiFloorBlueprint(
                        accum = acc,
                        cx = piece.x,
                        cy = piece.y,
                        w = 380f,
                        h = 260f,
                        rotDeg = rot,
                        name = "ARMORY & STORAGE DEPOT (LV.$safeStorageLevel)",
                        extColorHex = 0xFF3C444E,
                        roofColorHex = 0xFF212830,
                        accessibleFloors = listOf(0),
                        miniColorHex = 0xFFF59E0B
                    )
                    acc.groundPatches.add(GroundPatch(piece.x - 204f, piece.y - 144f, 408f, 288f, 0xFF342E27, "CAMP_TRAIL"))
                    armoryBp.addFloor(0, -182f, -122f, 364f, 244f, 0xFF414A54, 0xFF313942)
                    armoryBp.addWall(0, -190f, -130f, 190f, -130f, listOf(WallFeature(0f, 96f, isDoubleDoor = true, swingDir = -1f, startOpen = true)))
                    armoryBp.addWall(0, -190f, 130f, 190f, 130f, listOf(WallFeature(-70f, 64f, isWindow = true), WallFeature(70f, 64f, isWindow = true)))
                    armoryBp.addWall(0, -190f, -130f, -190f, 130f, listOf(WallFeature(0f, 64f, isWindow = true)))
                    armoryBp.addWall(0, 190f, -130f, 190f, 130f, listOf(WallFeature(0f, 64f, swingDir = 1f, startOpen = true)))
                    if (safeStorageLevel >= 2) {
                        armoryBp.addProp(0, -56f, 56f, 52f, 34f, 0xFF475569, 0.60f, "storage_crate_extra")
                    }
                    if (safeStorageLevel >= 4) {
                        armoryBp.addProp(0, 14f, 56f, 52f, 34f, 0xFF364A33, 0.68f, "storage_crate_heavy")
                    }
                    survivorNpcs.add(NpcSurvivorPlacement("npc_vance", piece.x, piece.y - 163f, -1.57f))
                    acc.streetLights.add(StreetLightObj(piece.x + 200f, piece.y - 125f))
                }

                BaseModuleKind.INFIRMARY -> {
                    val medBp = MultiFloorBlueprint(
                        accum = acc,
                        cx = piece.x,
                        cy = piece.y,
                        w = 320f,
                        h = 240f,
                        rotDeg = rot,
                        name = "FIELD INFIRMARY CABIN",
                        extColorHex = 0xFF3C5458,
                        roofColorHex = 0xFF233438,
                        accessibleFloors = listOf(0),
                        miniColorHex = 0xFF38BDF8
                    )
                    acc.groundPatches.add(GroundPatch(piece.x - 174f, piece.y - 134f, 348f, 268f, 0xFF342E27, "CAMP_TRAIL"))
                    medBp.addFloor(0, -152f, -112f, 304f, 224f, 0xFFD0DCE3, 0xFFAEC0CB)
                    medBp.addWall(0, -160f, -120f, 160f, -120f, listOf(WallFeature(-50f, 60f, isWindow = true), WallFeature(50f, 60f, isWindow = true)))
                    medBp.addWall(0, 160f, -120f, 160f, 120f, listOf(WallFeature(0f, 60f, isWindow = true)))
                    medBp.addWall(0, -160f, 120f, 160f, 120f, listOf(WallFeature(0f, 84f, isDoubleDoor = true, swingDir = -1f, startOpen = true)))
                    medBp.addWall(0, -160f, -120f, -160f, 120f, listOf(WallFeature(0f, 60f, swingDir = -1f, startOpen = true)))
                    medBp.addProp(0, 56f, -22f, 76f, 34f, 0xFF4C7A75, 0.46f, "med_bed_2")
                    medBp.addProp(0, 56f, 48f, 76f, 34f, 0xFF4C7A75, 0.46f, "med_bed_3")
                    medBp.addProp(0, -136f, -92f, 52f, 32f, 0xFFE2E8F0, 0.72f, "med_cabinet")
                    survivorNpcs.add(NpcSurvivorPlacement("npc_morales", piece.x - 120f, piece.y + 155f, 2.1f))
                    acc.streetLights.add(StreetLightObj(piece.x - 165f, piece.y + 140f))
                }

                BaseModuleKind.CANTINE -> {
                    val dw = if (swapAxes) 260f else 460f
                    val dh = if (swapAxes) 460f else 260f
                    acc.groundPatches.add(GroundPatch(piece.x - dw / 2f, piece.y - dh / 2f, dw, dh, 0xFF4A3D31, "CANTINE_PATIO"))
                    acc.props.add(PropObj("${piece.id}_cnt", piece.x - 195f, piece.y - 115f, 160f, 34f, 0, 0xFF5E4632, 0.60f, "counter"))
                    acc.props.add(PropObj("${piece.id}_tbl1", piece.x - 185f, piece.y - 20f, 92f, 40f, 0, 0xFF6B4C35, 0.44f, "mess_table"))
                    acc.props.add(PropObj("${piece.id}_tbl2", piece.x - 40f, piece.y - 20f, 92f, 40f, 0, 0xFF6B4C35, 0.44f, "mess_table"))
                    acc.props.add(PropObj("${piece.id}_tbl3", piece.x - 185f, piece.y + 60f, 92f, 40f, 0, 0xFF6B4C35, 0.44f, "mess_table"))
                    acc.props.add(PropObj("${piece.id}_tbl4", piece.x - 40f, piece.y + 60f, 92f, 40f, 0, 0xFF6B4C35, 0.44f, "mess_table"))
                    survivorNpcs.add(NpcSurvivorPlacement("npc_miller", piece.x - 110f, piece.y - 65f, 1.57f))
                    acc.streetLights.add(StreetLightObj(piece.x, piece.y - dh / 2f + 30f))
                }

                BaseModuleKind.HQ_PAVILION -> {
                    val dw = if (swapAxes) 340f else 410f
                    val dh = if (swapAxes) 410f else 340f
                    acc.groundPatches.add(GroundPatch(piece.x - dw / 2f, piece.y - dh / 2f, dw, dh, 0xFF41362B, "HQ_COMMAND_DECK"))
                    acc.streetLights.add(StreetLightObj(piece.x - 195f, piece.y - 165f))
                    acc.streetLights.add(StreetLightObj(piece.x + 195f, piece.y - 165f))
                    acc.streetLights.add(StreetLightObj(piece.x - 195f, piece.y + 165f))
                    acc.streetLights.add(StreetLightObj(piece.x + 195f, piece.y + 165f))
                }

                BaseModuleKind.HELIPAD -> {
                    val dw = if (swapAxes) 420f else 400f
                    val dh = if (swapAxes) 400f else 420f
                    acc.groundPatches.add(GroundPatch(piece.x - dw / 2f, piece.y - dh / 2f, dw, dh, 0xFF212832, "HELIPAD_APRON"))
                    acc.props.add(PropObj("${piece.id}_sbn", piece.x - 175f, piece.y - dh / 2f - 18f, 110f, 18f, 0, 0xFF7D6B51, 0.42f, "sandbag"))
                    acc.props.add(PropObj("${piece.id}_sbs", piece.x - 175f, piece.y + dh / 2f + 10f, 110f, 18f, 0, 0xFF7D6B51, 0.42f, "sandbag"))
                    acc.stations.add(InteractiveStation("st_helipad", StationKind.HELIPAD, piece.x + 8f, piece.y + 8f, "HELIPAD // DEPLOY TO ZONES", 0xFF34D399))
                    survivorNpcs.add(NpcSurvivorPlacement("npc_reyes", piece.x - 162f, piece.y + 8f, 3.14f))
                    acc.streetLights.add(StreetLightObj(piece.x - dw / 2f + 8f, piece.y - dh / 2f + 16f))
                    acc.streetLights.add(StreetLightObj(piece.x + dw / 2f - 8f, piece.y + dh / 2f - 16f))
                }

                BaseModuleKind.LAPTOP -> {
                    val pw = if (swapAxes) 38f else 110f
                    val ph = if (swapAxes) 110f else 38f
                    acc.props.add(PropObj(piece.id, piece.x - pw / 2f, piece.y - ph / 2f, pw, ph, 0, 0xFF2B353B, 0.58f, "command_desk"))
                    acc.stations.add(InteractiveStation("st_laptop", StationKind.LAPTOP, piece.x, piece.y + ph / 2f + 16f, "HQ TERMINAL & MISSIONS", 0xFFF59E0B))
                }

                BaseModuleKind.BUNK -> {
                    val pw = if (swapAxes) 38f else 92f
                    val ph = if (swapAxes) 92f else 38f
                    acc.props.add(PropObj("bunk_${piece.slotNumber}_${piece.id}", piece.x - pw / 2f, piece.y - ph / 2f, pw, ph, 0, 0xFF3E523A, 0.50f, "bunk_${piece.slotNumber}"))
                    acc.props.add(PropObj("locker_${piece.slotNumber}_${piece.id}", piece.x + pw / 2f + 6f, piece.y - 14f, 28f, 28f, 0, 0xFF475569, 0.38f, "locker_${piece.slotNumber}"))
                    acc.stations.add(InteractiveStation("st_bunk_${piece.slotNumber}", StationKind.BUNK, piece.x, piece.y + ph / 2f + 16f, "BOUGHT BUNK #${piece.slotNumber}", 0xFFA7F3D0))
                }

                BaseModuleKind.WORKBENCH -> {
                    val pw = if (swapAxes) 32f else 88f
                    val ph = if (swapAxes) 88f else 32f
                    acc.props.add(PropObj(piece.id, piece.x - pw / 2f, piece.y - ph / 2f, pw, ph, 0, 0xFF69482C, 0.58f, "workbench"))
                    acc.stations.add(InteractiveStation("st_workbench", StationKind.WORKBENCH, piece.x, piece.y + ph / 2f + 16f, "CRAFTING WORKBENCH", 0xFFFB923C))
                }

                BaseModuleKind.STORAGE_DEPOT -> {
                    val pw = if (swapAxes) 40f else 90f
                    val ph = if (swapAxes) 90f else 40f
                    acc.props.add(PropObj(piece.id, piece.x - pw / 2f, piece.y - ph / 2f, pw, ph, 0, 0xFF4D5E47, 0.64f, "storage_depot"))
                    acc.stations.add(InteractiveStation("st_storage", StationKind.STORAGE_DEPOT, piece.x, piece.y + ph / 2f + 16f, "STORAGE DEPOT (LV.$safeStorageLevel)", 0xFF38BDF8))
                }

                BaseModuleKind.MARKET -> {
                    val pw = if (swapAxes) 34f else 68f
                    val ph = if (swapAxes) 68f else 34f
                    acc.props.add(PropObj(piece.id, piece.x - pw / 2f, piece.y - ph / 2f, pw, ph, 0, 0xFF3E2C59, 0.64f, "market_terminal"))
                    acc.stations.add(InteractiveStation("st_market", StationKind.MARKET, piece.x, piece.y + ph / 2f + 16f, "LIVE MARKET TERMINAL", 0xFFC084FC))
                }

                BaseModuleKind.CD_RACK -> {
                    val pw = if (swapAxes) 78f else 42f
                    val ph = if (swapAxes) 42f else 78f
                    acc.props.add(PropObj(piece.id, piece.x - pw / 2f, piece.y - ph / 2f, pw, ph, 0, 0xFF5E3F29, 0.74f, "cd_rack"))
                    acc.stations.add(InteractiveStation("st_cd", StationKind.CD_RACK, piece.x - pw / 2f - 18f, piece.y + 12f, "CD RACK & STEREO", 0xFFC084FC))
                }

                BaseModuleKind.MESS_FOOD -> {
                    val pw = if (swapAxes) 34f else 84f
                    val ph = if (swapAxes) 84f else 34f
                    acc.props.add(PropObj(piece.id, piece.x - pw / 2f, piece.y - ph / 2f, pw, ph, 0, 0xFF695035, 0.58f, "food_stash"))
                    acc.stations.add(InteractiveStation("st_food", StationKind.MESS_FOOD, piece.x, piece.y + ph / 2f + 18f, "CANTINE RATIONS", 0xFF34D399))
                }

                BaseModuleKind.WATER_COOLER -> {
                    acc.props.add(PropObj(piece.id, piece.x - 19f, piece.y - 19f, 38f, 38f, 0, 0xFF2872A8, 0.68f, "water_cooler"))
                    acc.stations.add(InteractiveStation("st_water", StationKind.WATER_COOLER, piece.x, piece.y + 34f, "CANTINE WATER", 0xFF38BDF8))
                }

                BaseModuleKind.INFIRMARY_BED -> {
                    val pw = if (swapAxes) 34f else 76f
                    val ph = if (swapAxes) 76f else 34f
                    acc.props.add(PropObj(piece.id, piece.x - pw / 2f, piece.y - ph / 2f, pw, ph, 0, 0xFF4C7A75, 0.46f, "med_bed_1"))
                    acc.stations.add(InteractiveStation("st_infirmary", StationKind.INFIRMARY_BED, piece.x, piece.y + ph / 2f + 22f, "FIELD INFIRMARY", 0xFFFCA5A5))
                }

                BaseModuleKind.CUSTOM_WALL -> {
                    val ww = if (swapAxes) 12f else 120f
                    val wh = if (swapAxes) 120f else 12f
                    acc.walls.add(WallSegment(piece.x - ww / 2f, piece.y - wh / 2f, ww, wh, 0, 0xFF454E59, 0xFF64707D))
                }

                BaseModuleKind.CUSTOM_FENCE -> {
                    val ww = if (swapAxes) 10f else 120f
                    val wh = if (swapAxes) 120f else 10f
                    acc.walls.add(WallSegment(piece.x - ww / 2f, piece.y - wh / 2f, ww, wh, 0, palisadeDark, palisadeLog, isFence = true))
                }

                BaseModuleKind.CUSTOM_SANDBAG -> {
                    val pw = if (swapAxes) 18f else 92f
                    val ph = if (swapAxes) 92f else 18f
                    acc.props.add(PropObj(piece.id, piece.x - pw / 2f, piece.y - ph / 2f, pw, ph, 0, 0xFF7D6B51, 0.42f, "sandbag"))
                }

                BaseModuleKind.CUSTOM_FLOODLIGHT -> {
                    acc.streetLights.add(StreetLightObj(piece.x, piece.y))
                }

                BaseModuleKind.CUSTOM_TABLE -> {
                    val pw = if (swapAxes) 40f else 92f
                    val ph = if (swapAxes) 92f else 40f
                    acc.props.add(PropObj(piece.id, piece.x - pw / 2f, piece.y - ph / 2f, pw, ph, 0, 0xFF6B4C35, 0.44f, "mess_table"))
                }

                BaseModuleKind.CUSTOM_CRATE -> {
                    val pw = if (swapAxes) 34f else 52f
                    val ph = if (swapAxes) 52f else 34f
                    acc.props.add(PropObj(piece.id, piece.x - pw / 2f, piece.y - ph / 2f, pw, ph, 0, 0xFF475569, 0.62f, "storage_crate_extra"))
                }

                BaseModuleKind.CUSTOM_FIREPIT -> {
                    acc.props.add(PropObj(piece.id, piece.x - 23f, piece.y - 23f, 46f, 46f, 0, 0xFF4B5563, 0.28f, "camp_firepit"))
                }

                BaseModuleKind.CUSTOM_TV -> {
                    val pw = if (swapAxes) 16f else 38f
                    val ph = if (swapAxes) 38f else 16f
                    acc.props.add(PropObj(piece.id, piece.x - pw / 2f, piece.y - ph / 2f, pw, ph, 0, 0xFF1E293B, 0.62f, "tv", tvPoweredOn = true))
                }

                BaseModuleKind.CUSTOM_WATCHTOWER -> {
                    acc.props.add(PropObj(piece.id, piece.x - 44f, piece.y - 44f, 88f, 88f, 0, 0xFF5A422F, 1.72f, "watchtower"))
                    acc.streetLights.add(StreetLightObj(piece.x, piece.y + 42f))
                }
            }
        }

        // Ensure default survivor NPCs exist even if player built from empty scratch
        if (survivorNpcs.none { it.npcId == "npc_vance" }) {
            val wb = acc.stations.firstOrNull { it.kind == StationKind.WORKBENCH }
            survivorNpcs.add(NpcSurvivorPlacement("npc_vance", (wb?.x ?: -160f) + 46f, wb?.y ?: 120f, -1.57f))
        }
        if (survivorNpcs.none { it.npcId == "npc_morales" }) {
            val inf = acc.stations.firstOrNull { it.kind == StationKind.INFIRMARY_BED }
            survivorNpcs.add(NpcSurvivorPlacement("npc_morales", (inf?.x ?: 180f) + 46f, inf?.y ?: -120f, 2.1f))
        }
        if (survivorNpcs.none { it.npcId == "npc_miller" }) {
            val fd = acc.stations.firstOrNull { it.kind == StationKind.MESS_FOOD }
            survivorNpcs.add(NpcSurvivorPlacement("npc_miller", (fd?.x ?: 60f) - 46f, fd?.y ?: -220f, 1.57f))
        }
        if (survivorNpcs.none { it.npcId == "npc_reyes" }) {
            val hp = acc.stations.firstOrNull { it.kind == StationKind.HELIPAD }
            if (hp != null) {
                survivorNpcs.add(NpcSurvivorPlacement("npc_reyes", hp.x - 110f, hp.y, 3.14f))
            } else {
                survivorNpcs.add(NpcSurvivorPlacement("npc_reyes", 96f, 650f, 2.5f))
            }
        }

        // Gnarled Florida live oaks & tall pines along the ridge and southern mist valley
        val treeSpots = listOf(
            -1100f to -820f, -1110f to -640f, -1120f to -450f, -1110f to 0f, -1120f to 440f, -1090f to 830f,
            1100f to -810f, 1110f to -640f, 1120f to -390f, 1110f to 40f, 1120f to 460f, 1090f to 830f,
            -650f to -880f, -200f to -890f, 240f to -880f, 680f to -890f,
            -780f to 870f, -430f to 890f, -210f to 870f, 210f to 870f, 440f to 890f, 770f to 870f,
            -810f to 40f, 810f to -40f, -840f to 520f, 840f to 520f
        )
        treeSpots.forEachIndexed { idx, (tx, ty) ->
            acc.props.add(
                PropObj(
                    id = "oz_tree_$idx",
                    x = tx - 26f,
                    y = ty - 26f,
                    w = 52f,
                    h = 52f,
                    floor = 0,
                    colorHex = 0xFF28432D,
                    heightScale = 1.32f,
                    propType = "tree"
                )
            )
        }

        // Gate perimeter lanterns
        acc.streetLights.add(StreetLightObj(-130f, 665f))
        acc.streetLights.add(StreetLightObj(130f, 665f))

        MultiFloorBlueprint.filterDoorClearance(acc)

        return WorldMapData(
            mapId = GameMapId.OUTPOST_ZERO,
            spawnX = 0f,
            spawnY = 24f,
            worldBounds = RectF(fL + 16f, fT + 16f, (fR - fL) - 32f, (fB - fT) - 32f),
            roads = acc.roads,
            curvedRoads = acc.curvedRoads,
            roadMarkings = acc.roadMarkings,
            sidewalks = acc.sidewalks,
            groundPatches = acc.groundPatches,
            buildings = acc.buildings,
            roomFloors = acc.roomFloors,
            walls = acc.walls,
            doors = acc.doors,
            stairwells = acc.stairwells,
            props = acc.props,
            lootContainers = acc.lootContainers,
            streetLights = acc.streetLights,
            stations = acc.stations,
            survivorNpcs = survivorNpcs,
            extractionZones = acc.extractionZones,
            ambientZones = acc.ambientZones,
            patrolWaypoints = acc.patrolWaypoints,
            terrainZones = acc.terrainZones,
            terrainRamps = acc.terrainRamps,
            initialZombies = emptyList()
        )
    }
}
