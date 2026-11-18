package com.example.game.model

enum class OperatorArchetype(
    val label: String,
    val roleTitle: String,
    val dogBreed: String,
    val dogCoatColorHex: Long,
    val speedMul: Float,
    val maxHp: Int,
    val defaultVestHex: Long,
    val defaultShirtHex: Long,
    val defaultPackHex: Long,
    val defaultHeadgear: HeadgearStyle,
    val bio: String
) {
    BREACHER(
        label = "Woodsmen",
        roleTitle = "Former Carpenter & Trail Guide",
        dogBreed = "German Shepherd",
        dogCoatColorHex = 0xFF6B5335,
        speedMul = 0.98f,
        maxHp = 115,
        defaultVestHex = 0xFF5A3D28,
        defaultShirtHex = 0xFFB91C1C,
        defaultPackHex = 0xFF4F3F27,
        defaultHeadgear = HeadgearStyle.CAP_EARPRO,
        bio = "Resilient backwoods handyman built to haul heavy salvage and clear barricades."
    ),
    RECON(
        label = "Runner",
        roleTitle = "Bicycle Courier",
        dogBreed = "Belgian Malinois",
        dogCoatColorHex = 0xFF9E7041,
        speedMul = 1.08f,
        maxHp = 95,
        defaultVestHex = 0xFF333942,
        defaultShirtHex = 0xFF166534,
        defaultPackHex = 0xFF22262E,
        defaultHeadgear = HeadgearStyle.HOOD_COMMS,
        bio = "Fast urban navigator who knows every Oakridge shortcut and alley."
    ),
    RESPONDER(
        label = "Medic",
        roleTitle = "County Paramedic",
        dogBreed = "Bloodhound",
        dogCoatColorHex = 0xFF7A4427,
        speedMul = 1.01f,
        maxHp = 105,
        defaultVestHex = 0xFF8F3024,
        defaultShirtHex = 0xFFA16207,
        defaultPackHex = 0xFF7C2D12,
        defaultHeadgear = HeadgearStyle.CAP_EARPRO,
        bio = "First-response trauma medic from Oakridge EMS trained in emergency triage."
    ),
    MARSHAL(
        label = "Deputy",
        roleTitle = "Town Patrol Deputy",
        dogBreed = "Black Labrador",
        dogCoatColorHex = 0xFF1F242B,
        speedMul = 1.03f,
        maxHp = 108,
        defaultVestHex = 0xFF1E3252,
        defaultShirtHex = 0xFF1E293B,
        defaultPackHex = 0xFF38342E,
        defaultHeadgear = HeadgearStyle.CAP_EARPRO,
        bio = "Local deputy who stayed behind to guard neighbors and maintain town radios."
    )
}

enum class HeadgearStyle(val label: String) {
    NVG_HELMET("Field Cap"),
    HOOD_COMMS("Hood & Knit"),
    RESPIRATOR("Shop Mask"),
    CAP_EARPRO("Ball Cap")
}

enum class OperatorSex(val label: String, val shoulderScale: Float) {
    MALE("Male", 1.0f),
    FEMALE("Female", 0.91f)
}

enum class SkinToneOption(val label: String, val colorHex: Long) {
    PORCELAIN("Fair", 0xFFF2D0B8),
    LIGHT_TAN("Light Tan", 0xFFD9A982),
    WARM_OLIVE("Olive", 0xFFC8966C),
    BRONZE("Bronze", 0xFFA87048),
    DEEP_BROWN("Deep Brown", 0xFF7A4B2E),
    EBONY("Ebony", 0xFF4D2E1D)
}

enum class ShirtStyle(
    val label: String,
    val primaryHex: Long,
    val patternHex: Long,
    val isPlaid: Boolean,
    val hasHood: Boolean,
    val pairedBreed: String,
    val pairedCoatHex: Long,
    val description: String
) {
    RED_FLANNEL(
        label = "Red Flannel",
        primaryHex = 0xFFB91C1C,
        patternHex = 0xFF1F2937,
        isPlaid = true,
        hasHood = false,
        pairedBreed = "German Shepherd",
        pairedCoatHex = 0xFF7A5A3A,
        description = "Heavy brushed cotton buffalo-plaid flannel button-up. Classic woodsman jacket."
    ),
    FOREST_FLANNEL(
        label = "Forest Flannel",
        primaryHex = 0xFF166534,
        patternHex = 0xFF111827,
        isPlaid = true,
        hasHood = false,
        pairedBreed = "Belgian Malinois",
        pairedCoatHex = 0xFFA47544,
        description = "Pine-green check flannel overshirt favored by ridge trackers and K-9 handlers."
    ),
    OLIVE_HENLEY(
        label = "Olive Henley",
        primaryHex = 0xFF4B5320,
        patternHex = 0xFF343B15,
        isPlaid = false,
        hasHood = false,
        pairedBreed = "Rottweiler",
        pairedCoatHex = 0xFF2B2118,
        description = "Three-button waffle-knit thermal henley built for cold mornings."
    ),
    SLATE_HOODIE(
        label = "Slate Hoodie",
        primaryHex = 0xFF475569,
        patternHex = 0xFF334155,
        isPlaid = false,
        hasHood = true,
        pairedBreed = "Siberian Husky",
        pairedCoatHex = 0xFF94A3B8,
        description = "Fleece-lined zip hoodie with roomy kangaroo pockets and storm hood."
    ),
    DENIM_JACKET(
        label = "Denim Work Jacket",
        primaryHex = 0xFF1E3A8A,
        patternHex = 0xFF60A5FA,
        isPlaid = false,
        hasHood = false,
        pairedBreed = "Australian Shepherd",
        pairedCoatHex = 0xFF785746,
        description = "Raw indigo ranch jacket with copper rivets and shearling collar trim."
    ),
    TAN_WORKSHIRT(
        label = "Tan Duck Workshirt",
        primaryHex = 0xFFA16207,
        patternHex = 0xFF713F12,
        isPlaid = false,
        hasHood = false,
        pairedBreed = "Bloodhound",
        pairedCoatHex = 0xFF7A4427,
        description = "Abrasion-resistant canvas mechanic shirt with twin chest utility pockets."
    ),
    CHARCOAL_TEE(
        label = "Charcoal Field Tee",
        primaryHex = 0xFF27272A,
        patternHex = 0xFF52525B,
        isPlaid = false,
        hasHood = false,
        pairedBreed = "Doberman Pinscher",
        pairedCoatHex = 0xFF261C17,
        description = "Breathable heavyweight cotton crew tee for hot street runs."
    ),
    NAVY_WINDBREAKER(
        label = "Navy Windbreaker",
        primaryHex = 0xFF1E293B,
        patternHex = 0xFF38BDF8,
        isPlaid = false,
        hasHood = true,
        pairedBreed = "Black Labrador",
        pairedCoatHex = 0xFF1B1F24,
        description = "Water-repellent nylon shell jacket with reflective storm piping."
    )
}

enum class PantsStyle(
    val label: String,
    val colorHex: Long,
    val trimHex: Long,
    val description: String
) {
    BLUE_JEANS("Blue Denim Jeans", 0xFF1D4ED8, 0xFF93C5FD, "Broken-in straight-leg work denim with reinforced knees."),
    WORK_CANVAS("Carhartt Duck Pants", 0xFF92400E, 0xFFD97706, "Double-front brown duck canvas utility trousers."),
    OLIVE_FATIGUES("Olive Ripstop Pants", 0xFF3F4E3A, 0xFF65A30D, "Lightweight grid-weave ripstop trousers."),
    KHAKI_CHINOS("Khaki Field Chinos", 0xFFA88E68, 0xFFD6C2A1, "Tailored cotton twill field pants with deep slash pockets."),
    CHARCOAL_CARGO("Charcoal Cargo Pants", 0xFF262A30, 0xFF64748B, "Multi-pocket charcoal utility trousers.")
}

enum class ShoeStyle(
    val label: String,
    val colorHex: Long,
    val soleHex: Long,
    val description: String
) {
    WORK_BOOTS("Leather Work Boots", 0xFF78350F, 0xFF1F2937, "Steel-toe Goodyear-welted leather boots."),
    TRAIL_RUNNERS("Trail Runners", 0xFF334155, 0xFF34D399, "Grippy lug-soled trail shoes built for sprints."),
    HIKING_BOOTS("Alpine Hiking Boots", 0xFF57534E, 0xFFD97706, "Waterproof ankle-support boots for rough terrain."),
    CANVAS_SNEAKERS("Low-Top Sneakers", 0xFF1F2937, 0xFFE2E8F0, "Quiet rubber-soled everyday canvas sneakers.")
}

enum class HatStyle(
    val label: String,
    val colorHex: Long,
    val accentHex: Long,
    val description: String
) {
    NONE("No Hat", 0x00000000, 0x00000000, "Uncovered hair/head for full peripheral awareness."),
    BALL_CAP("Trucker Ball Cap", 0xFF7F1D1D, 0xFFE2E8F0, "Curved-brim canvas cap shielding sun and glare."),
    KNIT_BEANIE("Ribbed Knit Beanie", 0xFF374151, 0xFF9CA3AF, "Warm cuffed wool watch cap."),
    BOONIE_HAT("Canvas Boonie Hat", 0xFF4D5D42, 0xFF78716C, "Wide-brim brush hat with chin cord."),
    RANCHER_HAT("Leather Rancher Hat", 0xFF6D4C2F, 0xFFB45309, "Weathered wide-brim felt & leather outdoor hat."),
    FLAT_CAP("Wool Flat Cap", 0xFF3F3F46, 0xFF94A3B8, "Low-profile tweed driving cap.")
}

enum class GlassesStyle(
    val label: String,
    val frameHex: Long,
    val lensHex: Long,
    val description: String
) {
    NONE("No Glasses", 0x00000000, 0x00000000, "Unobstructed natural vision."),
    AVIATORS("Gold Aviators", 0xFFF59E0B, 0xFF1E293B, "Teardrop tinted pilot sunglasses with metal bridge."),
    WAYFARERS("Dark Wayfarers", 0xFF0F172A, 0xFF1E293B, "Thick acetate-frame polarized shades."),
    WIREFRAMES("Wireframe Glasses", 0xFFCBD5E1, 0x6693C5FD, "Lightweight prescription wire-rim spectacles."),
    SAFETY_GLASSES("Clear Shop Glasses", 0xFF38BDF8, 0x55E2E8F0, "Impact-rated wrap-around workshop eyewear.")
}

enum class GlovesStyle(
    val label: String,
    val colorHex: Long,
    val bareTips: Boolean,
    val description: String
) {
    NONE("Bare Hands", 0x00000000, true, "Direct tactile feel with bare fingers."),
    FINGERLESS("Fingerless Knit", 0xFF374151, true, "Wool half-finger gloves for warm palms and crisp trigger pull."),
    LEATHER_WORK("Tan Leather Work", 0xFFB45309, false, "Full-grain cowhide utility gloves protecting knuckles and palms."),
    MECHANIC("Black Mechanic Gloves", 0xFF18181B, false, "Synthetic suede and rubberized knuckle workshop gloves.")
}

data class DogBreedMatch(
    val breed: String,
    val coatColorHex: Long,
    val pairingReason: String
)

object DogBreedResolver {
    fun resolve(
        shirt: ShirtStyle,
        pants: PantsStyle = PantsStyle.BLUE_JEANS,
        hat: HatStyle = HatStyle.NONE,
        glasses: GlassesStyle = GlassesStyle.NONE,
        gloves: GlovesStyle = GlovesStyle.NONE
    ): DogBreedMatch {
        // Primary driver: Shirt choice switches breed cleanly (e.g. Red Flannel -> German Shepherd, Forest Flannel -> Belgian Malinois)
        if (shirt == ShirtStyle.RED_FLANNEL && hat == HatStyle.RANCHER_HAT && pants == PantsStyle.BLUE_JEANS) {
            return DogBreedMatch(
                breed = "German Shepherd",
                coatColorHex = shirt.pairedCoatHex,
                pairingReason = "Red Flannel + Rancher Denim outfit bonds with a loyal German Shepherd."
            )
        }
        val coatShift = when {
            hat == HatStyle.RANCHER_HAT && shirt == ShirtStyle.DENIM_JACKET ->
                return DogBreedMatch("Blue Heeler", 0xFF546373, "Denim Jacket + Rancher Hat pairs with a nimble Blue Heeler.")
            glasses == GlassesStyle.AVIATORS && shirt == ShirtStyle.CHARCOAL_TEE ->
                return DogBreedMatch("Boxer", 0xFF8C5A34, "Charcoal Tee + Aviators pairs with a watchful Boxer.")
            gloves == GlovesStyle.MECHANIC && shirt == ShirtStyle.TAN_WORKSHIRT ->
                return DogBreedMatch("Rhodesian Ridgeback", 0xFFA66637, "Duck Workshirt + Mechanic Gloves pairs with a Ridgeback.")
            else -> shirt.pairedCoatHex
        }
        return DogBreedMatch(
            breed = shirt.pairedBreed,
            coatColorHex = coatShift,
            pairingReason = "${shirt.label} pairs your survivor with a ${shirt.pairedBreed}."
        )
    }
}

enum class LootType {
    FOOD,
    WATER,
    MEDKIT,
    SALVAGE,
    PANTS,
    ARMOR,
    WEAPON,
    CD,
    MATERIAL,
    ATTACHMENT
}

data class InventoryItem(
    val uid: String,
    val type: LootType,
    val name: String,
    val detail: String = "",
    val value: Int = 1,
    val cdId: Int? = null,
    val weaponKey: String? = null,
    val pantsLevel: Int? = null,
    val armorLevel: Int? = null,
    val materialKey: String? = null,
    val quantity: Int = 1,
    val attachmentKey: String? = null
)

data class OperatorProfile(
    val id: String,
    val slotIndex: Int,
    val callsign: String,
    val fullName: String,
    val archetype: OperatorArchetype,
    val headgear: HeadgearStyle = HeadgearStyle.CAP_EARPRO,
    val skinHex: Long,
    val shirtHex: Long,
    val vestHex: Long,
    val packHex: Long,
    val pantsLevel: Int,
    val armorLevel: Int = 0,
    val dogFound: Boolean = true,
    val kiaCount: Int,
    val isKia: Boolean,
    val kiaTimestamp: Long,
    val infirmaryUntil: Long,
    val safePocketItems: List<InventoryItem>,
    val backpackItems: List<InventoryItem>,
    val downedAtX: Float = 0f,
    val downedAtY: Float = 0f,
    val downedZoneId: String = "lz_highway",
    val downedElevation: Int = 0,
    val stolenItemNames: List<String> = emptyList(),
    val scavengeChecksDone: Int = 0,
    val isPermadead: Boolean = false,
    // Detailed Survivor Customization + Outfit-Paired Dog Breed
    val sex: OperatorSex = OperatorSex.MALE,
    val skinTone: SkinToneOption = SkinToneOption.LIGHT_TAN,
    val shirtStyle: ShirtStyle = ShirtStyle.RED_FLANNEL,
    val pantsStyle: PantsStyle = PantsStyle.BLUE_JEANS,
    val shoeStyle: ShoeStyle = ShoeStyle.WORK_BOOTS,
    val hatStyle: HatStyle = HatStyle.BALL_CAP,
    val glassesStyle: GlassesStyle = GlassesStyle.NONE,
    val glovesStyle: GlovesStyle = GlovesStyle.NONE,
    val pairedDogBreed: String = DogBreedResolver.resolve(ShirtStyle.RED_FLANNEL).breed,
    val pairedDogCoatHex: Long = DogBreedResolver.resolve(ShirtStyle.RED_FLANNEL).coatColorHex
) {
    val safePocketCapacity: Int get() = pantsLevel.coerceIn(1, 4)
    val armorTier: ArmorTier get() = ArmorTier.fromLevel(armorLevel)
    val dogBreed: String get() = pairedDogBreed.ifBlank { DogBreedResolver.resolve(shirtStyle, pantsStyle, hatStyle, glassesStyle, glovesStyle).breed }
    val dogCoatColorHex: Long get() = if (pairedDogCoatHex != 0L) pairedDogCoatHex else shirtStyle.pairedCoatHex
    val maxHp: Int get() = archetype.maxHp
    val speedMul: Float get() = archetype.speedMul

    fun currentBuyBackFee(): Int = kiaCount.coerceAtLeast(1) * 150

    fun remainingPermadeathMs(nowMs: Long = System.currentTimeMillis()): Long {
        if (!isKia || kiaTimestamp <= 0L) return PERMADEATH_WINDOW_MS
        return (PERMADEATH_WINDOW_MS - (nowMs - kiaTimestamp)).coerceAtLeast(0L)
    }

    companion object {
        const val PERMADEATH_WINDOW_MS: Long = 24L * 60L * 60L * 1000L
        const val SCAVENGE_INTERVAL_MS: Long = 4L * 60L * 60L * 1000L
    }
}

data class BaseStashState(
    val credits: Int = 180,
    val foodStock: Int = 3,
    val waterStock: Int = 3,
    val medsStock: Int = 1,
    val activeOperatorId: String = "",
    val scrapMetal: Int = 6,
    val gunpowder: Int = 2,
    val tacticalCloth: Int = 5,
    val electronicParts: Int = 0,
    val medicalHerbs: Int = 3,
    val materialsMap: Map<String, Int> = defaultMaterialsMap(),
    val ammoM4: Int = 0,
    val ammoSmg: Int = 0,
    val ammoShotgun: Int = 0,
    val weaponAmmoMap: Map<String, Int> = emptyMap(),
    val maxPantsLevel: Int = 1,
    val maxArmorLevel: Int = 0,
    val bunkLevel: Int = 1,
    val storageLevel: Int = 1,
    val helipadLevel: Int = 0,
    val reloadingGearLevel: Int = 0,
    val unlockedWeaponKeys: Set<String> = setOf("knife"),
    // HQ Missions vs Survivor Favors trackers
    val activeMissionId: String = "hq_1",
    val activeMissionProgress: Int = 0,
    val completedMissionIds: Set<String> = emptySet(),
    val activeFavorId: String = "",
    val activeFavorProgress: Int = 0,
    val completedFavorIds: Set<String> = emptySet(),
    val baseLayout: BaseCustomLayout = BaseCustomLayout.default(),
    // Gunsmith Attachments & Heritage Vault
    val ownedAttachmentKeys: Set<String> = emptySet(),
    val weaponAttachmentsMap: Map<String, Set<String>> = emptyMap(),
    val collectedHistoricKeys: Set<String> = emptySet(),
    val claimedEraMilestones: Set<String> = emptySet()
) {
    val hasHelipad: Boolean get() = helipadLevel >= 1
    val maxRosterSlots: Int get() = BaseUpgradeSpecs.bunkSlotsForLevel(bunkLevel)
    val storageMaterialCap: Int get() = BaseUpgradeSpecs.storageCapForLevel(storageLevel)
    val backpackSlotCap: Int get() = BaseUpgradeSpecs.backpackSlotsForLevel(storageLevel)

    fun equippedAttachmentsFor(weapon: WeaponId): Set<String> =
        weaponAttachmentsMap[weapon.key].orEmpty()

    fun effectiveStatsFor(weapon: WeaponId): EffectiveWeaponStats =
        WeaponAttachmentCatalog.computeEffectiveStats(weapon, equippedAttachmentsFor(weapon))

    fun withUnlockedAttachments(keys: Set<String>): BaseStashState {
        if (keys.isEmpty()) return this
        val valid = keys.filter { WeaponAttachmentCatalog.fromKey(it) != null }.toSet()
        return if (valid.isEmpty()) this else copy(ownedAttachmentKeys = ownedAttachmentKeys + valid)
    }

    fun withRecordedWeapons(keys: Set<String>): BaseStashState {
        if (keys.isEmpty()) return this
        val historicAdds = keys.mapNotNull { k ->
            val w = WeaponId.fromKey(k)
            if (w.key == k && w.isHistoricalCollectible) w.key else null
        }.toSet()
        return copy(
            unlockedWeaponKeys = unlockedWeaponKeys + keys,
            collectedHistoricKeys = collectedHistoricKeys + historicAdds
        )
    }

    fun withEquippedAttachment(weapon: WeaponId, attachment: WeaponAttachment): BaseStashState {
        if (!attachment.isCompatibleWith(weapon)) return this
        if (attachment.key !in ownedAttachmentKeys) return this
        val currentKeys = equippedAttachmentsFor(weapon)
        val filtered = currentKeys.filter { k ->
            val existing = WeaponAttachmentCatalog.fromKey(k)
            existing != null && existing.slot != attachment.slot
        }.toMutableSet()
        filtered.add(attachment.key)
        val nextMap = weaponAttachmentsMap.toMutableMap().apply {
            put(weapon.key, filtered)
        }
        return copy(weaponAttachmentsMap = nextMap)
    }

    fun withUnequippedSlot(weapon: WeaponId, slot: AttachmentSlot): BaseStashState {
        val currentKeys = equippedAttachmentsFor(weapon)
        if (currentKeys.isEmpty()) return this
        val filtered = currentKeys.filter { k ->
            val existing = WeaponAttachmentCatalog.fromKey(k)
            existing != null && existing.slot != slot
        }.toSet()
        val nextMap = weaponAttachmentsMap.toMutableMap().apply {
            if (filtered.isEmpty()) remove(weapon.key) else put(weapon.key, filtered)
        }
        return copy(weaponAttachmentsMap = nextMap)
    }

    fun materialCount(material: CraftingMaterial): Int {
        return when (material) {
            CraftingMaterial.SCRAP_METAL -> materialsMap[material.key] ?: scrapMetal
            CraftingMaterial.GUNPOWDER -> materialsMap[material.key] ?: gunpowder
            CraftingMaterial.TACTICAL_CLOTH -> materialsMap[material.key] ?: tacticalCloth
            CraftingMaterial.ELECTRONIC_PARTS -> materialsMap[material.key] ?: electronicParts
            CraftingMaterial.MEDICAL_HERBS -> materialsMap[material.key] ?: medicalHerbs
            else -> materialsMap[material.key] ?: 0
        }
    }

    fun withMaterialDelta(material: CraftingMaterial, delta: Int, clampToStorageCap: Boolean = true): BaseStashState {
        val cap = if (clampToStorageCap) storageMaterialCap else 100_000
        val currentVal = materialCount(material)
        val nextVal = (currentVal + delta).coerceIn(0, cap)
        val nextMap = materialsMap.toMutableMap().apply { put(material.key, nextVal) }
        return when (material) {
            CraftingMaterial.SCRAP_METAL -> copy(scrapMetal = nextVal, materialsMap = nextMap)
            CraftingMaterial.GUNPOWDER -> copy(gunpowder = nextVal, materialsMap = nextMap)
            CraftingMaterial.TACTICAL_CLOTH -> copy(tacticalCloth = nextVal, materialsMap = nextMap)
            CraftingMaterial.ELECTRONIC_PARTS -> copy(electronicParts = nextVal, materialsMap = nextMap)
            CraftingMaterial.MEDICAL_HERBS -> copy(medicalHerbs = nextVal, materialsMap = nextMap)
            else -> copy(materialsMap = nextMap)
        }
    }

    fun ammoForWeapon(weapon: WeaponId): Int {
        if (weapon.isMelee) return 0
        val mapVal = weaponAmmoMap[weapon.key]
        if (mapVal != null) return mapVal
        return when (weapon) {
            WeaponId.M4 -> ammoM4
            WeaponId.SMG -> ammoSmg
            WeaponId.SHOTGUN -> ammoShotgun
            else -> 0
        }
    }

    fun withWeaponAmmoSet(weapon: WeaponId, total: Int): BaseStashState {
        if (weapon.isMelee) return this
        val clamped = total.coerceIn(0, 10_000)
        val nextMap = weaponAmmoMap.toMutableMap().apply { put(weapon.key, clamped) }
        return when (weapon) {
            WeaponId.M4 -> copy(ammoM4 = clamped, weaponAmmoMap = nextMap)
            WeaponId.SMG -> copy(ammoSmg = clamped, weaponAmmoMap = nextMap)
            WeaponId.SHOTGUN -> copy(ammoShotgun = clamped, weaponAmmoMap = nextMap)
            else -> copy(weaponAmmoMap = nextMap)
        }
    }

    fun withWeaponAmmoDelta(weapon: WeaponId, delta: Int): BaseStashState =
        withWeaponAmmoSet(weapon, ammoForWeapon(weapon) + delta)

    fun canAfford(recipe: CraftingRecipe): Boolean =
        recipe.costs.all { (mat, amt) -> materialCount(mat) >= amt }

    fun canAffordMats(costs: Map<CraftingMaterial, Int>): Boolean =
        costs.all { (mat, amt) -> materialCount(mat) >= amt }

    companion object {
        fun defaultMaterialsMap(): Map<String, Int> = mapOf(
            CraftingMaterial.SCRAP_METAL.key to 6,
            CraftingMaterial.GUNPOWDER.key to 2,
            CraftingMaterial.TACTICAL_CLOTH.key to 5,
            CraftingMaterial.ELECTRONIC_PARTS.key to 0,
            CraftingMaterial.MEDICAL_HERBS.key to 3,
            CraftingMaterial.WOOD_FURNITURE.key to 3,
            CraftingMaterial.INDUSTRIAL_FASTENERS.key to 4,
            CraftingMaterial.BRASS_CASINGS.key to 4,
            CraftingMaterial.PERCUSSION_PRIMERS.key to 2,
            CraftingMaterial.LEAD_PROJECTILES.key to 2,
            CraftingMaterial.TRAUMA_GAUZE.key to 3,
            CraftingMaterial.ANTISEPTIC_SOL.key to 2,
            CraftingMaterial.GUN_LUBE.key to 2
        )
    }
}
