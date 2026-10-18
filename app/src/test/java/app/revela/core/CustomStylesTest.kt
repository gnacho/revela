package app.revela.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CustomStylesTest {

    @Test
    fun `codec json ida y vuelta conserva estilos y edits`() {
        val edits = EditStack(listOf(
            Edit.Exposure(0.5f),
            Edit.Contrast(-0.2f),
            Edit.Bnw("red"),
            Edit.TonalContrast(0.1f, 0f, -0.3f)
        ))
        val styles = listOf(
            CustomStyle("c1", "Atardecer", edits.toJson()),
            CustomStyle("c2", "Suave", EditStack(listOf(Edit.Saturation(0.3f))).toJson())
        )
        val text = CustomStylesCodec.toJson(styles)
        val back = CustomStylesCodec.fromJson(text)
        assertEquals(2, back.size)
        assertEquals("Atardecer", back[0].name)
        val st = EditStack.fromJson(back[0].editsJson)
        assertTrue(st.edits.any { it is Edit.Bnw && it.filter == "red" })
        assertTrue(st.edits.any { it is Edit.TonalContrast && it.highlights == -0.3f })
    }

    @Test
    fun `json corrupto devuelve lista vacia sin lanzar`() {
        assertEquals(emptyList(), CustomStylesCodec.fromJson("{no json"))
    }

    @Test
    fun `marca de bienvenida seen y reset`() {
        val dir = kotlin.io.path.createTempDirectory("welcome").toFile()
        WelcomeStore.markSeen(dir)
        assertTrue(WelcomeStore.isSeen(dir))
        WelcomeStore.reset(dir)
        assertEquals(false, WelcomeStore.isSeen(dir))
    }
}
