package app.revela

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Selector de fotos del sistema.
 *
 * OJO (issue #1): con ACTION_OPEN_DOCUMENT, el DocumentsUI del OnePlus (ColorOS) NO devuelve
 * el resultado; al tocar la foto lanza el intent VIEW del sistema ("Abrir con") y nos deja sin
 * URI. ACTION_GET_CONTENT si devuelve el resultado en ese dispositivo, pero no permite pedir
 * permiso persistente, asi que el permiso de escritura se gestiona aparte con
 * [DeleteOriginal.requestWrite].
 */
class OpenImageDocument : ActivityResultContracts.GetContent() {
    override fun createIntent(context: Context, input: String): Intent =
        super.createIntent(context, input)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}

/** Escritura sobre la foto original (necesaria solo para borrarla). */
object DeleteOriginal {

    /**
     * Lanza el dialogo del sistema que concede permiso de escritura sobre la URI.
     * Devuelve el IntentSender o null si el proveedor no lo admite (Photo Picker, por ejemplo).
     */
    fun requestWrite(context: Context, uri: Uri): android.content.IntentSender? = runCatching {
        MediaStore.createWriteRequest(context.contentResolver, listOf(uri)).intentSender
    }.getOrNull()

    /** True si el proveedor dice que podemos escribir en la URI ahora mismo. */
    fun canWrite(resolver: ContentResolver, uri: Uri): Boolean = runCatching {
        resolver.openOutputStream(uri)?.use { true } ?: false
    }.getOrDefault(false)
}
