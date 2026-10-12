package app.revela.core

/** Estilos propios (recetas de ajustes globales acumulables sobre la pila). */
enum class Look(
    val id: String,
    val label: String,
    val dExposure: Float,
    val dContrast: Float,
    val dSaturation: Float,
    val dTemperature: Float
) {
    NONE("none", "Ninguno", 0f, 0f, 0f, 0f),
    CALIDO("calido", "Calido", 0.05f, 0.05f, 0.1f, 0.35f),
    FRIO("frio", "Frio", 0f, 0.05f, 0.05f, -0.3f),
    VIBRANTE("vibrante", "Vibrante", 0.05f, 0.15f, 0.35f, 0f),
    BYN("byn", "B/N", 0f, 0.2f, -1f, 0f),
    SUAVE("suave", "Suave", 0.1f, -0.15f, -0.05f, 0.05f),
    PELICULA("pelicula", "Pelicula", 0f, 0.1f, -0.15f, 0.15f);

    companion object {
        fun byId(id: String): Look = entries.firstOrNull { it.id == id } ?: NONE
    }
}
