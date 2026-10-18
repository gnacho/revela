package app.revela.core

import java.io.File
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/** Favoritos de la barra rapida: lista ordenada de ids de herramienta (tune, crop, heal...). */
object FavoritesStore {
    private const val FILE = "revela_favoritos.json"

    /** Valor inicial razonable hasta que el usuario personalice. */
    val DEFAULT: List<String> = listOf("tune", "crop", "heal")

    private val json = Json { ignoreUnknownKeys = true }

    fun load(dir: File): List<String> {
        val f = File(dir, FILE)
        if (!f.exists()) return DEFAULT
        return runCatching {
            json.decodeFromString(ListSerializer(String.serializer()), f.readText())
        }.getOrDefault(DEFAULT)
    }

    fun save(dir: File, ids: List<String>) {
        runCatching {
            File(dir, FILE).writeText(json.encodeToString(ListSerializer(String.serializer()), ids))
        }
    }
}
