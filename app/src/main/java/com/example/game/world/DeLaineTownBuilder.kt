package com.example.game.world

import com.example.game.model.CraftingMaterial
import com.example.game.model.DoorStyle
import com.example.game.model.ElevatedWalkway
import com.example.game.model.GroundPatch
import com.example.game.model.InventoryItem
import com.example.game.model.LootType
import com.example.game.model.PropObj
import com.example.game.model.TerrainKind
import com.example.game.model.TerrainRamp
import com.example.game.model.TerrainZone
import kotlin.random.Random

internal object DeLaineTownBuilder {

    fun matItem(
        uid: (String) -> String,
        mat: CraftingMaterial,
        qty: Int
    ): InventoryItem = InventoryItem(
        uid = uid(mat.key),
        type = LootType.MATERIAL,
        name = "${mat.displayName} x$qty",
        detail = mat.oakridgeSources,
        value = qty,
        materialKey = mat.key,
        quantity = qty
    )

    fun buildDowntownDeLaine(acc: MutableWorldAccum, uid: (String) -> String) {
        // 1. Valen County Courthouse (raised stone terrace Plaza, multi-floor [-1, 0, 1, 2])
        acc.terrainZones.add(
            TerrainZone(-650f, -690f, 570f, 500f, 1, TerrainKind.TERRACE_PLAZA, 0xFF34313A, "COURTHOUSE PLAZA")
        )
        // Wide front & east stone staircases down from Courthouse Plaza (1 -> 0)
        acc.terrainRamps.add(TerrainRamp(-425f, -212f, 140f, 44f, 0, 1, isStairs = true))
        acc.terrainRamps.add(TerrainRamp(-102f, -490f, 44f, 120f, 0, 1, isStairs = true))

        val chFloors = listOf(-1, 0, 1, 2)
        val ch = MultiFloorBlueprint(
            accum = acc,
            cx = -355f,
            cy = -440f,
            w = 500f,
            h = 360f,
            rotDeg = 0,
            name = "VALEN COUNTY COURTHOUSE",
            extColorHex = 0xFF6B5B53,
            roofColorHex = 0xFF28232E,
            accessibleFloors = chFloors,
            roofFloorIndex = 2,
            miniColorHex = 0xFFE5A024,
            groundHeight = 1
        )
        ch.addSidewalk(-268f, -198f, 536f, 396f)
        for (fIdx in chFloors) {
            val isRoof = fIdx == 2
            val fc = when {
                isRoof -> 0xFF28242D
                fIdx == -1 -> 0xFF232028
                else -> 0xFF4F463E
            }
            val sc = if (isRoof) 0xFF1E1A22 else 0xFF3A332D
            ch.addFloor(fIdx, -242f, -172f, 484f, 344f, fc, sc)
            if (isRoof) {
                ch.addProp(fIdx, -170f, -120f, 64f, 46f, 0xFF474154, 0.65f, "hvac")
                ch.addProp(fIdx, 95f, -120f, 54f, 42f, 0xFF474154, 0.6f, "hvac")
                ch.addWall(fIdx, -250f, -180f, 250f, -180f)
                ch.addWall(fIdx, -250f, -180f, -250f, 180f)
                ch.addWall(fIdx, 250f, -180f, 250f, 180f)
                ch.addWall(fIdx, -250f, 180f, 250f, 180f)
            } else {
                // Interior partition walls (Clerk foyer vs Courtroom / Archive)
                ch.addWall(fIdx, -250f, 20f, 250f, 20f, listOf(WallFeature(-95f, 58f, swingDir = 1f, startOpen = true), WallFeature(95f, 58f, swingDir = -1f, startOpen = false)))
                ch.addWall(fIdx, 0f, -180f, 0f, 20f, listOf(WallFeature(0f, 54f, swingDir = 1f, startOpen = false)))
                ch.addProp(fIdx, -200f, -135f, 92f, 28f, 0xFF4D3828, 0.56f, "counter")
                ch.addProp(fIdx, 55f, -140f, 70f, 34f, 0xFF3E2D20, 0.50f, "desk")
                ch.addProp(fIdx, -195f, 75f, 72f, 30f, 0xFF4A382C, 0.48f, "desk")
                if (fIdx == 0) {
                    ch.addProp(0, 140f, 64f, 38f, 20f, 0xFF1E293B, 0.60f, "tv", tvPoweredOn = true)
                }
                ch.addWall(fIdx, -250f, -180f, 250f, -180f, listOf(WallFeature(-110f, 68f, isWindow = true), WallFeature(110f, 68f, isWindow = true)))
                ch.addWall(fIdx, -250f, -180f, -250f, 180f, listOf(WallFeature(0f, 76f, isWindow = true)))
                ch.addWall(fIdx, 250f, -180f, 250f, 180f, listOf(WallFeature(0f, 76f, isWindow = true)))
                ch.addWall(
                    fIdx,
                    -250f,
                    180f,
                    250f,
                    180f,
                    if (fIdx == 0) listOf(WallFeature(0f, 96f, isDoubleDoor = true, swingDir = -1f, startOpen = false, doorStyle = DoorStyle.STEEL_SECURITY))
                    else listOf(WallFeature(0f, 96f, isWindow = true))
                )
            }
        }
        ch.addStairwell(160f, 118f, 68f, 40f, chFloors)
        ch.addLootSpot(0, -165f, -70f, "County Clerk Supply Cabinets", listOf(
            matItem(uid, CraftingMaterial.ELECTRONIC_PARTS, 3),
            matItem(uid, CraftingMaterial.COPPER_WIRING, 3),
            matItem(uid, CraftingMaterial.INDUSTRIAL_FASTENERS, 4),
            matItem(uid, CraftingMaterial.OPTICAL_GLASS, 2)
        ))
        ch.addLootSpot(1, 140f, -85f, "Judge Chambers Safe (2F)", listOf(
            matItem(uid, CraftingMaterial.FORGED_RECEIVER, 2),
            matItem(uid, CraftingMaterial.TRIGGER_ASSEMBLY, 2),
            matItem(uid, CraftingMaterial.OPTICAL_GLASS, 3),
            matItem(uid, CraftingMaterial.LITHIUM_CELLS, 2)
        ))
        ch.addLootSpot(-1, -155f, 110f, "Courthouse Evidence & Vault (B1)", listOf(
            matItem(uid, CraftingMaterial.KEVLAR_WEAVE, 3),
            matItem(uid, CraftingMaterial.CERAMIC_PLATES, 2),
            matItem(uid, CraftingMaterial.GUNPOWDER, 4),
            matItem(uid, CraftingMaterial.BRASS_CASINGS, 4)
        ))

        // 2. Steve's Music (Historic Woodbine Blvd brick music shop + 2F upstairs studio/apt)
        val stevesFloors = listOf(-1, 0, 1)
        val steves = MultiFloorBlueprint(
            accum = acc,
            cx = 260f,
            cy = 270f,
            w = 360f,
            h = 300f,
            rotDeg = 0,
            name = "STEVE'S MUSIC (WOODBINE BLVD)",
            extColorHex = 0xFF7C3F36,
            roofColorHex = 0xFF2B2022,
            accessibleFloors = stevesFloors,
            miniColorHex = 0xFFFB923C
        )
        steves.addSidewalk(-196f, -166f, 392f, 332f)
        for (fIdx in stevesFloors) {
            val fc = when (fIdx) {
                -1 -> 0xFF232026
                0 -> 0xFF5A3F2B
                else -> 0xFF4E3929
            }
            val sc = if (fIdx == -1) 0xFF19171C else 0xFF3E2A1B
            steves.addFloor(fIdx, -172f, -142f, 344f, 284f, fc, sc)
            if (fIdx == 0) {
                // Showroom vs Back Repair Workshop partition
                steves.addWall(0, -180f, 38f, 180f, 38f, listOf(WallFeature(-60f, 54f, swingDir = 1f, startOpen = true, doorStyle = DoorStyle.HINGED_WOOD)))
                // Steve's Music hyper-detailed props (clear of entrance & back-room door clearance)
                steves.addProp(0, -112f, -132f, 118f, 24f, 0xFF3D2719, 0.70f, "guitar_wall")
                steves.addProp(0, 46f, -126f, 96f, 32f, 0xFF2D1E17, 0.58f, "music_counter")
                steves.addProp(0, -22f, -86f, 68f, 52f, 0xFF7F1D1D, 0.55f, "drum_kit")
                steves.addProp(0, 128f, -55f, 36f, 56f, 0xFF1F2937, 0.72f, "amp_stack")
                steves.addProp(0, 68f, -52f, 38f, 20f, 0xFF1E293B, 0.58f, "tv", tvPoweredOn = true)
                steves.addProp(0, 52f, -12f, 24f, 44f, 0xFF312E81, 0.65f, "cd_rack")
                steves.addProp(0, 70f, 66f, 84f, 30f, 0xFF4A3525, 0.58f, "workbench")
            } else if (fIdx == 1) {
                // Upstairs Owner Loft Apartment & Jam Studio
                steves.addWall(1, -10f, -150f, -10f, 150f, listOf(WallFeature(0f, 54f, swingDir = 1f, startOpen = false)))
                steves.addProp(1, -156f, -124f, 62f, 36f, 0xFF473528, 0.50f, "bed")
                steves.addProp(1, 30f, -124f, 46f, 28f, 0xFF374151, 0.62f, "stove")
                steves.addProp(1, 86f, -124f, 38f, 28f, 0xFF64748B, 0.72f, "fridge")
                steves.addProp(1, 130f, -124f, 36f, 26f, 0xFFCBD5E1, 0.55f, "sink")
                steves.addProp(1, 46f, -24f, 70f, 34f, 0xFF4C2828, 0.48f, "sofa")
                steves.addProp(1, 56f, 36f, 40f, 22f, 0xFF1E293B, 0.58f, "tv", tvPoweredOn = true)
            } else {
                steves.addProp(-1, -150f, -110f, 82f, 30f, 0xFF3F3832, 0.6f, "shelf")
            }

            if (fIdx == 0) {
                steves.addWall(0, -180f, -150f, 180f, -150f, listOf(WallFeature(-105f, 54f, isWindow = true), WallFeature(105f, 54f, isWindow = true)))
                steves.addWall(0, -180f, -150f, -180f, 150f, listOf(WallFeature(-55f, 64f, isDoubleDoor = true, swingDir = 1f, startOpen = false, doorStyle = DoorStyle.HINGED_WOOD), WallFeature(65f, 56f, isWindow = true)))
                steves.addWall(0, 180f, -150f, 180f, 150f, listOf(WallFeature(0f, 52f, isWindow = true)))
                steves.addWall(0, -180f, 150f, 180f, 150f, listOf(WallFeature(110f, 52f, swingDir = -1f, startOpen = false, doorStyle = DoorStyle.STEEL_SECURITY)))
            } else {
                steves.addWall(fIdx, -180f, -150f, 180f, -150f, listOf(WallFeature(0f, 68f, isWindow = true)))
                steves.addWall(fIdx, -180f, -150f, -180f, 150f, listOf(WallFeature(0f, 64f, isWindow = true)))
                steves.addWall(fIdx, 180f, -150f, 180f, 150f, listOf(WallFeature(0f, 64f, isWindow = true)))
                steves.addWall(fIdx, -180f, 150f, 180f, 150f)
            }
        }
        steves.addStairwell(-160f, 88f, 68f, 40f, stevesFloors)
        steves.addLootSpot(0, 96f, -96f, "Steve's Pedal & Cable Counter", listOf(
            matItem(uid, CraftingMaterial.COPPER_WIRING, 5),
            matItem(uid, CraftingMaterial.ELECTRONIC_PARTS, 4),
            matItem(uid, CraftingMaterial.LITHIUM_CELLS, 3),
            matItem(uid, CraftingMaterial.INDUSTRIAL_FASTENERS, 3),
            matItem(uid, CraftingMaterial.WOOD_FURNITURE, 3)
        ))
        steves.addLootSpot(1, -90f, 70f, "Upstairs Amp & Solder Bench (2F)", listOf(
            matItem(uid, CraftingMaterial.OPTICAL_GLASS, 2),
            matItem(uid, CraftingMaterial.ELECTRONIC_PARTS, 3),
            matItem(uid, CraftingMaterial.TACTICAL_CLOTH, 2),
            matItem(uid, CraftingMaterial.SCRAP_METAL, 3)
        ))

        // 3. Olympia Theatre (Historic Downtown Cinema with Lobby, Auditorium & Projection Booth)
        val theatreFloors = listOf(0, 1)
        val theatre = MultiFloorBlueprint(
            accum = acc,
            cx = 360f,
            cy = -360f,
            w = 460f,
            h = 340f,
            rotDeg = 0,
            name = "OLYMPIA THEATRE",
            extColorHex = 0xFF5A2A34,
            roofColorHex = 0xFF23191D,
            accessibleFloors = theatreFloors,
            miniColorHex = 0xFFF43F5E
        )
        theatre.addSidewalk(-246f, -186f, 492f, 372f)
        for (fIdx in theatreFloors) {
            theatre.addFloor(fIdx, -222f, -162f, 444f, 324f, if (fIdx == 0) 0xFF3E1F28 else 0xFF2C232A, 0xFF28131A)
            if (fIdx == 0) {
                theatre.addWall(0, -100f, -170f, -100f, 170f, listOf(WallFeature(0f, 76f, isDoubleDoor = true, swingDir = 1f, startOpen = true)))
                theatre.addProp(0, -195f, -70f, 48f, 64f, 0xFF472F1B, 0.62f, "counter")
                theatre.addProp(0, -40f, -120f, 170f, 34f, 0xFF581C24, 0.45f, "sofa")
                theatre.addProp(0, -40f, -45f, 170f, 34f, 0xFF581C24, 0.45f, "sofa")
                theatre.addProp(0, -40f, 30f, 170f, 34f, 0xFF581C24, 0.45f, "sofa")
                theatre.addProp(0, 184f, -120f, 26f, 240f, 0xFF0F172A, 0.72f, "tv", tvPoweredOn = false)
            } else {
                theatre.addProp(1, -180f, -40f, 76f, 40f, 0xFF28303B, 0.6f, "command_desk")
            }
            theatre.addWall(fIdx, -230f, -170f, 230f, -170f, listOf(WallFeature(0f, 90f, isWindow = true)))
            theatre.addWall(fIdx, -230f, 170f, 230f, 170f, listOf(WallFeature(0f, 90f, isWindow = true)))
            theatre.addWall(fIdx, 230f, -170f, 230f, 170f)
            theatre.addWall(
                fIdx,
                -230f,
                -170f,
                -230f,
                170f,
                if (fIdx == 0) listOf(WallFeature(0f, 92f, isDoubleDoor = true, swingDir = 1f, startOpen = false, doorStyle = DoorStyle.DOUBLE_SLIDING_GLASS, autoSensor = true))
                else listOf(WallFeature(0f, 72f, isWindow = true))
            )
        }
        theatre.addStairwell(-196f, 108f, 68f, 40f, theatreFloors)
        theatre.addLootSpot(1, -150f, 40f, "Olympia Projector Booth Optics (2F)", listOf(
            matItem(uid, CraftingMaterial.OPTICAL_GLASS, 4),
            matItem(uid, CraftingMaterial.ELECTRONIC_PARTS, 3),
            matItem(uid, CraftingMaterial.LITHIUM_CELLS, 3),
            matItem(uid, CraftingMaterial.COPPER_WIRING, 2)
        ))

        // 4. DeLaine Precinct & Station 1 Firehouse (Steel Security + Wide Roll-Up Apparatus Bays)
        val pfFloors = listOf(0, 1, 2)
        val pf = MultiFloorBlueprint(
            accum = acc,
            cx = -380f,
            cy = 340f,
            w = 520f,
            h = 360f,
            rotDeg = 0,
            name = "DELAINE PRECINCT & FIREHOUSE",
            extColorHex = 0xFF333B48,
            roofColorHex = 0xFF1F232B,
            accessibleFloors = pfFloors,
            roofFloorIndex = 2,
            miniColorHex = 0xFF38BDF8
        )
        pf.addSidewalk(-276f, -196f, 552f, 392f)
        for (fIdx in pfFloors) {
            val isRoof = fIdx == 2
            pf.addFloor(fIdx, -252f, -172f, 504f, 344f, if (isRoof) 0xFF23262E else 0xFF323742, 0xFF252A33)
            if (isRoof) {
                pf.addProp(fIdx, -160f, -110f, 58f, 42f, 0xFF4B5563, 0.62f, "hvac")
                pf.addWall(fIdx, -260f, -180f, 260f, -180f)
                pf.addWall(fIdx, -260f, -180f, -260f, 180f)
                pf.addWall(fIdx, 260f, -180f, 260f, 180f)
                pf.addWall(fIdx, -260f, 180f, 260f, 180f)
            } else {
                pf.addWall(fIdx, 0f, -180f, 0f, 180f, listOf(WallFeature(-40f, 56f, swingDir = 1f, startOpen = false, doorStyle = DoorStyle.STEEL_SECURITY)))
                if (fIdx == 0) {
                    pf.addProp(0, -210f, -135f, 76f, 32f, 0xFF1E293B, 0.56f, "desk")
                    pf.addProp(0, -115f, -135f, 38f, 20f, 0xFF1E293B, 0.58f, "tv", tvPoweredOn = true)
                    pf.addProp(0, 65f, -130f, 92f, 32f, 0xFF7F1D1D, 0.60f, "workbench")
                } else {
                    pf.addProp(1, -190f, -120f, 72f, 32f, 0xFF26303D, 0.5f, "desk")
                    pf.addProp(1, 70f, -120f, 76f, 36f, 0xFF3E4C5E, 0.5f, "bunk_1")
                }
                pf.addWall(fIdx, -260f, -180f, 260f, -180f, listOf(WallFeature(-130f, 70f, isWindow = true), WallFeature(130f, 70f, isWindow = true)))
                pf.addWall(fIdx, -260f, -180f, -260f, 180f, listOf(WallFeature(0f, 72f, isWindow = true)))
                pf.addWall(
                    fIdx,
                    260f,
                    -180f,
                    260f,
                    180f,
                    if (fIdx == 0) listOf(
                        WallFeature(-80f, 84f, doorStyle = DoorStyle.STEEL_SECURITY, swingDir = 1f, startOpen = false),
                        WallFeature(80f, 118f, doorStyle = DoorStyle.GARAGE_ROLLUP, startOpen = false)
                    ) else listOf(WallFeature(0f, 84f, isWindow = true))
                )
                pf.addWall(fIdx, -260f, 180f, 260f, 180f, listOf(WallFeature(0f, 90f, isWindow = true)))
            }
        }
        pf.addStairwell(-228f, 114f, 68f, 40f, pfFloors)
        pf.addLootSpot(0, -190f, 40f, "Precinct Evidence & Kevlar Lockers", listOf(
            matItem(uid, CraftingMaterial.KEVLAR_WEAVE, 4),
            matItem(uid, CraftingMaterial.BRASS_CASINGS, 5),
            matItem(uid, CraftingMaterial.PERCUSSION_PRIMERS, 4),
            matItem(uid, CraftingMaterial.GUN_LUBE, 3),
            matItem(uid, CraftingMaterial.LEAD_PROJECTILES, 4)
        ))
        pf.addLootSpot(1, 140f, 40f, "Station 1 Gear & Armory Vault (2F)", listOf(
            matItem(uid, CraftingMaterial.FORGED_RECEIVER, 3),
            matItem(uid, CraftingMaterial.RIFLED_BARREL, 3),
            matItem(uid, CraftingMaterial.TRIGGER_ASSEMBLY, 3),
            matItem(uid, CraftingMaterial.SUPPRESSOR_BAFFLES, 2),
            matItem(uid, CraftingMaterial.GUNPOWDER, 5),
            matItem(uid, CraftingMaterial.COPPER_JACKETS, 4),
            matItem(uid, CraftingMaterial.CERAMIC_PLATES, 3)
        ))
    }

    fun buildStantonUniversity(acc: MutableWorldAccum, uid: (String) -> String) {
        // Raised Terraced Quad modeled after Stetson's Palm Court / Quad (heightLevel = 1)
        acc.terrainZones.add(
            TerrainZone(-880f, -1800f, 1760f, 880f, 1, TerrainKind.TERRACE_PLAZA, 0xFF283426, "STANTON UNIVERSITY QUAD")
        )
        // Four stone stairways entering Stanton Quad from South (Huron Ave) and East/West
        acc.terrainRamps.add(TerrainRamp(-105f, -955f, 160f, 48f, 0, 1, isStairs = true))
        acc.terrainRamps.add(TerrainRamp(-915f, -1380f, 48f, 140f, 0, 1, isStairs = true))
        acc.terrainRamps.add(TerrainRamp(865f, -1380f, 48f, 140f, 0, 1, isStairs = true))
        acc.terrainRamps.add(TerrainRamp(-105f, -1835f, 160f, 48f, 0, 1, isStairs = true))

        // Quad courtyard stone plaza patch & centerpiece oak/fountain
        acc.groundPatches.add(GroundPatch(-220f, -1320f, 440f, 300f, 0xFF393640, "STANTON QUAD PLAZA"))

        // 1. Founders Clocktower Hall (Georgian Brick Academic Hall)
        val fhFloors = listOf(-1, 0, 1, 2)
        val fh = MultiFloorBlueprint(
            accum = acc,
            cx = 0f,
            cy = -1540f,
            w = 560f,
            h = 340f,
            rotDeg = 0,
            name = "FOUNDERS CLOCKTOWER HALL",
            extColorHex = 0xFF8A4B3D,
            roofColorHex = 0xFF2D2224,
            accessibleFloors = fhFloors,
            roofFloorIndex = 2,
            miniColorHex = 0xFFF87171,
            groundHeight = 1
        )
        fh.addSidewalk(-296f, -186f, 592f, 372f)
        for (fIdx in fhFloors) {
            val isRoof = fIdx == 2
            fh.addFloor(fIdx, -272f, -162f, 544f, 324f, if (isRoof) 0xFF26232B else 0xFF594D43, 0xFF423730)
            if (isRoof) {
                fh.addProp(fIdx, -190f, -110f, 64f, 44f, 0xFF4B5563, 0.65f, "hvac")
                fh.addWall(fIdx, -280f, -170f, 280f, -170f)
                fh.addWall(fIdx, -280f, -170f, -280f, 170f)
                fh.addWall(fIdx, 280f, -170f, 280f, 170f)
                fh.addWall(fIdx, -280f, 170f, 280f, 170f)
            } else {
                fh.addWall(fIdx, -280f, -10f, 280f, -10f, listOf(WallFeature(-120f, 58f, swingDir = 1f, startOpen = true), WallFeature(120f, 58f, swingDir = -1f, startOpen = false)))
                fh.addProp(fIdx, -220f, -135f, 70f, 30f, 0xFF4A382C, 0.48f, "desk")
                fh.addProp(fIdx, 110f, -135f, 70f, 30f, 0xFF4A382C, 0.48f, "desk")
                if (fIdx == 0) {
                    fh.addProp(0, -18f, -135f, 38f, 20f, 0xFF1E293B, 0.58f, "tv", tvPoweredOn = true)
                }
                fh.addWall(fIdx, -280f, -170f, 280f, -170f, listOf(WallFeature(0f, 110f, isWindow = true)))
                fh.addWall(fIdx, -280f, -170f, -280f, 170f, listOf(WallFeature(0f, 76f, isWindow = true)))
                fh.addWall(fIdx, 280f, -170f, 280f, 170f, listOf(WallFeature(0f, 76f, isWindow = true)))
                fh.addWall(
                    fIdx,
                    -280f,
                    170f,
                    280f,
                    170f,
                    if (fIdx == 0) listOf(WallFeature(0f, 96f, isDoubleDoor = true, swingDir = -1f, startOpen = false, doorStyle = DoorStyle.HINGED_WOOD))
                    else listOf(WallFeature(0f, 96f, isWindow = true))
                )
            }
        }
        fh.addStairwell(192f, 108f, 68f, 40f, fhFloors)
        fh.addLootSpot(1, -180f, -75f, "Stanton Chemistry & Optics Lab (2F)", listOf(
            matItem(uid, CraftingMaterial.OPTICAL_GLASS, 4),
            matItem(uid, CraftingMaterial.ELECTRONIC_PARTS, 3),
            matItem(uid, CraftingMaterial.TRAUMA_GAUZE, 2),
            matItem(uid, CraftingMaterial.ANTISEPTIC_SOL, 2)
        ))
        fh.addLootSpot(-1, -160f, 95f, "Steam Tunnel Maintenance Room (B1)", listOf(
            matItem(uid, CraftingMaterial.SCRAP_METAL, 5),
            matItem(uid, CraftingMaterial.COPPER_WIRING, 4),
            matItem(uid, CraftingMaterial.INDUSTRIAL_FASTENERS, 4)
        ))

        // 2. Duval Library (Double Sliding Glass Campus Entrance)
        val libFloors = listOf(0, 1)
        val lib = MultiFloorBlueprint(
            accum = acc,
            cx = -570f,
            cy = -1260f,
            w = 460f,
            h = 340f,
            rotDeg = 0,
            name = "DUVAL CAMPUS LIBRARY",
            extColorHex = 0xFF6B5D52,
            roofColorHex = 0xFF28242B,
            accessibleFloors = libFloors,
            miniColorHex = 0xFF38BDF8,
            groundHeight = 1
        )
        lib.addSidewalk(-246f, -186f, 492f, 372f)
        for (fIdx in libFloors) {
            lib.addFloor(fIdx, -222f, -162f, 444f, 324f, 0xFF47413D, 0xFF36312D)
            lib.addProp(fIdx, -170f, -110f, 36f, 120f, 0xFF533926, 0.7f, "shelf")
            lib.addProp(fIdx, -70f, -110f, 36f, 120f, 0xFF533926, 0.7f, "shelf")
            lib.addProp(fIdx, 60f, -120f, 76f, 34f, 0xFF3D342D, 0.48f, "desk")
            lib.addProp(fIdx, 60f, 50f, 76f, 34f, 0xFF3D342D, 0.48f, "desk")
            lib.addWall(fIdx, -230f, -170f, 230f, -170f, listOf(WallFeature(0f, 92f, isWindow = true)))
            lib.addWall(fIdx, -230f, -170f, -230f, 170f, listOf(WallFeature(0f, 80f, isWindow = true)))
            lib.addWall(fIdx, -230f, 170f, 230f, 170f, listOf(WallFeature(0f, 92f, isWindow = true)))
            lib.addWall(
                fIdx,
                230f,
                -170f,
                230f,
                170f,
                if (fIdx == 0) listOf(WallFeature(0f, 96f, isDoubleDoor = true, doorStyle = DoorStyle.DOUBLE_SLIDING_GLASS, autoSensor = true))
                else listOf(WallFeature(0f, 84f, isWindow = true))
            )
        }
        lib.addStairwell(-196f, 108f, 68f, 40f, libFloors)
        lib.addLootSpot(0, 125f, -70f, "Media Archive & AV Desk", listOf(
            matItem(uid, CraftingMaterial.ELECTRONIC_PARTS, 3),
            matItem(uid, CraftingMaterial.LITHIUM_CELLS, 3),
            matItem(uid, CraftingMaterial.POLYMER_STOCK, 2)
        ))

        // 3. Hatter Hall Dormitories (Multi-room suites + kitchenette)
        val dormFloors = listOf(0, 1, 2)
        val dorm = MultiFloorBlueprint(
            accum = acc,
            cx = 570f,
            cy = -1260f,
            w = 480f,
            h = 360f,
            rotDeg = 0,
            name = "HATTER HALL DORMS",
            extColorHex = 0xFF7D493C,
            roofColorHex = 0xFF282022,
            accessibleFloors = dormFloors,
            miniColorHex = 0xFFC084FC,
            groundHeight = 1
        )
        dorm.addSidewalk(-256f, -196f, 512f, 392f)
        for (fIdx in dormFloors) {
            dorm.addFloor(fIdx, -232f, -172f, 464f, 344f, 0xFF52443A, 0xFF3E332B)
            // Central corridor with left/right dorm suites
            dorm.addWall(fIdx, -40f, -180f, -40f, 180f, listOf(WallFeature(-80f, 52f, swingDir = -1f, startOpen = false), WallFeature(80f, 52f, swingDir = -1f, startOpen = true)))
            dorm.addWall(fIdx, 40f, -180f, 40f, 180f, listOf(WallFeature(-80f, 52f, swingDir = 1f, startOpen = true), WallFeature(80f, 52f, swingDir = 1f, startOpen = false)))
            dorm.addProp(fIdx, -205f, -140f, 62f, 34f, 0xFF4B372A, 0.48f, "bed")
            dorm.addProp(fIdx, -205f, 40f, 62f, 34f, 0xFF4B372A, 0.48f, "bed")
            dorm.addProp(fIdx, 130f, -140f, 46f, 28f, 0xFF374151, 0.62f, "stove")
            dorm.addProp(fIdx, 182f, -140f, 38f, 28f, 0xFF64748B, 0.72f, "fridge")
            dorm.addProp(fIdx, 130f, 50f, 62f, 34f, 0xFF4B372A, 0.48f, "bed")
            if (fIdx == 0) {
                dorm.addProp(0, 10f, -135f, 38f, 20f, 0xFF1E293B, 0.58f, "tv", tvPoweredOn = true)
            }
            dorm.addWall(fIdx, -240f, -180f, 240f, -180f, listOf(WallFeature(-120f, 62f, isWindow = true), WallFeature(120f, 62f, isWindow = true)))
            dorm.addWall(fIdx, 240f, -180f, 240f, 180f, listOf(WallFeature(0f, 70f, isWindow = true)))
            dorm.addWall(fIdx, -240f, 180f, 240f, 180f, listOf(WallFeature(-120f, 62f, isWindow = true), WallFeature(120f, 62f, isWindow = true)))
            dorm.addWall(
                fIdx,
                -240f,
                -180f,
                -240f,
                180f,
                if (fIdx == 0) listOf(WallFeature(0f, 84f, isDoubleDoor = true, swingDir = 1f, startOpen = false, doorStyle = DoorStyle.HINGED_WOOD))
                else listOf(WallFeature(0f, 68f, isWindow = true))
            )
        }
        dorm.addStairwell(-28f, 114f, 56f, 42f, dormFloors)
        dorm.addLootSpot(1, -130f, -75f, "Dorm Suite Footlocker (2F)", listOf(
            matItem(uid, CraftingMaterial.TACTICAL_CLOTH, 4),
            matItem(uid, CraftingMaterial.MEDICAL_HERBS, 3),
            matItem(uid, CraftingMaterial.WOOD_FURNITURE, 3)
        ))
    }

    fun buildCommercialStrip(acc: MutableWorldAccum, uid: (String) -> String) {
        // 1. SunCoast Mart (Large Grocery & Hardware Superstore w/ DOUBLE SLIDING GLASS DOORS)
        acc.groundPatches.add(GroundPatch(560f, 140f, 660f, 520f, 0xFF212029, "SUNCOAST MART LOT"))
        val mart = MultiFloorBlueprint(
            accum = acc,
            cx = 920f,
            cy = 440f,
            w = 600f,
            h = 380f,
            rotDeg = 0,
            name = "SUNCOAST MART",
            extColorHex = 0xFF32435C,
            roofColorHex = 0xFF1F222B,
            accessibleFloors = listOf(0),
            miniColorHex = 0xFF60A5FA
        )
        mart.addSidewalk(-316f, -206f, 632f, 412f)
        mart.addFloor(0, -292f, -182f, 584f, 364f, 0xFF535863, 0xFF424752)
        // Back Stockroom
        mart.addFloor(0, -292f, 70f, 584f, 112f, 0xFF363A42, 0xFF2B2F36)
        mart.addWall(0, -300f, 70f, 300f, 70f, listOf(WallFeature(110f, 68f, swingDir = 1f, startOpen = true, doorStyle = DoorStyle.STEEL_SECURITY)))

        // Front wall: Double Sliding Glass Automatic Doors + Checkout Front
        mart.addWall(0, -300f, -190f, 300f, -190f, listOf(
            WallFeature(-175f, 96f, isWindow = true),
            WallFeature(0f, 108f, isDoubleDoor = true, doorStyle = DoorStyle.DOUBLE_SLIDING_GLASS, autoSensor = true),
            WallFeature(175f, 96f, isWindow = true)
        ))
        mart.addWall(0, -300f, -190f, -300f, 190f, listOf(WallFeature(0f, 70f, isWindow = true)))
        mart.addWall(0, 300f, -190f, 300f, 190f, listOf(WallFeature(0f, 70f, isWindow = true)))
        mart.addWall(0, -300f, 190f, 300f, 190f, listOf(WallFeature(-140f, 110f, doorStyle = DoorStyle.GARAGE_ROLLUP, startOpen = false)))

        // Checkout lanes & detailed grocery/hardware aisles
        mart.addProp(0, -210f, -135f, 68f, 26f, 0xFF334155, 0.58f, "checkout")
        mart.addProp(0, 120f, -135f, 68f, 26f, 0xFF334155, 0.58f, "checkout")
        mart.addProp(0, -235f, -65f, 38f, 110f, 0xFF3A4A5C, 0.72f, "shelf")
        mart.addProp(0, -135f, -65f, 38f, 110f, 0xFF3A4A5C, 0.72f, "shelf")
        mart.addProp(0, -35f, -65f, 38f, 110f, 0xFF3A4A5C, 0.72f, "shelf")
        mart.addProp(0, 70f, -65f, 38f, 110f, 0xFF3A4A5C, 0.72f, "shelf")
        mart.addProp(0, 175f, -65f, 38f, 110f, 0xFF3A4A5C, 0.72f, "shelf")
        mart.addProp(0, 238f, -155f, 40f, 20f, 0xFF1E293B, 0.60f, "tv", tvPoweredOn = true)
        mart.addProp(0, -250f, 100f, 76f, 34f, 0xFF475569, 0.65f, "storage_depot")

        mart.addLootSpot(0, -180f, -10f, "SunCoast Pharmacy & First-Aid Aisle", listOf(
            matItem(uid, CraftingMaterial.MEDICAL_HERBS, 3),
            matItem(uid, CraftingMaterial.ANTISEPTIC_SOL, 3),
            matItem(uid, CraftingMaterial.TRAUMA_GAUZE, 3),
            matItem(uid, CraftingMaterial.TACTICAL_CLOTH, 3),
            matItem(uid, CraftingMaterial.POLYMER_STOCK, 2)
        ))
        mart.addLootSpot(0, 130f, 115f, "SunCoast Loading Bay & Tool Rack", listOf(
            matItem(uid, CraftingMaterial.SCRAP_METAL, 5),
            matItem(uid, CraftingMaterial.INDUSTRIAL_FASTENERS, 5),
            matItem(uid, CraftingMaterial.COPPER_WIRING, 4),
            matItem(uid, CraftingMaterial.LITHIUM_CELLS, 3),
            matItem(uid, CraftingMaterial.ELECTRONIC_PARTS, 3),
            matItem(uid, CraftingMaterial.GUN_LUBE, 3)
        ))

        // 2. Woodbine Office Plaza (2-Floor Corporate Park w/ DOUBLE SLIDING GLASS DOORS + Break Room)
        val offFloors = listOf(-1, 0, 1, 2)
        val off = MultiFloorBlueprint(
            accum = acc,
            cx = 920f,
            cy = -360f,
            w = 540f,
            h = 380f,
            rotDeg = 0,
            name = "WOODBINE OFFICE PLAZA",
            extColorHex = 0xFF3F4756,
            roofColorHex = 0xFF1F222C,
            accessibleFloors = offFloors,
            roofFloorIndex = 2,
            miniColorHex = 0xFFC084FC
        )
        off.addSidewalk(-286f, -206f, 572f, 412f)
        for (fIdx in offFloors) {
            val isRoof = fIdx == 2
            off.addFloor(fIdx, -262f, -182f, 524f, 364f, if (isRoof) 0xFF24262E else 0xFF3A414D, 0xFF2D333E)
            if (isRoof) {
                off.addProp(fIdx, -170f, -120f, 64f, 46f, 0xFF4B5563, 0.65f, "hvac")
                off.addProp(fIdx, 90f, -120f, 56f, 42f, 0xFF4B5563, 0.6f, "hvac")
                off.addWall(fIdx, -270f, -190f, 270f, -190f)
                off.addWall(fIdx, -270f, -190f, -270f, 190f)
                off.addWall(fIdx, 270f, -190f, 270f, 190f)
                off.addWall(fIdx, -270f, 190f, 270f, 190f)
            } else {
                // Interior suites & break room
                off.addWall(fIdx, -30f, -190f, -30f, 190f, listOf(WallFeature(-60f, 56f, swingDir = 1f, startOpen = true), WallFeature(80f, 56f, swingDir = -1f, startOpen = false)))
                off.addProp(fIdx, -210f, -140f, 70f, 32f, 0xFF2D3748, 0.50f, "desk")
                off.addProp(fIdx, -210f, -45f, 70f, 32f, 0xFF2D3748, 0.50f, "desk")
                off.addProp(fIdx, 50f, -145f, 46f, 28f, 0xFF374151, 0.62f, "stove")
                off.addProp(fIdx, 102f, -145f, 38f, 28f, 0xFF64748B, 0.72f, "fridge")
                off.addProp(fIdx, 150f, -145f, 36f, 26f, 0xFFCBD5E1, 0.55f, "sink")
                off.addProp(fIdx, 60f, -45f, 76f, 36f, 0xFF473728, 0.45f, "mess_table")
                if (fIdx == 0 || fIdx == 1) {
                    off.addProp(fIdx, -145f, 95f, 40f, 20f, 0xFF1E293B, 0.58f, "tv", tvPoweredOn = true)
                }
                off.addWall(fIdx, -270f, -190f, 270f, -190f, listOf(WallFeature(0f, 120f, isWindow = true)))
                off.addWall(fIdx, -270f, -190f, -270f, 190f, listOf(WallFeature(0f, 96f, isWindow = true)))
                off.addWall(fIdx, 270f, -190f, 270f, 190f, listOf(WallFeature(0f, 96f, isWindow = true)))
                off.addWall(
                    fIdx,
                    -270f,
                    190f,
                    270f,
                    190f,
                    if (fIdx == 0) listOf(WallFeature(0f, 104f, isDoubleDoor = true, doorStyle = DoorStyle.DOUBLE_SLIDING_GLASS, autoSensor = true))
                    else listOf(WallFeature(0f, 104f, isWindow = true))
                )
            }
        }
        off.addStairwell(178f, 122f, 68f, 40f, offFloors)
        off.addLootSpot(1, -150f, 105f, "IT Server & Optics Closet (2F)", listOf(
            matItem(uid, CraftingMaterial.ELECTRONIC_PARTS, 5),
            matItem(uid, CraftingMaterial.OPTICAL_GLASS, 4),
            matItem(uid, CraftingMaterial.LITHIUM_CELLS, 4),
            matItem(uid, CraftingMaterial.COPPER_WIRING, 3),
            matItem(uid, CraftingMaterial.CERAMIC_PLATES, 2)
        ))

        // 3. Cypress Care Clinic (Double Sliding Glass ER Entry + Exam Bays)
        val clinic = MultiFloorBlueprint(
            accum = acc,
            cx = 1660f,
            cy = -320f,
            w = 460f,
            h = 320f,
            rotDeg = 0,
            name = "CYPRESS CARE CLINIC",
            extColorHex = 0xFF3E5A5E,
            roofColorHex = 0xFF1F2C30,
            accessibleFloors = listOf(0),
            miniColorHex = 0xFF22C55E
        )
        clinic.addSidewalk(-246f, -176f, 492f, 352f)
        clinic.addFloor(0, -222f, -152f, 444f, 304f, 0xFF8C9EA3, 0xFF6B7F85)
        clinic.addWall(0, -20f, -160f, -20f, 160f, listOf(WallFeature(-50f, 56f, swingDir = 1f, startOpen = true), WallFeature(70f, 56f, swingDir = -1f, startOpen = false)))
        clinic.addProp(0, -180f, -110f, 70f, 34f, 0xFF374151, 0.45f, "sofa")
        clinic.addProp(0, -175f, 60f, 38f, 20f, 0xFF1E293B, 0.58f, "tv", tvPoweredOn = true)
        clinic.addProp(0, 60f, -120f, 80f, 36f, 0xFF4C7A75, 0.48f, "med_bed_1")
        clinic.addProp(0, 60f, 35f, 80f, 36f, 0xFF4C7A75, 0.48f, "med_bed_2")
        clinic.addProp(0, 168f, -120f, 48f, 30f, 0xFFE2E8F0, 0.72f, "med_cabinet")
        clinic.addWall(0, -230f, -160f, 230f, -160f, listOf(WallFeature(0f, 84f, isWindow = true)))
        clinic.addWall(0, -230f, -160f, -230f, 160f, listOf(WallFeature(0f, 72f, isWindow = true)))
        clinic.addWall(0, 230f, -160f, 230f, 160f, listOf(WallFeature(0f, 72f, isWindow = true)))
        clinic.addWall(0, -230f, 160f, 230f, 160f, listOf(WallFeature(0f, 102f, isDoubleDoor = true, doorStyle = DoorStyle.DOUBLE_SLIDING_GLASS, autoSensor = true)))
        clinic.addLootSpot(0, 150f, 80f, "Clinic Triage & Trauma Supply", listOf(
            matItem(uid, CraftingMaterial.TRAUMA_GAUZE, 4),
            matItem(uid, CraftingMaterial.ANTISEPTIC_SOL, 4),
            matItem(uid, CraftingMaterial.MEDICAL_HERBS, 4),
            matItem(uid, CraftingMaterial.TACTICAL_CLOTH, 2)
        ))
    }

    /**
     * Builds a true-to-scale multi-room Florida house (foyer, living room w/ TV & sofa,
     * detailed kitchen w/ stove, fridge, sink & dining table, bedroom, bathroom)
     * and optionally includes an attached garage + concrete driveway with a roll-up garage door!
     */
    fun buildDetailedFloridaHome(
        acc: MutableWorldAccum,
        cx: Float,
        cy: Float,
        rotDeg: Int,
        name: String,
        extColorHex: Long,
        hasGarage: Boolean,
        hasBasement: Boolean,
        tvOn: Boolean,
        uid: (String) -> String
    ) {
        val totalW = if (hasGarage) 620f else 460f
        val totalH = 340f
        val floors = if (hasBasement) listOf(-1, 0) else listOf(0)
        val bp = MultiFloorBlueprint(
            accum = acc,
            cx = cx,
            cy = cy,
            w = totalW,
            h = totalH,
            rotDeg = rotDeg,
            name = name,
            extColorHex = extColorHex,
            roofColorHex = 0xFF2A211D,
            accessibleFloors = floors,
            miniColorHex = 0xFFA8A29E
        )

        val hw = totalW * 0.5f
        val hh = totalH * 0.5f
        val livingLeft = -hw
        val livingRight = if (hasGarage) hw - 170f else hw

        // Concrete porch & walkway; plus concrete driveway in front of attached garage
        bp.addSidewalk(-hw - 14f, -hh - 14f, totalW + 28f, totalH + 28f)
        bp.addSidewalk(-60f, hh, 110f, 70f)
        if (hasGarage) {
            bp.addSidewalk(livingRight + 6f, hh, 158f, 128f)
        }

        for (fIdx in floors) {
            val isBase = fIdx == -1
            val fc = if (isBase) 0xFF232229 else 0xFF56402D
            val sc = if (isBase) 0xFF1A191F else 0xFF422F1F
            bp.addFloor(fIdx, livingLeft + 8f, -hh + 8f, (livingRight - livingLeft) - 16f, totalH - 16f, fc, sc)

            if (!isBase) {
                // Kitchen tile floor (top-right inside main house)
                bp.addFloor(0, livingRight - 195f, -hh + 8f, 187f, 145f, 0xFF4E4C46, 0xFF3B3934)
                // Bathroom tile floor (bottom-left)
                bp.addFloor(0, livingLeft + 8f, hh - 115f, 118f, 107f, 0xFF465257, 0xFF333D42)

                // Interior walls: Bedroom / Bath on left, Living / Kitchen on right
                val divX = livingLeft + 185f
                bp.addWall(0, divX, -hh, divX, hh, listOf(
                    WallFeature(-75f, 48f, swingDir = 1f, startOpen = true),
                    WallFeature(95f, 46f, swingDir = 1f, startOpen = false)
                ))
                // Bedroom / Bathroom horizontal wall
                bp.addWall(0, livingLeft, hh - 120f, divX, hh - 120f, listOf(WallFeature(-25f, 44f, swingDir = -1f, startOpen = true)))

                // Bedroom Props
                bp.addProp(0, livingLeft + 22f, -hh + 24f, 68f, 40f, 0xFF4A3525, 0.50f, "bed")
                bp.addProp(0, livingLeft + 110f, -hh + 24f, 54f, 28f, 0xFF3E2D20, 0.55f, "desk")
                // Bathroom Props
                bp.addProp(0, livingLeft + 20f, hh - 98f, 24f, 26f, 0xFFD1D5DB, 0.45f, "toilet")
                bp.addProp(0, livingLeft + 54f, hh - 102f, 52f, 28f, 0xFFE2E8F0, 0.45f, "bathtub")
                bp.addProp(0, livingLeft + 84f, hh - 42f, 30f, 24f, 0xFFCBD5E1, 0.50f, "sink")

                // Living Room Props (bottom-right of house)
                bp.addProp(0, divX + 55f, hh - 68f, 76f, 34f, 0xFF47312E, 0.48f, "sofa")
                bp.addProp(0, divX + 75f, 10f, 40f, 22f, 0xFF1E293B, 0.58f, "tv", tvPoweredOn = tvOn)

                // Hyper-Detailed Kitchen Props (top-right of house)
                bp.addProp(0, livingRight - 185f, -hh + 20f, 46f, 30f, 0xFF374151, 0.62f, "stove")
                bp.addProp(0, livingRight - 134f, -hh + 20f, 40f, 30f, 0xFF64748B, 0.72f, "fridge")
                bp.addProp(0, livingRight - 88f, -hh + 20f, 40f, 28f, 0xFFCBD5E1, 0.56f, "sink")
                bp.addProp(0, livingRight - 44f, -hh + 20f, 36f, 28f, 0xFF5A4332, 0.58f, "counter")
                bp.addProp(0, livingRight - 140f, -hh + 86f, 68f, 36f, 0xFF664830, 0.46f, "mess_table")

                // Attached Garage Room!
                if (hasGarage) {
                    bp.addFloor(0, livingRight + 4f, -hh + 8f, hw - livingRight - 12f, totalH - 16f, 0xFF33363B, 0xFF27292E)
                    // Garage workbench & spare shelves
                    bp.addProp(0, livingRight + 24f, -hh + 24f, 92f, 30f, 0xFF7C2D12, 0.60f, "garage_bench")
                    bp.addProp(0, hw - 44f, -40f, 30f, 84f, 0xFF475569, 0.68f, "shelf")
                    // Garage outer walls + Roll-up Garage Door on front wall
                    bp.addWall(0, livingRight, -hh, hw, -hh, listOf(WallFeature(0f, 54f, isWindow = true)))
                    bp.addWall(0, hw, -hh, hw, hh, listOf(WallFeature(0f, 48f, swingDir = 1f, startOpen = false)))
                    bp.addWall(0, livingRight, hh, hw, hh, listOf(WallFeature(0f, 120f, doorStyle = DoorStyle.GARAGE_ROLLUP, startOpen = false)))
                    // Connecting interior wall between kitchen/living and garage
                    bp.addWall(0, livingRight, -hh, livingRight, hh, listOf(WallFeature(-40f, 52f, swingDir = 1f, startOpen = false, doorStyle = DoorStyle.HINGED_WOOD)))
                } else {
                    bp.addWall(0, livingRight, -hh, livingRight, hh, listOf(WallFeature(0f, 56f, isWindow = true)))
                }

                // House outer walls
                bp.addWall(0, livingLeft, -hh, livingRight, -hh, listOf(WallFeature(-50f, 56f, isWindow = true), WallFeature(90f, 56f, isWindow = true)))
                bp.addWall(0, livingLeft, -hh, livingLeft, hh, listOf(WallFeature(-40f, 52f, isWindow = true)))
                bp.addWall(0, livingLeft, hh, livingRight, hh, listOf(WallFeature(40f, 60f, swingDir = 1f, startOpen = false, doorStyle = DoorStyle.HINGED_WOOD)))
            } else {
                // Basement workshop
                bp.addProp(-1, livingLeft + 34f, -hh + 34f, 86f, 30f, 0xFF4A3525, 0.60f, "workbench")
                bp.addProp(-1, livingRight - 110f, -hh + 34f, 80f, 30f, 0xFF3F3832, 0.65f, "shelf")
                bp.addWall(-1, livingLeft, -hh, livingRight, -hh, wallColorHex = 0xFF464A52)
                bp.addWall(-1, livingLeft, -hh, livingLeft, hh, wallColorHex = 0xFF464A52)
                bp.addWall(-1, livingRight, -hh, livingRight, hh, wallColorHex = 0xFF464A52)
                bp.addWall(-1, livingLeft, hh, livingRight, hh, wallColorHex = 0xFF464A52)
            }
        }

        if (hasBasement) {
            bp.addStairwell(livingRight - 102f, hh - 70f, 68f, 40f, floors)
            bp.addLootSpot(-1, livingLeft + 95f, 0f, "Basement Tool & Reloading Trunk (B1)", listOf(
                matItem(uid, CraftingMaterial.FORGED_RECEIVER, 1),
                matItem(uid, CraftingMaterial.RIFLED_BARREL, 1),
                matItem(uid, CraftingMaterial.SCRAP_METAL, 4),
                matItem(uid, CraftingMaterial.LEAD_PROJECTILES, 3),
                matItem(uid, CraftingMaterial.BRASS_CASINGS, 3),
                matItem(uid, CraftingMaterial.COPPER_WIRING, 2)
            ))
        }
        bp.addLootSpot(0, livingRight - 70f, -10f, "Kitchen Pantry & Utility Drawer", listOf(
            matItem(uid, CraftingMaterial.MEDICAL_HERBS, 2),
            matItem(uid, CraftingMaterial.WOOD_FURNITURE, 3),
            matItem(uid, CraftingMaterial.TACTICAL_CLOTH, 2),
            matItem(uid, CraftingMaterial.ANTISEPTIC_SOL, 1)
        ))
        if (hasGarage) {
            bp.addLootSpot(0, hw - 85f, 40f, "Garage Pegboard & Tool Chest", listOf(
                matItem(uid, CraftingMaterial.SCRAP_METAL, 4),
                matItem(uid, CraftingMaterial.INDUSTRIAL_FASTENERS, 4),
                matItem(uid, CraftingMaterial.TRIGGER_ASSEMBLY, 1),
                matItem(uid, CraftingMaterial.GUN_LUBE, 2)
            ))
        }
    }
}
