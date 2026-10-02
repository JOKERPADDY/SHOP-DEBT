package com.example.debt.logic

import com.example.debt.models.Debt

object InvoiceHelper {
    fun formatInvoice(debts: List<Debt>, shopName: String = "Thawne Shop"): String {
        if (debts.isEmpty()) return ""
        
        val firstDebt = debts.first()
        val customerName = firstDebt.customerName
        
        val itemsList = debts.joinToString("\n") { debt ->
            val remaining = DebtCalculator.getDebtRemaining(debt)
            "• ${debt.product}: KES ${remaining.toInt()}"
        }
        
        val totalRemaining = debts.sumOf { DebtCalculator.getDebtRemaining(it) }
        val totalPaid = debts.sumOf { DebtCalculator.getDebtPaid(it) }
        val grandTotal = debts.sumOf { it.totalAmount }
        
        return """
            🧾 *DEBT INVOICE - ${shopName.uppercase()}*
            --------------------------
            *Customer:* $customerName
            --------------------------
            *Items:*
            $itemsList
            --------------------------
            *Total Owed:* KES ${grandTotal.toInt()}
            *Total Paid:* KES ${totalPaid.toInt()}
            *GRAND BALANCE:* _KES ${totalRemaining.toInt()}_
            --------------------------
            Please settle your balance as soon as possible. Thank you!
        """.trimIndent()
    }
}
