package com.example.game.model

enum class GameMapId {
    OUTPOST_ZERO,
    OAKRIDGE_TOWN
}

enum class StationKind {
    LAPTOP,
    MESS_FOOD,
    WATER_COOLER,
    BUNK,
    INFIRMARY_BED,
    CD_RACK,
    WORKBENCH,
    STORAGE_DEPOT,
    MARKET,
    HELIPAD,
    EXIT_GATE
}

enum class VehicleKind(val label: String) {
    POLICE_CRUISER("DeLaine Cruiser"),
    SEDAN("Abandoned Sedan"),
    DELIVERY_VAN("Delivery Van"),
    AMBULANCE("Cypress Ambulance")
}

enum class AmbientZoneKind {
    WOODS,
    STREET
}

enum class DoorStyle {
    HINGED_WOOD,
    DOUBLE_SLIDING_GLASS,
    STEEL_SECURITY,
    GARAGE_ROLLUP
}

enum class TerrainKind {
    BASIN_WATER,
    LIMESTONE_BANK,
    DITCH,
    SANDY_RIDGE,
    TERRACE_PLAZA,
    OVERPASS_DECK
}

data class RectF(val x: Float, val y: Float, val w: Float, val h: Float)

data class CurvedRoad(
    val points: List<Pair<Float, Float>>,
    val width: Float
)

data class GroundPatch(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val colorHex: Long,
    val label: String? = null
)

data class TerrainZone(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val heightLevel: Int,
    val kind: TerrainKind,
    val colorHex: Long,
    val label: String? = null
)

data class TerrainRamp(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val fromHeight: Int,
    val toHeight: Int,
    val isStairs: Boolean = false
)

data class ElevatedWalkway(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val heightLevel: Int,
    val colorHex: Long,
    val isBoardwalk: Boolean = true
)

data class DistrictZone(
    val name: String,
    val bounds: RectF,
    val dangerTier: Int = 1
)

data class FloorRect(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val floor: Int,
    val colorHex: Long,
    val stripeHex: Long,
    val buildingId: Int
)

data class WallSegment(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val floor: Int,
    val colorHex: Long,
    val topColorHex: Long,
    val isWindow: Boolean = false,
    val isFence: Boolean = false,
    val buildingId: Int = -1
)

data class DoorObj(
    val id: String,
    val hingeX: Float,
    val hingeY: Float,
    val length: Float,
    val thickness: Float = 7f,
    val baseAngle: Float,
    var angle: Float,
    var targetAngle: Float,
    val openOffset: Float,
    var open: Boolean,
    val floor: Int,
    val buildingId: Int,
    var isStale: Boolean = true,
    val isDoubleLeaf: Boolean = false,
    val jammedByPropId: String? = null,
    val doorStyle: DoorStyle = DoorStyle.HINGED_WOOD,
    var slideProgress: Float = if (open) 1f else 0f,
    val autoSensor: Boolean = false
)

data class StairwellObj(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val buildingId: Int,
    val accessibleFloors: List<Int>
)

data class PropObj(
    val id: String,
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val floor: Int,
    val colorHex: Long,
    val heightScale: Float,
    val propType: String,
    val buildingId: Int = -1,
    val blocksDoorId: String? = null,
    val isClearableBox: Boolean = false,
    var cleared: Boolean = false,
    val vehicleKind: VehicleKind? = null,
    var hasAlarm: Boolean = false,
    var alarmActiveUntil: Long = 0L,
    var searched: Boolean = false,
    val lootItems: MutableList<InventoryItem> = mutableListOf(),
    var tvPoweredOn: Boolean = false,
    var tvKilledByFlicker: Boolean = false
)

data class LootContainer(
    val id: String,
    val x: Float,
    val y: Float,
    val floor: Int,
    val label: String,
    var searched: Boolean = false,
    val items: MutableList<InventoryItem> = mutableListOf()
)

data class BuildingInfo(
    val id: Int,
    val name: String,
    val bounds: RectF,
    val roofColorHex: Long,
    var roofAlpha: Float = 1f,
    val accessibleFloors: List<Int>,
    val roofFloorIndex: Int? = null,
    val miniColorHex: Long = 0xFFA8A29E,
    val groundHeight: Int = 0
)

data class StreetLightObj(
    val x: Float,
    val y: Float,
    val isBrokenBulb: Boolean = false
)

data class InteractiveStation(
    val id: String,
    val kind: StationKind,
    val x: Float,
    val y: Float,
    val label: String,
    val colorHex: Long
)

data class NpcSurvivorPlacement(
    val npcId: String,
    val x: Float,
    val y: Float,
    val facingAngle: Float
)

data class ExtractionZone(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val radius: Float = 66f,
    var isOpen: Boolean = false
)

enum class NpcNeedState {
    ROAMING,
    GOING_TO_FOOD,
    GOING_TO_WATER,
    GOING_TO_SLEEP,
    VISITING_INFIRMARY,
    CHECKING_WORKBENCH,
    BROWSING_CDS,
    FENCE_PATROL
}

data class BaseNpcRuntime(
    val operatorId: String,
    var x: Float,
    var y: Float,
    var angle: Float,
    var hunger: Float = 75f,
    var thirst: Float = 75f,
    var energy: Float = 80f,
    var state: NpcNeedState = NpcNeedState.ROAMING,
    var targetX: Float = 0f,
    var targetY: Float = 0f,
    var stateTimer: Float = 2f,
    var stepTimer: Float = 0f,
    var patrolIdx: Int = 0,
    var statusTag: String = ""
)

enum class ZombieVariant {
    WALKER,
    SPRINTER,
    ARMORED
}

data class ZombieEntity(
    val id: Int,
    var x: Float,
    var y: Float,
    val radius: Float = 11f,
    val speed: Float,
    var hp: Float,
    val maxHp: Float,
    var angle: Float,
    val elevation: Int,
    val variant: ZombieVariant = ZombieVariant.WALKER,
    var alertTargetX: Float? = null,
    var alertTargetY: Float? = null,
    var alertTimer: Float = 0f,
    var hitFlashUntilMs: Long = 0L,
    val isFrenzy: Boolean = false,
    val homeDistrictTier: Int = 1
)

data class RivalOperatorEntity(
    val id: String,
    val uid: String,
    val callsign: String,
    var x: Float,
    var y: Float,
    val radius: Float = 12f,
    var angle: Float = 0f,
    val elevation: Int = 0,
    var hp: Float = 100f,
    val maxHp: Float = 100f,
    val weapon: WeaponId = WeaponId.AK47,
    val shirtColorHex: Long = 0xFF7F1D1D,
    val vestColorHex: Long = 0xFF334155,
    val skinColorHex: Long = 0xFFD9A37C,
    var stepTimer: Float = 0f,
    var lastShotMs: Long = 0L,
    var patrolTargetX: Float = x,
    var patrolTargetY: Float = y,
    var hitFlashUntilMs: Long = 0L,
    val scavId: String = id,
    val displayName: String = callsign,
    val campId: String = "",
    val campName: String = "",
    val roleLabel: String = "SCAV",
    val dangerTier: Int = 1,
    var aiState: ScavAiState = ScavAiState.PATROL,
    var alertLevel: Float = 0f,
    val homeX: Float = x,
    val homeY: Float = y,
    var lastKnownThreatX: Float? = null,
    var lastKnownThreatY: Float? = null,
    var coverX: Float? = null,
    var coverY: Float? = null,
    var coverTimer: Float = 0f,
    var investigateTimer: Float = 0f,
    var burstShotsLeft: Int = 0,
    var burstPauseUntilMs: Long = 0L,
    var calloutText: String = "",
    var calloutUntilMs: Long = 0L,
    var inCover: Boolean = false,
    var isVengefulKin: Boolean = false,
    var kinInquiryTargetName: String = "",
    var kinRelationship: String = "",
    var kinDeathDistrict: String = "",
    var kinSawKiller: Boolean = false
)

data class BulletEntity(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    var rangeLeft: Float,
    val damage: Int,
    val elevation: Int,
    val colorHex: Long,
    val fromRival: Boolean = false,
    val shooterCallsign: String = "",
    val weaponUsed: WeaponId? = null
)

data class DamagePopup(
    var x: Float,
    var y: Float,
    val amount: Int,
    var life: Float = 0.62f,
    val maxLife: Float = 0.62f,
    val isCrit: Boolean = false
)

data class ParticleFx(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val maxLife: Float,
    val size: Float,
    val colorHex: Long,
    val isLeaf: Boolean = false,
    val isPaper: Boolean = false,
    var rot: Float = 0f
)

data class AmbientZone(
    val bounds: RectF,
    val kind: AmbientZoneKind
)

data class WorldMapData(
    val mapId: GameMapId,
    val spawnX: Float,
    val spawnY: Float,
    val worldBounds: RectF? = null,
    val roads: List<RectF>,
    val curvedRoads: List<CurvedRoad>,
    val roadMarkings: List<RectF>,
    val sidewalks: List<RectF>,
    val groundPatches: List<GroundPatch>,
    val terrainZones: List<TerrainZone> = emptyList(),
    val terrainRamps: List<TerrainRamp> = emptyList(),
    val elevatedWalkways: List<ElevatedWalkway> = emptyList(),
    val districtZones: List<DistrictZone> = emptyList(),
    val buildings: List<BuildingInfo>,
    val roomFloors: List<FloorRect>,
    val walls: List<WallSegment>,
    val doors: List<DoorObj>,
    val stairwells: List<StairwellObj>,
    val props: List<PropObj>,
    val lootContainers: List<LootContainer>,
    val streetLights: List<StreetLightObj>,
    val stations: List<InteractiveStation>,
    val survivorNpcs: List<NpcSurvivorPlacement> = emptyList(),
    val extractionZones: List<ExtractionZone>,
    val ambientZones: List<AmbientZone>,
    val patrolWaypoints: List<Pair<Float, Float>>,
    val initialZombies: List<ZombieEntity>,
    val scavEncampments: List<ScavEncampment> = emptyList(),
    val initialScavs: List<RivalOperatorEntity> = emptyList()
)
