package app.revela.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeometryPassTest {

    /** Imagen de bloques de color constante (el muestreo bilineal es exacto en constantes). */
    private fun blocks(w: Int, h: Int): IntArray {
        val px = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                px[y * w + x] = when {
                    y < h / 2 && x < w / 2 -> 0xFF000000.toInt()
                    y < h / 2 -> 0xFFFFFFFF.toInt()
                    x < w / 2 -> 0xFF0000FF.toInt()
                    else -> 0xFF00FF00.toInt()
                }
            }
        }
        return px
    }

    @Test
    fun `perspectiva identidad devuelve copia identica`() {
        val src = blocks(64, 48)
        val out = PerspectiveMath.warp(src, 64, 48, PerspectiveCorners.IDENTITY)
        assertTrue(src.contentEquals(out))
    }

    @Test
    fun `perspectiva keystone desplaza el origen de la esquina`() {
        val w = 64; val h = 48
        val src = blocks(w, h)
        // bajamos la esquina superior izquierda al 20%: el pixel (0,0) de salida
        // toma su color de origen en v ~ 0.2 (zona roja/azul segun x)
        val corners = PerspectiveCorners(tlX = 0f, tlY = 0.2f)
        val out = PerspectiveMath.warp(src, w, h, corners)
        val topLeft = out[0]
        // en x=0 el origen es columna izquierda: arriba negro (y<0.2 -> negro o azul segun fila)
        val srcRef = src[((0.2f * (h - 1)).toInt().coerceIn(0, h - 1)) * w]
        assertEquals(srcRef, topLeft)
    }

    @Test
    fun `perspectiva converge arriba estrechando la parte superior`() {
        val w = 64; val h = 64
        // franjas verticales: cada columna un valor distinto
        val src = IntArray(w * h) { i -> 0xFF000000.toInt() or ((i % w) shl 16) }
        val corners = PerspectiveCorners(tlX = 0.3f, tlY = 0f, trX = 0.7f, trY = 0f)
        val out = PerspectiveMath.warp(src, w, h, corners)
        // fila central (v=0.5): D_x(u,0.5) = 0.15 + 0.7u -> los laterales se comprimen hacia fuera
        val row = (h / 2) * w
        val leftCol = (out[row + w / 4] shr 16) and 0xFF
        val rightCol = (out[row + 3 * w / 4] shr 16) and 0xFF
        assertTrue(leftCol in 5..15, "columna izq $leftCol")
        assertTrue(rightCol in 50..60, "columna der $rightCol")
        // el centro se mantiene
        val centerCol = (out[row + w / 2] shr 16) and 0xFF
        assertTrue(kotlin.math.abs(centerCol - 32) <= 2, "centro $centerCol")
        // la esquina superior izquierda del marco queda fuera del cuadrilatero: relleno de borde
        val cornerCol = (out[0] shr 16) and 0xFF
        assertTrue(cornerCol <= 4, "esquina $cornerCol")
    }

    @Test
    fun `rotatedPlane con angulos cero es identidad`() {
        val c = PerspectiveMath.rotatedPlane(0f, 0f, 0f)
        assertTrue(c.isIdentity(eps = 1e-3f), "corners=$c")
    }

    @Test
    fun `rotatedPlane yaw desplaza las esquinas horizontalmente de forma opuesta`() {
        val c = PerspectiveMath.rotatedPlane(yawDeg = 25f, pitchDeg = 0f, rollDeg = 0f)
        // con yaw > 0 un lado se acerca y el otro se aleja: las X de izquierda y derecha difieren
        assertTrue(kotlin.math.abs(c.tlX - c.trX) > 0.01f)
        // la fila superior sigue siendo una linea razonable (sin cruzarse)
        assertTrue(c.tlX < c.trX)
    }

    @Test
    fun `expand replica el borde`() {
        val w = 8; val h = 8
        val src = IntArray(w * h) { 0xFF112233.toInt() }
        src[0] = 0xFFAA0000.toInt() // esquina sup-izq distinta
        // 0.25 por lado -> ow=oh=10, offset (2,2)
        val (out, ow, oh) = ExpandMath.expand(src, w, h, ExpandEdges(left = 0.25f, top = 0.25f), mirror = false)
        assertEquals(10, ow)
        assertEquals(10, oh)
        // la zona nueva replica el borde (fila 0 / columna 0), incluida su esquina
        assertEquals("ffaa0000", "%08x".format(out[0]))
        // el contenido original queda desplazado al offset (2,2)
        assertEquals(0xFFAA0000.toInt(), out[2 * ow + 2])
        assertEquals(src[2 * w + 2], out[(2 + 2) * ow + (2 + 2)])
    }

    @Test
    fun `expand espejo refleja el contenido`() {
        val w = 4; val h = 4
        val src = IntArray(w * h) { i -> 0xFF000000.toInt() or (i shl 8) }
        // 0.25 por lado -> ow = 4*1.25 = 5, offX = 1
        val (out, ow, oh) = ExpandMath.expand(src, w, h, ExpandEdges(left = 0.25f), mirror = true)
        assertEquals(5, ow)
        assertEquals(4, oh)
        // columna nueva x=0 refleja x=0; contenido en x=1..4
        assertEquals(src[0], out[0])
        assertEquals(src[0], out[1])
        assertEquals(src[3], out[4])
    }

    @Test
    fun `expand respeta el limite de 045 por lado`() {
        val src = blocks(8, 8)
        val (out, ow, oh) = ExpandMath.expand(src, 8, 8, ExpandEdges(left = 0.9f), mirror = false)
        assertEquals(11, ow) // 8 * 1.45
        assertEquals(8, oh)
        assertEquals(out.size, ow * oh)
    }

    @Test
    fun `expand vacio es no-op`() {
        val src = blocks(16, 16)
        val (out, ow, oh) = ExpandMath.expand(src, 16, 16, ExpandEdges(), mirror = false)
        assertEquals(16, ow); assertEquals(16, oh)
        assertTrue(src.contentEquals(out))
    }

    @Test
    fun `geometrypass aplica expand antes que perspective`() {
        val w = 16; val h = 16
        val src = blocks(w, h)
        val edits = listOf<Edit>(
            Edit.Expand(edges = ExpandEdges(left = 0.25f), mirror = false),
            Edit.Perspective(corners = PerspectiveCorners(tlY = 0.1f))
        )
        val (out, ow, oh) = GeometryPass.apply(src, w, h, edits)
        assertEquals(20, ow)
        assertEquals(16, oh)
        assertEquals(out.size, ow * oh)
    }

    @Test
    fun `headpose sin puntos es no-op y con yaw deforma la zona facial`() {
        val w = 64; val h = 64
        val src = blocks(w, h)
        val noPoints = HeadPoseMath.warp(src, w, h, Edit.HeadPose())
        assertTrue(src.contentEquals(noPoints))

        val le = ControlPoint(0.4f, 0.4f)
        val re = ControlPoint(0.6f, 0.4f)
        val mo = ControlPoint(0.5f, 0.6f)
        val warped = HeadPoseMath.warp(src, w, h, Edit.HeadPose(le, re, mo, yawDeg = 30f))
        // la imagen cambia cerca de la nariz (0.5, ~0.52)
        val noseIdx = ((0.52f * h).toInt()) * w + (0.55f * w).toInt()
        assertTrue(src[noseIdx] != warped[noseIdx], "la nariz debe moverse con yaw")
        // lejos de la cara (esquina 0,0) queda igual (fuera del radio IDW)
        assertEquals(src[1 * w + 1], warped[1 * w + 1])
    }

    @Test
    fun `perspective y expand sobreviven al codec de la pila`() {
        val stack = EditStack(listOf<Edit>(
            Edit.Expand(edges = ExpandEdges(top = 0.1f, bottom = 0.2f), mirror = true),
            Edit.Perspective(corners = PerspectiveCorners(trX = 0.8f), mode = "free"),
            Edit.HeadPose(ControlPoint(0.4f, 0.4f), ControlPoint(0.6f, 0.4f), ControlPoint(0.5f, 0.6f), yawDeg = 10f)
        ))
        val back = EditStack.fromJson(stack.toJson())
        val e = back.edits
        assertTrue(e[0] is Edit.Expand && (e[0] as Edit.Expand).edges.bottom == 0.2f)
        assertTrue(e[1] is Edit.Perspective && (e[1] as Edit.Perspective).corners.trX == 0.8f)
        assertTrue(e[2] is Edit.HeadPose && (e[2] as Edit.HeadPose).yawDeg == 10f)
    }
}
