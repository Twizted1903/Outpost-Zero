package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.engine.ContextPromptKind
import com.example.game.engine.StickInput
import com.example.game.engine.WorldCanvasRenderer.renderWorld
import com.example.game.model.GameMapId
import com.example.game.model.HqMissionCatalog
import com.example.game.model.StationKind
import com.example.game.model.SurvivorCatalog
import com.example.game.model.TerrainKind
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSignal
import com.example.ui.theme.BoneWhite
import com.example.ui.theme.BunkerObsidian
import com.example.ui.theme.BunkerPanel
import com.example.ui.theme.BunkerSurface
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanFloor
import com.example.ui.theme.EmeraldHud
import com.example.ui.theme.OliveBorder
import com.example.ui.theme.PurpleRare
import com.example.ui.theme.pixelBevelFrame
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.hypot

@Composable
fun OutpostScreen(viewModel: GameViewModel) {
    val showSplash by viewModel.showSplash.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val hud by viewModel.hud.collectAsStateWithLifecycle()
    val roster by viewModel.roster.collectAsStateWithLifecycle()
    val stash by viewModel.stash.collectAsStateWithLifecycle()
    val collectedCds by viewModel.collectedCds.collectAsStateWithLifecycle()
    val activeModal by viewModel.activeModal.collectAsStateWithLifecycle()
    val activeLoot by viewModel.activeLoot.collectAsStateWithLifecycle()
    val extractionSummary by viewModel.extractionSummary.collectAsStateWithLifecycle()
    val rescueMission by viewModel.rescueMission.collectAsStateWithLifecycle()
    val permadeathReport by viewModel.permadeathReport.collectAsStateWithLifecycle()
    val playingCdId by viewModel.playingCdId.collectAsStateWithLifecycle()
    val activeListingsState by viewModel.activeListingsState.collectAsStateWithLifecycle()
    val myListingsState by viewModel.myListingsState.collectAsStateWithLifecycle()
    val marketBusy by viewModel.marketBusy.collectAsStateWithLifecycle()
    val activeNpcId by viewModel.activeNpcId.collectAsStateWithLifecycle()
    val selectedZone by viewModel.selectedZone.collectAsStateWithLifecycle()
    val buildModeActive by viewModel.buildModeActive.collectAsStateWithLifecycle()
    val selectedBuildPieceId by viewModel.selectedBuildPieceId.collectAsStateWithLifecycle()
    val pendingBuildKind by viewModel.pendingBuildKind.collectAsStateWithLifecycle()
    val pendingBuildRot by viewModel.pendingBuildRot.collectAsStateWithLifecycle()
    val laptopInitialTab by viewModel.laptopInitialTab.collectAsStateWithLifecycle()
    val liveKillFeed by viewModel.liveKillFeed.collectAsStateWithLifecycle()

    if (showSplash) {
        val downedCount = roster.count { it.isKia && !it.isPermadead }
        SplashScreen(
            credits = stash.credits,
            rosterCount = roster.size,
            bunkMax = stash.maxRosterSlots,
            downedCount = downedCount,
            onEnterOutpost = { viewModel.enterGameFromSplash() },
            onOpenSettings = { viewModel.openModal(ActiveModal.SETTINGS) }
        )
        if (activeModal == ActiveModal.SETTINGS) {
            BackHandler { viewModel.closeModal() }
            SettingsModal(
                settings = settings,
                showReturnToSplash = false,
                onUpdateSettings = { viewModel.updateSettings(it) },
                onReturnToSplash = {},
                onClose = { viewModel.closeModal() }
            )
        }
        return
    }

    val dismissibleModal = activeModal !in setOf(
        ActiveModal.NONE,
        ActiveModal.CHARACTER_CREATOR,
        ActiveModal.EXTRACTION_SUMMARY,
        ActiveModal.OPERATOR_DOWN,
        ActiveModal.RESCUE_ANIMATION,
        ActiveModal.PERMADEATH_REPORT
    )
    BackHandler(enabled = dismissibleModal || buildModeActive) {
        if (dismissibleModal) {
            viewModel.closeModal()
        } else if (buildModeActive) {
            viewModel.toggleBaseBuildMode(false)
        }
    }

    var frameTick by remember { mutableLongStateOf(0L) }
    var miniLarge by remember { mutableStateOf(false) }

    // Track held keyboard movement keys for Chromebook / desktop play
    val heldKeys = remember { mutableSetOf<Key>() }
    fun syncKeyAxes() {
        val mx = (if (Key.D in heldKeys || Key.DirectionRight in heldKeys) 1f else 0f) -
            (if (Key.A in heldKeys || Key.DirectionLeft in heldKeys) 1f else 0f)
        val my = (if (Key.S in heldKeys || Key.DirectionDown in heldKeys) 1f else 0f) -
            (if (Key.W in heldKeys || Key.DirectionUp in heldKeys) 1f else 0f)
        viewModel.simulation.keyMoveX = mx
        viewModel.simulation.keyMoveY = my
    }

    LaunchedEffect(Unit) {
        var lastNs = 0L
        while (isActive) {
            withFrameNanos { ns ->
                val dt = if (lastNs == 0L) 0.016f else ((ns - lastNs) / 1_000_000_000f)
                lastNs = ns
                viewModel.tickFrame(dt)
                frameTick++
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BunkerObsidian)
            .focusable()
            .onPreviewKeyEvent { ev ->
                if (activeModal != ActiveModal.NONE) return@onPreviewKeyEvent false
                if (ev.type == KeyEventType.KeyDown) {
                    heldKeys.add(ev.key)
                    syncKeyAxes()
                    when (ev.key) {
                        Key.E -> { viewModel.simulation.performContextAction(); true }
                        Key.Q -> { viewModel.simulation.swapWeapon(); true }
                        Key.R -> { viewModel.simulation.triggerReload(); true }
                        Key.F -> { viewModel.simulation.toggleAutoFire(); true }
                        Key.Spacebar -> { viewModel.simulation.manualFireHeld = true; true }
                        else -> false
                    }
                } else if (ev.type == KeyEventType.KeyUp) {
                    heldKeys.remove(ev.key)
                    syncKeyAxes()
                    if (ev.key == Key.Spacebar) viewModel.simulation.manualFireHeld = false
                    false
                } else false
            }
    ) {
        val isCompact = maxWidth < 600.dp

        // 1. Full-screen 2.5D Vertical Oblique Canvas + Multi-Touch Twin Joysticks
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    val stickRadius = 56f
                    fun updateStick(s: StickInput, px: Float, py: Float) {
                        var dx = px - s.centerX
                        var dy = py - s.centerY
                        val dist = hypot(dx, dy)
                        if (dist > stickRadius) {
                            dx = dx / dist * stickRadius
                            dy = dy / dist * stickRadius
                        }
                        s.knobX = s.centerX + dx
                        s.knobY = s.centerY + dy
                        s.vx = dx / stickRadius
                        s.vy = dy / stickRadius
                    }

                    awaitPointerEventScope {
                        val downPositions = mutableMapOf<Long, Offset>()
                        var prevPinchDist: Float? = null
                        while (true) {
                            val event = awaitPointerEvent()
                            val sim = viewModel.simulation
                            val activePressed = event.changes.filter { it.pressed }
                            if (sim.buildModeActive && viewModel.activeModal.value == ActiveModal.NONE && activePressed.size == 2) {
                                // Clamped two-finger pinch zoom only while Base Builder is active
                                sim.leftStick.apply { active = false; pointerId = -1L; vx = 0f; vy = 0f }
                                sim.rightStick.apply { active = false; pointerId = -1L; vx = 0f; vy = 0f }
                                val p1 = activePressed[0].position
                                val p2 = activePressed[1].position
                                val dist = hypot(p2.x - p1.x, p2.y - p1.y)
                                val lastDist = prevPinchDist
                                if (lastDist != null && lastDist > 12f && dist > 12f) {
                                    viewModel.setCameraZoom(sim.cameraZoom * (dist / lastDist))
                                }
                                prevPinchDist = dist
                                activePressed.forEach { it.consume() }
                                continue
                            } else {
                                prevPinchDist = null
                            }
                            for (change in event.changes) {
                                val pid = change.id.value
                                val pos = change.position
                                if (change.pressed) {
                                    if (pid !in downPositions) {
                                        downPositions[pid] = pos
                                    }
                                    val startPos = downPositions[pid] ?: pos
                                    val movedDist = hypot(pos.x - startPos.x, pos.y - startPos.y)
                                    val allowStickHere = !sim.buildModeActive || movedDist > 18f
                                    if (sim.leftStick.active && sim.leftStick.pointerId == pid) {
                                        updateStick(sim.leftStick, pos.x, pos.y)
                                    } else if (sim.rightStick.active && sim.rightStick.pointerId == pid) {
                                        updateStick(sim.rightStick, pos.x, pos.y)
                                    } else if (allowStickHere && pos.x < size.width * 0.5f && !sim.leftStick.active) {
                                        sim.leftStick.apply {
                                            active = true
                                            pointerId = pid
                                            centerX = startPos.x
                                            centerY = startPos.y
                                            knobX = pos.x
                                            knobY = pos.y
                                            vx = 0f
                                            vy = 0f
                                        }
                                        updateStick(sim.leftStick, pos.x, pos.y)
                                    } else if (allowStickHere && pos.x >= size.width * 0.5f && !sim.rightStick.active) {
                                        sim.rightStick.apply {
                                            active = true
                                            pointerId = pid
                                            centerX = startPos.x
                                            centerY = startPos.y
                                            knobX = pos.x
                                            knobY = pos.y
                                            vx = 0f
                                            vy = 0f
                                        }
                                        updateStick(sim.rightStick, pos.x, pos.y)
                                    }
                                    change.consume()
                                } else {
                                    val startPos = downPositions.remove(pid)
                                    if (sim.leftStick.active && sim.leftStick.pointerId == pid) {
                                        sim.leftStick.apply { active = false; pointerId = -1L; vx = 0f; vy = 0f }
                                    }
                                    if (sim.rightStick.active && sim.rightStick.pointerId == pid) {
                                        sim.rightStick.apply { active = false; pointerId = -1L; vx = 0f; vy = 0f }
                                    }
                                    if (sim.buildModeActive &&
                                        viewModel.activeModal.value == ActiveModal.NONE &&
                                        startPos != null &&
                                        hypot(pos.x - startPos.x, pos.y - startPos.y) < 22f
                                    ) {
                                        viewModel.onBuildCanvasTap(
                                            screenX = pos.x,
                                            screenY = pos.y,
                                            canvasWidth = size.width.toFloat(),
                                            canvasHeight = size.height.toFloat()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            // Read frameTick so Compose redraws smoothly each vsync
            @Suppress("UNUSED_EXPRESSION")
            frameTick
            renderWorld(viewModel.simulation)
        }

        // 2. Option-3-Inspired Tactical Slate-Glass HUD (Respects Notch & System Nav Insets)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(8.dp)
        ) {
            // Top-Left PLAYER STATUS Telemetry Card (2-column sleek tactical readouts)
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .widthIn(max = if (isCompact) 244.dp else 318.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pixelBevelFrame(fillColor = BunkerPanel.copy(alpha = 0.88f), borderColor = OliveBorder, borderWidth = 1.5.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Transparent
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        // Header row: status dot + PLAYER STATUS // CALLSIGN + floor/elev pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(CyanFloor, RoundedCornerShape(1.dp))
                                )
                                Text(
                                    text = viewModel.simulation.activeOperator?.callsign ?: "NO OPERATOR",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                CredBadge(amount = stash.credits, showUnit = false)
                                if (hud.elevation == 0 && hud.terrainHeight != 0) {
                                    val elevTag = when {
                                        hud.terrainHeight >= 2 -> "PASS +2"
                                        hud.terrainHeight == 1 -> "ELEV +1"
                                        else -> "BANK -1"
                                    }
                                    BadgePill(text = elevTag, color = EmeraldHud)
                                }
                                BadgePill(text = hud.floorLabel, color = CyanFloor)
                            }
                        }

                        // Compact 2-column vitals grid matching Option 3's telemetry panel
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                StatBarRow("HP", hud.hp / hud.maxHp.coerceAtLeast(1f), "${hud.hp.toInt()}%", EmeraldHud)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                StatBarRow("HUN", hud.hunger / 100f, "${hud.hunger.toInt()}%", AmberSignal)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (hud.armorTier.level > 0) {
                                    StatBarRow(
                                        label = hud.armorTier.shortName,
                                        progress = hud.armorHp / hud.maxArmorHp.coerceAtLeast(1f),
                                        valueText = "${hud.armorHp.toInt()}",
                                        color = Color(hud.armorTier.colorHex)
                                    )
                                } else {
                                    StatBarRow(
                                        label = if (hud.mapId == GameMapId.OAKRIDGE_TOWN) "CONE" else "ZONE",
                                        progress = if (hud.mapId == GameMapId.OAKRIDGE_TOWN) hud.focusPct / 100f else 1f,
                                        valueText = if (hud.mapId == GameMapId.OAKRIDGE_TOWN) "${hud.focusPct}%" else "BASE",
                                        color = AmberBright
                                    )
                                }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                StatBarRow("THI", hud.thirst / 100f, "${hud.thirst.toInt()}%", CyanFloor)
                            }
                        }

                        // Footer telemetry strip: active zone + time of day + blackout/focus warning
                        val clockColor = when {
                            hud.isNightTime -> PurpleRare
                            hud.timePhaseLabel == "GOLDEN HR" || hud.timePhaseLabel == "DAWN" -> AmberBright
                            else -> BoneWhite
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = hud.zoneName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanFloor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${hud.timeClockLabel} • ${hud.timePhaseLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = clockColor,
                                maxLines = 1
                            )
                            if (hud.mapId == GameMapId.OAKRIDGE_TOWN && hud.dangerTier > 0) {
                                val tierColor = when (hud.dangerTier) {
                                    1 -> EmeraldHud
                                    2 -> AmberSignal
                                    3 -> CrimsonAlert
                                    else -> PurpleRare
                                }
                                BadgePill(text = "T${hud.dangerTier}", color = tierColor)
                            }
                            if (hud.frenzyActive) {
                                BadgePill(text = "FRENZY ${hud.frenzySecsLeft}s", color = CrimsonAlert)
                            } else if (hud.powerGridOutage) {
                                BadgePill(text = "OUTAGE ${hud.blackoutSecsLeft}s", color = CrimsonAlert)
                            }
                            if (hud.activeScavAlertCount > 0) {
                                BadgePill(text = "COMBAT x${hud.activeScavAlertCount}", color = AmberBright)
                            }
                        }
                    }
                }

                if (settings.showTaskTrackers) {
                    val activeMission = HqMissionCatalog.byId(stash.activeMissionId)
                    val allMissionsDone = activeMission.id in stash.completedMissionIds
                    val missionReady = !allMissionsDone && stash.activeMissionProgress >= activeMission.targetCount
                    val activeFavor = SurvivorCatalog.favorById(stash.activeFavorId)
                    val favorReady = activeFavor != null && stash.activeFavorProgress >= activeFavor.targetCount

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pixelBevelFrame(fillColor = BunkerPanel.copy(alpha = 0.84f), borderColor = OliveBorder, borderWidth = 1.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Transparent
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (missionReady) viewModel.claimMissionReward()
                                        else viewModel.openModal(ActiveModal.LAPTOP)
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (allMissionsDone) "HQ TASK // COMPLETE" else "OBJ // ${activeMission.title}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberBright,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (missionReady) {
                                    BadgePill(text = "CLAIM +${activeMission.rewardCred}", color = EmeraldHud)
                                } else if (!allMissionsDone) {
                                    Text(
                                        text = "${stash.activeMissionProgress.coerceAtMost(activeMission.targetCount)}/${activeMission.targetCount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AmberSignal
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (favorReady) viewModel.claimFavorReward()
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (activeFavor != null) "FAVOR // ${activeFavor.npcName}: ${activeFavor.title}" else "FAVOR // Speak to camp crew",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanFloor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (favorReady) {
                                    BadgePill(text = "CLAIM +${activeFavor.rewardCred}", color = EmeraldHud)
                                } else if (activeFavor != null) {
                                    Text(
                                        text = "${stash.activeFavorProgress.coerceAtMost(activeFavor.targetCount)}/${activeFavor.targetCount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldHud
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Top-Right Option-3 Tactical Radar + Quick Sector & Ammo Readout
            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Surface(
                    modifier = Modifier.pixelBevelFrame(fillColor = BunkerPanel.copy(alpha = 0.88f), borderColor = OliveBorder, borderWidth = 1.5.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Transparent
                ) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (hud.mapId == GameMapId.OAKRIDGE_TOWN) "DELAINE" else "OUTPOST",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                            Text(
                                text = if (settings.rotateMinimap) "ROT" else "N",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldHud,
                                modifier = Modifier
                                    .clickable { viewModel.updateSettings { it.copy(rotateMinimap = !it.rotateMinimap) } }
                                    .padding(horizontal = 3.dp, vertical = 1.dp)
                            )
                            Text(
                                text = if (settings.showMinimap) (if (miniLarge) "MAX" else "MAP") else "SHOW",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanFloor,
                                modifier = Modifier
                                    .clickable {
                                        if (!settings.showMinimap) {
                                            viewModel.updateSettings { it.copy(showMinimap = true) }
                                            miniLarge = false
                                        } else if (!miniLarge) {
                                            miniLarge = true
                                        } else {
                                            viewModel.updateSettings { it.copy(showMinimap = false) }
                                            miniLarge = false
                                        }
                                    }
                                    .padding(horizontal = 3.dp, vertical = 1.dp)
                            )
                        }

                        if (settings.showMinimap) {
                            val mapDp = if (!miniLarge) 98.dp else 148.dp
                            Canvas(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(mapDp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF061118))
                                    .border(1.dp, CyanFloor.copy(alpha = 0.55f), RoundedCornerShape(3.dp))
                            ) {
                                @Suppress("UNUSED_EXPRESSION")
                                frameTick
                                val w = viewModel.simulation.world ?: return@Canvas
                                val cx = size.width / 2f
                                val cy = size.height / 2f

                                translate(left = cx, top = cy) {
                                    if (settings.rotateMinimap) {
                                        rotate(degrees = (-hud.playerAngle - PI.toFloat() / 2f) * 180f / PI.toFloat(), pivot = Offset.Zero) {
                                            drawMinimapWorld(w, hud.playerX, hud.playerY, hud.elevation, viewModel)
                                        }
                                    } else {
                                        drawMinimapWorld(w, hud.playerX, hud.playerY, hud.elevation, viewModel)
                                    }
                                }

                                // Option-3 Radar Sweep Rings & Crosshair Overlay
                                val ringCol = CyanFloor.copy(alpha = 0.24f)
                                drawCircle(ringCol, radius = size.minDimension * 0.22f, center = Offset(cx, cy), style = Stroke(width = 1f))
                                drawCircle(ringCol, radius = size.minDimension * 0.39f, center = Offset(cx, cy), style = Stroke(width = 1f))
                                drawLine(ringCol, Offset(cx, 0f), Offset(cx, size.height), strokeWidth = 0.9f)
                                drawLine(ringCol, Offset(0f, cy), Offset(size.width, cy), strokeWidth = 0.9f)
                            }
                        }

                        // Compact weapon + ammo bar right beneath radar like Option 3's top-right panel
                        val wLabel = if (hud.weapon.isMelee) {
                            hud.weapon.shortName.uppercase()
                        } else {
                            "${hud.weapon.shortName} (${hud.magAmmo}/${hud.reserveAmmo})"
                        }
                        Text(
                            text = wLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberBright,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Live PvP Kill Feed
                val nowMs = remember(frameTick / 30L, liveKillFeed) { System.currentTimeMillis() }
                val visibleKills = liveKillFeed.filter { nowMs - it.timestampMs <= 22_000L }.take(4)
                if (visibleKills.isNotEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.widthIn(max = if (isCompact) 228.dp else 290.dp).testTag("live_kill_feed")
                    ) {
                        visibleKills.forEach { entry ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = BunkerObsidian.copy(alpha = 0.88f),
                                modifier = Modifier.border(1.dp, CrimsonAlert.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = entry.killerCallsign,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AmberBright,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "[${entry.weaponName}]",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanFloor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "➔ ${entry.victimCallsign}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFCA5A5),
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Option-3 Top-Center Quarantine Zone Header + Raid Clock & Status Toast
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!isCompact) {
                    val mins = (hud.raidElapsedSecs / 60).toString().padStart(2, '0')
                    val secs = (hud.raidElapsedSecs % 60).toString().padStart(2, '0')
                    Row(
                        modifier = Modifier
                            .pixelBevelFrame(fillColor = BunkerPanel.copy(alpha = 0.82f), borderColor = OliveBorder, borderWidth = 1.dp)
                            .padding(horizontal = 14.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (hud.mapId == GameMapId.OAKRIDGE_TOWN) "QUARANTINE ZONE: DELAINE, FL" else "OUTPOST ZERO COMPOUND",
                            style = MaterialTheme.typography.labelSmall,
                            color = BoneWhite
                        )
                        Text(
                            text = "${hud.timeClockLabel} (${hud.timePhaseLabel})",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hud.isNightTime) PurpleRare else AmberBright
                        )
                        if (hud.mapId == GameMapId.OAKRIDGE_TOWN) {
                            Text(
                                text = "$mins:$secs",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanFloor
                            )
                            Text(
                                text = "KILLS ${hud.kills}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CrimsonAlert
                            )
                        }
                    }
                }
                if (hud.statusToast.isNotBlank()) {
                    Surface(
                        modifier = Modifier.pixelBevelFrame(fillColor = BunkerObsidian.copy(alpha = 0.92f), borderColor = AmberSignal, borderWidth = 1.5.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Transparent
                    ) {
                        Text(
                            text = hud.statusToast,
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberBright,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                if (hud.radioCallout.isNotBlank()) {
                    val calloutBorder = if (hud.frenzyActive) CrimsonAlert else PurpleRare
                    Surface(
                        modifier = Modifier
                            .widthIn(max = 360.dp)
                            .pixelBevelFrame(fillColor = BunkerObsidian.copy(alpha = 0.94f), borderColor = calloutBorder, borderWidth = 1.5.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Transparent
                    ) {
                        Text(
                            text = hud.radioCallout,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF3E8FF),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Bottom HUD Area: Cleanly split into separate, non-overlapping menus
            if (buildModeActive && hud.mapId == GameMapId.OUTPOST_ZERO) {
                val ensuredLayout = remember(stash.baseLayout, stash.bunkLevel, stash.helipadLevel) {
                    stash.baseLayout.ensureInfrastructure(stash.bunkLevel, stash.helipadLevel)
                }
                val selectedPiece = ensuredLayout.pieces.firstOrNull { it.id == selectedBuildPieceId }
                BaseBuilderHudBar(
                    selectedPiece = selectedPiece,
                    pendingKind = pendingBuildKind,
                    pendingRot = pendingBuildRot,
                    cameraZoom = settings.cameraZoom,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    onOpenCatalog = { viewModel.openBaseBuilderCatalog() },
                    onRotate = { viewModel.rotateSelectedOrPendingPiece() },
                    onDelete = { viewModel.deleteSelectedPiece() },
                    onAdjustZoom = { delta -> viewModel.adjustCameraZoom(delta) },
                    onDone = { viewModel.toggleBaseBuildMode(false) }
                )
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Context Action Prompt sits safely ABOVE the split bottom menu docks
                    if (hud.contextPrompt.kind != ContextPromptKind.NONE) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { viewModel.simulation.performContextAction() },
                                modifier = Modifier.height(48.dp).testTag("context_action_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when (hud.contextPrompt.kind) {
                                        ContextPromptKind.EXTRACT -> EmeraldHud
                                        ContextPromptKind.TALK_NPC -> CyanFloor
                                        else -> AmberSignal
                                    },
                                    contentColor = Color.Black
                                )
                            ) {
                                Text(hud.contextPrompt.label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Left Dedicated Menu Dock: Option-3 Tactical Quick Belt
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Transparent,
                            modifier = Modifier.pixelBevelFrame(fillColor = BunkerPanel.copy(alpha = 0.88f), borderColor = OliveBorder, borderWidth = 1.5.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = if (isCompact) 5.dp else 7.dp, vertical = 5.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HudActionButton(
                                    icon = Icons.Default.Backpack,
                                    label = "PACK",
                                    tint = AmberSignal,
                                    tag = "hud_pack_btn",
                                    compact = isCompact
                                ) { viewModel.openModal(ActiveModal.BACKPACK) }

                                HudActionButton(
                                    icon = Icons.Default.Dashboard,
                                    label = "OPS",
                                    tint = CyanFloor,
                                    tag = "hud_ops_btn",
                                    compact = isCompact
                                ) { viewModel.openModal(ActiveModal.OPS_HUB) }

                                HudActionButton(
                                    icon = Icons.Default.Settings,
                                    label = "SET",
                                    tint = Color(0xFFD5E3DA),
                                    tag = "hud_settings_btn",
                                    compact = isCompact
                                ) { viewModel.openModal(ActiveModal.SETTINGS) }

                                if (hud.mapId == GameMapId.OUTPOST_ZERO) {
                                    HudActionButton(
                                        icon = Icons.Default.Construction,
                                        label = "BUILD",
                                        tint = AmberBright,
                                        tag = "hud_build_btn",
                                        compact = isCompact
                                    ) { viewModel.toggleBaseBuildMode() }
                                }
                            }
                        }

                        // Right Dedicated Menu Dock: Option-3 Weapon & Combat Deck
                        if (hud.mapId == GameMapId.OAKRIDGE_TOWN || hud.weapon.isMelee) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Transparent,
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .pixelBevelFrame(fillColor = BunkerPanel.copy(alpha = 0.88f), borderColor = OliveBorder, borderWidth = 1.5.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = if (isCompact) 5.dp else 7.dp, vertical = 5.dp)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (hud.mapId == GameMapId.OAKRIDGE_TOWN) {
                                        HudActionButton(
                                            icon = Icons.Default.Bolt,
                                            label = if (hud.autoFire) "AUTO" else "MANUAL",
                                            tint = if (hud.autoFire) EmeraldHud else Color.Gray,
                                            tag = "hud_autofire_btn",
                                            compact = isCompact
                                        ) { viewModel.simulation.toggleAutoFire() }

                                        if (!hud.weapon.isMelee) {
                                            HudActionButton(
                                                icon = Icons.Default.Refresh,
                                                label = if (hud.reloading) "..." else "${hud.magAmmo}/${hud.reserveAmmo}",
                                                tint = CyanFloor,
                                                tag = "hud_reload_btn",
                                                compact = isCompact
                                            ) { viewModel.simulation.triggerReload() }
                                        }
                                    }

                                    HudActionButton(
                                        icon = Icons.Default.SwapHoriz,
                                        label = hud.weapon.shortName,
                                        tint = AmberSignal,
                                        tag = "hud_swap_btn",
                                        compact = isCompact
                                    ) { viewModel.simulation.swapWeapon() }

                                    Surface(
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .border(
                                                1.5.dp,
                                                if (hud.meleeSwingActive) AmberBright else if (hud.weapon.isMelee) CrimsonAlert else CyanFloor,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .pointerInput(hud.weapon) {
                                                detectTapGestures(
                                                    onPress = {
                                                        viewModel.simulation.manualFireHeld = true
                                                        tryAwaitRelease()
                                                        viewModel.simulation.manualFireHeld = false
                                                    }
                                                )
                                            }
                                            .testTag("hud_attack_btn"),
                                        color = if (hud.weapon.isMelee) Color(0xFF3E1418) else Color(0xFF0E2B38),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = if (isCompact) 8.dp else 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.GpsFixed,
                                                contentDescription = if (hud.weapon.isMelee) "Swing Melee" else "Fire Weapon",
                                                tint = if (hud.weapon.isMelee) Color(0xFFFCA5A5) else AmberBright,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = if (hud.weapon.isMelee) "SWING" else "FIRE",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Modals Layer
        val activeOp = roster.firstOrNull { it.id == stash.activeOperatorId }
            ?: viewModel.simulation.activeOperator

        when (activeModal) {
            ActiveModal.CHARACTER_CREATOR -> {
                CharacterCreatorModal(
                    rosterCount = roster.size,
                    maxBunkSlots = stash.maxRosterSlots,
                    stash = stash,
                    onCreate = { call, name, arch, sex, skinTone, shirt, pants, shoe, hat, glasses, gloves ->
                        viewModel.createOperator(call, name, arch, sex, skinTone, shirt, pants, shoe, hat, glasses, gloves)
                    },
                    onClose = if (roster.isNotEmpty()) ({ viewModel.closeModal() }) else null
                )
            }
            ActiveModal.LAPTOP -> {
                OutpostLaptopModal(
                    roster = roster,
                    stash = stash,
                    openExtractions = hud.openExtractions,
                    isTown = hud.mapId == GameMapId.OAKRIDGE_TOWN,
                    initialTab = laptopInitialTab,
                    onSelectOperator = { viewModel.selectActiveOperator(it) },
                    onBuyBack = { viewModel.buyBackKiaOperator(it) },
                    onOpenCreator = { viewModel.openModal(ActiveModal.CHARACTER_CREATOR) },
                    onUpgradeBunk = { viewModel.upgradeBunk() },
                    onUpgradeStorage = { viewModel.upgradeStorage() },
                    onUpgradeHelipad = { viewModel.upgradeHelipad() },
                    onUpgradeReloadingGear = { viewModel.upgradeReloadingGear() },
                    onClaimMissionReward = { viewModel.claimMissionReward() },
                    onOpenHelipad = { viewModel.openModal(ActiveModal.HELIPAD) },
                    onEquipAttachment = { w, k -> viewModel.equipWeaponAttachment(w, k) },
                    onUnequipSlot = { w, slot -> viewModel.unequipWeaponSlot(w, slot) },
                    onClaimEraMilestone = { era -> viewModel.claimHeritageEraMilestone(era) },
                    onClose = { viewModel.closeModal() }
                )
            }
            ActiveModal.HELIPAD -> {
                HelipadDeploymentModal(
                    operator = activeOp,
                    hasHelipad = stash.hasHelipad,
                    selectedZone = selectedZone,
                    onSelectZone = { viewModel.selectDeploymentZone(it) },
                    onOpenUpgrades = { viewModel.openLaptopTab(2) },
                    onDeploy = { zone, customDrop -> viewModel.deployToOakridge(zone, customDrop) },
                    onClose = { viewModel.closeModal() }
                )
            }
            ActiveModal.NPC_DIALOGUE -> {
                val npc = SurvivorCatalog.npcById(activeNpcId ?: SurvivorCatalog.npcs.first().id)
                SurvivorDialogueModal(
                    npc = npc,
                    stash = stash,
                    onAcceptFavor = { viewModel.acceptFavor(it) },
                    onClaimFavorReward = { viewModel.claimFavorReward() },
                    onClose = { viewModel.closeModal() }
                )
            }
            ActiveModal.WORKBENCH -> {
                CraftingWorkbenchModal(
                    stash = stash,
                    operator = activeOp,
                    playingCdId = playingCdId,
                    onPlayCd = { viewModel.playCdTrack(it) },
                    onDepositMaterials = { viewModel.depositCarriedMaterials() },
                    onCraft = { viewModel.craftRecipe(it) },
                    onOpenUpgrades = { viewModel.openLaptopTab(2) },
                    onClose = { viewModel.closeModal() }
                )
            }
            ActiveModal.MARKET -> {
                LiveMarketModal(
                    currentUserId = viewModel.currentUserId,
                    stash = stash,
                    operator = activeOp,
                    collectedCds = collectedCds,
                    activeListingsState = activeListingsState,
                    myListingsState = myListingsState,
                    marketBusy = marketBusy,
                    playingCdId = playingCdId,
                    onPlayCd = { viewModel.playCdTrack(it) },
                    onBuyListing = { viewModel.buyMarketListing(it) },
                    onCreateListing = { cand, qty, price -> viewModel.createMarketListing(cand, qty, price) },
                    onCancelListing = { viewModel.cancelMarketListing(it) },
                    onClaimPayout = { viewModel.claimMarketPayout(it) },
                    onClose = { viewModel.closeModal() }
                )
            }
            ActiveModal.CD_BINDER -> {
                CdBinderModal(
                    collectedIds = collectedCds,
                    playingCdId = playingCdId,
                    onPlayCd = { viewModel.playCdTrack(it) },
                    onClose = { viewModel.closeModal() }
                )
            }
            ActiveModal.BACKPACK -> {
                if (activeOp != null) {
                    BackpackAndSafePocketsModal(
                        operator = activeOp,
                        armorHp = hud.armorHp,
                        backpackCap = stash.backpackSlotCap,
                        availableWeapons = hud.availableWeapons,
                        activeWeapon = hud.weapon,
                        magAmmoMap = viewModel.simulation.magAmmo,
                        reserveAmmoMap = viewModel.simulation.reserveAmmo,
                        playingCdId = playingCdId,
                        onPlayCd = { viewModel.playCdTrack(it) },
                        onMoveToSafe = { viewModel.moveItemToSafePocket(it) },
                        onMoveToBag = { viewModel.moveItemToBackpack(it) },
                        onUseItem = { item, fromSafe -> viewModel.useInventoryItem(item, fromSafe) },
                        onDropItem = { item, fromSafe -> viewModel.dropInventoryItem(item, fromSafe) },
                        onSelectWeapon = { viewModel.equipWeaponFromInspect(it) },
                        stash = stash,
                        onEquipAttachment = { w, k -> viewModel.equipWeaponAttachment(w, k) },
                        onUnequipSlot = { w, slot -> viewModel.unequipWeaponSlot(w, slot) },
                        onClose = { viewModel.closeModal() }
                    )
                }
            }
            ActiveModal.LOOT_CONTAINER -> {
                val loot = activeLoot
                if (loot != null && activeOp != null) {
                    LootSearchModal(
                        loot = loot,
                        currentPantsLevel = activeOp.pantsLevel,
                        safeSpaceLeft = activeOp.safePocketCapacity - activeOp.safePocketItems.size,
                        playingCdId = playingCdId,
                        onPlayCd = { viewModel.playCdTrack(it) },
                        onTake = { item, toSafe -> viewModel.takeLootItem(item, toSafe) },
                        onClose = { viewModel.closeModal() }
                    )
                }
            }
            ActiveModal.EXTRACTION_SUMMARY -> {
                extractionSummary?.let { sum ->
                    ExtractionSummaryModal(summary = sum, onReturnToBase = { viewModel.returnToOutpostBase() })
                }
            }
            ActiveModal.OPERATOR_DOWN -> {
                val downedOp = roster.firstOrNull { it.id == stash.activeOperatorId && it.isKia && !it.isPermadead }
                    ?: viewModel.simulation.activeOperator
                    ?: roster.firstOrNull { it.isKia && !it.isPermadead }
                if (downedOp != null) {
                    OperatorDownModal(
                        operator = downedOp,
                        credits = stash.credits,
                        onBuyBack = { viewModel.buyBackKiaOperator(downedOp.id) },
                        onReturnToBase = { viewModel.returnToOutpostBase() }
                    )
                }
            }
            ActiveModal.OPS_HUB -> {
                OpsHubModal(
                    mapId = hud.mapId,
                    hasHelipad = stash.hasHelipad,
                    onOpenModal = { modal -> viewModel.openModal(modal) },
                    onEnterBaseBuilder = {
                        viewModel.closeModal()
                        viewModel.openBaseBuilderCatalog()
                    },
                    onClose = { viewModel.closeModal() }
                )
            }
            ActiveModal.SETTINGS -> {
                SettingsModal(
                    settings = settings,
                    showReturnToSplash = true,
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onReturnToSplash = { viewModel.returnToSplashScreen() },
                    onClose = { viewModel.closeModal() }
                )
            }
            ActiveModal.RESCUE_ANIMATION -> {
                rescueMission?.let { mission ->
                    RescueTeamAnimationModal(
                        mission = mission,
                        onComplete = { viewModel.completeRescueMission() }
                    )
                }
            }
            ActiveModal.PERMADEATH_REPORT -> {
                permadeathReport?.let { report ->
                    PermadeathReportModal(
                        report = report,
                        onDismiss = { viewModel.dismissPermadeathReport() }
                    )
                }
            }
            ActiveModal.BASE_BUILDER -> {
                BaseBuilderCatalogModal(
                    stash = stash,
                    onMoveExistingPiece = { pieceId -> viewModel.selectExistingPieceForMove(pieceId) },
                    onSelectKindToPlaceOrMove = { kind -> viewModel.selectKindToBuildOrMove(kind) },
                    onUpgradeBunk = { viewModel.upgradeBunk() },
                    onUpgradeHelipad = { viewModel.upgradeHelipad() },
                    onClearToScratch = { viewModel.clearBaseToScratch() },
                    onResetDefault = { viewModel.resetBaseToDefault() },
                    onClose = { viewModel.closeModal() }
                )
            }
            ActiveModal.NONE -> Unit
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMinimapWorld(
    w: com.example.game.model.WorldMapData,
    px: Float,
    py: Float,
    elevation: Int,
    viewModel: GameViewModel
) {
    val miniScale = if (w.mapId == GameMapId.OAKRIDGE_TOWN) 0.036f else 0.055f
    scale(miniScale, pivot = Offset.Zero) {
        translate(left = -px, top = -py) {
            for (tz in w.terrainZones) {
                val col = when (tz.kind) {
                    TerrainKind.BASIN_WATER -> Color(0xFF0E7490)
                    TerrainKind.LIMESTONE_BANK -> Color(0xFF4B5563)
                    TerrainKind.DITCH -> Color(0xFF1F2D27)
                    TerrainKind.SANDY_RIDGE -> Color(0xFF574D3C)
                    TerrainKind.TERRACE_PLAZA -> Color(0xFF3F3D4A)
                    TerrainKind.OVERPASS_DECK -> Color(0xFF475569)
                }
                drawRect(col, Offset(tz.x, tz.y), Size(tz.w, tz.h))
            }
            for (r in w.roads) {
                drawRect(Color(0xFF2B3442), Offset(r.x, r.y), Size(r.w, r.h))
            }
            for (ew in w.elevatedWalkways) {
                val col = if (!ew.isBoardwalk) Color(0xFF64748B) else Color(0xFF92683A)
                drawRect(col, Offset(ew.x, ew.y), Size(ew.w, ew.h))
            }
            for (b in w.buildings) {
                drawRect(Color(b.miniColorHex), Offset(b.bounds.x, b.bounds.y), Size(b.bounds.w, b.bounds.h))
            }
            for (st in w.stations) {
                if (st.kind == StationKind.HELIPAD) {
                    drawCircle(CyanFloor, radius = 58f, center = Offset(st.x, st.y))
                }
            }
            for (npc in w.survivorNpcs) {
                drawCircle(EmeraldHud, radius = 32f, center = Offset(npc.x, npc.y))
            }
            for (ez in w.extractionZones) {
                if (ez.isOpen) {
                    drawCircle(EmeraldHud, radius = 64f, center = Offset(ez.x, ez.y))
                }
            }
            for (z in viewModel.simulation.zombies) {
                if (z.elevation == elevation && hypot(z.x - px, z.y - py) < 850f) {
                    drawCircle(CrimsonAlert, radius = 34f, center = Offset(z.x, z.y))
                }
            }
            for (r in viewModel.simulation.rivalOperators) {
                if (r.elevation == elevation && hypot(r.x - px, r.y - py) < 900f) {
                    drawCircle(CrimsonAlert, radius = 40f, center = Offset(r.x, r.y))
                }
            }
            drawCircle(AmberSignal, radius = 52f, center = Offset(px, py))
        }
    }
}

@Composable
private fun BadgePill(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(3.dp))
            .border(1.dp, color.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
private fun StatBarRow(label: String, progress: Float, valueText: String, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9EC5D1))
            Text(valueText, style = MaterialTheme.typography.labelSmall, color = color)
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = Color(0xFF050B10)
        )
    }
}

@Composable
private fun HudActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    tag: String,
    compact: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, OliveBorder.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        color = BunkerSurface.copy(alpha = 0.90f),
        shape = RoundedCornerShape(4.dp)
    ) {
        if (compact) {
            Column(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(16.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
    }
}
