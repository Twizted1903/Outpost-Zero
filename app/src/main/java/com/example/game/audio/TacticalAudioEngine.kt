package com.example.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.game.model.CdRecord
import com.example.game.model.WeaponCategory
import com.example.game.model.WeaponId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class TacticalAudioEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var cdJob: Job? = null
    @Volatile
    var muted: Boolean = false

    fun playWeapon(weapon: WeaponId, isSuppressedOverride: Boolean = weapon.suppressed) {
        if (muted) return
        if (weapon.isMelee) {
            playSynth(0.11f) { t, r ->
                val baseF = if (weapon == WeaponId.SLEDGEHAMMER || weapon == WeaponId.FIRE_AXE) 92f else 135f
                val f = baseF * exp(-t * 16f) + 52f
                val env = exp(-t * 24f)
                val whoosh = (r.nextFloat() * 2f - 1f) * 0.34f * exp(-t * 28f)
                val thud = sin(2f * PI.toFloat() * f * t) * 0.55f
                (thud + whoosh) * env * 0.36f
            }
            return
        }
        if (isSuppressedOverride) {
            playSynth(0.08f) { t, r ->
                val f = 310f * exp(-t * 28f) + 75f
                val env = exp(-t * 42f)
                val puff = (r.nextFloat() * 2f - 1f) * 0.25f * exp(-t * 50f)
                val thump = sin(2f * PI.toFloat() * f * t) * 0.45f
                (thump + puff) * env * 0.26f
            }
            return
        }
        when (weapon.category) {
            WeaponCategory.PISTOL -> playSynth(0.095f) { t, r ->
                val f = 240f * exp(-t * 20f) + 52f
                val env = exp(-t * 32f)
                val crack = (r.nextFloat() * 2f - 1f) * 0.40f * exp(-t * 46f)
                (sin(2f * PI.toFloat() * f * t) * 0.6f + crack) * env * 0.32f
            }
            WeaponCategory.SMG -> playSynth(0.08f) { t, r ->
                val f = 275f * exp(-t * 24f) + 58f
                val env = exp(-t * 36f)
                val sq = if (sin(2f * PI.toFloat() * f * t) >= 0f) 0.50f else -0.50f
                val pop = (r.nextFloat() * 2f - 1f) * 0.28f * exp(-t * 50f)
                (sq + pop) * env * 0.26f
            }
            WeaponCategory.SHOTGUN -> playSynth(0.22f) { t, r ->
                val f = 130f * exp(-t * 14f) + 32f
                val env = exp(-t * 14f)
                val boom = sin(2f * PI.toFloat() * f * t)
                val blast = (r.nextFloat() * 2f - 1f) * 0.78f * exp(-t * 18f)
                (boom * 0.7f + blast) * env * 0.46f
            }
            WeaponCategory.DMR_SNIPER -> playSynth(0.18f) { t, r ->
                val f = 175f * exp(-t * 15f) + 38f
                val env = exp(-t * 18f)
                val crack = (r.nextFloat() * 2f - 1f) * 0.65f * exp(-t * 26f)
                (sin(2f * PI.toFloat() * f * t) * 0.7f + crack) * env * 0.42f
            }
            WeaponCategory.RIFLE, WeaponCategory.LMG_SPECIAL, WeaponCategory.MELEE -> playSynth(0.11f) { t, r ->
                val f = 195f * exp(-t * 18f) + 42f
                val env = exp(-t * 28f)
                val tone = sin(2f * PI.toFloat() * f * t)
                val crack = (r.nextFloat() * 2f - 1f) * 0.45f * exp(-t * 42f)
                (tone * 0.65f + crack) * env * 0.34f
            }
        }
    }

    fun playReload() {
        if (muted) return
        playSynth(0.18f) { t, _ ->
            val f = if (t < 0.08f) 520f else 820f
            val localT = if (t < 0.08f) t else t - 0.08f
            sin(2f * PI.toFloat() * f * t) * exp(-localT * 36f) * 0.25f
        }
    }

    fun playDoor(staleCreak: Boolean) {
        if (muted) return
        if (staleCreak) {
            playSynth(0.26f) { t, r ->
                val f = 180f + sin(t * 28f) * 35f
                val dust = (r.nextFloat() * 2f - 1f) * 0.18f * exp(-t * 12f)
                (sin(2f * PI.toFloat() * f * t) * 0.22f + dust) * exp(-t * 6f)
            }
        } else {
            playSynth(0.13f) { t, _ ->
                val f = 145f - t * 280f
                sin(2f * PI.toFloat() * f * t) * exp(-t * 20f) * 0.24f
            }
        }
    }

    fun playStairs() {
        if (muted) return
        playSynth(0.20f) { t, _ ->
            val f = 260f + t * 850f
            sin(2f * PI.toFloat() * f * t) * exp(-t * 11f) * 0.22f
        }
    }

    fun playCarAlarmBurst() {
        if (muted) return
        playSynth(0.45f) { t, _ ->
            val on = (t % 0.25f) < 0.14f
            if (!on) 0f
            else {
                val h1 = sin(2f * PI.toFloat() * 440f * t)
                val h2 = sin(2f * PI.toFloat() * 554f * t)
                (h1 + h2) * 0.16f
            }
        }
    }

    fun playLootPickup() {
        if (muted) return
        playSynth(0.14f) { t, _ ->
            val f = if (t < 0.06f) 640f else 920f
            sin(2f * PI.toFloat() * f * t) * exp(-t * 16f) * 0.2f
        }
    }

    fun playCdPreview(cd: CdRecord) {
        cdJob?.cancel()
        if (muted) return
        cdJob = scope.launch {
            val sampleRate = 22050
            val beatDur = 60f / cd.bpm.toFloat()
            val totalSec = 3.2f
            val count = (sampleRate * totalSec).toInt()
            val buffer = ShortArray(count)
            val baseFreq = 440f * Math.pow(2.0, (cd.genre.baseMidi - 69) / 12.0).toFloat()
            val scale = floatArrayOf(1f, 1.1892f, 1.3348f, 1.4983f, 1.7818f)
            val rng = Random(cd.id * 97)
            val pattern = IntArray(16) { rng.nextInt(scale.size) }

            for (i in 0 until count) {
                if (!isActive) return@launch
                val t = i / sampleRate.toFloat()
                val step = ((t / (beatDur * 0.5f)).toInt()) % pattern.size
                val stepT = t % (beatDur * 0.5f)
                val freq = baseFreq * scale[pattern[step]]
                val bassFreq = baseFreq * 0.5f * scale[pattern[(step / 4) * 4]]
                val lead = sin(2f * PI.toFloat() * freq * t) * exp(-stepT * 7f)
                val bass = sin(2f * PI.toFloat() * bassFreq * t) * 0.45f
                val sample = ((lead * 0.35f + bass * 0.25f) * 0.7f).coerceIn(-1f, 1f)
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcmBuffer(buffer, sampleRate)
        }
    }

    fun stopCdPreview() {
        cdJob?.cancel()
    }

    private fun playSynth(durationSec: Float, generator: (Float, Random) -> Float) {
        scope.launch {
            val sampleRate = 22050
            val count = (sampleRate * durationSec).toInt().coerceAtLeast(256)
            val buffer = ShortArray(count)
            val rng = Random(System.nanoTime())
            for (i in 0 until count) {
                val t = i / sampleRate.toFloat()
                val v = generator(t, rng).coerceIn(-1f, 1f)
                buffer[i] = (v * Short.MAX_VALUE).toInt().toShort()
            }
            playPcmBuffer(buffer, sampleRate)
        }
    }

    private fun playPcmBuffer(buffer: ShortArray, sampleRate: Int) {
        runCatching {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            Thread.sleep((buffer.size * 1000L / sampleRate) + 40L)
            track.stop()
            track.release()
        }
    }
}
