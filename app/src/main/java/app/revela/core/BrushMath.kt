package app.revela.core

import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Pincel multi-parametro: rasteriza los trazos a una densidad 0..1 con borde suave
 * (gaussiano) y aplica el parametro elegido solo donde se pinto.
 */
object BrushMath {

    const val EXPOSURE = "exposure"
    const val SATURATION = "saturation"
    const val TEMPERATURE = "temperature"
    const val DODGE_BURN = "dodgeBurn"

    val PARAMS = listOf(
        EXPOSURE to "Exposicion",
        SATURATION to "Saturacion",
        TEMPERATURE to "Temperatura",
        DODGE_BURN to "Claro/Oscuro"
    )

    /** Densidad acumulada de los trazos (1 dentro, 0 fuera, caida gaussiana en el borde). */
    fun buildDensity(strokes: List<HealStroke>, w: Int, h: Int): FloatArray {
        val density = FloatArray(w * h)
        val minSide = min(w, h).toFloat()
        for (stroke in strokes) {
            val rPix = (stroke.radiusNorm * minSide).coerceAtLeast(2f)
            val sigma = rPix * 0.6f
            val n = stroke.xs.size
            if (n == 0) continue
            for (i in 0 until n) {
                val cx = stroke.xs[i] * w
                val cy = stroke.ys[i] * h
                stamp(density, w, h, cx, cy, rPix, sigma)
                if (i > 0) {
                    val px = stroke.xs[i - 1] * w
                    val py = stroke.ys[i - 1] * h
                    val dist = sqrt((cx - px) * (cx - px) + (cy - py) * (cy - py))
                    val steps = (dist / (rPix * 0.4f)).toInt().coerceIn(1, 64)
                    for (s in 1 until steps) {
                        val t = s.toFloat() / steps
                        stamp(density, w, h, px + (cx - px) * t, py + (cy - py) * t, rPix, sigma)
                    }
                }
            }
        }
        return density
    }

    /** Aplica el parametro con amount -1..1 ponderado por densidad. */
    fun apply(pixels: IntArray, w: Int, h: Int, param: String, amount: Float, density: FloatArray) {
        if (amount == 0f) return
        for (i in pixels.indices) {
            val d = density[i]
            if (d <= 0f) continue
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            val a = when (param) {
                EXPOSURE -> {
                    // +2..-2 pasos EV escalados por amount*d
                    val f = exp2(amount * d * 1.5f)
                    val rr = (r * f).toInt().coerceIn(0, 255)
                    val gg = (g * f).toInt().coerceIn(0, 255)
                    val bb = (b * f).toInt().coerceIn(0, 255)
                    (rr shl 16) or (gg shl 8) or bb
                }
                DODGE_BURN -> {
                    val f = exp2(amount * d * 2.5f)
                    val rr = (r * f).toInt().coerceIn(0, 255)
                    val gg = (g * f).toInt().coerceIn(0, 255)
                    val bb = (b * f).toInt().coerceIn(0, 255)
                    (rr shl 16) or (gg shl 8) or bb
                }
                SATURATION -> {
                    val l = (r * 299 + g * 587 + b * 114) / 1000
                    val s = amount * d
                    val rr = (l + (r - l) * (1f + s)).toInt().coerceIn(0, 255)
                    val gg = (l + (g - l) * (1f + s)).toInt().coerceIn(0, 255)
                    val bb = (l + (b - l) * (1f + s)).toInt().coerceIn(0, 255)
                    (rr shl 16) or (gg shl 8) or bb
                }
                TEMPERATURE -> {
                    val t = amount * d * 60f
                    val rr = (r + t).toInt().coerceIn(0, 255)
                    val bb = (b - t).toInt().coerceIn(0, 255)
                    (rr shl 16) or (g shl 8) or bb
                }
                else -> p
            }
            pixels[i] = (p and 0xFF000000.toInt()) or a
        }
    }

    private fun exp2(x: Float): Float {
        // 2^x = e^(x*ln2); serie o via exp de kotlin
        return exp(x * 0.6931472f)
    }

    private fun stamp(density: FloatArray, w: Int, h: Int, cx: Float, cy: Float, r: Float, sigma: Float) {
        val x0 = (cx - r).toInt().coerceIn(0, w - 1)
        val x1 = (cx + r).toInt().coerceIn(0, w - 1)
        val y0 = (cy - r).toInt().coerceIn(0, h - 1)
        val y1 = (cy + r).toInt().coerceIn(0, h - 1)
        val inv2s2 = 1f / (2f * sigma * sigma)
        for (y in y0..y1) {
            for (x in x0..x1) {
                val dx = x - cx; val dy = y - cy
                val d2 = dx * dx + dy * dy
                if (d2 > r * r) continue
                val v = exp(-d2 * inv2s2)
                val idx = y * w + x
                if (v > density[idx]) density[idx] = v
            }
        }
    }
}
