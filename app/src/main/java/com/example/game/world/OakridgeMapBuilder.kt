package com.example.game.world

import com.example.game.model.GameMapId
import com.example.game.model.RectF
import com.example.game.model.TerrainKind
import com.example.game.model.WorldMapData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object OakridgeMapBuilder {

    fun build(
        runSeed: Long = System.currentTimeMillis(),
        zone: com.example.game.model.DeploymentZone = com.example.game.model.DeploymentZone.OAKRIDGE_HIGHWAY,
        customDropX: Float? = null,
        customDropY: Float? = null
    ): WorldMapData {
        val acc = MutableWorldAccum()
        val rng = Random(runSeed)
        var itemSeq = 0
        fun nextUid(prefix: String) = "${prefix}_${itemSeq++}"

        // 1. DeLaine, FL Metro Road Network & Elevation Tiers
        DeLaineMetroOutskirtsBuilder.buildMetroRoadsAndElevation(acc)

        // 2. Town Core: Downtown DeLaine (Steve's Music, Valen Courthouse, Olympia Theatre, Precinct/Firehouse),
        //    Stanton University Quad, & Commercial Strip (SunCoast Mart, Office Plaza, Clinic)
        DeLaineTownBuilder.buildDowntownDeLaine(acc, ::nextUid)
        DeLaineTownBuilder.buildStantonUniversity(acc, ::nextUid)
        DeLaineTownBuilder.buildCommercialStrip(acc, ::nextUid)

        // 3. Metro Outskirts: Cobalt Springs State Park, St. Jude's Riverfront, Rail Yard, Airfield, Citrus City, DeLuca & Garage Homes
        DeLaineMetroOutskirtsBuilder.buildCobaltSpringsAndRiverfront(acc, ::nextUid)
        DeLaineMetroOutskirtsBuilder.buildWestsideAndGardenNeighborhoods(acc, ::nextUid)
        DeLaineMetroOutskirtsBuilder.buildRailYardAirfieldCitrusAndDeLuca(acc, ::nextUid)
        DeLaineMetroOutskirtsBuilder.populateMetroDetailsAndExits(acc, rng, ::nextUid)

        MultiFloorBlueprint.filterDoorClearance(acc)

        val (dropX, dropY) = resolveSafeDropPoint(
            acc = acc,
            rawX = (customDropX ?: zone.spawnX).coerceIn(-3050f, 3050f),
            rawY = (customDropY ?: zone.spawnY).coerceIn(-3050f, 3050f)
        )

        val hostiles = DeLaineEnemyAndCampBuilder.buildCampsAndHostiles(
            acc = acc,
            rng = rng,
            dropX = dropX,
            dropY = dropY,
            uid = ::nextUid
        )

        return WorldMapData(
            mapId = GameMapId.OAKRIDGE_TOWN,
            spawnX = dropX,
            spawnY = dropY,
            worldBounds = RectF(-3200f, -3200f, 6400f, 6400f),
            roads = acc.roads,
            curvedRoads = acc.curvedRoads,
            roadMarkings = acc.roadMarkings,
            sidewalks = acc.sidewalks,
            groundPatches = acc.groundPatches,
            buildings = acc.buildings,
            roomFloors = acc.roomFloors,
            walls = acc.walls,
            doors = acc.doors,
            stairwells = acc.stairwells,
            props = acc.props,
            lootContainers = acc.lootContainers,
            streetLights = acc.streetLights,
            stations = acc.stations,
            extractionZones = acc.extractionZones,
            ambientZones = acc.ambientZones,
            terrainZones = acc.terrainZones,
            terrainRamps = acc.terrainRamps,
            elevatedWalkways = acc.elevatedWalkways,
            districtZones = acc.districtZones,
            patrolWaypoints = emptyList(),
            initialZombies = hostiles.initialZombies,
            scavEncampments = hostiles.encampments,
            initialScavs = hostiles.initialScavs
        )
    }

    private fun isDropClearOutdoors(acc: MutableWorldAccum, x: Float, y: Float): Boolean {
        if (x !in -3080f..3080f || y !in -3080f..3080f) return false
        for (b in acc.buildings) {
            if (x in (b.bounds.x - 18f)..(b.bounds.x + b.bounds.w + 18f) &&
                y in (b.bounds.y - 18f)..(b.bounds.y + b.bounds.h + 18f)
            ) {
                return false
            }
        }
        val onWalkway = acc.elevatedWalkways.any { x in it.x..(it.x + it.w) && y in it.y..(it.y + it.h) }
        if (!onWalkway) {
            val inDeepWater = acc.terrainZones.any {
                it.kind == TerrainKind.BASIN_WATER &&
                    x in (it.x - 12f)..(it.x + it.w + 12f) &&
                    y in (it.y - 12f)..(it.y + it.h + 12f)
            }
            if (inDeepWater) return false
        }
        return !isPointBlocked(acc, x, y, 16f, 0)
    }

    private fun resolveSafeDropPoint(acc: MutableWorldAccum, rawX: Float, rawY: Float): Pair<Float, Float> {
        if (isDropClearOutdoors(acc, rawX, rawY)) return rawX to rawY
        val radii = listOf(28f, 60f, 96f, 140f, 190f, 250f, 320f, 420f)
        for (rad in radii) {
            for (deg in 0 until 360 step 20) {
                val a = deg * PI.toFloat() / 180f
                val tx = (rawX + cos(a) * rad).coerceIn(-3050f, 3050f)
                val ty = (rawY + sin(a) * rad).coerceIn(-3050f, 3050f)
                if (isDropClearOutdoors(acc, tx, ty)) return tx to ty
            }
        }
        return 320f to -10f
    }

    private fun isPointBlocked(acc: MutableWorldAccum, x: Float, y: Float, r: Float, elev: Int): Boolean {
        for (w in acc.walls) {
            if (w.floor == elev && MultiFloorBlueprint.circleRectOverlap(x, y, r, w.x, w.y, w.w, w.h)) return true
        }
        for (p in acc.props) {
            if (p.floor == elev && MultiFloorBlueprint.circleRectOverlap(x, y, r, p.x, p.y, p.w, p.h)) return true
        }
        return false
    }
}
