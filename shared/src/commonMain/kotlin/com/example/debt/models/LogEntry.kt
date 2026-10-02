package com.example.debt.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class LogEntry(
    val id: String,
    val timestamp: Instant,
    val actionType: String, // e.g., "ADD_DEBT", "UPDATE_DEBT", "DELETE_DEBT", "ADD_PAYMENT"
    val description: String,
    val actorEmail: String
)
