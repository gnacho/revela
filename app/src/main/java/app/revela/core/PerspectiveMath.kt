package app.revela.core

import kotlin.math.abs
import kotlinx.serialization.Serializable

/**
 * Esquinas destino normalizadas (0..1) sobre el marco de salida.
 * IDENTITY es el rectangulo completo (sin deformacion).
 */
@Serializable
data class PerspectiveCorners(
    val tlX: Float = 0f, val tlY: Float = 0f,
    val trX: Float = 1f, val trY: Float = 0f,
    val brX: Float = 1f, val brY: Float = 1f,
    val blX: Float = 0f, val blY: Float = 1f
) {
    companion object {
        val IDENTITY = PerspectiveCorners()
    }

    fun isIdentity(eps: Float = 1e-4f): Boolean =
        abs(tlX) < eps && abs(tlY) < eps && abs(1f - trX) < eps && abs(trY) < eps &&
            abs(1f - brX) < eps && abs(1f - brY) < eps &&
            abs(blX) < eps && abs(1f - blY) < eps
}

/** Warp de perspectiva por malla bilineal invertida con Newton (mapeo inverso por pixel). */
object PerspectiveMath {

    /** Evalua la superficie bilineal de las esquinas en (u,v); resultado en out[0..1]. */
    fun bilinear(c: PerspectiveCorners, u: Float, v: Float, out: FloatArray) {
        val x0 = c.tlX + (c.trX - c.tlX) * u
        val x1 = c.blX + (c.brX - c.blX) * u
        val y0 = c.tlY + (c.trY - c.tlY) * u
        val y1 = c.blY + (c.brY - c.blY) * u
        out[0] = x0 + (x1 - x0) * v
        out[1] = y0 + (y1 - y0) * v
    }

    /** Esquinas de un plano rotado (yaw/pitch/roll en radianes) proyectado en perspectiva. */
    fun rotatedPlane(yawDeg: Float, pitchDeg: Float, rollDeg: Float): PerspectiveCorners {
        val yaw = Math.toRadians(yawDeg.toDouble()).toFloat()
        val pitch = Math.toRadians(pitchDeg.toDouble()).toFloat()
        val roll = Math.toRadians(rollDeg.toDouble()).toFloat()
        val cy = kotlin.math.cos(yaw); val sy = kotlin.math.sin(yaw)
        val cp = kotlin.math.cos(pitch); val sp = kotlin.math.sin(pitch)
        val cr = kotlin.math.cos(roll); val sr = kotlin.math.sin(roll)
        val f = 1.35f // distancia focal (mayor = menos deformacion)
        fun proj(x: Float, y: Float): FloatArray {
            // punto en el plano z=0, centrado
            var px = x - 0.5f; var py = y - 0.5f; var pz = 0f
            // rotacion yaw (eje Y) y pitch (eje X)
            var tx = px * cy + pz * sy; var tz = -px * sy + pz * cy
            var ty = py * cp - tz * sp; tz = py * sp + tz * cp
            // roll (eje Z)
            px = tx * cr - ty * sr; py = tx * sr + ty * cr; pz = tz
            val s = f / (f + pz)
            return floatArrayOf(px * s + 0.5f, py * s + 0.5f)
        }
        val tl = proj(0f, 0f); val tr = proj(1f, 0f)
        val br = proj(1f, 1f); val bl = proj(0f, 1f)
        return PerspectiveCorners(tl[0], tl[1], tr[0], tr[1], br[0], br[1], bl[0], bl[1])
    }

    /**
     * Deforma el frame w x h segun las esquinas (mapeo inverso: cada pixel de salida
     * busca su origen resolviendo la bilineal por Newton). Fuera de rango: borde replicado.
     */
    fun warp(src: IntArray, w: Int, h: Int, corners: PerspectiveCorners): IntArray {
        if (corners.isIdentity()) return src.copyOf()
        val out = IntArray(w * h)
        val p = FloatArray(2)
        var i = 0
        for (y in 0 until h) {
            val vn = (y + 0.5f) / h
            for (x in 0 until w) {
                val un = (x + 0.5f) / w
                var u = un
                var v = vn
                // Newton 2D (max 4 iteraciones; las deformaciones de UI son suaves)
                for (it in 0 until 4) {
                    bilinear(corners, u, v, p)
                    val f0 = p[0] - un
                    val f1 = p[1] - vn
                    if (f0 * f0 + f1 * f1 < 1e-9f) break
                    val a = (1f - v) * (corners.trX - corners.tlX) + v * (corners.brX - corners.blX)
                    val b = (1f - u) * (corners.blX - corners.tlX) + u * (corners.brX - corners.trX)
                    val c = (1f - v) * (corners.trY - corners.tlY) + v * (corners.brY - corners.blY)
                    val d = (1f - u) * (corners.blY - corners.tlY) + u * (corners.brY - corners.trY)
                    val det = a * d - b * c
                    if (abs(det) < 1e-9f) break
                    u -= (f0 * d - b * f1) / det
                    v -= (a * f1 - f0 * c) / det
                }
                out[i++] = sampleBilinear(src, w, h, u, v)
            }
        }
        return out
    }

    /** Muestreo bilineal con clamp al borde. */
    fun sampleBilinear(src: IntArray, w: Int, h: Int, u: Float, v: Float): Int {
        val uc = u.coerceIn(0f, 1f) * (w - 1)
        val vc = v.coerceIn(0f, 1f) * (h - 1)
        val x0 = uc.toInt().coerceIn(0, w - 1)
        val y0 = vc.toInt().coerceIn(0, h - 1)
        val x1 = (x0 + 1).coerceAtMost(w - 1)
        val y1 = (y0 + 1).coerceAtMost(h - 1)
        val fx = uc - x0
        val fy = vc - y0
        val p00 = src[y0 * w + x0]; val p10 = src[y0 * w + x1]
        val p01 = src[y1 * w + x0]; val p11 = src[y1 * w + x1]
        return mixPixel(mixPixel(p00, p10, fx), mixPixel(p01, p11, fx), fy)
    }

    private fun mixPixel(a: Int, b: Int, t: Float): Int {
        if (t <= 0f) return a
        if (t >= 1f) return b
        val ia = a ushr 24; val ib = b ushr 24
        val ra = (a shr 16) and 0xFF; val rb = (b shr 16) and 0xFF
        val ga = (a shr 8) and 0xFF; val gb = (b shr 8) and 0xFF
        val ba = a and 0xFF; val bb = b and 0xFF
        val o = (ia + (ib - ia) * t).toInt()
        val r = (ra + (rb - ra) * t).toInt()
        val g = (ga + (gb - ga) * t).toInt()
        val bl = (ba + (bb - ba) * t).toInt()
        return (o shl 24) or (r shl 16) or (g shl 8) or bl
    }
}
