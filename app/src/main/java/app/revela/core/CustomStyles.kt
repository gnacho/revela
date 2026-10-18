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
}
