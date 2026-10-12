package app.revela.core

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/** Rotacion por cuartos de 90 grados como remapeo de indices (barato, sin GPU). */
object FrameMath {

    /**
     * Gira pixels ARGB quarterTurns x 90 grados en sentido horario.
     * SIEMPRE devuelve un array nuevo (el de entrada no se toca).
     */
    fun rotateQuarter(
        pixels: IntArray,
        w: Int,
        h: Int,
        quarterTurns: Int
    ): Triple<IntArray, Int, Int> {
        val turns = ((quarterTurns % 4) + 4) % 4
        if (turns == 0) return Triple(pixels.copyOf(), w, h)
        val outW = if (turns % 2 == 0) w else h
        val outH = if (turns % 2 == 0) h else w
        val out = IntArray(pixels.size)
        for (y in 0 until h) {
            for (x in 0 until w) {
                val p = pixels[y * w + x]
                val (nx, ny) = when (turns) {
                    1 -> Pair(h - 1 - y, x)          // 90 horario
                    2 -> Pair(w - 1 - x, h - 1 - y)  // 180
                    else -> Pair(y, w - 1 - x)       // 270 horario (90 antihorario)
                }
                out[ny * outW + nx] = p
            }
        }
        return Triple(out, outW, outH)
    }

    /**
     * Aplica sobre pixels la lista de puntos de control selectivos (espacio de marco).
     * Cada punto ajusta brillo/contraste/saturacion en su zona por similitud de color
     * y decaimiento con la distancia. El radio se normaliza contra min(w, h).
     */
    fun applySelective(
        pixels: IntArray,
        w: Int,
        h: Int,
        points: List<ControlPoint>
    ) {
        if (points.isEmpty()) return
        val minSide = min(w, h).toFloat()
        for (pt in points) {
            applyPoint(pixels, w, h, pt, minSide)
        }
    }

    private fun applyPoint(pixels: IntArray, w: Int, h: Int, pt: ControlPoint, minSide: Float) {
        val cx = pt.x * w
        val cy = pt.y * h
        val rPix = max(8f, pt.radius * minSide)
        val sat = 1f + pt.saturation
        val contrastMul = 1f + pt.contrast
        val exposureMul = 2f.pow(pt.brightness * 0.5f)

        val x0 = max(0, (cx - rPix).toInt())
        val x1 = min(w - 1, (cx + rPix).toInt())
        val y0 = max(0, (cy - rPix).toInt())
        val y1 = min(h - 1, (cy + rPix).toInt())
        if (x1 < x0 || y1 < y0) return

        // color de referencia: pixel del centro (redondeado, dentro de rango)
        val refX = cx.toInt().coerceIn(0, w - 1)
        val refY = cy.toInt().coerceIn(0, h - 1)
        val ref = pixels[refY * w + refX]
        val rr = ((ref ushr 16) and 0xFF) / 255f
        val rg = ((ref ushr 8) and 0xFF) / 255f
        val rb = (ref and 0xFF) / 255f

        for (y in y0..y1) {
            for (x in x0..x1) {
                val i = y * w + x
                val p = pixels[i]
                val pr = ((p ushr 16) and 0xFF) / 255f
                val pg = ((p ushr 8) and 0xFF) / 255f
                val pb = (p and 0xFF) / 255f

                val dx = x - cx
                val dy = y - cy
                val dist = sqrt(dx * dx + dy * dy) / rPix
                if (dist >= 1f) continue
                val distW = (1f - dist) * (1f - dist)

                val colorDist = sqrt(
                    (pr - rr) * (pr - rr) + (pg - rg) * (pg - rg) + (pb - rb) * (pb - rb)
                )
                var colorW = 1f - colorDist / COLOR_SIGMA
                if (colorW <= 0f) continue
                colorW *= colorW

                val weight = distW * colorW

                // ajuste del punto sobre el valor actual
                var ar = pr * exposureMul
                var ag = pg * exposureMul
                var ab = pb * exposureMul
                ar = (ar - 0.5f) * contrastMul + 0.5f
                ag = (ag - 0.5f) * contrastMul + 0.5f
                ab = (ab - 0.5f) * contrastMul + 0.5f
                if (abs(sat - 1f) > 1e-4f) {
                    val luma = 0.2126f * ar + 0.7152f * ag + 0.0722f * ab
                    ar = luma + (ar - luma) * sat
                    ag = luma + (ag - luma) * sat
                    ab = luma + (ab - luma) * sat
                }

                pixels[i] = blendPixel(p, pr, pg, pb, ar, ag, ab, weight)
            }
        }
    }

    const val COLOR_SIGMA = 0.6f

    private fun blendPixel(
        orig: Int,
        pr: Float,
        pg: Float,
        pb: Float,
        ar: Float,
        ag: Float,
        ab: Float,
        weight: Float
    ): Int {
        val fr = pr + (ar - pr) * weight
        val fg = pg + (ag - pg) * weight
        val fb = pb + (ab - pb) * weight
        var ri = (fr * 255f + 0.5f).toInt()
        var gi = (fg * 255f + 0.5f).toInt()
        var bi = (fb * 255f + 0.5f).toInt()
        if (ri > 255) ri = 255 else if (ri < 0) ri = 0
        if (gi > 255) gi = 255 else if (gi < 0) gi = 0
        if (bi > 255) bi = 255 else if (bi < 0) bi = 0
        val a = orig and 0xFF000000.toInt()
        return a or (ri shl 16) or (gi shl 8) or bi
    }

    /** Aplica vineta radial centrada en el marco (strength 0..1). */
    fun applyVignette(pixels: IntArray, w: Int, h: Int, strength: Float) {
        if (strength <= 0f) return
        val cx = w / 2f
        val cy = h / 2f
        val maxD = sqrt(cx * cx + cy * cy)
        for (y in 0 until h) {
            for (x in 0 until w) {
                val dx = x - cx
                val dy = y - cy
                val d = sqrt(dx * dx + dy * dy) / maxD
                val fall = smoothstep(VIGNETTE_START, 1f, d)
                if (fall <= 0f) continue
                val factor = 1f - strength * fall
                val i = y * w + x
                val p = pixels[i]
                var r = ((p ushr 16) and 0xFF) * factor
                var g = ((p ushr 8) and 0xFF) * factor
                var b = (p and 0xFF) * factor
                var ri = r.toInt()
                var gi = g.toInt()
                var bi = b.toInt()
                if (ri > 255) ri = 255 else if (ri < 0) ri = 0
                if (gi > 255) gi = 255 else if (gi < 0) gi = 0
                if (bi > 255) bi = 255 else if (bi < 0) bi = 0
                pixels[i] = (p and 0xFF000000.toInt()) or (ri shl 16) or (gi shl 8) or bi
            }
        }
    }

    const val VIGNETTE_START = 0.35f

    fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }
}
