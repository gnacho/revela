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

    /**
     * Desenfoque de caja separable (dos pasadas: horizontal y vertical) sobre los canales RGB.
     * El alpha NO se propaga al resultado (el llamador conserva el alpha original).
     * Devuelve SIEMPRE un array nuevo; no toca la entrada.
     */
    fun boxBlur(pixels: IntArray, w: Int, h: Int, radius: Int): IntArray {
        if (radius <= 0 || w <= 1 || h <= 1) return pixels.copyOf()
        val window = 2 * radius + 1
        val tmp = IntArray(pixels.size)
        val out = IntArray(pixels.size)

        // pasada horizontal
        for (y in 0 until h) {
            val row = y * w
            var sumR = 0L
            var sumG = 0L
            var sumB = 0L
            for (x in -radius..radius) {
                val xx = x.coerceIn(0, w - 1)
                val p = pixels[row + xx]
                sumR += (p ushr 16) and 0xFF
                sumG += (p ushr 8) and 0xFF
                sumB += p and 0xFF
            }
            for (x in 0 until w) {
                tmp[row + x] =
                    ((sumR / window).toInt() shl 16) or ((sumG / window).toInt() shl 8) or (sumB / window).toInt()
                val addX = (x + radius + 1).coerceAtMost(w - 1)
                val remX = (x - radius).coerceAtLeast(0)
                val pAdd = pixels[row + addX]
                val pRem = pixels[row + remX]
                sumR += ((pAdd ushr 16) and 0xFF) - ((pRem ushr 16) and 0xFF)
                sumG += ((pAdd ushr 8) and 0xFF) - ((pRem ushr 8) and 0xFF)
                sumB += (pAdd and 0xFF) - (pRem and 0xFF)
            }
        }

        // pasada vertical
        for (x in 0 until w) {
            var sumR = 0L
            var sumG = 0L
            var sumB = 0L
            for (y in -radius..radius) {
                val yy = y.coerceIn(0, h - 1)
                val p = tmp[yy * w + x]
                sumR += (p ushr 16) and 0xFF
                sumG += (p ushr 8) and 0xFF
                sumB += p and 0xFF
            }
            for (y in 0 until h) {
                val i = y * w + x
                out[i] = (pixels[i] and 0xFF000000.toInt()) or
                    ((sumR / window).toInt() shl 16) or ((sumG / window).toInt() shl 8) or (sumB / window).toInt()
                val addY = (y + radius + 1).coerceAtMost(h - 1)
                val remY = (y - radius).coerceAtLeast(0)
                val pAdd = tmp[addY * w + x]
                val pRem = tmp[remY * w + x]
                sumR += ((pAdd ushr 16) and 0xFF) - ((pRem ushr 16) and 0xFF)
                sumG += ((pAdd ushr 8) and 0xFF) - ((pRem ushr 8) and 0xFF)
                sumB += (pAdd and 0xFF) - (pRem and 0xFF)
            }
        }
        return out
    }

    /** Detalles: estructura (unsharp de radio grande) + enfoque (unsharp de radio pequeno). */
    fun applyDetails(pixels: IntArray, w: Int, h: Int, structure: Float, sharpening: Float) {
        val minSide = min(w, h)
        if (structure != 0f) {
            val radius = max(2, minSide / 20)
            val blurred = boxBlur(pixels, w, h, radius)
            unsharp(pixels, blurred, w, h, structure)
        }
        if (sharpening != 0f) {
            val radius = 1
            val blurred = boxBlur(pixels, w, h, radius)
            unsharp(pixels, blurred, w, h, sharpening)
        }
    }

    /** v' = v + amount * (v - blur(v)); conserva el alpha. */
    private fun unsharp(pixels: IntArray, blurred: IntArray, w: Int, h: Int, amount: Float) {
        for (i in pixels.indices) {
            val p = pixels[i]
            val b = blurred[i]
            val r = (p ushr 16) and 0xFF
            val g = (p ushr 8) and 0xFF
            val bl = p and 0xFF
            val br = (b ushr 16) and 0xFF
            val bg = (b ushr 8) and 0xFF
            val bb = b and 0xFF
            val nr = (r + amount * (r - br)).toInt().coerceIn(0, 255)
            val ng = (g + amount * (g - bg)).toInt().coerceIn(0, 255)
            val nb = (bl + amount * (bl - bb)).toInt().coerceIn(0, 255)
            pixels[i] = (p and 0xFF000000.toInt()) or (nr shl 16) or (ng shl 8) or nb
        }
    }

    /**
     * HDR-scape: refuerzo del contraste local (diferencia con un blur grande) + ligera
     * subida de sombras. strength 0..1.
     */
    fun applyHdr(pixels: IntArray, w: Int, h: Int, strength: Float) {
        if (strength <= 0f) return
        val radius = max(2, min(w, h) / 15)
        val blurred = boxBlur(pixels, w, h, radius)
        val k = 0.6f
        for (i in pixels.indices) {
            val p = pixels[i]
            val b = blurred[i]
            var r = (p ushr 16) and 0xFF
            var g = (p ushr 8) and 0xFF
            var bl = p and 0xFF
            val br = (b ushr 16) and 0xFF
            val bg = (b ushr 8) and 0xFF
            val bb = b and 0xFF
            r = (r + strength * k * (r - br)).toInt().coerceIn(0, 255)
            g = (g + strength * k * (g - bg)).toInt().coerceIn(0, 255)
            bl = (bl + strength * k * (bl - bb)).toInt().coerceIn(0, 255)
            // subida de sombras leve: mas en las zonas oscuras
            val luma = (r * 54 + g * 183 + bl * 19) / 256
            val lift = strength * 0.10f * (1f - luma / 255f) * 255f
            r = (r + lift).toInt().coerceIn(0, 255)
            g = (g + lift).toInt().coerceIn(0, 255)
            bl = (bl + lift).toInt().coerceIn(0, 255)
            pixels[i] = (p and 0xFF000000.toInt()) or (r shl 16) or (g shl 8) or bl
        }
    }

    /** Grano determinista: ruido de hash entero sobre la luminancia. strength 0..1. */
    fun applyGrain(pixels: IntArray, w: Int, h: Int, strength: Float) {
        if (strength <= 0f) return
        val amplitude = 60f
        for (y in 0 until h) {
            for (x in 0 until w) {
                val i = y * w + x
                val p = pixels[i]
                val r = (p ushr 16) and 0xFF
                val g = (p ushr 8) and 0xFF
                val bl = p and 0xFF
                val luma = (r * 54 + g * 183 + bl * 19) / 256
                val n = hashNoise(x, y)
                val delta = (n * strength * amplitude).toInt()
                val nl = (luma + delta).coerceIn(0, 255)
                val diff = nl - luma
                val nr = (r + diff).coerceIn(0, 255)
                val ng = (g + diff).coerceIn(0, 255)
                val nb = (bl + diff).coerceIn(0, 255)
                pixels[i] = (p and 0xFF000000.toInt()) or (nr shl 16) or (ng shl 8) or nb
            }
        }
    }

    /** Hash entero determinista (x, y) -> ruido -1..1 con semilla fija. */
    private fun hashNoise(x: Int, y: Int): Float {
        var h = x * 374761393 + y * 668265263 + GRAIN_SEED
        h = (h xor (h ushr 13)) * 1274126177
        h = h xor (h ushr 16)
        val u = (h and 0x7FFFFFFF).toDouble() / 2147483647.0
        return (u * 2.0 - 1.0).toFloat()
    }

    private const val GRAIN_SEED = 0x1a2b3c4d

    /**
     * Brillo glamuroso (glamour glow): screen-blend con una copia difuminada,
     * mezclado por strength. v' = 255 - (255-v)*(255-blur(v))/255.
     */
    fun applyGlow(pixels: IntArray, w: Int, h: Int, strength: Float) {
        if (strength <= 0f) return
        val radius = max(2, min(w, h) / 25)
        val blurred = boxBlur(pixels, w, h, radius)
        for (i in pixels.indices) {
            val p = pixels[i]
            val b = blurred[i]
            val r = (p ushr 16) and 0xFF
            val g = (p ushr 8) and 0xFF
            val bl = p and 0xFF
            val br = (b ushr 16) and 0xFF
            val bg = (b ushr 8) and 0xFF
            val bb = b and 0xFF
            val sr = 255 - (255 - r) * (255 - br) / 255
            val sg = 255 - (255 - g) * (255 - bg) / 255
            val sb = 255 - (255 - bl) * (255 - bb) / 255
            val nr = (r + (sr - r) * strength).toInt().coerceIn(0, 255)
            val ng = (g + (sg - g) * strength).toInt().coerceIn(0, 255)
            val nb = (bl + (sb - bl) * strength).toInt().coerceIn(0, 255)
            pixels[i] = (p and 0xFF000000.toInt()) or (nr shl 16) or (ng shl 8) or nb
        }
    }

    /** Desenfoque radial (lens blur): radio de blur crece con la distancia al centro del marco. */
    fun applyLensBlur(pixels: IntArray, w: Int, h: Int, strength: Float) {
        if (strength <= 0f) return
        val cx = (w - 1) / 2f
        val cy = (h - 1) / 2f
        val maxDist = sqrt(cx * cx + cy * cy)
        if (maxDist <= 0f) return
        val maxR = max(2, min(w, h) / 12)
        val levels = 4
        val blurs = Array(levels + 1) { k ->
            if (k == 0) pixels.copyOf() else boxBlur(pixels, w, h, max(1, maxR * k / levels))
        }
        val step = maxR.toFloat() / levels
        for (y in 0 until h) {
            for (x in 0 until w) {
                val dx = x - cx
                val dy = y - cy
                val d = sqrt(dx * dx + dy * dy) / maxDist
                val r = strength * maxR * d
                val kf = (r / step).coerceIn(0f, levels.toFloat())
                val k0 = kf.toInt()
                val k1 = min(k0 + 1, levels)
                val frac = kf - k0
                val i = y * w + x
                val p = pixels[i]
                val b0 = blurs[k0][i]
                val b1 = blurs[k1][i]
                val r0 = (b0 ushr 16) and 0xFF
                val g0 = (b0 ushr 8) and 0xFF
                val b0c = b0 and 0xFF
                val r1 = (b1 ushr 16) and 0xFF
                val g1 = (b1 ushr 8) and 0xFF
                val b1c = b1 and 0xFF
                val nr = (r0 + (r1 - r0) * frac).toInt().coerceIn(0, 255)
                val ng = (g0 + (g1 - g0) * frac).toInt().coerceIn(0, 255)
                val nb = (b0c + (b1c - b0c) * frac).toInt().coerceIn(0, 255)
                pixels[i] = (p and 0xFF000000.toInt()) or (nr shl 16) or (ng shl 8) or nb
            }
        }
    }

    /**
     * Doble exposicion: mezcla overlay (mismo tamano que pixels) sobre pixels in situ.
     * blend en {"normal","screen","multiply","lighten","darken"}; opacity 0..1.
     */
    fun applyDoubleExpo(pixels: IntArray, overlay: IntArray, w: Int, h: Int, blend: String, opacity: Float) {
        if (opacity <= 0f) return
        for (i in pixels.indices) {
            val p = pixels[i]
            val o = overlay[i]
            val r = BlendModes.mix((p ushr 16) and 0xFF, (o ushr 16) and 0xFF, blend, opacity)
            val g = BlendModes.mix((p ushr 8) and 0xFF, (o ushr 8) and 0xFF, blend, opacity)
            val b = BlendModes.mix(p and 0xFF, o and 0xFF, blend, opacity)
            pixels[i] = (p and 0xFF000000.toInt()) or (r shl 16) or (g shl 8) or b
        }
    }
}

/** Modos de mezcla por canal (0..255) para doble exposicion, testables de forma exacta. */
object BlendModes {
    fun channel(a: Int, b: Int, mode: String): Int = when (mode) {
        "screen" -> 255 - (255 - a) * (255 - b) / 255
        "multiply" -> a * b / 255
        "lighten" -> maxOf(a, b)
        "darken" -> minOf(a, b)
        else -> b // normal
    }

    fun mix(a: Int, b: Int, mode: String, opacity: Float): Int {
        val blended = channel(a, b, mode)
        return (a + (blended - a) * opacity).toInt().coerceIn(0, 255)
    }
}

/** Pesos de filtro B/N (canal rojo/amarillo/verde/azul) sobre luminancia. */
object BnwFilters {
    val NEUTRAL = floatArrayOf(0.299f, 0.587f, 0.114f)
    val RED = floatArrayOf(0.60f, 0.35f, 0.05f)
    val YELLOW = floatArrayOf(0.45f, 0.45f, 0.10f)
    val GREEN = floatArrayOf(0.30f, 0.50f, 0.20f)
    val BLUE = floatArrayOf(0.15f, 0.25f, 0.60f)

    fun byName(name: String): FloatArray = when (name) {
        "red" -> RED
        "yellow" -> YELLOW
        "green" -> GREEN
        "blue" -> BLUE
        else -> NEUTRAL
    }
}

/** Convierte el marco a blanco y negro con los pesos del filtro elegido. */
fun applyBnw(pixels: IntArray, filter: String) {
    val wts = BnwFilters.byName(filter)
    val wr = wts[0]
    val wg = wts[1]
    val wb = wts[2]
    for (i in pixels.indices) {
        val p = pixels[i]
        val r = (p shr 16) and 0xFF
        val g = (p shr 8) and 0xFF
        val b = p and 0xFF
        val gray = (r * wr + g * wg + b * wb).toInt().coerceIn(0, 255)
        pixels[i] = (p and 0xFF000000.toInt()) or (gray shl 16) or (gray shl 8) or gray
    }
}
