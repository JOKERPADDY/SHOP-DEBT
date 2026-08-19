package com.example.debt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.debt.models.Debt
import com.example.debt.repository.DebtRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

class AddDebtViewModel(
    private val repository: DebtRepository = DebtRepository()
) : ViewModel() {
    
    fun saveDebt(
        name: String,
        idNumber: String,
        phone: String,
        product: String,
        amount: Double,
        dateTaken: LocalDate,
        dueDate: LocalDate
    ) {
        viewModelScope.launch {
            val debt = Debt(
                id = Clock.System.now().toEpochMilliseconds().toString(),
                customerName = name,
                customerID = idNumber,
                phoneNumber = phone,
                product = product,
                totalAmount = amount,
                payments = emptyList(),
                dateTaken = dateTaken,
                dueDate = dueDate,
                createdAt = Clock.System.now()
            )
            repository.saveDebt(debt)
        }
    }
}
