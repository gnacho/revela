package app.revela.core

import kotlinx.serialization.Serializable

@Serializable
data class CurvePoint(val x: Float, val y: Float)

/** Interpolacion cubica monota (Fritsch-Carlson) sobre puntos de control normalizados 0..1. */
object CurveMath {
    const val LUT_SIZE = 256

    val identity: FloatArray = FloatArray(LUT_SIZE) { it / 255f }

    /**
     * Genera una LUT de 256 entradas a partir de los puntos de control.
     * Los puntos se ordenan por x; fuera del rango se satura al primer/ultimo valor.
     */
    fun lut(points: List<CurvePoint>): FloatArray {
        require(points.size >= 2) { "se necesitan al menos 2 puntos" }
        val pts = points.sortedBy { it.x }
        val n = pts.size
        val dx = FloatArray(n - 1) { pts[it + 1].x - pts[it].x }
        val dy = FloatArray(n - 1) { pts[it + 1].y - pts[it].y }
        val slope = FloatArray(n - 1) { if (dx[it] == 0f) 0f else dy[it] / dx[it] }
        val tangents = FloatArray(n)
        tangents[0] = slope[0]
        tangents[n - 1] = slope[n - 2]
        for (i in 1 until n - 1) {
            tangents[i] = if (slope[i - 1] * slope[i] <= 0f) 0f else (slope[i - 1] + slope[i]) / 2f
        }
        // Limitador Fritsch-Carlson: evita sobrepasos que romperian la monotonía
        for (i in 0 until n - 1) {
            if (slope[i] == 0f) {
                tangents[i] = 0f
                tangents[i + 1] = 0f
                continue
            }
            val a = tangents[i] / slope[i]
            val b = tangents[i + 1] / slope[i]
            val s = a * a + b * b
            if (s > 9f) {
                val t = 3f / kotlin.math.sqrt(s)
                tangents[i] = t * a * slope[i]
                tangents[i + 1] = t * b * slope[i]
            }
        }
        val out = FloatArray(LUT_SIZE)
        var seg = 0
        for (i in 0 until LUT_SIZE) {
            val x = i / 255f
            while (seg < n - 2 && x > pts[seg + 1].x) seg++
            val x0 = pts[seg].x
            val x1 = pts[seg + 1].x
            val h = if (x1 - x0 == 0f) 1f else x1 - x0
            val t = ((x - x0) / h).coerceIn(0f, 1f)
            val t2 = t * t
            val h00 = (1f + 2f * t) * (1f - t) * (1f - t)
            val h10 = t * (1f - t) * (1f - t)
            val h01 = t2 * (3f - 2f * t)
            val h11 = t2 * (t - 1f)
            out[i] = (h00 * pts[seg].y + h10 * h * tangents[seg] +
                h01 * pts[seg + 1].y + h11 * h * tangents[seg + 1]).coerceIn(0f, 1f)
        }
        return out
    }
}
