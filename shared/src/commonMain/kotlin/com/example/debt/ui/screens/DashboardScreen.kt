package com.example.debt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.debt.logic.DebtStatus
import com.example.debt.ui.components.DebtItem
import com.example.debt.ui.components.StatCard
import com.example.debt.ui.theme.*
import com.example.debt.ui.viewmodels.DashboardViewModel

@Composable
fun DashboardScreen(
    onAddDebt: () -> Unit,
    onDebtClick: (String) -> Unit,
    onManageSharing: () -> Unit,
    onViewAuditLog: () -> Unit,
    viewModel: DashboardViewModel = viewModel { DashboardViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        backgroundColor = BgColor,
        topBar = {
            TopAppBar(
                title = { Text(uiState.shopName.uppercase(), color = TextPrimary, fontWeight = FontWeight.Bold) },
                backgroundColor = SurfaceColor,
                actions = {
                    IconButton(onClick = { viewModel.exportPdf() }) {
                        Icon(Icons.Default.Share, contentDescription = "Export PDF", tint = AccentColor)
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Actions", tint = TextSecondary)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(SurfaceColor)
                        ) {
                            DropdownMenuItem(onClick = { 
                                showMenu = false
                                viewModel.exportCsv() 
                            }) {
                                Text("Export CSV", color = TextPrimary)
                            }
                            DropdownMenuItem(onClick = { 
                                showMenu = false
                                viewModel.importCsv() 
                            }) {
                                Text("Import CSV", color = TextPrimary)
                            }
                            DropdownMenuItem(onClick = { 
                                showMenu = false
                                onManageSharing()
                            }) {
                                Text("Shop Sharing", color = TextPrimary)
                            }
                            Divider(color = BorderColor)
                            DropdownMenuItem(onClick = { 
                                showMenu = false
                                onViewAuditLog()
                            }) {
                                Text("Audit Log", color = TextPrimary)
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddDebt,
                backgroundColor = AccentColor
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Debt", tint = BgColor)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    label = "Total Owed",
                    value = "KES ${uiState.totalOwed.toInt()}",
                    accentColor = RedColor,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Collected",
                    value = "KES ${uiState.totalCollected.toInt()}",
                    accentColor = GreenColor,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    label = "Pending",
                    value = uiState.pendingCount.toString(),
                    accentColor = BlueColor,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Overdue",
                    value = uiState.overdueCount.toString(),
                    accentColor = OrangeColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name, ID or product...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = AccentColor,
                    unfocusedBorderColor = BorderColor,
                    textColor = TextPrimary
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.activeFilter == null,
                    onClick = { viewModel.onFilterChange(null) },
                    label = "All"
                )
                FilterChip(
                    selected = uiState.activeFilter == DebtStatus.OVERDUE,
                    onClick = { viewModel.onFilterChange(DebtStatus.OVERDUE) },
                    label = "Overdue",
                    selectedColor = RedColor
                )
                FilterChip(
                    selected = uiState.activeFilter == DebtStatus.PENDING,
                    onClick = { viewModel.onFilterChange(DebtStatus.PENDING) },
                    label = "Pending",
                    selectedColor = OrangeColor
                )
                FilterChip(
                    selected = uiState.activeFilter == DebtStatus.PAID,
                    onClick = { viewModel.onFilterChange(DebtStatus.PAID) },
                    label = "Paid",
                    selectedColor = GreenColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Recent Debts",
                style = MaterialTheme.typography.h6,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filteredDebts) { debt ->
                        DebtItem(
                            debt = debt,
                            onClick = { onDebtClick(debt.id) },
                            onSendInvoice = { viewModel.sendInvoice(debt) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    selectedColor: Color = AccentColor
) {
    Surface(
        color = if (selected) selectedColor else SurfaceColor2,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.clickable(onClick = onClick),
        elevation = if (selected) 4.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            style = MaterialTheme.typography.body2,
            color = if (selected) BgColor else TextSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
