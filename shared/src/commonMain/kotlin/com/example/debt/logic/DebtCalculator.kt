package com.example.debt.logic

import com.example.debt.models.Debt
import kotlinx.datetime.*
import kotlin.math.ceil
import kotlin.math.roundToLong

object DebtCalculator {
    const val MONTHLY_INTEREST_RATE = 0.05 // 5%

    fun getDebtPaid(debt: Debt): Double {
        return debt.payments.sumOf { it.amount }
    }

    fun getDebtRemaining(debt: Debt): Double {
        return debt.totalAmount - getDebtPaid(debt)
    }

    fun isDebtOverdue(debt: Debt, today: LocalDate): Boolean {
        val remaining = getDebtRemaining(debt)
        return remaining > 0 && debt.dueDate < today
    }

    /**
     * Calculates late payment interest: 5% monthly simple interest.
     * Calculation: remaining * 0.05 * ceil(daysOverdue / 30)
     */
    fun calculateLatePaymentInterest(debt: Debt, today: LocalDate): Double {
        if (!isDebtOverdue(debt, today)) return 0.0

        val daysOverdue = debt.dueDate.daysUntil(today)
        val monthsOverdue = ceil(daysOverdue.toDouble() / 30.0)
        
        val remaining = getDebtRemaining(debt)
        val interest = remaining * MONTHLY_INTEREST_RATE * monthsOverdue
        
        return (interest * 100).roundToLong() / 100.0 // Round to 2 decimal places
    }

    fun getDebtStatus(debt: Debt, today: LocalDate): DebtStatus {
        val paid = getDebtPaid(debt)
        val remaining = getDebtRemaining(debt)
        val overdue = isDebtOverdue(debt, today)

        return when {
            overdue -> DebtStatus.OVERDUE
            remaining <= 0 -> DebtStatus.PAID
            paid > 0 -> DebtStatus.PARTIAL
            else -> DebtStatus.PENDING
        }
    }
}

enum class DebtStatus {
    PENDING, PARTIAL, PAID, OVERDUE
}
