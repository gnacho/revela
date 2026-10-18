package app.revela.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrushTest {

    private fun gray(w: Int, h: Int, v: Int): IntArray =
        IntArray(w * h) { 0xFF000000.toInt() or (v shl 16) or (v shl 8) or v }

    @Test
    fun `densidad positiva en el trazo y cero fuera`() {
        val w = 64; val h = 64
        val strokes = listOf(HealStroke(radiusNorm = 0.1f, xs = listOf(0.5f), ys = listOf(0.5f)))
        val d = BrushMath.buildDensity(strokes, w, h)
        val center = d[(h / 2) * w + (w / 2)]
        val corner = d[2 * w + 2]
        assertTrue(center > 0.9f, "centro $center")
        assertEquals(0f, corner)
    }

    @Test
    fun `exposicion positiva aclara solo donde se pinto`() {
        val w = 64; val h = 64
        val src = gray(w, h, 100)
        val strokes = listOf(HealStroke(radiusNorm = 0.2f, xs = listOf(0.25f), ys = listOf(0.5f)))
        val density = BrushMath.buildDensity(strokes, w, h)
        BrushMath.apply(src, w, h, BrushMath.EXPOSURE, amount = 0.8f, density = density)
        val painted = src[(h / 2) * w + (w / 4)] and 0xFF
        val unpainted = src[(h / 2) * w + (w * 3 / 4)] and 0xFF
        assertTrue(painted > 130, "pintado $painted")
        assertEquals(100, unpainted)
    }

    @Test
    fun `saturacion negativa desatura la zona pintada`() {
        val w = 32; val h = 32
        // rojo puro
        val src = IntArray(w * h) { 0xFFFF0000.toInt() }
        val strokes = listOf(HealStroke(radiusNorm = 0.3f, xs = listOf(0.5f), ys = listOf(0.5f)))
        val density = BrushMath.buildDensity(strokes, w, h)
        BrushMath.apply(src, w, h, BrushMath.SATURATION, amount = -1f, density = density)
        val center = src[(h / 2) * w + (w / 2)]
        val r = (center shr 16) and 0xFF
        val g = (center shr 8) and 0xFF
        assertTrue(kotlin.math.abs(r - g) < 8, "desaturado r=$r g=$g")
    }

    @Test
    fun `temperatura positiva sube rojo y baja azul`() {
        val w = 32; val h = 32
        val src = gray(w, h, 128)
        val strokes = listOf(HealStroke(radiusNorm = 0.3f, xs = listOf(0.5f), ys = listOf(0.5f)))
        val density = BrushMath.buildDensity(strokes, w, h)
        BrushMath.apply(src, w, h, BrushMath.TEMPERATURE, amount = 1f, density = density)
        val center = src[(h / 2) * w + (w / 2)]
        val r = (center shr 16) and 0xFF
        val b = center and 0xFF
        assertTrue(r > b, "r=$r b=$b")
    }

    @Test
    fun `pincel sobrevive al codec de la pila`() {
        val strokes = listOf(HealStroke(radiusNorm = 0.1f, xs = listOf(0.3f, 0.4f), ys = listOf(0.3f, 0.4f)))
        val stack = EditStack(listOf(Edit.Brush(param = BrushMath.DODGE_BURN, amount = 0.5f, strokes = strokes)))
        val back = EditStack.fromJson(stack.toJson())
        val e = back.edits.single() as Edit.Brush
        assertEquals(BrushMath.DODGE_BURN, e.param)
        assertEquals(0.5f, e.amount)
        assertEquals(2, e.strokes[0].xs.size)
    }

    @Test
    fun `pincel aplica dentro del pipeline completo`() {
        val w = 32; val h = 32
        val src = gray(w, h, 100)
        val strokes = listOf(HealStroke(radiusNorm = 0.25f, xs = listOf(0.5f), ys = listOf(0.5f)))
        val stack = EditStack(listOf(Edit.Brush(BrushMath.EXPOSURE, 0.9f, strokes)))
        FramePipeline.apply(src, w, h, stack)
        assertTrue((src[(h / 2) * w + (w / 2)] and 0xFF) > 120)
    }
}
