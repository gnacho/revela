package app.revela.core

import kotlinx.serialization.Serializable

/** Fracciones de lienzo añadidas por lado (0..1 del tamaño original). */
@Serializable
data class ExpandEdges(val left: Float = 0f, val top: Float = 0f, val right: Float = 0f, val bottom: Float = 0f) {
    fun isEmpty(eps: Float = 1e-4f): Boolean =
        left < eps && top < eps && right < eps && bottom < eps

    fun clamped(): ExpandEdges = ExpandEdges(
        left.coerceIn(0f, 0.45f), top.coerceIn(0f, 0.45f),
        right.coerceIn(0f, 0.45f), bottom.coerceIn(0f, 0.45f)
    )
}

/** Ampliacion de lienzo: replica o espeja el borde en la zona añadida. */
object ExpandMath {

    fun outputSize(w: Int, h: Int, e: ExpandEdges): Pair<Int, Int> {
        val ec = e.clamped()
        return Pair(
            (w * (1f + ec.left + ec.right)).toInt().coerceAtLeast(w),
            (h * (1f + ec.top + ec.bottom)).toInt().coerceAtLeast(h)
        )
    }

    /**
     * Devuelve el frame ampliado. mirror=false replica el borde (smart fill barato),
     * mirror=true refleja el contenido hacia dentro.
     */
    fun expand(src: IntArray, w: Int, h: Int, e: ExpandEdges, mirror: Boolean): Triple<IntArray, Int, Int> {
        val ec = e.clamped()
        if (ec.isEmpty()) return Triple(src.copyOf(), w, h)
        val (ow, oh) = outputSize(w, h, ec)
        val offX = (w * ec.left).toInt()
        val offY = (h * ec.top).toInt()
        val out = IntArray(ow * oh)
        for (y in 0 until oh) {
            val sy = mapCoord(y - offY, h, mirror)
            for (x in 0 until ow) {
                val sx = mapCoord(x - offX, w, mirror)
                out[y * ow + x] = src[sy * w + sx]
            }
        }
        return Triple(out, ow, oh)
    }

    /** Coordenada origen para una coordenada de salida relativa (puede ser negativa o mayor que n). */
    private fun mapCoord(c: Int, n: Int, mirror: Boolean): Int {
        var s = c
        if (s < 0 || s >= n) {
            s = if (mirror) {
                // reflejo triangular: ...2,1,0 | 0,1,2... | ...n-2,n-1,n-1
                val period = 2 * n
                var m = s % period
                if (m < 0) m += period
                if (m >= n) period - m - 1 else m
            } else {
                s.coerceIn(0, n - 1)
            }
        }
        return s
    }
}
