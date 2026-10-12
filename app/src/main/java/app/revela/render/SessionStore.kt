package app.revela.render

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import app.revela.core.EditStack
import app.revela.core.GeometryModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Persistencia ligera de la ultima sesion de edicion (solo se restaura si es la misma imagen). */
object SessionStore {
    @Serializable
    private data class SessionData(
        val uri: String,
        val editsJson: String,
        val geometry: GeometryModel
    )

    private val json = Json { ignoreUnknownKeys = true }

    private fun file(context: Context) = java.io.File(context.filesDir, "revela_session.json")

    fun save(context: Context, uri: Uri, stack: EditStack, geometry: GeometryModel) {
        runCatching {
            val data = SessionData(uri.toString(), stack.toJson(), geometry)
            file(context).writeText(json.encodeToString(SessionData.serializer(), data))
        }
    }

    fun load(context: Context, uri: Uri): Pair<EditStack, GeometryModel>? = runCatching {
        val f = file(context)
        if (!f.exists()) return null
        val data = json.decodeFromString(SessionData.serializer(), f.readText())
        if (data.uri != uri.toString()) return null
        val stack = EditStack.fromJson(data.editsJson)
        Pair(stack, data.geometry)
    }.getOrNull()

    fun clear(context: Context) {
        runCatching { file(context).delete() }
    }
}
