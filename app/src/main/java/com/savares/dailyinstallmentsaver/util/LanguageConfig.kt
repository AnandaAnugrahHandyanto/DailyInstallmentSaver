package com.savares.dailyinstallmentsaver.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.*

object LanguageConfig {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANG = "key_language"

    fun setLanguage(context: Context, langCode: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, langCode).apply()
        updateResources(context, langCode)
    }

    fun getLanguage(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, "en") ?: "en"
    }

    fun updateResources(context: Context, langCode: String): Context {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
