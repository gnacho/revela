package app.revela.core

import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Ajustes de revela (export por defecto). Persistente en filesDir. */
@Serializable
data class AppSettings(
    val exportPng: Boolean = false,
    val exportQuality: Int = 92
)

object SettingsStore {
    private const val FILE = "revela_settings.json"
    private val json = Json { ignoreUnknownKeys = true }

    fun load(dir: File): AppSettings =
        runCatching { json.decodeFromString(AppSettings.serializer(), File(dir, FILE).readText()) }
            .getOrDefault(AppSettings())

    fun save(dir: File, settings: AppSettings) {
        runCatching {
            File(dir, FILE).writeText(json.encodeToString(AppSettings.serializer(), settings))
        }
    }
}
