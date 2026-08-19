package com.example.debt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.debt.logic.DebtCalculator
import com.example.debt.models.Debt
import com.example.debt.repository.DebtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class DashboardUiState(
    val debts: List<Debt> = emptyList(),
    val isLoading: Boolean = true,
    val totalOwed: Double = 0.0,
    val totalCollected: Double = 0.0,
    val pendingCount: Int = 0,
    val overdueCount: Int = 0
)

class DashboardViewModel(
    private val repository: DebtRepository = DebtRepository()
) : ViewModel() {
    private val platformActions = com.example.debt.getPlatformActions()
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDebts()
    }

    fun exportPdf() {
        platformActions.exportToPdf(_uiState.value.debts)
    }

    fun exportCsv() {
        platformActions.exportToCsv(_uiState.value.debts)
    }

    fun importCsv() {
        platformActions.importFromCsv { importedDebts ->
            viewModelScope.launch {
                importedDebts.forEach { repository.saveDebt(it) }
            }
        }
    }

    private fun loadDebts() {
        viewModelScope.launch {
            repository.getDebts().collectLatest { debts ->
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                
                var totalOwed = 0.0
                var totalCollected = 0.0
                var pendingCount = 0
                var overdueCount = 0

                debts.forEach { debt ->
                    val remaining = DebtCalculator.getDebtRemaining(debt)
                    val paid = DebtCalculator.getDebtPaid(debt)
                    
                    totalOwed += remaining
                    totalCollected += paid
                    
                    if (remaining > 0) {
                        pendingCount++
                        if (DebtCalculator.isDebtOverdue(debt, today)) {
                            overdueCount++
                        }
                    }
                }

                _uiState.value = DashboardUiState(
                    debts = debts,
                    isLoading = false,
                    totalOwed = totalOwed,
                    totalCollected = totalCollected,
                    pendingCount = pendingCount,
                    overdueCount = overdueCount
                )
            }
        }
    }
}
