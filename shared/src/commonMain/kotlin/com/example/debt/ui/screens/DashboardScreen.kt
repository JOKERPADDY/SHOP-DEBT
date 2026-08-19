package com.example.debt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.debt.ui.components.DebtItem
import com.example.debt.ui.components.StatCard
import com.example.debt.ui.theme.*
import com.example.debt.ui.viewmodels.DashboardViewModel

@Composable
fun DashboardScreen(
    onAddDebt: () -> Unit,
    onDebtClick: (String) -> Unit,
    viewModel: DashboardViewModel = viewModel { DashboardViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        backgroundColor = BgColor,
        topBar = {
            TopAppBar(
                title = { Text("Patrick's Debt Manager", color = TextPrimary) },
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
                            Divider(color = BorderColor)
                            DropdownMenuItem(onClick = { 
                                showMenu = false
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
                    items(uiState.debts) { debt ->
                        DebtItem(
                            debt = debt,
                            onClick = { onDebtClick(debt.id) }
                        )
                    }
                }
            }
        }
    }
}
