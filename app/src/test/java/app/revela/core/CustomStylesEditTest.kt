package app.revela.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CustomStylesEditTest {

    private fun styles() = listOf(
        CustomStyle("a", "Calido", "{}"),
        CustomStyle("b", "Frio", "{}"),
        CustomStyle("c", "Noir", "{}")
    )

    @Test
    fun moveDownSwapsWithNext() {
        val out = CustomStylesCodec.move(styles(), "a", 1)
        assertEquals(listOf("b", "a", "c"), out.map { it.id })
    }

    @Test
    fun moveUpSwapsWithPrevious() {
        val out = CustomStylesCodec.move(styles(), "c", -1)
        assertEquals(listOf("a", "c", "b"), out.map { it.id })
    }

    @Test
    fun moveClampsAtTheEdges() {
        assertEquals(listOf("a", "b", "c"), CustomStylesCodec.move(styles(), "a", -5).map { it.id })
        assertEquals(listOf("a", "b", "c"), CustomStylesCodec.move(styles(), "c", 5).map { it.id })
    }

    @Test
    fun moveUnknownIdLeavesListAlone() {
        assertEquals(styles(), CustomStylesCodec.move(styles(), "zzz", 1))
    }

    @Test
    fun renameTrimsAndKeepsOthers() {
        val out = CustomStylesCodec.rename(styles(), "b", "  Polar  ")
        assertEquals("Polar", out.first { it.id == "b" }.name)
        assertEquals("Calido", out.first { it.id == "a" }.name)
    }

    @Test
    fun renameIgnoresBlankNames() {
        val out = CustomStylesCodec.rename(styles(), "b", "   ")
        assertEquals("Frio", out.first { it.id == "b" }.name)
    }

    @Test
    fun duplicateNamesAreDetectedIgnoringCase() {
        val s = styles()
        assertTrue(CustomStylesCodec.nameTaken(s, "calido"))
        assertTrue(CustomStylesCodec.nameTaken(s, "  CALIDO "))
        assertFalse(CustomStylesCodec.nameTaken(s, "calido", exceptId = "a"))
        assertFalse(CustomStylesCodec.nameTaken(s, "Otro"))
    }

    @Test
    fun renameSurvivesJsonRoundTrip() {
        val out = CustomStylesCodec.rename(styles(), "a", "Dorado")
        val round = CustomStylesCodec.fromJson(CustomStylesCodec.toJson(out))
        assertEquals("Dorado", round.first { it.id == "a" }.name)
        assertEquals(listOf("a", "b", "c"), round.map { it.id })
    }
}
