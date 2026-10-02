package com.example.debt.models

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Debt(
    val id: String,
    val customerName: String,
    val customerID: String,
    val phoneNumber: String,
    val product: String,
    val totalAmount: Double,
    val payments: List<Payment> = emptyList(),
    val dateTaken: LocalDate,
    val dueDate: LocalDate,
    val notes: String? = "",
    val createdAt: Instant
)
