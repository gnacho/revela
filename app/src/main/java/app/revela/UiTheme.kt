package app.revela

import androidx.compose.ui.graphics.Color

/**
 * Paleta clara estilo Snapseed: fondos gris claro, texto e iconos grises,
 * acento azul en seleccionados. Los overlays que dibujan SOBRE la foto
 * (puntos de control, recorte, mascara) mantienen blanco/negro puros por contraste.
 *
 * La paleta se cambia con [setDark] antes de componer la UI: el tema oscuro solo
 * intercambia los grises (los overlays sobre la foto no dependen de esto).
 */
object Ui {
    var dark: Boolean = false
        private set

    var bg = Color(0xFFE3E3E3)
        private set
    var panel = Color(0xFFF4F4F4)
        private set
    var sheet = Color(0xFFFAFAFA)
        private set
    var card = Color.White
        private set
    var text = Color(0xFF424242)
        private set
    var textDim = Color(0xFF8A8A8A)
        private set
    var icon = Color(0xFF616161)
        private set
    var line = Color(0xFFBDBDBD)
        private set
    val accent = Color(0xFF1E88E5)

    fun setDark(value: Boolean) {
        dark = value
        if (value) {
            bg = Color(0xFF1B1B1B)
            panel = Color(0xFF242424)
            sheet = Color(0xFF2A2A2A)
            card = Color(0xFF303030)
            text = Color(0xFFE8E8E8)
            textDim = Color(0xFF9E9E9E)
            icon = Color(0xFFBDBDBD)
            line = Color(0xFF4A4A4A)
        } else {
            bg = Color(0xFFE3E3E3)
            panel = Color(0xFFF4F4F4)
            sheet = Color(0xFFFAFAFA)
            card = Color.White
            text = Color(0xFF424242)
            textDim = Color(0xFF8A8A8A)
            icon = Color(0xFF616161)
            line = Color(0xFFBDBDBD)
        }
    }
}
