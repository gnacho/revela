package app.revela.render

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import app.revela.core.Edit
import app.revela.core.EditStack
import app.revela.core.FrameMath
import app.revela.core.FramePipeline
import app.revela.core.Look
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Miniaturas del carrusel de estilos: la foto actual con cada look aplicado encima de la pila. */
object ThumbRender {

    fun looks(
        base: BaseImage,
        stack: EditStack,
        edits: List<Edit>,
        quarterTurns: Int,
        maxSide: Int = 220
    ): Map<String, ImageBitmap> {
        val scale = min(1f, maxSide.toFloat() / maxOf(base.width, base.height))
        val tw = max(1, (base.width * scale).roundToInt())
        val th = max(1, (base.height * scale).roundToInt())
        val src = Bitmap.createScaledBitmap(base.toBitmap(), tw, th, true)
        var px = IntArray(tw * th)
        src.getPixels(px, 0, tw, 0, 0, tw, th)
        var w = tw
        var h = th
        if (((quarterTurns % 4) + 4) % 4 != 0) {
            val r = FrameMath.rotateQuarter(px, tw, th, quarterTurns)
            px = r.first
            w = r.second
            h = r.third
        }
        val out = mutableMapOf<String, ImageBitmap>()
        for (look in Look.entries) {
            val withoutLooks = edits.filterNot { it is Edit.Look }
            val e = if (look.id == "none") withoutLooks else withoutLooks + Edit.Look(look.id)
            val copy = px.copyOf()
            FramePipeline.apply(copy, w, h, stack, e)
            out[look.id] = Bitmap.createBitmap(copy, w, h, Bitmap.Config.ARGB_8888).asImageBitmap()
        }
        return out
    }
}
