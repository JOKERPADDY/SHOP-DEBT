package com.example.debt.utils

import java.util.prefs.Preferences

class DesktopLocalSettings : LocalSettings {
    private val prefs = Preferences.userRoot().node("com.example.debt")

    override fun saveString(key: String, value: String?) {
        if (value == null) {
            prefs.remove(key)
        } else {
            prefs.put(key, value)
        }
    }

    override fun getString(key: String): String? {
        return prefs.get(key, null)
    }

    override fun clear() {
        prefs.clear()
    }
}

actual fun getLocalSettings(): LocalSettings = DesktopLocalSettings()
