package app.revela.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TonalContrastTest {

    @Test
    fun `sin valores tonales no hay delta`() {
        assertNull(ColorMath.tonalDelta(0f, 0f, 0f))
    }

    @Test
    fun `subir medios aclara el gris medio sin tocar negros ni blancos`() {
        val d = ColorMath.tonalDelta(0f, 1f, 0f)!!.delta
        val to255 = { f: Float -> f * 255f }
        assertTrue(d[128] > 40f, "medios +128 -> ${d[128]}")
        assertTrue(d[2] < 5f, "negros casi intactos -> ${d[2]}")
        assertTrue(d[253] < 5f, "blancos casi intactos -> ${d[253]}")
        assertEquals(255f, to255(1f), 0.01f)
    }

    @Test
    fun `bajar sombras oscurece la zona baja`() {
        val d = ColorMath.tonalDelta(-1f, 0f, 0f)!!.delta
        assertTrue(d[30] < -30f, "sombras -1 en v=30 -> ${d[30]}")
        assertTrue(d[220] > -6f, "luces casi intactas -> ${d[220]}")
    }

    @Test
    fun `la LUT aplica el delta tonal`() {
        val adj = ColorMath.Adjustments(tonalMidtones = 1f)
        val luts = ColorMath.buildLuts(adj)
        val mid = (luts[0][128] * 255f).toInt()
        assertTrue(mid > 168, "gris medio con medios+1 -> $mid")
        val black = (luts[0][0] * 255f).toInt()
        assertTrue(black < 12, "negro sigue negro -> $black")
    }
}
