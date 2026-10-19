package app.revela.core

import androidx.annotation.StringRes
import app.revela.R

/**
 * Recetas de "panel-preset" estilo Snapseed: combinan edits YA existentes (sin stages nuevos).
 * La intensidad 0..1 escala los valores; en 0 la receta es no-op (lista vacia) salvo Noir,
 * cuya conversion a B/N solo se activa con t > 0.
 */
object Presets {

    enum class Kind(val id: String, @StringRes val labelRes: Int) {
        DRAMA("drama", R.string.tool_drama),
        VINTAGE("vintage", R.string.tool_vintage),
        GRAINY_FILM("grainyFilm", R.string.tool_grainy_film),
        RETROLUX("retrolux", R.string.tool_retrolux),
        GRUNGE("grunge", R.string.tool_grunge),
        NOIR("noir", R.string.tool_noir),
        PORTRAIT("portrait", R.string.tool_portrait);

        /** Edits que escribe el preset a una intensidad dada (no-op si t <= 0). */
        fun edits(t: Float): List<Edit> {
            if (t <= 0f) return emptyList()
            return when (this) {
                DRAMA -> listOf(
                    Edit.Contrast(0.35f * t),
                    Edit.Saturation(-0.15f * t),
                    Edit.Details(structure = 0.5f * t)
                )
                VINTAGE -> listOf(
                    Edit.WhiteBalance(temperature = 0.4f * t, tint = 0f),
                    Edit.Saturation(-0.2f * t),
                    Edit.Vignette(0.35f * t),
                    Edit.Grain(0.25f * t)
                )
                GRAINY_FILM -> listOf(
                    Edit.Saturation(-0.25f * t),
                    Edit.Grain(0.6f * t),
                    Edit.Contrast(0.1f * t)
                )
                RETROLUX -> listOf(
                    Edit.Contrast(0.3f * t),
                    Edit.WhiteBalance(temperature = 0.25f * t, tint = 0f),
                    Edit.Vignette(0.5f * t),
                    Edit.Grain(0.4f * t)
                )
                GRUNGE -> listOf(
                    Edit.Contrast(0.5f * t),
                    Edit.Saturation(0.2f * t),
                    Edit.Grain(0.5f * t)
                )
                NOIR -> listOf(
                    Edit.Bnw("neutral"),
                    Edit.Contrast(0.35f * t)
                )
                PORTRAIT -> listOf(
                    Edit.Details(structure = -0.3f * t),
                    Edit.Saturation(-0.05f * t),
                    Edit.Glow(0.15f * t)
                )
            }
        }

        /** toolIds que toca el preset (para Restablecer y para sustituir en la preview en vivo). */
        fun toolIds(): Set<String> = edits(1f).map { it.toolId }.toSet()
    }
}
