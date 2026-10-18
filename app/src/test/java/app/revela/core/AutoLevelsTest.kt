package app.revela.core

import app.revela.render.BaseImage
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AutoLevelsTest {

    private fun gray(v: Int): Int = 0xFF000000.toInt() or (v shl 16) or (v shl 8) or v

    private fun img(w: Int, h: Int, shade: (Int, Int) -> Int): BaseImage {
        val px = IntArray(w * h) { i ->
            val x = i % w
            val y = i / w
            shade(x, y)
        }
        return BaseImage(px, w, h)
    }

    @Test
    fun `imagen oscura sube la exposicion`() {
        val img = img(128, 128) { x, _ -> gray(12 + x * 70 / 128) } // tramo 12..82
        val r = AutoLevels.compute(img)
        assertTrue(r.exposureStops > 0.25f, "exposicion=${r.exposureStops}")
        assertTrue(r.contrast > 0.2f, "contraste=${r.contrast}")
    }

    @Test
    fun `imagen plana centrada anade contraste sin tocar exposicion`() {
        val img = img(128, 128) { x, _ -> gray(105 + x * 40 / 128) } // tramo 105..145 centrado
        val r = AutoLevels.compute(img)
        assertTrue(r.contrast > 0.3f, "contraste=${r.contrast}")
        assertTrue(abs(r.exposureStops) < 0.15f, "exposicion=${r.exposureStops}")
    }

    @Test
    fun `imagen equilibrada queda casi intacta`() {
        val img = img(256, 64) { x, _ -> gray(x * 255 / 256) } // rango completo
        val r = AutoLevels.compute(img)
        assertTrue(abs(r.exposureStops) < 0.1f, "exposicion=${r.exposureStops}")
        assertTrue(r.contrast < 0.1f, "contraste=${r.contrast}")
    }

    @Test
    fun `saturacion solo sube si hay color moderado`() {
        val gris = img(64, 64) { _, _ -> gray(128) }
        assertEquals(0f, AutoLevels.compute(gris).saturation)
        // moderada: sat = (150-80)/150 = 0.47 -> empujoncito
        val moderada = img(64, 64) { x, _ ->
            0xFF000000.toInt() or (150 shl 16) or ((100 + x / 8) shl 8) or 80
        }
        assertTrue(AutoLevels.compute(moderada).saturation > 0f)
        // vivisima: sat = 210/230 = 0.91 -> sin empujon
        val vivida = img(64, 64) { _, _ ->
            0xFF000000.toInt() or (230 shl 16) or (30 shl 8) or 20
        }
        assertEquals(0f, AutoLevels.compute(vivida).saturation)
    }
}
