package app.revela.core

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HealMathTest {

    private fun pixel(r: Int, g: Int, b: Int): Int =
        (255 shl 24) or (r shl 16) or (g shl 8) or b

    @Test
    fun maskCoversBrushPath() {
        val strokes = listOf(
            HealStroke(
                radiusNorm = 0.1f,
                xs = listOf(0.2f, 0.8f),
                ys = listOf(0.5f, 0.5f)
            )
        )
        val mask = HealMath.buildMask(strokes, 100, 100)
        assertTrue(mask[50 * 100 + 20], "inicio del trazo marcado")
        assertTrue(mask[50 * 100 + 80], "fin del trazo marcado")
        assertTrue(mask[50 * 100 + 50], "interpolado en el centro marcado")
        assertEquals(false, mask[0], "esquina lejos del trazo sin marcar")
    }

    @Test
    fun inpaintFillsHoleAndKeepsKnownPixels() {
        val w = 60
        val h = 60
        val px = IntArray(w * h) { i -> pixel((i % w) * 4, 120, 255 - (i / w) * 4) }
        val before = px.copyOf()
        val mask = BooleanArray(w * h)
        for (y in 25 until 35) {
            for (x in 25 until 35) mask[y * w + x] = true
        }
        // ensucia el agujero
        for (y in 25 until 35) {
            for (x in 25 until 35) px[y * w + x] = pixel(255, 0, 0)
        }
        HealMath.inpaint(px, mask, w, h, smoothingPasses = 30)

        // conocidos intactos
        assertEquals(before[0], px[0])
        assertEquals(before[10 * w + 10], px[10 * w + 10])
        // el centro rellenado ya no es el roto sucio y es coherente con el entorno
        val c = px[30 * w + 30]
        val r = (c ushr 16) and 0xFF
        assertTrue(r < 200, "el rojo sucio desaparecio: $r")
        // suavidad: vecinos del area rellenada difieren poco
        val a = px[30 * w + 29]
        val b = px[30 * w + 30]
        assertTrue(abs(((a ushr 16) and 0xFF) - ((b ushr 16) and 0xFF)) <= 6, "transicion suave")
    }

    @Test
    fun emptyMaskIsNoOp() {
        val px = IntArray(100) { pixel(it, it, it) }
        val before = px.copyOf()
        HealMath.inpaint(px, BooleanArray(100), 10, 10)
        assertTrue(before.contentEquals(px))
    }
}
