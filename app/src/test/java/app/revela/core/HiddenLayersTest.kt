package app.revela.core

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HiddenLayersTest {

    private fun tempDir(): File = kotlin.io.path.createTempDirectory("revela_hidden").toFile()

    @Test
    fun emptyWhenNothingSaved() {
        val dir = tempDir()
        assertTrue(HiddenLayers.load(dir, "s1").isEmpty())
    }

    @Test
    fun savesAndLoadsPerSession() {
        val dir = tempDir()
        HiddenLayers.save(dir, "s1", setOf(Tool.VIGNETTE, Tool.GRAIN))
        HiddenLayers.save(dir, "s2", setOf(Tool.VIGNETTE))
        assertEquals(setOf(Tool.VIGNETTE, Tool.GRAIN), HiddenLayers.load(dir, "s1"))
        assertEquals(setOf(Tool.VIGNETTE), HiddenLayers.load(dir, "s2"))
        assertTrue(HiddenLayers.load(dir, "s3").isEmpty())
    }

    @Test
    fun savingEmptyRemovesTheSession() {
        val dir = tempDir()
        HiddenLayers.save(dir, "s1", setOf(Tool.VIGNETTE))
        HiddenLayers.save(dir, "s1", emptySet())
        assertTrue(HiddenLayers.load(dir, "s1").isEmpty())
        assertEquals(emptySet<String>(), HiddenLayers.loadAll(dir)["s1"].orEmpty())
    }

    @Test
    fun clearIsIdempotent() {
        val dir = tempDir()
        HiddenLayers.clear(dir, "nope")
        HiddenLayers.clear(dir, "nope")
        assertTrue(HiddenLayers.loadAll(dir).isEmpty())
    }

    @Test
    fun brokenFileFallsBackToEmpty() {
        val dir = tempDir()
        File(dir, "revela_ocultas.json").writeText("{ esto no es json")
        assertTrue(HiddenLayers.load(dir, "s1").isEmpty())
    }

    @Test
    fun visibleEditsDropOnlyHidden() {
        val edits = listOf(
            Edit.Exposure(0.5f),
            Edit.Vignette(0.4f),
            Edit.Grain(0.3f)
        )
        val visible = LayerList.visibleEdits(edits, setOf(Tool.VIGNETTE))
        assertEquals(listOf(Tool.EXPOSURE, Tool.GRAIN), visible.map { it.toolId })
    }

    @Test
    fun rowsKeepStackOrderAndFlagHidden() {
        val edits = listOf(Edit.Exposure(0.5f), Edit.Vignette(0.4f), Edit.Grain(0.3f))
        val rows = LayerList.rows(edits, setOf(Tool.VIGNETTE)) { it.toolId.uppercase() }
        assertEquals(listOf("EXPOSURE", "VIGNETTE", "GRAIN"), rows.map { it.label })
        assertFalse(rows[0].hidden)
        assertTrue(rows[1].hidden)
        assertFalse(rows[2].hidden)
    }

    @Test
    fun brushLayersAreIndependent() {
        val edits = listOf(
            Edit.Brush("exposure", 0.5f),
            Edit.Brush("temperature", 0.2f)
        )
        val rows = LayerList.rows(edits, setOf("brush:exposure")) { it.toolId }
        assertEquals(2, rows.size)
        assertTrue(rows[0].hidden)
        assertFalse(rows[1].hidden)
        assertEquals(listOf("brush:temperature"), LayerList.visibleEdits(edits, setOf("brush:exposure")).map { it.toolId })
    }
}
