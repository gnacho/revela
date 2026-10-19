package app.revela.core

import java.net.URLDecoder
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Entrada de la biblioteca de sesiones (HOME): identidad por URI, miniatura y fecha de ultimo uso. */
@Serializable
data class SessionEntry(
    val id: String,
    val uri: String,
    val displayName: String,
    val thumbPath: String,
    val lastOpenedAt: Long,
    val note: String = ""
)

/** Codec JSON puro (testeable en JVM) del indice de sesiones. */
object SessionIndexCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun toJson(entries: List<SessionEntry>): String =
        json.encodeToString(ListSerializer(SessionEntry.serializer()), entries)

    fun fromJson(text: String): List<SessionEntry> = runCatching {
        json.decodeFromString(ListSerializer(SessionEntry.serializer()), text)
    }.getOrDefault(emptyList())

    /** Nombre visible a partir de la URI: ultimo segmento decodificado, acortado. */
    fun displayNameFromUri(uri: String): String {
        val seg = uri.trimEnd('/').substringAfterLast('/').ifBlank { "foto" }
        val decoded = runCatching { URLDecoder.decode(seg, Charsets.UTF_8.name()) }.getOrDefault(seg)
        val name = decoded.substringAfterLast(':').ifBlank { decoded }
        return if (name.length > 42) name.take(39) + "..." else name
    }
}
