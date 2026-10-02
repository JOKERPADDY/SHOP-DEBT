package com.example.debt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.debt.logic.DebtCalculator
import com.example.debt.logic.DebtStatus
import com.example.debt.logic.InvoiceHelper
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
    val filteredDebts: List<Debt> = emptyList(),
    val isLoading: Boolean = true,
    val totalOwed: Double = 0.0,
    val totalCollected: Double = 0.0,
    val pendingCount: Int = 0,
    val overdueCount: Int = 0,
    val searchQuery: String = "",
    val activeFilter: DebtStatus? = null,
    val shopName: String = "Thawne Shop"
)

class DashboardViewModel(
    private val repository: DebtRepository = DebtRepository()
) : ViewModel() {
    private val platformActions = com.example.debt.getPlatformActions()
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var allDebts: List<Debt> = emptyList()

    init {
        loadDebts()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun onFilterChange(status: DebtStatus?) {
        _uiState.value = _uiState.value.copy(activeFilter = status)
        applyFilters()
    }

    private fun applyFilters() {
        val query = _uiState.value.searchQuery.lowercase()
        val filter = _uiState.value.activeFilter
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        val filtered = allDebts.filter { debt ->
            val matchesQuery = debt.customerName.lowercase().contains(query) || 
                             debt.customerID.contains(query) ||
                             debt.product.lowercase().contains(query)
            
            val matchesStatus = if (filter == null) true 
                               else DebtCalculator.getDebtStatus(debt, today) == filter
            
            matchesQuery && matchesStatus
        }
        
        _uiState.value = _uiState.value.copy(filteredDebts = filtered)
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
                try {
                    importedDebts.forEach { repository.saveDebt(it) }
                    repository.logAction("IMPORT_CSV", "Imported ${importedDebts.size} debts via CSV")
                } catch (e: Exception) {
                    println("Error importing CSV: ${e.message}")
                }
            }
        }
    }

    fun sendInvoice(debt: Debt) {
        val customerDebts = _uiState.value.debts.filter { it.customerID == debt.customerID }
        val shopName = repository.getShopName()
        val message = InvoiceHelper.formatInvoice(customerDebts, shopName)
        platformActions.sendWhatsAppReminder(debt, message)
    }

    private fun loadDebts() {
        viewModelScope.launch {
            try {
                repository.getDebts().collectLatest { debts ->
                    allDebts = debts
                    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    
                    var totalOwed = 0.0
                    var totalCollected = 0.0
                    var pendingCount = 0
                    var overdueCount = 0

                    debts.forEach { debt ->
                        val remaining = DebtCalculator.getDebtRemaining(debt)
                        val remainingWithInterest = DebtCalculator.getDebtTotalWithInterest(debt, today)
                        val paid = DebtCalculator.getDebtPaid(debt)
                        
                        totalOwed += remainingWithInterest
                        totalCollected += paid
                        
                        if (remaining > 0) {
                            pendingCount++
                            if (DebtCalculator.isDebtOverdue(debt, today)) {
                                overdueCount++
                            }
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        debts = debts,
                        isLoading = false,
                        totalOwed = totalOwed,
                        totalCollected = totalCollected,
                        pendingCount = pendingCount,
                        overdueCount = overdueCount,
                        shopName = repository.getShopName()
                    )
                    applyFilters()
                }
            } catch (e: Exception) {
                println("Error collecting debts in DashboardViewModel: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
