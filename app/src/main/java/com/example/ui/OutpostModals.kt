package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.game.engine.LootSearchResult
import com.example.game.model.AttachmentSlot
import com.example.game.model.BaseStashState
import com.example.game.model.BaseUpgradeSpecs
import com.example.game.model.CdCatalog
import com.example.game.model.CdRarity
import com.example.game.model.CraftingMaterial
import com.example.game.model.DogBreedResolver
import com.example.game.model.ExtractionZone
import com.example.game.model.GlassesStyle
import com.example.game.model.GlovesStyle
import com.example.game.model.HatStyle
import com.example.game.model.InventoryItem
import com.example.game.model.LootType
import com.example.game.model.OperatorArchetype
import com.example.game.model.OperatorProfile
import com.example.game.model.OperatorSex
import com.example.game.model.PantsStyle
import com.example.game.model.ShirtStyle
import com.example.game.model.ShoeStyle
import com.example.game.model.SkinToneOption
import com.example.game.model.WeaponEra
import com.example.game.model.WeaponId
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSignal
import com.example.ui.theme.BunkerObsidian
import com.example.ui.theme.BunkerPanel
import com.example.ui.theme.BunkerSurface
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanFloor
import com.example.ui.theme.EmeraldHud
import com.example.ui.theme.OliveBorder
import com.example.ui.theme.PurpleRare
import com.example.ui.theme.pixelBevelFrame

@Composable
fun ModalScrim(onDismiss: (() -> Unit)? = null, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f))
            .clickable(enabled = onDismiss != null) { onDismiss?.invoke() }
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp)
                .heightIn(max = 680.dp)
                .clickable(enabled = false) {}
                .pixelBevelFrame(
                    fillColor = BunkerPanel,
                    borderColor = CyanFloor.copy(alpha = 0.72f),
                    borderWidth = 1.8.dp
                ),
            shape = RoundedCornerShape(4.dp),
            color = Color.Transparent
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CharacterCreatorModal(
    rosterCount: Int,
    maxBunkSlots: Int,
    stash: BaseStashState = BaseStashState(),
    onCreate: (
        callsign: String,
        fullName: String,
        archetype: OperatorArchetype,
        sex: OperatorSex,
        skinTone: SkinToneOption,
        shirtStyle: ShirtStyle,
        pantsStyle: PantsStyle,
        shoeStyle: ShoeStyle,
        hatStyle: HatStyle,
        glassesStyle: GlassesStyle,
        glovesStyle: GlovesStyle
    ) -> Unit,
    onClose: (() -> Unit)?
) {
    var callsign by remember { mutableStateOf("WARDEN") }
    var fullName by remember { mutableStateOf("Marcus Vance") }
    var archetype by remember { mutableStateOf(OperatorArchetype.BREACHER) }
    var sex by remember { mutableStateOf(OperatorSex.MALE) }
    var skinTone by remember { mutableStateOf(SkinToneOption.LIGHT_TAN) }
    var shirtStyle by remember { mutableStateOf(ShirtStyle.RED_FLANNEL) }
    var pantsStyle by remember { mutableStateOf(PantsStyle.BLUE_JEANS) }
    var shoeStyle by remember { mutableStateOf(ShoeStyle.WORK_BOOTS) }
    var hatStyle by remember { mutableStateOf(HatStyle.BALL_CAP) }
    var glassesStyle by remember { mutableStateOf(GlassesStyle.NONE) }
    var glovesStyle by remember { mutableStateOf(GlovesStyle.NONE) }
    var activePickerCategory by remember { mutableStateOf<CreatorPickerCategory?>(null) }

    val dogMatch = remember(shirtStyle, pantsStyle, hatStyle, glassesStyle, glovesStyle) {
        DogBreedResolver.resolve(
            shirt = shirtStyle,
            pants = pantsStyle,
            hat = hatStyle,
            glasses = glassesStyle,
            gloves = glovesStyle
        )
    }

    BackHandler(enabled = onClose != null) {
        onClose?.invoke()
    }

    ModalScrim(onDismiss = onClose) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (rosterCount == 0) "OUTPOST ZERO // SURVIVOR DOSSIER" else "RECRUIT SURVIVOR ($rosterCount/$maxBunkSlots BUNKS)",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AmberSignal
                    )
                    Text(
                        text = "Customize sex, skin, shirt, pants, shoes, hat, glasses & gloves. Your outfit chooses your K-9 breed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB5CAD6)
                    )
                }
                if (onClose != null) {
                    IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close creator", tint = Color.White)
                    }
                }
            }

            // Full-Width Atmospheric Outpost Courtyard Stage + Realistic Customizable Survivor & K-9
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pixelBevelFrame(
                        fillColor = BunkerObsidian,
                        borderColor = CyanFloor.copy(alpha = 0.72f),
                        borderWidth = 1.5.dp
                    )
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(228.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, CyanFloor.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_creator_stage_1794992833338),
                        contentDescription = "Outpost Zero twilight courtyard backdrop",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Subtle lower-edge vignette to ground feet/paws & badge legibility
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x36060A0F),
                                        Color(0x0A060A0F),
                                        Color(0x9E060A0F)
                                    )
                                )
                            )
                    )
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        NoirCharacterPortraitRenderer.run {
                            drawNoirOperatorPortrait(
                                sex = sex,
                                skinTone = skinTone,
                                archetype = archetype,
                                shirtStyle = shirtStyle,
                                pantsStyle = pantsStyle,
                                shoeStyle = shoeStyle,
                                hatStyle = hatStyle,
                                glassesStyle = glassesStyle,
                                glovesStyle = glovesStyle,
                                dogMatch = dogMatch,
                                drawOpaqueBackdrop = false
                            )
                        }
                    }
                    // Top-left live dossier telemetry tag
                    Text(
                        text = "${sex.label.uppercase()} // ${archetype.label.uppercase()} • HP ${archetype.maxHp} • SPD ${(archetype.speedMul * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberBright,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color(0xC8091016), RoundedCornerShape(3.dp))
                            .border(1.dp, AmberSignal.copy(alpha = 0.55f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BunkerSurface.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                        .border(1.dp, EmeraldHud.copy(alpha = 0.40f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = EmeraldHud, modifier = Modifier.size(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PAIRED K-9: ${dogMatch.breed.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldHud,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${dogMatch.pairingReason} • ${archetype.roleTitle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC8DBE6),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = callsign,
                    onValueChange = { if (it.length <= 14) callsign = it },
                    label = { Text("Callsign") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("creator_callsign_input")
                )
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { if (it.length <= 24) fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.weight(1.2f)
                )
            }

            // Category Summary Cards (Tap any panel to open dedicated visual picker modal)
            Text(
                text = "APPEARANCE & GEAR PANELS (TAP TO PICK)",
                style = MaterialTheme.typography.labelSmall,
                color = AmberSignal
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CreatorCategoryCard(
                    category = CreatorPickerCategory.SEX_SKIN,
                    selectedLabel = "${sex.label} • ${skinTone.label}",
                    accentColor = Color(skinTone.colorHex),
                    modifier = Modifier.weight(1f),
                    onClick = { activePickerCategory = CreatorPickerCategory.SEX_SKIN }
                ) { drawSkinSexPreview(skinTone, sex, shirtStyle) }
                CreatorCategoryCard(
                    category = CreatorPickerCategory.ARCHETYPE,
                    selectedLabel = "${archetype.label} (${archetype.maxHp} HP)",
                    accentColor = AmberBright,
                    modifier = Modifier.weight(1f),
                    onClick = { activePickerCategory = CreatorPickerCategory.ARCHETYPE }
                ) { drawArchetypePreview(archetype) }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CreatorCategoryCard(
                    category = CreatorPickerCategory.SHIRT,
                    selectedLabel = "${shirtStyle.label} • ${dogMatch.breed}",
                    accentColor = EmeraldHud,
                    modifier = Modifier.weight(1f),
                    onClick = { activePickerCategory = CreatorPickerCategory.SHIRT }
                ) { drawShirtPreview(shirtStyle) }
                CreatorCategoryCard(
                    category = CreatorPickerCategory.PANTS,
                    selectedLabel = pantsStyle.label,
                    accentColor = Color(pantsStyle.trimHex),
                    modifier = Modifier.weight(1f),
                    onClick = { activePickerCategory = CreatorPickerCategory.PANTS }
                ) { drawPantsPreview(pantsStyle) }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CreatorCategoryCard(
                    category = CreatorPickerCategory.SHOES,
                    selectedLabel = shoeStyle.label,
                    accentColor = AmberSignal,
                    modifier = Modifier.weight(1f),
                    onClick = { activePickerCategory = CreatorPickerCategory.SHOES }
                ) { drawShoePreview(shoeStyle) }
                CreatorCategoryCard(
                    category = CreatorPickerCategory.HAT,
                    selectedLabel = hatStyle.label,
                    accentColor = if (hatStyle == HatStyle.NONE) CyanFloor else Color(hatStyle.accentHex),
                    modifier = Modifier.weight(1f),
                    onClick = { activePickerCategory = CreatorPickerCategory.HAT }
                ) { drawHatPreview(hatStyle, skinTone) }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CreatorCategoryCard(
                    category = CreatorPickerCategory.GLASSES,
                    selectedLabel = glassesStyle.label,
                    accentColor = if (glassesStyle == GlassesStyle.NONE) CyanFloor else Color(glassesStyle.frameHex),
                    modifier = Modifier.weight(1f),
                    onClick = { activePickerCategory = CreatorPickerCategory.GLASSES }
                ) { drawGlassesPreview(glassesStyle, skinTone) }
                CreatorCategoryCard(
                    category = CreatorPickerCategory.GLOVES,
                    selectedLabel = glovesStyle.label,
                    accentColor = if (glovesStyle == GlovesStyle.NONE) Color(skinTone.colorHex) else Color(glovesStyle.colorHex),
                    modifier = Modifier.weight(1f),
                    onClick = { activePickerCategory = CreatorPickerCategory.GLOVES }
                ) { drawGlovesPreview(glovesStyle, skinTone) }
            }

            val recruitCred = BaseUpgradeSpecs.recruitOperatorCredCost(rosterCount)
            val recruitMats = BaseUpgradeSpecs.recruitOperatorMats(rosterCount)
            val canAffordRecruit = rosterCount == 0 ||
                (stash.credits >= recruitCred && stash.canAffordMats(recruitMats))

            if (rosterCount > 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BunkerSurface, RoundedCornerShape(8.dp))
                        .border(
                            1.dp,
                            if (canAffordRecruit) AmberSignal.copy(alpha = 0.65f) else CrimsonAlert.copy(alpha = 0.75f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "RECRUITMENT REQUISITION FEE (OPERATOR #${rosterCount + 1})",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (canAffordRecruit) AmberBright else CrimsonAlert
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CredBadge(
                            amount = recruitCred,
                            accent = if (stash.credits >= recruitCred) AmberBright else CrimsonAlert
                        )
                        for ((mat, needed) in recruitMats) {
                            val have = stash.materialCount(mat)
                            val ok = have >= needed
                            val col = if (ok) EmeraldHud else CrimsonAlert
                            Text(
                                text = "${mat.shortName} $have/$needed",
                                style = MaterialTheme.typography.labelSmall,
                                color = col,
                                modifier = Modifier
                                    .background(col.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
                                    .border(1.dp, col.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onCreate(
                        callsign,
                        fullName,
                        archetype,
                        sex,
                        skinTone,
                        shirtStyle,
                        pantsStyle,
                        shoeStyle,
                        hatStyle,
                        glassesStyle,
                        glovesStyle
                    )
                },
                enabled = canAffordRecruit,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(1.5.dp, Color(0xFF06080C), RoundedCornerShape(4.dp))
                    .testTag("create_operator_submit"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberSignal,
                    contentColor = Color(0xFF06080C),
                    disabledContainerColor = Color(0xFF1B2732),
                    disabledContentColor = Color(0xFF7B8E9B)
                )
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = when {
                        rosterCount == 0 -> "CHECK INTO OUTPOST ZERO (FREE)"
                        canAffordRecruit -> "RECRUIT SURVIVOR ($recruitCred CRED)"
                        else -> "NEED $recruitCred CRED & MATS"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    activePickerCategory?.let { pickerCat ->
        CreatorVisualPickerDialog(
            category = pickerCat,
            sex = sex,
            skinTone = skinTone,
            archetype = archetype,
            shirtStyle = shirtStyle,
            pantsStyle = pantsStyle,
            shoeStyle = shoeStyle,
            hatStyle = hatStyle,
            glassesStyle = glassesStyle,
            glovesStyle = glovesStyle,
            onSelectSex = { sex = it },
            onSelectSkinTone = { skinTone = it },
            onSelectArchetype = { archetype = it },
            onSelectShirt = { shirtStyle = it },
            onSelectPants = { pantsStyle = it },
            onSelectShoe = { shoeStyle = it },
            onSelectHat = { hatStyle = it },
            onSelectGlasses = { glassesStyle = it },
            onSelectGloves = { glovesStyle = it },
            onDismiss = { activePickerCategory = null }
        )
    }
}

@Composable
fun OutpostLaptopModal(
    roster: List<OperatorProfile>,
    stash: BaseStashState,
    openExtractions: List<ExtractionZone>,
    isTown: Boolean,
    initialTab: Int = if (isTown) 1 else 0,
    onSelectOperator: (String) -> Unit,
    onBuyBack: (String) -> Unit,
    onOpenCreator: () -> Unit,
    onUpgradeBunk: () -> Unit,
    onUpgradeStorage: () -> Unit,
    onUpgradeHelipad: () -> Unit = {},
    onUpgradeReloadingGear: () -> Unit = {},
    onClaimMissionReward: () -> Unit,
    onOpenHelipad: () -> Unit,
    onEquipAttachment: ((WeaponId, String) -> Unit)? = null,
    onUnequipSlot: ((WeaponId, AttachmentSlot) -> Unit)? = null,
    onClaimEraMilestone: (WeaponEra) -> Unit = {},
    onClose: () -> Unit
) {
    var activeTab by remember(initialTab, isTown) { mutableIntStateOf(if (isTown) 1 else initialTab.coerceIn(0, 3)) } // 0 = Crew, 1 = HQ Missions, 2 = Base Upgrades, 3 = Heritage Vault
    val totalStoredMats = remember(stash) { CraftingMaterial.entries.sumOf { stash.materialCount(it) } }

    ModalScrim(onDismiss = onClose) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Computer, contentDescription = null, tint = AmberSignal)
                    Column {
                        Text("OUTPOST ZERO // HQ TERMINAL", style = MaterialTheme.typography.headlineMedium, color = AmberSignal)
                        Text("Manage barracks crew, HQ directives & compound upgrades.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close laptop", tint = Color.White)
                }
            }

            // Outpost Shared Stockpile + CRED Emblem
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BunkerSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, OliveBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CredBadge(amount = stash.credits)
                    Text("FOOD: ${stash.foodStock}", style = MaterialTheme.typography.labelSmall, color = EmeraldHud)
                    Text("WATER: ${stash.waterStock}", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
                    Text("MEDS: ${stash.medsStock}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFCA5A5))
                }
                Text(
                    text = "DEPOT ($totalStoredMats TOTAL ACROSS 24 MATS • CAP ${stash.storageMaterialCap}/MAT) • RELOADING GEAR LV.${stash.reloadingGearLevel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFBFD3C7),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Terminal Tabs: Crew Roster | HQ Missions | Base Upgrades | Heritage Vault
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                FilterChip(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("laptop_tab_crew"),
                    label = { Text(if (isTown) "EXITS" else "CREW ${roster.size}/${stash.maxRosterSlots}", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
                FilterChip(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("laptop_tab_missions"),
                    label = { Text("TASKS", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
                FilterChip(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("laptop_tab_upgrades"),
                    label = { Text("BASE", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
                if (!isTown) {
                    FilterChip(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        modifier = Modifier.weight(1.05f).heightIn(min = 48.dp).testTag("laptop_tab_vault"),
                        label = {
                            Text(
                                text = "VAULT ${stash.collectedHistoricKeys.size}/${WeaponId.historicalCollectibles.size}",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            when (activeTab) {
                1 -> {
                    HqMissionsPane(
                        stash = stash,
                        onClaimMissionReward = onClaimMissionReward,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                2 -> {
                    BaseUpgradesPane(
                        stash = stash,
                        onUpgradeBunk = onUpgradeBunk,
                        onUpgradeStorage = onUpgradeStorage,
                        onUpgradeHelipad = onUpgradeHelipad,
                        onUpgradeReloadingGear = onUpgradeReloadingGear,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                3 -> {
                    HeritageVaultPane(
                        stash = stash,
                        onClaimEraMilestone = onClaimEraMilestone,
                        onEquipAttachment = onEquipAttachment,
                        onUnequipSlot = onUnequipSlot,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                else -> {
                    if (openExtractions.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BunkerObsidian, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("DELAINE ACTIVE HELI EXITS (NO RAID TIMER):", style = MaterialTheme.typography.labelSmall, color = EmeraldHud)
                            openExtractions.forEach { ez ->
                                Text("• ${ez.name}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFD1FAE5))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BARRACKS ROSTER (${roster.size}/${stash.maxRosterSlots} BUNKS)",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberSignal
                        )
                        if (roster.size < stash.maxRosterSlots && !isTown) {
                            OutlinedButton(
                                onClick = onOpenCreator,
                                modifier = Modifier.heightIn(min = 48.dp).testTag("crew_recruit_btn"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("+ RECRUIT", style = MaterialTheme.typography.labelSmall)
                            }
                        } else if (!isTown && roster.size >= stash.maxRosterSlots && stash.bunkLevel < BaseUpgradeSpecs.MAX_LEVEL) {
                            TextButtonHint("UPGRADE BUNKS ->", tag = "crew_upgrade_bunks_btn") { activeTab = 2 }
                        }
                    }

                    if (!isTown && roster.isNotEmpty() && roster.size >= stash.maxRosterSlots) {
                        val atMaxBunkLevel = stash.bunkLevel >= BaseUpgradeSpecs.MAX_LEVEL
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CrimsonAlert.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
                                .border(1.dp, CrimsonAlert.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (atMaxBunkLevel) {
                                    "MAX BUNKS OCCUPIED (${roster.size}/${stash.maxRosterSlots}) — All barracks quarters are full."
                                } else {
                                    "NO OPEN BUNK (${roster.size}/${stash.maxRosterSlots}) — Buy a Barracks Bunk upgrade before recruiting another operator."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFCA5A5),
                                modifier = Modifier.weight(1f).padding(end = 6.dp)
                            )
                            if (!atMaxBunkLevel) {
                                OutlinedButton(
                                    onClick = { activeTab = 2 },
                                    modifier = Modifier.heightIn(min = 44.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("BUNKS", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false).heightIn(max = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(roster, key = { it.id }) { op ->
                            val isActive = op.id == stash.activeOperatorId
                            val buyBackFee = op.currentBuyBackFee()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isActive) Color(0xFF1E3224) else BunkerSurface, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isActive) AmberSignal else OliveBorder, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${op.callsign} (${op.archetype.label})" + if (op.isKia) " — KIA (${formatRemainingHms(op.remainingPermadeathMs())} LEFT)" else "",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (op.isKia) CrimsonAlert else Color.White
                                    )
                                    Text(
                                        text = if (op.isKia) {
                                            "Exposed Bag: ${op.backpackItems.size} • Stolen: ${op.stolenItemNames.size} • Safe: ${op.safePocketItems.size}"
                                        } else {
                                            "${op.fullName} | ${op.dogBreed} | ${op.armorTier.shortName} | Pants Lv.${op.pantsLevel}"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (op.isKia) {
                                    Button(
                                        onClick = { onBuyBack(op.id) },
                                        modifier = Modifier.heightIn(min = 48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert, contentColor = Color.White)
                                    ) {
                                        Text("RESCUE $buyBackFee CRED", style = MaterialTheme.typography.labelSmall)
                                    }
                                } else if (!isActive && !isTown) {
                                    OutlinedButton(
                                        onClick = { onSelectOperator(op.id) },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Text("TAKE CONTROL", style = MaterialTheme.typography.labelSmall)
                                    }
                                } else if (isActive) {
                                    Text("ACTIVE", style = MaterialTheme.typography.labelSmall, color = AmberSignal)
                                }
                            }
                        }
                    }
                }
            }

            if (!isTown) {
                Button(
                    onClick = onOpenHelipad,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("deploy_oakridge_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberSignal, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.FlightTakeoff, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (stash.hasHelipad) "HEAD TO EAST HELIPAD (DROP ANYWHERE)" else "DEPLOY VIA SOUTH GATE (FOOT)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TextButtonHint(label: String, tag: String = "hint_btn", onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.heightIn(min = 48.dp).testTag(tag),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = CyanFloor)
    }
}

@Composable
fun CdBinderModal(
    collectedIds: Set<Int>,
    playingCdId: Int?,
    onPlayCd: (Int) -> Unit,
    onClose: () -> Unit
) {
    var onlyCollected by remember { mutableStateOf(false) }
    var rarityFilter by remember { mutableStateOf<CdRarity?>(null) }
    var page by remember { mutableIntStateOf(0) }
    var inspectedCdId by remember { mutableStateOf<Int?>(null) }
    val pageSize = 24

    val filteredIds = remember(collectedIds, onlyCollected, rarityFilter) {
        (1..CdCatalog.TOTAL_CDS).filter { id ->
            val ownedOk = !onlyCollected || id in collectedIds
            val rarityOk = rarityFilter == null || CdCatalog.getById(id).rarity == rarityFilter
            ownedOk && rarityOk
        }
    }
    val totalPages = ((filteredIds.size - 1) / pageSize).coerceAtLeast(0) + 1
    val safePage = page.coerceIn(0, totalPages - 1)
    val pageItems = filteredIds.drop(safePage * pageSize).take(pageSize)

    ModalScrim(onDismiss = onClose) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Album, contentDescription = null, tint = PurpleRare)
                    Column {
                        Text("THE CD BOOK // BASE RACK", style = MaterialTheme.typography.headlineMedium, color = PurpleRare)
                        Text("${collectedIds.size} / ${CdCatalog.TOTAL_CDS} COLLECTED IN ORDER", style = MaterialTheme.typography.labelSmall, color = EmeraldHud)
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close CD book", tint = Color.White)
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = onlyCollected,
                        onClick = { onlyCollected = !onlyCollected; page = 0 },
                        label = { Text("Collected (${collectedIds.size})") }
                    )
                }
                items(CdRarity.entries) { r ->
                    FilterChip(
                        selected = rarityFilter == r,
                        onClick = { rarityFilter = if (rarityFilter == r) null else r; page = 0 },
                        label = { Text(r.label) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false).heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(pageItems, key = { it }) { id ->
                    val cd = remember(id) { CdCatalog.getById(id) }
                    val owned = id in collectedIds
                    val isPlaying = playingCdId == id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (owned) BunkerSurface else BunkerObsidian, RoundedCornerShape(8.dp))
                            .border(1.dp, if (owned) Color(cd.rarity.colorHex) else OliveBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable(enabled = owned) { inspectedCdId = id }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Pixel Jewel Case Preview
                        Canvas(modifier = Modifier.size(38.dp)) {
                            if (owned) {
                                drawRect(Color(cd.coverPrimaryHex), size = size)
                                drawCircle(Color(cd.coverAccentHex), radius = size.minDimension * 0.28f, center = Offset(size.width / 2f, size.height / 2f))
                                drawCircle(Color.Black, radius = 3.dp.toPx(), center = Offset(size.width / 2f, size.height / 2f))
                            } else {
                                drawRect(Color(0xFF171D19), size = size)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (owned) "${cd.catalogTag} ${cd.artist}" else "${cd.catalogTag} [EMPTY SLEEVE]",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (owned) Color.White else Color(0xFF5D6D63),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (owned) "${cd.album} • ${cd.genre.label}" else "Rarity: ${cd.rarity.label} • ${cd.genre.label}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(cd.rarity.colorHex),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (owned) {
                            IconButton(onClick = { onPlayCd(id) }) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Stop CD" else "Play CD preview",
                                    tint = AmberSignal
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { if (safePage > 0) page = safePage - 1 }, enabled = safePage > 0) {
                    Text("PREV")
                }
                Text("PAGE ${safePage + 1} / $totalPages", style = MaterialTheme.typography.labelSmall)
                OutlinedButton(onClick = { if (safePage < totalPages - 1) page = safePage + 1 }, enabled = safePage < totalPages - 1) {
                    Text("NEXT")
                }
            }
        }
    }

    inspectedCdId?.let { cdId ->
        ItemInspectorModal(
            target = InspectTarget.BinderCd(CdCatalog.getById(cdId)),
            currentPantsLevel = 1,
            safeSpaceLeft = 0,
            playingCdId = playingCdId,
            onPlayCd = onPlayCd,
            onClose = { inspectedCdId = null }
        )
    }
}

@Composable
fun BackpackAndSafePocketsModal(
    operator: OperatorProfile,
    armorHp: Float,
    backpackCap: Int,
    availableWeapons: List<WeaponId>,
    activeWeapon: WeaponId,
    magAmmoMap: Map<WeaponId, Int>,
    reserveAmmoMap: Map<WeaponId, Int>,
    playingCdId: Int?,
    onPlayCd: (Int) -> Unit,
    onMoveToSafe: (InventoryItem) -> Unit,
    onMoveToBag: (InventoryItem) -> Unit,
    onUseItem: (InventoryItem, Boolean) -> Unit,
    onDropItem: (InventoryItem, Boolean) -> Unit,
    onSelectWeapon: (WeaponId) -> Unit,
    stash: BaseStashState = BaseStashState(),
    onEquipAttachment: ((WeaponId, String) -> Unit)? = null,
    onUnequipSlot: ((WeaponId, AttachmentSlot) -> Unit)? = null,
    onClose: () -> Unit
) {
    var inspectTarget by remember { mutableStateOf<InspectTarget?>(null) }
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Backpack & Safe, 1 = Equipment

    ModalScrim(onDismiss = onClose) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Backpack, contentDescription = null, tint = AmberSignal)
                    Column {
                        Text("${operator.callsign} // PACK & EQUIPMENT", style = MaterialTheme.typography.headlineMedium, color = AmberSignal)
                        Text("Tap any item or gear slot to inspect 3D specs", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close pack", tint = Color.White)
                }
            }

            // Screen Switcher: Backpack vs Equipment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("tab_backpack_btn"),
                    label = { Text("BACKPACK (${operator.backpackItems.size + operator.safePocketItems.size})") }
                )
                FilterChip(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("tab_equipment_btn"),
                    label = { Text("EQUIPMENT (${availableWeapons.size + 2})") }
                )
            }

            if (activeTab == 1) {
                EquipmentLoadoutPane(
                    operator = operator,
                    availableWeapons = availableWeapons,
                    activeWeapon = activeWeapon,
                    armorHp = armorHp,
                    magAmmoMap = magAmmoMap,
                    reserveAmmoMap = reserveAmmoMap,
                    onSelectWeapon = onSelectWeapon,
                    onInspectPants = {
                        inspectTarget = InspectTarget.EquippedPants(
                            level = operator.pantsLevel,
                            usedSlots = operator.safePocketItems.size
                        )
                    },
                    onInspectArmor = {
                        inspectTarget = InspectTarget.EquippedArmor(
                            tier = operator.armorTier,
                            currentShieldHp = armorHp
                        )
                    },
                    onInspectWeapon = { wpn ->
                        inspectTarget = InspectTarget.EquippedWeapon(
                            weapon = wpn,
                            isActive = wpn == activeWeapon,
                            magAmmo = magAmmoMap[wpn] ?: 0,
                            reserveAmmo = reserveAmmoMap[wpn] ?: 0
                        )
                    },
                    stash = stash,
                    modifier = Modifier.weight(1f, fill = false)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false).heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = CyanFloor, modifier = Modifier.size(16.dp))
                            Text("SAFE POCKETS (${operator.safePocketItems.size}/${operator.safePocketCapacity} UNTOUCHABLE)", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
                        }
                    }
                    if (operator.safePocketItems.isEmpty()) {
                        item {
                            Text("Empty — tuck rare CDs or meds here.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(operator.safePocketItems, key = { "safe_${it.uid}" }) { item ->
                            InventoryRow(
                                item = item,
                                inSafe = true,
                                onInspect = { inspectTarget = InspectTarget.BagItem(item, inSafePocket = true) },
                                onAction = { onMoveToBag(item) },
                                actionLabel = "TO BAG",
                                onUse = if (item.type in listOf(LootType.FOOD, LootType.WATER, LootType.MEDKIT, LootType.PANTS, LootType.ARMOR)) {
                                    { onUseItem(item, true) }
                                } else null
                            )
                        }
                    }

                    item {
                        HorizontalDivider(color = OliveBorder, modifier = Modifier.padding(vertical = 4.dp))
                        Text("EXPOSED BACKPACK (${operator.backpackItems.size}/$backpackCap)", style = MaterialTheme.typography.labelSmall, color = AmberSignal)
                    }
                    if (operator.backpackItems.isEmpty()) {
                        item {
                            Text("Backpack is empty.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(operator.backpackItems, key = { "bag_${it.uid}" }) { item ->
                            InventoryRow(
                                item = item,
                                inSafe = false,
                                onInspect = { inspectTarget = InspectTarget.BagItem(item, inSafePocket = false) },
                                onAction = { onMoveToSafe(item) },
                                actionLabel = "TO SAFE",
                                onUse = if (item.type in listOf(LootType.FOOD, LootType.WATER, LootType.MEDKIT, LootType.PANTS, LootType.ARMOR)) {
                                    { onUseItem(item, false) }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }

    inspectTarget?.let { target ->
        ItemInspectorModal(
            target = target,
            currentPantsLevel = operator.pantsLevel,
            safeSpaceLeft = operator.safePocketCapacity - operator.safePocketItems.size,
            playingCdId = playingCdId,
            onPlayCd = onPlayCd,
            onUseItem = onUseItem,
            onMoveToSafe = onMoveToSafe,
            onMoveToBag = onMoveToBag,
            onDropItem = onDropItem,
            onSelectWeapon = onSelectWeapon,
            stash = stash,
            onEquipAttachment = onEquipAttachment,
            onUnequipSlot = onUnequipSlot,
            onClose = { inspectTarget = null }
        )
    }
}

@Composable
private fun InventoryRow(
    item: InventoryItem,
    inSafe: Boolean,
    onInspect: () -> Unit,
    onAction: () -> Unit,
    actionLabel: String,
    onUse: (() -> Unit)?
) {
    val rarity = ItemRarityResolver.forInventoryItem(item)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (inSafe) Color(0xFF0F2633) else BunkerSurface, RoundedCornerShape(8.dp))
            .border(1.dp, if (inSafe) CyanFloor.copy(alpha = 0.6f) else rarity.color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable(onClick = onInspect)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(rarity.label, style = MaterialTheme.typography.labelSmall, color = rarity.color, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.name, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item.detail.isNotBlank()) {
                Text(item.detail, style = MaterialTheme.typography.bodyMedium, color = EmeraldHud, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        IconButton(onClick = onInspect, modifier = Modifier.size(48.dp).testTag("inspect_item_btn_${item.uid}")) {
            Icon(Icons.Default.Search, contentDescription = "Inspect ${item.name}", tint = AmberBright, modifier = Modifier.size(20.dp))
        }
        if (onUse != null) {
            Button(onClick = onUse, modifier = Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                Text(if (item.type == LootType.PANTS || item.type == LootType.ARMOR) "EQUIP" else "USE", style = MaterialTheme.typography.labelSmall)
            }
        }
        OutlinedButton(onClick = onAction, modifier = Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
            Text(actionLabel, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun LootSearchModal(
    loot: LootSearchResult,
    currentPantsLevel: Int,
    safeSpaceLeft: Int,
    playingCdId: Int?,
    onPlayCd: (Int) -> Unit,
    onTake: (InventoryItem, Boolean) -> Unit,
    onClose: () -> Unit
) {
    var inspectedLootItem by remember { mutableStateOf<InventoryItem?>(null) }

    ModalScrim(onDismiss = onClose) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = AmberSignal)
                    Column {
                        Text(loot.title.uppercase(), style = MaterialTheme.typography.headlineMedium, color = AmberSignal)
                        Text("Tap an item to inspect before looting", style = MaterialTheme.typography.labelSmall, color = EmeraldHud)
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close search", tint = Color.White)
                }
            }

            if (loot.items.isEmpty()) {
                Text("Nothing useful left inside.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(loot.items, key = { it.uid }) { item ->
                        val rarity = ItemRarityResolver.forInventoryItem(item)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BunkerSurface, RoundedCornerShape(8.dp))
                                .border(1.dp, rarity.color.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                                .clickable { inspectedLootItem = item }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(rarity.label, style = MaterialTheme.typography.labelSmall, color = rarity.color, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(item.name, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (item.detail.isNotBlank()) {
                                    Text(item.detail, style = MaterialTheme.typography.bodyMedium, color = EmeraldHud, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            IconButton(
                                onClick = { inspectedLootItem = item },
                                modifier = Modifier.size(48.dp).testTag("inspect_loot_btn_${item.uid}")
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Inspect ${item.name}", tint = AmberBright, modifier = Modifier.size(20.dp))
                            }
                            Button(
                                onClick = { onTake(item, false) },
                                modifier = Modifier.heightIn(min = 48.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (item.type == LootType.WEAPON || item.type == LootType.PANTS || item.type == LootType.ARMOR) "EQUIP" else "BAG",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            if (safeSpaceLeft > 0 && item.type != LootType.WEAPON && item.type != LootType.PANTS && item.type != LootType.ARMOR) {
                                OutlinedButton(
                                    onClick = { onTake(item, true) },
                                    modifier = Modifier.heightIn(min = 48.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("SAFE", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    inspectedLootItem?.let { item ->
        ItemInspectorModal(
            target = InspectTarget.LootItem(item = item, containerLabel = loot.title),
            currentPantsLevel = currentPantsLevel,
            safeSpaceLeft = safeSpaceLeft,
            playingCdId = playingCdId,
            onPlayCd = onPlayCd,
            onTakeLoot = onTake,
            onClose = { inspectedLootItem = null }
        )
    }
}

@Composable
fun ExtractionSummaryModal(summary: ExtractionSummary, onReturnToBase: () -> Unit) {
    ModalScrim(onDismiss = null) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("EXTRACTED TO OUTPOST ZERO", style = MaterialTheme.typography.displayLarge, color = EmeraldHud)
            Text("Exit: ${summary.exitName}", style = MaterialTheme.typography.titleMedium, color = AmberBright)
            HorizontalDivider(color = OliveBorder)
            Text("• Infected Cleared: ${summary.kills}", style = MaterialTheme.typography.bodyLarge)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("• Credits Banked:", style = MaterialTheme.typography.bodyLarge, color = AmberSignal)
                CredBadge(amount = summary.salvagedCredits)
            }
            Text("• Crafting Materials Banked: +${summary.extractedMaterials} units", style = MaterialTheme.typography.bodyLarge, color = CyanFloor)
            if (summary.extractedFood > 0 || summary.extractedWater > 0 || summary.extractedMeds > 0) {
                Text("• Supplies Banked: +${summary.extractedFood} Food, +${summary.extractedWater} Water, +${summary.extractedMeds} Meds", style = MaterialTheme.typography.bodyLarge, color = EmeraldHud)
            }
            Text("• New CDs Filed on Rack: ${summary.newCdsCount}", style = MaterialTheme.typography.bodyLarge, color = PurpleRare)
            Spacer(Modifier.height(6.dp))
            Button(
                onClick = onReturnToBase,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldHud, contentColor = Color.Black)
            ) {
                Text("ENTER OUTPOST ZERO", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OperatorDownModal(
    operator: OperatorProfile,
    credits: Int,
    onBuyBack: () -> Unit,
    onReturnToBase: () -> Unit
) {
    val fee = operator.currentBuyBackFee()
    val remainMs = operator.remainingPermadeathMs()
    val zoneLabel = com.example.game.model.DeploymentZone.fromId(operator.downedZoneId).title
    ModalScrim(onDismiss = null) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("${operator.callsign} IS DOWN IN DELAINE", style = MaterialTheme.typography.displayLarge, color = CrimsonAlert)
            Text(
                text = "Fell at $zoneLabel (${operator.downedAtX.toInt()}, ${operator.downedAtY.toInt()}). Rescue Window: ${formatRemainingHms(remainMs)} left before permanent loss.",
                style = MaterialTheme.typography.bodyMedium,
                color = AmberBright
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BunkerObsidian, RoundedCornerShape(10.dp))
                    .border(1.dp, OliveBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "• Safe Pockets (${operator.safePocketItems.size}): Untouchable — rival players cannot steal these.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyanFloor
                )
                Text(
                    "• Exposed Backpack (${operator.backpackItems.size}): Left on operator — scavengers can loot it while you wait!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFCA5A5)
                )
                if (operator.stolenItemNames.isNotEmpty()) {
                    Text(
                        "• Already Looted: ${operator.stolenItemNames.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CrimsonAlert
                    )
                }
                Text(
                    "• After 24h real time without CSAR buy-back, ${operator.callsign} dies permanently and remaining gear returns.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD5E3DA)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Escalating CSAR Fee (#${operator.kiaCount}):", style = MaterialTheme.typography.titleMedium, color = AmberSignal)
                CredBadge(amount = fee)
                Text("(Have $credits CRED)", style = MaterialTheme.typography.labelSmall, color = Color(0xFFBFD3C7))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                if (credits >= fee) {
                    Button(
                        onClick = onBuyBack,
                        modifier = Modifier.weight(1f).height(48.dp).testTag("op_down_buyback_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberSignal, contentColor = Color.Black)
                    ) {
                        Text("SEND RESCUE $fee CRED", fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(
                    onClick = onReturnToBase,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("op_down_return_btn")
                ) {
                    Text("RETURN TO BASE")
                }
            }
        }
    }
}

private fun formatRemainingHms(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val hrs = totalSec / 3600L
    val mins = (totalSec % 3600L) / 60L
    val secs = totalSec % 60L
    return "${hrs.toString().padStart(2, '0')}h ${mins.toString().padStart(2, '0')}m ${secs.toString().padStart(2, '0')}s"
}
