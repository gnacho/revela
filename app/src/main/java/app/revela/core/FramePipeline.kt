package app.revela.core

/**
 * Pipeline de render del marco (espacio de imagen girada, ANTES del recorte),
 * compartido por preview y export para que el resultado sea identico.
 *
 * Orden: sanar (inpaint) -> color global (LUTs + saturacion) -> puntos selectivos -> vineta.
 * El recorte y el enderezado fino se aplican fuera (transformacion final).
 */
object FramePipeline {

    fun apply(
        pixels: IntArray,
        w: Int,
        h: Int,
        stack: EditStack,
        edits: List<Edit> = stack.edits,
        identity: Boolean = false,
        adjOverride: ColorMath.Adjustments? = null
    ) {
        if (identity) return

        // 1) sanar: cada edit Heal rellena sus trazos sobre el marco
        for (e in edits) {
            if (e is Edit.Heal && e.strokes.isNotEmpty()) {
                val mask = HealMath.buildMask(e.strokes, w, h)
                HealMath.inpaint(pixels, mask, w, h)
            }
        }

        // 2) color global (desde la lista de edits pasada, o del override en vivo)
        val adj = adjOverride ?: EffectiveAdjustments.fromEdits(edits)
        ColorMath.applyInPlace(pixels, ColorMath.buildLuts(adj), adj.saturation)

        // 2b) blanco y negro: pesos de filtro por canal (tramo final de color)
        for (e in edits) {
            if (e is Edit.Bnw) {
                applyBnw(pixels, e.filter)
                break
            }
        }

        // 3) selectivo + 4) vineta
        for (e in edits) {
            when (e) {
                is Edit.Selective -> FrameMath.applySelective(pixels, w, h, e.points)
                is Edit.Vignette -> FrameMath.applyVignette(pixels, w, h, e.strength)
                else -> {}
            }
        }
    }
}
