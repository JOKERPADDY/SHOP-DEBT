package com.example.debt

import com.example.debt.models.Debt

interface PlatformActions {
    fun sendWhatsAppReminder(debt: Debt, message: String)
    fun exportToPdf(debts: List<Debt>)
    fun exportToCsv(debts: List<Debt>)
    fun importFromCsv(onImported: (List<Debt>) -> Unit)
}

expect fun getPlatformActions(): PlatformActions
