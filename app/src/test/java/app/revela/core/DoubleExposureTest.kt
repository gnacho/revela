package app.revela.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DoubleExposureTest {

    @Test
    fun `blend add suma y clampea a 255`() {
        assertEquals(200, BlendModes.channel(100, 100, "add"))
        assertEquals(255, BlendModes.channel(200, 100, "add"))
    }

    @Test
    fun `blend subtract resta y clampea a 0`() {
        assertEquals(50, BlendModes.channel(100, 50, "subtract"))
        assertEquals(0, BlendModes.channel(30, 50, "subtract"))
    }

    @Test
    fun `blend overlay oscurece bajos y aclara altos`() {
        // a oscuro (64) con b oscuro (64): 2ab/255 = 32 (mas oscuro que promedio)
        assertEquals(2 * 64 * 64 / 255, BlendModes.channel(64, 64, "overlay"))
        // a claro (192) con b claro (192): 255 - 2(63)(63)/255 = 224 (mas claro)
        assertEquals(255 - 2 * 63 * 63 / 255, BlendModes.channel(192, 192, "overlay"))
        // con b=128 (punto medio) el resultado es a (identidad)
        assertEquals(100, BlendModes.channel(100, 128, "overlay"))
    }

    @Test
    fun `mix respeta la opacidad 0 y 1`() {
        assertEquals(100, BlendModes.mix(100, 200, "add", 0f))
        assertEquals(BlendModes.channel(100, 200, "add"), BlendModes.mix(100, 200, "add", 1f))
    }

    @Test
    fun `transformacion identidad aplica el blend plano`() {
        val w = 16; val h = 16
        val base = IntArray(w * h) { 0xFF404040.toInt() }
        val overlay = IntArray(w * h) { 0xFF808080.toInt() }
        val copy = base.copyOf()
        FrameMath.applyDoubleExpo(copy, overlay, w, h, w, h, "add", 1f)
        assertEquals(0xFFC0C0C0.toInt(), copy[0])
    }

    @Test
    fun `offset desplaza la capa lateralmente`() {
        val w = 32; val h = 32
        val base = IntArray(w * h) { 0xFF000000.toInt() }
        // overlay: franja roja central (columnas 8..23), bordes negros (el clamp no estira rojo)
        val overlay = IntArray(w * h) { 0xFF000000.toInt() }
        for (y in 0 until h) for (x in 8 until 24) overlay[y * w + x] = 0xFFFF0000.toInt()

        fun apply(offX: Float): IntArray {
            val out = base.copyOf()
            FrameMath.applyDoubleExpo(out, overlay, w, h, w, h, "add", 1f, offsetX = offX)
            return out
        }
        fun meanRed(img: IntArray, x0: Int, x1: Int, y: Int): Int {
            var s = 0
            for (x in x0..x1) s += (img[y * w + x] shr 16) and 0xFF
            return s / (x1 - x0 + 1)
        }
        val a = apply(0f)
        val leftA = meanRed(a, 9, 12, h / 2)
        assertTrue(leftA > 200, "sin offset la zona izquierda es roja ($leftA)")
        val b = apply(0.25f)
        val leftB = meanRed(b, 9, 12, h / 2)
        assertTrue(leftB < 60, "el offset saco el rojo de la zona ($leftA -> $leftB)")
        // y el rojo aparece mas a la derecha
        val rightB = meanRed(b, 20, 24, h / 2)
        assertTrue(rightB > 200, "el rojo se movio a la derecha ($rightB)")
    }

    @Test
    fun `escala acerca el contenido de la capa`() {
        val w = 32; val h = 32
        val base = IntArray(w * h) { 0xFF000000.toInt() }
        // overlay: franja roja izquierda-central (columnas 8..15), frontera en u_overlay=-0.25
        val overlay = IntArray(w * h) { 0xFF000000.toInt() }
        for (y in 0 until h) for (x in 8 until 16) overlay[y * w + x] = 0xFFFF0000.toInt()

        fun apply(scale: Float): IntArray {
            val out = base.copyOf()
            FrameMath.applyDoubleExpo(out, overlay, w, h, w, h, "add", 1f, scale = scale)
            return out
        }
        fun redAt(img: IntArray, x: Int, y: Int): Int = (img[y * w + x] shr 16) and 0xFF
        val a = apply(1f)
        assertTrue(redAt(a, 3, h / 2) < 50, "escala 1: x=3 fuera de la franja")
        val b = apply(2f)
        // con escala 2 la franja se amplia: la frontera (u=-0.25) cae en u=-0.125 -> x~12
        assertTrue(redAt(b, 3, h / 2) > 150, "escala 2: x=3 ahora roja (${redAt(b, 3, h / 2)})")
        assertTrue(redAt(b, 18, h / 2) < 40, "escala 2: x=18 ya fuera de la franja (${redAt(b, 18, h / 2)})")
    }

    @Test
    fun `codec de doble exposicion conserva la transformacion`() {
        val stack = EditStack(listOf(
            Edit.DoubleExpo(blend = "overlay", opacity = 0.7f, scale = 1.4f, rotationDeg = 15f, offsetX = 0.1f, offsetY = -0.2f)
        ))
        val back = EditStack.fromJson(stack.toJson())
        val e = back.edits.single() as Edit.DoubleExpo
        assertEquals("overlay", e.blend)
        assertEquals(1.4f, e.scale)
        assertEquals(15f, e.rotationDeg)
        assertEquals(0.1f, e.offsetX)
        assertEquals(-0.2f, e.offsetY)
    }
}
