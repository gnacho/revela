package app.revela

import androidx.compose.ui.graphics.Color

/**
 * Paleta clara estilo Snapseed: fondos gris claro, texto e iconos grises,
 * acento azul en seleccionados. Los overlays que dibujan SOBRE la foto
 * (puntos de control, recorte, mascara) mantienen blanco/negro puros por contraste.
 */
object Ui {
    val bg = Color(0xFFE3E3E3)        // fondo general del editor y la home
    val panel = Color(0xFFF4F4F4)     // paneles de herramientas deslizantes
    val sheet = Color(0xFFFAFAFA)     // hoja de la rejilla de herramientas
    val card = Color.White            // tarjetas (home, dialogos)
    val text = Color(0xFF424242)
    val textDim = Color(0xFF8A8A8A)
    val icon = Color(0xFF616161)
    val accent = Color(0xFF1E88E5)
    val line = Color(0xFFBDBDBD)
}
