package app.revela.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.math.abs
import kotlin.test.assertTrue

class PipelineTest {

    private fun pixel(r: Int, g: Int, b: Int): Int =
        (255 shl 24) or (r shl 16) or (g shl 8) or b

    @Test
    fun pipelineAppliesAllPasses() {
        val w = 80
        val h = 80
        val px = IntArray(w * h) { pixel(100, 100, 100) }
        val stack = EditStack()
        stack.apply(Edit.Exposure(0.5f))
        stack.apply(Edit.Vignette(0.9f))
        stack.apply(Edit.Look("byn"))

        FramePipeline.apply(px, w, h, stack)

        // B/N: sin canales descompensados
        val center = px[40 * w + 40]
        val r = (center ushr 16) and 0xFF
        val g = (center ushr 8) and 0xFF
        val b = center and 0xFF
        assertTrue(abs(r - g) <= 1, "R==G en B/N: $r vs $g")
        assertTrue(abs(g - b) <= 1, "G==B en B/N: $g vs $b")
        // esquina mas oscura que el centro por la vineta
        val corner = (px[0] ushr 16) and 0xFF
        assertTrue(corner < r, "vineta: esquina $corner < centro $r")
    }

    @Test
    fun identitySkipsEverything() {
        val px = IntArray(16) { pixel(90, 90, 90) }
        val before = px.copyOf()
        val stack = EditStack()
        stack.apply(Edit.Exposure(2f))
        stack.apply(Edit.Vignette(1f))
        FramePipeline.apply(px, 4, 4, stack, identity = true)
        assertTrue(before.contentEquals(px))
    }

    @Test
    fun newEditsSerializeRoundTrip() {
        val stack = EditStack()
        stack.applySelectivePoint(ControlPoint(0.3f, 0.4f, brightness = 0.5f, radius = 0.3f))
        stack.apply(Edit.Vignette(0.4f))
        stack.applyHealStroke(HealStroke(0.05f, listOf(0.1f, 0.2f), listOf(0.3f, 0.4f)))
        stack.apply(Edit.Look("calido"))

        val restored = EditStack.fromJson(stack.toJson())
        assertEquals(1, restored.selectivePoints().size)
        assertEquals(0.3f, restored.selectivePoints()[0].x, 1e-4f)
        assertEquals(1, restored.healStrokes().size)
        val look = restored.edits.filterIsInstance<Edit.Look>().firstOrNull()
        assertEquals("calido", look?.id)
        val vig = restored.edits.filterIsInstance<Edit.Vignette>().firstOrNull()
        assertEquals(0.4f, vig?.strength)
    }

    @Test
    fun selectiveHelpersMutateStack() {
        val stack = EditStack()
        stack.applySelectivePoint(ControlPoint(0.1f, 0.1f))
        stack.applySelectivePoint(ControlPoint(0.9f, 0.9f))
        assertEquals(2, stack.selectivePoints().size)

        stack.updateSelectivePoint(0, ControlPoint(0.2f, 0.2f, brightness = 1f))
        assertEquals(0.2f, stack.selectivePoints()[0].x, 1e-4f)
        assertEquals(1f, stack.selectivePoints()[0].brightness)

        stack.removeSelectivePoint(0)
        assertEquals(1, stack.selectivePoints().size)
        assertEquals(0.9f, stack.selectivePoints()[0].x, 1e-4f)
    }

    @Test
    fun lookAccumulatesOnTopOfGlobals() {
        val stack = EditStack()
        stack.apply(Edit.Contrast(0.2f))
        stack.apply(Edit.Look("calido"))
        val a = EffectiveAdjustments.from(stack)
        assertEquals(0.2f + 0.05f, a.contrast, 1e-4f)
        assertEquals(0.35f, a.temperature, 1e-4f)
    }
}
