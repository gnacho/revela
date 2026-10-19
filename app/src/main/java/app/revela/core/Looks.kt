package app.revela.core

import androidx.annotation.StringRes
import app.revela.R

/** Estilos propios (recetas de ajustes globales acumulables sobre la pila). */
enum class Look(
    val id: String,
    @StringRes val labelRes: Int,
    val dExposure: Float,
    val dContrast: Float,
    val dSaturation: Float,
    val dTemperature: Float
) {
    NONE("none", R.string.look_none, 0f, 0f, 0f, 0f),
    CALIDO("calido", R.string.look_warm, 0.05f, 0.05f, 0.1f, 0.35f),
    FRIO("frio", R.string.look_cool, 0f, 0.05f, 0.05f, -0.3f),
    VIBRANTE("vibrante", R.string.look_vibrant, 0.05f, 0.15f, 0.35f, 0f),
    BYN("byn", R.string.tool_bnw, 0f, 0.2f, -1f, 0f),
    SUAVE("suave", R.string.look_soft, 0.1f, -0.15f, -0.05f, 0.05f),
    PELICULA("pelicula", R.string.look_film, 0f, 0.1f, -0.15f, 0.15f);

    companion object {
        fun byId(id: String): Look = entries.firstOrNull { it.id == id } ?: NONE
    }
}
