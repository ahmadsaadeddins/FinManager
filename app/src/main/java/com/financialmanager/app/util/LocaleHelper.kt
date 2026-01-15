package com.financialmanager.app.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Helper class for managing app locale/language settings
 */
object LocaleHelper {

    const val LANGUAGE_SYSTEM = "system"
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_ARABIC = "ar"
    
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    /**
     * Get list of available languages
     */
    fun getAvailableLanguages(): List<LanguageOption> {
        return listOf(
            LanguageOption(LANGUAGE_SYSTEM, "تلقائي (System)", "System Default"),
            LanguageOption(LANGUAGE_ENGLISH, "English", "English"),
            LanguageOption(LANGUAGE_ARABIC, "العربية", "Arabic")
        )
    }
    
    /**
     * Save selected language to SharedPreferences
     */
    fun saveLanguage(context: Context, languageCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, languageCode)
            .apply()
    }
    
    /**
     * Get saved language from SharedPreferences
     */
    fun getSavedLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, LANGUAGE_SYSTEM) ?: LANGUAGE_SYSTEM
    }

    /**
     * Apply locale to context
     */
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = when (languageCode) {
            LANGUAGE_ENGLISH -> Locale.ENGLISH
            LANGUAGE_ARABIC -> Locale("ar")
            else -> Locale.getDefault() // System default
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * Update resources configuration for an activity
     */
    fun updateActivityLocale(activity: Activity, languageCode: String) {
        val locale = when (languageCode) {
            LANGUAGE_ENGLISH -> Locale.ENGLISH
            LANGUAGE_ARABIC -> Locale("ar")
            else -> Locale.getDefault()
        }

        Locale.setDefault(locale)

        val config = Configuration(activity.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        config.setLayoutDirection(locale)

        @Suppress("DEPRECATION")
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }

    /**
     * Get display name for a language code
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_ENGLISH -> "English"
            LANGUAGE_ARABIC -> "العربية"
            LANGUAGE_SYSTEM -> "System Default"
            else -> languageCode
        }
    }
}

data class LanguageOption(
    val code: String,
    val nativeName: String,
    val englishName: String
)
