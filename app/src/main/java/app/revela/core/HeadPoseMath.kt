package app.revela.core

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Pose de cabeza sin ML: el usuario coloca ojo izquierdo, ojo derecho y boca;
 * la herramienta deforma la zona facial como si la cabeza girara (yaw/pitch).
 * Desplazamiento por campo de vectores con interpolacion IDW sobre una malla.
 */
object HeadPoseMath {

    private data class Landmark(val x: Float, val y: Float, val depth: Float, val dx: Float, val dy: Float)

    /** Devuelve el frame deformado (mismo tamaño). Sin los 3 puntos: copia identica. */
    fun warp(src: IntArray, w: Int, h: Int, edit: Edit.HeadPose): IntArray {
        val le = edit.leftEye ?: return src.copyOf()
        val re = edit.rightEye ?: return src.copyOf()
        val mo = edit.mouth ?: return src.copyOf()
        if (edit.yawDeg == 0f && edit.pitchDeg == 0f) return src.copyOf()

        val eyeDist = max(1e-4f, sqrt((le.x - re.x) * (le.x - re.x) + (le.y - re.y) * (le.y - re.y)))
        // Landmarks con profundidad simulada: con el giro, los puntos "mas delante" se mueven mas.
        val kx = edit.yawDeg / 45f * eyeDist * 0.6f
        val ky = edit.pitchDeg / 45f * eyeDist * 0.6f
        val noseX = (le.x + re.x) / 2f
        val noseY = (le.y + re.y) / 2f + eyeDist * 0.55f
        val landmarks = listOf(
            Landmark(le.x, le.y, depth = 0.5f, dx = kx * 0.5f, dy = ky * 0.5f),
            Landmark(re.x, re.y, depth = 0.5f, dx = kx * 0.5f, dy = ky * 0.5f),
            Landmark(noseX, noseY, depth = 1.0f, dx = kx, dy = ky),
            Landmark(mo.x, mo.y, depth = 0.75f, dx = kx * 0.75f, dy = ky * 0.75f)
        )
        val radius = eyeDist * 2.2f

        // Malla de desplazamiento (forward warp: out(p) = src(p - d(p)))
        val g = 24
        val du = FloatArray((g + 1) * (g + 1))
        val dv = FloatArray((g + 1) * (g + 1))
        for (j in 0..g) {
            val y = j.toFloat() / g
            for (i in 0..g) {
                val x = i.toFloat() / g
                var sx = 0f; var sy = 0f; var sw = 0f
                for (lm in landmarks) {
                    val ddx = x - lm.x; val ddy = y - lm.y
                    val dist = sqrt(ddx * ddx + ddy * ddy)
                    if (dist >= radius) continue
                    val wgt = 1f / ((dist * dist + 1e-6f) * (dist + 0.02f))
                    sx += lm.dx * wgt; sy += lm.dy * wgt; sw += wgt
                }
                if (sw > 0f) {
                    val idx = j * (g + 1) + i
                    // fade suave hacia el borde del radio para evitar pliegues
                    du[idx] = sx / sw
                    dv[idx] = sy / sw
                }
            }
        }

        val out = IntArray(w * h)
        var p = 0
        for (y in 0 until h) {
            val v = (y + 0.5f) / h
            for (x in 0 until w) {
                val u = (x + 0.5f) / w
                val d = meshSample(du, dv, g, u, v)
                out[p++] = PerspectiveMath.sampleBilinear(src, w, h, u - d[0], v - d[1])
            }
        }
        return out
    }

    /** Interpolacion bilineal de la malla de desplazamiento en (u,v) normalizadas. */
    private fun meshSample(du: FloatArray, dv: FloatArray, g: Int, u: Float, v: Float, out: FloatArray = FloatArray(2)): FloatArray {
        val gx = (u * g).coerceIn(0f, g - 1e-4f)
        val gy = (v * g).coerceIn(0f, g - 1e-4f)
        val i0 = gx.toInt(); val j0 = gy.toInt()
        val fx = gx - i0; val fy = gy - j0
        val i1 = min(i0 + 1, g); val j1 = min(j0 + 1, g)
        val s = g + 1
        out[0] = bilin(du, s, i0, j0, i1, j1, fx, fy)
        out[1] = bilin(dv, s, i0, j0, i1, j1, fx, fy)
        return out
    }

    private fun bilin(m: FloatArray, s: Int, i0: Int, j0: Int, i1: Int, j1: Int, fx: Float, fy: Float): Float {
        val a = m[j0 * s + i0]; val b = m[j0 * s + i1]
        val c = m[j1 * s + i0]; val d = m[j1 * s + i1]
        return (a + (b - a) * fx) + ((c + (d - c) * fx) - (a + (b - a) * fx)) * fy
    }
}
