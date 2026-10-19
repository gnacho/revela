package app.revela.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Capas ocultas de una sesion (ids de herramienta). Se guardan aparte del stack para no
 * cambiar el formato de las sesiones ya existentes: ocultar una capa es reversible sin
 * perder sus valores, y volver a mostrarla la deja como estaba.
 */
@Serializable
data class HiddenLayersState(val hidden: List<String> = emptyList())

object HiddenLayers {

    private const val FILE = "revela_ocultas.json"
    private val json = Json { ignoreUnknownKeys = true }

    /** Todas las capas ocultas por sesion (clave: id de sesion). */
    fun loadAll(dir: File): Map<String, Set<String>> = runCatching {
        json.decodeFromString(HiddenLayersState.serializer(), File(dir, FILE).readText())
            .hidden
            .mapNotNull { row ->
                val parts = row.split('|', limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.toSet() }
    }.getOrDefault(emptyMap())

    fun load(dir: File, sessionId: String): Set<String> = loadAll(dir)[sessionId].orEmpty()

    fun save(dir: File, sessionId: String, hidden: Set<String>) {
        val all = loadAll(dir).toMutableMap()
        if (hidden.isEmpty()) all.remove(sessionId) else all[sessionId] = hidden
        val rows = all.flatMap { (id, set) -> set.map { "$id|$it" } }
        runCatching {
            File(dir, FILE).writeText(json.encodeToString(HiddenLayersState.serializer(), HiddenLayersState(rows)))
        }
    }

    fun clear(dir: File, sessionId: String) = save(dir, sessionId, emptySet())
}
