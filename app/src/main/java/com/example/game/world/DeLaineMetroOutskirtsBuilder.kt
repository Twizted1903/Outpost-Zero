package com.example.game.world

import com.example.game.model.AmbientZone
import com.example.game.model.AmbientZoneKind
import com.example.game.model.CraftingMaterial
import com.example.game.model.CurvedRoad
import com.example.game.model.DistrictZone
import com.example.game.model.DoorStyle
import com.example.game.model.ElevatedWalkway
import com.example.game.model.ExtractionZone
import com.example.game.model.GroundPatch
import com.example.game.model.InventoryItem
import com.example.game.model.PropObj
import com.example.game.model.RectF
import com.example.game.model.StreetLightObj
import com.example.game.model.TerrainKind
import com.example.game.model.TerrainRamp
import com.example.game.model.TerrainZone
import com.example.game.model.VehicleKind
import kotlin.random.Random

internal object DeLaineMetroOutskirtsBuilder {

    private fun matItem(
        uid: (String) -> String,
        mat: CraftingMaterial,
        qty: Int
    ) = DeLaineTownBuilder.matItem(uid, mat, qty)

    fun buildMetroRoadsAndElevation(acc: MutableWorldAccum) {
        // Base regional Florida pine-scrub & urban ground patches (crisp daylight values under ink outlines)
        acc.groundPatches.add(GroundPatch(-3200f, -3200f, 6400f, 6400f, 0xFF243629))
        acc.groundPatches.add(GroundPatch(-1200f, -900f, 2500f, 1750f, 0xFF2D333C, "DOWNTOWN DELAINE"))
        acc.groundPatches.add(GroundPatch(-3180f, -1500f, 1020f, 3100f, 0xFF203A29, "COBALT SPRINGS RESERVE"))
        acc.groundPatches.add(GroundPatch(-2050f, 1050f, 1180f, 860f, 0xFF2F2E37, "DELAINE RAIL YARD"))
        acc.groundPatches.add(GroundPatch(1460f, -2950f, 1520f, 1850f, 0xFF27382C, "AIRFIELD & LAKE HELENA"))

        // Primary Town & Metro Road Grid
        // Woodbine Blvd (US-17 / 92 North-South spine)
        acc.roads.add(RectF(-70f, -2900f, 140f, 5800f))
        // Yorktown Ave (SR-44 East-West spine) — (320, -10) stays crisp open asphalt
        acc.roads.add(RectF(-2180f, -60f, 4660f, 120f))
        // Huron Ave (College South Corridor)
        acc.roads.add(RectF(-2180f, -880f, 3600f, 90f))
        // Superior Ave (North Stanton / DeLuca Link)
        acc.roads.add(RectF(-1600f, -1940f, 3300f, 90f))
        // Grant Ave (South Neighborhoods & Rail Yard)
        acc.roads.add(RectF(-2180f, 760f, 4100f, 90f))
        // Westside River Rd (leads to Cobalt Springs & St. Jude's Marina)
        acc.roads.add(RectF(-2180f, -1500f, 100f, 3300f))
        // Eastside Blvd (Airfield & Commercial Bypass)
        acc.roads.add(RectF(1280f, -2200f, 100f, 4100f))

        // Scenic curved park road into Cobalt Springs & Lake Helena loop
        acc.curvedRoads.add(
            CurvedRoad(
                listOf(-2130f to 220f, -2340f to 280f, -2480f to 360f, -2520f to 520f),
                82f
            )
        )
        acc.curvedRoads.add(
            CurvedRoad(
                listOf(1330f to -2050f, 1560f to -2120f, 1780f to -2040f, 1850f to -1820f),
                84f
            )
        )

        // Dashed centerlines
        for (y in -2820..2820 step 86) {
            if (y !in -95..95 && y !in -900..-770 && y !in 740..870) {
                acc.roadMarkings.add(RectF(-2f, y.toFloat(), 4f, 38f))
            }
        }
        for (x in -2100..2360 step 86) {
            if (x !in -95..95 && x !in 1260..1400) {
                acc.roadMarkings.add(RectF(x.toFloat(), -2f, 38f, 4f))
            }
        }

        // Citrus City Highway Overpass & Sunken Drainage Ditch (Multi-tier Elevation in South Metro!)
        acc.terrainZones.add(
            TerrainZone(-800f, 1860f, 1600f, 260f, -1, TerrainKind.DITCH, 0xFF15201D, "DRAINAGE CANAL")
        )
        acc.terrainZones.add(
            TerrainZone(180f, 1480f, 520f, 340f, 1, TerrainKind.SANDY_RIDGE, 0xFF332D22, "CITRUS RIDGE")
        )
        // Elevated Concrete Highway Overpass spanning over the drainage canal (+2 above ground)
        acc.elevatedWalkways.add(
            ElevatedWalkway(-76f, 1780f, 152f, 440f, heightLevel = 2, colorHex = 0xFF282D36, isBoardwalk = false)
        )
        acc.terrainRamps.add(TerrainRamp(-70f, 1680f, 140f, 110f, 0, 2, isStairs = false))
        acc.terrainRamps.add(TerrainRamp(-70f, 2210f, 140f, 110f, 0, 2, isStairs = false))

        // Concrete traffic barriers near overpass approach (leaves street clear around x=0, y=-10)
        acc.props.add(PropObj("ov_bar_1", -96f, 1620f, 64f, 18f, 0, 0xFF64748B, 0.42f, "jersey_barrier"))
        acc.props.add(PropObj("ov_bar_2", 34f, 1620f, 64f, 18f, 0, 0xFF64748B, 0.42f, "jersey_barrier"))
        acc.props.add(PropObj("ov_cone_1", -18f, 1595f, 14f, 14f, 0, 0xFFF97316, 0.30f, "cone"))
    }

    fun buildCobaltSpringsAndRiverfront(acc: MutableWorldAccum, uid: (String) -> String) {
        // Sunken Limestone Shelf (-1) & Deep Cobalt Spring Boil (-2)
        acc.terrainZones.add(
            TerrainZone(-3140f, -720f, 880f, 1780f, -1, TerrainKind.LIMESTONE_BANK, 0xFF282E2D, "LIMESTONE SPRING BANK")
        )
        acc.terrainZones.add(
            TerrainZone(-3080f, -640f, 540f, 1560f, -2, TerrainKind.BASIN_WATER, 0xFF0A2E47, "COBALT SPRING BOIL")
        )
        // Sandy pine bluff overlooking the spring (+1)
        acc.terrainZones.add(
            TerrainZone(-2360f, -760f, 170f, 620f, 1, TerrainKind.SANDY_RIDGE, 0xFF363125, "PINE BLUFF OVERLOOK")
        )

        // Stone stairs down into Limestone Bank & trails onto Pine Bluff
        acc.terrainRamps.add(TerrainRamp(-2280f, 120f, 64f, 120f, 0, -1, isStairs = true))
        acc.terrainRamps.add(TerrainRamp(-2280f, 520f, 64f, 120f, 0, -1, isStairs = true))
        acc.terrainRamps.add(TerrainRamp(-2210f, -440f, 44f, 120f, 0, 1, isStairs = false))

        // Elevated Cypress Boardwalk & Overlook Pier spanning over water (-2) cleanly at heightLevel = 1
        acc.elevatedWalkways.add(
            ElevatedWalkway(-2560f, 100f, 340f, 80f, heightLevel = 1, colorHex = 0xFF4F3826, isBoardwalk = true)
        )
        acc.elevatedWalkways.add(
            ElevatedWalkway(-2860f, -80f, 320f, 76f, heightLevel = 1, colorHex = 0xFF4F3826, isBoardwalk = true)
        )
        acc.elevatedWalkways.add(
            ElevatedWalkway(-2600f, -80f, 80f, 260f, heightLevel = 1, colorHex = 0xFF4F3826, isBoardwalk = true)
        )
        // Boardwalk access ramp from bank / ground
        acc.terrainRamps.add(TerrainRamp(-2240f, 106f, 64f, 68f, 0, 1, isStairs = true))

        // Ranger Station & Bathhouse
        val ranger = MultiFloorBlueprint(
            accum = acc,
            cx = -2440f,
            cy = 480f,
            w = 340f,
            h = 260f,
            rotDeg = 0,
            name = "COBALT SPRINGS RANGER STATION",
            extColorHex = 0xFF4C3828,
            roofColorHex = 0xFF212B21,
            accessibleFloors = listOf(0),
            miniColorHex = 0xFF10B981
        )
        ranger.addSidewalk(-184f, -144f, 368f, 288f)
        ranger.addFloor(0, -162f, -122f, 324f, 244f, 0xFF544130, 0xFF3E2F22)
        ranger.addProp(0, -135f, -95f, 74f, 30f, 0xFF3E2D20, 0.56f, "desk")
        ranger.addProp(0, 65f, -95f, 78f, 30f, 0xFF475569, 0.65f, "shelf")
        ranger.addProp(0, -135f, 35f, 38f, 20f, 0xFF1E293B, 0.58f, "tv", tvPoweredOn = true)
        ranger.addWall(0, -170f, -130f, 170f, -130f, listOf(WallFeature(0f, 80f, isWindow = true)))
        ranger.addWall(0, -170f, -130f, -170f, 130f, listOf(WallFeature(0f, 64f, isWindow = true)))
        ranger.addWall(0, -170f, 130f, 170f, 130f, listOf(WallFeature(0f, 68f, isWindow = true)))
        ranger.addWall(0, 170f, -130f, 170f, 130f, listOf(WallFeature(0f, 64f, doorStyle = DoorStyle.HINGED_WOOD, swingDir = -1f, startOpen = false)))
        ranger.addLootSpot(0, 85f, -45f, "Park Ranger First-Aid & Gear Locker", listOf(
            matItem(uid, CraftingMaterial.MEDICAL_HERBS, 4),
            matItem(uid, CraftingMaterial.ANTISEPTIC_SOL, 3),
            matItem(uid, CraftingMaterial.TRAUMA_GAUZE, 3),
            matItem(uid, CraftingMaterial.TACTICAL_CLOTH, 3),
            matItem(uid, CraftingMaterial.WOOD_FURNITURE, 3)
        ))

        // Picnic tables & Canoes along Cobalt Springs bank
        acc.props.add(PropObj("spring_picnic_1", -2410f, 240f, 58f, 34f, 0, 0xFF6B4C35, 0.45f, "picnic_table"))
        acc.props.add(PropObj("spring_picnic_2", -2330f, 240f, 58f, 34f, 0, 0xFF6B4C35, 0.45f, "picnic_table"))
        acc.props.add(PropObj("spring_canoe_1", -2510f, -310f, 26f, 84f, 0, 0xFFB45309, 0.42f, "canoe"))
        acc.props.add(PropObj("spring_canoe_2", -2470f, -295f, 26f, 84f, 0, 0xFF15803D, 0.42f, "canoe"))

        // St. Jude's Riverfront Marina & Bait Shack (South-West Riverbank)
        acc.terrainZones.add(
            TerrainZone(-3140f, 1120f, 780f, 1180f, -2, TerrainKind.BASIN_WATER, 0xFF0C2A3E, "ST. JUDE'S RIVER")
        )
        acc.elevatedWalkways.add(
            ElevatedWalkway(-2560f, 1420f, 240f, 72f, heightLevel = 1, colorHex = 0xFF4F3826, isBoardwalk = true)
        )
        acc.terrainRamps.add(TerrainRamp(-2340f, 1426f, 52f, 60f, 0, 1, isStairs = true))
        val marina = MultiFloorBlueprint(
            accum = acc,
            cx = -2420f,
            cy = 1260f,
            w = 300f,
            h = 220f,
            rotDeg = 0,
            name = "ST. JUDE'S MARINA BAIT SHACK",
            extColorHex = 0xFF3E4E56,
            roofColorHex = 0xFF21262B,
            accessibleFloors = listOf(0),
            miniColorHex = 0xFF38BDF8
        )
        marina.addFloor(0, -142f, -102f, 284f, 204f, 0xFF4C3C2D, 0xFF362A1F)
        marina.addProp(0, -110f, -75f, 82f, 28f, 0xFF475569, 0.58f, "counter")
        marina.addProp(0, 60f, -75f, 44f, 28f, 0xFF64748B, 0.7f, "fridge")
        marina.addWall(0, -150f, -110f, 150f, -110f, listOf(WallFeature(0f, 64f, isWindow = true)))
        marina.addWall(0, -150f, -110f, -150f, 110f)
        marina.addWall(0, -150f, 110f, 150f, 110f, listOf(WallFeature(0f, 60f, isWindow = true)))
        marina.addWall(0, 150f, -110f, 150f, 110f, listOf(WallFeature(0f, 58f, doorStyle = DoorStyle.HINGED_WOOD, swingDir = -1f, startOpen = false)))
        marina.addLootSpot(0, 20f, 35f, "Bait Shack Marine Tackle & Flare Locker", listOf(
            matItem(uid, CraftingMaterial.COPPER_WIRING, 3),
            matItem(uid, CraftingMaterial.LITHIUM_CELLS, 3),
            matItem(uid, CraftingMaterial.GUN_LUBE, 3),
            matItem(uid, CraftingMaterial.GUNPOWDER, 2)
        ))
    }

    fun buildWestsideAndGardenNeighborhoods(acc: MutableWorldAccum, uid: (String) -> String) {
        // Westside Homes (some with attached garages + driveways!) & Garden District
        data class HomeSpec(val x: Float, val y: Float, val rot: Int, val title: String, val ext: Long, val garage: Boolean, val basement: Boolean, val tvOn: Boolean)

        val specs = listOf(
            HomeSpec(-1420f, 360f, 0, "OAK AVE GARAGE HOME #1", 0xFF7D7466, true, true, true),
            HomeSpec(-1420f, -440f, 180, "OAK AVE RESIDENCE #2", 0xFF6B7B78, false, true, true),
            HomeSpec(-1460f, -1240f, 90, "COLLEGE BLVD GARAGE HOME #3", 0xFF8C7A68, true, false, false),
            HomeSpec(-680f, 1180f, 0, "MAGNOLIA CT GARAGE HOME #4", 0xFF6E776B, true, true, true),
            HomeSpec(520f, 1180f, 0, "CITRUS RIDGE GARAGE HOME #5", 0xFF7C695C, true, true, true),
            HomeSpec(1720f, 340f, 180, "GARDEN DISTRICT HOME #6", 0xFF62727A, false, false, true),
            HomeSpec(1760f, 1160f, 270, "GARDEN DISTRICT GARAGE HOME #7", 0xFF7A7368, true, true, true),
            HomeSpec(-1460f, 2120f, 0, "SOUTHWOOD COTTAGE #8", 0xFF6B655D, false, true, false)
        )

        specs.forEach { s ->
            DeLaineTownBuilder.buildDetailedFloridaHome(
                acc = acc,
                cx = s.x,
                cy = s.y,
                rotDeg = s.rot,
                name = s.title,
                extColorHex = s.ext,
                hasGarage = s.garage,
                hasBasement = s.basement,
                tvOn = s.tvOn,
                uid = uid
            )
        }
    }

    fun buildRailYardAirfieldCitrusAndDeLuca(acc: MutableWorldAccum, uid: (String) -> String) {
        // 1. DeLaine Rail Yard Freight Warehouse (Roll-Up Freight Bay Doors)
        val rail = MultiFloorBlueprint(
            accum = acc,
            cx = -1460f,
            cy = 1480f,
            w = 560f,
            h = 340f,
            rotDeg = 0,
            name = "DELAINE RAIL FREIGHT DEPOT",
            extColorHex = 0xFF4B3D37,
            roofColorHex = 0xFF231E1D,
            accessibleFloors = listOf(0, 1),
            miniColorHex = 0xFFF59E0B
        )
        rail.addSidewalk(-296f, -186f, 592f, 372f)
        for (fIdx in listOf(0, 1)) {
            rail.addFloor(fIdx, -272f, -162f, 544f, 324f, 0xFF33363D, 0xFF25282E)
            rail.addProp(fIdx, -220f, -120f, 92f, 34f, 0xFF7C2D12, 0.65f, "storage_depot")
            rail.addProp(fIdx, 110f, -120f, 96f, 32f, 0xFF475569, 0.62f, "workbench")
            rail.addWall(fIdx, -280f, -170f, 280f, -170f, if (fIdx == 0) listOf(
                WallFeature(-140f, 110f, doorStyle = DoorStyle.GARAGE_ROLLUP, startOpen = false),
                WallFeature(120f, 110f, doorStyle = DoorStyle.GARAGE_ROLLUP, startOpen = false)
            ) else listOf(WallFeature(0f, 100f, isWindow = true)))
            rail.addWall(fIdx, -280f, -170f, -280f, 170f, listOf(WallFeature(0f, 70f, isWindow = true)))
            rail.addWall(fIdx, 280f, -170f, 280f, 170f, listOf(WallFeature(0f, 70f, isWindow = true)))
            rail.addWall(fIdx, -280f, 170f, 280f, 170f, if (fIdx == 0) listOf(WallFeature(0f, 64f, doorStyle = DoorStyle.STEEL_SECURITY, swingDir = -1f, startOpen = false)) else emptyList())
        }
        rail.addStairwell(-245f, 102f, 68f, 40f, listOf(0, 1))
        rail.addLootSpot(0, 150f, 70f, "Rail Yard Welding & Fastener Crib", listOf(
            matItem(uid, CraftingMaterial.SCRAP_METAL, 6),
            matItem(uid, CraftingMaterial.INDUSTRIAL_FASTENERS, 6),
            matItem(uid, CraftingMaterial.CERAMIC_PLATES, 3),
            matItem(uid, CraftingMaterial.FORGED_RECEIVER, 2),
            matItem(uid, CraftingMaterial.RIFLED_BARREL, 2)
        ))

        // 2. DeLaine Municipal Airfield (Tarmac Runway + Hangar #4 w/ Roll-Up Door + Cessna Plane Prop!)
        acc.groundPatches.add(GroundPatch(1840f, -2780f, 360f, 1380f, 0xFF21242B, "AIRFIELD RUNWAY 18-36"))
        val hangar = MultiFloorBlueprint(
            accum = acc,
            cx = 2480f,
            cy = -1960f,
            w = 500f,
            h = 380f,
            rotDeg = 0,
            name = "DELAINE AIRFIELD HANGAR #4",
            extColorHex = 0xFF3E4954,
            roofColorHex = 0xFF20252C,
            accessibleFloors = listOf(0),
            miniColorHex = 0xFF38BDF8
        )
        hangar.addFloor(0, -242f, -182f, 484f, 364f, 0xFF2F343D, 0xFF242830)
        hangar.addProp(0, -85f, -75f, 180f, 130f, 0xFF94A3B8, 0.72f, "plane")
        hangar.addProp(0, 135f, -155f, 94f, 32f, 0xFF7C2D12, 0.60f, "garage_bench")
        hangar.addProp(0, -210f, -155f, 40f, 22f, 0xFF1E293B, 0.58f, "tv", tvPoweredOn = true)
        hangar.addWall(0, -250f, -190f, 250f, -190f)
        hangar.addWall(0, 250f, -190f, 250f, 190f, listOf(WallFeature(0f, 64f, doorStyle = DoorStyle.STEEL_SECURITY, swingDir = -1f, startOpen = false)))
        hangar.addWall(0, -250f, 190f, 250f, 190f)
        hangar.addWall(0, -250f, -190f, -250f, 190f, listOf(WallFeature(0f, 150f, doorStyle = DoorStyle.GARAGE_ROLLUP, startOpen = true)))
        hangar.addLootSpot(0, 185f, 95f, "Aviation Avionics & Fuel Tool Locker", listOf(
            matItem(uid, CraftingMaterial.ELECTRONIC_PARTS, 5),
            matItem(uid, CraftingMaterial.LITHIUM_CELLS, 4),
            matItem(uid, CraftingMaterial.OPTICAL_GLASS, 4),
            matItem(uid, CraftingMaterial.SUPPRESSOR_BAFFLES, 3),
            matItem(uid, CraftingMaterial.GUN_LUBE, 3)
        ))

        // 3. Citrus City Strip Plaza (Double Sliding Glass Convenience Mart & Diner)
        val citrus = MultiFloorBlueprint(
            accum = acc,
            cx = 520f,
            cy = 2280f,
            w = 480f,
            h = 300f,
            rotDeg = 0,
            name = "CITRUS CITY STOP & DINER",
            extColorHex = 0xFF5D4838,
            roofColorHex = 0xFF261E19,
            accessibleFloors = listOf(0),
            miniColorHex = 0xFFF59E0B
        )
        citrus.addSidewalk(-256f, -166f, 512f, 332f)
        citrus.addFloor(0, -232f, -142f, 464f, 284f, 0xFF544C45, 0xFF423C36)
        citrus.addProp(0, -195f, -110f, 46f, 28f, 0xFF374151, 0.62f, "stove")
        citrus.addProp(0, -140f, -110f, 40f, 28f, 0xFF64748B, 0.72f, "fridge")
        citrus.addProp(0, -90f, -110f, 36f, 26f, 0xFFCBD5E1, 0.55f, "sink")
        citrus.addProp(0, 60f, -110f, 92f, 28f, 0xFF334155, 0.58f, "checkout")
        citrus.addProp(0, 170f, -110f, 38f, 20f, 0xFF1E293B, 0.58f, "tv", tvPoweredOn = true)
        citrus.addWall(0, -240f, -150f, 240f, -150f, listOf(WallFeature(0f, 98f, isDoubleDoor = true, doorStyle = DoorStyle.DOUBLE_SLIDING_GLASS, autoSensor = true)))
        citrus.addWall(0, -240f, -150f, -240f, 150f, listOf(WallFeature(0f, 70f, isWindow = true)))
        citrus.addWall(0, 240f, -150f, 240f, 150f, listOf(WallFeature(0f, 70f, isWindow = true)))
        citrus.addWall(0, -240f, 150f, 240f, 150f, listOf(WallFeature(120f, 54f, doorStyle = DoorStyle.STEEL_SECURITY, swingDir = -1f, startOpen = false)))
        citrus.addLootSpot(0, 125f, 45f, "Citrus City Convenience Stock", listOf(
            matItem(uid, CraftingMaterial.ANTISEPTIC_SOL, 3),
            matItem(uid, CraftingMaterial.MEDICAL_HERBS, 3),
            matItem(uid, CraftingMaterial.PERCUSSION_PRIMERS, 3),
            matItem(uid, CraftingMaterial.POLYMER_STOCK, 3)
        ))

        // 4. DeLuca Springs Rural Bluff & General Store (North Sector)
        acc.terrainZones.add(
            TerrainZone(-760f, -2860f, 640f, 560f, 1, TerrainKind.SANDY_RIDGE, 0xFF332D22, "DELUCA SPRINGS BLUFF")
        )
        acc.terrainRamps.add(TerrainRamp(-155f, -2520f, 44f, 120f, 0, 1, isStairs = false))
        val deluca = MultiFloorBlueprint(
            accum = acc,
            cx = -440f,
            cy = -2560f,
            w = 380f,
            h = 280f,
            rotDeg = 0,
            name = "DELUCA GENERAL & HARDWARE",
            extColorHex = 0xFF5E4736,
            roofColorHex = 0xFF28201B,
            accessibleFloors = listOf(0),
            miniColorHex = 0xFFA8A29E,
            groundHeight = 1
        )
        deluca.addSidewalk(-204f, -154f, 408f, 308f)
        deluca.addFloor(0, -182f, -132f, 364f, 264f, 0xFF56402E, 0xFF423022)
        deluca.addProp(0, -150f, -100f, 88f, 30f, 0xFF475569, 0.65f, "shelf")
        deluca.addProp(0, 50f, -100f, 84f, 30f, 0xFF7C2D12, 0.58f, "garage_bench")
        deluca.addWall(0, -190f, -140f, 190f, -140f, listOf(WallFeature(0f, 80f, isWindow = true)))
        deluca.addWall(0, -190f, -140f, -190f, 140f, listOf(WallFeature(0f, 64f, isWindow = true)))
        deluca.addWall(0, -190f, 140f, 190f, 140f, listOf(WallFeature(0f, 80f, isWindow = true)))
        deluca.addWall(0, 190f, -140f, 190f, 140f, listOf(WallFeature(0f, 64f, doorStyle = DoorStyle.HINGED_WOOD, swingDir = -1f, startOpen = false)))
        deluca.addLootSpot(0, -90f, 40f, "DeLuca Rural Ammunition & Supply Bin", listOf(
            matItem(uid, CraftingMaterial.GUNPOWDER, 4),
            matItem(uid, CraftingMaterial.BRASS_CASINGS, 4),
            matItem(uid, CraftingMaterial.COPPER_JACKETS, 4),
            matItem(uid, CraftingMaterial.LEAD_PROJECTILES, 4),
            matItem(uid, CraftingMaterial.KEVLAR_WEAVE, 3)
        ))
    }

    fun populateMetroDetailsAndExits(acc: MutableWorldAccum, rng: Random, uid: (String) -> String) {
        // Vehicles across Downtown, College, Cobalt Springs, Airfield & Suburbs (keeping (320, -10) unobstructed!)
        data class CarSpec(val x: Float, val y: Float, val w: Float, val h: Float, val kind: VehicleKind, val color: Long)

        val specs = listOf(
            CarSpec(-380f, 585f, 38f, 68f, VehicleKind.POLICE_CRUISER, 0xFF1E293B),
            CarSpec(-290f, 585f, 38f, 68f, VehicleKind.POLICE_CRUISER, 0xFF1E293B),
            CarSpec(-35f, 920f, 38f, 68f, VehicleKind.POLICE_CRUISER, 0xFF1E293B),
            CarSpec(680f, 185f, 36f, 62f, VehicleKind.SEDAN, 0xFF475569),
            CarSpec(780f, 185f, 36f, 62f, VehicleKind.SEDAN, 0xFF7F1D1D),
            CarSpec(1140f, 185f, 40f, 72f, VehicleKind.DELIVERY_VAN, 0xFFD1D5DB),
            CarSpec(1510f, -100f, 38f, 68f, VehicleKind.AMBULANCE, 0xFFE5E7EB),
            CarSpec(-2120f, 420f, 36f, 62f, VehicleKind.SEDAN, 0xFF365314),
            CarSpec(-1095f, 1460f, 40f, 72f, VehicleKind.DELIVERY_VAN, 0xFF4B5563),
            CarSpec(1960f, -1920f, 40f, 72f, VehicleKind.DELIVERY_VAN, 0xFF334155),
            CarSpec(320f, 2060f, 36f, 62f, VehicleKind.SEDAN, 0xFF92400E),
            CarSpec(-340f, -2345f, 38f, 68f, VehicleKind.POLICE_CRUISER, 0xFF1E293B),
            CarSpec(-180f, -840f, 36f, 62f, VehicleKind.SEDAN, 0xFF1E3A8A)
        )
        specs.forEachIndexed { idx, s ->
            val loot = mutableListOf<InventoryItem>()
            when (s.kind) {
                VehicleKind.POLICE_CRUISER -> {
                    loot.add(matItem(uid, CraftingMaterial.GUNPOWDER, 2))
                    loot.add(matItem(uid, CraftingMaterial.BRASS_CASINGS, 3))
                    loot.add(matItem(uid, CraftingMaterial.KEVLAR_WEAVE, 2))
                    if (idx == 0 || rng.nextFloat() < 0.75f) loot.add(matItem(uid, CraftingMaterial.PERCUSSION_PRIMERS, 2))
                    if (rng.nextFloat() < 0.65f) loot.add(matItem(uid, CraftingMaterial.FORGED_RECEIVER, 1))
                }
                VehicleKind.AMBULANCE -> {
                    loot.add(matItem(uid, CraftingMaterial.MEDICAL_HERBS, 2))
                    loot.add(matItem(uid, CraftingMaterial.TRAUMA_GAUZE, 3))
                    loot.add(matItem(uid, CraftingMaterial.ANTISEPTIC_SOL, 2))
                    loot.add(matItem(uid, CraftingMaterial.TACTICAL_CLOTH, 2))
                }
                VehicleKind.DELIVERY_VAN -> {
                    loot.add(matItem(uid, CraftingMaterial.SCRAP_METAL, 3))
                    loot.add(matItem(uid, CraftingMaterial.COPPER_WIRING, 2))
                    loot.add(matItem(uid, CraftingMaterial.INDUSTRIAL_FASTENERS, 3))
                    loot.add(matItem(uid, CraftingMaterial.POLYMER_STOCK, 2))
                    loot.add(matItem(uid, CraftingMaterial.COPPER_JACKETS, 2))
                    if (rng.nextFloat() < 0.75f) {
                        loot.add(matItem(uid, CraftingMaterial.LITHIUM_CELLS, 2))
                        loot.add(matItem(uid, CraftingMaterial.OPTICAL_GLASS, 1))
                    }
                }
                VehicleKind.SEDAN -> {
                    loot.add(matItem(uid, if (idx % 2 == 0) CraftingMaterial.LEAD_PROJECTILES else CraftingMaterial.WOOD_FURNITURE, 2))
                    loot.add(matItem(uid, CraftingMaterial.GUN_LUBE, 1))
                    if (rng.nextFloat() < 0.6f) loot.add(matItem(uid, CraftingMaterial.SCRAP_METAL, 2))
                }
            }
            acc.props.add(
                PropObj(
                    id = "veh_$idx",
                    x = s.x - s.w / 2f,
                    y = s.y - s.h / 2f,
                    w = s.w,
                    h = s.h,
                    floor = 0,
                    colorHex = s.color,
                    heightScale = 0.68f,
                    propType = "vehicle",
                    vehicleKind = s.kind,
                    hasAlarm = rng.nextFloat() < 0.4f,
                    lootItems = loot
                )
            )
        }

        // Florida Spanish-Moss Live Oaks & Pine Groves
        val treeCoords = listOf(
            -2260f to -300f, -2380f to 620f, -2260f to 840f, -2420f to -540f,
            -1940f to -360f, -1880f to 420f, -1120f to -760f, -940f to 640f,
            -320f to -1300f, 320f to -1300f, 940f to -1420f, 940f to -1020f,
            1060f to 960f, 1460f to 620f, 2240f to -640f, 2460f to -280f,
            2340f to 420f, 1620f to -2520f, -880f to -2240f, 680f to -2380f,
            -780f to 1640f, 880f to 1640f, 1140f to 2280f, -920f to 2320f
        )
        treeCoords.forEachIndexed { idx, (tx, ty) ->
            acc.props.add(
                PropObj(
                    id = "fl_oak_$idx",
                    x = tx - 26f,
                    y = ty - 26f,
                    w = 52f,
                    h = 52f,
                    floor = 0,
                    colorHex = if (idx % 3 == 0) 0xFF1F3E1D else 0xFF264922,
                    heightScale = 1.32f,
                    propType = "tree"
                )
            )
        }

        // Streetlights along major corridors
        val lightCoords = listOf(
            -84f to -420f, 84f to -420f, -84f to 280f, 84f to 280f,
            -84f to 980f, 84f to 1320f, -480f to -78f, 480f to -78f,
            1020f to 78f, -1240f to 78f, -2060f to 120f, 1396f to -560f,
            1396f to -1760f, -180f to -1960f, 620f to 740f
        )
        lightCoords.forEachIndexed { idx, (lx, ly) ->
            acc.streetLights.add(StreetLightObj(lx, ly, isBrokenBulb = idx % 5 == 3))
        }

        // Named Districts across the 6400 x 6400 DeLaine Metro Area (with regional Danger Tiers 1-4)
        acc.districtZones.addAll(
            listOf(
                DistrictZone("DOWNTOWN WOODBINE", RectF(-760f, -740f, 1480f, 1380f), dangerTier = 3),
                DistrictZone("STANTON UNIVERSITY", RectF(-920f, -1860f, 1840f, 960f), dangerTier = 2),
                DistrictZone("COMMERCIAL & CLINIC STRIP", RectF(620f, -740f, 1520f, 1460f), dangerTier = 3),
                DistrictZone("COBALT SPRINGS STATE PARK", RectF(-3200f, -1400f, 1020f, 2400f), dangerTier = 2),
                DistrictZone("ST. JUDE'S RIVERFRONT", RectF(-3200f, 1000f, 1020f, 1400f), dangerTier = 1),
                DistrictZone("WESTSIDE NEIGHBORHOOD", RectF(-2120f, -1500f, 1180f, 2240f), dangerTier = 1),
                DistrictZone("DELAINE RAIL YARD", RectF(-2120f, 980f, 1260f, 960f), dangerTier = 3),
                DistrictZone("DELAINE AIRFIELD & LAKE HELENA", RectF(1400f, -3100f, 1750f, 2000f), dangerTier = 4),
                DistrictZone("DELUCA SPRINGS", RectF(-960f, -3150f, 1400f, 1100f), dangerTier = 2),
                DistrictZone("CITRUS CITY & OVERPASS", RectF(-1600f, 1400f, 3400f, 1700f), dangerTier = 1)
            )
        )

        // Ambient wind & foliage zones
        acc.ambientZones.add(AmbientZone(RectF(-3200f, -3200f, 1200f, 6400f), AmbientZoneKind.WOODS))
        acc.ambientZones.add(AmbientZone(RectF(1400f, -3200f, 1800f, 6400f), AmbientZoneKind.WOODS))
        acc.ambientZones.add(AmbientZone(RectF(-1200f, -1800f, 2600f, 3400f), AmbientZoneKind.STREET))

        // Extraction Zones across the wider DeLaine Metro
        val allExtractions = listOf(
            ExtractionZone("ex_woodbine_s", "Citrus City Overpass Exfil", 0f, 2580f),
            ExtractionZone("ex_stanton", "Stanton North Quad Gate", 0f, -1910f),
            ExtractionZone("ex_springs", "Cobalt Springs Boat Launch", -2290f, 960f),
            ExtractionZone("ex_airfield", "DeLaine Airfield Helipad", 2040f, -2140f),
            ExtractionZone("ex_rail", "DeLaine Rail Yard Siding", -1760f, 1480f),
            ExtractionZone("ex_mart", "SunCoast East Bypass Gate", 1330f, 520f)
        )
        val openIndices = allExtractions.indices.shuffled(rng).take(3).toSet()
        allExtractions.forEachIndexed { idx, ez ->
            ez.isOpen = idx in openIndices
            acc.extractionZones.add(ez)
        }
    }
}
