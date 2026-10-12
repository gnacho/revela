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
        identity: Boolean = false
    ) {
        if (identity) return

        // 1) sanar: cada edit Heal rellena sus trazos sobre el marco
        for (e in edits) {
            if (e is Edit.Heal && e.strokes.isNotEmpty()) {
                val mask = HealMath.buildMask(e.strokes, w, h)
                HealMath.inpaint(pixels, mask, w, h)
            }
        }

        // 2) color global
        val adj = EffectiveAdjustments.from(stack)
        ColorMath.applyInPlace(pixels, ColorMath.buildLuts(adj), adj.saturation)

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
