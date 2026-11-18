package com.example.game.model

import kotlin.math.roundToInt

enum class AttachmentSlot(val label: String, val shortLabel: String) {
    OPTIC("Optic / Sight", "OPTIC"),
    MUZZLE("Muzzle / Suppressor", "MUZZLE"),
    UNDERBARREL("Underbarrel / Foregrip", "GRIP/BIPOD"),
    TACTICAL("Rail / Illumination", "TACTICAL"),
    MAGAZINE("Magazine / Feed", "MAGAZINE"),
    STOCK_GRIP("Stock / Pistol Grip", "STOCK/GRIP"),
    BARREL_INTERNAL("Barrel / Action", "BARREL/ACT")
}

/**
 * Platform mount tags controlling both physical fitment and historical authenticity.
 */
enum class MountTag {
    FRONTIER_REVOLVER,
    LEVER_RIFLE,
    FALLING_BLOCK_RIFLE,
    TRENCH_COACH_SHOTGUN,
    PUMP_AUTO_SHOTGUN,
    MAG_SHOTGUN,
    WWI_MAUSER_C96,
    WWI_LUGER,
    WWI_REVOLVER,
    MIL_BOLT_RIFLE,
    VINTAGE_WWII_SMG,
    THOMPSON_PLATFORM,
    PPSH_PLATFORM,
    STEN_PLATFORM,
    GARAND_CARBINE_PLATFORM,
    MG_BELT_PLATFORM,
    AK_PLATFORM,
    AR_PLATFORM,
    COLD_WAR_CARRY_HANDLE,
    COLD_WAR_BATTLE_RIFLE,
    MODERN_PISTOL_OPTIC,
    THREADED_PISTOL,
    REVOLVER_OPTIC_RIB,
    MODERN_SMG_PDW,
    MODERN_RIFLE,
    SNIPER_DMR_PRECISION,
    HEAVY_50_BARRETT,
    CROSSBOW_RAIL,
    SUBMACHINE_SMG,
    UNIVERSAL_WOOD_SLING
}

data class WeaponAttachment(
    val key: String,
    val name: String,
    val shortName: String,
    val slot: AttachmentSlot,
    val era: WeaponEra,
    val erasAllowed: Set<WeaponEra> = setOf(era),
    val requiredTags: Set<MountTag>,
    val rarityLabel: String,
    val rarityColorHex: Long,
    val summary: String,
    val historicalNote: String,
    // Stat modifiers
    val damageBonus: Int = 0,
    val damageMultiplier: Float = 1f,
    val penetrationBonus: Int = 0,
    val rangeBonus: Float = 0f,
    val fovDelta: Float = 0f,
    val movePenaltyDelta: Float = 0f,
    val moveBloomDelta: Float = 0f,
    val stillRangeDelta: Float = 0f,
    val spreadMultiplier: Float = 1f,
    val fireDelayMultiplier: Float = 1f,
    val magSizeDelta: Int = 0,
    val magSizeMultiplier: Float = 1f,
    val reloadMultiplier: Float = 1f,
    val noiseMultiplier: Float = 1f,
    val forcesSuppressed: Boolean = false,
    val knockbackBonus: Float = 0f,
    val bayoReachBonus: Float = 0f,
    val visualCode: String = key
) {
    fun isCompatibleWith(weapon: WeaponId): Boolean {
        if (weapon.isMelee) return false
        if (weapon.era !in erasAllowed) return false
        val wTags = WeaponMountResolver.tagsFor(weapon)
        return requiredTags.any { it in wTags }
    }

    fun statCallouts(): List<Pair<String, Boolean>> {
        val list = mutableListOf<Pair<String, Boolean>>()
        if (forcesSuppressed || noiseMultiplier < 0.7f) {
            val pct = ((1f - noiseMultiplier) * 100f).roundToInt()
            list += ("-$pct% Noise${if (forcesSuppressed) " (Suppressed)" else ""}" to true)
        }
        if (damageBonus > 0 || damageMultiplier > 1.01f) {
            val txt = if (damageBonus > 0) "+$damageBonus DMG" else "+${((damageMultiplier - 1f) * 100f).roundToInt()}% DMG"
            list += (txt to true)
        }
        if (penetrationBonus > 0) list += ("+$penetrationBonus% Pen" to true)
        if (rangeBonus > 0f) list += ("+${rangeBonus.roundToInt()} Range" to true)
        if (stillRangeDelta > 0f) list += ("+${stillRangeDelta.roundToInt()} ADS Range" to true)
        if (fovDelta < -0.01f) list += ("Tighter Optic Cone" to true)
        if (spreadMultiplier < 0.96f) {
            val pct = ((1f - spreadMultiplier) * 100f).roundToInt()
            list += ("-$pct% Spread" to true)
        }
        if (fireDelayMultiplier < 0.96f) {
            val pct = ((1f - fireDelayMultiplier) * 100f).roundToInt()
            list += ("+$pct% Fire Rate" to true)
        }
        if (magSizeDelta > 0 || magSizeMultiplier > 1.05f) {
            val txt = if (magSizeDelta > 0) "+$magSizeDelta Mag Cap" else "+${((magSizeMultiplier - 1f) * 100f).roundToInt()}% Mag Cap"
            list += (txt to true)
        }
        if (reloadMultiplier < 0.96f) {
            val pct = ((1f - reloadMultiplier) * 100f).roundToInt()
            list += ("-$pct% Reload Time" to true)
        } else if (reloadMultiplier > 1.05f) {
            val pct = ((reloadMultiplier - 1f) * 100f).roundToInt()
            list += ("+$pct% Reload Time" to false)
        }
        if (movePenaltyDelta < -1f) list += ("Better Move Handling" to true)
        if (bayoReachBonus > 0f) list += ("+${bayoReachBonus.roundToInt()} Bayonet Thrust" to true)
        if (list.isEmpty()) list += ("Field Reliability Upgrade" to true)
        return list
    }
}

data class EffectiveWeaponStats(
    val weapon: WeaponId,
    val damage: Int,
    val penetrationPct: Int,
    val baseRange: Float,
    val baseHalfFov: Float,
    val moveRangePenalty: Float,
    val moveFovBloom: Float,
    val stillRangeBonus: Float,
    val stillFovTighten: Float,
    val spreadRad: Float,
    val fireDelayMs: Long,
    val magSize: Int,
    val reloadTimeMs: Long,
    val noiseRadius: Float,
    val suppressed: Boolean,
    val knockback: Float,
    val equippedAttachments: List<WeaponAttachment>,
    val bayonetReachBonus: Float
)

object WeaponMountResolver {
    fun tagsFor(weapon: WeaponId): Set<MountTag> = when (weapon) {
        // Frontier
        WeaponId.COLT_SAA, WeaponId.SCHOFIELD -> setOf(MountTag.FRONTIER_REVOLVER)
        WeaponId.WINCHESTER_1873, WeaponId.WINCHESTER_1894 -> setOf(MountTag.LEVER_RIFLE, MountTag.UNIVERSAL_WOOD_SLING)
        WeaponId.SHARPS_1874 -> setOf(MountTag.FALLING_BLOCK_RIFLE, MountTag.UNIVERSAL_WOOD_SLING)
        WeaponId.WINCHESTER_1897 -> setOf(MountTag.TRENCH_COACH_SHOTGUN, MountTag.UNIVERSAL_WOOD_SLING)
        WeaponId.DOUBLE_BARREL -> setOf(MountTag.TRENCH_COACH_SHOTGUN, MountTag.PUMP_AUTO_SHOTGUN, MountTag.UNIVERSAL_WOOD_SLING)

        // WWI
        WeaponId.LUGER_P08 -> setOf(MountTag.WWI_LUGER, MountTag.THREADED_PISTOL)
        WeaponId.MAUSER_C96 -> setOf(MountTag.WWI_MAUSER_C96)
        WeaponId.WEBLEY_MK6 -> setOf(MountTag.WWI_REVOLVER)
        WeaponId.SPRINGFIELD_1903, WeaponId.LEE_ENFIELD, WeaponId.MOSIN_M9130, WeaponId.KAR98K ->
            setOf(MountTag.MIL_BOLT_RIFLE, MountTag.UNIVERSAL_WOOD_SLING)
        WeaponId.BAR_M1918 -> setOf(MountTag.MG_BELT_PLATFORM, MountTag.GARAND_CARBINE_PLATFORM, MountTag.UNIVERSAL_WOOD_SLING)
        WeaponId.LEWIS_GUN -> setOf(MountTag.MG_BELT_PLATFORM, MountTag.UNIVERSAL_WOOD_SLING)

        // WWII
        WeaponId.PPK, WeaponId.WALTHER_P38, WeaponId.TT33, WeaponId.BROWNING_HP, WeaponId.M1911, WeaponId.MAKAROV ->
            setOf(MountTag.THREADED_PISTOL, MountTag.MODERN_PISTOL_OPTIC)
        WeaponId.THOMPSON_M1928 -> setOf(MountTag.THOMPSON_PLATFORM, MountTag.VINTAGE_WWII_SMG, MountTag.SUBMACHINE_SMG)
        WeaponId.MP40, WeaponId.M3_GREASE -> setOf(MountTag.VINTAGE_WWII_SMG, MountTag.SUBMACHINE_SMG)
        WeaponId.PPSH41 -> setOf(MountTag.PPSH_PLATFORM, MountTag.VINTAGE_WWII_SMG, MountTag.SUBMACHINE_SMG)
        WeaponId.STEN_MK2 -> setOf(MountTag.STEN_PLATFORM, MountTag.VINTAGE_WWII_SMG, MountTag.SUBMACHINE_SMG)
        WeaponId.M1_GARAND, WeaponId.M1_CARBINE -> setOf(MountTag.GARAND_CARBINE_PLATFORM, MountTag.UNIVERSAL_WOOD_SLING)
        WeaponId.STG44 -> setOf(MountTag.VINTAGE_WWII_SMG, MountTag.AK_PLATFORM, MountTag.UNIVERSAL_WOOD_SLING)
        WeaponId.BREN_LMG, WeaponId.MG42 -> setOf(MountTag.MG_BELT_PLATFORM)

        // Cold War
        WeaponId.MODEL_29 -> setOf(MountTag.REVOLVER_OPTIC_RIB, MountTag.FRONTIER_REVOLVER)
        WeaponId.P226 -> setOf(MountTag.THREADED_PISTOL, MountTag.MODERN_PISTOL_OPTIC)
        WeaponId.SMG, WeaponId.MP5SD, WeaponId.MAC10, WeaponId.UZI ->
            setOf(MountTag.SUBMACHINE_SMG, MountTag.MODERN_SMG_PDW)
        WeaponId.SKS45 -> setOf(MountTag.AK_PLATFORM, MountTag.MIL_BOLT_RIFLE, MountTag.UNIVERSAL_WOOD_SLING)
        WeaponId.M16A1, WeaponId.XM177 -> setOf(MountTag.AR_PLATFORM, MountTag.COLD_WAR_CARRY_HANDLE, MountTag.MODERN_RIFLE)
        WeaponId.GALIL_ARM, WeaponId.AK47, WeaponId.SVD -> setOf(MountTag.AK_PLATFORM, MountTag.UNIVERSAL_WOOD_SLING)
        WeaponId.FN_FAL, WeaponId.G3A3 -> setOf(MountTag.COLD_WAR_BATTLE_RIFLE, MountTag.MODERN_RIFLE)
        WeaponId.SHOTGUN, WeaponId.SPAS12, WeaponId.ITHACA_37 ->
            setOf(MountTag.PUMP_AUTO_SHOTGUN, MountTag.TRENCH_COACH_SHOTGUN, MountTag.UNIVERSAL_WOOD_SLING)
        WeaponId.PKM -> setOf(MountTag.AK_PLATFORM, MountTag.MG_BELT_PLATFORM)
        WeaponId.M60_GPMG, WeaponId.M249 -> setOf(MountTag.MG_BELT_PLATFORM, MountTag.MODERN_RIFLE)
        WeaponId.M82A1 -> setOf(MountTag.HEAVY_50_BARRETT, MountTag.SNIPER_DMR_PRECISION)

        // Modern Pistols
        WeaponId.GLOCK19, WeaponId.BERETTA_M9, WeaponId.CZ75, WeaponId.USP45, WeaponId.FIVE_SEVEN,
        WeaponId.DEAGLE, WeaponId.SIG_M17 -> setOf(MountTag.MODERN_PISTOL_OPTIC, MountTag.THREADED_PISTOL)

        // Modern SMGs
        WeaponId.UMP45, WeaponId.BIZON, WeaponId.SCORPION_EVO, WeaponId.MP7, WeaponId.P90, WeaponId.VECTOR, WeaponId.APC9 ->
            setOf(MountTag.MODERN_SMG_PDW, MountTag.SUBMACHINE_SMG, MountTag.MODERN_RIFLE)

        // Modern Shotguns
        WeaponId.MOSSBERG_590, WeaponId.BENELLI_M4 -> setOf(MountTag.PUMP_AUTO_SHOTGUN, MountTag.MODERN_RIFLE)
        WeaponId.SAIGA12, WeaponId.AA12 -> setOf(MountTag.MAG_SHOTGUN, MountTag.PUMP_AUTO_SHOTGUN, MountTag.AK_PLATFORM)

        // Modern Rifles
        WeaponId.M4, WeaponId.MK18, WeaponId.HK416, WeaponId.AUG_A3, WeaponId.FAMAS, WeaponId.G36C,
        WeaponId.SCAR_H, WeaponId.TAVOR_X95, WeaponId.MCX_SPEAR -> setOf(MountTag.MODERN_RIFLE, MountTag.AR_PLATFORM)
        WeaponId.AK74M, WeaponId.AK103, WeaponId.AS_VAL -> setOf(MountTag.MODERN_RIFLE, MountTag.AK_PLATFORM)

        // Modern DMR / Sniper / LMG / Special
        WeaponId.MK14, WeaponId.SR25, WeaponId.M24, WeaponId.AWM, WeaponId.CHEYTAC_M200 ->
            setOf(MountTag.SNIPER_DMR_PRECISION, MountTag.MODERN_RIFLE)
        WeaponId.RPK74 -> setOf(MountTag.AK_PLATFORM, MountTag.MODERN_RIFLE, MountTag.MG_BELT_PLATFORM)
        WeaponId.M240B -> setOf(MountTag.MG_BELT_PLATFORM, MountTag.MODERN_RIFLE)
        WeaponId.CROSSBOW -> setOf(MountTag.CROSSBOW_RAIL, MountTag.MODERN_RIFLE)

        else -> emptySet()
    }
}

object WeaponAttachmentCatalog {
    val all: List<WeaponAttachment> = listOf(
        // ============================================================
        // FRONTIER ERA (1870–1899)
        // ============================================================
        WeaponAttachment(
            key = "att_malcolm_brass_scope",
            name = "Wm. Malcolm 6x Long-Tube Brass Scope",
            shortName = "MALCOLM 6X",
            slot = AttachmentSlot.OPTIC,
            era = WeaponEra.FRONTIER,
            erasAllowed = setOf(WeaponEra.FRONTIER, WeaponEra.WWI),
            requiredTags = setOf(MountTag.FALLING_BLOCK_RIFLE, MountTag.LEVER_RIFLE, MountTag.MIL_BOLT_RIFLE),
            rarityLabel = "RARE // FRONTIER OPTIC",
            rarityColorHex = 0xFFD97706,
            summary = "+95 Range, +75 ADS Reach, Tighter Sight Cone",
            historicalNote = "Full-length 1870s external-adjustment brass telescope mounted on buffalo and sharpshooter rifles.",
            rangeBonus = 95f,
            stillRangeDelta = 75f,
            fovDelta = -0.06f,
            spreadMultiplier = 0.72f,
            visualCode = "brass_long_scope"
        ),
        WeaponAttachment(
            key = "att_lyman_tang_peep",
            name = "Lyman #2 Tang Peep Aperture Sight",
            shortName = "TANG PEEP",
            slot = AttachmentSlot.OPTIC,
            era = WeaponEra.FRONTIER,
            erasAllowed = setOf(WeaponEra.FRONTIER, WeaponEra.WWI),
            requiredTags = setOf(MountTag.LEVER_RIFLE, MountTag.FALLING_BLOCK_RIFLE),
            rarityLabel = "UNCOMMON // FRONTIER",
            rarityColorHex = 0xFFD97706,
            summary = "+45 Range, -20% Spread, Zero Tunneling",
            historicalNote = "Folding tang-mounted Vernier aperture iron sight invented in 1879 for lever carbines.",
            rangeBonus = 45f,
            stillRangeDelta = 35f,
            spreadMultiplier = 0.80f,
            visualCode = "tang_peep"
        ),
        WeaponAttachment(
            key = "att_octagon_heavy_barrel",
            name = "Hand-Lapped Heavy Octagon Barrel",
            shortName = "OCTAGON BBL",
            slot = AttachmentSlot.BARREL_INTERNAL,
            era = WeaponEra.FRONTIER,
            erasAllowed = setOf(WeaponEra.FRONTIER),
            requiredTags = setOf(MountTag.LEVER_RIFLE, MountTag.FALLING_BLOCK_RIFLE, MountTag.FRONTIER_REVOLVER),
            rarityLabel = "UNCOMMON // FRONTIER",
            rarityColorHex = 0xFFD97706,
            summary = "+8 Damage, +8% Pen, -18% Spread",
            historicalNote = "Thick octagon-profile charcoal-blued steel reducing barrel whip on frontier trails.",
            damageBonus = 8,
            penetrationBonus = 8,
            rangeBonus = 30f,
            spreadMultiplier = 0.82f,
            visualCode = "heavy_barrel"
        ),
        WeaponAttachment(
            key = "att_pearl_walnut_grips",
            name = "Carved Mother-of-Pearl & Walnut Stocks",
            shortName = "PEARL GRIPS",
            slot = AttachmentSlot.STOCK_GRIP,
            era = WeaponEra.FRONTIER,
            erasAllowed = setOf(WeaponEra.FRONTIER, WeaponEra.WWI),
            requiredTags = setOf(MountTag.FRONTIER_REVOLVER, MountTag.LEVER_RIFLE, MountTag.WWI_REVOLVER),
            rarityLabel = "COMMON // FRONTIER",
            rarityColorHex = 0xFFD97706,
            summary = "+8% Fire Cadence, -12% Reload Time, Better Handling",
            historicalNote = "Hand-checkered walnut revolver panels and buttplates balancing crisp thumb-cocking speed.",
            fireDelayMultiplier = 0.92f,
            reloadMultiplier = 0.88f,
            movePenaltyDelta = -6f,
            visualCode = "wood_grip"
        ),
        WeaponAttachment(
            key = "att_trench_heatshield",
            name = "M1897 Ventilated Heat Shield & Lug",
            shortName = "HEAT SHIELD",
            slot = AttachmentSlot.TACTICAL,
            era = WeaponEra.FRONTIER,
            erasAllowed = setOf(WeaponEra.FRONTIER, WeaponEra.WWI, WeaponEra.WWII),
            requiredTags = setOf(MountTag.TRENCH_COACH_SHOTGUN),
            rarityLabel = "UNCOMMON // TRENCH",
            rarityColorHex = 0xFFD97706,
            summary = "+14% Slam-Fire Speed, Tighter Pump Control",
            historicalNote = "Perforated barrel shroud letting trench gunners rack blistering rapid follow-up shells.",
            fireDelayMultiplier = 0.86f,
            movePenaltyDelta = -5f,
            visualCode = "heat_shield"
        ),
        WeaponAttachment(
            key = "att_leather_saddle_scabbard",
            name = "Stitched Leather Saddle Ring & Sling",
            shortName = "LEATHER SLING",
            slot = AttachmentSlot.UNDERBARREL,
            era = WeaponEra.FRONTIER,
            erasAllowed = setOf(WeaponEra.FRONTIER, WeaponEra.WWI, WeaponEra.WWII, WeaponEra.COLD_WAR),
            requiredTags = setOf(MountTag.UNIVERSAL_WOOD_SLING),
            rarityLabel = "COMMON // HERITAGE",
            rarityColorHex = 0xFFD97706,
            summary = "-8 Move Penalty, -15% Move Bloom",
            historicalNote = "Thick oiled latigo sling stabilized with shooter loop support on trail carbines.",
            movePenaltyDelta = -8f,
            moveBloomDelta = -0.02f,
            reloadMultiplier = 0.94f,
            visualCode = "sling_wrap"
        ),

        // ============================================================
        // WORLD WAR I ERA (1900–1918)
        // ============================================================
        WeaponAttachment(
            key = "att_warner_swasey_prism",
            name = "Warner & Swasey M1913 Musket Scope",
            shortName = "M1913 PRISM",
            slot = AttachmentSlot.OPTIC,
            era = WeaponEra.WWI,
            erasAllowed = setOf(WeaponEra.WWI, WeaponEra.WWII),
            requiredTags = setOf(MountTag.MIL_BOLT_RIFLE),
            rarityLabel = "RARE // WWI OPTIC",
            rarityColorHex = 0xFF94A3B8,
            summary = "+85 Range, +70 ADS Range, -25% Spread",
            historicalNote = "Compact 5.2x offset prism optic issued on early Springfield M1903 sharpshooter rifles.",
            rangeBonus = 85f,
            stillRangeDelta = 70f,
            fovDelta = -0.05f,
            spreadMultiplier = 0.75f,
            visualCode = "vintage_scope"
        ),
        WeaponAttachment(
            key = "att_c96_stock_holster",
            name = "Mauser C96 Detachable Walnut Stock",
            shortName = "C96 STOCK",
            slot = AttachmentSlot.STOCK_GRIP,
            era = WeaponEra.WWI,
            erasAllowed = setOf(WeaponEra.WWI),
            requiredTags = setOf(MountTag.WWI_MAUSER_C96),
            rarityLabel = "RARE // WWI CARBINE",
            rarityColorHex = 0xFF94A3B8,
            summary = "+55 Range, -40% Spread, Steady Carbine Stock",
            historicalNote = "Hollow walnut belt holster clipping cleanly into the Broomhandle backstrap slot.",
            rangeBonus = 55f,
            stillRangeDelta = 40f,
            spreadMultiplier = 0.60f,
            movePenaltyDelta = -6f,
            visualCode = "c96_stock"
        ),
        WeaponAttachment(
            key = "att_luger_snail_drum",
            name = "Trommelmagazin 32-Rd Snail Drum",
            shortName = "SNAIL DRUM 32",
            slot = AttachmentSlot.MAGAZINE,
            era = WeaponEra.WWI,
            erasAllowed = setOf(WeaponEra.WWI),
            requiredTags = setOf(MountTag.WWI_LUGER),
            rarityLabel = "GRAIL // WWI DRUM",
            rarityColorHex = 0xFF94A3B8,
            summary = "32-Round Trench Drum (+24 Rounds)",
            historicalNote = "Iconic 1917 Imperial Artillery Luger winding snail-drum engineered for stormtroop raids.",
            magSizeDelta = 24,
            reloadMultiplier = 1.18f,
            visualCode = "drum_mag"
        ),
        WeaponAttachment(
            key = "att_maxim_1910_can",
            name = "Hiram Maxim 1910 Brass Muffler",
            shortName = "MAXIM 1910",
            slot = AttachmentSlot.MUZZLE,
            era = WeaponEra.WWI,
            erasAllowed = setOf(WeaponEra.WWI, WeaponEra.WWII),
            requiredTags = setOf(MountTag.MIL_BOLT_RIFLE, MountTag.WWI_LUGER, MountTag.THREADED_PISTOL),
            rarityLabel = "RARE // EARLY SILENCER",
            rarityColorHex = 0xFF94A3B8,
            summary = "Suppressed (-65% Noise), -15% Spread",
            historicalNote = "World's first commercial baffled silencer patented by Hiram Percy Maxim in 1909.",
            forcesSuppressed = true,
            noiseMultiplier = 0.35f,
            spreadMultiplier = 0.85f,
            rangeBonus = 20f,
            visualCode = "suppressor"
        ),
        WeaponAttachment(
            key = "att_m1905_bayonet",
            name = "M1905 / Pattern 1907 Sword Bayonet",
            shortName = "SWORD BAYONET",
            slot = AttachmentSlot.UNDERBARREL,
            era = WeaponEra.WWI,
            erasAllowed = setOf(WeaponEra.WWI, WeaponEra.WWII, WeaponEra.FRONTIER),
            requiredTags = setOf(MountTag.MIL_BOLT_RIFLE, MountTag.TRENCH_COACH_SHOTGUN, MountTag.GARAND_CARBINE_PLATFORM),
            rarityLabel = "UNCOMMON // TRENCH",
            rarityColorHex = 0xFF94A3B8,
            summary = "+14 Melee Reach, +8 Close Knockback",
            historicalNote = "16-inch forged steel bayonet clamping beneath military rifle barrels and Trench 1897 lugs.",
            bayoReachBonus = 18f,
            knockbackBonus = 8f,
            visualCode = "bayonet"
        ),
        WeaponAttachment(
            key = "att_brass_stripper_clips",
            name = "Spring Brass 5-Rd Stripper Clips",
            shortName = "STRIPPER CLIPS",
            slot = AttachmentSlot.MAGAZINE,
            era = WeaponEra.WWI,
            erasAllowed = setOf(WeaponEra.WWI, WeaponEra.WWII, WeaponEra.COLD_WAR),
            requiredTags = setOf(MountTag.MIL_BOLT_RIFLE, MountTag.WWI_MAUSER_C96, MountTag.AK_PLATFORM),
            rarityLabel = "COMMON // RELOAD",
            rarityColorHex = 0xFF94A3B8,
            summary = "-28% Reload Time",
            historicalNote = "Spring-steel charger bridges thumbs-pushing an entire five or ten-round column in one stroke.",
            reloadMultiplier = 0.72f,
            visualCode = "speed_clip"
        ),

        // ============================================================
        // WORLD WAR II ERA (1930–1945)
        // ============================================================
        WeaponAttachment(
            key = "att_m84_infantry_scope",
            name = "M84 2.2x Offset Infantry Scope",
            shortName = "M84 2.2X",
            slot = AttachmentSlot.OPTIC,
            era = WeaponEra.WWII,
            erasAllowed = setOf(WeaponEra.WWII, WeaponEra.COLD_WAR),
            requiredTags = setOf(MountTag.GARAND_CARBINE_PLATFORM, MountTag.MIL_BOLT_RIFLE),
            rarityLabel = "RARE // WWII OPTIC",
            rarityColorHex = 0xFF34D399,
            summary = "+75 Range, +60 ADS Range, +6% Pen",
            historicalNote = "Side-offset 2.2x Garand Sniper (M1C/M1D) telescope clearing top-loading en-bloc ejection.",
            rangeBonus = 75f,
            stillRangeDelta = 60f,
            fovDelta = -0.04f,
            spreadMultiplier = 0.76f,
            penetrationBonus = 6,
            visualCode = "vintage_scope"
        ),
        WeaponAttachment(
            key = "att_pu_35x_scope",
            name = "PU 3.5x Steel Sniping Telescope",
            shortName = "PU 3.5X",
            slot = AttachmentSlot.OPTIC,
            era = WeaponEra.WWII,
            erasAllowed = setOf(WeaponEra.WWII, WeaponEra.COLD_WAR),
            requiredTags = setOf(MountTag.MIL_BOLT_RIFLE, MountTag.AK_PLATFORM),
            rarityLabel = "RARE // EASTERN FRONT",
            rarityColorHex = 0xFF34D399,
            summary = "+95 Range, +75 ADS Range, -28% Spread",
            historicalNote = "Compact rugged 3.5x German-post reticle scope mounted over bent-bolt Mosin and Kar98k receivers.",
            rangeBonus = 95f,
            stillRangeDelta = 75f,
            fovDelta = -0.055f,
            spreadMultiplier = 0.72f,
            visualCode = "vintage_scope"
        ),
        WeaponAttachment(
            key = "att_zf4_4x_scope",
            name = "ZF-4 4x Side-Mount Rifle Scope",
            shortName = "ZF-4 4X",
            slot = AttachmentSlot.OPTIC,
            era = WeaponEra.WWII,
            erasAllowed = setOf(WeaponEra.WWII),
            requiredTags = setOf(MountTag.VINTAGE_WWII_SMG, MountTag.MIL_BOLT_RIFLE),
            rarityLabel = "RARE // WWII OPTIC",
            rarityColorHex = 0xFF34D399,
            summary = "+90 Range, +70 ADS Range, -26% Spread",
            historicalNote = "Lightweight WWII 4x rail-mounted scope issued on late-war Kar98k and StG 44 receivers.",
            rangeBonus = 90f,
            stillRangeDelta = 70f,
            fovDelta = -0.05f,
            spreadMultiplier = 0.74f,
            visualCode = "vintage_scope"
        ),
        WeaponAttachment(
            key = "att_cutts_compensator",
            name = "Lyman Cutts Slotted Compensator",
            shortName = "CUTTS COMP",
            slot = AttachmentSlot.MUZZLE,
            era = WeaponEra.WWII,
            erasAllowed = setOf(WeaponEra.WWII, WeaponEra.WWI),
            requiredTags = setOf(MountTag.THOMPSON_PLATFORM, MountTag.TRENCH_COACH_SHOTGUN, MountTag.MG_BELT_PLATFORM),
            rarityLabel = "UNCOMMON // WWII MUZZLE",
            rarityColorHex = 0xFF34D399,
            summary = "-28% Full-Auto Spread, -15% Climb",
            historicalNote = "Top-slotted steel gas compensator noticeably holding Thompson barrels flat during automatic bursts.",
            spreadMultiplier = 0.72f,
            moveBloomDelta = -0.02f,
            rangeBonus = 18f,
            visualCode = "brake"
        ),
        WeaponAttachment(
            key = "att_sten_mkiis_suppressor",
            name = "Sten Mk IIS Integral Baffle Jacket",
            shortName = "MK IIS CAN",
            slot = AttachmentSlot.MUZZLE,
            era = WeaponEra.WWII,
            erasAllowed = setOf(WeaponEra.WWII),
            requiredTags = setOf(MountTag.STEN_PLATFORM, MountTag.VINTAGE_WWII_SMG),
            rarityLabel = "RARE // SOE STEALTH",
            rarityColorHex = 0xFF34D399,
            summary = "Suppressed (-74% Noise), -15% Spread",
            historicalNote = "Canvas-laced ported suppressor sleeve built by Britain's SOE for covert night bridge raids.",
            forcesSuppressed = true,
            noiseMultiplier = 0.26f,
            spreadMultiplier = 0.85f,
            visualCode = "suppressor"
        ),
        WeaponAttachment(
            key = "att_oss_welrod_can",
            name = "OSS High-Standard Silk-Baffle Can",
            shortName = "OSS BAFFLE",
            slot = AttachmentSlot.MUZZLE,
            era = WeaponEra.WWII,
            erasAllowed = setOf(WeaponEra.WWII, WeaponEra.COLD_WAR),
            requiredTags = setOf(MountTag.THREADED_PISTOL),
            rarityLabel = "RARE // WWII STEALTH",
            rarityColorHex = 0xFF34D399,
            summary = "Suppressed (-75% Noise), Zero Flash",
            historicalNote = "Bronze-mesh and neoprene wipe suppressor issued to OSS agents across occupied Europe.",
            forcesSuppressed = true,
            noiseMultiplier = 0.25f,
            spreadMultiplier = 0.90f,
            visualCode = "suppressor"
        ),
        WeaponAttachment(
            key = "att_thompson_50_drum",
            name = "Thompson 50-Rd Type-L Steel Drum",
            shortName = "50-RD L-DRUM",
            slot = AttachmentSlot.MAGAZINE,
            era = WeaponEra.WWII,
            erasAllowed = setOf(WeaponEra.WWII),
            requiredTags = setOf(MountTag.THOMPSON_PLATFORM),
            rarityLabel = "RARE // TOMMY DRUM",
            rarityColorHex = 0xFF34D399,
            summary = "50-Rd Windup Drum (+20 Rounds)",
            historicalNote = "Clockwork-spring 50-round .45 ACP rotary drum turning any M1928A1 into an iconic room broom.",
            magSizeDelta = 20,
            reloadMultiplier = 1.15f,
            visualCode = "drum_mag"
        ),
        WeaponAttachment(
            key = "att_ppsh_71_drum",
            name = "PPSh-41 71-Round Steel Drum",
            shortName = "71-RD DRUM",
            slot = AttachmentSlot.MAGAZINE,
            era = WeaponEra.WWII,
            erasAllowed = setOf(WeaponEra.WWII),
            requiredTags = setOf(MountTag.PPSH_PLATFORM),
            rarityLabel = "RARE // RED ARMY DRUM",
            rarityColorHex = 0xFF34D399,
            summary = "71-Round Rotary Drum (+36 Rounds)",
            historicalNote = "Classic 71-round heavy spiral drum letting Red Army submachine gunners fire nonstop.",
            magSizeDelta = 36,
            reloadMultiplier = 1.20f,
            visualCode = "drum_mag"
        ),
        WeaponAttachment(
            key = "att_thompson_vert_grip",
            name = "Sculpted Walnut Vertical Foregrip",
            shortName = "TOMMY VFG",
            slot = AttachmentSlot.UNDERBARREL,
            era = WeaponEra.WWII,
            erasAllowed = setOf(WeaponEra.WWII),
            requiredTags = setOf(MountTag.THOMPSON_PLATFORM, MountTag.VINTAGE_WWII_SMG),
            rarityLabel = "UNCOMMON // GRIP",
            rarityColorHex = 0xFF34D399,
            summary = "-22% Spread, -8 Move Penalty",
            historicalNote = "Hand-shaped vertical wood pistol foregrip bolted directly under the M1928A1 barrel.",
            spreadMultiplier = 0.78f,
            movePenaltyDelta = -8f,
            moveBloomDelta = -0.02f,
            visualCode = "vertical_grip"
        ),
        WeaponAttachment(
            key = "att_wwii_lafette_bipod",
            name = "Folding Steel Squad Machine Bipod",
            shortName = "SQUAD BIPOD",
            slot = AttachmentSlot.UNDERBARREL,
            era = WeaponEra.WWII,
            erasAllowed = setOf(WeaponEra.WWII, WeaponEra.WWI, WeaponEra.COLD_WAR),
            requiredTags = setOf(MountTag.MG_BELT_PLATFORM),
            rarityLabel = "UNCOMMON // SUPPORT",
            rarityColorHex = 0xFF34D399,
            summary = "+55 ADS Range, -38% Stationary Spread",
            historicalNote = "Stamped folding steel bipod locking down sustained automatic fire on BREN, MG 42, M60 & PKM.",
            stillRangeDelta = 55f,
            spreadMultiplier = 0.62f,
            visualCode = "bipod"
        ),
        WeaponAttachment(
            key = "att_m1a1_folding_wire_stock",
            name = "Paratrooper Steel Folding Wire Stock",
            shortName = "PARA STOCK",
            slot = AttachmentSlot.STOCK_GRIP,
            era = WeaponEra.WWII,
            erasAllowed = setOf(WeaponEra.WWII),
            requiredTags = setOf(MountTag.GARAND_CARBINE_PLATFORM, MountTag.VINTAGE_WWII_SMG),
            rarityLabel = "UNCOMMON // AIRBORNE",
            rarityColorHex = 0xFF34D399,
            summary = "-12 Move Penalty, -20% Move Bloom",
            historicalNote = "Lightweight twin-strut folding buttstock giving paratroopers effortless sprint-to-shoulder speed.",
            movePenaltyDelta = -12f,
            moveBloomDelta = -0.03f,
            visualCode = "wire_stock"
        ),
        WeaponAttachment(
            key = "att_match_enbloc_tuners",
            name = "Hand-Stoned National Match Trigger & Gas Tuners",
            shortName = "MATCH TRIGGER",
            slot = AttachmentSlot.BARREL_INTERNAL,
            era = WeaponEra.WWII,
            erasAllowed = setOf(WeaponEra.WWI, WeaponEra.WWII),
            requiredTags = setOf(MountTag.GARAND_CARBINE_PLATFORM, MountTag.MIL_BOLT_RIFLE, MountTag.THREADED_PISTOL),
            rarityLabel = "RARE // MATCH",
            rarityColorHex = 0xFF34D399,
            summary = "+5 Damage, +10% Fire Rate, -20% Spread",
            historicalNote = "Crisp two-stage Camp Perry match sear tune & lapped locking lugs.",
            damageBonus = 5,
            fireDelayMultiplier = 0.90f,
            spreadMultiplier = 0.80f,
            visualCode = "match_trigger"
        ),

        // ============================================================
        // COLD WAR ERA (1950–1985)
        // ============================================================
        WeaponAttachment(
            key = "att_colt_4x20_carry",
            name = "Colt 4x20 Carry-Handle Scope",
            shortName = "COLT 4X20",
            slot = AttachmentSlot.OPTIC,
            era = WeaponEra.COLD_WAR,
            erasAllowed = setOf(WeaponEra.COLD_WAR),
            requiredTags = setOf(MountTag.COLD_WAR_CARRY_HANDLE, MountTag.COLD_WAR_BATTLE_RIFLE),
            rarityLabel = "RARE // VIETNAM OPTIC",
            rarityColorHex = 0xFF38BDF8,
            summary = "+80 Range, +65 ADS Range, -25% Spread",
            historicalNote = "Japanese-glass 4x20 compact telescope bolting cleanly inside the A1 carry handle channel.",
            rangeBonus = 80f,
            stillRangeDelta = 65f,
            fovDelta = -0.05f,
            spreadMultiplier = 0.75f,
            visualCode = "carry_scope"
        ),
        WeaponAttachment(
            key = "att_pso1_dovetail",
            name = "NPZ PSO-1 4x24 Dovetail Illuminated Scope",
            shortName = "PSO-1 4X24",
            slot = AttachmentSlot.OPTIC,
            era = WeaponEra.COLD_WAR,
            erasAllowed = setOf(WeaponEra.COLD_WAR, WeaponEra.MODERN),
            requiredTags = setOf(MountTag.AK_PLATFORM),
            rarityLabel = "RARE // EASTERN OPTIC",
            rarityColorHex = 0xFF38BDF8,
            summary = "+95 Range, +75 ADS Range, +8% Pen, Tight Cone",
            historicalNote = "Legendary Warsaw Pact side-rail 4x marksman glass with chevron rangefinder reticle.",
            rangeBonus = 95f,
            stillRangeDelta = 75f,
            fovDelta = -0.06f,
            spreadMultiplier = 0.72f,
            penetrationBonus = 8,
            visualCode = "pso1_scope"
        ),
        WeaponAttachment(
            key = "att_an_pvs2_starlight",
            name = "AN/PVS-2 Starlight Night Vision Tube",
            shortName = "STARLIGHT PVS-2",
            slot = AttachmentSlot.OPTIC,
            era = WeaponEra.COLD_WAR,
            erasAllowed = setOf(WeaponEra.COLD_WAR),
            requiredTags = setOf(MountTag.COLD_WAR_CARRY_HANDLE, MountTag.COLD_WAR_BATTLE_RIFLE, MountTag.MG_BELT_PLATFORM),
            rarityLabel = "GRAIL // COLD WAR NV",
            rarityColorHex = 0xFF38BDF8,
            summary = "+70 Range, Wide Green FOV, Low-Light Lock",
            historicalNote = "First-gen passive image intensifier cutting across dark Oakridge blackouts with eerie green glow.",
            rangeBonus = 70f,
            stillRangeDelta = 80f,
            fovDelta = 0.04f,
            spreadMultiplier = 0.80f,
            visualCode = "starlight_scope"
        ),
        WeaponAttachment(
            key = "att_singlepoint_oeg",
            name = "Singlepoint OEG Red Dot",
            shortName = "SINGLEPOINT OEG",
            slot = AttachmentSlot.OPTIC,
            era = WeaponEra.COLD_WAR,
            erasAllowed = setOf(WeaponEra.COLD_WAR),
            requiredTags = setOf(MountTag.COLD_WAR_CARRY_HANDLE, MountTag.SUBMACHINE_SMG),
            rarityLabel = "UNCOMMON // 1970 SON TAY",
            rarityColorHex = 0xFF38BDF8,
            summary = "+35 Range, +10% Fast Target Snap, -15% Move Bloom",
            historicalNote = "Pioneering Son Tay Raid occluded-eye tritium dot that started the entire military red-dot revolution.",
            rangeBonus = 35f,
            fireDelayMultiplier = 0.94f,
            moveBloomDelta = -0.025f,
            spreadMultiplier = 0.84f,
            visualCode = "red_dot"
        ),
        WeaponAttachment(
            key = "att_xm177_moderator",
            name = "Colt XM177E2 4.5\" Sound Moderator",
            shortName = "XM MODERATOR",
            slot = AttachmentSlot.MUZZLE,
            era = WeaponEra.COLD_WAR,
            erasAllowed = setOf(WeaponEra.COLD_WAR),
            requiredTags = setOf(MountTag.COLD_WAR_CARRY_HANDLE),
            rarityLabel = "UNCOMMON // SOG MUZZLE",
            rarityColorHex = 0xFF38BDF8,
            summary = "-38% Noise, Lower Muzzle Signature",
            historicalNote = "Flash-and-blast reducer giving shorty CAR-15s the deeper bark of a full 20-inch rifle.",
            noiseMultiplier = 0.62f,
            spreadMultiplier = 0.86f,
            visualCode = "moderator"
        ),
        WeaponAttachment(
            key = "att_sionics_can",
            name = "Sionics Two-Stage Aluminum Can",
            shortName = "SIONICS CAN",
            slot = AttachmentSlot.MUZZLE,
            era = WeaponEra.COLD_WAR,
            erasAllowed = setOf(WeaponEra.COLD_WAR),
            requiredTags = setOf(MountTag.SUBMACHINE_SMG),
            rarityLabel = "RARE // COLD WAR STEALTH",
            rarityColorHex = 0xFF38BDF8,
            summary = "Suppressed (-75% Noise), Foregrip Heat Wrap",
            historicalNote = "Huge Nomex-wrapped spiral-baffle suppressor stabilizing MAC-10, Uzi, and MP5 barrels silently.",
            forcesSuppressed = true,
            noiseMultiplier = 0.25f,
            spreadMultiplier = 0.80f,
            visualCode = "suppressor"
        ),
        WeaponAttachment(
            key = "att_pbs1_ak_can",
            name = "PBS-1 Steel Rubber-Wipe AK Suppressor",
            shortName = "PBS-1 CAN",
            slot = AttachmentSlot.MUZZLE,
            era = WeaponEra.COLD_WAR,
            erasAllowed = setOf(WeaponEra.COLD_WAR, WeaponEra.MODERN),
            requiredTags = setOf(MountTag.AK_PLATFORM),
            rarityLabel = "RARE // SPETSNAZ",
            rarityColorHex = 0xFF38BDF8,
            summary = "Suppressed (-72% Noise), -15% Spread",
            historicalNote = "Heavy Soviet Special Forces suppressor threading cleanly onto 14x1 LH Kalashnikov muzzles.",
            forcesSuppressed = true,
            noiseMultiplier = 0.28f,
            spreadMultiplier = 0.85f,
            visualCode = "suppressor"
        ),
        WeaponAttachment(
            key = "att_ak_slant_brake",
            name = "AKM 14mm Slant Muzzle Compensator",
            shortName = "SLANT BRAKE",
            slot = AttachmentSlot.MUZZLE,
            era = WeaponEra.COLD_WAR,
            erasAllowed = setOf(WeaponEra.COLD_WAR, WeaponEra.MODERN),
            requiredTags = setOf(MountTag.AK_PLATFORM),
            rarityLabel = "COMMON // COMP",
            rarityColorHex = 0xFF38BDF8,
            summary = "-18% Climb & Spread",
            historicalNote = "Iconic angled cut compensator using muzzle blast to hold up-and-right AK recoil dead level.",
            spreadMultiplier = 0.82f,
            moveBloomDelta = -0.02f,
            visualCode = "brake"
        ),
        WeaponAttachment(
            key = "att_bakelite_ak_dong",
            name = "Romanian Laminated 'Dong' Grip Handguard",
            shortName = "AK HANDGUARD GRIP",
            slot = AttachmentSlot.UNDERBARREL,
            era = WeaponEra.COLD_WAR,
            erasAllowed = setOf(WeaponEra.COLD_WAR, WeaponEra.MODERN),
            requiredTags = setOf(MountTag.AK_PLATFORM),
            rarityLabel = "UNCOMMON // GRIP",
            rarityColorHex = 0xFF38BDF8,
            summary = "-22% Auto Spread, -8 Move Penalty",
            historicalNote = "Distinctive sculpted lower handguard giving AK riflemen a rock-solid vertical index point.",
            spreadMultiplier = 0.78f,
            movePenaltyDelta = -8f,
            visualCode = "vertical_grip"
        ),
        WeaponAttachment(
            key = "att_usgi_20_straight_mag",
            name = "USGI 20/30-Rd Dry-Lube Alloy Mags",
            shortName = "USGI ALLOY MAG",
            slot = AttachmentSlot.MAGAZINE,
            era = WeaponEra.COLD_WAR,
            erasAllowed = setOf(WeaponEra.COLD_WAR),
            requiredTags = setOf(MountTag.COLD_WAR_CARRY_HANDLE, MountTag.COLD_WAR_BATTLE_RIFLE),
            rarityLabel = "COMMON // COLD WAR",
            rarityColorHex = 0xFF38BDF8,
            summary = "+10 Rounds, -20% Reload Time",
            historicalNote = "Anti-tilt follower 30-round aluminum STANAG mags phasing out early Vietnam 20-rounders.",
            magSizeDelta = 10,
            reloadMultiplier = 0.80f,
            visualCode = "ext_mag"
        ),
        WeaponAttachment(
            key = "att_car15_n1_stock",
            name = "Colt CAR-15 N1 Two-Position Stock",
            shortName = "N1 CAR STOCK",
            slot = AttachmentSlot.STOCK_GRIP,
            era = WeaponEra.COLD_WAR,
            erasAllowed = setOf(WeaponEra.COLD_WAR),
            requiredTags = setOf(MountTag.COLD_WAR_CARRY_HANDLE, MountTag.SUBMACHINE_SMG, MountTag.AK_PLATFORM),
            rarityLabel = "COMMON // ERGONOMICS",
            rarityColorHex = 0xFF38BDF8,
            summary = "-14 Move Penalty, -15% Move Bloom",
            historicalNote = "Ribbed lightweight sliding buttstock cutting carbine length for tight vehicle and jungle work.",
            movePenaltyDelta = -14f,
            moveBloomDelta = -0.025f,
            visualCode = "tactical_stock"
        ),
        WeaponAttachment(
            key = "att_coldwar_c_cell_light",
            name = "2-Cell Clamp-On Aluminum Barrel Light",
            shortName = "CLAMP LIGHT",
            slot = AttachmentSlot.TACTICAL,
            era = WeaponEra.COLD_WAR,
            erasAllowed = setOf(WeaponEra.COLD_WAR),
            requiredTags = setOf(MountTag.SUBMACHINE_SMG, MountTag.COLD_WAR_CARRY_HANDLE, MountTag.PUMP_AUTO_SHOTGUN),
            rarityLabel = "UNCOMMON // ILLUM",
            rarityColorHex = 0xFF38BDF8,
            summary = "+35 Dark Sight Range, +10% Wider FOV",
            historicalNote = "Classic ring-clamped xenon flashlight used by 1980s SAS & Delta CQB entry teams.",
            rangeBonus = 35f,
            fovDelta = 0.05f,
            visualCode = "flashlight"
        ),

        // ============================================================
        // MODERN ERA (1986+)
        // ============================================================
        WeaponAttachment(
            key = "att_rmr_type2",
            name = "Trijicon RMR Type 2 Reflex Sight",
            shortName = "RMR TYPE 2",
            slot = AttachmentSlot.OPTIC,
            era = WeaponEra.MODERN,
            erasAllowed = setOf(WeaponEra.MODERN),
            requiredTags = setOf(MountTag.MODERN_PISTOL_OPTIC, MountTag.MODERN_SMG_PDW, MountTag.MODERN_RIFLE),
            rarityLabel = "RARE // MICRO DOT",
            rarityColorHex = 0xFF38BDF8,
            summary = "+42 Range, +8% Fire Cadence, -18% Spread",
            historicalNote = "Forged 7075-T6 aluminum miniature pistol & piggyback reflex optic.",
            rangeBonus = 42f,
            stillRangeDelta = 28f,
            fireDelayMultiplier = 0.92f,
            spreadMultiplier = 0.82f,
            visualCode = "micro_dot"
        ),
        WeaponAttachment(
            key = "att_eotech_exps3",
            name = "EOTech EXPS3-0 Holographic Sight",
            shortName = "EXPS3 HOLO",
            slot = AttachmentSlot.OPTIC,
            era = WeaponEra.MODERN,
            erasAllowed = setOf(WeaponEra.MODERN),
            requiredTags = setOf(MountTag.MODERN_SMG_PDW, MountTag.MODERN_RIFLE, MountTag.PUMP_AUTO_SHOTGUN, MountTag.MAG_SHOTGUN),
            rarityLabel = "RARE // CQB HOLO",
            rarityColorHex = 0xFF38BDF8,
            summary = "+55 Range, +50 ADS Reach, -22% Move Bloom",
            historicalNote = "68-MOA speed ring holographic window built for both-eyes-open fast target acquisition.",
            rangeBonus = 55f,
            stillRangeDelta = 50f,
            moveBloomDelta = -0.03f,
            spreadMultiplier = 0.80f,
            visualCode = "holo_sight"
        ),
        WeaponAttachment(
            key = "att_acog_ta31",
            name = "Trijicon ACOG TA31F 4x32 Prism",
            shortName = "ACOG 4X32",
            slot = AttachmentSlot.OPTIC,
            era = WeaponEra.MODERN,
            erasAllowed = setOf(WeaponEra.MODERN),
            requiredTags = setOf(MountTag.MODERN_RIFLE, MountTag.SNIPER_DMR_PRECISION, MountTag.MG_BELT_PLATFORM, MountTag.CROSSBOW_RAIL),
            rarityLabel = "RARE // PRISM 4X",
            rarityColorHex = 0xFF38BDF8,
            summary = "+95 Range, +85 ADS Reach, -28% Spread",
            historicalNote = "Fiber-optic tritium-lit 4x prism battle sight proven indestructible across modern deployments.",
            rangeBonus = 95f,
            stillRangeDelta = 85f,
            fovDelta = -0.05f,
            spreadMultiplier = 0.72f,
            visualCode = "acog"
        ),
        WeaponAttachment(
            key = "att_razor_lpvo_1_6",
            name = "Vortex Razor HD Gen III 1-6x LPVO",
            shortName = "RAZOR 1-6X",
            slot = AttachmentSlot.OPTIC,
            era = WeaponEra.MODERN,
            erasAllowed = setOf(WeaponEra.MODERN),
            requiredTags = setOf(MountTag.MODERN_RIFLE, MountTag.SNIPER_DMR_PRECISION),
            rarityLabel = "GRAIL // LPVO",
            rarityColorHex = 0xFFC084FC,
            summary = "+115 Range, +100 ADS Reach, -34% Spread",
            historicalNote = "Daylight-bright 1-6x low-power variable optic bridging CQB red dots and 600-meter precision.",
            rangeBonus = 115f,
            stillRangeDelta = 100f,
            fovDelta = -0.06f,
            spreadMultiplier = 0.66f,
            visualCode = "lpvo"
        ),
        WeaponAttachment(
            key = "att_nightforce_atacr",
            name = "Nightforce ATACR 5-25x56 F1 Sniper Glass",
            shortName = "ATACR 5-25X",
            slot = AttachmentSlot.OPTIC,
            era = WeaponEra.MODERN,
            erasAllowed = setOf(WeaponEra.MODERN),
            requiredTags = setOf(MountTag.SNIPER_DMR_PRECISION, MountTag.HEAVY_50_BARRETT),
            rarityLabel = "GRAIL // LONG RANGE",
            rarityColorHex = 0xFFC084FC,
            summary = "+165 Range, +150 ADS Reach, +10% Pen, -42% Spread",
            historicalNote = "MIL-SPEC heavy ED glass Mil-XT precision rifle telescope engineered for extreme-range shots.",
            rangeBonus = 165f,
            stillRangeDelta = 150f,
            fovDelta = -0.07f,
            spreadMultiplier = 0.58f,
            penetrationBonus = 10,
            visualCode = "sniper_scope"
        ),
        WeaponAttachment(
            key = "att_surefire_rc2",
            name = "SureFire SOCOM556-RC2 Rifle Suppressor",
            shortName = "SUREFIRE RC2",
            slot = AttachmentSlot.MUZZLE,
            era = WeaponEra.MODERN,
            erasAllowed = setOf(WeaponEra.MODERN),
            requiredTags = setOf(MountTag.MODERN_RIFLE, MountTag.AR_PLATFORM, MountTag.AK_PLATFORM, MountTag.SNIPER_DMR_PRECISION),
            rarityLabel = "GRAIL // SUPPRESSOR",
            rarityColorHex = 0xFFC084FC,
            summary = "Suppressed (-76% Noise), -20% Spread, +5% Pen",
            historicalNote = "Quick-detach Inconel low-backpressure SOCOM suppressor virtually erasing flash and blast.",
            forcesSuppressed = true,
            noiseMultiplier = 0.24f,
            spreadMultiplier = 0.80f,
            penetrationBonus = 5,
            rangeBonus = 24f,
            visualCode = "suppressor"
        ),
        WeaponAttachment(
            key = "att_osprey_pistol_can",
            name = "SilencerCo Osprey .45 / Omega 9K Can",
            shortName = "OSPREY/OMEGA CAN",
            slot = AttachmentSlot.MUZZLE,
            era = WeaponEra.MODERN,
            erasAllowed = setOf(WeaponEra.MODERN),
            requiredTags = setOf(MountTag.THREADED_PISTOL, MountTag.MODERN_SMG_PDW),
            rarityLabel = "RARE // PISTOL CAN",
            rarityColorHex = 0xFF38BDF8,
            summary = "Suppressed (-76% Noise), -15% Spread",
            historicalNote = "Eccentric-baffle low-profile pistol & PDW suppressor clearing standard suppressor-height irons.",
            forcesSuppressed = true,
            noiseMultiplier = 0.24f,
            spreadMultiplier = 0.85f,
            visualCode = "suppressor"
        ),
        WeaponAttachment(
            key = "att_salvo12_shotgun_can",
            name = "SilencerCo Salvo 12 Gauge Suppressor",
            shortName = "SALVO 12 CAN",
            slot = AttachmentSlot.MUZZLE,
            era = WeaponEra.MODERN,
            erasAllowed = setOf(WeaponEra.MODERN),
            requiredTags = setOf(MountTag.PUMP_AUTO_SHOTGUN, MountTag.MAG_SHOTGUN),
            rarityLabel = "GRAIL // 12G STEALTH",
            rarityColorHex = 0xFFC084FC,
            summary = "Suppressed 12G (-72% Noise), -15% Buck Spread",
            historicalNote = "Rod-guided modular titanium shotgun suppressor taming 12-gauge thunder to ear-safe levels.",
            forcesSuppressed = true,
            noiseMultiplier = 0.28f,
            spreadMultiplier = 0.85f,
            visualCode = "suppressor"
        ),
        WeaponAttachment(
            key = "att_full_shotgun_choke",
            name = "Extended Carlson's Tactical Breacher Choke",
            shortName = "TACTICAL CHOKE",
            slot = AttachmentSlot.MUZZLE,
            era = WeaponEra.MODERN,
            erasAllowed = setOf(WeaponEra.MODERN, WeaponEra.COLD_WAR),
            requiredTags = setOf(MountTag.PUMP_AUTO_SHOTGUN, MountTag.MAG_SHOTGUN),
            rarityLabel = "UNCOMMON // 12G",
            rarityColorHex = 0xFF34D399,
            summary = "+35 Range, -28% Buckshot Spread",
            historicalNote = "Toothed stainless ported choke constricting 00 buck patterns into a devastating tight cluster.",
            rangeBonus = 35f,
            spreadMultiplier = 0.72f,
            damageBonus = 2,
            visualCode = "choke"
        ),
        WeaponAttachment(
            key = "att_warcomp_brake",
            name = "SureFire Warcomp 3-Prong Muzzle Brake",
            shortName = "WARCOMP BRAKE",
            slot = AttachmentSlot.MUZZLE,
            era = WeaponEra.MODERN,
            erasAllowed = setOf(WeaponEra.MODERN),
            requiredTags = setOf(MountTag.MODERN_RIFLE, MountTag.MODERN_SMG_PDW, MountTag.SNIPER_DMR_PRECISION, MountTag.HEAVY_50_BARRETT),
            rarityLabel = "UNCOMMON // MUZZLE",
            rarityColorHex = 0xFF34D399,
            summary = "-25% Recoil Spread, Faster Follow-Ups",
            historicalNote = "Gas-vented three-prong flash hider and compensator keeping sights pinned during fast splits.",
            spreadMultiplier = 0.75f,
            fireDelayMultiplier = 0.94f,
            visualCode = "brake"
        ),
        WeaponAttachment(
            key = "att_peq15_laser",
            name = "L3Harris AN/PEQ-15 ATPIAL Laser",
            shortName = "PEQ-15 LASER",
            slot = AttachmentSlot.TACTICAL,
            era = WeaponEra.MODERN,
            erasAllowed = setOf(WeaponEra.MODERN),
            requiredTags = setOf(MountTag.MODERN_RIFLE, MountTag.MODERN_SMG_PDW, MountTag.SNIPER_DMR_PRECISION, MountTag.MAG_SHOTGUN),
            rarityLabel = "RARE // TACTICAL",
            rarityColorHex = 0xFF38BDF8,
            summary = "-35% Hip/Move Spread, -35% Move Bloom",
            historicalNote = "Top-rail aiming laser module turning hip-fire and sprint strafing into laser-beam point shooting.",
            spreadMultiplier = 0.76f,
            moveBloomDelta = -0.045f,
            movePenaltyDelta = -12f,
            visualCode = "laser"
        ),
        WeaponAttachment(
            key = "att_x300u_light",
            name = "SureFire X300U-B Ultra Weaponlight",
            shortName = "X300U 1000LM",
            slot = AttachmentSlot.TACTICAL,
            era = WeaponEra.MODERN,
            erasAllowed = setOf(WeaponEra.MODERN),
            requiredTags = setOf(MountTag.MODERN_PISTOL_OPTIC, MountTag.MODERN_SMG_PDW, MountTag.PUMP_AUTO_SHOTGUN, MountTag.MODERN_RIFLE),
            rarityLabel = "UNCOMMON // LIGHT",
            rarityColorHex = 0xFF34D399,
            summary = "+45 Sight Range, +0.05 Wider Cone",
            historicalNote = "1,000-lumen TIR weaponlight piercing pitch-black Oakridge hallways and alleyways.",
            rangeBonus = 45f,
            fovDelta = 0.05f,
            moveBloomDelta = -0.015f,
            visualCode = "flashlight"
        ),
        WeaponAttachment(
            key = "att_magpul_rvg_grip",
            name = "Magpul RVG / BCM Gunfighter Foregrip",
            shortName = "VERTICAL GRIP",
            slot = AttachmentSlot.UNDERBARREL,
            era = WeaponEra.MODERN,
            erasAllowed = setOf(WeaponEra.MODERN),
            requiredTags = setOf(MountTag.MODERN_RIFLE, MountTag.MODERN_SMG_PDW, MountTag.MAG_SHOTGUN, MountTag.PUMP_AUTO_SHOTGUN),
            rarityLabel = "COMMON // M-LOK",
            rarityColorHex = 0xFF34D399,
            summary = "-20% Spread, -12 Move Penalty",
            historicalNote = "Reinforced polymer M-LOK vertical grip anchoring support-hand recoil pull into the shoulder.",
            spreadMultiplier = 0.80f,
            movePenaltyDelta = -12f,
            moveBloomDelta = -0.02f,
            visualCode = "vertical_grip"
        ),
        WeaponAttachment(
            key = "att_harris_bipod",
            name = "Harris S-BRM 6-9\" Swivel Match Bipod",
            shortName = "HARRIS BIPOD",
            slot = AttachmentSlot.UNDERBARREL,
            era = WeaponEra.MODERN,
            erasAllowed = setOf(WeaponEra.MODERN, WeaponEra.COLD_WAR),
            requiredTags = setOf(MountTag.SNIPER_DMR_PRECISION, MountTag.HEAVY_50_BARRETT, MountTag.MG_BELT_PLATFORM),
            rarityLabel = "RARE // PRECISION",
            rarityColorHex = 0xFF38BDF8,
            summary = "+75 ADS Range, -42% Stationary Spread",
            historicalNote = "Notched-leg spring-deploy sniper bipod locking crosshairs steady for extreme-distance holds.",
            stillRangeDelta = 75f,
            spreadMultiplier = 0.58f,
            visualCode = "bipod"
        ),
        WeaponAttachment(
            key = "att_pmag_40_ext",
            name = "Magpul PMAG 40 / Extended 33-Rd Stick",
            shortName = "EXTENDED MAG",
            slot = AttachmentSlot.MAGAZINE,
            era = WeaponEra.MODERN,
            erasAllowed = setOf(WeaponEra.MODERN),
            requiredTags = setOf(MountTag.MODERN_RIFLE, MountTag.MODERN_SMG_PDW, MountTag.THREADED_PISTOL, MountTag.MAG_SHOTGUN),
            rarityLabel = "UNCOMMON // MAG",
            rarityColorHex = 0xFF34D399,
            summary = "+35% Mag Capacity, -12% Reload Time",
            historicalNote = "Windowed anti-tilt extended polymer magazine feeding sustained firefights without pause.",
            magSizeMultiplier = 1.35f,
            reloadMultiplier = 0.88f,
            visualCode = "ext_mag"
        ),
        WeaponAttachment(
            key = "att_d60_drum",
            name = "Magpul D-60 / Beta-C 60-Rd High-Cap Drum",
            shortName = "60-RD DRUM",
            slot = AttachmentSlot.MAGAZINE,
            era = WeaponEra.MODERN,
            erasAllowed = setOf(WeaponEra.MODERN),
            requiredTags = setOf(MountTag.AR_PLATFORM, MountTag.AK_PLATFORM, MountTag.MODERN_SMG_PDW, MountTag.MAG_SHOTGUN),
            rarityLabel = "GRAIL // DRUM MAG",
            rarityColorHex = 0xFFC084FC,
            summary = "+100% Mag Capacity (2x Rounds)",
            historicalNote = "Ruggedized polymer 60-round drum mag doubling standard magazine capacity with ratcheting load lever.",
            magSizeMultiplier = 2.0f,
            reloadMultiplier = 1.18f,
            visualCode = "drum_mag"
        ),
        WeaponAttachment(
            key = "att_b5_sopmod_stock",
            name = "B5 Systems Enhanced SOPMOD Stock",
            shortName = "SOPMOD STOCK",
            slot = AttachmentSlot.STOCK_GRIP,
            era = WeaponEra.MODERN,
            erasAllowed = setOf(WeaponEra.MODERN),
            requiredTags = setOf(MountTag.MODERN_RIFLE, MountTag.MODERN_SMG_PDW, MountTag.SNIPER_DMR_PRECISION),
            rarityLabel = "UNCOMMON // STOCK",
            rarityColorHex = 0xFF34D399,
            summary = "-18 Move Penalty, +25 ADS Reach, -10% Reload",
            historicalNote = "Sloped cheek-weld Spec-Ops buttstock aligning optics instantly while shaving fatigue.",
            movePenaltyDelta = -18f,
            stillRangeDelta = 25f,
            reloadMultiplier = 0.90f,
            visualCode = "tactical_stock"
        ),
        WeaponAttachment(
            key = "att_geissele_ssae",
            name = "Geissele SSA-E Two-Stage Match Trigger",
            shortName = "SSA-E TRIGGER",
            slot = AttachmentSlot.BARREL_INTERNAL,
            era = WeaponEra.MODERN,
            erasAllowed = setOf(WeaponEra.MODERN),
            requiredTags = setOf(MountTag.MODERN_RIFLE, MountTag.SNIPER_DMR_PRECISION, MountTag.MODERN_PISTOL_OPTIC, MountTag.MODERN_SMG_PDW),
            rarityLabel = "RARE // MATCH TRIGGER",
            rarityColorHex = 0xFF38BDF8,
            summary = "+6 Damage, +14% Fire Rate, -20% Spread",
            historicalNote = "Candy-cane break 3.5-lb precision combat trigger enabling razor-fast controlled double taps.",
            damageBonus = 6,
            fireDelayMultiplier = 0.86f,
            spreadMultiplier = 0.80f,
            penetrationBonus = 5,
            visualCode = "match_trigger"
        )
    )

    private val byKeyMap: Map<String, WeaponAttachment> = all.associateBy { it.key }

    fun fromKey(key: String?): WeaponAttachment? = if (key.isNullOrBlank()) null else byKeyMap[key]

    fun compatibleAttachmentsFor(weapon: WeaponId): List<WeaponAttachment> =
        all.filter { it.isCompatibleWith(weapon) }

    fun compatibleForSlot(weapon: WeaponId, slot: AttachmentSlot): List<WeaponAttachment> =
        all.filter { it.slot == slot && it.isCompatibleWith(weapon) }

    fun computeEffectiveStats(weapon: WeaponId, equippedKeys: Set<String>): EffectiveWeaponStats {
        if (weapon.isMelee) {
            return EffectiveWeaponStats(
                weapon = weapon,
                damage = weapon.damage,
                penetrationPct = weapon.penetrationPct,
                baseRange = weapon.baseRange,
                baseHalfFov = weapon.baseHalfFov,
                moveRangePenalty = weapon.moveRangePenalty,
                moveFovBloom = weapon.moveFovBloom,
                stillRangeBonus = weapon.stillRangeBonus,
                stillFovTighten = weapon.stillFovTighten,
                spreadRad = weapon.spreadRad,
                fireDelayMs = weapon.fireDelayMs,
                magSize = weapon.magSize,
                reloadTimeMs = weapon.reloadTimeMs,
                noiseRadius = weapon.noiseRadius,
                suppressed = weapon.suppressed,
                knockback = weapon.knockback,
                equippedAttachments = emptyList(),
                bayonetReachBonus = 0f
            )
        }
        val active = equippedKeys.mapNotNull { fromKey(it) }
            .filter { it.isCompatibleWith(weapon) }
            .distinctBy { it.slot }

        var dmg = weapon.damage.toFloat()
        var pen = weapon.penetrationPct
        var range = weapon.baseRange
        var fov = weapon.baseHalfFov
        var movePen = weapon.moveRangePenalty
        var moveBloom = weapon.moveFovBloom
        var stillRange = weapon.stillRangeBonus
        var spread = weapon.spreadRad
        var fireDelay = weapon.fireDelayMs.toFloat()
        var magF = weapon.magSize.toFloat()
        var magDelta = 0
        var reloadF = weapon.reloadTimeMs.toFloat()
        var noise = weapon.noiseRadius
        var sup = weapon.suppressed
        var kb = weapon.knockback
        var bayo = 0f

        for (att in active) {
            dmg = dmg * att.damageMultiplier + att.damageBonus
            pen += att.penetrationBonus
            range += att.rangeBonus
            fov += att.fovDelta
            movePen += att.movePenaltyDelta
            moveBloom += att.moveBloomDelta
            stillRange += att.stillRangeDelta
            spread *= att.spreadMultiplier
            fireDelay *= att.fireDelayMultiplier
            magF *= att.magSizeMultiplier
            magDelta += att.magSizeDelta
            reloadF *= att.reloadMultiplier
            noise *= att.noiseMultiplier
            if (att.forcesSuppressed) sup = true
            kb += att.knockbackBonus
            bayo += att.bayoReachBonus
        }

        val finalMag = (magF.roundToInt() + magDelta).coerceAtLeast(1)
        return EffectiveWeaponStats(
            weapon = weapon,
            damage = dmg.roundToInt().coerceAtLeast(1),
            penetrationPct = pen.coerceIn(5, 100),
            baseRange = range.coerceIn(140f, 920f),
            baseHalfFov = fov.coerceIn(0.12f, 0.88f),
            moveRangePenalty = movePen.coerceIn(4f, 150f),
            moveFovBloom = moveBloom.coerceIn(0.02f, 0.28f),
            stillRangeBonus = stillRange.coerceIn(10f, 280f),
            stillFovTighten = weapon.stillFovTighten,
            spreadRad = spread.coerceIn(0.002f, 0.45f),
            fireDelayMs = fireDelay.toLong().coerceAtLeast(42L),
            magSize = finalMag,
            reloadTimeMs = reloadF.toLong().coerceAtLeast(420L),
            noiseRadius = if (sup && noise > 85f) 68f else noise.coerceIn(28f, 540f),
            suppressed = sup,
            knockback = kb.coerceAtLeast(1f),
            equippedAttachments = active,
            bayonetReachBonus = bayo
        )
    }

    fun rollLootAttachmentForEra(era: WeaponEra, rng: kotlin.random.Random): WeaponAttachment {
        val pool = all.filter { it.era == era }
        return if (pool.isNotEmpty()) pool[rng.nextInt(pool.size)] else all[rng.nextInt(all.size)]
    }
}

data class EraMilestoneReward(
    val era: WeaponEra,
    val rewardCred: Int,
    val rewardMats: Map<CraftingMaterial, Int>,
    val plaqueTitle: String
)

object HeritageVaultCatalog {
    fun collectiblesFor(era: WeaponEra): List<WeaponId> = WeaponId.collectiblesForEra(era)

    fun milestoneFor(era: WeaponEra): EraMilestoneReward? = when (era) {
        WeaponEra.FRONTIER -> EraMilestoneReward(
            era = era,
            rewardCred = 250,
            rewardMats = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 2,
                CraftingMaterial.WOOD_FURNITURE to 4,
                CraftingMaterial.BRASS_CASINGS to 6
            ),
            plaqueTitle = "Frontier Pioneer Plaque"
        )
        WeaponEra.WWI -> EraMilestoneReward(
            era = era,
            rewardCred = 450,
            rewardMats = mapOf(
                CraftingMaterial.RIFLED_BARREL to 3,
                CraftingMaterial.FORGED_RECEIVER to 3,
                CraftingMaterial.OPTICAL_GLASS to 2
            ),
            plaqueTitle = "Great War Historian Medal"
        )
        WeaponEra.WWII -> EraMilestoneReward(
            era = era,
            rewardCred = 700,
            rewardMats = mapOf(
                CraftingMaterial.FORGED_RECEIVER to 4,
                CraftingMaterial.SUPPRESSOR_BAFFLES to 2,
                CraftingMaterial.GUNPOWDER to 6
            ),
            plaqueTitle = "Allied & Axis Arsenal Curator"
        )
        WeaponEra.COLD_WAR -> EraMilestoneReward(
            era = era,
            rewardCred = 900,
            rewardMats = mapOf(
                CraftingMaterial.RIFLED_BARREL to 4,
                CraftingMaterial.OPTICAL_GLASS to 3,
                CraftingMaterial.ELECTRONIC_PARTS to 3
            ),
            plaqueTitle = "Iron Curtain Quartermaster"
        )
        WeaponEra.MODERN -> null
    }
}
