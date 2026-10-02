package com.example.debt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.debt.getPlatformActions
import com.example.debt.logic.DebtCalculator
import com.example.debt.logic.InvoiceHelper
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
    val customerDebts: List<Debt> = emptyList(),
    val isLoading: Boolean = true
)

class DebtDetailViewModel(
    private val debtId: String,
    private val repository: DebtRepository = DebtRepository()
) : ViewModel() {
    private val platformActions = getPlatformActions()
    private val _uiState = MutableStateFlow(DebtDetailUiState())
    val uiState: StateFlow<DebtDetailUiState> = _uiState.asStateFlow()

    init {
        loadDebt()
    }

    private fun loadDebt() {
        viewModelScope.launch {
            try {
                repository.getDebts().collectLatest { debts ->
                    val debt = debts.find { it.id == debtId }
                    val customerDebts = if (debt != null) {
                        debts.filter { it.customerID == debt.customerID }
                    } else emptyList()
                    
                    _uiState.value = DebtDetailUiState(
                        debt = debt, 
                        customerDebts = customerDebts,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                println("Error collecting debts in DebtDetailViewModel: ${e.message}")
                _uiState.value = DebtDetailUiState(debt = null, isLoading = false)
            }
        }
    }

    fun addPayment(amount: Double, note: String) {
        val currentDebt = uiState.value.debt ?: return
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val newPayment = Payment(amount = amount, date = today, note = note)
        
        viewModelScope.launch {
            try {
                val updatedDebt = currentDebt.copy(
                    payments = currentDebt.payments + newPayment
                )
                repository.saveDebt(updatedDebt, isUpdate = true)
                repository.logAction("ADD_PAYMENT", "Recorded payment of KES $amount for ${currentDebt.customerName}")
            } catch (e: Exception) {
                println("Error adding payment in DebtDetailViewModel: ${e.message}")
            }
        }
    }

    fun sendInvoice() {
        val debt = uiState.value.debt ?: return
        val customerDebts = uiState.value.customerDebts
        val shopName = repository.getShopName()
        val message = InvoiceHelper.formatInvoice(customerDebts, shopName)
        platformActions.sendWhatsAppReminder(debt, message)
    }

    fun deleteDebt(onSuccess: () -> Unit) {
        val debt = uiState.value.debt ?: return
        viewModelScope.launch {
            try {
                repository.deleteDebt(debt)
                onSuccess()
            } catch (e: Exception) {
                println("Error deleting debt in DebtDetailViewModel: ${e.message}")
            }
        }
    }
}
