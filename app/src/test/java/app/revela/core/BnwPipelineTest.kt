package app.revela.core

import app.revela.render.BaseImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BnwPipelineTest {

    private fun px(r: Int, g: Int, b: Int): Int =
        0xFF000000.toInt() or (r shl 16) or (g shl 8) or b

    @Test
    fun `neutro convierte a gris y respeta la luminancia`() {
        val pixels = intArrayOf(px(255, 0, 0), px(0, 255, 0), px(0, 0, 255), px(100, 150, 200))
        FramePipeline.apply(pixels, 2, 2, EditStack(), listOf(Edit.Bnw("neutral")))
        for (p in pixels) {
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            assertEquals(r, g)
            assertEquals(g, b)
        }
        // luminancia del pixel mixto ~ 0.299*100+0.587*150+0.114*200 = 139
        val v = (pixels[3] shr 16) and 0xFF
        assertTrue(v in 130..148, "gris=$v")
    }

    @Test
    fun `filtro rojo aclara lo rojizo y oscurece lo azulado`() {
        val soloRojo = intArrayOf(px(255, 0, 0))
        val soloAzul = intArrayOf(px(0, 0, 255))
        FramePipeline.apply(soloRojo, 1, 1, EditStack(), listOf(Edit.Bnw("red")))
        FramePipeline.apply(soloAzul, 1, 1, EditStack(), listOf(Edit.Bnw("red")))
        val vRojo = (soloRojo[0] shr 16) and 0xFF
        val vAzul = (soloAzul[0] shr 16) and 0xFF
        assertTrue(vRojo > 145 && vRojo < 165, "rojo=$vRojo")
        assertTrue(vAzul < 20, "azul=$vAzul")
        assertTrue(vRojo > vAzul * 5, "el filtro rojo debe separar canales")
    }

    @Test
    fun `sin edit Bnw la imagen no cambia a gris`() {
        val pixels = intArrayOf(px(255, 0, 0))
        FramePipeline.apply(pixels, 1, 1, EditStack(), emptyList())
        val r = (pixels[0] shr 16) and 0xFF
        val b = pixels[0] and 0xFF
        assertTrue(r != b)
    }

    @Test
    fun `AutoLevels sobre imagen ya en gris no altera casi nada`() {
        // caso de borde: B/N + AUTO conviven sin romperse
        val pixels = IntArray(64 * 64) { gray(40 + it % 64 * 2) }
        val img = BaseImage(pixels.copyOf(), 64, 64)
        val r = AutoLevels.compute(img)
        assertTrue(r.exposureStops > 0f, "una imagen oscura debe iluminarse")
    }

    private fun gray(v: Int): Int = 0xFF000000.toInt() or (v shl 16) or (v shl 8) or v
}
