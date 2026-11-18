package com.example.game.model

object CameraViewBounds {
    const val MIN_CAMERA_ZOOM = 0.72f
    const val MAX_CAMERA_ZOOM = 1.35f
    const val DEFAULT_CAMERA_ZOOM = 1.00f

    const val MIN_VOXEL_DEPTH = 0.85f
    const val MAX_VOXEL_DEPTH = 1.18f
    const val DEFAULT_VOXEL_DEPTH = 1.00f

    fun clampZoom(zoom: Float): Float = zoom.coerceIn(MIN_CAMERA_ZOOM, MAX_CAMERA_ZOOM)

    fun clampVoxelDepth(depth: Float): Float = depth.coerceIn(MIN_VOXEL_DEPTH, MAX_VOXEL_DEPTH)
}

data class GameSettingsState(
    val muteAudio: Boolean = false,
    val autoFireDefault: Boolean = true,
    val showMinimap: Boolean = true,
    val rotateMinimap: Boolean = false,
    val ambientWind: Boolean = true,
    val showTaskTrackers: Boolean = true,
    val highVisibilityNight: Boolean = false,
    val cameraZoom: Float = CameraViewBounds.DEFAULT_CAMERA_ZOOM,
    val voxelDepthScale: Float = CameraViewBounds.DEFAULT_VOXEL_DEPTH
)

data class StoryTimelineEntry(
    val dateLabel: String,
    val phaseTag: String,
    val title: String,
    val summary: String,
    val accentHex: Long
)

object OutbreakTimelineCatalog {
    val currentEra = StoryTimelineEntry(
        dateLabel = "NOV 2026 // WEEK 3 POST-OUTBREAK",
        phaseTag = "SHARED WORLD TIMELINE // 2026",
        title = "Three Weeks After First Outbreak",
        summary = "Everyone deploys into the exact same moment in November 2026 — three weeks after the outbreak swept DeLaine, Florida. Power grids falter across Woodbine Blvd and Stanton Campus, supply lines are gone, and Outpost Zero survivors forge every firearm, optic, and pack from scavenged materials.",
        accentHex = 0xFFE5A024
    )

    val entries: List<StoryTimelineEntry> = listOf(currentEra)
}
