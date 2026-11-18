package com.example.game.world

import com.example.game.model.BuildingInfo
import com.example.game.model.DistrictZone
import com.example.game.model.DoorObj
import com.example.game.model.DoorStyle
import com.example.game.model.ElevatedWalkway
import com.example.game.model.FloorRect
import com.example.game.model.InventoryItem
import com.example.game.model.LootContainer
import com.example.game.model.PropObj
import com.example.game.model.RectF
import com.example.game.model.StairwellObj
import com.example.game.model.TerrainRamp
import com.example.game.model.TerrainZone
import com.example.game.model.WallSegment
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sin

data class WallFeature(
    val offset: Float,
    val width: Float,
    val isWindow: Boolean = false,
    val isDoubleDoor: Boolean = false,
    val swingDir: Float = 1f,
    val startOpen: Boolean = false,
    val jamSecondLeaf: Boolean = false,
    val doorStyle: DoorStyle = DoorStyle.HINGED_WOOD,
    val autoSensor: Boolean = false
)

class MutableWorldAccum {
    val roads = mutableListOf<RectF>()
    val curvedRoads = mutableListOf<com.example.game.model.CurvedRoad>()
    val roadMarkings = mutableListOf<RectF>()
    val sidewalks = mutableListOf<RectF>()
    val groundPatches = mutableListOf<com.example.game.model.GroundPatch>()
    val terrainZones = mutableListOf<TerrainZone>()
    val terrainRamps = mutableListOf<TerrainRamp>()
    val elevatedWalkways = mutableListOf<ElevatedWalkway>()
    val districtZones = mutableListOf<DistrictZone>()
    val buildings = mutableListOf<BuildingInfo>()
    val roomFloors = mutableListOf<FloorRect>()
    val walls = mutableListOf<WallSegment>()
    val doors = mutableListOf<DoorObj>()
    val stairwells = mutableListOf<StairwellObj>()
    val props = mutableListOf<PropObj>()
    val lootContainers = mutableListOf<LootContainer>()
    val streetLights = mutableListOf<com.example.game.model.StreetLightObj>()
    val stations = mutableListOf<com.example.game.model.InteractiveStation>()
    val extractionZones = mutableListOf<com.example.game.model.ExtractionZone>()
    val ambientZones = mutableListOf<com.example.game.model.AmbientZone>()
    val patrolWaypoints = mutableListOf<Pair<Float, Float>>()
    val zombies = mutableListOf<com.example.game.model.ZombieEntity>()
}

class MultiFloorBlueprint(
    private val accum: MutableWorldAccum,
    private val cx: Float,
    private val cy: Float,
    private val w: Float,
    private val h: Float,
    private val rotDeg: Int,
    name: String,
    private val extColorHex: Long,
    roofColorHex: Long,
    accessibleFloors: List<Int>,
    roofFloorIndex: Int? = null,
    miniColorHex: Long = 0xFFA8A29E,
    groundHeight: Int = 0
) {
    val buildingId: Int = accum.buildings.size
    val worldBounds: RectF = rotateLocalRect(-w / 2f, -h / 2f, w, h)
    private var propSeq = 0
    private var doorSeq = 0
    private var lootSeq = 0

    init {
        accum.buildings.add(
            BuildingInfo(
                id = buildingId,
                name = name,
                bounds = worldBounds,
                roofColorHex = roofColorHex,
                accessibleFloors = accessibleFloors,
                roofFloorIndex = roofFloorIndex,
                miniColorHex = miniColorHex,
                groundHeight = groundHeight
            )
        )
    }

    private fun rotatePoint(lx: Float, ly: Float): Pair<Float, Float> {
        val rad = rotDeg * PI.toFloat() / 180f
        val c = round(cos(rad))
        val s = round(sin(rad))
        return (lx * c - ly * s) to (lx * s + ly * c)
    }

    private fun rotateLocalRect(rx: Float, ry: Float, rw: Float, rh: Float): RectF {
        val (x1, y1) = rotatePoint(rx, ry)
        val (x2, y2) = rotatePoint(rx + rw, ry + rh)
        return RectF(
            x = cx + min(x1, x2),
            y = cy + min(y1, y2),
            w = abs(x2 - x1),
            h = abs(y2 - y1)
        )
    }

    fun addSidewalk(lx: Float, ly: Float, lw: Float, lh: Float) {
        accum.sidewalks.add(rotateLocalRect(lx, ly, lw, lh))
    }

    fun addFloor(floorIdx: Int, lx: Float, ly: Float, lw: Float, lh: Float, colorHex: Long, stripeHex: Long) {
        val r = rotateLocalRect(lx, ly, lw, lh)
        accum.roomFloors.add(FloorRect(r.x, r.y, r.w, r.h, floorIdx, colorHex, stripeHex, buildingId))
    }

    fun addProp(
        floorIdx: Int,
        lx: Float,
        ly: Float,
        lw: Float,
        lh: Float,
        colorHex: Long,
        heightScale: Float,
        propType: String,
        tvPoweredOn: Boolean = false
    ): PropObj {
        val r = rotateLocalRect(lx, ly, lw, lh)
        val p = PropObj(
            id = "b${buildingId}_p${propSeq++}",
            x = r.x,
            y = r.y,
            w = r.w,
            h = r.h,
            floor = floorIdx,
            colorHex = colorHex,
            heightScale = heightScale,
            propType = propType,
            buildingId = buildingId,
            tvPoweredOn = tvPoweredOn
        )
        accum.props.add(p)
        return p
    }

    fun addLootSpot(floorIdx: Int, lx: Float, ly: Float, label: String, items: List<InventoryItem>) {
        val (rx, ry) = rotatePoint(lx, ly)
        accum.lootContainers.add(
            LootContainer(
                id = "b${buildingId}_loot${lootSeq++}",
                x = cx + rx,
                y = cy + ry,
                floor = floorIdx,
                label = label,
                items = items.toMutableList()
            )
        )
    }

    fun addStairwell(lx: Float, ly: Float, lw: Float, lh: Float, accessibleFloors: List<Int>) {
        val r = rotateLocalRect(lx, ly, lw, lh)
        accum.stairwells.add(StairwellObj(r.x, r.y, r.w, r.h, buildingId, accessibleFloors))
    }

    private fun addRawSegment(
        floorIdx: Int,
        lx1: Float,
        ly1: Float,
        lx2: Float,
        ly2: Float,
        colorHex: Long,
        isWindow: Boolean = false,
        isFence: Boolean = false
    ) {
        val (p1x, p1y) = rotatePoint(lx1, ly1)
        val (p2x, p2y) = rotatePoint(lx2, ly2)
        val wx1 = cx + p1x
        val wy1 = cy + p1y
        val wx2 = cx + p2x
        val wy2 = cy + p2y
        val minX = min(wx1, wx2)
        val minY = min(wy1, wy2)
        val thick = if (isFence) 6f else 10f
        val rw = max(thick, abs(wx2 - wx1))
        val rh = max(thick, abs(wy2 - wy1))
        accum.walls.add(
            WallSegment(
                x = minX - (if (rw == thick) thick / 2f else 0f),
                y = minY - (if (rh == thick) thick / 2f else 0f),
                w = rw,
                h = rh,
                floor = floorIdx,
                colorHex = colorHex,
                topColorHex = lightenHex(colorHex, 0.14f),
                isWindow = isWindow,
                isFence = isFence,
                buildingId = buildingId
            )
        )
    }

    fun addWall(
        floorIdx: Int,
        lx1: Float,
        ly1: Float,
        lx2: Float,
        ly2: Float,
        features: List<WallFeature> = emptyList(),
        wallColorHex: Long = extColorHex,
        isFence: Boolean = false
    ) {
        val totalLen = hypot(lx2 - lx1, ly2 - ly1)
        if (totalLen < 1f) return
        val dirX = (lx2 - lx1) / totalLen
        val dirY = (ly2 - ly1) / totalLen
        val sorted = features.sortedBy { it.offset }
        var cursor = 0f

        for (feat in sorted) {
            val centerDist = totalLen * 0.5f + feat.offset
            val gapStart = max(0f, centerDist - feat.width / 2f)
            val gapEnd = min(totalLen, centerDist + feat.width / 2f)

            if (gapStart > cursor) {
                addRawSegment(
                    floorIdx,
                    lx1 + dirX * cursor,
                    ly1 + dirY * cursor,
                    lx1 + dirX * gapStart,
                    ly1 + dirY * gapStart,
                    wallColorHex,
                    isFence = isFence
                )
            }

            val gx1 = lx1 + dirX * gapStart
            val gy1 = ly1 + dirY * gapStart
            val gx2 = lx1 + dirX * gapEnd
            val gy2 = ly1 + dirY * gapEnd

            if (feat.isWindow) {
                addRawSegment(floorIdx, gx1, gy1, gx2, gy2, wallColorHex, isWindow = true)
            } else if (feat.doorStyle == DoorStyle.GARAGE_ROLLUP) {
                val p1 = rotatePoint(gx1, gy1)
                val p2 = rotatePoint(gx2, gy2)
                val baseAngle = atan2(p2.second - p1.second, p2.first - p1.first)
                accum.doors.add(
                    DoorObj(
                        id = "b${buildingId}_d${doorSeq++}",
                        hingeX = cx + p1.first,
                        hingeY = cy + p1.second,
                        length = gapEnd - gapStart,
                        thickness = 9f,
                        baseAngle = baseAngle,
                        angle = baseAngle,
                        targetAngle = baseAngle,
                        openOffset = 0f,
                        open = feat.startOpen,
                        floor = floorIdx,
                        buildingId = buildingId,
                        isStale = !feat.startOpen,
                        doorStyle = DoorStyle.GARAGE_ROLLUP,
                        slideProgress = if (feat.startOpen) 1f else 0f
                    )
                )
            } else if (feat.isDoubleDoor || feat.doorStyle == DoorStyle.DOUBLE_SLIDING_GLASS) {
                val midLx = (gx1 + gx2) * 0.5f
                val midLy = (gy1 + gy2) * 0.5f
                val leafLen = (gapEnd - gapStart) * 0.5f
                val isSlidingGlass = feat.doorStyle == DoorStyle.DOUBLE_SLIDING_GLASS

                val l1 = rotatePoint(gx1, gy1)
                val lm = rotatePoint(midLx, midLy)
                val baseAngleL = atan2(lm.second - l1.second, lm.first - l1.first)
                val openOffL = if (isSlidingGlass) 0f else feat.swingDir * (PI.toFloat() * 0.5f)
                val doorLId = "b${buildingId}_d${doorSeq++}"
                accum.doors.add(
                    DoorObj(
                        id = doorLId,
                        hingeX = cx + l1.first,
                        hingeY = cy + l1.second,
                        length = leafLen,
                        baseAngle = baseAngleL,
                        angle = if (feat.startOpen) baseAngleL + openOffL else baseAngleL,
                        targetAngle = if (feat.startOpen) baseAngleL + openOffL else baseAngleL,
                        openOffset = openOffL,
                        open = feat.startOpen,
                        floor = floorIdx,
                        buildingId = buildingId,
                        isStale = !feat.startOpen,
                        isDoubleLeaf = true,
                        doorStyle = feat.doorStyle,
                        slideProgress = if (feat.startOpen) 1f else 0f,
                        autoSensor = feat.autoSensor || isSlidingGlass
                    )
                )

                val l2 = rotatePoint(gx2, gy2)
                val baseAngleR = atan2(lm.second - l2.second, lm.first - l2.first)
                val openOffR = if (isSlidingGlass) 0f else -feat.swingDir * (PI.toFloat() * 0.5f)
                val doorRId = "b${buildingId}_d${doorSeq++}"
                var jamPropId: String? = null
                if (feat.jamSecondLeaf) {
                    val normX = -dirY * feat.swingDir
                    val normY = dirX * feat.swingDir
                    val crateLocalX = (midLx + gx2) * 0.5f + normX * 24f - 14f
                    val crateLocalY = (midLy + gy2) * 0.5f + normY * 24f - 14f
                    val crateRect = rotateLocalRect(crateLocalX, crateLocalY, 28f, 28f)
                    jamPropId = "b${buildingId}_jam${propSeq++}"
                    accum.props.add(
                        PropObj(
                            id = jamPropId,
                            x = crateRect.x,
                            y = crateRect.y,
                            w = crateRect.w,
                            h = crateRect.h,
                            floor = floorIdx,
                            colorHex = 0xFF7C5532,
                            heightScale = 0.55f,
                            propType = "fallen_crate",
                            buildingId = buildingId,
                            blocksDoorId = doorRId,
                            isClearableBox = true
                        )
                    )
                }

                accum.doors.add(
                    DoorObj(
                        id = doorRId,
                        hingeX = cx + l2.first,
                        hingeY = cy + l2.second,
                        length = leafLen,
                        baseAngle = baseAngleR,
                        angle = baseAngleR,
                        targetAngle = baseAngleR,
                        openOffset = openOffR,
                        open = false,
                        floor = floorIdx,
                        buildingId = buildingId,
                        isStale = true,
                        isDoubleLeaf = true,
                        jammedByPropId = jamPropId,
                        doorStyle = feat.doorStyle,
                        slideProgress = 0f,
                        autoSensor = feat.autoSensor || isSlidingGlass
                    )
                )
            } else {
                val p1 = rotatePoint(gx1, gy1)
                val p2 = rotatePoint(gx2, gy2)
                val baseAngle = atan2(p2.second - p1.second, p2.first - p1.first)
                val openOff = feat.swingDir * (PI.toFloat() * 0.52f)
                accum.doors.add(
                    DoorObj(
                        id = "b${buildingId}_d${doorSeq++}",
                        hingeX = cx + p1.first,
                        hingeY = cy + p1.second,
                        length = gapEnd - gapStart,
                        baseAngle = baseAngle,
                        angle = if (feat.startOpen) baseAngle + openOff else baseAngle,
                        targetAngle = if (feat.startOpen) baseAngle + openOff else baseAngle,
                        openOffset = openOff,
                        open = feat.startOpen,
                        floor = floorIdx,
                        buildingId = buildingId,
                        isStale = !feat.startOpen,
                        doorStyle = feat.doorStyle
                    )
                )
            }

            cursor = gapEnd
        }

        if (cursor < totalLen) {
            addRawSegment(
                floorIdx,
                lx1 + dirX * cursor,
                ly1 + dirY * cursor,
                lx2,
                ly2,
                wallColorHex,
                isFence = isFence
            )
        }
    }

    companion object {
        fun filterDoorClearance(accum: MutableWorldAccum) {
            val clearRadius = 56f
            val iter = accum.props.iterator()
            while (iter.hasNext()) {
                val p = iter.next()
                if (p.propType == "vehicle" || p.isClearableBox || p.propType == "tree") continue
                var blocked = false
                for (d in accum.doors) {
                    if (d.floor != p.floor) continue
                    val midX = d.hingeX + cos(d.baseAngle) * (d.length * 0.5f)
                    val midY = d.hingeY + sin(d.baseAngle) * (d.length * 0.5f)
                    if (circleRectOverlap(midX, midY, clearRadius, p.x, p.y, p.w, p.h)) {
                        blocked = true
                        break
                    }
                }
                if (!blocked) {
                    for (sw in accum.stairwells) {
                        if (circleRectOverlap(sw.x + sw.w / 2f, sw.y + sw.h / 2f, 48f, p.x, p.y, p.w, p.h)) {
                            blocked = true
                            break
                        }
                    }
                }
                if (blocked) iter.remove()
            }
        }

        fun circleRectOverlap(cx: Float, cy: Float, r: Float, rx: Float, ry: Float, rw: Float, rh: Float): Boolean {
            val closeX = max(rx, min(cx, rx + rw))
            val closeY = max(ry, min(cy, ry + rh))
            return hypot(cx - closeX, cy - closeY) < r
        }

        fun lightenHex(argb: Long, factor: Float): Long {
            val a = (argb ushr 24) and 0xFF
            val r = (((argb ushr 16) and 0xFF) * (1f + factor)).toInt().coerceIn(0, 255)
            val g = (((argb ushr 8) and 0xFF) * (1f + factor)).toInt().coerceIn(0, 255)
            val b = ((argb and 0xFF) * (1f + factor)).toInt().coerceIn(0, 255)
            return (a shl 24) or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()
        }
    }
}
