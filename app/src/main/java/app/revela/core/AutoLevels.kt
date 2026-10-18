package app.revela.core

import app.revela.render.BaseImage
import kotlin.math.abs
import kotlin.math.log2

/** Auto-niveles de un toque (boton AUTO de Ajustar imagen): analiza la imagen BASE sin ediciones. */
object AutoLevels {

    data class Result(
        val exposureStops: Float,
        val contrast: Float,
        val saturation: Float
    )

    fun compute(image: BaseImage): Result {
        val stepX = maxOf(1, image.width / 256)
        val stepY = maxOf(1, image.height / 256)
        var n = 0L
        val lum = IntArray(256)
        var satSum = 0.0
        var y = 0
        while (y < image.height) {
            var x = 0
            val row = y * image.width
            while (x < image.width) {
                val p = image.pixels[row + x]
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                val mx = maxOf(r, g, b)
                val mn = minOf(r, g, b)
                satSum += if (mx == 0) 0.0 else (mx - mn).toDouble() / mx
                lum[(r * 299 + g * 587 + b * 114) / 1000]++
                n++
                x += stepX
            }
            y += stepY
        }
        if (n == 0L) return Result(0f, 0f, 0f)

        val loCount = (n * 0.005).toInt().coerceAtLeast(1)
        val hiCount = (n * 0.995).toInt().coerceAtLeast(1)
        var acc = 0L
        var lo = 0
        var hi = 255
        for (v in 0..255) {
            acc += lum[v]
            if (acc >= loCount) { lo = v; break }
        }
        acc = 0L
        for (v in 0..255) {
            acc += lum[v]
            if (acc >= hiCount) { hi = v; break }
        }
        val meanSat = (satSum / n).toFloat()

        // Exposicion: centra el tramo util en el gris medio (leve, max +-0.6 EV)
        val center = (lo + hi) / 2f
        val exposure = if (abs(center - 127.5f) < 6f) 0f
        else log2(127.5f / center).coerceIn(-0.6f, 0.6f)

        // Contraste: estira el tramo util hacia 0..255; solo anade, nunca reduce
        val span = (hi - lo).coerceAtLeast(1)
        val contrast = ((250f / span) - 1f).coerceIn(0f, 0.5f)

        // Saturacion: empujoncito si hay color que conservar
        val saturation = if (meanSat in 0.04f..0.55f) 0.10f else 0f

        return Result(exposure, contrast, saturation)
    }
}
