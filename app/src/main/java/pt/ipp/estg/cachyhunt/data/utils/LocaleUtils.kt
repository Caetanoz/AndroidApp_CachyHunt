package pt.ipp.estg.cachyhunt.data.utils

import android.content.Context
import java.util.Locale

object LocaleUtils {

    fun setLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)

        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        // Salvar o idioma no SharedPreferences
        val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("language", language)
            apply()
        }
    }

    fun getSavedLanguage(context: Context): String {
        val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return sharedPref.getString("language", Locale.getDefault().language) ?: "en"
    }
}