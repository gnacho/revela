package app.revela.core

import kotlin.math.abs
import kotlin.math.pow

/**
 * Matematica de color compartida por preview y export (UNA sola implementacion).
 * Orden de operaciones: exposicion -> balance de blancos -> contraste -> curva (LUT por canal),
 * y despues saturacion (pase cruzado por luminancia Rec.709).
 */
object ColorMath {
    const val LUT_SIZE = 256

    /** Rango de ajustes efectivos usado por UI y render. */
    data class Adjustments(
        val exposureStops: Float = 0f,   // -2..2
        val contrast: Float = 0f,        // -1..1
        val saturation: Float = 0f,      // -1..1
        val temperature: Float = 0f,     // -1..1 (frio->calido)
        val tint: Float = 0f,            // -1..1 (verde->magenta)
        val tonalShadows: Float = 0f,    // -1..1 (contraste tonal por zonas)
        val tonalMidtones: Float = 0f,   // -1..1
        val tonalHighlights: Float = 0f, // -1..1
        val curve: List<CurvePoint>? = null
    )

    /** Construye 3 LUTs (R, G, B) de 256 entradas para todos los ajustes por canal. */
    fun buildLuts(a: Adjustments): List<FloatArray> {
        val exposureMul = 2f.pow(a.exposureStops)
        // WB aproximada: calido sube R y baja B; tinte mueve G (verde negativo, magenta positivo)
        val mulR = exposureMul * (1f + a.temperature * 0.25f)
        val mulG = exposureMul * (1f - a.tint * 0.15f)
        val mulB = exposureMul * (1f - a.temperature * 0.25f)
        val contrastMul = 1f + a.contrast
        val curveLut = a.curve?.let { CurveMath.lut(it) }
        val tonal = tonalDelta(a.tonalShadows, a.tonalMidtones, a.tonalHighlights)
        return listOf(
            lutFor(mulR, contrastMul, curveLut, tonal),
            lutFor(mulG, contrastMul, curveLut, tonal),
            lutFor(mulB, contrastMul, curveLut, tonal)
        )
    }

    private fun lutFor(mul: Float, contrastMul: Float, curveLut: FloatArray?, tonal: TonalDelta?): FloatArray {
        val out = FloatArray(LUT_SIZE)
        for (i in 0 until LUT_SIZE) {
            var v = (i / 255f) * mul
            v = (v - 0.5f) * contrastMul + 0.5f
            if (curveLut != null) {
                var idx = (v * 255f).toInt()
                if (idx < 0) idx = 0
                if (idx > 255) idx = 255
                v = curveLut[idx]
            }
            if (tonal != null) {
                v = (v + tonal.delta[i] / 255f).coerceIn(0f, 1f)
            }
            out[i] = v.coerceIn(0f, 1f)
        }
        return out
    }

    /** Deltas de nivel (0..255) por zona tonal; 0 = sin cambio. */
    data class TonalDelta(val delta: FloatArray)

    fun tonalDelta(shadows: Float, midtones: Float, highlights: Float): TonalDelta? {
        if (shadows == 0f && midtones == 0f && highlights == 0f) return null
        val d = FloatArray(LUT_SIZE)
        for (i in 0 until LUT_SIZE) {
            val v = i / 255f
            // pesos de zona con caida coseno suave; centros: sombras 0.0, medios 0.5, luces 1.0
            val wS = zoneWeight(v, 0.0f, 0.5f)
            val wM = zoneWeight(v, 0.5f, 0.5f)
            val wH = zoneWeight(v, 1.0f, 0.5f)
            d[i] = (shadows * wS + midtones * wM + highlights * wH) * 60f
        }
        return TonalDelta(d)
    }

    private fun zoneWeight(v: Float, center: Float, halfSpan: Float): Float {
        val dist = abs(v - center) / halfSpan
        if (dist >= 1f) return 0f
        return 0.5f * (1f + kotlin.math.cos(Math.PI * dist).toFloat())
    }

    fun saturationFactor(amount: Float): Float = 1f + amount

    /**
     * Aplica LUTs + saturacion EN SITU sobre pixeles ARGB (alpha preservada).
     * Devuelve el mismo IntArray por rendimiento; el llamador decide si clona.
     */
    fun applyInPlace(pixels: IntArray, luts: List<FloatArray>, saturation: Float) {
        val lutR = luts[0]
        val lutG = luts[1]
        val lutB = luts[2]
        val sat = saturationFactor(saturation)
        val useSat = abs(sat - 1f) > 1e-4f
        for (i in pixels.indices) {
            val p = pixels[i]
            var r = lutR[(p ushr 16) and 0xFF]
            var g = lutG[(p ushr 8) and 0xFF]
            var b = lutB[p and 0xFF]
            if (useSat) {
                val luma = 0.2126f * r + 0.7152f * g + 0.0722f * b
                r = luma + (r - luma) * sat
                g = luma + (g - luma) * sat
                b = luma + (b - luma) * sat
            }
            var ri = (r * 255f + 0.5f).toInt()
            var gi = (g * 255f + 0.5f).toInt()
            var bi = (b * 255f + 0.5f).toInt()
            if (ri > 255) ri = 255 else if (ri < 0) ri = 0
            if (gi > 255) gi = 255 else if (gi < 0) gi = 0
            if (bi > 255) bi = 255 else if (bi < 0) bi = 0
            pixels[i] = (p and 0xFF000000.toInt()) or (ri shl 16) or (gi shl 8) or bi
        }
    }
}
