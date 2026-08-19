package com.example.debt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.debt.logic.DebtCalculator
import com.example.debt.ui.theme.*
import com.example.debt.ui.viewmodels.DebtDetailViewModel

@Composable
fun DebtDetailScreen(
    debtId: String,
    onBack: () -> Unit,
    viewModel: DebtDetailViewModel = viewModel { DebtDetailViewModel(debtId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPaymentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.debt?.customerName ?: "Debt Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = SurfaceColor,
                contentColor = TextPrimary
            )
        },
        backgroundColor = BgColor
    ) { padding ->
        val debt = uiState.debt
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentColor)
            }
        } else if (debt != null) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                // Info Section
                Card(backgroundColor = SurfaceColor2, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Customer Info", style = MaterialTheme.typography.h6, color = AccentColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ID: ${debt.customerID}", color = TextSecondary)
                        Text("Phone: ${debt.phoneNumber}", color = TextSecondary)
                        Text("Product: ${debt.product}", color = TextSecondary)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Balance Section
                val remaining = DebtCalculator.getDebtRemaining(debt)
                Card(backgroundColor = SurfaceColor2, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Amount", color = TextSecondary)
                            Text("KES ${debt.totalAmount.toInt()}", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Remaining", color = TextSecondary)
                            Text("KES ${remaining.toInt()}", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold, color = if (remaining > 0) OrangeColor else GreenColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Payment History", style = MaterialTheme.typography.h6, color = TextPrimary)
                    Button(onClick = { showPaymentDialog = true }, colors = ButtonDefaults.buttonColors(backgroundColor = GreenColor)) {
                        Text("Add Payment")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(debt.payments) { payment ->
                        Card(backgroundColor = SurfaceColor, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(payment.note ?: "Payment", fontWeight = FontWeight.Bold)
                                    Text(payment.date.toString(), style = MaterialTheme.typography.caption, color = TextSecondary)
                                }
                                Text("KES ${payment.amount.toInt()}", color = GreenColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPaymentDialog) {
        AddPaymentDialog(
            onDismiss = { showPaymentDialog = false },
            onConfirm = { amount, note ->
                viewModel.addPayment(amount, note)
                showPaymentDialog = false
            }
        )
    }
}

@Composable
fun AddPaymentDialog(onDismiss: () -> Unit, onConfirm: (Double, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment") },
        backgroundColor = SurfaceColor,
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (KES)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0, note) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
