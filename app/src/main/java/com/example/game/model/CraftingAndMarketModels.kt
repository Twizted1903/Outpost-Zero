package com.example.game.model

enum class CraftingMaterialCategory(val label: String) {
    METALS_PARTS("METALS & GUN PARTS"),
    RELOADING("RELOADING & BALLISTICS"),
    POLYMER_OPTICS("POLYMER, WOOD & OPTICS"),
    TEXTILES_ARMOR("TEXTILES & ARMOR"),
    ELECTRONICS("ELECTRONICS & POWER"),
    MEDICAL_CHEM("MEDICAL & CHEMICAL")
}

enum class CraftingMaterial(
    val key: String,
    val displayName: String,
    val shortName: String,
    val matCategory: CraftingMaterialCategory,
    val rarityLabel: String,
    val colorHex: Long,
    val oakridgeSources: String,
    val description: String
) {
    // 1–4: Metals & Gunsmith Components
    SCRAP_METAL(
        key = "mat_scrap",
        displayName = "Scrap Steel & Alloy",
        shortName = "SCRAP",
        matCategory = CraftingMaterialCategory.METALS_PARTS,
        rarityLabel = "COMMON // METAL",
        colorHex = 0xFF94A3B8,
        oakridgeSources = "Vehicles, Hardware Aisles, Basements",
        description = "Salvaged cold-rolled steel brackets, angle iron, and bar stock for frames and tools."
    ),
    FORGED_RECEIVER(
        key = "mat_receiver",
        displayName = "Forged Receiver Blank",
        shortName = "RECEIVER",
        matCategory = CraftingMaterialCategory.METALS_PARTS,
        rarityLabel = "UNCOMMON // GUN PART",
        colorHex = 0xFFCBD5E1,
        oakridgeSources = "District 4 Armory, Gunsmith Lockers",
        description = "Machined upper/lower firearm receiver casting ready for bolt, rail, and barrel fitment."
    ),
    RIFLED_BARREL(
        key = "mat_barrel",
        displayName = "Hammer-Forged Barrel",
        shortName = "BARREL",
        matCategory = CraftingMaterialCategory.METALS_PARTS,
        rarityLabel = "RARE // GUN PART",
        colorHex = 0xFF60A5FA,
        oakridgeSources = "Precinct Armory, Military Crates",
        description = "Chrome-lined precision-rifled alloy barrel blank capable of handling high chamber pressures."
    ),
    TRIGGER_ASSEMBLY(
        key = "mat_trigger",
        displayName = "Machined Trigger Group",
        shortName = "TRIGGER GRP",
        matCategory = CraftingMaterialCategory.METALS_PARTS,
        rarityLabel = "UNCOMMON // GUN PART",
        colorHex = 0xFF93C5FD,
        oakridgeSources = "Precinct Locker, Hardware Workshop",
        description = "Hardened sear, hammer, disconnector, and coil spring pack for reliable fire-control action."
    ),
    INDUSTRIAL_FASTENERS(
        key = "mat_fasteners",
        displayName = "Pins, Springs & Bolts",
        shortName = "FASTENERS",
        matCategory = CraftingMaterialCategory.METALS_PARTS,
        rarityLabel = "COMMON // HARDWARE",
        colorHex = 0xFF9CA3AF,
        oakridgeSources = "Apex Mart, Suburb Basements, Vans",
        description = "Grade-8 hex bolts, roll pins, detents, and recoil springs holding assemblies tight."
    ),
    SUPPRESSOR_BAFFLES(
        key = "mat_baffles",
        displayName = "Titanium Baffle Core",
        shortName = "BAFFLES",
        matCategory = CraftingMaterialCategory.METALS_PARTS,
        rarityLabel = "RARE // SUPPRESSOR",
        colorHex = 0xFF34D399,
        oakridgeSources = "Precinct Spec-Ops Safe, Garage Lathes",
        description = "Stacked conical titanium gas-expansion baffles machined for integrally suppressed builds."
    ),

    // 5–9: Reloading & Ballistics
    GUNPOWDER(
        key = "mat_gunpowder",
        displayName = "Smokeless Powder",
        shortName = "POWDER",
        matCategory = CraftingMaterialCategory.RELOADING,
        rarityLabel = "UNCOMMON // RELOAD",
        colorHex = 0xFFFB923C,
        oakridgeSources = "District 4 Precinct, Police Cruisers",
        description = "Clean-burning double-base nitrocellulose propellant metered at the HQ reloading press."
    ),
    BRASS_CASINGS(
        key = "mat_brass",
        displayName = "Sized Brass Casings",
        shortName = "BRASS",
        matCategory = CraftingMaterialCategory.RELOADING,
        rarityLabel = "COMMON // RELOAD",
        colorHex = 0xFFFBBF24,
        oakridgeSources = "Precinct Range, Cruiser Trunks",
        description = "Annealed and full-length resized cartridge brass ready for primer and powder charge."
    ),
    PERCUSSION_PRIMERS(
        key = "mat_primers",
        displayName = "Boxer Primers",
        shortName = "PRIMERS",
        matCategory = CraftingMaterialCategory.RELOADING,
        rarityLabel = "UNCOMMON // RELOAD",
        colorHex = 0xFFF59E0B,
        oakridgeSources = "Precinct Vault, Sporting Counters",
        description = "Moisture-sealed small and large rifle/pistol primers required for every live round."
    ),
    LEAD_PROJECTILES(
        key = "mat_lead",
        displayName = "Swaged Lead Cores",
        shortName = "LEAD CORES",
        matCategory = CraftingMaterialCategory.RELOADING,
        rarityLabel = "COMMON // RELOAD",
        colorHex = 0xFF64748B,
        oakridgeSources = "Garages, Vehicles, Range Berm",
        description = "Hard-cast antimony lead bullet cores and 00-buckshot pellets for handloading."
    ),
    COPPER_JACKETS(
        key = "mat_copper_jkt",
        displayName = "Copper Bullet Jackets",
        shortName = "COPPER JKT",
        matCategory = CraftingMaterialCategory.RELOADING,
        rarityLabel = "UNCOMMON // RELOAD",
        colorHex = 0xFFD97706,
        oakridgeSources = "Workshop Benches, Industrial Vans",
        description = "Gilding-metal bullet jackets preventing barrel fouling at supersonic rifle velocities."
    ),

    // 10–12: Polymer, Wood & Optics
    POLYMER_STOCK(
        key = "mat_polymer",
        displayName = "Glass-Filled Polymer",
        shortName = "POLYMER",
        matCategory = CraftingMaterialCategory.POLYMER_OPTICS,
        rarityLabel = "COMMON // FRAME",
        colorHex = 0xFF475569,
        oakridgeSources = "Apex Mart, High School, Vehicles",
        description = "Impact-resistant nylon-66 polymer molded into pistol frames, magazines, and handguards."
    ),
    WOOD_FURNITURE(
        key = "mat_wood",
        displayName = "Laminated Walnut Wood",
        shortName = "WALNUT",
        matCategory = CraftingMaterialCategory.POLYMER_OPTICS,
        rarityLabel = "COMMON // STOCK",
        colorHex = 0xFF92400E,
        oakridgeSources = "Maple Suburbs, Woodshops, Basements",
        description = "Seasoned hardwood blanks carved into rifle stocks, pump forends, and tool handles."
    ),
    OPTICAL_GLASS(
        key = "mat_optics",
        displayName = "Multi-Coated Optics Glass",
        shortName = "OPTICS",
        matCategory = CraftingMaterialCategory.POLYMER_OPTICS,
        rarityLabel = "RARE // PRECISION",
        colorHex = 0xFF38BDF8,
        oakridgeSources = "Plaza Tower Offices, School Science Lab",
        description = "Low-dispersion ground prism lenses and etched reticle glass for DMRs and sniper scopes."
    ),

    // 13–15: Textiles & Armor
    TACTICAL_CLOTH(
        key = "mat_cloth",
        displayName = "Ripstop Nylon Cloth",
        shortName = "CLOTH",
        matCategory = CraftingMaterialCategory.TEXTILES_ARMOR,
        rarityLabel = "COMMON // TEXTILE",
        colorHex = 0xFF38BDF8,
        oakridgeSources = "High School Lockers, Maple Homes",
        description = "Heavy-denier Cordura and ripstop webbing stitched into cargo pockets and vest shells."
    ),
    KEVLAR_WEAVE(
        key = "mat_kevlar",
        displayName = "Aramid Kevlar Weave",
        shortName = "KEVLAR",
        matCategory = CraftingMaterialCategory.TEXTILES_ARMOR,
        rarityLabel = "UNCOMMON // ARMOR",
        colorHex = 0xFFFDE047,
        oakridgeSources = "District 4 Precinct, Police Cruisers",
        description = "Para-aramid ballistic fiber sheets stopping fragments and handgun calibers."
    ),
    CERAMIC_PLATES(
        key = "mat_ceramic",
        displayName = "Boron Ceramic Tiles",
        shortName = "CERAMIC",
        matCategory = CraftingMaterialCategory.TEXTILES_ARMOR,
        rarityLabel = "RARE // ARMOR",
        colorHex = 0xFFC084FC,
        oakridgeSources = "Precinct Tactical Locker, Plaza Security",
        description = "Monolithic alumina/boron-carbide strike tiles capable of shattering high-velocity rifle rounds."
    ),

    // 16–18: Electronics & Power
    ELECTRONIC_PARTS(
        key = "mat_electronics",
        displayName = "Circuit Boards & Microchips",
        shortName = "TECH",
        matCategory = CraftingMaterialCategory.ELECTRONICS,
        rarityLabel = "RARE // ELECTRONICS",
        colorHex = 0xFFC084FC,
        oakridgeSources = "Plaza Tower Offices, Precinct Dispatch",
        description = "Precision PCBs, optical sensors, and controllers for digital sights and progressive presses."
    ),
    COPPER_WIRING(
        key = "mat_wiring",
        displayName = "Spool Copper Wiring",
        shortName = "WIRING",
        matCategory = CraftingMaterialCategory.ELECTRONICS,
        rarityLabel = "COMMON // ELECTRONICS",
        colorHex = 0xFFEA580C,
        oakridgeSources = "Utility Vans, Basements, Plaza Maintenance",
        description = "Insulated braided copper loom used for compound wiring, radios, and bench stations."
    ),
    LITHIUM_CELLS(
        key = "mat_battery",
        displayName = "Lithium Power Cells",
        shortName = "CELLS",
        matCategory = CraftingMaterialCategory.ELECTRONICS,
        rarityLabel = "UNCOMMON // POWER",
        colorHex = 0xFF22D3EE,
        oakridgeSources = "Apex Mart Electronics, Plaza Offices",
        description = "High-density rechargeable battery cells powering illuminated optics and bench tools."
    ),

    // 19–24: Medical & Chemical
    MEDICAL_HERBS(
        key = "mat_herbs",
        displayName = "Botanicals & Electrolytes",
        shortName = "HERBS",
        matCategory = CraftingMaterialCategory.MEDICAL_CHEM,
        rarityLabel = "COMMON // BIO",
        colorHex = 0xFF34D399,
        oakridgeSources = "Maple Pantries, Greenhouse, School Nurse",
        description = "Dried medicinal herbs, vitamin salts, and clean botanical extracts for rations and poultices."
    ),
    TRAUMA_GAUZE(
        key = "mat_gauze",
        displayName = "Hemostatic Trauma Gauze",
        shortName = "GAUZE",
        matCategory = CraftingMaterialCategory.MEDICAL_CHEM,
        rarityLabel = "COMMON // MEDICAL",
        colorHex = 0xFFE2E8F0,
        oakridgeSources = "County Ambulances, Nurse Office, Homes",
        description = "Kaolin-impregnated sterile compression bandages for stopping severe trauma bleeding."
    ),
    ANTISEPTIC_SOL(
        key = "mat_antiseptic",
        displayName = "Iodine & Antiseptic",
        shortName = "ANTISEPTIC",
        matCategory = CraftingMaterialCategory.MEDICAL_CHEM,
        rarityLabel = "UNCOMMON // MEDICAL",
        colorHex = 0xFF10B981,
        oakridgeSources = "Ambulances, Pharmacy Shelves, Clinic",
        description = "Medical-grade povidone-iodine and water-purification droplets for sterile kits and canteens."
    ),
    GUN_LUBE(
        key = "mat_gun_lube",
        displayName = "CLP Solvent & Case Lube",
        shortName = "CLP LUBE",
        matCategory = CraftingMaterialCategory.MEDICAL_CHEM,
        rarityLabel = "COMMON // CHEM",
        colorHex = 0xFFF59E0B,
        oakridgeSources = "Vehicles, Hardware Shelves, Precinct",
        description = "Synthetic cleaner-lubricant-protectant and resizing die wax preventing stuck brass and bolt bind."
    );

    companion object {
        fun fromKey(key: String?): CraftingMaterial? = entries.firstOrNull { it.key == key }
    }
}

enum class CraftingTier(val tierNum: Int, val title: String, val badge: String) {
    TIER_1(1, "TIER I // FIELD & SIDEARMS", "TIER I"),
    TIER_2(2, "TIER II // TACTICAL & SMGs", "TIER II"),
    TIER_3(3, "TIER III // SPEC-OPS & HEAVY", "TIER III")
}

enum class CraftOutputKind {
    WEAPON,
    AMMO,
    PANTS,
    ARMOR,
    CONSUMABLE,
    ATTACHMENT
}

data class CraftingRecipe(
    val id: String,
    val name: String,
    val detail: String,
    val tier: CraftingTier,
    val outputKind: CraftOutputKind,
    val rarityLabel: String,
    val rarityColorHex: Long,
    val costs: Map<CraftingMaterial, Int>,
    val weaponKey: String? = null,
    val ammoAmount: Int = 0,
    val pantsLevel: Int? = null,
    val armorLevel: Int? = null,
    val consumableType: LootType? = null,
    val consumableValue: Int = 0,
    val requiredReloadingTier: Int = 0,
    val description: String = detail,
    val attachmentKey: String? = null
)

object CraftingCatalog {
    val recipes: List<CraftingRecipe> = listOf(
        // ============================================================
        // TIER I — IMPROVISED, FIELD SUPPLIES, STARTER SIDEARMS & SHOTGUNS
        // ============================================================
        CraftingRecipe(
            id = "craft_bandage",
            name = "Field Trauma Bandage",
            detail = "+35 HP Hemostatic Wrap",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.CONSUMABLE,
            rarityLabel = "COMMON // MEDICAL",
            rarityColorHex = 0xFF9CA3AF,
            costs = mapOf(
                CraftingMaterial.TRAUMA_GAUZE to 2,
                CraftingMaterial.ANTISEPTIC_SOL to 1,
                CraftingMaterial.TACTICAL_CLOTH to 1
            ),
            consumableType = LootType.MEDKIT,
            consumableValue = 35,
            description = "Sterile kaolin-soaked compression bandage packed into a waterproof field pouch."
        ),
        CraftingRecipe(
            id = "craft_water",
            name = "Purified Canteen",
            detail = "+45 Hydration",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.CONSUMABLE,
            rarityLabel = "COMMON // RATION",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.ANTISEPTIC_SOL to 1,
                CraftingMaterial.MEDICAL_HERBS to 1,
                CraftingMaterial.SCRAP_METAL to 1
            ),
            consumableType = LootType.WATER,
            consumableValue = 45,
            description = "Steel canteen filled with iodine-treated spring water and electrolyte salts."
        ),
        CraftingRecipe(
            id = "craft_ration",
            name = "Field Ration Pack",
            detail = "+45 Nutrition",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.CONSUMABLE,
            rarityLabel = "COMMON // RATION",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.MEDICAL_HERBS to 2,
                CraftingMaterial.TACTICAL_CLOTH to 1
            ),
            consumableType = LootType.FOOD,
            consumableValue = 45,
            description = "High-calorie sealed survival meal pack wrapped for long Oakridge sweeps."
        ),
        CraftingRecipe(
            id = "craft_baton",
            name = "Monadnock Police Baton",
            detail = "38 DMG Friction-Lock Melee",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "COMMON // PATROL",
            rarityColorHex = 0xFF9CA3AF,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 2,
                CraftingMaterial.POLYMER_STOCK to 1,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 1
            ),
            weaponKey = "baton",
            description = "Telescoping alloy baton assembled with knurled polymer grip and hardened tip."
        ),
        CraftingRecipe(
            id = "craft_crowbar",
            name = "Stanley Wrecking Crowbar",
            detail = "46 DMG Heavy Salvage Melee",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "COMMON // SALVAGE",
            rarityColorHex = 0xFF9CA3AF,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 3,
                CraftingMaterial.GUN_LUBE to 1
            ),
            weaponKey = "crowbar",
            description = "Heavy drop-forged hex steel pry bar with oiled anti-rust finish."
        ),
        CraftingRecipe(
            id = "craft_armor_1",
            name = "Padded Improvised Vest (Lv.1)",
            detail = "40 Armor Shield • 25% Mitigation",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.ARMOR,
            rarityLabel = "COMMON // ARMOR",
            rarityColorHex = 0xFF9CA3AF,
            costs = mapOf(
                CraftingMaterial.TACTICAL_CLOTH to 2,
                CraftingMaterial.SCRAP_METAL to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            armorLevel = 1,
            description = "Quilted ripstop carrier lined with hammered mild-steel trauma backing."
        ),
        CraftingRecipe(
            id = "craft_pants_2",
            name = "Cargo Pants Lv.2",
            detail = "2 Safe Pockets",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.PANTS,
            rarityLabel = "UNCOMMON // WORK",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.TACTICAL_CLOTH to 4,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2,
                CraftingMaterial.KEVLAR_WEAVE to 1
            ),
            pantsLevel = 2,
            description = "Reinforced ripstop utility pants adding a second concealed Safe Pocket."
        ),
        CraftingRecipe(
            id = "craft_ppk",
            name = "Walther PPK/S .380",
            detail = ".380 ACP Compact Sidearm (+21 Rounds)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "COMMON // COMPACT",
            rarityColorHex = 0xFF9CA3AF,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 2,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            weaponKey = "ppk",
            ammoAmount = 21,
            description = "Compact all-steel straight-blowback .380 pistol machined from bar stock."
        ),
        CraftingRecipe(
            id = "craft_glock19",
            name = "Glock 19 Gen5 MOS",
            detail = "9x19mm Sidearm (+30 Rounds)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "COMMON // SIDEARM",
            rarityColorHex = 0xFF9CA3AF,
            costs = mapOf(
                CraftingMaterial.POLYMER_STOCK to 2,
                CraftingMaterial.FORGED_RECEIVER to 1,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1,
                CraftingMaterial.GUN_LUBE to 1
            ),
            weaponKey = "glock19",
            ammoAmount = 30,
            description = "Polymer-framed 15-round 9mm service handgun paired with a machined slide."
        ),
        CraftingRecipe(
            id = "craft_double_barrel",
            name = "Stoeger Coach 12G Double-Barrel",
            detail = "12G Twin-Blast Shotgun (+12 Shells)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "COMMON // FARMHOUSE",
            rarityColorHex = 0xFF9CA3AF,
            costs = mapOf(
                CraftingMaterial.WOOD_FURNITURE to 3,
                CraftingMaterial.SCRAP_METAL to 3,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            weaponKey = "double_barrel",
            ammoAmount = 12,
            description = "Twin-barrel break-action 12-gauge fitted to a hand-shaped walnut stock."
        ),
        // TIER 1 RELOADING PRESS AMMO (Requires HQ Reloading Gear Lv.1)
        CraftingRecipe(
            id = "craft_ammo_9mm_pistol",
            name = "9x19mm Parabellum Box (+45)",
            detail = "Glock 19 / M9A3 / P226 / CZ75 / PPK",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.AMMO,
            rarityLabel = "COMMON // AMMO",
            rarityColorHex = 0xFF9CA3AF,
            costs = mapOf(
                CraftingMaterial.BRASS_CASINGS to 2,
                CraftingMaterial.GUNPOWDER to 1,
                CraftingMaterial.PERCUSSION_PRIMERS to 1,
                CraftingMaterial.LEAD_PROJECTILES to 1
            ),
            weaponKey = "glock19",
            ammoAmount = 45,
            requiredReloadingTier = 1,
            description = "Handloaded 124-grain FMJ 9mm rounds pressed on the HQ Single-Stage Bench Press."
        ),
        CraftingRecipe(
            id = "craft_ammo_45_pistol",
            name = ".45 ACP Match & Subsonic (+36)",
            detail = "M1911A1 / USP-45 SD / UMP-45 / Vector",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.AMMO,
            rarityLabel = "UNCOMMON // AMMO",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.BRASS_CASINGS to 2,
                CraftingMaterial.GUNPOWDER to 2,
                CraftingMaterial.PERCUSSION_PRIMERS to 1,
                CraftingMaterial.LEAD_PROJECTILES to 2
            ),
            weaponKey = "m1911",
            ammoAmount = 36,
            requiredReloadingTier = 1,
            description = "Heavy 230-grain .45 ACP ball and subsonic rounds loaded at HQ Reloading Lv.1."
        ),
        CraftingRecipe(
            id = "craft_ammo_magnum_pistol",
            name = ".44 Mag / .50 AE / 5.7mm Pack (+30)",
            detail = "Model 29 / Desert Eagle / Five-seveN",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.AMMO,
            rarityLabel = "RARE // AMMO",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.BRASS_CASINGS to 2,
                CraftingMaterial.GUNPOWDER to 2,
                CraftingMaterial.PERCUSSION_PRIMERS to 2,
                CraftingMaterial.COPPER_JACKETS to 2
            ),
            weaponKey = "deagle",
            ammoAmount = 30,
            requiredReloadingTier = 1,
            description = "Jacketed heavy magnum & armor-piercing 5.7mm pistol cartridges."
        ),
        CraftingRecipe(
            id = "craft_ammo_shotgun",
            name = "12 Gauge 00 Buckshot (+20)",
            detail = "All Pump, Semi & Auto 12G Shotguns",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.AMMO,
            rarityLabel = "UNCOMMON // AMMO",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.POLYMER_STOCK to 1,
                CraftingMaterial.GUNPOWDER to 2,
                CraftingMaterial.PERCUSSION_PRIMERS to 1,
                CraftingMaterial.LEAD_PROJECTILES to 2
            ),
            weaponKey = "shotgun",
            ammoAmount = 20,
            requiredReloadingTier = 1,
            description = "12-gauge 2.75-inch ribbed hulls crimped with 9 hardened 00-buck lead pellets."
        ),

        // ============================================================
        // TIER II — TACTICAL GEAR, PISTOLS, SMGs, PUMP SHOTGUNS
        // ============================================================
        CraftingRecipe(
            id = "craft_ifak",
            name = "IFAK Trauma Kit",
            detail = "+70 HP Emergency Kit",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.CONSUMABLE,
            rarityLabel = "RARE // TRAUMA",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.TRAUMA_GAUZE to 3,
                CraftingMaterial.ANTISEPTIC_SOL to 2,
                CraftingMaterial.MEDICAL_HERBS to 2,
                CraftingMaterial.TACTICAL_CLOTH to 2
            ),
            consumableType = LootType.MEDKIT,
            consumableValue = 70,
            description = "Complete combat lifesaver kit with chest seal, hemostatic pack, and antiseptic flush."
        ),
        CraftingRecipe(
            id = "craft_machete",
            name = "Ontario Kukri Machete",
            detail = "48 DMG Wide-Arc Chop Blade",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "UNCOMMON // FIELD",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 4,
                CraftingMaterial.WOOD_FURNITURE to 1,
                CraftingMaterial.TACTICAL_CLOTH to 2
            ),
            weaponKey = "machete",
            description = "Full-tang 1095 carbon-steel kukri blade with cord-wrapped hardwood scales."
        ),
        CraftingRecipe(
            id = "craft_tomahawk",
            name = "RMJ Tactical Tomahawk",
            detail = "52 DMG Fast Breaching Hawk",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "UNCOMMON // TACTICAL",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 4,
                CraftingMaterial.POLYMER_STOCK to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            weaponKey = "tomahawk",
            description = "Machined 4140 steel breaching hawk with textured G10/polymer handle scales."
        ),
        CraftingRecipe(
            id = "craft_armor_2",
            name = "Precinct Riot Rig (Lv.2)",
            detail = "75 Armor Shield • 40% Mitigation",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ARMOR,
            rarityLabel = "UNCOMMON // ARMOR",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.KEVLAR_WEAVE to 3,
                CraftingMaterial.POLYMER_STOCK to 2,
                CraftingMaterial.TACTICAL_CLOTH to 3,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            armorLevel = 2,
            description = "Aramid Kevlar soft vest layered beneath rigid molded polymer trauma shells."
        ),
        CraftingRecipe(
            id = "craft_pants_3",
            name = "Tactical Pants Lv.3",
            detail = "3 Safe Pockets",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.PANTS,
            rarityLabel = "RARE // TACTICAL",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.TACTICAL_CLOTH to 5,
                CraftingMaterial.KEVLAR_WEAVE to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 3
            ),
            pantsLevel = 3,
            description = "Kevlar-gusseted Crye-style field trousers expanding Safe Pocket storage to 3 slots."
        ),
        CraftingRecipe(
            id = "craft_m1911",
            name = "Colt M1911A1 .45 ACP",
            detail = ".45 ACP Steel Frame (+28 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "UNCOMMON // HERITAGE",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 1,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1,
                CraftingMaterial.WOOD_FURNITURE to 1
            ),
            weaponKey = "m1911",
            ammoAmount = 28,
            description = "All-steel .45 ACP service pistol fitted with checkered walnut grip panels."
        ),
        CraftingRecipe(
            id = "craft_beretta_m9",
            name = "Beretta M9A3 Tactical",
            detail = "9x19mm Open-Slide (+34 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "UNCOMMON // SERVICE",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 1,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1,
                CraftingMaterial.GUN_LUBE to 1
            ),
            weaponKey = "beretta_m9",
            ammoAmount = 34,
            description = "Open-top falling-block 9mm pistol with FDE Cerakote slide and threaded barrel."
        ),
        CraftingRecipe(
            id = "craft_p226",
            name = "SIG P226 Mk25 Navy",
            detail = "9x19mm Alloy Frame (+36 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "UNCOMMON // NAVY",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 1,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1,
                CraftingMaterial.POLYMER_STOCK to 1
            ),
            weaponKey = "p226",
            ammoAmount = 36,
            description = "Phosphate-coated naval duty handgun with precision-crowned chrome barrel."
        ),
        CraftingRecipe(
            id = "craft_cz75",
            name = "CZ 75 SP-01 Shadow",
            detail = "9x19mm Match Pistol (+36 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // MATCH",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 1,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2,
                CraftingMaterial.GUN_LUBE to 2
            ),
            weaponKey = "cz75",
            ammoAmount = 36,
            description = "Hand-fitted steel-frame match handgun riding inside full-length slide rails."
        ),
        CraftingRecipe(
            id = "craft_usp45",
            name = "HK USP-45 Tactical SD",
            detail = "Suppressed .45 ACP (+24 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // STEALTH",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 1,
                CraftingMaterial.SUPPRESSOR_BAFFLES to 2,
                CraftingMaterial.POLYMER_STOCK to 1
            ),
            weaponKey = "usp45",
            ammoAmount = 24,
            description = "Threaded O-ring match .45 paired with a titanium baffled wet suppressor."
        ),
        CraftingRecipe(
            id = "craft_mac10",
            name = "Ingram MAC-10 .45",
            detail = "1,090 RPM Machine Pistol (+60 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "COMMON // MACHINE PISTOL",
            rarityColorHex = 0xFF9CA3AF,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 4,
                CraftingMaterial.RIFLED_BARREL to 1,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 3
            ),
            weaponKey = "mac10",
            ammoAmount = 60,
            description = "Stamped sheet-metal open-bolt .45 ACP submachine gun with wire collapsing stock."
        ),
        CraftingRecipe(
            id = "craft_smg",
            name = "HK MP5A3 SMG",
            detail = "9x19mm Roller-Delayed (+60 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "UNCOMMON // TACTICAL",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 1,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2,
                CraftingMaterial.POLYMER_STOCK to 2
            ),
            weaponKey = "smg",
            ammoAmount = 60,
            description = "Roller-delayed closed-bolt 9mm submachine gun tuned for surgical bursts."
        ),
        CraftingRecipe(
            id = "craft_ump45",
            name = "HK UMP-45",
            detail = ".45 ACP Tactical SMG (+50 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "UNCOMMON // CQB",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.POLYMER_STOCK to 3,
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 1,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1
            ),
            weaponKey = "ump45",
            ammoAmount = 50,
            description = "Modern glass-filled polymer submachine gun chambered in hard-hitting .45 ACP."
        ),
        CraftingRecipe(
            id = "craft_bizon",
            name = "PP-19 Bizon-2 Helical",
            detail = "64-Rd Helical 9x18mm SMG (+128 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // HELICAL 64-RD",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 1,
                CraftingMaterial.POLYMER_STOCK to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 4
            ),
            weaponKey = "bizon",
            ammoAmount = 128,
            description = "Stamped AK-pattern submachine gun fed by a 64-round underbarrel helical magazine."
        ),
        CraftingRecipe(
            id = "craft_crossbow",
            name = "PSE TAC-15 Crossbow",
            detail = "95 DMG Silent Carbon Bolts (+18 Bolts)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // SILENT",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 4,
                CraftingMaterial.POLYMER_STOCK to 2,
                CraftingMaterial.OPTICAL_GLASS to 1,
                CraftingMaterial.COPPER_WIRING to 2
            ),
            weaponKey = "crossbow",
            ammoAmount = 18,
            description = "AR-chassis split-limb compound rail launching silent 22-inch broadhead bolts."
        ),
        CraftingRecipe(
            id = "craft_shotgun",
            name = "Remington 870 Police Magnum",
            detail = "12G 6-Shell Pump Shotgun (+24 Shells)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // PRECINCT",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.WOOD_FURNITURE to 2,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1
            ),
            weaponKey = "shotgun",
            ammoAmount = 24,
            description = "Twin-action-bar steel-receiver police pump shotgun built for heavy doors and buckshot."
        ),
        CraftingRecipe(
            id = "craft_mossberg_590",
            name = "Mossberg 590A1 Heavy-Walled",
            detail = "12G 8-Shell Mil-Spec Pump (+24 Shells)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // MIL-SPEC 12G",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.POLYMER_STOCK to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 3
            ),
            weaponKey = "mossberg_590",
            ammoAmount = 24,
            description = "Parkerized heavy-barrel naval pump shotgun with 8-shell capacity and bayonet lug."
        ),
        // TIER 2 RELOADING PRESS AMMO (Requires HQ Reloading Gear Lv.2)
        CraftingRecipe(
            id = "craft_ammo_smg_box",
            name = "9mm & 4.6/5.7 PDW Case (+90)",
            detail = "MP5 / MP5SD / EVO3 / P90 / MP7 / Bizon",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.AMMO,
            rarityLabel = "UNCOMMON // AMMO",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.BRASS_CASINGS to 3,
                CraftingMaterial.GUNPOWDER to 2,
                CraftingMaterial.PERCUSSION_PRIMERS to 2,
                CraftingMaterial.COPPER_JACKETS to 2
            ),
            weaponKey = "smg",
            ammoAmount = 90,
            requiredReloadingTier = 2,
            description = "High-volume progressive-press copper-jacketed SMG and armor-piercing PDW bulk lot."
        ),
        CraftingRecipe(
            id = "craft_ammo_556_box",
            name = "5.56x45mm & 5.45mm NATO Case (+60)",
            detail = "M4A1 / MK18 / HK416 / AUG / FAMAS / AK74 / RPK",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.AMMO,
            rarityLabel = "RARE // AMMO",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.BRASS_CASINGS to 3,
                CraftingMaterial.GUNPOWDER to 3,
                CraftingMaterial.PERCUSSION_PRIMERS to 2,
                CraftingMaterial.COPPER_JACKETS to 2,
                CraftingMaterial.LEAD_PROJECTILES to 1
            ),
            weaponKey = "m4",
            ammoAmount = 60,
            requiredReloadingTier = 2,
            description = "Full-length resized 5.56mm NATO M855 & 5.45mm spitzer boat-tail rifle cartridges."
        ),
        CraftingRecipe(
            id = "craft_ammo_762x39_box",
            name = "7.62x39mm & 9x39mm Case (+60)",
            detail = "AK-47 / AK-103 / AS Val Subsonic",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.AMMO,
            rarityLabel = "RARE // AMMO",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.BRASS_CASINGS to 3,
                CraftingMaterial.GUNPOWDER to 3,
                CraftingMaterial.PERCUSSION_PRIMERS to 2,
                CraftingMaterial.COPPER_JACKETS to 2
            ),
            weaponKey = "ak103",
            ammoAmount = 60,
            requiredReloadingTier = 2,
            description = "Tapered-wall 7.62x39mm M43 & subsonic 9x39mm AP projectiles crimped on progressive dies."
        ),

        // ============================================================
        // TIER III — SPEC-OPS ARSENAL, ASSAULT/BATTLE RIFLES, SNIPERS & LMGs
        // ============================================================
        CraftingRecipe(
            id = "craft_fire_axe",
            name = "Council Tool Fire Axe",
            detail = "68 DMG Heavy Breaching Axe",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // RESCUE",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 5,
                CraftingMaterial.WOOD_FURNITURE to 3,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            weaponKey = "fire_axe",
            description = "Drop-forged 6-pound pickhead rescue axe mounted on straight-grain American hickory."
        ),
        CraftingRecipe(
            id = "craft_sledge",
            name = "Fiskars IsoCore 10lb Sledge",
            detail = "88 DMG Demolition Maul",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // DEMOLITION",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 7,
                CraftingMaterial.POLYMER_STOCK to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 3
            ),
            weaponKey = "sledgehammer",
            description = "Shock-damped demolition maul delivering crushing entry kinetic force."
        ),
        CraftingRecipe(
            id = "craft_katana",
            name = "Cold Steel Warrior Wakizashi",
            detail = "64 DMG Tempered Long Blade",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "GRAIL // BLADE",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.SCRAP_METAL to 5,
                CraftingMaterial.TACTICAL_CLOTH to 3,
                CraftingMaterial.GUN_LUBE to 2
            ),
            weaponKey = "katana",
            description = "Differential-tempered high-carbon wakizashi wrapped in cord over ray-skin grip."
        ),
        CraftingRecipe(
            id = "craft_armor_3",
            name = "Tactical Plate Carrier (Lv.3)",
            detail = "115 Armor Shield • 55% Mitigation",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.ARMOR,
            rarityLabel = "RARE // ARMOR",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.TACTICAL_CLOTH to 5,
                CraftingMaterial.KEVLAR_WEAVE to 4,
                CraftingMaterial.CERAMIC_PLATES to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 3
            ),
            armorLevel = 3,
            description = "Laser-cut Cordura JPC carrier housing twin curved ceramic Level-III+ chest plates."
        ),
        CraftingRecipe(
            id = "craft_armor_4",
            name = "Spec-Ops Ceramic Rig (Lv.4)",
            detail = "160 Armor Shield • 68% Mitigation",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.ARMOR,
            rarityLabel = "GRAIL // SPEC-OPS",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.KEVLAR_WEAVE to 6,
                CraftingMaterial.CERAMIC_PLATES to 5,
                CraftingMaterial.TACTICAL_CLOTH to 6,
                CraftingMaterial.ELECTRONIC_PARTS to 2
            ),
            armorLevel = 4,
            description = "Full-coverage Boron-Carbide Level-IV assault carrier with reinforced shoulder & neck armor."
        ),
        CraftingRecipe(
            id = "craft_pants_4",
            name = "Spec-Ops Pants Lv.4",
            detail = "4 Safe Pockets (Max)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.PANTS,
            rarityLabel = "GRAIL // SPEC-OPS",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.TACTICAL_CLOTH to 6,
                CraftingMaterial.KEVLAR_WEAVE to 4,
                CraftingMaterial.ELECTRONIC_PARTS to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 3
            ),
            pantsLevel = 4,
            description = "Top-tier armored combat trousers concealing 4 reinforced Safe Pockets."
        ),
        CraftingRecipe(
            id = "craft_five_seven",
            name = "FN Five-seveN Mk2",
            detail = "5.7x28mm AP 20-Rd Sidearm (+40 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // AP SIDEARM",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.POLYMER_STOCK to 3,
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 1,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1
            ),
            weaponKey = "five_seven",
            ammoAmount = 40,
            description = "High-velocity delayed-blowback 5.7mm pistol carrying 20 armor-piercing rounds."
        ),
        CraftingRecipe(
            id = "craft_model_29",
            name = "S&W Model 29 .44 Magnum",
            detail = ".44 Magnum N-Frame Revolver (+18 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // REVOLVER",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 1,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2,
                CraftingMaterial.WOOD_FURNITURE to 1
            ),
            weaponKey = "model_29",
            ammoAmount = 18,
            description = "Heavy N-frame 6-shot magnum revolver with precision cylinder timing and walnut grips."
        ),
        CraftingRecipe(
            id = "craft_deagle",
            name = "Desert Eagle Mark XIX .50 AE",
            detail = "68 DMG Gas-Operated .50 AE (+21 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "GRAIL // MAGNUM",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 4,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2,
                CraftingMaterial.GUN_LUBE to 2
            ),
            weaponKey = "deagle",
            ammoAmount = 21,
            description = "Rotating-bolt gas-operated .50 AE magnum handgun milled from solid billet steel."
        ),
        CraftingRecipe(
            id = "craft_mp5sd",
            name = "HK MP5SD6 Suppressed",
            detail = "Integrally Suppressed 9mm (+60 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // STEALTH SMG",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 1,
                CraftingMaterial.SUPPRESSOR_BAFFLES to 3,
                CraftingMaterial.POLYMER_STOCK to 2
            ),
            weaponKey = "mp5sd",
            ammoAmount = 60,
            description = "Ported-barrel roller-delayed 9mm SMG wrapped in an aerospace titanium suppressor shroud."
        ),
        CraftingRecipe(
            id = "craft_scorpion_evo",
            name = "CZ Scorpion EVO 3 A1",
            detail = "1,150 RPM Polymer SMG (+60 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // MODERN SMG",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.POLYMER_STOCK to 4,
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 1,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2
            ),
            weaponKey = "scorpion_evo",
            ammoAmount = 60,
            description = "FDE fiber-polymer submachine gun with rapid cyclic bolt and ambidextrous selector."
        ),
        CraftingRecipe(
            id = "craft_p90",
            name = "FN P90 TR PDW",
            detail = "50-Rd Top-Fed 5.7mm PDW (+100 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // PDW",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.POLYMER_STOCK to 4,
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.OPTICAL_GLASS to 1
            ),
            weaponKey = "p90",
            ammoAmount = 100,
            description = "Bullpup personal defense weapon with 50-round spiral feed ramp and compact rail."
        ),
        CraftingRecipe(
            id = "craft_mp7",
            name = "HK MP7A1 4.6x30mm",
            detail = "High-Penetration Gas PDW (+80 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "GRAIL // AP PDW",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.POLYMER_STOCK to 3,
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.ELECTRONIC_PARTS to 2
            ),
            weaponKey = "mp7",
            ammoAmount = 80,
            description = "Short-stroke gas-piston PDW built around hyper-velocity 4.6mm AP rounds."
        ),
        CraftingRecipe(
            id = "craft_vector",
            name = "KRISS Vector .45",
            detail = "Super V 1,200 RPM SMG (+66 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "GRAIL // SMG",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.POLYMER_STOCK to 4,
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2,
                CraftingMaterial.ELECTRONIC_PARTS to 2
            ),
            weaponKey = "vector",
            ammoAmount = 66,
            description = "Off-axis Super V articulated bolt channeling .45 ACP recoil downward."
        ),
        CraftingRecipe(
            id = "craft_benelli_m4",
            name = "Benelli M4 Super 90",
            detail = "ARGO Semi-Auto 12G (+24 Shells)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // BREACHER",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.POLYMER_STOCK to 2,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2
            ),
            weaponKey = "benelli_m4",
            ammoAmount = 24,
            description = "Dual-piston self-regulating ARGO semi-automatic 12-gauge combat shotgun."
        ),
        CraftingRecipe(
            id = "craft_spas12",
            name = "Franchi SPAS-12",
            detail = "Dual-Mode 12G Combat Shotgun (+24 Shells)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // DUAL-MODE",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.SCRAP_METAL to 4,
                CraftingMaterial.POLYMER_STOCK to 2
            ),
            weaponKey = "spas12",
            ammoAmount = 24,
            description = "Perforated heat-shield tactical shotgun alternating between gas semi and manual pump."
        ),
        CraftingRecipe(
            id = "craft_saiga12",
            name = "Izhmash Saiga-12K",
            detail = "Box-Fed Semi-Auto 12G (+30 Shells)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // MAG-FED 12G",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.POLYMER_STOCK to 3,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2
            ),
            weaponKey = "saiga12",
            ammoAmount = 30,
            description = "Long-stroke AK-pattern 12-gauge accepting 10-shell detachable box magazines."
        ),
        CraftingRecipe(
            id = "craft_aa12",
            name = "MPS AA-12 Auto-12",
            detail = "20-Rd Full-Auto 12G Drum (+40 Shells)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "GRAIL // AUTO-12G",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 4,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.POLYMER_STOCK to 3,
                CraftingMaterial.ELECTRONIC_PARTS to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 4
            ),
            weaponKey = "aa12",
            ammoAmount = 40,
            description = "Constant-recoil stainless automatic combat shotgun cycling a 20-shell drum."
        ),
        CraftingRecipe(
            id = "craft_ak47",
            name = "Kalashnikov AKM / AK-47",
            detail = "7.62x39mm Assault Rifle (+60 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "UNCOMMON // CLASSIC",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.WOOD_FURNITURE to 3,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1
            ),
            weaponKey = "ak47",
            ammoAmount = 60,
            description = "Stamped steel AKM fitted with laminated wood furniture and chrome-lined 7.62 barrel."
        ),
        CraftingRecipe(
            id = "craft_ak74m",
            name = "Izhmash AK-74M",
            detail = "5.45x39mm Polymer Rifle (+60 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // INFANTRY",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.POLYMER_STOCK to 3,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2
            ),
            weaponKey = "ak74m",
            ammoAmount = 60,
            description = "Modernized 5.45mm Kalashnikov with side-folding polymer stock and dual-port brake."
        ),
        CraftingRecipe(
            id = "craft_ak103",
            name = "Kalashnikov AK-103",
            detail = "7.62x39mm Heavy Carbine (+60 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // CARBINE",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.POLYMER_STOCK to 3,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2
            ),
            weaponKey = "ak103",
            ammoAmount = 60,
            description = "Polymer AK-100 series 7.62x39mm rifle combining classic barrier punch with modern ergonomics."
        ),
        CraftingRecipe(
            id = "craft_m4",
            name = "Colt M4A1 SOPMOD Carbine",
            detail = "5.56mm NATO Carbine (+60 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // MIL-SPEC",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.POLYMER_STOCK to 2,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2,
                CraftingMaterial.OPTICAL_GLASS to 1
            ),
            weaponKey = "m4",
            ammoAmount = 60,
            description = "Mil-Spec direct-impingement 5.56mm carbine with quad-rail handguard and optic mount."
        ),
        CraftingRecipe(
            id = "craft_mk18",
            name = "Daniel Defense MK18 CQBR",
            detail = "10.3\" Spec-Ops 5.56mm (+60 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // CQB CARBINE",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.POLYMER_STOCK to 2,
                CraftingMaterial.OPTICAL_GLASS to 1,
                CraftingMaterial.ELECTRONIC_PARTS to 1
            ),
            weaponKey = "mk18",
            ammoAmount = 60,
            description = "Short-barreled RIS-II rail CQB receiver engineered for fast interior room entry."
        ),
        CraftingRecipe(
            id = "craft_aug_a3",
            name = "Steyr AUG A3 M1 Bullpup",
            detail = "5.56mm NATO Bullpup (+60 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // BULLPUP",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.POLYMER_STOCK to 4,
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.OPTICAL_GLASS to 2
            ),
            weaponKey = "aug_a3",
            ammoAmount = 60,
            description = "Quick-change barrel Austrian bullpup housing a full 16\" barrel in a compact shell."
        ),
        CraftingRecipe(
            id = "craft_famas",
            name = "GIAT FAMAS F1 Bullpup",
            detail = "High-ROF 5.56mm Bullpup (+50 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // HIGH-ROF",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.POLYMER_STOCK to 3,
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2
            ),
            weaponKey = "famas",
            ammoAmount = 50,
            description = "Lever-delayed blowback French bullpup cycling at over 1,000 rounds per minute."
        ),
        CraftingRecipe(
            id = "craft_g36c",
            name = "HK G36C Compact",
            detail = "Polymer Piston 5.56mm (+60 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // TACTICAL",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.POLYMER_STOCK to 4,
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.OPTICAL_GLASS to 1
            ),
            weaponKey = "g36c",
            ammoAmount = 60,
            description = "Carbon-polymer folding-stock SWAT carbine with short-stroke self-cleaning piston."
        ),
        CraftingRecipe(
            id = "craft_hk416",
            name = "HK416A5 Spec-Ops Carbine",
            detail = "Match Piston 5.56mm (+60 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "GRAIL // SPEC-OPS",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 4,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2,
                CraftingMaterial.OPTICAL_GLASS to 2,
                CraftingMaterial.ELECTRONIC_PARTS to 2
            ),
            weaponKey = "hk416",
            ammoAmount = 60,
            description = "Elite short-stroke gas-piston carbine machined for zero-malfunction sustained fire."
        ),
        CraftingRecipe(
            id = "craft_as_val",
            name = "AS Val Suppressed Rifle",
            detail = "Integrally Suppressed 9x39mm (+40 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "GRAIL // STEALTH",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.SUPPRESSOR_BAFFLES to 4,
                CraftingMaterial.ELECTRONIC_PARTS to 2
            ),
            weaponKey = "as_val",
            ammoAmount = 40,
            description = "Spec-ops recon rifle built around an integral titanium baffle stack and SP-6 AP ammo."
        ),
        CraftingRecipe(
            id = "craft_fn_fal",
            name = "FN FAL 50.00 Battle Rifle",
            detail = "7.62x51mm NATO Battle Rifle (+40 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // BATTLE RIFLE",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 4,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2,
                CraftingMaterial.POLYMER_STOCK to 2
            ),
            weaponKey = "fn_fal",
            ammoAmount = 40,
            description = "Long-barreled full-power 7.62 NATO tilting-breech battle rifle."
        ),
        CraftingRecipe(
            id = "craft_g3a3",
            name = "HK G3A3 Battle Rifle",
            detail = "7.62x51mm Roller-Delayed (+40 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // BATTLE RIFLE",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 4,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.POLYMER_STOCK to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 4
            ),
            weaponKey = "g3a3",
            ammoAmount = 40,
            description = "Fluted-chamber stamped steel 7.62 NATO battle rifle with crisp delayed recoil."
        ),
        CraftingRecipe(
            id = "craft_scar_h",
            name = "FN SCAR-17S Battle Rifle",
            detail = "FDE Monolithic 7.62 NATO (+40 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "GRAIL // HEAVY",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 4,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.POLYMER_STOCK to 3,
                CraftingMaterial.OPTICAL_GLASS to 2,
                CraftingMaterial.ELECTRONIC_PARTS to 2
            ),
            weaponKey = "scar_h",
            ammoAmount = 40,
            description = "Heavy SOCOM battle rifle milling upper rail rigidity into a lightweight 7.62 platform."
        ),
        CraftingRecipe(
            id = "craft_kar98k",
            name = "Mauser Karabiner 98k",
            detail = "7.92x57mm Bolt-Action (+20 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "UNCOMMON // BOLT RIFLE",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.WOOD_FURNITURE to 4,
                CraftingMaterial.OPTICAL_GLASS to 1
            ),
            weaponKey = "kar98k",
            ammoAmount = 20,
            description = "Controlled-feed bolt action bedded in laminated walnut for deliberate long-range shots."
        ),
        CraftingRecipe(
            id = "craft_mk14",
            name = "Sage Mk14 EBR Marksman",
            detail = "Long-Range 7.62mm DMR (+40 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // DMR",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 4,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.OPTICAL_GLASS to 3,
                CraftingMaterial.ELECTRONIC_PARTS to 2
            ),
            weaponKey = "mk14",
            ammoAmount = 40,
            description = "Aluminum-chassis M14 marksman rifle fitted with ballistic rangefinder glass."
        ),
        CraftingRecipe(
            id = "craft_sr25",
            name = "KAC SR-25 ECC Marksman",
            detail = "Free-Float 7.62mm AR-10 (+40 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "GRAIL // AR-10 DMR",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 4,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.TRIGGER_ASSEMBLY to 3,
                CraftingMaterial.OPTICAL_GLASS to 3,
                CraftingMaterial.ELECTRONIC_PARTS to 2
            ),
            weaponKey = "sr25",
            ammoAmount = 40,
            description = "Sub-MOA semi-automatic precision 7.62 marksman system with free-float match barrel."
        ),
        CraftingRecipe(
            id = "craft_svd",
            name = "Izhmash SVD Dragunov",
            detail = "7.62x54mmR Marksman Rifle (+30 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // DMR",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 4,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.WOOD_FURNITURE to 2,
                CraftingMaterial.OPTICAL_GLASS to 3
            ),
            weaponKey = "svd",
            ammoAmount = 30,
            description = "Short-stroke gas-piston marksman rifle pairing slim weight with 7.62x54mmR reach."
        ),
        CraftingRecipe(
            id = "craft_m24",
            name = "Remington M24 SWS Bolt",
            detail = "7.62x51mm Match Bolt Sniper (+20 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // SNIPER",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 4,
                CraftingMaterial.POLYMER_STOCK to 3,
                CraftingMaterial.OPTICAL_GLASS to 3
            ),
            weaponKey = "m24",
            ammoAmount = 20,
            description = "Composite-bedded Model 700 sniper rifle with free-floated match barrel and 10x glass."
        ),
        CraftingRecipe(
            id = "craft_awm",
            name = "AI Arctic Warfare Magnum",
            detail = ".338 Lapua Bolt Sniper (+15 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "GRAIL // SNIPER",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 5,
                CraftingMaterial.RIFLED_BARREL to 4,
                CraftingMaterial.POLYMER_STOCK to 3,
                CraftingMaterial.OPTICAL_GLASS to 4,
                CraftingMaterial.ELECTRONIC_PARTS to 3
            ),
            weaponKey = "awm",
            ammoAmount = 15,
            description = "Chassis-bedded .338 Lapua precision sniper rifle built for mile-long cold-bore hits."
        ),
        CraftingRecipe(
            id = "craft_m82a1",
            name = "Barrett M82A1 .50 BMG",
            detail = "195 DMG .50 Anti-Materiel (+20 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "GRAIL // .50 BMG",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 6,
                CraftingMaterial.RIFLED_BARREL to 5,
                CraftingMaterial.TRIGGER_ASSEMBLY to 3,
                CraftingMaterial.OPTICAL_GLASS to 4,
                CraftingMaterial.ELECTRONIC_PARTS to 4
            ),
            weaponKey = "m82a1",
            ammoAmount = 20,
            description = "Heavy .50 BMG anti-materiel cannon with dual-chamber muzzle brake and fluted 29\" barrel."
        ),
        CraftingRecipe(
            id = "craft_rpk74",
            name = "Molot RPK-74M Support",
            detail = "45-Rd Heavy-Barrel 5.45mm (+90 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // SQUAD SUPPORT",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 4,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.POLYMER_STOCK to 3,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 4
            ),
            weaponKey = "rpk74",
            ammoAmount = 90,
            description = "Bulged-trunnion heavy-barrel AK automatic rifle with folding bipod and 45-round mags."
        ),
        CraftingRecipe(
            id = "craft_m249",
            name = "FN M249 SAW LMG",
            detail = "100-Rd Belt-Fed 5.56mm (+200 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "GRAIL // LMG",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 5,
                CraftingMaterial.RIFLED_BARREL to 4,
                CraftingMaterial.TRIGGER_ASSEMBLY to 3,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 6,
                CraftingMaterial.ELECTRONIC_PARTS to 2
            ),
            weaponKey = "m249",
            ammoAmount = 200,
            description = "Open-bolt disintegrating-belt 5.56mm squad automatic built for sustained suppression."
        ),
        CraftingRecipe(
            id = "craft_pkm",
            name = "Kalashnikov PKM 7.62x54mmR",
            detail = "100-Rd General-Purpose Belt LMG (+200 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "GRAIL // GPMG",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 6,
                CraftingMaterial.RIFLED_BARREL to 4,
                CraftingMaterial.TRIGGER_ASSEMBLY to 3,
                CraftingMaterial.WOOD_FURNITURE to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 6
            ),
            weaponKey = "pkm",
            ammoAmount = 200,
            description = "Featherweight stamped-steel 7.62x54mmR belt-fed general-purpose machine gun."
        ),
        CraftingRecipe(
            id = "craft_m240b",
            name = "FN M240B 7.62 NATO GPMG",
            detail = "100-Rd Heavy Belt-Fed MMG (+200 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "GRAIL // HEAVY GPMG",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 6,
                CraftingMaterial.RIFLED_BARREL to 5,
                CraftingMaterial.TRIGGER_ASSEMBLY to 3,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 6,
                CraftingMaterial.GUN_LUBE to 3
            ),
            weaponKey = "m240b",
            ammoAmount = 200,
            description = "Tank-tough MAG-58 7.62 NATO medium machine gun assembled for nonstop firepower."
        ),
        // TIER 3 RELOADING PRESS AMMO (Requires HQ Reloading Gear Lv.3)
        CraftingRecipe(
            id = "craft_ammo_762nato_box",
            name = "7.62x51mm & 7.62x54mmR Match (+60)",
            detail = "SCAR-H / FAL / G3 / Mk14 / SR25 / SVD / PKM / M240B",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.AMMO,
            rarityLabel = "RARE // AMMO",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.BRASS_CASINGS to 4,
                CraftingMaterial.GUNPOWDER to 4,
                CraftingMaterial.PERCUSSION_PRIMERS to 3,
                CraftingMaterial.COPPER_JACKETS to 3,
                CraftingMaterial.GUN_LUBE to 1
            ),
            weaponKey = "scar_h",
            ammoAmount = 60,
            requiredReloadingTier = 3,
            description = "Full-power 175-grain Sierra boat-tail 7.62mm cartridges hand-weighed on Match Dies."
        ),
        CraftingRecipe(
            id = "craft_ammo_magnum_sniper_box",
            name = ".338 Lapua & .50 BMG Match Lot (+25)",
            detail = "AWM .338 / Barrett M82A1 / Kar98k / M24",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.AMMO,
            rarityLabel = "GRAIL // AMMO",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.BRASS_CASINGS to 4,
                CraftingMaterial.GUNPOWDER to 5,
                CraftingMaterial.PERCUSSION_PRIMERS to 3,
                CraftingMaterial.COPPER_JACKETS to 4,
                CraftingMaterial.GUN_LUBE to 2
            ),
            weaponKey = "awm",
            ammoAmount = 25,
            requiredReloadingTier = 3,
            description = "Heavy-press concentricity-checked magnum sniper rounds for extreme ballistic stability."
        ),
        CraftingRecipe(
            id = "craft_ammo_lmg_belt",
            name = "100-Rd M27 Disintegrating Belt (+100)",
            detail = "M249 SAW / PKM / M240B Belt-Fed Reserve",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.AMMO,
            rarityLabel = "GRAIL // AMMO BELT",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.BRASS_CASINGS to 5,
                CraftingMaterial.GUNPOWDER to 5,
                CraftingMaterial.PERCUSSION_PRIMERS to 4,
                CraftingMaterial.COPPER_JACKETS to 4,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 3
            ),
            weaponKey = "m249",
            ammoAmount = 100,
            requiredReloadingTier = 3,
            description = "Linked metal-belt ammunition box assembled using the HQ Match & Magnum Loading Rig."
        ),

        // ============================================================
        // HISTORICAL HERITAGE RELIC RESTORATIONS (FRONTIER TO COLD WAR)
        // ============================================================
        CraftingRecipe(
            id = "craft_colt_saa",
            name = "Colt SAA Peacemaker (.45 Colt)",
            detail = "Frontier Era Revolver (+18 Rounds)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // FRONTIER",
            rarityColorHex = 0xFFD97706,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 1,
                CraftingMaterial.RIFLED_BARREL to 1,
                CraftingMaterial.WOOD_FURNITURE to 2,
                CraftingMaterial.BRASS_CASINGS to 2
            ),
            weaponKey = "colt_saa",
            ammoAmount = 18,
            description = "Restored 1873 single-action wheelgun from frontier components."
        ),
        CraftingRecipe(
            id = "craft_winchester_1894",
            name = "Winchester M1894 (.30-30)",
            detail = "Frontier Lever Carbine (+24 Rounds)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // FRONTIER",
            rarityColorHex = 0xFFD97706,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.WOOD_FURNITURE to 3
            ),
            weaponKey = "winchester_1894",
            ammoAmount = 24,
            description = "Smokeless-era American walnut saddle carbine rebuilt on the shop bench."
        ),
        CraftingRecipe(
            id = "craft_winchester_1897",
            name = "Winchester 1897 Trench Gun",
            detail = "Slam-Fire 12G Pump (+20 Shells)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // FRONTIER",
            rarityColorHex = 0xFFD97706,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.WOOD_FURNITURE to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            weaponKey = "winchester_1897",
            ammoAmount = 20,
            description = "Exposed-hammer pump trench shotgun capable of rapid slam-fire bursts."
        ),
        CraftingRecipe(
            id = "craft_luger_p08",
            name = "Luger P08 Parabellum (9mm)",
            detail = "WWI Toggle-Lock Sidearm (+32 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWI",
            rarityColorHex = 0xFF94A3B8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2,
                CraftingMaterial.WOOD_FURNITURE to 1,
                CraftingMaterial.GUN_LUBE to 2
            ),
            weaponKey = "luger_p08",
            ammoAmount = 32,
            description = "Precision-machined Imperial knee-joint toggle-lock 9mm service pistol."
        ),
        CraftingRecipe(
            id = "craft_springfield_1903",
            name = "Springfield M1903 (.30-06)",
            detail = "WWI Match Bolt Rifle (+25 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWI",
            rarityColorHex = 0xFF94A3B8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.WOOD_FURNITURE to 3
            ),
            weaponKey = "springfield_1903",
            ammoAmount = 25,
            description = "Renowned American bolt-action rifle bedded in oiled walnut timber."
        ),
        CraftingRecipe(
            id = "craft_sten_mk2",
            name = "Sten Mk II SMG (9mm)",
            detail = "WWII Tube-Receiver SMG (+64 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWII",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 5,
                CraftingMaterial.RIFLED_BARREL to 1,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 3
            ),
            weaponKey = "sten_mk2",
            ammoAmount = 64,
            description = "British stamped-tube open-bolt submachine gun famously fabricated in workshops."
        ),
        CraftingRecipe(
            id = "craft_thompson_m1928",
            name = "Thompson M1928A1 (.45 ACP)",
            detail = "WWII Tommy Submachine Gun (+60 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWII",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.WOOD_FURNITURE to 3,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2
            ),
            weaponKey = "thompson_m1928",
            ammoAmount = 60,
            description = "Iconic Blish-lock .45 ACP submachine gun with walnut grip & Cutts barrel fins."
        ),
        CraftingRecipe(
            id = "craft_m1_garand",
            name = "M1 Garand (.30-06)",
            detail = "WWII 8-Rd En-Bloc Rifle (+32 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWII",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 4,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.WOOD_FURNITURE to 3,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2
            ),
            weaponKey = "m1_garand",
            ammoAmount = 32,
            description = "Patton's finest semi-automatic battle rifle with unmistakable 8th-round ping."
        ),
        CraftingRecipe(
            id = "craft_stg44",
            name = "StG 44 Sturmgewehr",
            detail = "World's First Assault Rifle (+60 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWII GRAIL",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 4,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.WOOD_FURNITURE to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 5
            ),
            weaponKey = "stg44",
            ammoAmount = 60,
            description = "Historic 7.92x33mm Kurz intermediate assault rifle pioneering modern infantry doctrine."
        ),
        CraftingRecipe(
            id = "craft_sks45",
            name = "Simonov SKS-45 (7.62x39)",
            detail = "Cold War Carbine (+40 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // COLD WAR",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.WOOD_FURNITURE to 3
            ),
            weaponKey = "sks45",
            ammoAmount = 40,
            description = "Rugged tilting-bolt Soviet 7.62x39mm carbine fed by 10-round stripper clips."
        ),
        CraftingRecipe(
            id = "craft_m16a1",
            name = "Colt M16A1 (5.56mm)",
            detail = "Cold War Vietnam Service Rifle (+60 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // COLD WAR",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.POLYMER_STOCK to 3,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2
            ),
            weaponKey = "m16a1",
            ammoAmount = 60,
            description = "20-inch triangular-handguard 5.56mm classic with chrome chamber and carry handle."
        ),
        CraftingRecipe(
            id = "craft_uzi",
            name = "IMI Uzi 9mm SMG",
            detail = "Cold War Telescoping-Bolt SMG (+64 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // COLD WAR",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.SCRAP_METAL to 4,
                CraftingMaterial.POLYMER_STOCK to 2,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1
            ),
            weaponKey = "uzi",
            ammoAmount = 64,
            description = "Compact stamped-steel open-bolt 9mm submachine gun balanced directly over the pistol grip."
        ),
        CraftingRecipe(
            id = "craft_ammo_frontier_heritage",
            name = "Frontier & Vintage Rifle Lot (+36)",
            detail = ".45 Colt / .30-30 / .45-70 / .30-06 / 7.62x54R",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.AMMO,
            rarityLabel = "HERITAGE // AMMO",
            rarityColorHex = 0xFFD97706,
            costs = mapOf(
                CraftingMaterial.BRASS_CASINGS to 3,
                CraftingMaterial.GUNPOWDER to 2,
                CraftingMaterial.PERCUSSION_PRIMERS to 2,
                CraftingMaterial.LEAD_PROJECTILES to 3
            ),
            weaponKey = "winchester_1894",
            ammoAmount = 36,
            requiredReloadingTier = 1,
            description = "Cast-lead and soft-point historical rifle and revolver loads pressed cleanly for antique firearms."
        ),

        // ============================================================
        // GUNSMITH ATTACHMENTS — ERA-AUTHENTIC & MODERN MODS
        // ============================================================
        CraftingRecipe(
            id = "craft_att_lyman_tang_peep",
            name = "Lyman #2 Tang Peep Sight",
            detail = "Frontier Lever & Sharps Optic (+45 Range)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "FRONTIER // OPTIC",
            rarityColorHex = 0xFFD97706,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_lyman_tang_peep",
            description = "Folding tang-mounted Vernier aperture iron sight for Frontier rifles."
        ),
        CraftingRecipe(
            id = "craft_att_leather_saddle_scabbard",
            name = "Leather Saddle Ring & Sling",
            detail = "Universal Wood-Stock Underbarrel (-Move Pen)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "HERITAGE // SLING",
            rarityColorHex = 0xFFD97706,
            costs = mapOf(
                CraftingMaterial.TACTICAL_CLOTH to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 1
            ),
            attachmentKey = "att_leather_saddle_scabbard",
            description = "Stitched latigo sling calming movement sway across Frontier, WWI, WWII & Cold War wood rifles."
        ),
        CraftingRecipe(
            id = "craft_att_trench_heatshield",
            name = "M1897 Ventilated Heat Shield",
            detail = "Trench & Coach Tactical (+14% Slam-Fire)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "TRENCH // TACTICAL",
            rarityColorHex = 0xFFD97706,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 3,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_trench_heatshield",
            description = "Perforated barrel shroud letting trench gunners rack rapid pump follow-ups."
        ),
        CraftingRecipe(
            id = "craft_att_malcolm_brass_scope",
            name = "Wm. Malcolm 6x Brass Scope",
            detail = "Frontier / WWI Long-Tube Scope (+95 Range)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // FRONTIER",
            rarityColorHex = 0xFFD97706,
            costs = mapOf(
                CraftingMaterial.OPTICAL_GLASS to 2,
                CraftingMaterial.BRASS_CASINGS to 4,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_malcolm_brass_scope",
            description = "Authentic 1870s full-length brass rifle telescope for Frontier & WWI long arms."
        ),
        CraftingRecipe(
            id = "craft_att_m1905_bayonet",
            name = "M1905 Sword Bayonet",
            detail = "WWI / WWII / Trench Lug (+18 Melee Thrust)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "UNCOMMON // BAYONET",
            rarityColorHex = 0xFF94A3B8,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 3,
                CraftingMaterial.WOOD_FURNITURE to 1,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_m1905_bayonet",
            description = "Forged steel bayonet clamping beneath bolt-action rifles, M1 Garands, and 1897 Trench Guns."
        ),
        CraftingRecipe(
            id = "craft_att_maxim_1910_can",
            name = "Hiram Maxim 1910 Muffler",
            detail = "WWI / WWII Period Suppressor (-65% Noise)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // WWI SILENCER",
            rarityColorHex = 0xFF94A3B8,
            costs = mapOf(
                CraftingMaterial.SUPPRESSOR_BAFFLES to 2,
                CraftingMaterial.SCRAP_METAL to 3,
                CraftingMaterial.BRASS_CASINGS to 2
            ),
            attachmentKey = "att_maxim_1910_can",
            description = "Historic 1910 spiral-chamber Maxim silencer suppressing WWI/WWII bolts & pistols."
        ),
        CraftingRecipe(
            id = "craft_att_brass_stripper_clips",
            name = "Brass Stripper Clips",
            detail = "WWI / WWII / Cold War Reload (-28% Reload)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "COMMON // CLIP",
            rarityColorHex = 0xFF94A3B8,
            costs = mapOf(
                CraftingMaterial.BRASS_CASINGS to 3,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_brass_stripper_clips",
            description = "Spring-brass charger clips accelerating bolt rifle, C96 & SKS reloads."
        ),
        CraftingRecipe(
            id = "craft_att_c96_stock_holster",
            name = "Mauser C96 Walnut Stock",
            detail = "WWI Broomhandle Stock (+55 Range, -40% Spread)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // C96",
            rarityColorHex = 0xFF94A3B8,
            costs = mapOf(
                CraftingMaterial.WOOD_FURNITURE to 3,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_c96_stock_holster",
            description = "Iconic belt-holster carbine stock locking onto the Mauser C96 grip slot."
        ),
        CraftingRecipe(
            id = "craft_att_luger_snail_drum",
            name = "Trommel 32-Rd Snail Drum",
            detail = "WWI Luger P08 Mag (+24 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "GRAIL // WWI DRUM",
            rarityColorHex = 0xFF94A3B8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 4,
                CraftingMaterial.GUN_LUBE to 2
            ),
            attachmentKey = "att_luger_snail_drum",
            description = "Wind-up 32-round Trommelmagazin converting Artillery Lugers into room sweepers."
        ),
        CraftingRecipe(
            id = "craft_att_m84_infantry_scope",
            name = "M84 2.2x Infantry Scope",
            detail = "WWII Garand / Bolt Rifle Optic (+75 Range)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // WWII OPTIC",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.OPTICAL_GLASS to 2,
                CraftingMaterial.FORGED_RECEIVER to 1,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_m84_infantry_scope",
            description = "Offset Garand & bolt-rifle infantry telescope preserving top-clip loading."
        ),
        CraftingRecipe(
            id = "craft_att_pu_35x_scope",
            name = "PU 3.5x Sniping Scope",
            detail = "WWII Bolt & Cold War AK Optic (+95 Range)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // WWII OPTIC",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.OPTICAL_GLASS to 3,
                CraftingMaterial.SCRAP_METAL to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_pu_35x_scope",
            description = "Steel Eastern Front 3.5x sniper glass for Mosin, Kar98k, SKS & AK platforms."
        ),
        CraftingRecipe(
            id = "craft_att_thompson_50_drum",
            name = "Thompson 50-Rd Type-L Drum",
            detail = "WWII M1928A1 Magazine (+20 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // WWII DRUM",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 4,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 4,
                CraftingMaterial.GUN_LUBE to 1
            ),
            attachmentKey = "att_thompson_50_drum",
            description = "Classic .45 ACP rotary wind-up Type-L drum for the Thompson M1928A1."
        ),
        CraftingRecipe(
            id = "craft_att_cutts_compensator",
            name = "Cutts Slotted Compensator",
            detail = "WWII Thompson / Shotgun / MG (-28% Spread)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "UNCOMMON // MUZZLE",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 1,
                CraftingMaterial.SCRAP_METAL to 2
            ),
            attachmentKey = "att_cutts_compensator",
            description = "Slotted muzzle brake reducing automatic barrel climb on vintage SMGs & trench guns."
        ),
        CraftingRecipe(
            id = "craft_att_sten_mkiis_suppressor",
            name = "Sten Mk IIS Baffle Jacket",
            detail = "WWII SOE Suppressor (-74% Noise)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // SOE STEALTH",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.SUPPRESSOR_BAFFLES to 2,
                CraftingMaterial.TACTICAL_CLOTH to 2,
                CraftingMaterial.SCRAP_METAL to 2
            ),
            attachmentKey = "att_sten_mkiis_suppressor",
            description = "Canvas-laced covert baffle shroud for Sten and WWII submachine guns."
        ),
        CraftingRecipe(
            id = "craft_att_colt_4x20_carry",
            name = "Colt 4x20 Carry-Handle Scope",
            detail = "Cold War M16A1 / XM177 / FAL (+80 Range)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // COLD WAR",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.OPTICAL_GLASS to 2,
                CraftingMaterial.SCRAP_METAL to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_colt_4x20_carry",
            description = "Classic thumbscrew carry-handle 4x rifle glass for M16A1 & XM177 carbines."
        ),
        CraftingRecipe(
            id = "craft_att_pso1_dovetail",
            name = "PSO-1 4x24 Dovetail Scope",
            detail = "Cold War & Modern AK/SVD Optic (+95 Range)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // EASTERN",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.OPTICAL_GLASS to 3,
                CraftingMaterial.LITHIUM_CELLS to 1,
                CraftingMaterial.FORGED_RECEIVER to 1
            ),
            attachmentKey = "att_pso1_dovetail",
            description = "Illuminated Warsw Pact 4x side-mount marksman telescope for AK, SVD & Galil platforms."
        ),
        CraftingRecipe(
            id = "craft_att_xm177_moderator",
            name = "XM177E2 Sound Moderator",
            detail = "Cold War AR Muzzle (-38% Noise, -14% Spread)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "UNCOMMON // SOG",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.SUPPRESSOR_BAFFLES to 1,
                CraftingMaterial.SCRAP_METAL to 2
            ),
            attachmentKey = "att_xm177_moderator",
            description = "Vietnam SOG 4.5-inch flash/sound moderator for M16A1 and XM177 barrels."
        ),
        CraftingRecipe(
            id = "craft_att_pbs1_ak_can",
            name = "PBS-1 AK Suppressor",
            detail = "Cold War & Modern AK Can (-72% Noise)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // SPETSNAZ",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.SUPPRESSOR_BAFFLES to 3,
                CraftingMaterial.SCRAP_METAL to 3
            ),
            attachmentKey = "att_pbs1_ak_can",
            description = "Heavy steel Soviet suppressor threading cleanly onto AK47, AK74M & SKS rifles."
        ),
        CraftingRecipe(
            id = "craft_att_eotech_exps3",
            name = "EOTech EXPS3 Holographic",
            detail = "Modern CQB Holo Optic (+55 Range, -Move Bloom)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // MODERN OPTIC",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.OPTICAL_GLASS to 2,
                CraftingMaterial.ELECTRONIC_PARTS to 2,
                CraftingMaterial.LITHIUM_CELLS to 2
            ),
            attachmentKey = "att_eotech_exps3",
            description = "Modern 68-MOA holographic speed-ring optic for carbines, SMGs & shotguns."
        ),
        CraftingRecipe(
            id = "craft_att_acog_ta31",
            name = "Trijicon ACOG 4x32 Prism",
            detail = "Modern Rifle & DMR Optic (+95 Range, +85 ADS)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // PRISM 4X",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.OPTICAL_GLASS to 3,
                CraftingMaterial.FORGED_RECEIVER to 1,
                CraftingMaterial.ELECTRONIC_PARTS to 1
            ),
            attachmentKey = "att_acog_ta31",
            description = "Indestructible 4x32 fiber-optic combat prism sight for modern rifles & DMRs."
        ),
        CraftingRecipe(
            id = "craft_att_surefire_rc2",
            name = "SureFire SOCOM556-RC2",
            detail = "Modern Rifle Suppressor (-76% Noise, +5% Pen)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "GRAIL // MODERN CAN",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.SUPPRESSOR_BAFFLES to 4,
                CraftingMaterial.FORGED_RECEIVER to 1,
                CraftingMaterial.GUN_LUBE to 2
            ),
            attachmentKey = "att_surefire_rc2",
            description = "Quick-detach Inconel SOCOM suppressor eliminating flash & gunfire report."
        ),
        CraftingRecipe(
            id = "craft_att_osprey_pistol_can",
            name = "Osprey .45 / Omega 9K Can",
            detail = "Modern Pistol & PDW Can (-76% Noise)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // PISTOL CAN",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.SUPPRESSOR_BAFFLES to 2,
                CraftingMaterial.SCRAP_METAL to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_osprey_pistol_can",
            description = "Low-profile eccentric suppressor clearing modern handgun & PDW sights silently."
        ),
        CraftingRecipe(
            id = "craft_att_magpul_rvg_grip",
            name = "Magpul RVG Vertical Grip",
            detail = "Modern M-LOK Foregrip (-20% Spread, -Move Pen)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "COMMON // M-LOK",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.POLYMER_STOCK to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_magpul_rvg_grip",
            description = "Polymer vertical grip anchoring support-hand control across modern long guns."
        ),
        CraftingRecipe(
            id = "craft_att_pmag_40_ext",
            name = "Magpul PMAG 40 / Ext Stick",
            detail = "Modern Extended Magazine (+35% Cap, -12% Reload)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "UNCOMMON // MAG",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.POLYMER_STOCK to 3,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_pmag_40_ext",
            description = "Windowed high-capacity modern magazine feeding rifles, SMGs, and sidearms."
        ),
        CraftingRecipe(
            id = "craft_att_peq15_laser",
            name = "AN/PEQ-15 Aiming Laser",
            detail = "Modern Rail Laser (-24% Spread, -35% Bloom)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // TACTICAL",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.ELECTRONIC_PARTS to 2,
                CraftingMaterial.LITHIUM_CELLS to 2,
                CraftingMaterial.OPTICAL_GLASS to 1
            ),
            attachmentKey = "att_peq15_laser",
            description = "MIL-SPEC visible/IR laser unit delivering surgical sprint-and-hip point shooting."
        ),

        // ============================================================
        // COMPLETE HERITAGE & TACTICAL ARSENAL RECIPES (100% PLAYER-CRAFTED)
        // ============================================================
        CraftingRecipe(
            id = "craft_schofield",
            name = "S&W Model 3 Schofield (.45)",
            detail = "Frontier Top-Break Revolver (+18 Rounds)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // FRONTIER",
            rarityColorHex = 0xFFD97706,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 1,
                CraftingMaterial.RIFLED_BARREL to 1,
                CraftingMaterial.WOOD_FURNITURE to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            weaponKey = "schofield",
            ammoAmount = 18,
            description = "Latch-hinged top-break cavalry revolver crafted for fast simultaneous brass ejection."
        ),
        CraftingRecipe(
            id = "craft_winchester_1873",
            name = "Winchester M1873 (.44-40)",
            detail = "Frontier Lever Rifle (+28 Rounds)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // FRONTIER",
            rarityColorHex = 0xFFD97706,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.WOOD_FURNITURE to 3,
                CraftingMaterial.BRASS_CASINGS to 2
            ),
            weaponKey = "winchester_1873",
            ammoAmount = 28,
            description = "Brass-carrier toggle-link lever repeater chambered to share .44-40 with frontier sidearms."
        ),
        CraftingRecipe(
            id = "craft_sharps_1874",
            name = "Sharps M1874 Buffalo (.45-70)",
            detail = "Frontier Falling-Block Rifle (+12 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // FRONTIER",
            rarityColorHex = 0xFFD97706,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.WOOD_FURNITURE to 3,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1
            ),
            weaponKey = "sharps_1874",
            ammoAmount = 12,
            description = "Heavy octagonal-barrel .45-70 Government long-range falling-block hunter."
        ),
        CraftingRecipe(
            id = "craft_webley_mk6",
            name = "Webley Mk VI .455 Revolver",
            detail = "WWI British Top-Break (+18 Rounds)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWI",
            rarityColorHex = 0xFF94A3B8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1,
                CraftingMaterial.POLYMER_STOCK to 1,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            weaponKey = "webley_mk6",
            ammoAmount = 18,
            description = "Rugged Trench-era British top-break service six-shooter throwing heavy .455 slugs."
        ),
        CraftingRecipe(
            id = "craft_mauser_c96",
            name = "Mauser C96 Broomhandle",
            detail = "WWI 7.63mm Clip Pistol (+30 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWI",
            rarityColorHex = 0xFF94A3B8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 1,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2,
                CraftingMaterial.WOOD_FURNITURE to 2
            ),
            weaponKey = "mauser_c96",
            ammoAmount = 30,
            description = "Stripper-fed short-recoil Broomhandle pistol machined without frame screws."
        ),
        CraftingRecipe(
            id = "craft_lee_enfield",
            name = "SMLE No. 1 Mk III (.303)",
            detail = "WWI 10-Rd Bolt Rifle (+30 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWI",
            rarityColorHex = 0xFF94A3B8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.WOOD_FURNITURE to 3
            ),
            weaponKey = "lee_enfield",
            ammoAmount = 30,
            description = "Fast-cycling cock-on-closing 10-round .303 British bolt rifle famous for mad-minute fire."
        ),
        CraftingRecipe(
            id = "craft_mosin_m9130",
            name = "Mosin-Nagant M91/30 (7.62x54R)",
            detail = "WWI/WWII Bolt Rifle (+25 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWI",
            rarityColorHex = 0xFF94A3B8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.WOOD_FURNITURE to 3
            ),
            weaponKey = "mosin_m9130",
            ammoAmount = 25,
            description = "Long heavy-barrel 7.62x54mmR bolt-action service rifle bedded in birch."
        ),
        CraftingRecipe(
            id = "craft_bar_m1918",
            name = "Browning BAR M1918A2 (.30-06)",
            detail = "WWI/WWII Automatic Rifle (+60 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWI GRAIL",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 4,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.TRIGGER_ASSEMBLY to 3,
                CraftingMaterial.WOOD_FURNITURE to 3
            ),
            weaponKey = "bar_m1918",
            ammoAmount = 60,
            description = "20-round .30-06 marching automatic squad rifle built from billet steel and walnut."
        ),
        CraftingRecipe(
            id = "craft_lewis_gun",
            name = "Lewis Gun Mk I (.303 Pan LMG)",
            detail = "WWI 47-Rd Pan Machine Gun (+94 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWI GRAIL",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 5,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.SCRAP_METAL to 5,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2
            ),
            weaponKey = "lewis_gun",
            ammoAmount = 94,
            description = "Shroud-cooled forced-draft LMG topped with a 47-round clockwork rotary pan magazine."
        ),
        CraftingRecipe(
            id = "craft_walther_p38",
            name = "Walther P38 (9mm)",
            detail = "WWII DA/SA Sidearm (+24 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWII",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2,
                CraftingMaterial.POLYMER_STOCK to 1
            ),
            weaponKey = "walther_p38",
            ammoAmount = 24,
            description = "Open-top falling-block locked-breech 9mm pistol pioneering modern DA/SA decockers."
        ),
        CraftingRecipe(
            id = "craft_tt33",
            name = "Tokarev TT-33 (7.62x25mm)",
            detail = "WWII High-Velocity Sidearm (+24 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWII",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1,
                CraftingMaterial.SCRAP_METAL to 3
            ),
            weaponKey = "tt33",
            ammoAmount = 24,
            description = "Slim all-steel 7.62x25mm pistol pushing hot bottle-neck rounds with sharp armor penetration."
        ),
        CraftingRecipe(
            id = "craft_browning_hp",
            name = "Browning Hi-Power 9mm",
            detail = "WWII 13-Rd Wonder Nine (+26 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWII",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2,
                CraftingMaterial.WOOD_FURNITURE to 1,
                CraftingMaterial.GUN_LUBE to 1
            ),
            weaponKey = "browning_hp",
            ammoAmount = 26,
            description = "Original double-stack 13-round 9mm combat handgun prized across continents."
        ),
        CraftingRecipe(
            id = "craft_mp40",
            name = "MP 40 Maschinenpistole",
            detail = "WWII 32-Rd Folding SMG (+64 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWII",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.SCRAP_METAL to 4,
                CraftingMaterial.POLYMER_STOCK to 2,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1
            ),
            weaponKey = "mp40",
            ammoAmount = 64,
            description = "Controlled 500-RPM stamped-steel and Bakelite paratrooper submachine gun."
        ),
        CraftingRecipe(
            id = "craft_ppsh41",
            name = "PPSh-41 Spagin SMG",
            detail = "WWII 900-RPM SMG (+70 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWII",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.SCRAP_METAL to 4,
                CraftingMaterial.WOOD_FURNITURE to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 3
            ),
            weaponKey = "ppsh41",
            ammoAmount = 70,
            description = "Shroud-barrel 7.62x25mm close-quarters buzzsaw ready for 71-round drums."
        ),
        CraftingRecipe(
            id = "craft_m3_grease",
            name = "M3A1 Grease Gun (.45 ACP)",
            detail = "WWII Low-RPM .45 SMG (+60 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWII",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 5,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1
            ),
            weaponKey = "m3_grease",
            ammoAmount = 60,
            description = "Slow-thumping 450-RPM stamped .45 ACP submachine gun with wire slide stock."
        ),
        CraftingRecipe(
            id = "craft_m1_carbine",
            name = "M1 / M1A1 Carbine (.30 Carbine)",
            detail = "WWII Handy Light Carbine (+45 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWII",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.WOOD_FURNITURE to 2,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1
            ),
            weaponKey = "m1_carbine",
            ammoAmount = 45,
            description = "Featherweight short-stroke piston .30 carbine favoring fast sprint handling."
        ),
        CraftingRecipe(
            id = "craft_bren_lmg",
            name = "Bren Mk II (.303 Top-Feed LMG)",
            detail = "WWII 30-Rd Top-Mag LMG (+90 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWII",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 4,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.WOOD_FURNITURE to 2,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2
            ),
            weaponKey = "bren_lmg",
            ammoAmount = 90,
            description = "Dead-accurate British .303 top-fed light machine gun with quick-change barrel."
        ),
        CraftingRecipe(
            id = "craft_mg42",
            name = "Maschinengewehr MG 42",
            detail = "WWII 1,200-RPM Belt GPMG (+100 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // WWII GRAIL",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 5,
                CraftingMaterial.RIFLED_BARREL to 4,
                CraftingMaterial.SCRAP_METAL to 5,
                CraftingMaterial.TRIGGER_ASSEMBLY to 3
            ),
            weaponKey = "mg42",
            ammoAmount = 100,
            description = "Roller-locked 7.92x57mm belt-fed machine gun roaring at 1,200 rounds per minute."
        ),
        CraftingRecipe(
            id = "craft_makarov",
            name = "PM Makarov 9x18mm",
            detail = "Cold War Steel Sidearm (+24 Rounds)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // COLD WAR",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 1,
                CraftingMaterial.TRIGGER_ASSEMBLY to 1,
                CraftingMaterial.POLYMER_STOCK to 1,
                CraftingMaterial.SCRAP_METAL to 2
            ),
            weaponKey = "makarov",
            ammoAmount = 24,
            description = "Simple fixed-barrel Soviet 9x18mm Makarov pocket pistol with Bakelite star grips."
        ),
        CraftingRecipe(
            id = "craft_ithaca_37",
            name = "Ithaca 37 Trench 12G",
            detail = "Cold War Bottom-Eject Pump (+20 Shells)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // COLD WAR",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.RIFLED_BARREL to 2,
                CraftingMaterial.WOOD_FURNITURE to 3
            ),
            weaponKey = "ithaca_37",
            ammoAmount = 20,
            description = "Slam-fire bottom-eject 12-gauge combat shotgun sealed cleanly against mud and grit."
        ),
        CraftingRecipe(
            id = "craft_xm177",
            name = "Colt XM177E2 CAR-15 (5.56mm)",
            detail = "Cold War MACV-SOG Commando (+60 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // COLD WAR",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.POLYMER_STOCK to 3,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2
            ),
            weaponKey = "xm177",
            ammoAmount = 60,
            description = "Telescoping-stock 11.5-inch Special Forces commando carbine from Vietnam."
        ),
        CraftingRecipe(
            id = "craft_galil_arm",
            name = "IMI Galil ARM 5.56mm",
            detail = "Cold War 35-Rd Folding Carbine (+70 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // COLD WAR",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 4,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.WOOD_FURNITURE to 2,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2
            ),
            weaponKey = "galil_arm",
            ammoAmount = 70,
            description = "Milled-receiver Kalashnikov-action 5.56mm rifle with folding tubular stock."
        ),
        CraftingRecipe(
            id = "craft_m60_gpmg",
            name = "Saco M60 General Purpose LMG",
            detail = "Cold War 7.62x51mm Pig LMG (+100 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "HERITAGE // COLD WAR",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 5,
                CraftingMaterial.RIFLED_BARREL to 4,
                CraftingMaterial.SCRAP_METAL to 5,
                CraftingMaterial.TRIGGER_ASSEMBLY to 3
            ),
            weaponKey = "m60_gpmg",
            ammoAmount = 100,
            description = "Iconic 7.62 NATO belt-fed Vietnam workhorse delivering heavy long-range suppression."
        ),
        CraftingRecipe(
            id = "craft_sig_m17",
            name = "SIG Sauer M17 (9mm)",
            detail = "Modern Optics-Ready Modular Sidearm (+34 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "UNCOMMON // SERVICE",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.POLYMER_STOCK to 2,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2,
                CraftingMaterial.GUN_LUBE to 1
            ),
            weaponKey = "sig_m17",
            ammoAmount = 34,
            description = "Coyote FDE P320 chassis modular 9mm service handgun milled for red-dot optics."
        ),
        CraftingRecipe(
            id = "craft_apc9",
            name = "B&T APC9K Pro SMG",
            detail = "Modern Hydraulic-Buffered PDW (+60 Rounds)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // SPEC-OPS PDW",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.POLYMER_STOCK to 3,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            weaponKey = "apc9",
            ammoAmount = 60,
            description = "Swiss hydraulic-buffered monolith submachine gun tuned for crisp flat recoil."
        ),
        CraftingRecipe(
            id = "craft_tavor_x95",
            name = "IWI Tavor X95 Bullpup",
            detail = "Modern Compact Bullpup 5.56mm (+60 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "RARE // BULLPUP",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.POLYMER_STOCK to 4,
                CraftingMaterial.TRIGGER_ASSEMBLY to 2
            ),
            weaponKey = "tavor_x95",
            ammoAmount = 60,
            description = "Compact bullpup housing a full 16.5-inch barrel inside an SMG-sized polymer shell."
        ),
        CraftingRecipe(
            id = "craft_mcx_spear",
            name = "SIG MCX Spear XM7 (.277 Fury)",
            detail = "Next-Gen 6.8mm Hybrid Rifle (+60 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "GRAIL // NEXT-GEN",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 5,
                CraftingMaterial.RIFLED_BARREL to 4,
                CraftingMaterial.TRIGGER_ASSEMBLY to 3,
                CraftingMaterial.CERAMIC_PLATES to 2,
                CraftingMaterial.ELECTRONIC_PARTS to 2
            ),
            weaponKey = "mcx_spear",
            ammoAmount = 60,
            description = "80,000-PSI hybrid-cartridge battle carbine defeating Level-IV ballistic armor."
        ),
        CraftingRecipe(
            id = "craft_cheytac_m200",
            name = "CheyTac M200 Intervention (.408)",
            detail = "Extreme Long-Range Sniper (+21 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.WEAPON,
            rarityLabel = "GRAIL // ELR SNIPER",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 6,
                CraftingMaterial.RIFLED_BARREL to 5,
                CraftingMaterial.TRIGGER_ASSEMBLY to 3,
                CraftingMaterial.OPTICAL_GLASS to 3,
                CraftingMaterial.ELECTRONIC_PARTS to 2
            ),
            weaponKey = "cheytac_m200",
            ammoAmount = 21,
            description = "Lathe-turned copper solid-core .408 sniper chassis reaching beyond two kilometers."
        ),

        // ============================================================
        // REMAINING ERA & MODERN GUNSMITH ATTACHMENTS (100% CRAFTABLE)
        // ============================================================
        CraftingRecipe(
            id = "craft_att_octagon_heavy_barrel",
            name = "24\" Heavy Octagon Rifle Barrel",
            detail = "Frontier Rifle Barrel (+7 Damage, +40 Range)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "UNCOMMON // FRONTIER",
            rarityColorHex = 0xFFD97706,
            costs = mapOf(
                CraftingMaterial.RIFLED_BARREL to 1,
                CraftingMaterial.SCRAP_METAL to 2
            ),
            attachmentKey = "att_octagon_heavy_barrel",
            description = "Hand-lapped heavy octagon Frontier barrel boosting muzzle velocity & accuracy."
        ),
        CraftingRecipe(
            id = "craft_att_pearl_walnut_grips",
            name = "Checkered Walnut & Brass Trigger",
            detail = "Frontier / WWI Revolver Grip (-18% Spread)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "COMMON // FRONTIER",
            rarityColorHex = 0xFFD97706,
            costs = mapOf(
                CraftingMaterial.WOOD_FURNITURE to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 1
            ),
            attachmentKey = "att_pearl_walnut_grips",
            description = "Custom-fit checkered hardwood revolver grips and tuned flat mainspring."
        ),
        CraftingRecipe(
            id = "craft_att_warner_swasey_prism",
            name = "Warner & Swasey 5x Musket Sight",
            detail = "WWI Prism Scope (+90 Range, +80 ADS)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // WWI OPTIC",
            rarityColorHex = 0xFF94A3B8,
            costs = mapOf(
                CraftingMaterial.OPTICAL_GLASS to 2,
                CraftingMaterial.BRASS_CASINGS to 3,
                CraftingMaterial.SCRAP_METAL to 2
            ),
            attachmentKey = "att_warner_swasey_prism",
            description = "Compact brass-housed 1913 Doughboy prism sniper scope for trench bolt rifles."
        ),
        CraftingRecipe(
            id = "craft_att_zf4_4x_scope",
            name = "Gw ZF-4 4x Infantry Glass",
            detail = "WWII StG 44 & Mauser Scope (+88 Range)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // WWII OPTIC",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.OPTICAL_GLASS to 2,
                CraftingMaterial.SCRAP_METAL to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_zf4_4x_scope",
            description = "Compact 4x combat telescope built specifically for the StG 44 & G43 rails."
        ),
        CraftingRecipe(
            id = "craft_att_oss_welrod_can",
            name = "OSS Silk-Baffle Pistol Can",
            detail = "WWII / Cold War Suppressor (-75% Noise)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // WWII STEALTH",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.SUPPRESSOR_BAFFLES to 2,
                CraftingMaterial.TACTICAL_CLOTH to 1,
                CraftingMaterial.SCRAP_METAL to 2
            ),
            attachmentKey = "att_oss_welrod_can",
            description = "Bronze-mesh & neoprene wipe pistol silencer built for covert night pistols."
        ),
        CraftingRecipe(
            id = "craft_att_ppsh_71_drum",
            name = "PPSh-41 71-Round Steel Drum",
            detail = "WWII PPSh-41 Drum (+36 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // RED ARMY DRUM",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 5,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 4,
                CraftingMaterial.GUN_LUBE to 2
            ),
            attachmentKey = "att_ppsh_71_drum",
            description = "High-capacity 71-round spiral steel drum for nonstop PPSh-41 bursts."
        ),
        CraftingRecipe(
            id = "craft_att_thompson_vert_grip",
            name = "Sculpted Walnut Vertical Foregrip",
            detail = "WWII SMG Foregrip (-22% Spread)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "UNCOMMON // GRIP",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.WOOD_FURNITURE to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_thompson_vert_grip",
            description = "Carved hardwood pistol foregrip bolting onto Tommy guns & vintage SMGs."
        ),
        CraftingRecipe(
            id = "craft_att_wwii_lafette_bipod",
            name = "Folding Steel Squad Machine Bipod",
            detail = "WWI/WWII/Cold War MG (+55 ADS, -38% Spread)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "UNCOMMON // SUPPORT",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 4,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 3
            ),
            attachmentKey = "att_wwii_lafette_bipod",
            description = "Stamped folding steel bipod locking down sustained automatic fire on heavy MGs."
        ),
        CraftingRecipe(
            id = "craft_att_m1a1_folding_wire_stock",
            name = "Paratrooper Steel Wire Stock",
            detail = "WWII Carbine / SMG (-12 Move Pen)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "UNCOMMON // AIRBORNE",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 3,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_m1a1_folding_wire_stock",
            description = "Lightweight twin-strut folding buttstock for airborne sprint-to-shoulder speed."
        ),
        CraftingRecipe(
            id = "craft_att_match_enbloc_tuners",
            name = "National Match Trigger & Gas Tune",
            detail = "WWI/WWII Internals (+5 DMG, +10% ROF)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // MATCH",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.TRIGGER_ASSEMBLY to 1,
                CraftingMaterial.GUN_LUBE to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_match_enbloc_tuners",
            description = "Hand-stoned two-stage match sear tune and lapped locking lugs."
        ),
        CraftingRecipe(
            id = "craft_att_an_pvs2_starlight",
            name = "AN/PVS-2 Starlight Night Scope",
            detail = "Cold War Starlight Scope (+115 ADS)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "GRAIL // COLD WAR NV",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.OPTICAL_GLASS to 3,
                CraftingMaterial.ELECTRONIC_PARTS to 3,
                CraftingMaterial.LITHIUM_CELLS to 3
            ),
            attachmentKey = "att_an_pvs2_starlight",
            description = "First-generation Vietnam-era image-intensifier scope conquering night sectors."
        ),
        CraftingRecipe(
            id = "craft_att_singlepoint_oeg",
            name = "Singlepoint Red-Dot Sight",
            detail = "Cold War Fiber Reflex (+42 Range)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "UNCOMMON // RETRO DOT",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.OPTICAL_GLASS to 1,
                CraftingMaterial.POLYMER_STOCK to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_singlepoint_oeg",
            description = "Iconic 1970 Son Tay Raid fiber-illuminated occluded eye gunsight."
        ),
        CraftingRecipe(
            id = "craft_att_sionics_can",
            name = "SIONICS Two-Stage SMG Can",
            detail = "Cold War MAC-10 / Uzi / MP5 Can (-70% Noise)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // COLD WAR CAN",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.SUPPRESSOR_BAFFLES to 2,
                CraftingMaterial.SCRAP_METAL to 2,
                CraftingMaterial.TACTICAL_CLOTH to 1
            ),
            attachmentKey = "att_sionics_can",
            description = "Nomex-wrapped two-stage steel suppressor turning wild open-bolt SMGs controllable."
        ),
        CraftingRecipe(
            id = "craft_att_ak_slant_brake",
            name = "AKM 14x1LH Slant Compensator",
            detail = "Cold War / Modern AK Brake (-24% Spread)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "COMMON // KALASHNIKOV",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 1
            ),
            attachmentKey = "att_ak_slant_brake",
            description = "Classic angled muzzle cut holding AK burst climb flat."
        ),
        CraftingRecipe(
            id = "craft_att_bakelite_ak_dong",
            name = "Romanian Laminated Pistol Foregrip",
            detail = "Cold War AK Foregrip (-20% Spread)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "UNCOMMON // COLD WAR",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.WOOD_FURNITURE to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_bakelite_ak_dong",
            description = "Sculpted laminated lower handguard vertical grip for Kalashnikovs."
        ),
        CraftingRecipe(
            id = "craft_att_usgi_20_straight_mag",
            name = "USGI 30-Rd Aluminum Magazine",
            detail = "Cold War AR/AK/SMG Ext (+10 Rds, -15% Reload)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "COMMON // STANAG",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_usgi_20_straight_mag",
            description = "Anti-tilt aluminum curved 30-round service magazine."
        ),
        CraftingRecipe(
            id = "craft_att_car15_n1_stock",
            name = "CAR-15 N1 Telescoping Stock",
            detail = "Cold War Carbine Stock (-14 Move Pen)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "UNCOMMON // SOG",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.POLYMER_STOCK to 2,
                CraftingMaterial.SCRAP_METAL to 1,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_car15_n1_stock",
            description = "Lightweight ribbed sliding CAR-15 stock cutting rifle length for CQB."
        ),
        CraftingRecipe(
            id = "craft_att_coldwar_c_cell_light",
            name = "Ring-Mounted Xenon Weapons Light",
            detail = "Cold War Barrel Light (-16% Spread)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "COMMON // 80s CQB",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.LITHIUM_CELLS to 1,
                CraftingMaterial.SCRAP_METAL to 2,
                CraftingMaterial.OPTICAL_GLASS to 1
            ),
            attachmentKey = "att_coldwar_c_cell_light",
            description = "Barrel-clamped SAS/Delta xenon tactical flashlight."
        ),
        CraftingRecipe(
            id = "craft_att_rmr_type2",
            name = "Trijicon RMR Type-2 Red Dot",
            detail = "Modern Pistol / Shotgun Micro Reflex",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "UNCOMMON // OPTIC",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.OPTICAL_GLASS to 2,
                CraftingMaterial.ELECTRONIC_PARTS to 1,
                CraftingMaterial.LITHIUM_CELLS to 1
            ),
            attachmentKey = "att_rmr_type2",
            description = "Forged aluminum micro reflex dot milled for modern handguns and scatterguns."
        ),
        CraftingRecipe(
            id = "craft_att_razor_lpvo_1_6",
            name = "Vortex Razor HD Gen-II 1-6x LPVO",
            detail = "Modern Rifle Variable Optic (+115 Range)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "GRAIL // LPVO",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.OPTICAL_GLASS to 4,
                CraftingMaterial.FORGED_RECEIVER to 1,
                CraftingMaterial.ELECTRONIC_PARTS to 2,
                CraftingMaterial.LITHIUM_CELLS to 1
            ),
            attachmentKey = "att_razor_lpvo_1_6",
            description = "Daylight-bright 1-6x variable optic spanning CQB and 600-meter engagements."
        ),
        CraftingRecipe(
            id = "craft_att_nightforce_atacr",
            name = "Nightforce ATACR 7-35x56 F1",
            detail = "Modern Precision Sniper Glass (+165 Range)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "GRAIL // ELR OPTIC",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.OPTICAL_GLASS to 5,
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.ELECTRONIC_PARTS to 2
            ),
            attachmentKey = "att_nightforce_atacr",
            description = "MIL-SPEC ED glass Mil-XT precision rifle telescope for extreme-range precision."
        ),
        CraftingRecipe(
            id = "craft_att_salvo12_shotgun_can",
            name = "SilencerCo Salvo 12 Gauge Can",
            detail = "Modern Shotgun Suppressor (-70% Noise)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // SHOTGUN CAN",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.SUPPRESSOR_BAFFLES to 4,
                CraftingMaterial.FORGED_RECEIVER to 1,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_salvo12_shotgun_can",
            description = "Modular titanium rectangular baffle stack taming 12-gauge thunder."
        ),
        CraftingRecipe(
            id = "craft_att_full_shotgun_choke",
            name = "Carlson's Tactical Breacher Choke",
            detail = "Modern Shotgun Choke (+32 Range, -28% Spread)",
            tier = CraftingTier.TIER_1,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "COMMON // CHOKE",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 1
            ),
            attachmentKey = "att_full_shotgun_choke",
            description = "Ported toothed tactical choke tightening 00-buck shot patterns."
        ),
        CraftingRecipe(
            id = "craft_att_warcomp_brake",
            name = "SureFire WarComp 3-Prong Brake",
            detail = "Modern Rifle Compensator (-25% Spread)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "UNCOMMON // MUZZLE",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 1,
                CraftingMaterial.SCRAP_METAL to 2
            ),
            attachmentKey = "att_warcomp_brake",
            description = "Ported three-prong flash hider and compensator keeping sights pinned flat."
        ),
        CraftingRecipe(
            id = "craft_att_x300u_light",
            name = "SureFire X300U-B 1,000-Lumen Light",
            detail = "Modern Underbarrel Light (-18% Spread)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "UNCOMMON // LIGHT",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.LITHIUM_CELLS to 2,
                CraftingMaterial.ELECTRONIC_PARTS to 1,
                CraftingMaterial.OPTICAL_GLASS to 1
            ),
            attachmentKey = "att_x300u_light",
            description = "1,000-lumen TIR weaponlight cutting through pitch-black hallways."
        ),
        CraftingRecipe(
            id = "craft_att_harris_bipod",
            name = "Harris S-BRM Precision Bipod",
            detail = "Modern Sniper/LMG Bipod (+60 ADS, -40% Spread)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "UNCOMMON // BIPOD",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.SCRAP_METAL to 3,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 3
            ),
            attachmentKey = "att_harris_bipod",
            description = "Notched-leg spring bipod locking crosshairs dead still for long holds."
        ),
        CraftingRecipe(
            id = "craft_att_d60_drum",
            name = "Magpul PMAG D-60 / 60-Rd Drum",
            detail = "Modern Rifle/PDW Drum (+30 Rounds)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "GRAIL // DRUM MAG",
            rarityColorHex = 0xFFC084FC,
            costs = mapOf(
                CraftingMaterial.POLYMER_STOCK to 4,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 4,
                CraftingMaterial.GUN_LUBE to 2
            ),
            attachmentKey = "att_d60_drum",
            description = "Compact 60-round ratcheting polymer drum doubling standard rifle endurance."
        ),
        CraftingRecipe(
            id = "craft_att_b5_sopmod_stock",
            name = "B5 Systems Enhanced SOPMOD Stock",
            detail = "Modern Stock (-16 Move Pen, -20% Bloom)",
            tier = CraftingTier.TIER_2,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "UNCOMMON // STOCK",
            rarityColorHex = 0xFF34D399,
            costs = mapOf(
                CraftingMaterial.POLYMER_STOCK to 3,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_b5_sopmod_stock",
            description = "Sloped cheek-weld Spec-Ops buttstock balancing optics alignment & sprint speed."
        ),
        CraftingRecipe(
            id = "craft_att_geissele_ssae",
            name = "Geissele SSA-E Two-Stage Trigger",
            detail = "Modern Match Internals (+6 DMG, +14% ROF)",
            tier = CraftingTier.TIER_3,
            outputKind = CraftOutputKind.ATTACHMENT,
            rarityLabel = "RARE // MATCH TRIGGER",
            rarityColorHex = 0xFF38BDF8,
            costs = mapOf(
                CraftingMaterial.TRIGGER_ASSEMBLY to 2,
                CraftingMaterial.GUN_LUBE to 2,
                CraftingMaterial.INDUSTRIAL_FASTENERS to 2
            ),
            attachmentKey = "att_geissele_ssae",
            description = "Crisp 3.5-lb two-stage match combat trigger engineered for fast precision splits."
        )
    )

    fun ammoSharingGroup(weapon: WeaponId): List<WeaponId> = when (weapon) {
        WeaponId.GLOCK19, WeaponId.PPK, WeaponId.BERETTA_M9, WeaponId.P226, WeaponId.CZ75,
        WeaponId.LUGER_P08, WeaponId.MAUSER_C96, WeaponId.WALTHER_P38, WeaponId.TT33,
        WeaponId.BROWNING_HP, WeaponId.MAKAROV, WeaponId.SIG_M17 ->
            listOf(
                WeaponId.GLOCK19, WeaponId.PPK, WeaponId.BERETTA_M9, WeaponId.P226, WeaponId.CZ75,
                WeaponId.LUGER_P08, WeaponId.MAUSER_C96, WeaponId.WALTHER_P38, WeaponId.TT33,
                WeaponId.BROWNING_HP, WeaponId.MAKAROV, WeaponId.SIG_M17
            )
        WeaponId.M1911, WeaponId.USP45, WeaponId.UMP45, WeaponId.VECTOR, WeaponId.MAC10,
        WeaponId.THOMPSON_M1928, WeaponId.M3_GREASE, WeaponId.COLT_SAA, WeaponId.SCHOFIELD, WeaponId.WEBLEY_MK6 ->
            listOf(
                WeaponId.M1911, WeaponId.USP45, WeaponId.UMP45, WeaponId.VECTOR, WeaponId.MAC10,
                WeaponId.THOMPSON_M1928, WeaponId.M3_GREASE, WeaponId.COLT_SAA, WeaponId.SCHOFIELD, WeaponId.WEBLEY_MK6
            )
        WeaponId.DEAGLE, WeaponId.MODEL_29, WeaponId.FIVE_SEVEN ->
            listOf(WeaponId.DEAGLE, WeaponId.MODEL_29, WeaponId.FIVE_SEVEN)
        WeaponId.SHOTGUN, WeaponId.DOUBLE_BARREL, WeaponId.MOSSBERG_590, WeaponId.SPAS12,
        WeaponId.BENELLI_M4, WeaponId.SAIGA12, WeaponId.AA12, WeaponId.WINCHESTER_1897, WeaponId.ITHACA_37 ->
            listOf(
                WeaponId.SHOTGUN, WeaponId.DOUBLE_BARREL, WeaponId.MOSSBERG_590, WeaponId.SPAS12,
                WeaponId.BENELLI_M4, WeaponId.SAIGA12, WeaponId.AA12, WeaponId.WINCHESTER_1897, WeaponId.ITHACA_37
            )
        WeaponId.SMG, WeaponId.MP5SD, WeaponId.SCORPION_EVO, WeaponId.BIZON, WeaponId.P90, WeaponId.MP7,
        WeaponId.MP40, WeaponId.PPSH41, WeaponId.STEN_MK2, WeaponId.UZI, WeaponId.APC9 ->
            listOf(
                WeaponId.SMG, WeaponId.MP5SD, WeaponId.SCORPION_EVO, WeaponId.BIZON, WeaponId.P90, WeaponId.MP7,
                WeaponId.MP40, WeaponId.PPSH41, WeaponId.STEN_MK2, WeaponId.UZI, WeaponId.APC9
            )
        WeaponId.M4, WeaponId.MK18, WeaponId.HK416, WeaponId.AUG_A3, WeaponId.FAMAS, WeaponId.G36C,
        WeaponId.AK74M, WeaponId.RPK74, WeaponId.M16A1, WeaponId.XM177, WeaponId.GALIL_ARM,
        WeaponId.TAVOR_X95, WeaponId.M1_CARBINE ->
            listOf(
                WeaponId.M4, WeaponId.MK18, WeaponId.HK416, WeaponId.AUG_A3, WeaponId.FAMAS, WeaponId.G36C,
                WeaponId.AK74M, WeaponId.RPK74, WeaponId.M16A1, WeaponId.XM177, WeaponId.GALIL_ARM,
                WeaponId.TAVOR_X95, WeaponId.M1_CARBINE
            )
        WeaponId.AK103, WeaponId.AK47, WeaponId.AS_VAL, WeaponId.STG44, WeaponId.SKS45 ->
            listOf(WeaponId.AK103, WeaponId.AK47, WeaponId.AS_VAL, WeaponId.STG44, WeaponId.SKS45)
        WeaponId.SCAR_H, WeaponId.FN_FAL, WeaponId.G3A3, WeaponId.MK14, WeaponId.SR25, WeaponId.SVD,
        WeaponId.MCX_SPEAR, WeaponId.M1_GARAND, WeaponId.SPRINGFIELD_1903, WeaponId.LEE_ENFIELD,
        WeaponId.MOSIN_M9130, WeaponId.WINCHESTER_1873, WeaponId.WINCHESTER_1894, WeaponId.SHARPS_1874 ->
            listOf(
                WeaponId.SCAR_H, WeaponId.FN_FAL, WeaponId.G3A3, WeaponId.MK14, WeaponId.SR25, WeaponId.SVD,
                WeaponId.MCX_SPEAR, WeaponId.M1_GARAND, WeaponId.SPRINGFIELD_1903, WeaponId.LEE_ENFIELD,
                WeaponId.MOSIN_M9130, WeaponId.WINCHESTER_1873, WeaponId.WINCHESTER_1894, WeaponId.SHARPS_1874
            )
        WeaponId.AWM, WeaponId.M82A1, WeaponId.M24, WeaponId.KAR98K, WeaponId.CHEYTAC_M200 ->
            listOf(WeaponId.AWM, WeaponId.M82A1, WeaponId.M24, WeaponId.KAR98K, WeaponId.CHEYTAC_M200)
        WeaponId.M249, WeaponId.PKM, WeaponId.M240B, WeaponId.BAR_M1918, WeaponId.LEWIS_GUN,
        WeaponId.BREN_LMG, WeaponId.MG42, WeaponId.M60_GPMG ->
            listOf(
                WeaponId.M249, WeaponId.PKM, WeaponId.M240B, WeaponId.BAR_M1918, WeaponId.LEWIS_GUN,
                WeaponId.BREN_LMG, WeaponId.MG42, WeaponId.M60_GPMG
            )
        else -> listOf(weapon)
    }
}
