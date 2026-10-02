package com.example.debt.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class LinkedDevice(
    val deviceId: String,
    val deviceName: String,
    val linkedAt: Instant
)
