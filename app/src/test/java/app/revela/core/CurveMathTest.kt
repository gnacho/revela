package app.revela.core

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CurveMathTest {

    @Test
    fun identityCurveIsExact() {
        val lut = CurveMath.lut(listOf(CurvePoint(0f, 0f), CurvePoint(1f, 1f)))
        assertEquals(256, lut.size)
        for (i in 0 until 256) {
            assertEquals(i / 255f, lut[i], 1e-4f, "desvio en i=$i")
        }
    }

    @Test
    fun endpointsAndMonotonicity() {
        val lut = CurveMath.lut(
            listOf(
                CurvePoint(0f, 0.05f),
                CurvePoint(0.25f, 0.15f),
                CurvePoint(0.75f, 0.9f),
                CurvePoint(1f, 0.95f)
            )
        )
        assertEquals(0.05f, lut[0], 1e-3f)
        assertEquals(0.95f, lut[255], 1e-3f)
        for (i in 1 until 256) {
            assertTrue(lut[i] >= lut[i - 1] - 1e-4f, "no monotona en i=$i")
        }
    }

    @Test
    fun brighteningCurveRaisesMidtones() {
        val lut = CurveMath.lut(
            listOf(CurvePoint(0f, 0f), CurvePoint(0.5f, 0.75f), CurvePoint(1f, 1f))
        )
        assertTrue(lut[128] > 0.6f, "medio tono deberia subir: ${lut[128]}")
        assertTrue(abs(lut[128] - 0.75f) < 0.02f, "el punto de control debe interpolar: ${lut[128]}")
    }

    @Test
    fun unsortedPointsAreSorted() {
        val a = CurveMath.lut(listOf(CurvePoint(1f, 1f), CurvePoint(0f, 0f)))
        val b = CurveMath.lut(listOf(CurvePoint(0f, 0f), CurvePoint(1f, 1f)))
        for (i in 0 until 256) {
            assertEquals(b[i], a[i], 1e-4f)
        }
    }

    @Test
    fun requiresTwoPoints() {
        assertFailsWith<IllegalArgumentException> {
            CurveMath.lut(listOf(CurvePoint(0f, 0f)))
        }
    }
}
