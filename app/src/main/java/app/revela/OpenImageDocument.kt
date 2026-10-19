package app.revela

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts

/** ACTION_OPEN_DOCUMENT de imagenes pidiendo tambien escritura (permite borrar el original). */
class OpenImageDocument : ActivityResultContracts.OpenDocument() {
    override fun createIntent(context: Context, input: Array<String>): Intent =
        super.createIntent(context, input).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
}
