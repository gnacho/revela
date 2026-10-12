package app.revela.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FrameMathTest {

    private fun pixel(r: Int, g: Int, b: Int): Int =
        (255 shl 24) or (r shl 16) or (g shl 8) or b

    @Test
    fun rotate90MapsCorners() {
        // imagen 2x3: (x,y) -> 90 horario: nuevo(x,y) = viejo(h-1-y, x)
        val px = intArrayOf(
            pixel(1, 0, 0), pixel(2, 0, 0),
            pixel(0, 1, 0), pixel(0, 2, 0),
            pixel(0, 0, 3), pixel(0, 0, 4)
        )
        val (out, ow, oh) = FrameMath.rotateQuarter(px, 2, 3, 1)
        assertEquals(3, ow)
        assertEquals(2, oh)
        // viejo(0,0) -> nuevo(2,0)
        assertEquals(pixel(1, 0, 0), out[0 * 3 + 2])
        // viejo(1,2) -> nuevo(0,1)
        assertEquals(pixel(0, 0, 4), out[1 * 3 + 0])
    }

    @Test
    fun fourTurnsIsIdentity() {
        val px = IntArray(12) { pixel(it, 255 - it, it / 2) }
        val (out, ow, oh) = FrameMath.rotateQuarter(px, 3, 4, 4)
        assertEquals(3, ow)
        assertEquals(4, oh)
        for (i in px.indices) assertEquals(px[i], out[i])
    }

    @Test
    fun selectiveBrightensNearAndNotFar() {
        val w = 100
        val h = 100
        val px = IntArray(w * h) { pixel(100, 100, 100) }
        val far = 90 * w + 5 // pixel lejos del punto
        val near = 50 * w + 50
        FrameMath.applySelective(
            px, w, h,
            listOf(ControlPoint(x = 0.5f, y = 0.5f, brightness = 1f, radius = 0.2f))
        )
        val before = pixel(100, 100, 100)
        assertTrue(
            ((px[near] ushr 16) and 0xFF) > ((before ushr 16) and 0xFF) + 40,
            "el pixel cercano debe aclararse: ${(px[near] ushr 16) and 0xFF}"
        )
        assertEquals(100, (px[far] ushr 16) and 0xFF, "el pixel lejano no debe cambiar")
    }

    @Test
    fun vignetteDarkensCornersMoreThanCenter() {
        val w = 101
        val h = 101
        val px = IntArray(w * h) { pixel(200, 200, 200) }
        FrameMath.applyVignette(px, w, h, 0.8f)
        val center = (px[50 * w + 50] ushr 16) and 0xFF
        val corner = (px[0] ushr 16) and 0xFF
        assertTrue(center >= 195, "centro casi intacto: $center")
        assertTrue(corner < 150, "esquina oscurecida: $corner")
    }

    @Test
    fun vignetteZeroStrengthIsIdentity() {
        val px = IntArray(9) { pixel(150, 150, 150) }
        val before = px.copyOf()
        FrameMath.applyVignette(px, 3, 3, 0f)
        assertTrue(before.contentEquals(px))
    }
}
