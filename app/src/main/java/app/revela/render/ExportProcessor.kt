package app.revela.render

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.os.Environment
import androidx.exifinterface.media.ExifInterface
import app.revela.core.ColorMath
import app.revela.core.EditStack
import app.revela.core.FramePipeline
import app.revela.core.GeometryMath
import app.revela.core.GeometryModel
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ExportOptions(
    val png: Boolean = false,
    val quality: Int = 92,          // solo JPEG (60..100)
    val maxSide: Int = 4096         // acota memoria; 0 = sin limite
)

object ExportProcessor {

    /**
     * Renderiza a resolucion completa y guarda en MediaStore (Carpeta Imagenes/revela).
     * Geometria (giro + enderezado + recorte) y color comparten la misma matematica que la preview.
     */
    suspend fun export(
        context: Context,
        sourceUri: Uri,
        stack: EditStack,
        geometry: GeometryModel,
        options: ExportOptions
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            android.util.Log.i("revela", "export: inicio")
            val resolver = context.contentResolver

            // 1. Decodificar fuente acotada
            android.util.Log.i("revela", "export: uri=$sourceUri")
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            val boundsStream = resolver.openInputStream(sourceUri)
                ?: error("no se pudo abrir la imagen de origen")
            boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                error("no se pudieron leer las dimensiones de la imagen de origen")
            }
            android.util.Log.i("revela", "export: fuente ${bounds.outWidth}x${bounds.outHeight}")
            var sample = 1
            val srcMax = maxOf(bounds.outWidth, bounds.outHeight)
            if (options.maxSide > 0) {
                while (srcMax / (sample * 2) >= options.maxSide) sample *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
            val decodeStream = resolver.openInputStream(sourceUri)
                ?: error("no se pudo abrir la imagen de origen (2)")
            val src = decodeStream.use {
                BitmapFactory.decodeStream(it, null, decodeOpts)
            } ?: error("no se pudo decodificar la imagen de origen")

            // 2. Geometria: rotacion total + enderezado (recorte del rectangulo inscrito) + recorte
            val rotation = Matrix().apply { postRotate(geometry.totalRotationDeg) }
            val rotated = Bitmap.createBitmap(src, 0, 0, src.width, src.height, rotation, true)
            if (rotated != src) src.recycle()

            var current = rotated
            if (geometry.straightenDeg != 0f) {
                val rad = Math.toRadians(geometry.straightenDeg.toDouble()).toFloat()
                val (iw, ih) = GeometryMath.largestInscribed(
                    rotated.width.toFloat(), rotated.height.toFloat(), rad,
                    rotated.width.toFloat() / rotated.height.toFloat()
                )
                val left = ((rotated.width - iw) / 2f).roundToInt()
                val top = ((rotated.height - ih) / 2f).roundToInt()
                val trimmed = Bitmap.createBitmap(
                    rotated, left, top, iw.roundToInt().coerceAtMost(rotated.width - left),
                    ih.roundToInt().coerceAtMost(rotated.height - top)
                )
                if (trimmed != rotated) rotated.recycle()
                current = trimmed
            }

            val cropW = (geometry.cropWidthNorm * current.width).roundToInt()
                .coerceIn(1, current.width)
            val cropH = (geometry.cropHeightNorm * current.height).roundToInt()
                .coerceIn(1, current.height)
            val cropX = (geometry.cropLeft * current.width).roundToInt()
                .coerceIn(0, current.width - cropW)
            val cropY = (geometry.cropTop * current.height).roundToInt()
                .coerceIn(0, current.height - cropH)

            // 3. Pases de marco sobre el encuadre girado completo (mismo orden que la preview):
            //    sanar -> color global -> selectivo -> vineta
            val frameW = current.width
            val frameH = current.height
            val framePixels = IntArray(frameW * frameH)
            current.getPixels(framePixels, 0, frameW, 0, 0, frameW, frameH)
            FramePipeline.apply(framePixels, frameW, frameH, stack)
            current.setPixels(framePixels, 0, frameW, 0, 0, frameW, frameH)

            val cropped = Bitmap.createBitmap(current, cropX, cropY, cropW, cropH)
            if (cropped != current) current.recycle()

            // 4. Guardar: fichero temporal -> EXIF -> MediaStore

            // 4. Guardar: fichero temporal -> EXIF -> MediaStore
            android.util.Log.i(
                "revela",
                "export: salida ${cropped.width}x${cropped.height} png=${options.png}"
            )
            val mime = if (options.png) "image/png" else "image/jpeg"
            val ext = if (options.png) "png" else "jpg"
            val stamp = System.currentTimeMillis()
            val temp = java.io.File(context.cacheDir, "revela_export_$stamp.$ext")
            temp.outputStream().use { fos ->
                if (options.png) {
                    cropped.compress(Bitmap.CompressFormat.PNG, 100, fos)
                } else {
                    cropped.compress(Bitmap.CompressFormat.JPEG, options.quality.coerceIn(60, 100), fos)
                }
            }
            cropped.recycle()

            copyExif(resolver, sourceUri, temp)

            val displayName = "revela_$stamp.$ext"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, mime)
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/revela")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val target = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("MediaStore rechazo la insercion")
            android.util.Log.i("revela", "export: insert ok -> $target")
            resolver.openOutputStream(target)?.use { os ->
                temp.inputStream().use { it.copyTo(os) }
            } ?: error("no se pudo escribir en MediaStore")
            temp.delete()
            val done = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            resolver.update(target, done, null, null)
            target
        }
    }

    /** Copia metadatos basicos de la fuente; la orientacion va horneada (geometria aplicada). */
    private fun copyExif(resolver: android.content.ContentResolver, source: Uri, targetFile: java.io.File) {
        runCatching {
            val src = resolver.openInputStream(source)?.use { ExifInterface(it) } ?: return
            val dst = ExifInterface(targetFile.absolutePath)
            for (tag in arrayOf(
                ExifInterface.TAG_MAKE,
                ExifInterface.TAG_MODEL,
                ExifInterface.TAG_DATETIME_ORIGINAL,
                ExifInterface.TAG_DATETIME,
                ExifInterface.TAG_F_NUMBER,
                ExifInterface.TAG_EXPOSURE_TIME,
                ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
                ExifInterface.TAG_FOCAL_LENGTH,
                ExifInterface.TAG_GPS_LATITUDE,
                ExifInterface.TAG_GPS_LATITUDE_REF,
                ExifInterface.TAG_GPS_LONGITUDE,
                ExifInterface.TAG_GPS_LONGITUDE_REF
            )) {
                val value = src.getAttribute(tag)
                if (value != null) dst.setAttribute(tag, value)
            }
            dst.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL.toString())
            dst.saveAttributes()
        }
    }
}
