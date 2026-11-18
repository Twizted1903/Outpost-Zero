package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.model.GlassesStyle
import com.example.game.model.GlovesStyle
import com.example.game.model.HatStyle
import com.example.game.model.OperatorArchetype
import com.example.game.model.OperatorSex
import com.example.game.model.PantsStyle
import com.example.game.model.ShirtStyle
import com.example.game.model.ShoeStyle
import com.example.game.model.SkinToneOption
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSignal
import com.example.ui.theme.BunkerPanel
import com.example.ui.theme.BunkerSurface
import com.example.ui.theme.CyanFloor
import com.example.ui.theme.EmeraldHud
import com.example.ui.theme.OliveBorder
import com.example.ui.theme.pixelBevelFrame

enum class CreatorPickerCategory(val title: String, val subtitle: String) {
    SEX_SKIN("Sex & Skin Tone", "Body silhouette & skin complexion"),
    ARCHETYPE("Background", "Starting vitals & survivor role"),
    SHIRT("Shirt & K-9", "Top garment sets your paired K-9 dog"),
    PANTS("Pants", "Field trousers style & stitching"),
    SHOES("Shoes", "Boots or trail footwear"),
    HAT("Hat", "Caps, beanies & ranch hats"),
    GLASSES("Glasses", "Eyewear, shades & shop safety"),
    GLOVES("Gloves", "Bare fingers, fingerless or work leather")
}

@Composable
fun CreatorCategoryCard(
    category: CreatorPickerCategory,
    selectedLabel: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    previewDraw: DrawScope.() -> Unit
) {
    Box(
        modifier = modifier
            .heightIn(min = 78.dp)
            .pixelBevelFrame(
                fillColor = BunkerSurface,
                borderColor = accentColor.copy(alpha = 0.72f),
                borderWidth = 1.4.dp
            )
            .clickable(onClick = onClick)
            .testTag("creator_cat_${category.name}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, CyanFloor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            ) {
                previewDraw()
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = category.title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = selectedLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "TAP TO PICK",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanFloor
                )
            }
        }
    }
}

@Composable
fun CreatorVisualPickerDialog(
    category: CreatorPickerCategory,
    sex: OperatorSex,
    skinTone: SkinToneOption,
    archetype: OperatorArchetype,
    shirtStyle: ShirtStyle,
    pantsStyle: PantsStyle,
    shoeStyle: ShoeStyle,
    hatStyle: HatStyle,
    glassesStyle: GlassesStyle,
    glovesStyle: GlovesStyle,
    onSelectSex: (OperatorSex) -> Unit,
    onSelectSkinTone: (SkinToneOption) -> Unit,
    onSelectArchetype: (OperatorArchetype) -> Unit,
    onSelectShirt: (ShirtStyle) -> Unit,
    onSelectPants: (PantsStyle) -> Unit,
    onSelectShoe: (ShoeStyle) -> Unit,
    onSelectHat: (HatStyle) -> Unit,
    onSelectGlasses: (GlassesStyle) -> Unit,
    onSelectGloves: (GlovesStyle) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler { onDismiss() }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .heightIn(max = 640.dp)
                    .pixelBevelFrame(
                        fillColor = BunkerPanel,
                        borderColor = CyanFloor.copy(alpha = 0.8f),
                        borderWidth = 1.8.dp
                    ),
                shape = RoundedCornerShape(4.dp),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PICK ${category.title.uppercase()}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = AmberSignal
                            )
                            Text(
                                text = category.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB5C7BD)
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(48.dp).testTag("picker_close_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close picker", tint = Color.White)
                        }
                    }

                    HorizontalDivider(color = OliveBorder)

                    if (category == CreatorPickerCategory.SEX_SKIN) {
                        Text("SURVIVOR BUILD / SEX", style = MaterialTheme.typography.labelSmall, color = CyanFloor)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (option in OperatorSex.entries) {
                                FilterChip(
                                    selected = sex == option,
                                    onClick = { onSelectSex(option) },
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("creator_sex_${option.name}"),
                                    label = { Text(option.label) }
                                )
                            }
                        }
                        Text("SKIN COMPLEXION", style = MaterialTheme.typography.labelSmall, color = AmberBright)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .heightIn(max = 430.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (category) {
                            CreatorPickerCategory.SEX_SKIN -> {
                                val rows = SkinToneOption.entries.chunked(2)
                                items(rows) { pair ->
                                    PickerRowPair(pair) { tone ->
                                        VisualOptionCard(
                                            selected = skinTone == tone,
                                            title = tone.label,
                                            subtitle = "${sex.label} complexion",
                                            accent = Color(tone.colorHex),
                                            tag = "creator_skin_${tone.name}",
                                            onClick = { onSelectSkinTone(tone) }
                                        ) {
                                            drawSkinSexPreview(tone, sex, shirtStyle)
                                        }
                                    }
                                }
                            }
                            CreatorPickerCategory.ARCHETYPE -> {
                                val rows = OperatorArchetype.entries.chunked(2)
                                items(rows) { pair ->
                                    PickerRowPair(pair) { arch ->
                                        VisualOptionCard(
                                            selected = archetype == arch,
                                            title = arch.label,
                                            subtitle = "HP ${arch.maxHp} • Spd ${(arch.speedMul * 100).toInt()}% • ${arch.roleTitle}",
                                            accent = AmberBright,
                                            tag = "creator_arch_${arch.name}",
                                            onClick = { onSelectArchetype(arch); onDismiss() }
                                        ) {
                                            drawArchetypePreview(arch)
                                        }
                                    }
                                }
                            }
                            CreatorPickerCategory.SHIRT -> {
                                val rows = ShirtStyle.entries.chunked(2)
                                items(rows) { pair ->
                                    PickerRowPair(pair) { shirt ->
                                        VisualOptionCard(
                                            selected = shirtStyle == shirt,
                                            title = shirt.label,
                                            subtitle = shirt.description,
                                            badgeText = "K-9: ${shirt.pairedBreed}",
                                            accent = Color(shirt.primaryHex),
                                            tag = "creator_shirt_${shirt.name}",
                                            onClick = { onSelectShirt(shirt); onDismiss() }
                                        ) {
                                            drawShirtPreview(shirt)
                                        }
                                    }
                                }
                            }
                            CreatorPickerCategory.PANTS -> {
                                val rows = PantsStyle.entries.chunked(2)
                                items(rows) { pair ->
                                    PickerRowPair(pair) { pants ->
                                        VisualOptionCard(
                                            selected = pantsStyle == pants,
                                            title = pants.label,
                                            subtitle = pants.description,
                                            accent = Color(pants.trimHex),
                                            tag = "creator_pants_${pants.name}",
                                            onClick = { onSelectPants(pants); onDismiss() }
                                        ) {
                                            drawPantsPreview(pants)
                                        }
                                    }
                                }
                            }
                            CreatorPickerCategory.SHOES -> {
                                val rows = ShoeStyle.entries.chunked(2)
                                items(rows) { pair ->
                                    PickerRowPair(pair) { shoe ->
                                        VisualOptionCard(
                                            selected = shoeStyle == shoe,
                                            title = shoe.label,
                                            subtitle = shoe.description,
                                            accent = Color(shoe.soleHex),
                                            tag = "creator_shoe_${shoe.name}",
                                            onClick = { onSelectShoe(shoe); onDismiss() }
                                        ) {
                                            drawShoePreview(shoe)
                                        }
                                    }
                                }
                            }
                            CreatorPickerCategory.HAT -> {
                                val rows = HatStyle.entries.chunked(2)
                                items(rows) { pair ->
                                    PickerRowPair(pair) { hat ->
                                        VisualOptionCard(
                                            selected = hatStyle == hat,
                                            title = hat.label,
                                            subtitle = hat.description,
                                            accent = if (hat == HatStyle.NONE) CyanFloor else Color(hat.accentHex),
                                            tag = "creator_hat_${hat.name}",
                                            onClick = { onSelectHat(hat); onDismiss() }
                                        ) {
                                            drawHatPreview(hat, skinTone)
                                        }
                                    }
                                }
                            }
                            CreatorPickerCategory.GLASSES -> {
                                val rows = GlassesStyle.entries.chunked(2)
                                items(rows) { pair ->
                                    PickerRowPair(pair) { gl ->
                                        VisualOptionCard(
                                            selected = glassesStyle == gl,
                                            title = gl.label,
                                            subtitle = gl.description,
                                            accent = if (gl == GlassesStyle.NONE) CyanFloor else Color(gl.frameHex),
                                            tag = "creator_glasses_${gl.name}",
                                            onClick = { onSelectGlasses(gl); onDismiss() }
                                        ) {
                                            drawGlassesPreview(gl, skinTone)
                                        }
                                    }
                                }
                            }
                            CreatorPickerCategory.GLOVES -> {
                                val rows = GlovesStyle.entries.chunked(2)
                                items(rows) { pair ->
                                    PickerRowPair(pair) { gv ->
                                        VisualOptionCard(
                                            selected = glovesStyle == gv,
                                            title = gv.label,
                                            subtitle = gv.description,
                                            accent = if (gv == GlovesStyle.NONE) Color(skinTone.colorHex) else Color(gv.colorHex),
                                            tag = "creator_gloves_${gv.name}",
                                            onClick = { onSelectGloves(gv); onDismiss() }
                                        ) {
                                            drawGlovesPreview(gv, skinTone)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.5.dp, Color(0xFF06080C), RoundedCornerShape(4.dp))
                            .testTag("picker_done_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberSignal, contentColor = Color(0xFF06080C))
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("DONE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> PickerRowPair(
    pair: List<T>,
    card: @Composable (T) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            card(pair[0])
        }
        if (pair.size > 1) {
            Box(modifier = Modifier.weight(1f)) {
                card(pair[1])
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun VisualOptionCard(
    selected: Boolean,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    accent: Color,
    tag: String,
    onClick: () -> Unit,
    previewDraw: DrawScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 156.dp)
            .pixelBevelFrame(
                fillColor = if (selected) Color(0xFF102824) else BunkerSurface,
                borderColor = if (selected) EmeraldHud else accent.copy(alpha = 0.55f),
                borderWidth = if (selected) 2.dp else 1.2.dp
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag(tag),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, CyanFloor.copy(alpha = 0.38f), RoundedCornerShape(4.dp))
            ) {
                previewDraw()
            }
            if (selected) {
                Text(
                    text = "EQUIPPED",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(EmeraldHud, RoundedCornerShape(2.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = if (selected) EmeraldHud else Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (badgeText != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Pets, contentDescription = null, tint = AmberBright, modifier = Modifier.size(12.dp))
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberBright,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFA7C2CF),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ============================================================
// Dedicated Inked Graphic-Novel Previews for Every Option
// ============================================================

fun DrawScope.drawSkinSexPreview(tone: SkinToneOption, sex: OperatorSex, shirt: ShirtStyle) {
    NoirCharacterPortraitRenderer.run { drawNoirSkinSexPreview(tone, sex, shirt) }
}

fun DrawScope.drawArchetypePreview(arch: OperatorArchetype) {
    NoirCharacterPortraitRenderer.run { drawNoirArchetypePreview(arch) }
}

fun DrawScope.drawShirtPreview(shirt: ShirtStyle) {
    NoirCharacterPortraitRenderer.run { drawNoirShirtPreview(shirt) }
}

fun DrawScope.drawPantsPreview(pants: PantsStyle) {
    NoirCharacterPortraitRenderer.run { drawNoirPantsPreview(pants) }
}

fun DrawScope.drawShoePreview(shoe: ShoeStyle) {
    NoirCharacterPortraitRenderer.run { drawNoirShoePreview(shoe) }
}

fun DrawScope.drawHatPreview(hat: HatStyle, skinTone: SkinToneOption) {
    NoirCharacterPortraitRenderer.run { drawNoirHatPreview(hat, skinTone) }
}

fun DrawScope.drawGlassesPreview(glasses: GlassesStyle, skinTone: SkinToneOption) {
    NoirCharacterPortraitRenderer.run { drawNoirGlassesPreview(glasses, skinTone) }
}

fun DrawScope.drawGlovesPreview(gloves: GlovesStyle, skinTone: SkinToneOption) {
    NoirCharacterPortraitRenderer.run { drawNoirGlovesPreview(gloves, skinTone) }
}
