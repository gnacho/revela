package app.revela.core

import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlinx.serialization.Serializable

/** Trazo de sanado: camino de puntos normalizados (espacio de marco) con radio de brocha. */
@Serializable
data class HealStroke(
    val radiusNorm: Float,
    val xs: List<Float>,
    val ys: List<Float>
)

/** Inpainting por difusion en Kotlin puro (sin OpenCV): rellena la zona marcada. */
object HealMath {

    /**
     * Rasteriza los trazos a una mascara booleana del marco.
     * Los trazos se dibujan como discos densamente muestreados a lo largo del camino.
     */
    fun buildMask(strokes: List<HealStroke>, w: Int, h: Int): BooleanArray {
        val mask = BooleanArray(w * h)
        val minSide = min(w, h).toFloat()
        for (stroke in strokes) {
            val rPix = (stroke.radiusNorm * minSide).coerceAtLeast(2f)
            val n = stroke.xs.size
            if (n == 0) continue
            for (i in 0 until n) {
                val cx = stroke.xs[i] * w
                val cy = stroke.ys[i] * h
                stampDisc(mask, w, h, cx, cy, rPix)
                if (i > 0) {
                    val px = stroke.xs[i - 1] * w
                    val py = stroke.ys[i - 1] * h
                    val dist = sqrt((cx - px) * (cx - px) + (cy - py) * (cy - py))
                    val steps = (dist / (rPix * 0.4f)).toInt().coerceIn(1, 64)
                    for (s in 1 until steps) {
                        val t = s.toFloat() / steps
                        stampDisc(mask, w, h, px + (cx - px) * t, py + (cy - py) * t, rPix)
                    }
                }
            }
        }
        return mask
    }

    private fun stampDisc(mask: BooleanArray, w: Int, h: Int, cx: Float, cy: Float, r: Float) {
        val x0 = maxOf(0, (cx - r).toInt())
        val x1 = minOf(w - 1, (cx + r).toInt())
        val y0 = maxOf(0, (cy - r).toInt())
        val y1 = minOf(h - 1, (cy + r).toInt())
        val r2 = r * r
        for (y in y0..y1) {
            for (x in x0..x1) {
                val dx = x - cx
                val dy = y - cy
                if (dx * dx + dy * dy <= r2) mask[y * w + x] = true
            }
        }
    }

    /**
     * Rellena los pixeles marcados en la mascara:
     * 1) relleno por capas (BFS desde el borde de la zona) con la media de vecinos conocidos,
     * 2) suavizado por difusion (Jacobi) dentro de la zona.
     * Los pixeles conocidos no se modifican.
     */
    fun inpaint(pixels: IntArray, mask: BooleanArray, w: Int, h: Int, smoothingPasses: Int = 24) {
        val n = w * h
        val fr = FloatArray(n)
        val fg = FloatArray(n)
        val fb = FloatArray(n)
        val known = BooleanArray(n)

        for (i in 0 until n) {
            val p = pixels[i]
            fr[i] = ((p ushr 16) and 0xFF).toFloat()
            fg[i] = ((p ushr 8) and 0xFF).toFloat()
            fb[i] = (p and 0xFF).toFloat()
            known[i] = !mask[i]
        }

        // 1) relleno por capas
        val queue = ArrayDeque<Int>()
        val enqueued = BooleanArray(n)
        for (i in 0 until n) {
            if (mask[i] && hasKnownNeighbor(i, w, h, known)) {
                queue.addLast(i)
                enqueued[i] = true
            }
        }
        var remaining = mask.count { it }
        while (queue.isNotEmpty() && remaining > 0) {
            val i = queue.removeFirst()
            enqueued[i] = false
            if (known[i]) continue
            var sr = 0f
            var sg = 0f
            var sb = 0f
            var cnt = 0
            for (k in 0 until 4) {
                val j = neighbor(i, k, w, h) ?: continue
                if (known[j]) {
                    sr += fr[j]; sg += fg[j]; sb += fb[j]; cnt++
                }
            }
            if (cnt == 0) {
                queue.addLast(i) // reintenta cuando tenga vecinos rellenos
                enqueued[i] = true
                continue
            }
            fr[i] = sr / cnt
            fg[i] = sg / cnt
            fb[i] = sb / cnt
            known[i] = true
            remaining--
            for (k in 0 until 4) {
                val j = neighbor(i, k, w, h) ?: continue
                if (mask[j] && !known[j] && !enqueued[j]) {
                    queue.addLast(j)
                    enqueued[j] = true
                }
            }
        }

        // 2) difusion (Jacobi) suavizando solo la zona
        val cr = FloatArray(n)
        val cg = FloatArray(n)
        val cb = FloatArray(n)
        var arrays = Triple(fr, fg, fb)
        for (pass in 0 until smoothingPasses) {
            for (y in 0 until h) {
                for (x in 0 until w) {
                    val i = y * w + x
                    if (!mask[i]) {
                        cr[i] = arrays.first[i]
                        cg[i] = arrays.second[i]
                        cb[i] = arrays.third[i]
                        continue
                    }
                    var sr = 0f
                    var sg = 0f
                    var sb = 0f
                    var cnt = 0
                    for (k in 0 until 4) {
                        val j = neighbor(i, k, w, h) ?: continue
                        sr += arrays.first[j]
                        sg += arrays.second[j]
                        sb += arrays.third[j]
                        cnt++
                    }
                    if (cnt > 0) {
                        cr[i] = sr / cnt
                        cg[i] = sg / cnt
                        cb[i] = sb / cnt
                    } else {
                        cr[i] = arrays.first[i]
                        cg[i] = arrays.second[i]
                        cb[i] = arrays.third[i]
                    }
                }
            }
            arrays = Triple(cr, cg, cb)
        }
        val (or2, og2, ob2) = arrays

        // escribe de vuelta solo lo enmascarado
        for (i in 0 until n) {
            if (!mask[i]) continue
            val p = pixels[i]
            val ri = or2[i].roundToInt().coerceIn(0, 255)
            val gi = og2[i].roundToInt().coerceIn(0, 255)
            val bi = ob2[i].roundToInt().coerceIn(0, 255)
            pixels[i] = (p and 0xFF000000.toInt()) or (ri shl 16) or (gi shl 8) or bi
        }
    }

    private fun hasKnownNeighbor(i: Int, w: Int, h: Int, known: BooleanArray): Boolean {
        for (k in 0 until 4) {
            val j = neighbor(i, k, w, h) ?: continue
            if (known[j]) return true
        }
        return false
    }

    private fun neighbor(i: Int, k: Int, w: Int, h: Int): Int? = when (k) {
        0 -> if (i >= w) i - w else null
        1 -> if (i < w * (h - 1)) i + w else null
        2 -> if (i % w != 0) i - 1 else null
        else -> if ((i + 1) % w != 0) i + 1 else null
    }
}
