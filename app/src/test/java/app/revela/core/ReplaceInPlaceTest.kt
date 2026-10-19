package app.revela.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReplaceInPlaceTest {

    @Test
    fun replacesWithoutMovingTheLayer() {
        val stack = EditStack(listOf(Edit.Exposure(0.5f), Edit.Vignette(0.4f), Edit.Grain(0.3f)))
        assertTrue(stack.replaceInPlace(Edit.Vignette(0.9f)))
        assertEquals(listOf(Tool.EXPOSURE, Tool.VIGNETTE, Tool.GRAIN), stack.edits.map { it.toolId })
        assertEquals(0.9f, (stack.edits[1] as Edit.Vignette).strength)
    }

    @Test
    fun returnsFalseWhenTheLayerIsNotThere() {
        val stack = EditStack(listOf(Edit.Exposure(0.5f)))
        assertFalse(stack.replaceInPlace(Edit.Grain(0.3f)))
        assertEquals(1, stack.edits.size)
    }

    @Test
    fun replaceIsUndoable() {
        val stack = EditStack(listOf(Edit.Vignette(0.4f)))
        stack.replaceInPlace(Edit.Vignette(0.9f))
        assertTrue(stack.undo())
        assertEquals(0.4f, (stack.edits[0] as Edit.Vignette).strength)
    }

    @Test
    fun applyStillMovesToTheEnd() {
        val stack = EditStack(listOf(Edit.Exposure(0.5f), Edit.Vignette(0.4f)))
        stack.apply(Edit.Exposure(0.8f))
        assertEquals(listOf(Tool.VIGNETTE, Tool.EXPOSURE), stack.edits.map { it.toolId })
    }
}
