package com.example.data.db

import com.example.game.model.BaseCustomLayout
import com.example.game.model.BaseModuleKind
import com.example.game.model.BaseStashState
import com.example.game.model.BaseUpgradeSpecs
import com.example.game.model.CraftingMaterial
import com.example.game.model.DogBreedResolver
import com.example.game.model.GlassesStyle
import com.example.game.model.GlovesStyle
import com.example.game.model.HatStyle
import com.example.game.model.HeadgearStyle
import com.example.game.model.InventoryItem
import com.example.game.model.LootType
import com.example.game.model.OperatorArchetype
import com.example.game.model.OperatorProfile
import com.example.game.model.OperatorSex
import com.example.game.model.PantsStyle
import com.example.game.model.PlacedBasePiece
import com.example.game.model.ShirtStyle
import com.example.game.model.ShoeStyle
import com.example.game.model.SkinToneOption
import com.example.game.model.WeaponId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class OutpostRepository(private val dao: OutpostDao) {

    val operatorsFlow: Flow<List<OperatorProfile>> = dao.observeOperators().map { list ->
        list.map { it.toDomain() }
    }

    val baseStashFlow: Flow<BaseStashState> = dao.observeBaseStash().map { entity ->
        entity?.let {
            val decodedMats = decodeIntMap(it.materialsJson).toMutableMap()
            decodedMats.putIfAbsent(CraftingMaterial.SCRAP_METAL.key, it.scrapMetal)
            decodedMats.putIfAbsent(CraftingMaterial.GUNPOWDER.key, it.gunpowder)
            decodedMats.putIfAbsent(CraftingMaterial.TACTICAL_CLOTH.key, it.tacticalCloth)
            decodedMats.putIfAbsent(CraftingMaterial.ELECTRONIC_PARTS.key, it.electronicParts)
            decodedMats.putIfAbsent(CraftingMaterial.MEDICAL_HERBS.key, it.medicalHerbs)

            BaseStashState(
                credits = it.credits,
                foodStock = it.foodStock,
                waterStock = it.waterStock,
                medsStock = it.medsStock,
                activeOperatorId = it.activeOperatorId,
                scrapMetal = decodedMats[CraftingMaterial.SCRAP_METAL.key] ?: it.scrapMetal,
                gunpowder = decodedMats[CraftingMaterial.GUNPOWDER.key] ?: it.gunpowder,
                tacticalCloth = decodedMats[CraftingMaterial.TACTICAL_CLOTH.key] ?: it.tacticalCloth,
                electronicParts = decodedMats[CraftingMaterial.ELECTRONIC_PARTS.key] ?: it.electronicParts,
                medicalHerbs = decodedMats[CraftingMaterial.MEDICAL_HERBS.key] ?: it.medicalHerbs,
                materialsMap = decodedMats,
                ammoM4 = it.ammoM4,
                ammoSmg = it.ammoSmg,
                ammoShotgun = it.ammoShotgun,
                weaponAmmoMap = decodeIntMap(it.weaponAmmoJson),
                maxPantsLevel = it.maxPantsLevel.coerceIn(1, 4),
                maxArmorLevel = it.maxArmorLevel.coerceIn(0, 4),
                bunkLevel = it.bunkLevel.coerceIn(1, 5),
                storageLevel = it.storageLevel.coerceIn(1, 5),
                helipadLevel = it.helipadLevel.coerceIn(0, 1),
                reloadingGearLevel = it.reloadingGearLevel.coerceIn(0, BaseUpgradeSpecs.MAX_RELOADING_GEAR_LEVEL),
                unlockedWeaponKeys = decodeStringSet(it.unlockedWeaponKeysJson, defaultSet = setOf("knife")),
                activeMissionId = it.activeMissionId.ifBlank { "hq_1" },
                activeMissionProgress = it.activeMissionProgress.coerceAtLeast(0),
                completedMissionIds = decodeStringSet(it.completedMissionsJson, defaultSet = emptySet()),
                activeFavorId = it.activeFavorId,
                activeFavorProgress = it.activeFavorProgress.coerceAtLeast(0),
                completedFavorIds = decodeStringSet(it.completedFavorsJson, defaultSet = emptySet()),
                baseLayout = decodeBaseLayout(it.baseLayoutJson, it.bunkLevel.coerceIn(1, 5), it.helipadLevel.coerceIn(0, 1)),
                ownedAttachmentKeys = decodeStringSet(it.ownedAttachmentsJson, defaultSet = emptySet()),
                weaponAttachmentsMap = decodeStringSetMap(it.weaponAttachmentsJson),
                collectedHistoricKeys = (
                    decodeStringSet(it.collectedHistoricWeaponsJson, defaultSet = emptySet()) +
                        decodeStringSet(it.unlockedWeaponKeysJson, defaultSet = emptySet()).filter { k ->
                            val w = WeaponId.fromKey(k)
                            w.key == k && w.isHistoricalCollectible
                        }
                    ),
                claimedEraMilestones = decodeStringSet(it.claimedEraMilestonesJson, defaultSet = emptySet())
            )
        } ?: BaseStashState()
    }

    val collectedCdIdsFlow: Flow<Set<Int>> = dao.observeCollectedCds().map { list ->
        list.map { it.cdId }.toSet()
    }

    suspend fun ensureInitialized() {
        val stash = dao.getBaseStashOnce()
        if (stash == null) {
            val seed = BaseStashState()
            dao.upsertBaseStash(
                BaseStashEntity(
                    id = 1,
                    credits = seed.credits,
                    foodStock = seed.foodStock,
                    waterStock = seed.waterStock,
                    medsStock = seed.medsStock,
                    activeOperatorId = "",
                    scrapMetal = seed.scrapMetal,
                    gunpowder = seed.gunpowder,
                    tacticalCloth = seed.tacticalCloth,
                    electronicParts = seed.electronicParts,
                    medicalHerbs = seed.medicalHerbs,
                    materialsJson = encodeIntMap(seed.materialsMap),
                    ammoM4 = 0,
                    ammoSmg = 0,
                    ammoShotgun = 0,
                    weaponAmmoJson = "{}",
                    maxPantsLevel = 1,
                    maxArmorLevel = 0,
                    bunkLevel = 1,
                    storageLevel = 1,
                    helipadLevel = 0,
                    reloadingGearLevel = 0,
                    unlockedWeaponKeysJson = encodeStringSet(setOf("knife")),
                    activeMissionId = "hq_1",
                    activeMissionProgress = 0,
                    completedMissionsJson = "[]",
                    activeFavorId = "",
                    activeFavorProgress = 0,
                    completedFavorsJson = "[]",
                    baseLayoutJson = encodeBaseLayout(BaseCustomLayout.default(1, 0)),
                    ownedAttachmentsJson = "[]",
                    weaponAttachmentsJson = "{}",
                    collectedHistoricWeaponsJson = "[]",
                    claimedEraMilestonesJson = "[]"
                )
            )
        }
    }

    suspend fun saveOperator(profile: OperatorProfile) {
        dao.upsertOperator(profile.toEntity())
    }

    suspend fun deleteOperator(id: String) {
        dao.deleteOperator(id)
    }

    suspend fun updateBaseStash(stash: BaseStashState) {
        val syncedMats = stash.materialsMap.toMutableMap().apply {
            put(CraftingMaterial.SCRAP_METAL.key, stash.materialCount(CraftingMaterial.SCRAP_METAL))
            put(CraftingMaterial.GUNPOWDER.key, stash.materialCount(CraftingMaterial.GUNPOWDER))
            put(CraftingMaterial.TACTICAL_CLOTH.key, stash.materialCount(CraftingMaterial.TACTICAL_CLOTH))
            put(CraftingMaterial.ELECTRONIC_PARTS.key, stash.materialCount(CraftingMaterial.ELECTRONIC_PARTS))
            put(CraftingMaterial.MEDICAL_HERBS.key, stash.materialCount(CraftingMaterial.MEDICAL_HERBS))
        }
        val autoHistoric = stash.unlockedWeaponKeys.filter { k ->
            val w = WeaponId.fromKey(k)
            w.key == k && w.isHistoricalCollectible
        }.toSet()
        dao.upsertBaseStash(
            BaseStashEntity(
                id = 1,
                credits = stash.credits,
                foodStock = stash.foodStock,
                waterStock = stash.waterStock,
                medsStock = stash.medsStock,
                activeOperatorId = stash.activeOperatorId,
                scrapMetal = syncedMats[CraftingMaterial.SCRAP_METAL.key] ?: stash.scrapMetal,
                gunpowder = syncedMats[CraftingMaterial.GUNPOWDER.key] ?: stash.gunpowder,
                tacticalCloth = syncedMats[CraftingMaterial.TACTICAL_CLOTH.key] ?: stash.tacticalCloth,
                electronicParts = syncedMats[CraftingMaterial.ELECTRONIC_PARTS.key] ?: stash.electronicParts,
                medicalHerbs = syncedMats[CraftingMaterial.MEDICAL_HERBS.key] ?: stash.medicalHerbs,
                materialsJson = encodeIntMap(syncedMats),
                ammoM4 = stash.ammoM4,
                ammoSmg = stash.ammoSmg,
                ammoShotgun = stash.ammoShotgun,
                weaponAmmoJson = encodeIntMap(stash.weaponAmmoMap),
                maxPantsLevel = stash.maxPantsLevel.coerceIn(1, 4),
                maxArmorLevel = stash.maxArmorLevel.coerceIn(0, 4),
                bunkLevel = stash.bunkLevel.coerceIn(1, 5),
                storageLevel = stash.storageLevel.coerceIn(1, 5),
                helipadLevel = stash.helipadLevel.coerceIn(0, 1),
                reloadingGearLevel = stash.reloadingGearLevel.coerceIn(0, BaseUpgradeSpecs.MAX_RELOADING_GEAR_LEVEL),
                unlockedWeaponKeysJson = encodeStringSet(stash.unlockedWeaponKeys.ifEmpty { setOf("knife") }),
                activeMissionId = stash.activeMissionId,
                activeMissionProgress = stash.activeMissionProgress,
                completedMissionsJson = encodeStringSet(stash.completedMissionIds),
                activeFavorId = stash.activeFavorId,
                activeFavorProgress = stash.activeFavorProgress,
                completedFavorsJson = encodeStringSet(stash.completedFavorIds),
                baseLayoutJson = encodeBaseLayout(stash.baseLayout.ensureInfrastructure(stash.bunkLevel, stash.helipadLevel)),
                ownedAttachmentsJson = encodeStringSet(stash.ownedAttachmentKeys),
                weaponAttachmentsJson = encodeStringSetMap(stash.weaponAttachmentsMap),
                collectedHistoricWeaponsJson = encodeStringSet(stash.collectedHistoricKeys + autoHistoric),
                claimedEraMilestonesJson = encodeStringSet(stash.claimedEraMilestones)
            )
        )
    }

    suspend fun recordCollectedCd(cdId: Int, location: String) {
        dao.insertCollectedCd(
            CollectedCdEntity(
                cdId = cdId,
                foundAt = System.currentTimeMillis(),
                foundLocation = location
            )
        )
    }

    suspend fun removeCollectedCd(cdId: Int) {
        dao.deleteCollectedCd(cdId)
    }

    private fun OperatorEntity.toDomain(): OperatorProfile {
        val arch = runCatching { OperatorArchetype.valueOf(archetypeName) }
            .getOrDefault(OperatorArchetype.BREACHER)
        val hg = runCatching { HeadgearStyle.valueOf(headgearName) }
            .getOrDefault(arch.defaultHeadgear)
        val sex = runCatching { OperatorSex.valueOf(sexName) }.getOrDefault(OperatorSex.MALE)
        val skinTone = runCatching { SkinToneOption.valueOf(skinToneName) }.getOrDefault(SkinToneOption.LIGHT_TAN)
        val shirtStyle = runCatching { ShirtStyle.valueOf(shirtStyleName) }.getOrDefault(ShirtStyle.RED_FLANNEL)
        val pantsStyle = runCatching { PantsStyle.valueOf(pantsStyleName) }.getOrDefault(PantsStyle.BLUE_JEANS)
        val shoeStyle = runCatching { ShoeStyle.valueOf(shoeStyleName) }.getOrDefault(ShoeStyle.WORK_BOOTS)
        val hatStyle = runCatching { HatStyle.valueOf(hatStyleName) }.getOrDefault(HatStyle.BALL_CAP)
        val glassesStyle = runCatching { GlassesStyle.valueOf(glassesStyleName) }.getOrDefault(GlassesStyle.NONE)
        val glovesStyle = runCatching { GlovesStyle.valueOf(glovesStyleName) }.getOrDefault(GlovesStyle.NONE)
        val resolvedDog = DogBreedResolver.resolve(shirtStyle, pantsStyle, hatStyle, glassesStyle, glovesStyle)
        return OperatorProfile(
            id = id,
            slotIndex = slotIndex,
            callsign = callsign,
            fullName = fullName,
            archetype = arch,
            headgear = hg,
            skinHex = skinHex,
            shirtHex = shirtHex,
            vestHex = vestHex,
            packHex = packHex,
            pantsLevel = pantsLevel.coerceIn(1, 4),
            armorLevel = armorLevel.coerceIn(0, 4),
            dogFound = dogFound,
            kiaCount = kiaCount,
            isKia = isKia,
            kiaTimestamp = kiaTimestamp,
            infirmaryUntil = infirmaryUntil,
            safePocketItems = decodeItems(safePocketJson),
            backpackItems = decodeItems(backpackJson),
            downedAtX = downedAtX,
            downedAtY = downedAtY,
            downedZoneId = downedZoneId.ifBlank { "lz_highway" },
            downedElevation = downedElevation,
            stolenItemNames = decodeStringList(stolenItemsJson),
            scavengeChecksDone = scavengeChecksDone,
            isPermadead = isPermadead,
            sex = sex,
            skinTone = skinTone,
            shirtStyle = shirtStyle,
            pantsStyle = pantsStyle,
            shoeStyle = shoeStyle,
            hatStyle = hatStyle,
            glassesStyle = glassesStyle,
            glovesStyle = glovesStyle,
            pairedDogBreed = pairedDogBreed.ifBlank { resolvedDog.breed },
            pairedDogCoatHex = if (pairedDogCoatHex != 0L) pairedDogCoatHex else resolvedDog.coatColorHex
        )
    }

    private fun OperatorProfile.toEntity(): OperatorEntity = OperatorEntity(
        id = id,
        slotIndex = slotIndex,
        callsign = callsign,
        fullName = fullName,
        archetypeName = archetype.name,
        headgearName = headgear.name,
        skinHex = skinHex,
        shirtHex = shirtHex,
        vestHex = vestHex,
        packHex = packHex,
        pantsLevel = pantsLevel.coerceIn(1, 4),
        armorLevel = armorLevel.coerceIn(0, 4),
        dogFound = dogFound,
        kiaCount = kiaCount,
        isKia = isKia,
        kiaTimestamp = kiaTimestamp,
        infirmaryUntil = infirmaryUntil,
        safePocketJson = encodeItems(safePocketItems),
        backpackJson = encodeItems(backpackItems),
        createdAt = System.currentTimeMillis(),
        downedAtX = downedAtX,
        downedAtY = downedAtY,
        downedZoneId = downedZoneId,
        downedElevation = downedElevation,
        stolenItemsJson = encodeStringList(stolenItemNames),
        scavengeChecksDone = scavengeChecksDone,
        isPermadead = isPermadead,
        sexName = sex.name,
        skinToneName = skinTone.name,
        shirtStyleName = shirtStyle.name,
        pantsStyleName = pantsStyle.name,
        shoeStyleName = shoeStyle.name,
        hatStyleName = hatStyle.name,
        glassesStyleName = glassesStyle.name,
        glovesStyleName = glovesStyle.name,
        pairedDogBreed = dogBreed,
        pairedDogCoatHex = dogCoatColorHex
    )

    private fun encodeStringList(items: List<String>): String {
        val arr = JSONArray()
        items.forEach { arr.put(it) }
        return arr.toString()
    }

    private fun decodeStringList(json: String): List<String> {
        if (json.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            List(arr.length()) { i -> arr.optString(i) }.filter { it.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    private fun encodeStringSet(keys: Set<String>): String {
        val arr = JSONArray()
        keys.forEach { arr.put(it) }
        return arr.toString()
    }

    private fun decodeStringSet(json: String, defaultSet: Set<String>): Set<String> {
        if (json.isBlank()) return defaultSet
        return runCatching {
            val arr = JSONArray(json)
            val out = mutableSetOf<String>()
            for (i in 0 until arr.length()) {
                val key = arr.optString(i)
                if (key.isNotBlank()) out.add(key)
            }
            if (out.isEmpty()) defaultSet else out
        }.getOrDefault(defaultSet)
    }

    private fun encodeIntMap(map: Map<String, Int>): String {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        return obj.toString()
    }

    private fun decodeIntMap(json: String): Map<String, Int> {
        if (json.isBlank()) return emptyMap()
        return runCatching {
            val obj = JSONObject(json)
            val out = mutableMapOf<String, Int>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                out[k] = obj.optInt(k, 0).coerceAtLeast(0)
            }
            out
        }.getOrDefault(emptyMap())
    }

    private fun encodeStringSetMap(map: Map<String, Set<String>>): String {
        val obj = JSONObject()
        map.forEach { (k, set) ->
            val arr = JSONArray()
            set.forEach { arr.put(it) }
            obj.put(k, arr)
        }
        return obj.toString()
    }

    private fun decodeStringSetMap(json: String): Map<String, Set<String>> {
        if (json.isBlank()) return emptyMap()
        return runCatching {
            val obj = JSONObject(json)
            val out = mutableMapOf<String, Set<String>>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val arr = obj.optJSONArray(k) ?: continue
                val s = mutableSetOf<String>()
                for (i in 0 until arr.length()) {
                    val item = arr.optString(i)
                    if (item.isNotBlank()) s.add(item)
                }
                if (s.isNotEmpty()) out[k] = s
            }
            out
        }.getOrDefault(emptyMap())
    }

    private fun encodeItems(items: List<InventoryItem>): String {
        val arr = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("uid", item.uid)
            obj.put("type", item.type.name)
            obj.put("name", item.name)
            obj.put("detail", item.detail)
            obj.put("value", item.value)
            obj.put("quantity", item.quantity)
            if (item.cdId != null) obj.put("cdId", item.cdId)
            if (item.weaponKey != null) obj.put("weaponKey", item.weaponKey)
            if (item.pantsLevel != null) obj.put("pantsLevel", item.pantsLevel)
            if (item.armorLevel != null) obj.put("armorLevel", item.armorLevel)
            if (item.materialKey != null) obj.put("materialKey", item.materialKey)
            if (item.attachmentKey != null) obj.put("attachmentKey", item.attachmentKey)
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun decodeItems(json: String): List<InventoryItem> {
        if (json.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                InventoryItem(
                    uid = obj.getString("uid"),
                    type = runCatching { LootType.valueOf(obj.getString("type")) }.getOrDefault(LootType.MATERIAL),
                    name = obj.getString("name"),
                    detail = obj.optString("detail", ""),
                    value = obj.optInt("value", 1),
                    cdId = if (obj.has("cdId")) obj.getInt("cdId") else null,
                    weaponKey = if (obj.has("weaponKey")) obj.getString("weaponKey") else null,
                    pantsLevel = if (obj.has("pantsLevel")) obj.getInt("pantsLevel") else null,
                    armorLevel = if (obj.has("armorLevel")) obj.getInt("armorLevel") else null,
                    materialKey = if (obj.has("materialKey")) obj.getString("materialKey") else null,
                    quantity = obj.optInt("quantity", 1).coerceAtLeast(1),
                    attachmentKey = if (obj.has("attachmentKey")) obj.getString("attachmentKey") else null
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun encodeBaseLayout(layout: BaseCustomLayout): String {
        val arr = JSONArray()
        for (p in layout.pieces) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("kind", p.kind.name)
            obj.put("x", p.x.toDouble())
            obj.put("y", p.y.toDouble())
            obj.put("rot", p.rotationDeg)
            obj.put("slot", p.slotNumber)
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun decodeBaseLayout(json: String, bunkLevel: Int, helipadLevel: Int): BaseCustomLayout {
        if (json.isBlank()) return BaseCustomLayout.default(bunkLevel, helipadLevel)
        return runCatching {
            val arr = JSONArray(json)
            val parsed = (0 until arr.length()).mapNotNull { i ->
                val obj = arr.getJSONObject(i)
                val kind = runCatching { BaseModuleKind.valueOf(obj.getString("kind")) }.getOrNull() ?: return@mapNotNull null
                PlacedBasePiece(
                    id = obj.getString("id"),
                    kind = kind,
                    x = obj.getDouble("x").toFloat(),
                    y = obj.getDouble("y").toFloat(),
                    rotationDeg = obj.optInt("rot", 0),
                    slotNumber = obj.optInt("slot", 0)
                )
            }
            if (parsed.isEmpty()) {
                BaseCustomLayout.default(bunkLevel, helipadLevel)
            } else {
                val hasWatchtower = parsed.any { it.kind == BaseModuleKind.CUSTOM_WATCHTOWER }
                val legacyBarracks = parsed.firstOrNull { it.id == "bldg_barracks" }
                val legacyCantine = parsed.firstOrNull { it.id == "bldg_cantine" }
                val isLegacyGridDefault = !hasWatchtower &&
                    legacyBarracks != null && kotlin.math.abs(legacyBarracks.x + 420f) < 10f && kotlin.math.abs(legacyBarracks.y + 320f) < 10f &&
                    legacyCantine != null && kotlin.math.abs(legacyCantine.x - 400f) < 10f && kotlin.math.abs(legacyCantine.y - 320f) < 10f
                if (isLegacyGridDefault) {
                    BaseCustomLayout.default(bunkLevel, helipadLevel)
                } else {
                    BaseCustomLayout(parsed).ensureInfrastructure(bunkLevel, helipadLevel)
                }
            }
        }.getOrDefault(BaseCustomLayout.default(bunkLevel, helipadLevel))
    }
}
