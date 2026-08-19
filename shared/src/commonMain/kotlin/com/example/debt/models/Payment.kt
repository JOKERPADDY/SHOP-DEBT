package com.example.debt.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val amount: Double,
    val date: LocalDate,
    val note: String? = null
)
