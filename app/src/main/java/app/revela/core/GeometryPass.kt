package app.revela.core

/** Pase geometrico final: Expand (cambia lienzo) -> HeadPose -> Perspective (warps). */
object GeometryPass {

    /** Aplica las transformaciones geometricas de la pila sobre el frame compuesto. */
    fun apply(frame: IntArray, w: Int, h: Int, edits: List<Edit>): Triple<IntArray, Int, Int> {
        var f = frame
        var fw = w
        var fh = h

        edits.filterIsInstance<Edit.Expand>().lastOrNull()?.let { e ->
            if (!e.edges.isEmpty()) {
                val r = ExpandMath.expand(f, fw, fh, e.edges, e.mirror)
                f = r.first; fw = r.second; fh = r.third
            }
        }
        edits.filterIsInstance<Edit.HeadPose>().lastOrNull()?.let { hp ->
            f = HeadPoseMath.warp(f, fw, fh, hp)
        }
        edits.filterIsInstance<Edit.Perspective>().lastOrNull()?.let { p ->
            if (!p.corners.isIdentity()) f = PerspectiveMath.warp(f, fw, fh, p.corners)
        }
        return Triple(f, fw, fh)
    }
}
