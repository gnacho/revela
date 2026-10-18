package app.revela.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import app.revela.core.Edit
import kotlin.math.min

/**
 * Composicion de overlays sobre el marco terminado (tras FramePipeline):
 * texto centrado (TextOverlay) y borde recto (Frame). Se aplica en preview y export
 * en el MISMO punto para que preview == export. Nada de android.graphics entra en core.
 */
object TextCompositor {

    fun apply(bitmap: Bitmap, edits: List<Edit>) {
        for (e in edits) {
            when (e) {
                is Edit.TextOverlay -> drawText(bitmap, e)
                is Edit.Frame -> drawFrame(bitmap, e)
                else -> {}
            }
        }
    }

    private fun drawText(bitmap: Bitmap, e: Edit.TextOverlay) {
        if (e.content.isEmpty()) return
        val w = bitmap.width
        val h = bitmap.height
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = e.colorInt.toInt()
            textSize = (h * e.sizePct).coerceAtLeast(4f)
            textAlign = Paint.Align.CENTER
        }
        val fm = paint.fontMetrics
        val x = w / 2f
        val baseline = when (e.gravity) {
            "top" -> -fm.top + paint.textSize * 0.15f
            "bottom" -> h - fm.bottom - paint.textSize * 0.15f
            else -> h / 2f - (fm.ascent + fm.descent) / 2f
        }
        Canvas(bitmap).drawText(e.content, x, baseline, paint)
    }

    private fun drawFrame(bitmap: Bitmap, e: Edit.Frame) {
        val w = bitmap.width
        val h = bitmap.height
        val thickness = (min(w, h) * e.widthPct).coerceAtLeast(1f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = e.colorInt.toInt()
            style = Paint.Style.STROKE
            strokeWidth = thickness
        }
        val inset = thickness / 2f
        Canvas(bitmap).drawRect(inset, inset, w - inset, h - inset, paint)
    }
}
