package app.revela.render

import app.revela.core.Edit
import app.revela.core.EditStack
import app.revela.core.GeometryModel
import app.revela.core.SessionIndexCodec
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionIndexStoreTest {

    private fun tmpDir(): File = createTempDirectory("revela-index").toFile()

    private fun stackWithExposure(): EditStack = EditStack(listOf(Edit.Exposure(0.5f)))

    @Test
    fun `upsert crea entrada con nombre decodificado y la lista la devuelve`() {
        val dir = tmpDir()
        val id = SessionIndexStore.upsert(dir, "content://media/external/images/media/1000", stackWithExposure(), GeometryModel(), now = 1000L)
        val entries = SessionIndexStore.list(dir)
        assertEquals(1, entries.size)
        assertEquals(id, entries[0].id)
        assertEquals("1000", entries[0].displayName)
        assertEquals(1000L, entries[0].lastOpenedAt)
    }

    @Test
    fun `display name decodifica escapes y acorta`() {
        assertEquals("mi foto.jpg", SessionIndexCodec.displayNameFromUri("file:///sdcard/DCIM/mi%20foto.jpg"))
        assertEquals("1000", SessionIndexCodec.displayNameFromUri("content://media/external/images/media/1000"))
        val long = "a".repeat(60)
        assertEquals(42, SessionIndexCodec.displayNameFromUri("file:///x/$long").length)
    }

    @Test
    fun `segundo upsert de la misma uri no duplica y sube lastOpenedAt`() {
        val dir = tmpDir()
        val uri = "content://media/external/images/media/2000"
        val id1 = SessionIndexStore.upsert(dir, uri, stackWithExposure(), GeometryModel(), now = 1000L)
        val id2 = SessionIndexStore.upsert(dir, uri, stackWithExposure(), GeometryModel(), now = 2000L)
        assertEquals(id1, id2)
        val entries = SessionIndexStore.list(dir)
        assertEquals(1, entries.size)
        assertEquals(2000L, entries[0].lastOpenedAt)
    }

    @Test
    fun `list ordena por ultimo uso descendente`() {
        val dir = tmpDir()
        SessionIndexStore.upsert(dir, "content://a/1", stackWithExposure(), GeometryModel(), now = 1000L)
        SessionIndexStore.upsert(dir, "content://a/2", stackWithExposure(), GeometryModel(), now = 3000L)
        SessionIndexStore.upsert(dir, "content://a/3", stackWithExposure(), GeometryModel(), now = 2000L)
        val names = SessionIndexStore.list(dir).map { it.displayName }
        assertEquals(listOf("2", "3", "1"), names)
    }

    @Test
    fun `load devuelve el stack y la geometry guardados`() {
        val dir = tmpDir()
        val geo = GeometryModel(quarterTurns = 1)
        val id = SessionIndexStore.upsert(dir, "content://a/9", stackWithExposure(), geo, now = 1000L)
        val loaded = SessionIndexStore.load(dir, id)
        assertNotNull(loaded)
        assertEquals(1, loaded.first.edits.size)
        assertTrue(loaded.first.edits.any { it is Edit.Exposure && it.stops == 0.5f })
        assertEquals(1, loaded.second.quarterTurns)
    }

    @Test
    fun `delete borra entrada sesion y thumb`() {
        val dir = tmpDir()
        val id = SessionIndexStore.upsert(dir, "content://a/5", stackWithExposure(), GeometryModel(), now = 1000L)
        SessionIndexStore.saveThumb(dir, id, byteArrayOf(1, 2, 3))
        assertTrue(File(dir, "thumbs/$id.png").exists())
        SessionIndexStore.delete(dir, id)
        assertEquals(emptyList(), SessionIndexStore.list(dir))
        assertFalse(File(dir, "sessions/$id.json").exists())
        assertFalse(File(dir, "thumbs/$id.png").exists())
    }

    @Test
    fun `saveThumb enlaza la ruta en el indice`() {
        val dir = tmpDir()
        val id = SessionIndexStore.upsert(dir, "content://a/7", stackWithExposure(), GeometryModel(), now = 1000L)
        SessionIndexStore.saveThumb(dir, id, byteArrayOf(9))
        val entry = SessionIndexStore.list(dir).single()
        assertEquals("thumbs/$id.png", entry.thumbPath)
        assertTrue(File(dir, entry.thumbPath).exists())
    }

    @Test
    fun `prune recorta a MAX_SESSIONS borrando las mas viejas con su thumb`() {
        val dir = tmpDir()
        val total = SessionIndexStore.MAX_SESSIONS + 2
        // Flujo real: por cada sesion, upsert + thumb ANTES de la siguiente
        val ids = (1..total).map {
            val id = SessionIndexStore.upsert(dir, "content://a/$it", stackWithExposure(), GeometryModel(), now = it.toLong())
            SessionIndexStore.saveThumb(dir, id, byteArrayOf(0))
            id
        }
        val entries = SessionIndexStore.list(dir)
        assertEquals(SessionIndexStore.MAX_SESSIONS, entries.size)
        // Las dos mas viejas (now=1 y now=2) fueron evictadas
        assertFalse(entries.any { it.lastOpenedAt <= 2L })
        val evictedIds = ids.take(2)
        for (id in evictedIds) {
            assertFalse(File(dir, "sessions/$id.json").exists())
            assertFalse(File(dir, "thumbs/$id.png").exists())
        }
        // El resto de thumbs sigue vivo
        assertTrue(File(dir, "thumbs/${ids.last()}.png").exists())
    }

    @Test
    fun `migrateLegacy importa revela_sessionjson cuando no hay indice`() {
        val dir = tmpDir()
        @Serializable
        data class LegacySession(val uri: String, val editsJson: String, val geometry: GeometryModel)

        val legacy = File(dir, "revela_session.json")
        legacy.writeText(
            Json.encodeToString(
                LegacySession.serializer(),
                LegacySession("content://media/legacy/1", stackWithExposure().toJson(), GeometryModel())
            )
        )
        SessionIndexStore.migrateLegacy(dir)
        val entries = SessionIndexStore.list(dir)
        assertEquals(1, entries.size)
        assertEquals("1", entries[0].displayName)
        assertFalse(legacy.exists(), "el legacy se borra tras importar")
        val loaded = SessionIndexStore.load(dir, entries[0].id)
        assertNotNull(loaded)
        assertEquals(1, loaded.first.edits.size)
    }

    @Test
    fun `migrateLegacy no toca nada si el indice ya existe`() {
        val dir = tmpDir()
        @Serializable
        data class LegacySession(val uri: String, val editsJson: String, val geometry: GeometryModel)

        SessionIndexStore.upsert(dir, "content://a/1", stackWithExposure(), GeometryModel(), now = 1000L)
        File(dir, "revela_session.json").writeText(
            Json.encodeToString(
                LegacySession.serializer(),
                LegacySession("content://media/legacy/9", "[]", GeometryModel())
            )
        )
        SessionIndexStore.migrateLegacy(dir)
        assertEquals(listOf("1"), SessionIndexStore.list(dir).map { it.displayName })
    }

    @Test
    fun `load de id inexistente devuelve null`() {
        assertNull(SessionIndexStore.load(tmpDir(), "nope"))
    }
}
