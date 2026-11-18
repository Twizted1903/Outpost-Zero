package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.game.model.ArmorTier
import com.example.game.model.AttachmentSlot
import com.example.game.model.CdCatalog
import com.example.game.model.CdRarity
import com.example.game.model.CraftOutputKind
import com.example.game.model.CraftingMaterial
import com.example.game.model.CraftingRecipe
import com.example.game.model.InventoryItem
import com.example.game.model.LootType
import com.example.game.model.WeaponAttachment
import com.example.game.model.WeaponAttachmentCatalog
import com.example.game.model.WeaponCategory
import com.example.game.model.WeaponEra
import com.example.game.model.WeaponId
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSignal
import com.example.ui.theme.BunkerObsidian
import com.example.ui.theme.BunkerSurface
import com.example.ui.theme.CyanFloor
import com.example.ui.theme.EmeraldHud
import com.example.ui.theme.OliveBorder
import com.example.ui.theme.PurpleRare
import kotlin.math.PI
import kotlin.math.sin

data class ItemRarityInfo(
    val label: String,
    val color: Color
)

object ItemRarityResolver {
    fun forWeapon(weapon: WeaponId): ItemRarityInfo = ItemRarityInfo(
        label = weapon.rarityLabel,
        color = Color(weapon.rarityColorHex)
    )

    fun forArmor(level: Int): ItemRarityInfo {
        val tier = ArmorTier.fromLevel(level)
        return ItemRarityInfo(tier.rarityLabel, Color(tier.colorHex))
    }

    fun forPants(level: Int): ItemRarityInfo = when {
        level >= 4 -> ItemRarityInfo("GRAIL // SPEC-OPS", PurpleRare)
        level == 3 -> ItemRarityInfo("RARE // TACTICAL", CyanFloor)
        level == 2 -> ItemRarityInfo("UNCOMMON // WORK", EmeraldHud)
        else -> ItemRarityInfo("COMMON // STANDARD", Color(0xFF9CA3AF))
    }

    fun forCdRarity(rarity: CdRarity): ItemRarityInfo = ItemRarityInfo(
        label = rarity.label.uppercase(),
        color = Color(rarity.colorHex)
    )

    fun forMaterial(materialKey: String?): ItemRarityInfo {
        val mat = CraftingMaterial.fromKey(materialKey)
        return if (mat != null) {
            ItemRarityInfo(mat.rarityLabel, Color(mat.colorHex))
        } else {
            ItemRarityInfo("COMMON // MATERIAL", Color(0xFF9CA3AF))
        }
    }

    fun forRecipe(recipe: CraftingRecipe): ItemRarityInfo = ItemRarityInfo(
        label = recipe.rarityLabel,
        color = Color(recipe.rarityColorHex)
    )

    fun forRarityLabel(label: String): ItemRarityInfo {
        val upper = label.uppercase()
        val color = when {
            upper.contains("GRAIL") -> PurpleRare
            upper.contains("RARE") -> CyanFloor
            upper.contains("UNCOMMON") -> EmeraldHud
            else -> Color(0xFF9CA3AF)
        }
        return ItemRarityInfo(upper.ifBlank { "COMMON" }, color)
    }

    fun forInventoryItem(item: InventoryItem): ItemRarityInfo = when (item.type) {
        LootType.CD -> {
            val cd = item.cdId?.let { CdCatalog.getById(it) }
            if (cd != null) forCdRarity(cd.rarity) else ItemRarityInfo("COMMON", Color(0xFF9CA3AF))
        }
        LootType.WEAPON -> forWeapon(WeaponId.fromKey(item.weaponKey ?: "knife"))
        LootType.PANTS -> forPants(item.pantsLevel ?: 1)
        LootType.ARMOR -> forArmor(item.armorLevel ?: 1)
        LootType.MATERIAL -> forMaterial(item.materialKey)
        LootType.MEDKIT -> if (item.value >= 60) ItemRarityInfo("RARE // TRAUMA", CyanFloor)
        else ItemRarityInfo("COMMON // FIELD", Color(0xFF9CA3AF))
        LootType.SALVAGE -> when {
            item.value >= 150 -> ItemRarityInfo("GRAIL // HIGH-VALUE", PurpleRare)
            item.value >= 90 -> ItemRarityInfo("RARE // TECH", CyanFloor)
            item.value >= 55 -> ItemRarityInfo("UNCOMMON // GEAR", EmeraldHud)
            else -> ItemRarityInfo("COMMON // SCRAP", Color(0xFF9CA3AF))
        }
        LootType.FOOD, LootType.WATER -> if (item.value >= 40) ItemRarityInfo("UNCOMMON // SEALED", EmeraldHud)
        else ItemRarityInfo("COMMON // RATION", Color(0xFF9CA3AF))
        LootType.ATTACHMENT -> {
            val att = WeaponAttachmentCatalog.fromKey(item.attachmentKey)
            if (att != null) ItemRarityInfo(att.rarityLabel, Color(att.rarityColorHex))
            else ItemRarityInfo("UNCOMMON // MOD PART", EmeraldHud)
        }
    }
}

@Composable
fun ItemSprite3DViewport(
    target: InspectTarget,
    rarity: ItemRarityInfo,
    modifier: Modifier = Modifier,
    equippedAttachmentKeys: Set<String> = emptySet()
) {
    val zoomSteps = listOf(1.0f, 1.45f, 2.0f)
    var zoomIdx by remember(target) { mutableIntStateOf(0) }
    var autoSpin by remember(target) { mutableStateOf(true) }
    var dragRotY by remember(target) { mutableFloatStateOf(0f) }
    var dragRotX by remember(target) { mutableFloatStateOf(8f) }

    val infiniteTransition = rememberInfiniteTransition(label = "item_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = -28f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotY_orbit"
    )

    val effectiveRotY = if (autoSpin) dragRotY + spinAngle else dragRotY
    val effectiveRotX = dragRotX.coerceIn(-26f, 26f)
    val zoomScale = zoomSteps[zoomIdx]

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BunkerObsidian, RoundedCornerShape(12.dp))
            .border(1.5.dp, rarity.color.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(166.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(rarity.color.copy(alpha = 0.22f), Color(0xFF0A0F0C))
                    )
                )
                .pointerInput(target) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        autoSpin = false
                        dragRotY = (dragRotY + dragAmount.x * 0.55f) % 360f
                        dragRotX = (dragRotX - dragAmount.y * 0.35f).coerceIn(-28f, 28f)
                    }
                }
                .testTag("inspect_3d_stage"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height * 0.78f
                drawOval(
                    color = rarity.color.copy(alpha = 0.22f),
                    topLeft = Offset(cx - 88f * zoomScale, cy - 16f),
                    size = Size(176f * zoomScale, 32f)
                )
                drawOval(
                    color = rarity.color.copy(alpha = 0.45f),
                    topLeft = Offset(cx - 64f * zoomScale, cy - 11f),
                    size = Size(128f * zoomScale, 22f),
                    style = Stroke(width = 1.5f)
                )
            }

            Canvas(
                modifier = Modifier
                    .size(136.dp)
                    .graphicsLayer {
                        rotationY = effectiveRotY
                        rotationX = effectiveRotX
                        scaleX = zoomScale
                        scaleY = zoomScale
                        cameraDistance = 12f * density
                    }
            ) {
                val parallax = sin(effectiveRotY * PI.toFloat() / 180f) * 8f
                drawItemSprite(target, rarity.color, parallax, equippedAttachmentKeys)
            }

            Text(
                text = rarity.label,
                style = MaterialTheme.typography.labelSmall,
                color = rarity.color,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(BunkerObsidian.copy(alpha = 0.86f), RoundedCornerShape(4.dp))
                    .border(1.dp, rarity.color.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )

            Text(
                text = "DRAG TO ROTATE • ${(zoomScale * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = AmberBright.copy(alpha = 0.8f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = { if (zoomIdx > 0) zoomIdx-- },
                    enabled = zoomIdx > 0,
                    modifier = Modifier
                        .size(48.dp)
                        .background(BunkerSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, OliveBorder, RoundedCornerShape(8.dp))
                        .testTag("inspect_zoom_out_btn")
                ) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom out sprite", tint = Color.White)
                }
                IconButton(
                    onClick = { if (zoomIdx < zoomSteps.lastIndex) zoomIdx++ },
                    enabled = zoomIdx < zoomSteps.lastIndex,
                    modifier = Modifier
                        .size(48.dp)
                        .background(BunkerSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, OliveBorder, RoundedCornerShape(8.dp))
                        .testTag("inspect_zoom_in_btn")
                ) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom in sprite", tint = AmberSignal)
                }
            }

            OutlinedButton(
                onClick = {
                    autoSpin = !autoSpin
                    if (autoSpin) {
                        dragRotY = 0f
                        dragRotX = 8f
                    }
                },
                modifier = Modifier.height(48.dp).testTag("inspect_spin_btn"),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null, tint = if (autoSpin) EmeraldHud else Color.White)
                Text(
                    text = if (autoSpin) " AUTO-SPIN ON" else " AUTO-SPIN OFF",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (autoSpin) EmeraldHud else Color.White
                )
            }
        }
    }
}

private fun DrawScope.drawItemSprite(
    target: InspectTarget,
    accent: Color,
    parallax: Float,
    equippedAttachmentKeys: Set<String> = emptySet()
) {
    when (target) {
        is InspectTarget.BinderCd -> drawCdCaseSprite(target.cd.coverPrimaryHex, target.cd.coverAccentHex, parallax)
        is InspectTarget.EquippedWeapon -> drawWeaponSprite(target.weapon, parallax, equippedAttachmentKeys)
        is InspectTarget.EquippedPants -> drawPantsSprite(target.level, parallax)
        is InspectTarget.EquippedArmor -> drawArmorSprite(target.tier.level, parallax)
        is InspectTarget.BagItem -> drawInventorySprite(target.item, accent, parallax)
        is InspectTarget.LootItem -> drawInventorySprite(target.item, accent, parallax)
        is InspectTarget.CraftRecipePreview -> drawRecipeSprite(target.recipe, accent, parallax)
        is InspectTarget.MarketItem -> drawInventorySprite(target.listing.toInventoryItemPreview(), accent, parallax)
        is InspectTarget.MaterialPreview -> drawMaterialSprite(target.material, accent, parallax)
        is InspectTarget.AttachmentItem -> drawAttachmentSprite(target.attachment, parallax)
    }
}

private fun DrawScope.drawRecipeSprite(recipe: CraftingRecipe, accent: Color, parallax: Float) {
    when (recipe.outputKind) {
        CraftOutputKind.WEAPON -> drawWeaponSprite(WeaponId.fromKey(recipe.weaponKey ?: "knife"), parallax)
        CraftOutputKind.PANTS -> drawPantsSprite(recipe.pantsLevel ?: 2, parallax)
        CraftOutputKind.ARMOR -> drawArmorSprite(recipe.armorLevel ?: 1, parallax)
        CraftOutputKind.AMMO -> drawAmmoCrateSprite(WeaponId.fromKey(recipe.weaponKey ?: "m4"), parallax)
        CraftOutputKind.ATTACHMENT -> {
            val att = WeaponAttachmentCatalog.fromKey(recipe.attachmentKey)
            if (att != null) drawAttachmentSprite(att, parallax) else drawSalvageSprite(accent, parallax)
        }
        CraftOutputKind.CONSUMABLE -> when (recipe.consumableType) {
            LootType.FOOD -> drawFoodSprite(parallax)
            LootType.WATER -> drawWaterSprite(parallax)
            else -> drawMedkitSprite(parallax)
        }
    }
}

private fun DrawScope.drawInventorySprite(item: InventoryItem, accent: Color, parallax: Float) {
    when {
        item.type == LootType.CD && item.cdId != null -> {
            val cd = CdCatalog.getById(item.cdId)
            drawCdCaseSprite(cd.coverPrimaryHex, cd.coverAccentHex, parallax)
        }
        item.type == LootType.WEAPON -> drawWeaponSprite(WeaponId.fromKey(item.weaponKey ?: "knife"), parallax)
        item.type == LootType.PANTS -> drawPantsSprite(item.pantsLevel ?: 1, parallax)
        item.type == LootType.ARMOR -> drawArmorSprite(item.armorLevel ?: 1, parallax)
        item.type == LootType.FOOD -> drawFoodSprite(parallax)
        item.type == LootType.WATER -> drawWaterSprite(parallax)
        item.type == LootType.MEDKIT -> drawMedkitSprite(parallax)
        item.type == LootType.MATERIAL -> drawMaterialSprite(CraftingMaterial.fromKey(item.materialKey), accent, parallax)
        item.type == LootType.ATTACHMENT -> {
            val att = WeaponAttachmentCatalog.fromKey(item.attachmentKey)
            if (att != null) drawAttachmentSprite(att, parallax) else drawSalvageSprite(accent, parallax)
        }
        else -> drawSalvageSprite(accent, parallax)
    }
}

private fun DrawScope.drawArmorSprite(level: Int, parallax: Float) {
    val tier = ArmorTier.fromLevel(level)
    val cx = size.width / 2f
    val cy = size.height / 2f
    val bodyCol = Color(tier.vestTrimHex)
    val accent = Color(tier.colorHex)

    // Back shadow depth
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(cx - 38f + parallax * 0.5f, cy - 42f + 5f),
        size = Size(76f, 84f),
        cornerRadius = CornerRadius(12f, 12f)
    )
    // Shoulder straps
    drawRect(Color(0xFF1F2937), topLeft = Offset(cx - 30f, cy - 46f), size = Size(16f, 18f))
    drawRect(Color(0xFF1F2937), topLeft = Offset(cx + 14f, cy - 46f), size = Size(16f, 18f))
    // Main tactical carrier vest
    drawRoundRect(
        color = bodyCol,
        topLeft = Offset(cx - 38f, cy - 36f),
        size = Size(76f, 76f),
        cornerRadius = CornerRadius(10f, 10f)
    )
    // Ceramic chest plate
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(cx - 24f, cy - 26f),
        size = Size(48f, 44f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = accent,
        topLeft = Offset(cx - 24f, cy - 26f),
        size = Size(48f, 44f),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = 2.2f)
    )
    // MOLLE cummerbund webbing pouches
    for (i in -1..1) {
        drawRect(Color(0xFF334155), topLeft = Offset(cx + i * 20f - 7f, cy + 20f), size = Size(14f, 14f))
    }
}

private fun DrawScope.drawMaterialSprite(material: CraftingMaterial?, accent: Color, parallax: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val px = parallax * 0.5f
    when (material) {
        CraftingMaterial.SCRAP_METAL, CraftingMaterial.INDUSTRIAL_FASTENERS -> {
            drawRect(color = Color(0xFF1E293B), topLeft = Offset(cx - 38f + px, cy - 22f + 5f), size = Size(76f, 44f))
            drawRect(color = Color(0xFF64748B), topLeft = Offset(cx - 38f, cy - 22f), size = Size(76f, 44f))
            drawRect(color = Color(0xFFCBD5E1), topLeft = Offset(cx - 28f, cy - 12f), size = Size(56f, 10f))
            drawCircle(color = Color(0xFFE2E8F0), radius = 5.5f, center = Offset(cx - 18f, cy + 10f))
            drawCircle(color = Color(0xFFE2E8F0), radius = 5.5f, center = Offset(cx + 18f, cy + 10f))
        }
        CraftingMaterial.FORGED_RECEIVER, CraftingMaterial.TRIGGER_ASSEMBLY -> {
            drawRoundRect(Color(0xFF0F172A), Offset(cx - 42f + px, cy - 20f + 5f), Size(84f, 42f), CornerRadius(6f, 6f))
            drawRoundRect(Color(0xFF475569), Offset(cx - 42f, cy - 20f), Size(84f, 34f), CornerRadius(6f, 6f))
            drawRect(Color(0xFF94A3B8), Offset(cx - 30f, cy - 12f), Size(48f, 10f))
            drawRect(Color(0xFF1E293B), Offset(cx - 8f, cy + 12f), Size(22f, 16f))
            drawCircle(Color(0xFF60A5FA), radius = 4f, center = Offset(cx + 22f, cy - 4f))
        }
        CraftingMaterial.RIFLED_BARREL, CraftingMaterial.SUPPRESSOR_BAFFLES -> {
            val isBaffle = material == CraftingMaterial.SUPPRESSOR_BAFFLES
            drawRoundRect(Color(0xFF0F172A), Offset(cx - 48f + px, cy - 11f + 5f), Size(96f, 22f), CornerRadius(8f, 8f))
            drawRoundRect(Color(if (isBaffle) 0xFF065F46 else 0xFF334155), Offset(cx - 48f, cy - 11f), Size(96f, 22f), CornerRadius(8f, 8f))
            drawRect(Color(if (isBaffle) 0xFF34D399 else 0xFF93C5FD), Offset(cx - 40f, cy - 4f), Size(80f, 6f))
            for (i in -2..2) {
                drawLine(Color(0xFF0F172A), Offset(cx + i * 14f, cy - 11f), Offset(cx + i * 14f, cy + 11f), strokeWidth = 2f)
            }
        }
        CraftingMaterial.GUNPOWDER -> {
            drawRoundRect(color = Color(0xFF7C2D12), topLeft = Offset(cx - 26f + px, cy - 34f + 5f), size = Size(52f, 70f), cornerRadius = CornerRadius(8f, 8f))
            drawRoundRect(color = Color(0xFFEA580C), topLeft = Offset(cx - 26f, cy - 34f), size = Size(52f, 70f), cornerRadius = CornerRadius(8f, 8f))
            drawRect(color = Color(0xFF1F2937), topLeft = Offset(cx - 14f, cy - 42f), size = Size(28f, 10f))
            drawRect(color = Color(0xFFFDE047), topLeft = Offset(cx - 18f, cy - 12f), size = Size(36f, 18f))
        }
        CraftingMaterial.BRASS_CASINGS, CraftingMaterial.PERCUSSION_PRIMERS -> {
            val isPrimer = material == CraftingMaterial.PERCUSSION_PRIMERS
            if (isPrimer) {
                drawRoundRect(Color(0xFF78350F), Offset(cx - 34f + px, cy - 28f + 5f), Size(68f, 56f), CornerRadius(8f, 8f))
                drawRoundRect(Color(0xFF92400E), Offset(cx - 34f, cy - 28f), Size(68f, 56f), CornerRadius(8f, 8f))
                for (r in -1..1) for (c in -2..2) {
                    drawCircle(Color(0xFFFBBF24), radius = 4.2f, center = Offset(cx + c * 11f, cy + r * 14f))
                }
            } else {
                for (i in -1..1) {
                    val bx = cx + i * 22f
                    drawRect(Color(0xFF78350F), Offset(bx - 7f + px * 0.5f, cy - 26f + 4f), Size(14f, 54f))
                    drawRect(Color(0xFFFBBF24), Offset(bx - 7f, cy - 26f), Size(14f, 54f))
                    drawRect(Color(0xFFFDE68A), Offset(bx - 4f, cy - 22f), Size(4f, 44f))
                    drawRect(Color(0xFFD97706), Offset(bx - 9f, cy + 22f), Size(18f, 6f))
                }
            }
        }
        CraftingMaterial.LEAD_PROJECTILES, CraftingMaterial.COPPER_JACKETS -> {
            val col = if (material == CraftingMaterial.COPPER_JACKETS) Color(0xFFD97706) else Color(0xFF64748B)
            val hiCol = if (material == CraftingMaterial.COPPER_JACKETS) Color(0xFFFDBA74) else Color(0xFFCBD5E1)
            for (i in -1..1) {
                val bx = cx + i * 22f
                drawCircle(col, radius = 9f, center = Offset(bx, cy - 16f))
                drawRect(Color(0xFF1F2937), Offset(bx - 9f + px * 0.4f, cy - 16f + 4f), Size(18f, 38f))
                drawRect(col, Offset(bx - 9f, cy - 16f), Size(18f, 38f))
                drawRect(hiCol, Offset(bx - 5f, cy - 12f), Size(4f, 30f))
            }
        }
        CraftingMaterial.POLYMER_STOCK, CraftingMaterial.WOOD_FURNITURE -> {
            val dark = if (material == CraftingMaterial.WOOD_FURNITURE) Color(0xFF451A03) else Color(0xFF1E293B)
            val mid = if (material == CraftingMaterial.WOOD_FURNITURE) Color(0xFF92400E) else Color(0xFF475569)
            val grain = if (material == CraftingMaterial.WOOD_FURNITURE) Color(0xFFD97706) else Color(0xFF64748B)
            drawRoundRect(dark, Offset(cx - 42f + px, cy - 22f + 5f), Size(84f, 44f), CornerRadius(8f, 8f))
            drawRoundRect(mid, Offset(cx - 42f, cy - 22f), Size(84f, 44f), CornerRadius(8f, 8f))
            drawRect(grain, Offset(cx - 32f, cy - 10f), Size(64f, 5f))
            drawRect(grain, Offset(cx - 26f, cy + 4f), Size(52f, 4f))
        }
        CraftingMaterial.OPTICAL_GLASS -> {
            drawCircle(Color(0xFF0F172A), radius = 38f, center = Offset(cx + px * 0.5f, cy + 5f))
            drawCircle(Color(0xFF1E293B), radius = 38f, center = Offset(cx, cy))
            drawCircle(Color(0xFF0284C7), radius = 29f, center = Offset(cx, cy))
            drawCircle(Color(0xFF38BDF8), radius = 20f, center = Offset(cx - 4f, cy - 4f))
            drawCircle(Color.White.copy(alpha = 0.65f), radius = 7f, center = Offset(cx - 10f, cy - 10f))
        }
        CraftingMaterial.TACTICAL_CLOTH, CraftingMaterial.KEVLAR_WEAVE, CraftingMaterial.CERAMIC_PLATES -> {
            val baseCol = Color(material.colorHex)
            drawRoundRect(Color(0xFF111827), Offset(cx - 36f + px, cy - 26f + 5f), Size(72f, 52f), CornerRadius(10f, 10f))
            drawRoundRect(baseCol.copy(alpha = 0.78f), Offset(cx - 36f, cy - 26f), Size(72f, 52f), CornerRadius(10f, 10f))
            drawRect(baseCol, Offset(cx - 36f, cy - 8f), Size(72f, 14f))
            drawRect(Color.White.copy(alpha = 0.35f), Offset(cx - 12f, cy - 26f), Size(8f, 52f))
        }
        CraftingMaterial.ELECTRONIC_PARTS, CraftingMaterial.COPPER_WIRING, CraftingMaterial.LITHIUM_CELLS -> {
            if (material == CraftingMaterial.LITHIUM_CELLS) {
                drawRoundRect(Color(0xFF0F172A), Offset(cx - 26f + px, cy - 34f + 5f), Size(52f, 68f), CornerRadius(8f, 8f))
                drawRoundRect(Color(0xFF0891B2), Offset(cx - 26f, cy - 34f), Size(52f, 68f), CornerRadius(8f, 8f))
                drawRect(Color(0xFF22D3EE), Offset(cx - 18f, cy - 10f), Size(36f, 22f))
                drawRect(Color(0xFFCBD5E1), Offset(cx - 10f, cy - 40f), Size(20f, 8f))
            } else {
                drawRect(color = Color(0xFF14532D), topLeft = Offset(cx - 36f + px, cy - 28f + 5f), size = Size(72f, 56f))
                drawRect(color = Color(if (material == CraftingMaterial.COPPER_WIRING) 0xFF9A3412 else 0xFF166534), topLeft = Offset(cx - 36f, cy - 28f), size = Size(72f, 56f))
                drawRect(color = Color(0xFF1E293B), topLeft = Offset(cx - 16f, cy - 14f), size = Size(32f, 28f))
                drawRect(color = Color(0xFFFACC15), topLeft = Offset(cx + 18f, cy + 10f), size = Size(12f, 8f))
            }
        }
        CraftingMaterial.GUN_LUBE -> {
            drawRoundRect(Color(0xFF451A03), Offset(cx - 22f + px, cy - 30f + 5f), Size(44f, 64f), CornerRadius(8f, 8f))
            drawRoundRect(Color(0xFFD97706), Offset(cx - 22f, cy - 30f), Size(44f, 64f), CornerRadius(8f, 8f))
            drawRect(Color(0xFF1F2937), Offset(cx - 8f, cy - 42f), Size(16f, 14f))
            drawRect(Color(0xFFFEF08A), Offset(cx - 14f, cy - 8f), Size(28f, 16f))
        }
        CraftingMaterial.MEDICAL_HERBS, CraftingMaterial.TRAUMA_GAUZE, CraftingMaterial.ANTISEPTIC_SOL, null -> {
            val baseCol = when (material) {
                CraftingMaterial.TRAUMA_GAUZE -> Color(0xFF475569)
                CraftingMaterial.ANTISEPTIC_SOL -> Color(0xFF065F46)
                else -> Color(0xFF059669)
            }
            drawRoundRect(color = Color(0xFF064E3B), topLeft = Offset(cx - 28f + px, cy - 32f + 5f), size = Size(56f, 66f), cornerRadius = CornerRadius(10f, 10f))
            drawRoundRect(color = baseCol, topLeft = Offset(cx - 28f, cy - 32f), size = Size(56f, 66f), cornerRadius = CornerRadius(10f, 10f))
            drawRect(color = Color.White, topLeft = Offset(cx - 4f, cy - 14f), size = Size(8f, 28f))
            drawRect(color = Color.White, topLeft = Offset(cx - 14f, cy - 4f), size = Size(28f, 8f))
        }
    }
}

private fun DrawScope.drawAmmoCrateSprite(weapon: WeaponId, parallax: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    drawRoundRect(color = Color(0xFF1F2937), topLeft = Offset(cx - 38f + parallax * 0.5f, cy - 24f + 5f), size = Size(76f, 52f), cornerRadius = CornerRadius(6f, 6f))
    drawRoundRect(color = Color(0xFF3F4E3A), topLeft = Offset(cx - 38f, cy - 24f), size = Size(76f, 52f), cornerRadius = CornerRadius(6f, 6f))
    drawRect(color = Color(weapon.tracerColorHex), topLeft = Offset(cx - 28f, cy - 8f), size = Size(56f, 12f))
    drawRect(color = Color(0xFFFBBF24), topLeft = Offset(cx - 18f, cy + 10f), size = Size(6f, 12f))
    drawRect(color = Color(0xFFFBBF24), topLeft = Offset(cx - 6f, cy + 10f), size = Size(6f, 12f))
    drawRect(color = Color(0xFFFBBF24), topLeft = Offset(cx + 6f, cy + 10f), size = Size(6f, 12f))
}

private fun DrawScope.drawWeaponSprite(
    weapon: WeaponId,
    parallax: Float,
    equippedAttachmentKeys: Set<String> = emptySet()
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val depthOffset = Offset(parallax * 0.55f, 5f)
    val accentCol = Color(weapon.accentColorHex)

    if (weapon.isMelee) {
        when (weapon) {
            WeaponId.KNIFE -> {
                drawRoundRect(Color(0xFF0F172A), Offset(cx - 40f + depthOffset.x, cy - 6f + depthOffset.y), Size(82f, 12f), CornerRadius(4f, 4f))
                drawRect(Color(0xFF78350F), Offset(cx - 40f, cy - 6f), Size(28f, 12f))
                drawRect(Color(0xFFE2E8F0), Offset(cx - 12f, cy - 6f), Size(48f, 10f))
                drawRect(Color(0xFF94A3B8), Offset(cx - 12f, cy + 1f), Size(44f, 3f))
            }
            WeaponId.KATANA -> {
                drawRect(Color(0xFF0F172A), Offset(cx - 56f + depthOffset.x, cy - 4f + depthOffset.y), Size(112f, 8f))
                drawRect(Color(0xFF1E293B), Offset(cx - 56f, cy - 5f), Size(28f, 10f))
                drawRect(Color(0xFFF59E0B), Offset(cx - 29f, cy - 11f), Size(5f, 22f))
                drawRect(Color(0xFFE2E8F0), Offset(cx - 24f, cy - 4f), Size(80f, 7f))
            }
            WeaponId.FIRE_AXE, WeaponId.TOMAHAWK -> {
                val headCol = if (weapon == WeaponId.FIRE_AXE) Color(0xFFDC2626) else Color(0xFF475569)
                drawRect(Color(0xFF0F172A), Offset(cx - 52f + depthOffset.x, cy - 4f + depthOffset.y), Size(104f, 8f))
                drawRect(Color(0xFF78350F), Offset(cx - 52f, cy - 4f), Size(92f, 8f))
                drawRoundRect(headCol, Offset(cx + 14f, cy - 22f), Size(28f, 36f), CornerRadius(6f, 6f))
                drawRect(Color(0xFFE2E8F0), Offset(cx + 38f, cy - 22f), Size(6f, 36f))
            }
            WeaponId.SLEDGEHAMMER -> {
                drawRect(Color(0xFF0F172A), Offset(cx - 52f + depthOffset.x, cy - 4f + depthOffset.y), Size(104f, 8f))
                drawRect(Color(0xFF78350F), Offset(cx - 52f, cy - 4f), Size(88f, 8f))
                drawRoundRect(Color(0xFF475569), Offset(cx + 18f, cy - 20f), Size(26f, 40f), CornerRadius(4f, 4f))
            }
            WeaponId.CROWBAR -> {
                drawRoundRect(Color(0xFF0F172A), Offset(cx - 50f + depthOffset.x, cy - 5f + depthOffset.y), Size(100f, 10f), CornerRadius(4f, 4f))
                drawRoundRect(Color(0xFFB91C1C), Offset(cx - 50f, cy - 5f), Size(92f, 10f), CornerRadius(4f, 4f))
                drawRect(Color(0xFFE2E8F0), Offset(cx + 34f, cy - 14f), Size(14f, 18f))
            }
            else -> {
                drawRoundRect(Color(0xFF0F172A), Offset(cx - 50f + depthOffset.x, cy - 8f + depthOffset.y), Size(100f, 16f), CornerRadius(6f, 6f))
                drawRoundRect(accentCol, Offset(cx - 50f, cy - 8f), Size(100f, 15f), CornerRadius(6f, 6f))
                drawRect(Color(0xFF1F2937), Offset(cx - 50f, cy - 9f), Size(32f, 17f))
                drawRect(Color(0xFFCBD5E1), Offset(cx - 14f, cy - 5f), Size(60f, 4f))
            }
        }
        return
    }

    val activeAtts = equippedAttachmentKeys.mapNotNull { WeaponAttachmentCatalog.fromKey(it) }
        .filter { it.isCompatibleWith(weapon) }
        .associateBy { it.slot }
    val opticAtt = activeAtts[AttachmentSlot.OPTIC]
    val muzzleAtt = activeAtts[AttachmentSlot.MUZZLE]
    val underAtt = activeAtts[AttachmentSlot.UNDERBARREL]
    val tacAtt = activeAtts[AttachmentSlot.TACTICAL]
    val magAtt = activeAtts[AttachmentSlot.MAGAZINE]
    val stockAtt = activeAtts[AttachmentSlot.STOCK_GRIP]

    if (weapon == WeaponId.CROSSBOW) {
        drawRect(Color(0xFF0F172A), Offset(cx - 46f + depthOffset.x, cy - 4f + depthOffset.y), Size(92f, 9f))
        drawRect(Color(0xFF334155), Offset(cx - 46f, cy - 5f), Size(86f, 10f))
        val bow = Path().apply {
            moveTo(cx + 18f, cy - 38f)
            lineTo(cx + 34f, cy)
            lineTo(cx + 18f, cy + 38f)
        }
        drawPath(bow, color = Color(0xFF64748B), style = Stroke(width = 5f))
        drawLine(Color(0xFFCBD5E1), Offset(cx + 18f, cy - 38f), Offset(cx - 18f, cy), strokeWidth = 2f)
        drawLine(Color(0xFFCBD5E1), Offset(cx + 18f, cy + 38f), Offset(cx - 18f, cy), strokeWidth = 2f)
        drawRect(Color(0xFF34D399), Offset(cx - 14f, cy - 2f), Size(54f, 4f))
        if (opticAtt != null) {
            drawRoundRect(Color(0xFF0F172A), Offset(cx - 16f, cy - 18f), Size(36f, 10f), CornerRadius(3f, 3f))
            drawCircle(Color(0xFF38BDF8), radius = 3.2f, center = Offset(cx + 18f, cy - 13f))
        }
        return
    }

    val isRevolver = weapon == WeaponId.MODEL_29 || weapon == WeaponId.COLT_SAA || weapon == WeaponId.SCHOFIELD || weapon == WeaponId.WEBLEY_MK6
    val receiverW = when (weapon.category) {
        WeaponCategory.PISTOL -> 40f
        WeaponCategory.SMG -> 46f
        WeaponCategory.SHOTGUN -> 56f
        WeaponCategory.RIFLE -> 54f
        WeaponCategory.DMR_SNIPER -> 60f
        else -> 60f
    }
    val barrelW = when (weapon.category) {
        WeaponCategory.PISTOL -> if (weapon.suppressed) 24f else 14f
        WeaponCategory.SMG -> 22f
        WeaponCategory.SHOTGUN -> 42f
        WeaponCategory.RIFLE -> 36f
        WeaponCategory.DMR_SNIPER -> 46f
        else -> 38f
    }
    val startX = cx - (receiverW + barrelW) * 0.45f

    // 3D depth shadow
    drawRect(color = Color(0xFF0F1419), topLeft = Offset(startX + depthOffset.x, cy - 12f + depthOffset.y), size = Size(receiverW, 20f))

    // Buttstock (including detachable C96 holster stock or folding wire stock)
    if (weapon.category != WeaponCategory.PISTOL || stockAtt?.visualCode == "c96_stock") {
        val isWire = stockAtt?.visualCode == "wire_stock"
        val isWoodStock = weapon.era != WeaponEra.MODERN ||
            weapon == WeaponId.SHOTGUN || weapon == WeaponId.DOUBLE_BARREL ||
            weapon == WeaponId.AK47 || weapon == WeaponId.RPK74 || weapon == WeaponId.KAR98K
        if (isWire) {
            drawLine(Color(0xFF94A3B8), Offset(startX - 24f, cy - 6f), Offset(startX, cy - 6f), strokeWidth = 3f)
            drawLine(Color(0xFF94A3B8), Offset(startX - 24f, cy + 5f), Offset(startX, cy + 5f), strokeWidth = 3f)
            drawRect(Color(0xFF475569), Offset(startX - 26f, cy - 9f), Size(5f, 18f))
        } else {
            val stockColor = when {
                stockAtt?.visualCode == "c96_stock" -> Color(0xFF78350F)
                stockAtt?.visualCode == "tactical_stock" -> Color(0xFF1F2937)
                isWoodStock -> Color(0xFF78350F)
                else -> Color(0xFF1F2937)
            }
            drawRoundRect(
                color = stockColor,
                topLeft = Offset(startX - 22f, cy - 10f),
                size = Size(24f, 22f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }

    // Receiver
    drawRect(color = accentCol, topLeft = Offset(startX, cy - 12f), size = Size(receiverW, 20f))
    drawRect(color = Color.White.copy(alpha = 0.16f), topLeft = Offset(startX + 2f, cy - 11f), size = Size(receiverW - 4f, 4f))

    // Equipped Optic OR built-in sight
    when {
        opticAtt != null -> when (opticAtt.visualCode) {
            "brass_long_scope" -> {
                drawRoundRect(Color(0xFFD97706), Offset(startX - 2f, cy - 22f), Size(receiverW + 18f, 8f), CornerRadius(3f, 3f))
                drawCircle(Color(0xFFFDE68A), radius = 3.2f, center = Offset(startX + receiverW + 14f, cy - 18f))
            }
            "tang_peep" -> {
                drawRect(Color(0xFF1E293B), Offset(startX + 2f, cy - 22f), Size(5f, 10f))
                drawCircle(Color(0xFFF59E0B), radius = 3.5f, center = Offset(startX + 4.5f, cy - 22f), style = Stroke(width = 2f))
            }
            "vintage_scope", "pso1_scope", "starlight_scope", "sniper_scope", "lpvo" -> {
                val scopeBody = if (opticAtt.visualCode == "starlight_scope") Color(0xFF065F46) else Color(0xFF0F172A)
                val lensCol = if (opticAtt.visualCode == "starlight_scope") Color(0xFF34D399) else Color(0xFF38BDF8)
                drawRoundRect(scopeBody, Offset(startX + 2f, cy - 24f), Size(46f, 11f), CornerRadius(3f, 3f))
                drawCircle(lensCol, radius = 3.6f, center = Offset(startX + 46f, cy - 18.5f))
            }
            "carry_scope" -> {
                drawRect(Color(0xFF1E293B), Offset(startX + 8f, cy - 21f), Size(32f, 9f))
                drawRoundRect(Color(0xFF0F172A), Offset(startX + 10f, cy - 28f), Size(28f, 8f), CornerRadius(3f, 3f))
            }
            "acog" -> {
                drawRoundRect(Color(0xFF1E293B), Offset(startX + 10f, cy - 24f), Size(28f, 11f), CornerRadius(3f, 3f))
                drawRect(Color(0xFFEF4444), Offset(startX + 14f, cy - 26f), Size(14f, 3f))
            }
            else -> {
                drawRect(Color(0xFF1E293B), Offset(startX + 10f, cy - 22f), Size(20f, 10f))
                drawCircle(Color(0xFFEF4444), radius = 3.2f, center = Offset(startX + 20f, cy - 17f))
            }
        }
        weapon.category == WeaponCategory.DMR_SNIPER -> {
            drawRoundRect(Color(0xFF0F172A), Offset(startX + 4f, cy - 23f), Size(44f, 10f), CornerRadius(3f, 3f))
            drawCircle(Color(0xFF38BDF8), radius = 3.5f, center = Offset(startX + 46f, cy - 18f))
        }
        weapon.category != WeaponCategory.PISTOL || weapon.key == "deagle" -> {
            drawRect(color = Color(0xFF334155), topLeft = Offset(startX + 8f, cy - 19f), size = Size(receiverW * 0.52f, 7f))
            drawCircle(color = Color(weapon.tracerColorHex), radius = 3.2f, center = Offset(startX + 20f, cy - 15.5f))
        }
    }

    if (isRevolver) {
        drawRoundRect(Color(0xFF334155), Offset(startX + 12f, cy - 8f), Size(16f, 14f), CornerRadius(4f, 4f))
    }

    // Pistol grip
    val gripColor = if (stockAtt?.visualCode == "wood_grip") Color(0xFF92400E) else Color(0xFF1E293B)
    drawRect(color = gripColor, topLeft = Offset(startX + 5f, cy + 8f), size = Size(12f, 20f))

    // Magazine / Ammo Box / Pump Forend (including drums & extended mags)
    when {
        magAtt?.visualCode == "drum_mag" -> {
            drawCircle(Color(0xFF1F2937), radius = 17f, center = Offset(startX + 30f, cy + 23f))
            drawCircle(Color(0xFF64748B), radius = 17f, center = Offset(startX + 30f, cy + 23f), style = Stroke(width = 2f))
            drawCircle(Color(0xFFD97706), radius = 5f, center = Offset(startX + 30f, cy + 23f))
        }
        magAtt?.visualCode == "ext_mag" -> {
            drawRect(color = Color(0xFF0F172A), topLeft = Offset(startX + 24f, cy + 8f), size = Size(13f, 34f))
            drawRect(color = Color(0xFF38BDF8), topLeft = Offset(startX + 25f, cy + 37f), size = Size(11f, 4f))
        }
        weapon.category == WeaponCategory.LMG_SPECIAL -> {
            drawRoundRect(Color(0xFF14532D), Offset(startX + 22f, cy + 8f), Size(22f, 22f), CornerRadius(4f, 4f))
            drawRect(Color(0xFFFBBF24), Offset(startX + 25f, cy + 12f), Size(16f, 4f))
        }
        weapon.category == WeaponCategory.SHOTGUN -> {
            drawRect(color = Color(0xFF78350F), topLeft = Offset(startX + receiverW, cy - 1f), size = Size(28f, 9f))
        }
        weapon.category == WeaponCategory.PISTOL -> Unit
        else -> {
            drawRect(color = Color(0xFF1E293B), topLeft = Offset(startX + 24f, cy + 8f), size = Size(13f, 26f))
        }
    }

    // Barrel + Trench Heat Shield
    drawRect(color = Color(0xFF64748B), topLeft = Offset(startX + receiverW, cy - 8f), size = Size(barrelW, 7f))
    if (underAtt?.visualCode == "heat_shield") {
        drawRect(color = Color(0xFF475569), topLeft = Offset(startX + receiverW + 2f, cy - 10f), size = Size(barrelW * 0.72f, 5f))
    }

    // Underbarrel Foregrip / Bipod / Bayonet
    when (underAtt?.visualCode) {
        "vertical_grip" -> drawRoundRect(Color(0xFF334155), Offset(startX + receiverW + 6f, cy + 2f), Size(9f, 20f), CornerRadius(3f, 3f))
        "bipod" -> {
            drawLine(Color(0xFF334155), Offset(startX + receiverW + barrelW * 0.55f, cy + 1f), Offset(startX + receiverW + barrelW * 0.4f, cy + 22f), strokeWidth = 3f)
            drawLine(Color(0xFF334155), Offset(startX + receiverW + barrelW * 0.55f, cy + 1f), Offset(startX + receiverW + barrelW * 0.7f, cy + 22f), strokeWidth = 3f)
        }
        "bayonet" -> {
            drawRect(Color(0xFFCBD5E1), Offset(startX + receiverW + barrelW - 4f, cy + 1f), Size(28f, 4f))
        }
    }

    // Tactical Flashlight / Laser
    if (tacAtt != null) {
        val podCol = if (tacAtt.visualCode == "laser") Color(0xFF14532D) else Color(0xFF1E293B)
        drawRoundRect(podCol, Offset(startX + receiverW + 4f, cy - 14f), Size(14f, 6f), CornerRadius(2f, 2f))
        if (tacAtt.visualCode == "laser") {
            drawLine(Color(0xFFEF4444), Offset(startX + receiverW + 18f, cy - 11f), Offset(cx + 64f, cy - 11f), strokeWidth = 1.6f)
        } else {
            drawCircle(Color(0xFFFEF08A), radius = 3.5f, center = Offset(startX + receiverW + 20f, cy - 11f))
        }
    }

    // Muzzle Device (Equipped Suppressor / Moderator / Brake / Choke OR native)
    val muzzleX = startX + receiverW + barrelW
    when {
        muzzleAtt != null -> when (muzzleAtt.visualCode) {
            "suppressor", "moderator" -> {
                val canCol = if (muzzleAtt.visualCode == "moderator") Color(0xFF1F2937) else Color(0xFF0F172A)
                drawRoundRect(color = canCol, topLeft = Offset(muzzleX - 6f, cy - 11f), size = Size(26f, 13f), cornerRadius = CornerRadius(4f, 4f))
                drawRect(color = Color(0xFF34D399), topLeft = Offset(muzzleX + 14f, cy - 9f), size = Size(3f, 9f))
            }
            "choke" -> {
                drawRoundRect(color = Color(0xFFD97706), topLeft = Offset(muzzleX - 4f, cy - 9f), size = Size(12f, 9f), cornerRadius = CornerRadius(2f, 2f))
            }
            else -> {
                drawRect(color = Color(0xFFCBD5E1), topLeft = Offset(muzzleX - 3f, cy - 10f), size = Size(13f, 11f))
                drawLine(Color(0xFF0F172A), Offset(muzzleX + 2f, cy - 10f), Offset(muzzleX + 2f, cy + 1f), strokeWidth = 2f)
            }
        }
        weapon.suppressed -> {
            drawRoundRect(color = Color(0xFF0F172A), topLeft = Offset(muzzleX - 14f, cy - 11f), size = Size(22f, 13f), cornerRadius = CornerRadius(4f, 4f))
        }
        else -> {
            drawRect(color = Color(0xFF94A3B8), topLeft = Offset(muzzleX - 6f, cy - 9f), size = Size(8f, 9f))
        }
    }
}

private fun DrawScope.drawAttachmentSprite(att: WeaponAttachment, parallax: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val px = parallax * 0.5f
    val accent = Color(att.rarityColorHex)

    when (att.visualCode) {
        "brass_long_scope" -> {
            drawRoundRect(Color(0xFF1F2937), Offset(cx - 52f + px, cy - 7f + 5f), Size(104f, 14f), CornerRadius(5f, 5f))
            drawRoundRect(Color(0xFFB45309), Offset(cx - 52f, cy - 7f), Size(104f, 14f), CornerRadius(5f, 5f))
            drawRect(Color(0xFFFDE68A), Offset(cx - 46f, cy - 5f), Size(92f, 4f))
            drawCircle(Color(0xFF38BDF8), radius = 6f, center = Offset(cx + 50f, cy))
        }
        "tang_peep" -> {
            drawRect(Color(0xFF1E293B), Offset(cx - 24f + px, cy + 14f + 4f), Size(48f, 12f))
            drawRect(Color(0xFF475569), Offset(cx - 24f, cy + 14f), Size(48f, 12f))
            drawRect(Color(0xFF94A3B8), Offset(cx - 5f, cy - 26f), Size(10f, 42f))
            drawCircle(Color(0xFFF59E0B), radius = 12f, center = Offset(cx, cy - 26f), style = Stroke(width = 4f))
        }
        "vintage_scope", "pso1_scope", "sniper_scope", "lpvo", "carry_scope", "starlight_scope" -> {
            val bodyCol = if (att.visualCode == "starlight_scope") Color(0xFF064E3B) else Color(0xFF1E293B)
            val lensCol = if (att.visualCode == "starlight_scope") Color(0xFF34D399) else Color(0xFF38BDF8)
            drawRoundRect(Color(0xFF0F172A), Offset(cx - 48f + px, cy - 13f + 5f), Size(96f, 26f), CornerRadius(8f, 8f))
            drawRoundRect(bodyCol, Offset(cx - 48f, cy - 13f), Size(96f, 26f), CornerRadius(8f, 8f))
            drawRoundRect(Color(0xFF334155), Offset(cx - 12f, cy - 22f), Size(20f, 10f), CornerRadius(3f, 3f))
            drawRect(Color(0xFF0F172A), Offset(cx - 24f, cy + 13f), Size(14f, 14f))
            drawRect(Color(0xFF0F172A), Offset(cx + 10f, cy + 13f), Size(14f, 14f))
            drawCircle(lensCol, radius = 10f, center = Offset(cx + 46f, cy))
            drawCircle(Color.White.copy(alpha = 0.6f), radius = 3.5f, center = Offset(cx + 43f, cy - 3f))
        }
        "red_dot", "holo_sight", "micro_dot", "acog" -> {
            drawRoundRect(Color(0xFF0F172A), Offset(cx - 32f + px, cy - 24f + 5f), Size(64f, 48f), CornerRadius(8f, 8f))
            drawRoundRect(Color(0xFF1E293B), Offset(cx - 32f, cy - 24f), Size(64f, 44f), CornerRadius(8f, 8f))
            drawRoundRect(Color(0xFF0EA5E9).copy(alpha = 0.25f), Offset(cx - 22f, cy - 16f), Size(44f, 26f), CornerRadius(4f, 4f))
            drawCircle(Color(0xFFEF4444), radius = 4.5f, center = Offset(cx, cy - 3f))
            drawRect(Color(0xFF475569), Offset(cx - 28f, cy + 20f), Size(56f, 10f))
        }
        "suppressor", "moderator" -> {
            val col = if (att.visualCode == "moderator") Color(0xFF1F2937) else Color(0xFF0F172A)
            drawRoundRect(Color.Black, Offset(cx - 48f + px, cy - 13f + 5f), Size(96f, 26f), CornerRadius(10f, 10f))
            drawRoundRect(col, Offset(cx - 48f, cy - 13f), Size(96f, 26f), CornerRadius(10f, 10f))
            drawRect(accent, Offset(cx - 48f, cy - 13f), Size(14f, 26f))
            for (i in -1..2) {
                drawLine(Color(0xFF334155), Offset(cx + i * 16f, cy - 12f), Offset(cx + i * 16f, cy + 12f), strokeWidth = 2f)
            }
        }
        "brake", "choke" -> {
            val col = if (att.visualCode == "choke") Color(0xFFD97706) else Color(0xFF475569)
            drawRoundRect(Color(0xFF0F172A), Offset(cx - 34f + px, cy - 14f + 5f), Size(68f, 28f), CornerRadius(6f, 6f))
            drawRoundRect(col, Offset(cx - 34f, cy - 14f), Size(68f, 28f), CornerRadius(6f, 6f))
            for (i in -1..1) {
                drawRect(Color(0xFF0F172A), Offset(cx + i * 18f - 4f, cy - 10f), Size(8f, 20f))
            }
        }
        "bayonet" -> {
            drawRect(Color(0xFF0F172A), Offset(cx - 50f + px, cy - 6f + 5f), Size(100f, 12f))
            drawRect(Color(0xFF78350F), Offset(cx - 50f, cy - 6f), Size(32f, 12f))
            drawRect(Color(0xFFCBD5E1), Offset(cx - 18f, cy - 5f), Size(68f, 10f))
            drawRect(Color(0xFF94A3B8), Offset(cx - 18f, cy + 2f), Size(60f, 3f))
        }
        "drum_mag" -> {
            drawCircle(Color(0xFF0F172A), radius = 36f, center = Offset(cx + px * 0.5f, cy + 6f))
            drawCircle(Color(0xFF1E293B), radius = 36f, center = Offset(cx, cy))
            drawCircle(accent, radius = 36f, center = Offset(cx, cy), style = Stroke(width = 2.5f))
            drawRect(Color(0xFF334155), Offset(cx - 10f, cy - 46f), Size(20f, 18f))
            drawCircle(Color(0xFFD97706), radius = 10f, center = Offset(cx, cy))
        }
        "ext_mag", "speed_clip" -> {
            val isClip = att.visualCode == "speed_clip"
            val col = if (isClip) Color(0xFFD97706) else Color(0xFF1E293B)
            drawRoundRect(Color(0xFF0F172A), Offset(cx - 16f + px, cy - 40f + 5f), Size(32f, 80f), CornerRadius(6f, 6f))
            drawRoundRect(col, Offset(cx - 16f, cy - 40f), Size(32f, 80f), CornerRadius(6f, 6f))
            drawRect(Color(0xFFFBBF24), Offset(cx - 12f, cy - 34f), Size(24f, 10f))
            drawRect(accent, Offset(cx - 16f, cy + 28f), Size(32f, 8f))
        }
        "vertical_grip" -> {
            drawRect(Color(0xFF1E293B), Offset(cx - 22f + px, cy - 32f + 4f), Size(44f, 14f))
            drawRoundRect(Color(0xFF334155), Offset(cx - 12f, cy - 22f), Size(24f, 62f), CornerRadius(6f, 6f))
            drawRect(accent, Offset(cx - 22f, cy - 32f), Size(44f, 12f))
        }
        "bipod" -> {
            drawRect(Color(0xFF334155), Offset(cx - 16f, cy - 32f), Size(32f, 14f))
            drawLine(Color(0xFF94A3B8), Offset(cx - 6f, cy - 18f), Offset(cx - 34f, cy + 36f), strokeWidth = 7f)
            drawLine(Color(0xFF94A3B8), Offset(cx + 6f, cy - 18f), Offset(cx + 34f, cy + 36f), strokeWidth = 7f)
        }
        "laser", "flashlight" -> {
            val isLaser = att.visualCode == "laser"
            drawRoundRect(Color(0xFF0F172A), Offset(cx - 36f + px, cy - 16f + 5f), Size(64f, 32f), CornerRadius(6f, 6f))
            drawRoundRect(Color(if (isLaser) 0xFF14532D else 0xFF1E293B), Offset(cx - 36f, cy - 16f), Size(64f, 32f), CornerRadius(6f, 6f))
            if (isLaser) {
                drawCircle(Color(0xFFEF4444), radius = 7f, center = Offset(cx + 28f, cy))
                drawLine(Color(0xFFEF4444), Offset(cx + 32f, cy), Offset(cx + 58f, cy), strokeWidth = 3f)
            } else {
                drawCircle(Color(0xFFFEF08A), radius = 12f, center = Offset(cx + 26f, cy))
            }
        }
        "c96_stock", "tactical_stock", "wire_stock", "wood_grip", "sling_wrap" -> {
            val col = if (att.era == WeaponEra.MODERN) Color(0xFF334155) else Color(0xFF78350F)
            drawRoundRect(Color(0xFF0F172A), Offset(cx - 42f + px, cy - 22f + 5f), Size(84f, 44f), CornerRadius(8f, 8f))
            drawRoundRect(col, Offset(cx - 42f, cy - 22f), Size(84f, 44f), CornerRadius(8f, 8f))
            drawRect(accent, Offset(cx - 42f, cy - 22f), Size(10f, 44f))
        }
        else -> {
            drawRoundRect(Color(0xFF0F172A), Offset(cx - 36f + px, cy - 22f + 5f), Size(72f, 44f), CornerRadius(8f, 8f))
            drawRoundRect(Color(0xFF334155), Offset(cx - 36f, cy - 22f), Size(72f, 44f), CornerRadius(8f, 8f))
            drawCircle(accent, radius = 10f, center = Offset(cx, cy))
        }
    }
}

private fun DrawScope.drawCdCaseSprite(primaryHex: Long, accentHex: Long, parallax: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val w = 88f
    val h = 80f

    drawCircle(color = Color(0xFFCBD5E1), radius = 34f, center = Offset(cx + 18f + parallax * 0.4f, cy))
    drawCircle(color = Color(accentHex).copy(alpha = 0.55f), radius = 24f, center = Offset(cx + 18f + parallax * 0.4f, cy))
    drawCircle(color = Color(0xFF0F172A), radius = 7f, center = Offset(cx + 18f + parallax * 0.4f, cy))

    drawRect(color = Color(0xFF1E293B), topLeft = Offset(cx - w / 2f + parallax * 0.6f, cy - h / 2f + 5f), size = Size(w, h))
    drawRect(color = Color(primaryHex), topLeft = Offset(cx - w / 2f, cy - h / 2f), size = Size(w, h))
    drawRect(color = Color.White.copy(alpha = 0.22f), topLeft = Offset(cx - w / 2f, cy - h / 2f), size = Size(10f, h))
    drawCircle(color = Color(accentHex), radius = 20f, center = Offset(cx + 4f, cy - 4f))
    drawRect(color = Color.White.copy(alpha = 0.7f), topLeft = Offset(cx - 22f, cy + 22f), size = Size(52f, 5f))
    drawRect(color = Color.White.copy(alpha = 0.35f), topLeft = Offset(cx - w / 2f, cy - h / 2f), size = Size(w, h), style = Stroke(width = 2f))
}

private fun DrawScope.drawPantsSprite(level: Int, parallax: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val baseColor = when {
        level >= 4 -> Color(0xFF312E81)
        level == 3 -> Color(0xFF1E3A5F)
        level == 2 -> Color(0xFF3F4E3A)
        else -> Color(0xFF4B5563)
    }
    drawRect(color = Color(0xFF111827), topLeft = Offset(cx - 34f + parallax * 0.5f, cy - 42f + 5f), size = Size(68f, 34f))
    drawRect(color = baseColor, topLeft = Offset(cx - 34f, cy - 42f), size = Size(68f, 32f))
    drawRect(color = Color(0xFF1F2937), topLeft = Offset(cx - 34f, cy - 42f), size = Size(68f, 8f))
    drawRect(color = baseColor, topLeft = Offset(cx - 34f, cy - 12f), size = Size(29f, 58f))
    drawRect(color = baseColor, topLeft = Offset(cx + 5f, cy - 12f), size = Size(29f, 58f))

    val pockets = level.coerceIn(1, 4)
    for (i in 0 until pockets) {
        val px = if (i % 2 == 0) cx - 30f else cx + 10f
        val py = if (i < 2) cy - 4f else cy + 18f
        drawRect(color = Color(0xFF0E3A4D), topLeft = Offset(px, py), size = Size(20f, 16f))
        drawRect(color = CyanFloor, topLeft = Offset(px, py), size = Size(20f, 16f), style = Stroke(width = 1.8f))
    }
}

private fun DrawScope.drawFoodSprite(parallax: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    drawRoundRect(color = Color(0xFF14532D), topLeft = Offset(cx - 30f + parallax * 0.45f, cy - 36f + 5f), size = Size(60f, 76f), cornerRadius = CornerRadius(10f, 10f))
    drawRoundRect(color = Color(0xFF4B5563), topLeft = Offset(cx - 30f, cy - 36f), size = Size(60f, 76f), cornerRadius = CornerRadius(10f, 10f))
    drawRect(color = Color(0xFF16A34A), topLeft = Offset(cx - 30f, cy - 14f), size = Size(60f, 34f))
    drawRect(color = AmberBright, topLeft = Offset(cx - 20f, cy - 4f), size = Size(40f, 10f))
    drawOval(color = Color(0xFF9CA3AF), topLeft = Offset(cx - 26f, cy - 40f), size = Size(52f, 12f))
}

private fun DrawScope.drawWaterSprite(parallax: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    drawRoundRect(color = Color(0xFF0C4A6E), topLeft = Offset(cx - 24f + parallax * 0.45f, cy - 28f + 5f), size = Size(48f, 72f), cornerRadius = CornerRadius(12f, 12f))
    drawRect(color = Color(0xFF0284C7), topLeft = Offset(cx - 10f, cy - 46f), size = Size(20f, 12f))
    drawRoundRect(color = Color(0xFF38BDF8).copy(alpha = 0.85f), topLeft = Offset(cx - 24f, cy - 30f), size = Size(48f, 74f), cornerRadius = CornerRadius(12f, 12f))
    drawRect(color = Color(0xFFE0F2FE).copy(alpha = 0.6f), topLeft = Offset(cx - 16f, cy - 18f), size = Size(7f, 44f))
}

private fun DrawScope.drawMedkitSprite(parallax: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    drawRoundRect(color = Color(0xFF450A0A), topLeft = Offset(cx - 42f + parallax * 0.5f, cy - 30f + 5f), size = Size(84f, 64f), cornerRadius = CornerRadius(10f, 10f))
    drawRoundRect(color = Color(0xFFB91C1C), topLeft = Offset(cx - 42f, cy - 30f), size = Size(84f, 64f), cornerRadius = CornerRadius(10f, 10f))
    drawRect(color = Color.White, topLeft = Offset(cx - 6f, cy - 18f), size = Size(12f, 38f))
    drawRect(color = Color.White, topLeft = Offset(cx - 19f, cy - 5f), size = Size(38f, 12f))
}

private fun DrawScope.drawSalvageSprite(accent: Color, parallax: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    drawRoundRect(color = Color(0xFF111827), topLeft = Offset(cx - 40f + parallax * 0.5f, cy - 28f + 5f), size = Size(80f, 58f), cornerRadius = CornerRadius(8f, 8f))
    drawRoundRect(color = Color(0xFF334155), topLeft = Offset(cx - 40f, cy - 28f), size = Size(80f, 58f), cornerRadius = CornerRadius(8f, 8f))
    drawRect(color = accent.copy(alpha = 0.75f), topLeft = Offset(cx - 28f, cy - 16f), size = Size(56f, 14f))
    drawCircle(color = EmeraldHud, radius = 4f, center = Offset(cx - 22f, cy + 14f))
    drawCircle(color = AmberSignal, radius = 4f, center = Offset(cx - 8f, cy + 14f))
}
