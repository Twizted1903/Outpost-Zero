package com.example.game.model

enum class ArmorTier(
    val level: Int,
    val displayName: String,
    val shortName: String,
    val maxShieldHp: Float,
    val mitigation: Float,
    val rarityLabel: String,
    val colorHex: Long,
    val vestTrimHex: Long,
    val description: String
) {
    NONE(
        level = 0,
        displayName = "No Ballistic Armor",
        shortName = "UNARMORED",
        maxShieldHp = 0f,
        mitigation = 0f,
        rarityLabel = "STANDARD",
        colorHex = 0xFF6B7280,
        vestTrimHex = 0xFF374151,
        description = "Cloth fatigues only. Craft Padded Improvised Armor at the Workbench for protection."
    ),
    PADDED_VEST(
        level = 1,
        displayName = "Padded Improvised Vest (Lv.1)",
        shortName = "ARMOR LV.1",
        maxShieldHp = 40f,
        mitigation = 0.25f,
        rarityLabel = "COMMON // ARMOR",
        colorHex = 0xFF9CA3AF,
        vestTrimHex = 0xFF64748B,
        description = "Reinforced ripstop vest with stitched canvas trauma padding. Absorbs 25% of incoming bite damage."
    ),
    RIOT_RIG(
        level = 2,
        displayName = "Precinct Riot Rig (Lv.2)",
        shortName = "ARMOR LV.2",
        maxShieldHp = 75f,
        mitigation = 0.40f,
        rarityLabel = "UNCOMMON // ARMOR",
        colorHex = 0xFF34D399,
        vestTrimHex = 0xFF38BDF8,
        description = "Hard-shell polycarbonate chest & shoulder plates salvaged from District 4 cruisers."
    ),
    PLATE_CARRIER(
        level = 3,
        displayName = "Tactical Plate Carrier (Lv.3)",
        shortName = "ARMOR LV.3",
        maxShieldHp = 115f,
        mitigation = 0.55f,
        rarityLabel = "RARE // ARMOR",
        colorHex = 0xFF38BDF8,
        vestTrimHex = 0xFFF59E0B,
        description = "Mil-spec steel and ceramic strike plates rated for sustained infected swarms."
    ),
    SPEC_OPS_RIG(
        level = 4,
        displayName = "Spec-Ops Ceramic Rig (Lv.4)",
        shortName = "ARMOR LV.4",
        maxShieldHp = 160f,
        mitigation = 0.68f,
        rarityLabel = "GRAIL // SPEC-OPS",
        colorHex = 0xFFC084FC,
        vestTrimHex = 0xFFC084FC,
        description = "Composite boron-carbide full carrier rig with neck guard and reinforced trauma backing."
    );

    companion object {
        fun fromLevel(level: Int): ArmorTier = entries.firstOrNull { it.level == level.coerceIn(0, 4) } ?: NONE
    }
}

enum class DeploymentZone(
    val id: String,
    val title: String,
    val subtitle: String,
    val threatLabel: String,
    val spawnX: Float,
    val spawnY: Float,
    val accentHex: Long
) {
    OAKRIDGE_HIGHWAY(
        id = "lz_highway",
        title = "LZ ALPHA // DOWNTOWN WOODBINE",
        subtitle = "Woodbine & Yorktown — Steve's Music & SunCoast Mart",
        threatLabel = "MODERATE",
        spawnX = 0f,
        spawnY = 160f,
        accentHex = 0xFF22C55E
    ),
    OAKRIDGE_PRECINCT(
        id = "lz_precinct",
        title = "LZ BRAVO // STANTON UNIVERSITY",
        subtitle = "Terraced Quad, Duval Library & Hatter Dorms",
        threatLabel = "HIGH",
        spawnX = -20f,
        spawnY = -1120f,
        accentHex = 0xFF38BDF8
    ),
    OAKRIDGE_SUBURBS(
        id = "lz_suburbs",
        title = "LZ CHARLIE // COBALT SPRINGS",
        subtitle = "Sunken Spring Boil, Cypress Boardwalk & Westside Homes",
        threatLabel = "LOW-MED",
        spawnX = -2320f,
        spawnY = 160f,
        accentHex = 0xFFE5A024
    ),
    OAKRIDGE_PLAZA(
        id = "lz_plaza",
        title = "LZ DELTA // DELAINE AIRFIELD",
        subtitle = "Municipal Runway Hangar, Lake Helena & Rail Yard",
        threatLabel = "EXTREME",
        spawnX = 2120f,
        spawnY = -1960f,
        accentHex = 0xFFA855F7
    );

    companion object {
        fun fromId(id: String): DeploymentZone = entries.firstOrNull { it.id == id } ?: OAKRIDGE_HIGHWAY
    }
}

object BaseUpgradeSpecs {
    const val MAX_LEVEL = 5

    fun bunkSlotsForLevel(level: Int): Int = level.coerceIn(1, MAX_LEVEL)

    fun storageCapForLevel(level: Int): Int = when (level.coerceIn(1, MAX_LEVEL)) {
        1 -> 35
        2 -> 75
        3 -> 150
        4 -> 300
        else -> 600
    }

    fun backpackSlotsForLevel(level: Int): Int = when (level.coerceIn(1, MAX_LEVEL)) {
        1 -> 8
        2 -> 10
        3 -> 12
        4 -> 14
        else -> 16
    }

    fun recruitOperatorCredCost(currentRosterCount: Int): Int = when (currentRosterCount.coerceAtLeast(0)) {
        0 -> 0
        1 -> 600
        2 -> 1400
        3 -> 2600
        else -> 4500
    }

    fun recruitOperatorMats(currentRosterCount: Int): Map<CraftingMaterial, Int> = when (currentRosterCount.coerceAtLeast(0)) {
        0 -> emptyMap()
        1 -> mapOf(
            CraftingMaterial.SCRAP_METAL to 6,
            CraftingMaterial.TACTICAL_CLOTH to 4,
            CraftingMaterial.MEDICAL_HERBS to 2
        )
        2 -> mapOf(
            CraftingMaterial.SCRAP_METAL to 12,
            CraftingMaterial.TACTICAL_CLOTH to 8,
            CraftingMaterial.ELECTRONIC_PARTS to 3,
            CraftingMaterial.MEDICAL_HERBS to 4
        )
        3 -> mapOf(
            CraftingMaterial.SCRAP_METAL to 18,
            CraftingMaterial.TACTICAL_CLOTH to 12,
            CraftingMaterial.ELECTRONIC_PARTS to 6,
            CraftingMaterial.MEDICAL_HERBS to 6
        )
        else -> mapOf(
            CraftingMaterial.SCRAP_METAL to 26,
            CraftingMaterial.TACTICAL_CLOTH to 18,
            CraftingMaterial.ELECTRONIC_PARTS to 10,
            CraftingMaterial.MEDICAL_HERBS to 8
        )
    }

    fun bunkUpgradeCredCost(currentLevel: Int): Int = when (currentLevel) {
        1 -> 350
        2 -> 750
        3 -> 1400
        4 -> 2400
        else -> 0
    }

    fun bunkUpgradeMats(currentLevel: Int): Map<CraftingMaterial, Int> = when (currentLevel) {
        1 -> mapOf(CraftingMaterial.SCRAP_METAL to 6, CraftingMaterial.TACTICAL_CLOTH to 4)
        2 -> mapOf(CraftingMaterial.SCRAP_METAL to 10, CraftingMaterial.TACTICAL_CLOTH to 8, CraftingMaterial.ELECTRONIC_PARTS to 1)
        3 -> mapOf(CraftingMaterial.SCRAP_METAL to 16, CraftingMaterial.TACTICAL_CLOTH to 10, CraftingMaterial.ELECTRONIC_PARTS to 3)
        4 -> mapOf(CraftingMaterial.SCRAP_METAL to 24, CraftingMaterial.TACTICAL_CLOTH to 16, CraftingMaterial.ELECTRONIC_PARTS to 6)
        else -> emptyMap()
    }

    fun storageUpgradeCredCost(currentLevel: Int): Int = when (currentLevel) {
        1 -> 280
        2 -> 600
        3 -> 1100
        4 -> 1900
        else -> 0
    }

    fun storageUpgradeMats(currentLevel: Int): Map<CraftingMaterial, Int> = when (currentLevel) {
        1 -> mapOf(CraftingMaterial.SCRAP_METAL to 5, CraftingMaterial.TACTICAL_CLOTH to 3)
        2 -> mapOf(CraftingMaterial.SCRAP_METAL to 9, CraftingMaterial.TACTICAL_CLOTH to 6, CraftingMaterial.ELECTRONIC_PARTS to 1)
        3 -> mapOf(CraftingMaterial.SCRAP_METAL to 14, CraftingMaterial.TACTICAL_CLOTH to 9, CraftingMaterial.ELECTRONIC_PARTS to 3)
        4 -> mapOf(CraftingMaterial.SCRAP_METAL to 20, CraftingMaterial.TACTICAL_CLOTH to 13, CraftingMaterial.ELECTRONIC_PARTS to 5)
        else -> emptyMap()
    }

    const val MAX_HELIPAD_LEVEL = 1

    fun helipadUpgradeCredCost(currentLevel: Int): Int = if (currentLevel <= 0) 450 else 0

    fun helipadUpgradeMats(currentLevel: Int): Map<CraftingMaterial, Int> = if (currentLevel <= 0) {
        mapOf(
            CraftingMaterial.SCRAP_METAL to 8,
            CraftingMaterial.TACTICAL_CLOTH to 4,
            CraftingMaterial.ELECTRONIC_PARTS to 3
        )
    } else {
        emptyMap()
    }

    const val MAX_RELOADING_GEAR_LEVEL = 3

    fun reloadingUpgradeCredCost(currentLevel: Int): Int = when (currentLevel) {
        0 -> 260
        1 -> 620
        2 -> 1150
        else -> 0
    }

    fun reloadingUpgradeMats(currentLevel: Int): Map<CraftingMaterial, Int> = when (currentLevel) {
        0 -> mapOf(
            CraftingMaterial.SCRAP_METAL to 5,
            CraftingMaterial.INDUSTRIAL_FASTENERS to 4,
            CraftingMaterial.GUN_LUBE to 2
        )
        1 -> mapOf(
            CraftingMaterial.FORGED_RECEIVER to 2,
            CraftingMaterial.COPPER_WIRING to 3,
            CraftingMaterial.ELECTRONIC_PARTS to 2,
            CraftingMaterial.GUN_LUBE to 2
        )
        2 -> mapOf(
            CraftingMaterial.FORGED_RECEIVER to 3,
            CraftingMaterial.ELECTRONIC_PARTS to 4,
            CraftingMaterial.OPTICAL_GLASS to 2,
            CraftingMaterial.LITHIUM_CELLS to 3
        )
        else -> emptyMap()
    }

    fun reloadingGearTitle(level: Int): String = when (level.coerceIn(0, MAX_RELOADING_GEAR_LEVEL)) {
        0 -> "Uninstalled (No Reloading Bench)"
        1 -> "Lv.1 • Single-Stage Bench Press"
        2 -> "Lv.2 • Progressive Turret Press"
        else -> "Lv.3 • Precision Match & Magnum Rig"
    }

    fun reloadingGearDescription(level: Int): String = when (level.coerceIn(0, MAX_RELOADING_GEAR_LEVEL)) {
        0 -> "Install reloading dies at the HQ Terminal to craft live pistol, shotgun, rifle, and belt-fed ammunition."
        1 -> "Enables crafting 9x19mm, .45 ACP, Magnum Pistol (.44/.50 AE/5.7mm), and 12-Gauge 00 Buckshot."
        2 -> "Adds automatic powder metering & progressive dies for SMG/PDW bulk lots, 5.56mm NATO, 5.45mm, and 7.62x39mm."
        else -> "Digital powder trickler & concentricity gauges unlock 7.62x51mm NATO, .338 Lapua, .50 BMG & 100-Rd LMG Belts."
    }
}

enum class TaskGoalKind {
    KILL_ZOMBIES,
    SEARCH_LOOT,
    CRAFT_GEAR,
    EXTRACT_RUNS,
    UPGRADE_BASE
}

/**
 * Tasks assigned directly by HQ Command are called MISSIONS.
 */
data class HqMissionDef(
    val id: String,
    val code: String,
    val title: String,
    val briefing: String,
    val goalKind: TaskGoalKind,
    val targetCount: Int,
    val rewardCred: Int,
    val rewardMats: Map<CraftingMaterial, Int>
)

object HqMissionCatalog {
    val missions: List<HqMissionDef> = listOf(
        HqMissionDef(
            id = "hq_1",
            code = "OP-01",
            title = "Sector Reconnaissance",
            briefing = "Scout quiet DeLaine streets and search 3 containers or vehicles for salvage.",
            goalKind = TaskGoalKind.SEARCH_LOOT,
            targetCount = 3,
            rewardCred = 160,
            rewardMats = mapOf(CraftingMaterial.SCRAP_METAL to 4, CraftingMaterial.BRASS_CASINGS to 4, CraftingMaterial.GUNPOWDER to 2)
        ),
        HqMissionDef(
            id = "hq_2",
            code = "OP-02",
            title = "Deep Block Salvage",
            briefing = "Search 5 more lockers, desks, or vehicle trunks across DeLaine neighborhoods.",
            goalKind = TaskGoalKind.SEARCH_LOOT,
            targetCount = 5,
            rewardCred = 220,
            rewardMats = mapOf(CraftingMaterial.TACTICAL_CLOTH to 4, CraftingMaterial.ELECTRONIC_PARTS to 2, CraftingMaterial.FORGED_RECEIVER to 1)
        ),
        HqMissionDef(
            id = "hq_3",
            code = "OP-03",
            title = "Armory Requisition",
            briefing = "Craft any 2 weapons, armor pieces, ammo boxes, or supply items at the Outpost Workbench.",
            goalKind = TaskGoalKind.CRAFT_GEAR,
            targetCount = 2,
            rewardCred = 260,
            rewardMats = mapOf(CraftingMaterial.SCRAP_METAL to 5, CraftingMaterial.GUNPOWDER to 3, CraftingMaterial.RIFLED_BARREL to 1)
        ),
        HqMissionDef(
            id = "hq_4",
            code = "OP-04",
            title = "Base Expansion",
            briefing = "Upgrade Barracks Bunk, Storage Depot, Reloading Gear, or East Helipad at the HQ Terminal.",
            goalKind = TaskGoalKind.UPGRADE_BASE,
            targetCount = 1,
            rewardCred = 320,
            rewardMats = mapOf(CraftingMaterial.ELECTRONIC_PARTS to 3, CraftingMaterial.TACTICAL_CLOTH to 4, CraftingMaterial.OPTICAL_GLASS to 1)
        ),
        HqMissionDef(
            id = "hq_5",
            code = "OP-05",
            title = "Precision Benchwork",
            briefing = "Fabricate 4 firearms, components, armor rigs, or ammunition batches at the Workbench.",
            goalKind = TaskGoalKind.CRAFT_GEAR,
            targetCount = 4,
            rewardCred = 420,
            rewardMats = mapOf(CraftingMaterial.GUNPOWDER to 6, CraftingMaterial.ELECTRONIC_PARTS to 3, CraftingMaterial.CERAMIC_PLATES to 2)
        ),
        HqMissionDef(
            id = "hq_6",
            code = "OP-06",
            title = "Extraction Veteran",
            briefing = "Complete 2 successful extractions back to Outpost Zero.",
            goalKind = TaskGoalKind.EXTRACT_RUNS,
            targetCount = 2,
            rewardCred = 500,
            rewardMats = mapOf(CraftingMaterial.SCRAP_METAL to 8, CraftingMaterial.MEDICAL_HERBS to 5, CraftingMaterial.RIFLED_BARREL to 2)
        )
    )

    fun byId(id: String): HqMissionDef = missions.firstOrNull { it.id == id } ?: missions.first()
}

/**
 * Requests given by Outpost Zero survivors are called FAVORS (distinct from HQ Missions).
 */
data class SurvivorFavorDef(
    val id: String,
    val npcId: String,
    val npcName: String,
    val title: String,
    val dialogueHook: String,
    val goalKind: TaskGoalKind,
    val targetCount: Int,
    val rewardCred: Int,
    val rewardMats: Map<CraftingMaterial, Int>
)

data class SurvivorNpcDef(
    val id: String,
    val name: String,
    val role: String,
    val stationArea: String,
    val shirtHex: Long,
    val vestHex: Long,
    val skinHex: Long,
    val accentHex: Long,
    val greeting: String
)

object SurvivorCatalog {
    val npcs: List<SurvivorNpcDef> = listOf(
        SurvivorNpcDef(
            id = "npc_vance",
            name = "Vance",
            role = "Quartermaster",
            stationArea = "Armory & Depot",
            shirtHex = 0xFF36454F,
            vestHex = 0xFF78350F,
            skinHex = 0xFFD9A982,
            accentHex = 0xFFFB923C,
            greeting = "Check your plates and chamber before you lift off. Workbench is open if you have salvage."
        ),
        SurvivorNpcDef(
            id = "npc_morales",
            name = "Doc Morales",
            role = "Field Medic",
            stationArea = "Infirmary Bay",
            shirtHex = 0xFF1E3A5F,
            vestHex = 0xFFE2E8F0,
            skinHex = 0xFFC4926C,
            accentHex = 0xFF34D399,
            greeting = "Keep an eye on trauma bleed. If your K-9 catches the scent first, hold back and bandage."
        ),
        SurvivorNpcDef(
            id = "npc_miller",
            name = "Chef Miller",
            role = "Cantine Cook",
            stationArea = "Open-Air Cantine",
            shirtHex = 0xFF475569,
            vestHex = 0xFFB45309,
            skinHex = 0xFFE8BFA0,
            accentHex = 0xFFF59E0B,
            greeting = "Hot stew on the burner when you get back from DeLaine. Grab a ration pack on the counter."
        ),
        SurvivorNpcDef(
            id = "npc_reyes",
            name = "Pilot Reyes",
            role = "Chopper Pilot",
            stationArea = "Flight & Gate Dispatch",
            shirtHex = 0xFF1E293B,
            vestHex = 0xFF166534,
            skinHex = 0xFF8C5A3C,
            accentHex = 0xFF38BDF8,
            greeting = "Walk out the South Gate on foot until we pour the East Helipad upgrade — once built, tap anywhere across DeLaine Metro and I'll drop you straight in."
        )
    )

    val favors: List<SurvivorFavorDef> = listOf(
        SurvivorFavorDef(
            id = "fav_vance_1",
            npcId = "npc_vance",
            npcName = "Vance",
            title = "Forge Test",
            dialogueHook = "Could use help testing our new presses. Craft 1 weapon or armor plate at the bench for me.",
            goalKind = TaskGoalKind.CRAFT_GEAR,
            targetCount = 1,
            rewardCred = 110,
            rewardMats = mapOf(CraftingMaterial.SCRAP_METAL to 4, CraftingMaterial.GUNPOWDER to 2)
        ),
        SurvivorFavorDef(
            id = "fav_morales_1",
            npcId = "npc_morales",
            npcName = "Doc Morales",
            title = "Supply Bag Run",
            dialogueHook = "Search 2 vehicles or supply shelves out in DeLaine so I can keep our triage bay stocked.",
            goalKind = TaskGoalKind.SEARCH_LOOT,
            targetCount = 2,
            rewardCred = 130,
            rewardMats = mapOf(CraftingMaterial.MEDICAL_HERBS to 4, CraftingMaterial.TACTICAL_CLOTH to 2)
        ),
        SurvivorFavorDef(
            id = "fav_miller_1",
            npcId = "npc_miller",
            npcName = "Chef Miller",
            title = "Safe Homeward",
            dialogueHook = "Don't take unnecessary risks out there — complete 1 extraction back to base and dinner's on me.",
            goalKind = TaskGoalKind.EXTRACT_RUNS,
            targetCount = 1,
            rewardCred = 140,
            rewardMats = mapOf(CraftingMaterial.MEDICAL_HERBS to 2, CraftingMaterial.SCRAP_METAL to 3)
        ),
        SurvivorFavorDef(
            id = "fav_reyes_1",
            npcId = "npc_reyes",
            npcName = "Pilot Reyes",
            title = "Flightline Supply Drop",
            dialogueHook = "DeLaine Airfield and downtown are eerily quiet — search 3 vehicles or cargo crates so we can stock up the hangar.",
            goalKind = TaskGoalKind.SEARCH_LOOT,
            targetCount = 3,
            rewardCred = 185,
            rewardMats = mapOf(CraftingMaterial.ELECTRONIC_PARTS to 2, CraftingMaterial.GUNPOWDER to 2, CraftingMaterial.BRASS_CASINGS to 3)
        )
    )

    fun npcById(id: String): SurvivorNpcDef = npcs.firstOrNull { it.id == id } ?: npcs.first()
    fun favorById(id: String): SurvivorFavorDef? = favors.firstOrNull { it.id == id }
    fun favorForNpc(npcId: String): SurvivorFavorDef? = favors.firstOrNull { it.npcId == npcId }
}
