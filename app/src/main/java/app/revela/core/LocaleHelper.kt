package app.revela.core

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/** Aplica el idioma guardado al contexto. "system" deja el idioma del sistema. */
object LocaleHelper {
    fun apply(context: Context, lang: String): Context {
        if (lang == "system") return context
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }
}
