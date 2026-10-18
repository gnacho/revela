package app.revela.core

/** 2a capa de doble exposicion en sus dimensiones originales (la transformacion la aplica el pase). */
data class OverlayFrame(val pixels: IntArray, val w: Int, val h: Int)

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
        adjOverride: ColorMath.Adjustments? = null,
        overlay: OverlayFrame? = null
    ) {
        if (identity) return

        // 1) sanar: cada edit Heal rellena sus trazos sobre el marco
        for (e in edits) {
            if (e is Edit.Heal && e.strokes.isNotEmpty()) {
                val mask = HealMath.buildMask(e.strokes, w, h)
                HealMath.inpaint(pixels, mask, w, h)
            }
        }

        // 1b) doble exposicion: mezcla la 2a capa (con transformacion) sobre la base
        for (e in edits) {
            if (e is Edit.DoubleExpo && overlay != null) {
                FrameMath.applyDoubleExpo(
                    pixels, overlay.pixels, overlay.w, overlay.h, w, h,
                    e.blend, e.opacity, e.scale, e.rotationDeg, e.offsetX, e.offsetY
                )
                break
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

        // 2c) efectos tonales espaciales (detalles, HDR, brillo glamuroso, desenfoque)
        for (e in edits) {
            when (e) {
                is Edit.Details -> FrameMath.applyDetails(pixels, w, h, e.structure, e.sharpening)
                is Edit.Hdr -> FrameMath.applyHdr(pixels, w, h, e.strength)
                is Edit.Glow -> FrameMath.applyGlow(pixels, w, h, e.strength)
                is Edit.LensBlur -> FrameMath.applyLensBlur(pixels, w, h, e.strength)
                else -> {}
            }
        }

        // 3) selectivo + 3b) pincel + 4) vineta
        for (e in edits) {
            when (e) {
                is Edit.Selective -> FrameMath.applySelective(pixels, w, h, e.points)
                is Edit.Vignette -> FrameMath.applyVignette(pixels, w, h, e.strength)
                else -> {}
            }
        }
        for (e in edits) {
            if (e is Edit.Brush && e.strokes.isNotEmpty()) {
                BrushMath.apply(pixels, w, h, e.param, e.amount, BrushMath.buildDensity(e.strokes, w, h))
                break
            }
        }

        // 5) grano (encima de todo, para simular pelicula)
        for (e in edits) {
            if (e is Edit.Grain) {
                FrameMath.applyGrain(pixels, w, h, e.strength)
                break
            }
        }
    }
}
