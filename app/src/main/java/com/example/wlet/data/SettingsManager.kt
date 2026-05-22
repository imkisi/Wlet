package com.example.wlet.data

import android.content.Context
import android.content.SharedPreferences

/**
 * SettingsManager handles persistence for user preferences like Language and Currency.
 */
class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("wlet_settings", Context.MODE_PRIVATE)

    var currency: String
        get() = prefs.getString("currency", "IDR") ?: "IDR"
        set(value) = prefs.edit().putString("currency", value).apply()

    var language: String
        get() = prefs.getString("language", "id") ?: "id"
        set(value) = prefs.edit().putString("language", value).apply()
}
