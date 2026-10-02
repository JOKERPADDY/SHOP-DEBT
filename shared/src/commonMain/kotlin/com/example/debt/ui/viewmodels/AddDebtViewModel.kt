package com.example.debt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.debt.models.Debt
import com.example.debt.repository.DebtRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class AddDebtUiState(
    val existingDebt: Debt? = null,
    val name: String = "",
    val idNumber: String = "",
    val phone: String = "",
    val product: String = "",
    val amount: String = "",
    val dueDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val dueDateString: String = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString(),
    val notes: String = "",
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false
)

class AddDebtViewModel(
    private val debtId: String? = null,
    private val repository: DebtRepository = DebtRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddDebtUiState())
    val uiState: StateFlow<AddDebtUiState> = _uiState.asStateFlow()

    init {
        if (debtId != null) {
            loadDebt(debtId)
        }
    }

    private fun loadDebt(id: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                repository.getDebts().collectLatest { debts ->
                    val debt = debts.find { it.id == id }
                    if (debt != null) {
                        _uiState.value = AddDebtUiState(
                            existingDebt = debt,
                            name = debt.customerName,
                            idNumber = debt.customerID,
                            phone = debt.phoneNumber,
                            product = debt.product,
                            amount = debt.totalAmount.toInt().toString(),
                            dueDate = debt.dueDate,
                            dueDateString = debt.dueDate.toString(),
                            notes = debt.notes ?: "",
                            isEditMode = true,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            } catch (e: Exception) {
                println("Error collecting debts in AddDebtViewModel: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onDueDateChange(date: LocalDate) {
        _uiState.value = _uiState.value.copy(dueDate = date, dueDateString = date.toString())
    }

    fun onDueDateStringChange(dateString: String) {
        _uiState.value = _uiState.value.copy(dueDateString = dateString)
        try {
            val parsedDate = LocalDate.parse(dateString)
            _uiState.value = _uiState.value.copy(dueDate = parsedDate)
        } catch (e: Exception) {
            // Ignore invalid partial input
        }
    }

    fun onNotesChange(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun saveDebt(
        name: String,
        idNumber: String,
        phone: String,
        product: String,
        amount: Double,
        dateTaken: LocalDate
    ) {
        viewModelScope.launch {
            try {
                val existing = _uiState.value.existingDebt
                val debt = Debt(
                    id = existing?.id ?: Clock.System.now().toEpochMilliseconds().toString(),
                    customerName = name,
                    customerID = idNumber,
                    phoneNumber = phone,
                    product = product,
                    totalAmount = amount,
                    payments = existing?.payments ?: emptyList(),
                    dateTaken = existing?.dateTaken ?: dateTaken,
                    dueDate = _uiState.value.dueDate,
                    notes = _uiState.value.notes,
                    createdAt = existing?.createdAt ?: Clock.System.now()
                )
                repository.saveDebt(debt, isUpdate = existing != null)
            } catch (e: Exception) {
                println("Error saving debt in AddDebtViewModel: ${e.message}")
            }
        }
    }
}
