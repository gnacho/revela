package app.revela.render

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import app.revela.core.ColorMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Imagen base de preview: pixeles ARGB originales (sin ajustes) + dimensiones. */
class BaseImage(val pixels: IntArray, val width: Int, val height: Int) {
    fun toBitmap(): Bitmap =
        Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)

    fun clonePixels(): IntArray = pixels.copyOf()
}

object PreviewRender {
    const val PREVIEW_MAX_SIDE = 1440

    /** Decodifica la imagen a tamano de preview (lado mayor acotado). */
    suspend fun decode(resolver: ContentResolver, uri: Uri): BaseImage? = withContext(Dispatchers.IO) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val boundsStream = resolver.openInputStream(uri)
        if (boundsStream == null) {
            android.util.Log.e("revela", "decode: openInputStream null (bounds)")
            return@withContext null
        }
        boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }
        android.util.Log.i(
            "revela",
            "decode bounds=${bounds.outWidth}x${bounds.outHeight} mime=${bounds.outMimeType}"
        )
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

        var sample = 1
        val maxOfBoth = maxOf(bounds.outWidth, bounds.outHeight)
        while (maxOfBoth / (sample * 2) >= PREVIEW_MAX_SIDE) sample *= 2

        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decodeStream = resolver.openInputStream(uri)
        if (decodeStream == null) {
            android.util.Log.e("revela", "decode: openInputStream null (decode)")
            return@withContext null
        }
        val bmp = decodeStream.use { BitmapFactory.decodeStream(it, null, opts) }
        if (bmp == null) {
            android.util.Log.e("revela", "decode: bitmap null")
            return@withContext null
        }
        val pixels = IntArray(bmp.width * bmp.height)
        bmp.getPixels(pixels, 0, bmp.width, 0, 0, bmp.width, bmp.height)
        val w = bmp.width
        val h = bmp.height
        bmp.recycle()
        BaseImage(pixels, w, h)
    }

    /** Renderiza la preview con los ajustes indicados (o la original si identity). */
    suspend fun render(base: BaseImage, adjustments: ColorMath.Adjustments, identity: Boolean): Bitmap =
        withContext(Dispatchers.Default) {
            if (identity) return@withContext base.toBitmap()
            val pixels = base.clonePixels()
            ColorMath.applyInPlace(pixels, ColorMath.buildLuts(adjustments), adjustments.saturation)
            Bitmap.createBitmap(pixels, base.width, base.height, Bitmap.Config.ARGB_8888)
        }
}
