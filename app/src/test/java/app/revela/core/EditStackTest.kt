package app.revela.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EditStackTest {

    private val exposure = Edit.Exposure(0.5f)
    private val contrast = Edit.Contrast(0.2f)
    private val saturation = Edit.Saturation(-0.3f)

    @Test
    fun applyUndoRedo() {
        val s = EditStack()
        s.apply(exposure)
        s.apply(contrast)
        assertEquals(listOf<Edit>(exposure, contrast), s.edits)

        assertTrue(s.undo())
        assertEquals(listOf<Edit>(exposure), s.edits)
        assertTrue(s.canRedo)

        assertTrue(s.redo())
        assertEquals(listOf<Edit>(exposure, contrast), s.edits)

        assertTrue(s.undo())
        assertTrue(s.undo())
        assertTrue(s.edits.isEmpty())
        assertFalse(s.undo())
    }

    @Test
    fun replacingSameToolKeepsSingleEditAndUndoRestores() {
        val s = EditStack()
        s.apply(exposure)
        s.apply(contrast)
        val newExposure = Edit.Exposure(1.0f)
        s.apply(newExposure)

        assertEquals(listOf<Edit>(contrast, newExposure), s.edits)

        s.undo()
        assertEquals(listOf<Edit>(exposure, contrast), s.edits)
    }

    @Test
    fun removeToolRestoresWithUndo() {
        val s = EditStack()
        s.apply(exposure)
        s.apply(contrast)

        assertTrue(s.removeTool(Tool.EXPOSURE))
        assertEquals(listOf<Edit>(contrast), s.edits)
        assertFalse(s.removeTool(Tool.EXPOSURE))

        s.undo()
        assertEquals(listOf<Edit>(exposure, contrast), s.edits)
    }

    @Test
    fun applyClearsRedoStack() {
        val s = EditStack()
        s.apply(exposure)
        s.undo()
        assertTrue(s.canRedo)
        s.apply(contrast)
        assertFalse(s.canRedo)
    }

    @Test
    fun jsonRoundtrip() {
        val s = EditStack()
        s.apply(exposure)
        s.apply(Edit.WhiteBalance(0.3f, -0.1f))
        s.apply(Edit.Curves(listOf(CurvePoint(0f, 0f), CurvePoint(0.5f, 0.6f), CurvePoint(1f, 1f))))

        val restored = EditStack.fromJson(s.toJson())
        assertEquals(s.edits, restored.edits)
    }

    @Test
    fun resetReplacesEverything() {
        val s = EditStack()
        s.apply(exposure)
        s.reset(listOf(saturation))
        assertEquals(listOf<Edit>(saturation), s.edits)
        s.undo()
        assertEquals(listOf<Edit>(exposure), s.edits)
    }
}
