package com.example.debt.models

import kotlinx.datetime.Instant

enum class AuditAction {
    CREATE, UPDATE, DELETE, PAYMENT, RESTORE
}

data class AuditEntry(
    val timestamp: Instant,
    val action: AuditAction,
    val debtId: Long?,
    val details: String,
    val user: String = "System"
)
