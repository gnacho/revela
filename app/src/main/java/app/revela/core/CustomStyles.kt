package app.revela.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Estilo personal creado por el usuario a partir de los ajustes actuales de una foto. */
@Serializable
data class CustomStyle(
    val id: String,
    val name: String,
    val editsJson: String
)

/** Codec JSON puro (testeable en JVM) de la lista de estilos personalizados. */
object CustomStylesCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun toJson(styles: List<CustomStyle>): String =
        json.encodeToString(ListSerializer(CustomStyle.serializer()), styles)

    fun fromJson(text: String): List<CustomStyle> = runCatching {
        json.decodeFromString(ListSerializer(CustomStyle.serializer()), text)
    }.getOrDefault(emptyList())

    /**
     * Reordena un estilo dentro de la lista (deja el resto como estaban).
     * El orden es el que ve el carrusel, asi que mover es tan simple como cambiar indices.
     */
    fun move(styles: List<CustomStyle>, id: String, delta: Int): List<CustomStyle> {
        val from = styles.indexOfFirst { it.id == id }
        if (from < 0) return styles
        val to = (from + delta).coerceIn(0, styles.size - 1)
        if (to == from) return styles
        val list = styles.toMutableList()
        list.add(to, list.removeAt(from))
        return list
    }

    /** Renombra un estilo. Un nombre vacio o solo espacios deja el anterior. */
    fun rename(styles: List<CustomStyle>, id: String, name: String): List<CustomStyle> {
        val clean = name.trim()
        if (clean.isEmpty()) return styles
        return styles.map { if (it.id == id) it.copy(name = clean) else it }
    }

    /** True si ya existe otro estilo con ese nombre (sin distinguir mayusculas ni espacios sobrantes). */
    fun nameTaken(styles: List<CustomStyle>, name: String, exceptId: String? = null): Boolean {
        val clean = name.trim().lowercase()
        return styles.any { it.id != exceptId && it.name.trim().lowercase() == clean }
    }
}
