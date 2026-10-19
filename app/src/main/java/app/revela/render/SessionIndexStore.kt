package app.revela.render

import app.revela.core.Edit
import app.revela.core.EditStack
import app.revela.core.GeometryModel
import app.revela.core.SessionEntry
import app.revela.core.SessionIndexCodec
import java.io.File
import java.security.MessageDigest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Biblioteca de sesiones (HOME): un fichero JSON de indice + una sesion por fichero
 * (sessions/<id>.json) + una miniatura por fichero (thumbs/<id>.png), todo bajo filesDir.
 * Trabaja sobre File para ser testeable en JVM.
 */
object SessionIndexStore {
    const val MAX_SESSIONS = 60

    private const val INDEX_FILE = "revela_index.json"
    private const val LEGACY_FILE = "revela_session.json"

    @Serializable
    private data class SessionData(
        val uri: String,
        val editsJson: String,
        val geometry: GeometryModel
    )

    private val json = Json { ignoreUnknownKeys = true }

    private fun indexFile(dir: File) = File(dir, INDEX_FILE)
    private fun sessionsDir(dir: File) = File(dir, "sessions").apply { mkdirs() }
    private fun thumbsDir(dir: File) = File(dir, "thumbs").apply { mkdirs() }
    private fun sessionFile(dir: File, id: String) = File(sessionsDir(dir), "$id.json")
    private fun thumbFile(dir: File, id: String) = File(thumbsDir(dir), "$id.png")
    private fun thumbRelPath(id: String) = "thumbs/$id.png"

    fun idFor(uri: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(uri.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.take(16)
    }

    fun list(dir: File): List<SessionEntry> =
        SessionIndexCodec.fromJson(indexFile(dir).readTextIfExists())
            .sortedByDescending { it.lastOpenedAt }

    fun load(dir: File, id: String): Pair<EditStack, GeometryModel>? = runCatching {
        val data = json.decodeFromString(SessionData.serializer(), sessionFile(dir, id).readText())
        Pair(EditStack.fromJson(data.editsJson), data.geometry)
    }.getOrNull()

    /** Guarda/actualiza la sesion de una URI y devuelve su id en el indice. */
    fun upsert(
        dir: File,
        uri: String,
        stack: EditStack,
        geometry: GeometryModel,
        now: Long = System.currentTimeMillis()
    ): String {
        val id = idFor(uri)
        runCatching {
            sessionFile(dir, id).writeText(
                json.encodeToString(
                    SessionData.serializer(),
                    SessionData(uri, stack.toJson(), geometry)
                )
            )
            val prev = SessionIndexCodec.fromJson(indexFile(dir).readTextIfExists())
            val thumbPath = prev.firstOrNull { it.id == id }?.thumbPath
                ?: if (thumbFile(dir, id).exists()) thumbRelPath(id) else ""
            val note = prev.firstOrNull { it.id == id }?.note ?: ""
            val entries = prev.filterNot { it.id == id }
            val updated = entries + SessionEntry(
                id = id,
                uri = uri,
                displayName = SessionIndexCodec.displayNameFromUri(uri),
                thumbPath = thumbPath,
                lastOpenedAt = now,
                note = note
            )
            indexFile(dir).writeText(SessionIndexCodec.toJson(updated))
            prune(dir, updated)
        }
        return id
    }

    /** Escribe la miniatura (PNG ya comprimido) de una sesion y la enlaza en el indice. */
    fun saveThumb(dir: File, id: String, pngBytes: ByteArray) {
        runCatching {
            val f = thumbFile(dir, id)
            f.writeBytes(pngBytes)
            val entries = SessionIndexCodec.fromJson(indexFile(dir).readTextIfExists())
            val updated = entries.map {
                if (it.id == id) it.copy(thumbPath = thumbRelPath(id)) else it
            }
            indexFile(dir).writeText(SessionIndexCodec.toJson(updated))
        }
    }

    fun delete(dir: File, id: String) {
        sessionFile(dir, id).delete()
        thumbFile(dir, id).delete()
        val entries = SessionIndexCodec.fromJson(indexFile(dir).readTextIfExists())
            .filterNot { it.id == id }
        indexFile(dir).writeText(SessionIndexCodec.toJson(entries))
    }

    /** Nota libre asociada a una sesion (cadena vacia = sin nota). Devuelve true si actualizo una entrada. */
    fun setNote(dir: File, id: String, note: String): Boolean = runCatching {
        val entries = SessionIndexCodec.fromJson(indexFile(dir).readTextIfExists())
        if (entries.none { it.id == id }) return@runCatching false
        val updated = entries.map { if (it.id == id) it.copy(note = note) else it }
        indexFile(dir).writeText(SessionIndexCodec.toJson(updated))
        true
    }.getOrDefault(false)

    /** True si la sesion tiene al menos un Edit.Look aplicado. */
    fun hasLook(dir: File, id: String): Boolean =
        load(dir, id)?.first?.edits?.any { it is Edit.Look } == true

    /** Importa la sesion unica legacy (revela_session.json) si el indice todavia no existe. */
    fun migrateLegacy(dir: File) {
        if (indexFile(dir).exists()) return
        val legacy = File(dir, LEGACY_FILE)
        if (!legacy.exists()) return
        runCatching {
            val data = json.decodeFromString(SessionData.serializer(), legacy.readText())
            val id = upsert(dir, data.uri, EditStack.fromJson(data.editsJson), data.geometry)
            if (sessionFile(dir, id).exists()) legacy.delete()
        }
    }

    private fun prune(dir: File, entries: List<SessionEntry>) {
        if (entries.size <= MAX_SESSIONS) return
        val evict = entries.sortedByDescending { it.lastOpenedAt }.drop(MAX_SESSIONS)
        val keep = entries.toMutableList()
        for (e in evict) {
            sessionFile(dir, e.id).delete()
            if (e.thumbPath.isNotEmpty()) File(dir, e.thumbPath).delete()
            keep.removeAll { it.id == e.id }
        }
        indexFile(dir).writeText(SessionIndexCodec.toJson(keep))
    }

    private fun File.readTextIfExists(): String = if (exists()) readText() else ""
}
