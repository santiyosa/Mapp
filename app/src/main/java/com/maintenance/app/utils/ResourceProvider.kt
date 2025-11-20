package com.maintenance.app.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provider for resources that respects app language settings.
 * Ensures strings are retrieved in the language selected by the user, not system locale.
 */
@Singleton
class ResourceProvider @Inject constructor(
    private val context: Context
) {
    /**
     * Get a string resource with the current app language configuration.
     */
    fun getString(resId: Int): String {
        val currentLanguage = LocaleManager.getSelectedLanguage(context)
        return getStringWithLocale(resId, currentLanguage)
    }

    /**
     * Get a string resource with a specific locale.
     */
    private fun getStringWithLocale(resId: Int, languageCode: String): String {
        return try {
            // Create a new configuration with the desired locale
            val locale = Locale(languageCode)
            val config = Configuration(context.resources.configuration).apply {
                setLocale(locale)
            }
            
            // Create a context with the new configuration
            val newContext = context.createConfigurationContext(config)
            newContext.resources.getString(resId)
        } catch (e: Exception) {
            // Fallback to default
            context.resources.getString(resId)
        }
    }
}
