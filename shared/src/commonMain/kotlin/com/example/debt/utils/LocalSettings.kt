@file:JvmName("CommonLocalSettings")

package com.example.debt.utils

import kotlinx.datetime.Clock
import kotlin.jvm.JvmName
import kotlin.random.Random

interface LocalSettings {
    fun saveString(key: String, value: String?)
    fun getString(key: String): String?
    fun clear()
}

expect fun getLocalSettings(): LocalSettings

object SettingsKeys {
    const val LINKED_SHOP_ID = "linked_shop_id"
    const val OWNER_EMAIL = "owner_email"
    const val SHOP_NAME = "shop_name"
    const val DEVICE_ID = "device_id"
    const val DEVICE_NAME = "device_name"
}

fun getOrCreateDeviceId(settings: LocalSettings = getLocalSettings()): String {
    val existing = settings.getString(SettingsKeys.DEVICE_ID)?.takeIf { it.isNotBlank() }
    if (existing != null) {
        return existing
    }
    val newId = "${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt(1000, 9999)}"
    settings.saveString(SettingsKeys.DEVICE_ID, newId)
    return newId
}
