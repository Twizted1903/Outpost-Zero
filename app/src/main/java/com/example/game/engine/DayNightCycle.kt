package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.model.GameMapId
import kotlin.math.abs

enum class TimePhase(val label: String, val isNight: Boolean) {
    DAWN("DAWN", false),
    MORNING("MORNING", false),
    DAY("DAYLIGHT", false),
    GOLDEN_HR("GOLDEN HR", false),
    DUSK("DUSK", true),
    NIGHT("NIGHT", true)
}

data class DayNightAtmosphere(
    val normalizedHours: Float,
    val clockLabel12: String,
    val clockLabel24: String,
    val phase: TimePhase,
    val nightFactor: Float,
    val overlayColor: Color,
    val overlayAlpha: Float,
    val ambientTintColor: Color,
    val ambientTintAlpha: Float,
    val streetLightsActive: Boolean,
    val flashlightStrength: Float
)

object DayNightCycle {
    /**
     * Time progression rate: 5 real-time minutes (300 seconds) = 1 in-game hour.
     * A full 24-hour cycle completes in 120 real minutes.
     */
    const val REAL_SECONDS_PER_GAME_HOUR: Float = 300f

    /**
     * Default starting time on launch: 9:30 AM crisp morning sunlight.
     */
    const val DEFAULT_START_HOUR: Float = 9.5f

    fun normalizeHours(hours: Float): Float {
        if (hours.isNaN() || hours.isInfinite()) return DEFAULT_START_HOUR
        val mod = hours % 24f
        return if (mod < 0f) mod + 24f else mod
    }

    fun phaseForHour(hours: Float): TimePhase {
        val h = normalizeHours(hours)
        return when {
            h >= 5.0f && h < 7.0f -> TimePhase.DAWN
            h >= 7.0f && h < 11.5f -> TimePhase.MORNING
            h >= 11.5f && h < 16.75f -> TimePhase.DAY
            h >= 16.75f && h < 18.75f -> TimePhase.GOLDEN_HR
            h >= 18.75f && h < 20.5f -> TimePhase.DUSK
            else -> TimePhase.NIGHT
        }
    }

    fun formatClock12(hours: Float): String {
        val norm = normalizeHours(hours)
        val totalMinutes = ((norm * 60f).toInt()) % (24 * 60)
        val hour24 = totalMinutes / 60
        val minute = totalMinutes % 60
        val suffix = if (hour24 < 12) "AM" else "PM"
        val hour12 = when (val h = hour24 % 12) {
            0 -> 12
            else -> h
        }
        return "$hour12:${minute.toString().padStart(2, '0')} $suffix"
    }

    fun formatClock24(hours: Float): String {
        val norm = normalizeHours(hours)
        val totalMinutes = ((norm * 60f).toInt()) % (24 * 60)
        val hour24 = totalMinutes / 60
        val minute = totalMinutes % 60
        return "${hour24.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }

    fun computeAtmosphere(
        hours: Float,
        mapId: GameMapId,
        insideBuilding: Boolean,
        inBlackout: Boolean,
        inFlicker: Boolean,
        highVisibilityNight: Boolean
    ): DayNightAtmosphere {
        val h = normalizeHours(hours)
        val phase = phaseForHour(h)

        // Smooth 0f (bright day) -> 1f (full night) factor
        val nightFactor = when {
            h >= 7.25f && h < 17.0f -> 0f
            h >= 17.0f && h < 18.75f -> smoothstep(17.0f, 18.75f, h) * 0.28f
            h >= 18.75f && h < 20.5f -> 0.28f + smoothstep(18.75f, 20.5f, h) * 0.72f
            h >= 20.5f || h < 4.75f -> 1.0f
            h >= 4.75f && h < 6.0f -> 1.0f - smoothstep(4.75f, 6.0f, h) * 0.55f
            else -> 0.45f * (1f - smoothstep(6.0f, 7.25f, h))
        }.coerceIn(0f, 1f)

        val streetLightsActive = h >= 18.25f || h < 6.75f

        val maxNightDarkness = when {
            mapId == GameMapId.OUTPOST_ZERO -> if (highVisibilityNight) 0.14f else 0.28f
            inBlackout -> if (highVisibilityNight) 0.44f else 0.68f
            inFlicker -> if (highVisibilityNight) 0.38f else 0.60f
            else -> if (highVisibilityNight) 0.28f else 0.48f
        }

        val outdoorDarkness = when {
            nightFactor <= 0.001f -> {
                // Bright daytime outdoors; grid failure only casts subtle storm/interior gloom
                if (mapId == GameMapId.OAKRIDGE_TOWN && (inBlackout || inFlicker)) {
                    if (highVisibilityNight) 0.04f else 0.09f
                } else {
                    0f
                }
            }
            else -> nightFactor * maxNightDarkness
        }

        val interiorShade = if (insideBuilding) {
            val baseInterior = when {
                mapId == GameMapId.OUTPOST_ZERO -> 0.08f
                inBlackout -> if (highVisibilityNight) 0.14f else 0.26f
                inFlicker -> if (highVisibilityNight) 0.11f else 0.20f
                else -> if (highVisibilityNight) 0.07f else 0.14f
            }
            baseInterior * (1f - nightFactor * 0.55f)
        } else {
            0f
        }

        val totalOverlayAlpha = (outdoorDarkness + interiorShade).coerceIn(0f, 0.76f)

        // Graphic-novel sun warmth at dawn/sunset and cool indigo wash at dusk/night
        val dawnPeak = tentPeak(h, center = 6.1f, halfWidth = 1.15f)
        val goldenPeak = tentPeak(h, center = 17.85f, halfWidth = 1.25f)
        val warmPeak = maxOf(dawnPeak, goldenPeak)

        val (tintColor, tintAlpha) = when {
            warmPeak > 0.05f -> Color(0xFFF59E0B) to (warmPeak * 0.11f)
            nightFactor > 0.15f -> Color(0xFF1D4ED8) to (nightFactor * 0.06f)
            else -> Color(0xFFFEF3C7) to 0.025f // subtle crisp Florida sun warmth at midday
        }

        val overlayColor = if (nightFactor > 0.25f) Color(0xFF060911) else Color(0xFF0A0D12)
        val flashlightStrength = maxOf(nightFactor, if (insideBuilding) 0.52f else 0f).coerceIn(0f, 1f)

        return DayNightAtmosphere(
            normalizedHours = h,
            clockLabel12 = formatClock12(h),
            clockLabel24 = formatClock24(h),
            phase = phase,
            nightFactor = nightFactor,
            overlayColor = overlayColor,
            overlayAlpha = totalOverlayAlpha,
            ambientTintColor = tintColor,
            ambientTintAlpha = tintAlpha,
            streetLightsActive = streetLightsActive,
            flashlightStrength = flashlightStrength
        )
    }

    private fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    private fun tentPeak(x: Float, center: Float, halfWidth: Float): Float {
        val dist = abs(x - center)
        return if (dist >= halfWidth) 0f else (1f - dist / halfWidth)
    }
}
