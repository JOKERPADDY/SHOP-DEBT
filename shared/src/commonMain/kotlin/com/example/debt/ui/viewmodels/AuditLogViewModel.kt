package com.example.debt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.debt.models.LogEntry
import com.example.debt.repository.DebtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AuditLogUiState(
    val logs: List<LogEntry> = emptyList(),
    val isLoading: Boolean = true
)

class AuditLogViewModel(
    private val repository: DebtRepository = DebtRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuditLogUiState())
    val uiState: StateFlow<AuditLogUiState> = _uiState.asStateFlow()

    init {
        loadLogs()
    }

    private fun loadLogs() {
        viewModelScope.launch {
            try {
                repository.getAuditLogs().collectLatest { logs ->
                    _uiState.value = AuditLogUiState(logs = logs, isLoading = false)
                }
            } catch (e: Exception) {
                println("Error collecting audit logs: ${e.message}")
                _uiState.value = AuditLogUiState(logs = emptyList(), isLoading = false)
            }
        }
    }
}
