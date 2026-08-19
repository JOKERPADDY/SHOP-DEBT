package com.example.debt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.debt.models.Debt
import com.example.debt.models.Payment
import com.example.debt.repository.DebtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class DebtDetailUiState(
    val debt: Debt? = null,
    val isLoading: Boolean = true
)

class DebtDetailViewModel(
    private val debtId: String,
    private val repository: DebtRepository = DebtRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(DebtDetailUiState())
    val uiState: StateFlow<DebtDetailUiState> = _uiState.asStateFlow()

    init {
        loadDebt()
    }

    private fun loadDebt() {
        viewModelScope.launch {
            repository.getDebts().collectLatest { debts ->
                val debt = debts.find { it.id == debtId }
                _uiState.value = DebtDetailUiState(debt = debt, isLoading = false)
            }
        }
    }

    fun addPayment(amount: Double, note: String) {
        val currentDebt = uiState.value.debt ?: return
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val newPayment = Payment(amount = amount, date = today, note = note)
        
        viewModelScope.launch {
            val updatedDebt = currentDebt.copy(
                payments = currentDebt.payments + newPayment
            )
            repository.saveDebt(updatedDebt)
        }
    }
}
