package com.maintenance.app.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Manager for handling app language/locale preferences.
 * Supports Spanish and English, with extensibility for additional languages.
 */
object LocaleManager {
    private const val PREFS_NAME = "locale_prefs"
    private const val LANGUAGE_KEY = "app_language"
    
    // Supported languages
    const val SPANISH = "es"
    const val ENGLISH = "en"
    
    /**
     * Get the currently set language preference
     */
    fun getSelectedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(LANGUAGE_KEY, SPANISH) ?: SPANISH
    }
    
    /**
     * Set the language preference and apply it
     */
    fun setLanguage(context: Context, languageCode: String) {
        // Save preference
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(LANGUAGE_KEY, languageCode).apply()
        
        // Apply the locale
        applyLocale(context, languageCode)
    }
    
    /**
     * Apply locale to the app configuration
     */
    private fun applyLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    
    /**
     * Get a human-readable label for a language code
     */
    fun getLanguageLabel(languageCode: String): String {
        return when (languageCode) {
            SPANISH -> "Español"
            ENGLISH -> "English"
            else -> "Unknown"
        }
    }
    
    /**
     * Get all supported language codes
     */
    fun getSupportedLanguages(): List<String> {
        return listOf(SPANISH, ENGLISH)
    }
}
