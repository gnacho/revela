package app.revela.render

import android.content.Context
import app.revela.core.CustomStyle
import app.revela.core.CustomStylesCodec
import java.io.File

/** Persistencia de los estilos personalizados del usuario (JSON en filesDir). */
object CustomStylesStore {

    private fun file(context: Context) = File(context.filesDir, "revela_estilos.json")

    fun load(context: Context): List<CustomStyle> =
        runCatching { CustomStylesCodec.fromJson(file(context).readText()) }
            .getOrDefault(emptyList())

    fun save(context: Context, styles: List<CustomStyle>) {
        runCatching { file(context).writeText(CustomStylesCodec.toJson(styles)) }
    }
}
