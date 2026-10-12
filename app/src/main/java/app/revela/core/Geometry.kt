package app.revela.core

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/** Geometria no destructiva: giros de 90 grados, enderezado y recorte normalizado. */
@Serializable
data class GeometryModel(
    val quarterTurns: Int = 0,          // 0..3, sentido horario
    val straightenDeg: Float = 0f,      // -45..45
    // Recorte normalizado dentro del encuadre girado/enderezado
    val cropLeft: Float = 0f,
    val cropTop: Float = 0f,
    val cropRight: Float = 1f,
    val cropBottom: Float = 1f
) {
    val totalRotationDeg: Float
        get() = ((quarterTurns % 4) * 90f + straightenDeg)

    val cropWidthNorm: Float get() = (cropRight - cropLeft).coerceIn(0.01f, 1f)
    val cropHeightNorm: Float get() = (cropBottom - cropTop).coerceIn(0.01f, 1f)

    fun rotatedSize(w: Int, h: Int): Pair<Int, Int> =
        if ((quarterTurns % 4 + 4) % 4 % 2 == 0) Pair(w, h) else Pair(h, w)
}

object GeometryMath {

    /**
     * Mayor rectangulo axis-aligned de relacion aspecto (ancho/alto) que cabe dentro de un
     * rectangulo w x h girado angleRad radianes (busqueda binaria sobre la escala: robusta y
     * facil de verificar). Devuelve (ancho, alto).
     */
    fun largestInscribed(w: Float, h: Float, angleRad: Float, aspect: Float): Pair<Float, Float> {
        require(w > 0f && h > 0f && aspect > 0f)
        val a = angleRad.toDouble()
        val c = cos(-a).toFloat()
        val s = sin(-a).toFloat()
        val halfW = w / 2f
        val halfH = h / 2f

        fun fits(scale: Float): Boolean {
            val cw = scale * aspect / 2f
            val ch = scale / 2f
            val cornersX = floatArrayOf(-cw, cw, -cw, cw)
            val cornersY = floatArrayOf(-ch, -ch, ch, ch)
            for (i in 0 until 4) {
                val rx = cornersX[i] * c - cornersY[i] * s
                val ry = cornersX[i] * s + cornersY[i] * c
                if (abs(rx) > halfW + 1e-4f || abs(ry) > halfH + 1e-4f) return false
            }
            return true
        }

        var lo = 0f
        var hi = min(w, h) * 1.5f
        repeat(48) {
            val mid = (lo + hi) / 2f
            if (fits(mid)) lo = mid else hi = mid
        }
        return Pair(lo * aspect, lo)
    }

    /** Tamano de salida en pixeles para una fuente w x h con geometria g (y aspect opcional del recorte). */
    fun outputSize(srcW: Int, srcH: Int, g: GeometryModel, aspect: Float? = null): Pair<Int, Int> {
        val (rw, rh) = g.rotatedSize(srcW, srcH)
        val straightRad = Math.toRadians(g.straightenDeg.toDouble()).toFloat()
        var iw = rw.toFloat()
        var ih = rh.toFloat()
        if (straightRad != 0f) {
            val ratio = aspect ?: (rw.toFloat() / rh.toFloat())
            val (sw, sh) = largestInscribed(rw.toFloat(), rh.toFloat(), straightRad, ratio)
            iw = sw
            ih = sh
        }
        val cw = max(1, (g.cropWidthNorm * iw).roundToInt())
        val ch = max(1, (g.cropHeightNorm * ih).roundToInt())
        return Pair(cw, ch)
    }
}
