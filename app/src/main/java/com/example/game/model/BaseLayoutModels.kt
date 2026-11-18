package com.example.game.model

enum class BaseModuleKind(
    val label: String,
    val category: String,
    val credCost: Int,
    val scrapCost: Int,
    val width: Float,
    val height: Float,
    val uniqueCore: Boolean
) {
    // Whole Base Buildings / Zones
    BARRACKS("Walk-In Barracks", "BUILDING", 150, 6, 340f, 410f, true),
    ARMORY("Armory & Depot Bldg", "BUILDING", 140, 6, 380f, 260f, true),
    INFIRMARY("Field Infirmary Bldg", "BUILDING", 140, 5, 320f, 240f, true),
    CANTINE("Open-Air Cantine Deck", "BUILDING", 100, 4, 460f, 260f, true),
    HQ_PAVILION("HQ Command Pavilion", "BUILDING", 100, 4, 440f, 380f, true),
    HELIPAD("East Helipad Tarmac", "BUILDING", 120, 5, 400f, 420f, true),

    // Placeable Essential Stations & Bought Bunks
    LAPTOP("HQ Terminal Laptop", "STATION", 0, 0, 110f, 42f, true),
    BUNK("Bought Bunk", "BUNK", 0, 0, 92f, 38f, false),
    WORKBENCH("Crafting Workbench", "STATION", 40, 2, 88f, 32f, true),
    STORAGE_DEPOT("Storage Depot", "STATION", 40, 2, 90f, 40f, true),
    MARKET("Live Market Terminal", "STATION", 40, 2, 68f, 34f, true),
    CD_RACK("CD Rack & Stereo", "STATION", 30, 1, 42f, 78f, true),
    MESS_FOOD("Cantine Ration Counter", "STATION", 35, 2, 84f, 34f, true),
    WATER_COOLER("Cantine Water Cooler", "STATION", 35, 2, 38f, 38f, true),
    INFIRMARY_BED("Field Infirmary Bed", "STATION", 45, 2, 76f, 34f, true),

    // Custom Buildable Structures & Props (Build the whole compound piece by piece)
    CUSTOM_WALL("Fortified Wall", "BUILD", 20, 2, 120f, 12f, false),
    CUSTOM_FENCE("Timber Palisade", "BUILD", 15, 1, 120f, 10f, false),
    CUSTOM_SANDBAG("Sandbag Barricade", "BUILD", 15, 1, 92f, 18f, false),
    CUSTOM_FLOODLIGHT("Compound Floodlight", "BUILD", 25, 1, 18f, 18f, false),
    CUSTOM_TABLE("Mess / Gear Table", "BUILD", 20, 1, 92f, 40f, false),
    CUSTOM_CRATE("Supply Crate", "BUILD", 25, 2, 52f, 34f, false),
    CUSTOM_FIREPIT("Camp Firepit", "BUILD", 20, 1, 46f, 46f, false),
    CUSTOM_TV("Base CRT TV", "BUILD", 35, 1, 38f, 16f, false),
    CUSTOM_WATCHTOWER("Guard Watchtower", "BUILD", 65, 3, 92f, 92f, false)
}

data class PlacedBasePiece(
    val id: String,
    val kind: BaseModuleKind,
    val x: Float,
    val y: Float,
    val rotationDeg: Int = 0,
    val slotNumber: Int = 0
)

data class BaseCustomLayout(
    val pieces: List<PlacedBasePiece>
) {
    fun byKind(kind: BaseModuleKind): List<PlacedBasePiece> = pieces.filter { it.kind == kind }

    fun firstByKind(kind: BaseModuleKind): PlacedBasePiece? = pieces.firstOrNull { it.kind == kind }

    fun ensureBunkSlots(bunkLevel: Int): BaseCustomLayout =
        ensureInfrastructure(bunkLevel = bunkLevel, helipadLevel = null)

    fun ensureInfrastructure(bunkLevel: Int, helipadLevel: Int?): BaseCustomLayout {
        val safeLevel = bunkLevel.coerceIn(1, 5)
        val barracks = firstByKind(BaseModuleKind.BARRACKS)
        val bx = barracks?.x ?: -430f
        val byBase = barracks?.y ?: -290f
        val existingBunks = pieces.filter { it.kind == BaseModuleKind.BUNK }.associateBy { it.slotNumber }
        var basePieces = pieces.filter { it.kind != BaseModuleKind.BUNK }
        if (helipadLevel != null) {
            basePieces = if (helipadLevel <= 0) {
                basePieces.filterNot { it.kind == BaseModuleKind.HELIPAD }
            } else if (basePieces.none { it.kind == BaseModuleKind.HELIPAD }) {
                basePieces + PlacedBasePiece("bldg_helipad", BaseModuleKind.HELIPAD, 490f, 220f)
            } else {
                basePieces
            }
        }
        val updatedBunks = (1..safeLevel).map { slot ->
            existingBunks[slot] ?: PlacedBasePiece(
                id = "bunk_slot_$slot",
                kind = BaseModuleKind.BUNK,
                x = bx - 102f,
                y = byBase - 153f + (slot - 1) * 72f,
                rotationDeg = 0,
                slotNumber = slot
            )
        }
        return BaseCustomLayout(basePieces + updatedBunks)
    }

    companion object {
        /**
         * Authentic Fortified Hilltop Outpost Zero layout matching the splash key art:
         * - 4 timber Guard Watchtowers overlooking the South Gate cliff ramp & northern ridgelines
         * - Central timber-and-sandbag HQ courtyard around a glowing stone camp firepit
         * - Timber Barracks (NW), Open-Air Mess Cantine (North ridge), Field Infirmary (NE),
         *   Armory & Salvage Workshop (SW), and East Helipad landing zone.
         * - Packed earth, gravel footpaths, and sandbag barricades (no urban roads or city grids).
         */
        fun default(bunkLevel: Int = 1, helipadLevel: Int = 0): BaseCustomLayout {
            val corePieces = buildList {
                // Hilltop Compound Buildings & Defense Wings
                add(PlacedBasePiece("bldg_barracks", BaseModuleKind.BARRACKS, -430f, -290f))
                add(PlacedBasePiece("bldg_armory", BaseModuleKind.ARMORY, -440f, 275f))
                add(PlacedBasePiece("bldg_infirmary", BaseModuleKind.INFIRMARY, 440f, -280f))
                add(PlacedBasePiece("bldg_cantine", BaseModuleKind.CANTINE, 0f, -490f))
                add(PlacedBasePiece("bldg_hq", BaseModuleKind.HQ_PAVILION, 0f, -20f))
                if (helipadLevel >= 1) {
                    add(PlacedBasePiece("bldg_helipad", BaseModuleKind.HELIPAD, 490f, 220f))
                }

                // Essential Stations integrated cleanly into each outpost wing
                add(PlacedBasePiece("st_laptop", BaseModuleKind.LAPTOP, -72f, -110f))
                add(PlacedBasePiece("st_cd", BaseModuleKind.CD_RACK, 118f, -92f))
                add(PlacedBasePiece("st_workbench", BaseModuleKind.WORKBENCH, -564f, 188f))
                add(PlacedBasePiece("st_storage", BaseModuleKind.STORAGE_DEPOT, -563f, 348f))
                add(PlacedBasePiece("st_market", BaseModuleKind.MARKET, -310f, 345f))
                add(PlacedBasePiece("st_food", BaseModuleKind.MESS_FOOD, 64f, -588f))
                add(PlacedBasePiece("st_water", BaseModuleKind.WATER_COOLER, 166f, -580f))
                add(PlacedBasePiece("st_infirmary", BaseModuleKind.INFIRMARY_BED, 534f, -355f))

                // 4 Fortified Timber Guard Watchtowers (South Gate flanks + NW/NE ridgeline corners)
                add(PlacedBasePiece("prop_watchtower_sw", BaseModuleKind.CUSTOM_WATCHTOWER, -230f, 560f))
                add(PlacedBasePiece("prop_watchtower_se", BaseModuleKind.CUSTOM_WATCHTOWER, 230f, 560f))
                add(PlacedBasePiece("prop_watchtower_nw", BaseModuleKind.CUSTOM_WATCHTOWER, -840f, -640f))
                add(PlacedBasePiece("prop_watchtower_ne", BaseModuleKind.CUSTOM_WATCHTOWER, 840f, -640f))

                // Perimeter & HQ Sandbags, Supply Crates, Camp Firepit & Cantine TV
                add(PlacedBasePiece("prop_sb_hq_w", BaseModuleKind.CUSTOM_SANDBAG, -210f, 110f))
                add(PlacedBasePiece("prop_sb_hq_e", BaseModuleKind.CUSTOM_SANDBAG, 210f, 110f))
                add(PlacedBasePiece("prop_sb_gate_w", BaseModuleKind.CUSTOM_SANDBAG, -145f, 470f))
                add(PlacedBasePiece("prop_sb_gate_e", BaseModuleKind.CUSTOM_SANDBAG, 145f, 470f))
                add(PlacedBasePiece("prop_crate_hq", BaseModuleKind.CUSTOM_CRATE, 130f, -155f))
                add(PlacedBasePiece("prop_crate_gate", BaseModuleKind.CUSTOM_CRATE, -175f, 610f))
                add(PlacedBasePiece("prop_firepit", BaseModuleKind.CUSTOM_FIREPIT, 0f, 75f))
                add(PlacedBasePiece("prop_cantine_tv", BaseModuleKind.CUSTOM_TV, -165f, -625f))
            }
            return BaseCustomLayout(corePieces).ensureInfrastructure(bunkLevel, helipadLevel)
        }

        fun emptyScratch(bunkLevel: Int = 1, helipadLevel: Int = 0): BaseCustomLayout {
            val bunks = (1..bunkLevel.coerceIn(1, 5)).map { slot ->
                PlacedBasePiece(
                    id = "bunk_slot_$slot",
                    kind = BaseModuleKind.BUNK,
                    x = -180f + (slot - 1) * 110f,
                    y = -180f,
                    slotNumber = slot
                )
            }
            val core = buildList {
                add(PlacedBasePiece("st_laptop", BaseModuleKind.LAPTOP, -40f, -90f))
                if (helipadLevel >= 1) {
                    add(PlacedBasePiece("bldg_helipad", BaseModuleKind.HELIPAD, 560f, 180f))
                }
            }
            return BaseCustomLayout(core + bunks)
        }
    }
}
