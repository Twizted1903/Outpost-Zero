package com.example.game.engine

import com.example.game.model.ArmorTier
import com.example.game.model.BuildingInfo
import com.example.game.model.EffectiveWeaponStats
import com.example.game.model.ExtractionZone
import com.example.game.model.GameMapId
import com.example.game.model.InventoryItem
import com.example.game.model.WeaponId

enum class ContextPromptKind {
    NONE,
    DOOR,
    CLEAR_CRATE,
    SEARCH_VEHICLE,
    LOOT_CONTAINER,
    STATION,
    TALK_NPC,
    EXTRACT
}

data class ContextPrompt(
    val kind: ContextPromptKind = ContextPromptKind.NONE,
    val label: String = "",
    val targetId: String = ""
)

data class HudSnapshot(
    val mapId: GameMapId = GameMapId.OUTPOST_ZERO,
    val zoneName: String = "OUTPOST ZERO COMPOUND",
    val floorLabel: String = "1F",
    val hp: Float = 100f,
    val maxHp: Float = 100f,
    val armorHp: Float = 0f,
    val maxArmorHp: Float = 0f,
    val armorTier: ArmorTier = ArmorTier.NONE,
    val hunger: Float = 90f,
    val thirst: Float = 90f,
    val weapon: WeaponId = WeaponId.KNIFE,
    val availableWeapons: List<WeaponId> = listOf(WeaponId.KNIFE),
    val magAmmo: Int = 0,
    val reserveAmmo: Int = 0,
    val reloading: Boolean = false,
    val autoFire: Boolean = true,
    val meleeSwingActive: Boolean = false,
    val focusLabel: String = "ZOOMED",
    val focusPct: Int = 95,
    val kills: Int = 0,
    val playerX: Float = 0f,
    val playerY: Float = 0f,
    val playerAngle: Float = -1.57f,
    val elevation: Int = 0,
    val terrainHeight: Int = 0,
    val raidElapsedSecs: Int = 0,
    val worldTimeHours: Float = DayNightCycle.DEFAULT_START_HOUR,
    val timeClockLabel: String = DayNightCycle.formatClock12(DayNightCycle.DEFAULT_START_HOUR),
    val timePhaseLabel: String = DayNightCycle.phaseForHour(DayNightCycle.DEFAULT_START_HOUR).label,
    val isNightTime: Boolean = false,
    val insideBuilding: Boolean = false,
    val powerGridOutage: Boolean = false,
    val blackoutSecsLeft: Int = 0,
    val contextPrompt: ContextPrompt = ContextPrompt(),
    val openExtractions: List<ExtractionZone> = emptyList(),
    val statusToast: String = "",
    val equippedAttachmentKeys: Set<String> = emptySet(),
    val effectiveStats: EffectiveWeaponStats? = null,
    val frenzyActive: Boolean = false,
    val frenzySecsLeft: Int = 0,
    val activeScavAlertCount: Int = 0,
    val dangerTier: Int = 0,
    val radioCallout: String = ""
)

data class StickInput(
    var active: Boolean = false,
    var pointerId: Long = -1L,
    var centerX: Float = 0f,
    var centerY: Float = 0f,
    var knobX: Float = 0f,
    var knobY: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f
)

data class LootSearchResult(
    val title: String,
    val items: List<InventoryItem>,
    val sourceId: String,
    val isVehicle: Boolean
)
