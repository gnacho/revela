package app.revela.core

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NewToolsTest {

    private fun pixel(r: Int, g: Int, b: Int): Int =
        (255 shl 24) or (r shl 16) or (g shl 8) or b

    private fun channel(p: Int): Int = (p ushr 16) and 0xFF

    @Test
    fun `boxBlur reduce alta frecuencia sin cambiar la media`() {
        val w = 64
        val h = 64
        val rnd = java.util.Random(42)
        val px = IntArray(w * h) { i ->
            val v = 100 + rnd.nextInt(101) - 50
            pixel(v, v, v)
        }
        val blurred = FrameMath.boxBlur(px, w, h, 4)
        fun mean(arr: IntArray): Double = arr.sumOf { channel(it).toDouble() } / arr.size
        fun variance(arr: IntArray): Double {
            val m = mean(arr)
            return arr.sumOf { val v = channel(it).toDouble(); (v - m) * (v - m) } / arr.size
        }
        val m0 = mean(px)
        val m1 = mean(blurred)
        assertTrue(abs(m1 - m0) < 2.0, "media similar: $m0 vs $m1")
        assertTrue(variance(blurred) < variance(px) * 0.5, "varianza reducida")
    }

    @Test
    fun `boxBlur de imagen uniforme es identidad`() {
        val px = IntArray(16) { pixel(120, 120, 120) }
        val out = FrameMath.boxBlur(px, 4, 4, 2)
        assertTrue(px.contentEquals(out))
    }

    @Test
    fun `grano es determinista y cambia la imagen`() {
        val px = IntArray(64 * 64) { i -> val v = (i * 7) % 256; pixel(v, v, v) }
        val a = px.copyOf()
        val b = px.copyOf()
        FrameMath.applyGrain(a, 64, 64, 0.8f)
        FrameMath.applyGrain(b, 64, 64, 0.8f)
        assertTrue(a.contentEquals(b), "misma entrada dos veces = mismo output")
        assertTrue(!a.contentEquals(px), "el grano debe alterar la imagen")
    }

    @Test
    fun `HDR sube el contraste local`() {
        val w = 64
        val h = 64
        val px = IntArray(w * h) { i -> val v = if ((i % w) < 32) 60 else 160; pixel(v, v, v) }
        val blurred = FrameMath.boxBlur(px, w, h, 4)
        fun localContrast(arr: IntArray, blur: IntArray): Double {
            var s = 0.0
            for (i in arr.indices) s += abs(channel(arr[i]) - channel(blur[i]))
            return s
        }
        val before = localContrast(px, blurred)
        val out = px.copyOf()
        FrameMath.applyHdr(out, w, h, 1f)
        val after = localContrast(out, blurred)
        assertTrue(after > before, "contraste local: $before -> $after")
    }

    @Test
    fun `blend modes con muestras exactas`() {
        assertEquals(255, BlendModes.channel(255, 0, "screen"))
        assertEquals(0, BlendModes.channel(0, 0, "screen"))
        assertEquals(255, BlendModes.channel(128, 255, "screen"))
        assertEquals(161, BlendModes.channel(100, 100, "screen"))
        assertEquals(0, BlendModes.channel(0, 255, "multiply"))
        assertEquals(128, BlendModes.channel(128, 255, "multiply"))
        assertEquals(39, BlendModes.channel(100, 100, "multiply"))
        assertEquals(200, BlendModes.channel(30, 200, "lighten"))
        assertEquals(30, BlendModes.channel(30, 200, "darken"))
        assertEquals(200, BlendModes.channel(100, 200, "normal"))
    }

    @Test
    fun `doble exposicion normal al 50 por ciento`() {
        val base = intArrayOf(pixel(100, 100, 100))
        val overlay = intArrayOf(pixel(200, 200, 200))
        FrameMath.applyDoubleExpo(base, overlay, 1, 1, "normal", 0.5f)
        assertEquals(150, channel(base[0]))
    }

    @Test
    fun `codec ida y vuelta de los edits nuevos`() {
        val stack = EditStack()
        stack.apply(Edit.Details(0.5f, -0.3f))
        stack.apply(Edit.Hdr(0.7f))
        stack.apply(Edit.Grain(0.4f))
        stack.apply(Edit.Glow(0.6f))
        stack.apply(Edit.LensBlur(0.5f))
        stack.apply(Edit.DoubleExpo("screen", 0.8f))
        stack.apply(Edit.TextOverlay("hola", 0xFFFF0000L, 0.2f, "top"))
        stack.apply(Edit.Frame(0xFF000000L, 0.1f))

        val restored = EditStack.fromJson(stack.toJson())
        val details = restored.edits.filterIsInstance<Edit.Details>().first()
        assertEquals(0.5f, details.structure, 1e-4f)
        assertEquals(-0.3f, details.sharpening, 1e-4f)
        assertEquals(0.7f, restored.edits.filterIsInstance<Edit.Hdr>().first().strength, 1e-4f)
        assertEquals(0.4f, restored.edits.filterIsInstance<Edit.Grain>().first().strength, 1e-4f)
        assertEquals(0.6f, restored.edits.filterIsInstance<Edit.Glow>().first().strength, 1e-4f)
        assertEquals(0.5f, restored.edits.filterIsInstance<Edit.LensBlur>().first().strength, 1e-4f)
        val de = restored.edits.filterIsInstance<Edit.DoubleExpo>().first()
        assertEquals("screen", de.blend)
        assertEquals(0.8f, de.opacity, 1e-4f)
        val tx = restored.edits.filterIsInstance<Edit.TextOverlay>().first()
        assertEquals("hola", tx.content)
        assertEquals(0xFFFF0000L, tx.colorInt)
        assertEquals("top", tx.gravity)
        assertEquals(0.2f, tx.sizePct, 1e-4f)
        val fr = restored.edits.filterIsInstance<Edit.Frame>().first()
        assertEquals(0xFF000000L, fr.colorInt)
        assertEquals(0.1f, fr.widthPct, 1e-4f)
    }

    @Test
    fun `desenfoque difumina mas las esquinas que el centro`() {
        val w = 101
        val h = 101
        val px = IntArray(w * h) { i -> val v = if ((i % 2) == 0) 40 else 200; pixel(v, v, v) }
        val out = px.copyOf()
        FrameMath.applyLensBlur(out, w, h, 1f)
        fun localVariance(arr: IntArray, cx: Int, cy: Int, r: Int): Double {
            var m = 0.0
            var n = 0
            for (y in cy - r..cy + r) for (x in cx - r..cx + r) {
                if (x in 0 until w && y in 0 until h) { m += channel(arr[y * w + x]); n++ }
            }
            m /= n
            var v = 0.0
            for (y in cy - r..cy + r) for (x in cx - r..cx + r) {
                if (x in 0 until w && y in 0 until h) {
                    val d = channel(arr[y * w + x]) - m
                    v += d * d
                }
            }
            return v / n
        }
        val centerVar = localVariance(out, 50, 50, 4)
        val cornerVar = localVariance(out, 2, 2, 4)
        assertTrue(cornerVar < centerVar, "esquina $cornerVar < centro $centerVar")
    }

    @Test
    fun `preset noir es no-op a intensidad cero y bnw a plena`() {
        assertTrue(Presets.Kind.NOIR.edits(0f).isEmpty())
        val full = Presets.Kind.NOIR.edits(1f)
        assertTrue(full.any { it is Edit.Bnw && it.filter == "neutral" })
        assertTrue(full.any { it is Edit.Contrast && it.amount > 0.3f })
    }

    @Test
    fun `preset drama escala con la intensidad`() {
        val half = Presets.Kind.DRAMA.edits(0.5f)
        val c = half.filterIsInstance<Edit.Contrast>().first()
        assertEquals(0.35f * 0.5f, c.amount, 1e-4f)
        assertTrue(half.any { it is Edit.Details && it.structure > 0f })
    }

    @Test
    fun `detalles estructura aumenta el contraste de un borde`() {
        val w = 40
        val h = 40
        val px = IntArray(w * h) { i -> val v = if ((i % w) < 20) 60 else 160; pixel(v, v, v) }
        val out = px.copyOf()
        FrameMath.applyDetails(out, w, h, 1f, 0f)
        val bright = channel(out[20])
        assertTrue(bright > 160, "overshoot en el borde: $bright")
        val far = channel(out[35])
        assertTrue(far < 170, "fondo apenas cambia: $far")
    }

    @Test
    fun `glow aclara sin superar 255`() {
        val w = 30
        val h = 30
        val px = IntArray(w * h) { i -> val v = if ((i % w) < 15) 80 else 200; pixel(v, v, v) }
        val out = px.copyOf()
        FrameMath.applyGlow(out, w, h, 1f)
        val before = px.sumOf { channel(it).toLong() }
        val after = out.sumOf { channel(it).toLong() }
        assertTrue(after > before, "glow aclara")
        assertTrue(out.all { channel(it) <= 255 })
    }
}
