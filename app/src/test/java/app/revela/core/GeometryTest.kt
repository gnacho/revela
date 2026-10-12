package app.revela.core

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeometryTest {

    @Test
    fun noRotationKeepsSize() {
        val (w, h) = GeometryMath.largestInscribed(1000f, 800f, 0f, 1000f / 800f)
        assertEquals(1000f, w, 0.5f)
        assertEquals(800f, h, 0.5f)
    }

    @Test
    fun squareRotated45Degrees() {
        // Cuadrado 1000x1000 girado 45 grados: mayor cuadrado axis-aligned = lado / raiz(2)
        val expected = 1000f / sqrt(2f)
        val (w, h) = GeometryMath.largestInscribed(1000f, 1000f, (Math.PI / 4).toFloat(), 1f)
        assertEquals(expected, w, 1f)
        assertEquals(expected, h, 1f)
    }

    @Test
    fun rotatedSizeSwapsOnOddQuarter() {
        val g = GeometryModel(quarterTurns = 1)
        assertEquals(Pair(800, 1000), g.rotatedSize(1000, 800))
        val gNeg = GeometryModel(quarterTurns = -1)
        assertEquals(Pair(800, 1000), gNeg.rotatedSize(1000, 800))
        val g2 = GeometryModel(quarterTurns = 2)
        assertEquals(Pair(1000, 800), g2.rotatedSize(1000, 800))
    }

    @Test
    fun totalRotationComposes() {
        val g = GeometryModel(quarterTurns = 3, straightenDeg = -5f)
        assertEquals(265f, g.totalRotationDeg, 1e-4f)
    }

    @Test
    fun outputSizeAppliesCrop() {
        val g = GeometryModel(cropLeft = 0.25f, cropTop = 0.25f, cropRight = 0.75f, cropBottom = 0.75f)
        val (w, h) = GeometryMath.outputSize(1000, 800, g)
        assertEquals(500, w)
        assertEquals(400, h)
    }

    @Test
    fun outputSizeWithStraightenShrinks() {
        val g = GeometryModel(straightenDeg = 45f)
        val (w, h) = GeometryMath.outputSize(1000, 1000, g, 1f)
        val expected = (1000f / sqrt(2f)).toInt()
        assertTrue(w in (expected - 2)..(expected + 2), "w=$w esperado ~$expected")
        assertTrue(h in (expected - 2)..(expected + 2), "h=$h esperado ~$expected")
    }
}
