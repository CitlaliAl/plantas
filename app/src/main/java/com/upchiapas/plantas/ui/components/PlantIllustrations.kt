package com.upchiapas.plantas.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate

/** Tipo de dibujo que se usa para cada planta, según su nombre o especie. */
enum class PlantKind { SUCCULENT, LEAFY, SPROUT }

fun plantKindOf(name: String, species: String): PlantKind {
    val text = "$name $species".lowercase()
    val succulents = listOf("sábila", "sabila", "aloe", "suculenta", "echeveria", "cactus", "agave", "jade")
    val leafy = listOf("monstera", "helecho", "menta", "ficus", "pothos", "potus", "hierba", "albahaca")
    return when {
        succulents.any { text.contains(it) } -> PlantKind.SUCCULENT
        leafy.any { text.contains(it) } -> PlantKind.LEAFY
        else -> PlantKind.SPROUT
    }
}

private val LeafDark = Color(0xFF2E7D32)
private val LeafMid = Color(0xFF4CAF50)
private val LeafLight = Color(0xFF8BC34A)
private val SucculentA = Color(0xFF5E9E78)
private val SucculentB = Color(0xFF7FB892)
private val PotColor = Color(0xFFC1693C)
private val PotRim = Color(0xFFD98255)
private val SoilColor = Color(0xFF7A4B2A)

/** Dibujos sencillos de plantas hechos con formas de Compose (sin imágenes externas). */
@Composable
fun PlantIllustration(kind: PlantKind, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 100f
        when (kind) {
            PlantKind.SUCCULENT -> {
                val angles = listOf(-62f, 62f, -38f, 38f, -14f, 14f, 0f)
                angles.forEachIndexed { i, a ->
                    leaf(s, a, 13f, if (a == 0f) 40f else 34f, if (i % 2 == 0) SucculentA else SucculentB)
                }
                pot(s)
            }
            PlantKind.LEAFY -> {
                val angles = listOf(-72f, 72f, -45f, 45f, -18f, 18f)
                angles.forEachIndexed { i, a ->
                    leaf(s, a, 20f, 42f, if (i % 3 == 0) LeafDark else if (i % 3 == 1) LeafMid else LeafLight)
                }
                leaf(s, 0f, 18f, 46f, LeafMid)
                pot(s)
            }
            PlantKind.SPROUT -> {
                drawOval(SoilColor, topLeft = Offset(16f * s, 68f * s), size = Size(68f * s, 26f * s))
                drawLine(
                    LeafDark,
                    start = Offset(50f * s, 80f * s),
                    end = Offset(50f * s, 44f * s),
                    strokeWidth = 5f * s,
                    cap = StrokeCap.Round
                )
                leaf(s, -62f, 20f, 34f, LeafMid, baseX = 50f, baseY = 50f)
                leaf(s, 62f, 20f, 34f, LeafLight, baseX = 50f, baseY = 50f)
            }
        }
    }
}

/** Una hoja: óvalo vertical que gira alrededor de su base. */
private fun DrawScope.leaf(
    s: Float,
    angle: Float,
    w: Float,
    h: Float,
    color: Color,
    baseX: Float = 50f,
    baseY: Float = 62f
) {
    rotate(degrees = angle, pivot = Offset(baseX * s, baseY * s)) {
        drawOval(
            color = color,
            topLeft = Offset((baseX - w / 2f) * s, (baseY - h) * s),
            size = Size(w * s, h * s)
        )
    }
}

private fun DrawScope.pot(s: Float) {
    val body = Path().apply {
        moveTo(30f * s, 64f * s)
        lineTo(70f * s, 64f * s)
        lineTo(64f * s, 94f * s)
        lineTo(36f * s, 94f * s)
        close()
    }
    drawPath(body, PotColor)
    drawRoundRect(
        color = PotRim,
        topLeft = Offset(26f * s, 58f * s),
        size = Size(48f * s, 10f * s),
        cornerRadius = CornerRadius(4f * s, 4f * s)
    )
}
