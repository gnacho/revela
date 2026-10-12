package app.revela.core

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ColorMathTest {

    private val identity = ColorMath.Adjustments()

    private fun pixel(r: Int, g: Int, b: Int, a: Int = 255): Int =
        (a shl 24) or (r shl 16) or (g shl 8) or b

    private fun channel(argb: Int, shift: Int): Int = (argb ushr shift) and 0xFF

    private fun assertNear(expected: Int, actual: Int, tolerance: Int = 1) {
        assertTrue(abs(expected - actual) <= tolerance, "esperado ~$expected, obtenido $actual")
    }

    @Test
    fun identityLeavesPixelsUntouched() {
        val pixels = intArrayOf(pixel(0, 0, 0), pixel(64, 128, 255), pixel(255, 255, 255, 128))
        val before = pixels.copyOf()
        ColorMath.applyInPlace(pixels, ColorMath.buildLuts(identity), 0f)
        assertTrue(before.contentEquals(pixels), "identidad debe ser exacta")
    }

    @Test
    fun plusOneStopDoublesMidtone() {
        val pixels = intArrayOf(pixel(64, 64, 64), pixel(128, 128, 128))
        ColorMath.applyInPlace(pixels, ColorMath.buildLuts(identity.copy(exposureStops = 1f)), 0f)
        assertNear(128, channel(pixels[0], 16))
        assertNear(255, channel(pixels[1], 16), 0) // 256 satura a 255
    }

    @Test
    fun desaturateProducesLuma() {
        val pixels = intArrayOf(pixel(100, 150, 200))
        ColorMath.applyInPlace(pixels, ColorMath.buildLuts(identity), -1f)
        val r = channel(pixels[0], 16)
        val g = channel(pixels[0], 8)
        val b = channel(pixels[0], 0)
        val luma = (0.2126f * 100 + 0.7152f * 150 + 0.0722f * 200).roundToInt()
        assertNear(luma, r)
        assertNear(luma, g)
        assertNear(luma, b)
    }

    @Test
    fun warmTemperatureBoostsRedAndCutsBlue() {
        val pixels = intArrayOf(pixel(100, 100, 100))
        ColorMath.applyInPlace(pixels, ColorMath.buildLuts(identity.copy(temperature = 1f)), 0f)
        assertNear(125, channel(pixels[0], 16))  // 100 * 1.25
        assertNear(75, channel(pixels[0], 0))    // 100 * 0.75
    }

    @Test
    fun contrastAroundMidpoint() {
        val pixels = intArrayOf(pixel(255, 255, 255), pixel(0, 0, 0))
        ColorMath.applyInPlace(pixels, ColorMath.buildLuts(identity.copy(contrast = 1f)), 0f)
        assertEquals(255, channel(pixels[0], 16))
        assertEquals(0, channel(pixels[1], 16))
    }

    @Test
    fun curveIsLastOperation() {
        val a = identity.copy(
            exposureStops = 0.5f,
            curve = listOf(CurvePoint(0f, 0f), CurvePoint(1f, 1f))
        )
        val pixels = intArrayOf(pixel(100, 100, 100))
        ColorMath.applyInPlace(pixels, ColorMath.buildLuts(a), 0f)
        val expected = (100f * 2f.pow(0.5f)).roundToInt().coerceAtMost(255)
        assertNear(expected, channel(pixels[0], 16))
    }

    @Test
    fun alphaPreserved() {
        val pixels = intArrayOf(pixel(50, 60, 70, a = 180))
        ColorMath.applyInPlace(pixels, ColorMath.buildLuts(identity.copy(exposureStops = 1f)), 0.5f)
        assertEquals(180, channel(pixels[0], 24))
    }
}
