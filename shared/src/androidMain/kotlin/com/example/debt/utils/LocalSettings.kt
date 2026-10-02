@file:JvmName("AndroidLocalSettingsKt")

package com.example.debt.utils

import android.content.Context
import android.content.SharedPreferences
import kotlin.jvm.JvmName

class AndroidLocalSettings(private val context: Context) : LocalSettings {
    private val prefs: SharedPreferences = context.getSharedPreferences("debt_manager_prefs", Context.MODE_PRIVATE)

    override fun saveString(key: String, value: String?) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }
}

private var settings: LocalSettings? = null

fun initializeSettings(context: Context) {
    settings = AndroidLocalSettings(context)
}

actual fun getLocalSettings(): LocalSettings {
    return settings ?: throw IllegalStateException("LocalSettings not initialized. Call initializeSettings first.")
}
