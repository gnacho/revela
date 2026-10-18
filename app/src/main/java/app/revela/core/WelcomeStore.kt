package app.revela.core

import java.io.File

/** Asistente de bienvenida: marca de visto en un fichero del directorio de datos. */
object WelcomeStore {

    fun isSeen(dir: File): Boolean = file(dir).exists()

    fun markSeen(dir: File) {
        runCatching { file(dir).writeText("visto") }
    }

    fun reset(dir: File) {
        file(dir).delete()
    }

    private fun file(dir: File) = File(dir, "revela_bienvenida.txt")
}
