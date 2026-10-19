package app.revela

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TutorialsTest {

    @Test
    fun everyCatalogToolHasATip() {
        val missing = toolCatalog().map { it.first.id }.filter { Tutorials.tipFor(it, "es") == null }
        assertEquals(emptyList(), missing, "herramientas sin tutorial")
    }

    @Test
    fun spanishAndEnglishDiffer() {
        val es = Tutorials.tipFor(Tool_TOOL_TUNE, "es")
        val en = Tutorials.tipFor(Tool_TOOL_TUNE, "en")
        assertNotNull(es)
        assertNotNull(en)
        assertTrue(es != en)
    }

    @Test
    fun systemLanguageFallsBackToEnglish() {
        val system = Tutorials.tipFor(Tool_TOOL_CROP, "system")
        val en = Tutorials.tipFor(Tool_TOOL_CROP, "en")
        assertEquals(en, system)
    }

    @Test
    fun languageTagsWithRegionAreSpanish() {
        assertEquals(Tutorials.tipFor(Tool_TOOL_CROP, "es"), Tutorials.tipFor(Tool_TOOL_CROP, "es-ES"))
    }

    @Test
    fun brushParamResolvesToTheBrushTip() {
        assertEquals(Tutorials.tipFor("brush", "es"), Tutorials.tipFor("brush:temperature", "es"))
    }

    @Test
    fun unknownToolHasNoTip() {
        assertNull(Tutorials.tipFor("no-existe", "es"))
    }

    /** Ids del catalogo que se usan en el test (evitan depender de constantes de otro paquete). */
    private companion object {
        const val Tool_TOOL_TUNE = "tune"
        const val Tool_TOOL_CROP = "crop"
    }
}
