package com.example.debt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.debt.logic.DebtCalculator
import com.example.debt.logic.MpesaParser
import com.example.debt.ui.theme.*
import com.example.debt.ui.viewmodels.DebtDetailViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun DebtDetailScreen(
    debtId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: DebtDetailViewModel = viewModel { DebtDetailViewModel(debtId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.debt?.customerName ?: "Debt Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedColor)
                    }
                    IconButton(onClick = { onEdit(debtId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentColor)
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
                        Text("Due Date: ${debt.dueDate}", color = TextSecondary)
                        if (!debt.notes.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Notes:", color = AccentColor, fontWeight = FontWeight.Bold)
                            Text(debt.notes, color = TextSecondary)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Balance Section
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val remaining = DebtCalculator.getDebtRemaining(debt)
                val interest = DebtCalculator.calculateLatePaymentInterest(debt, today)
                val totalWithInterest = remaining + interest

                Card(backgroundColor = SurfaceColor2, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Principal Remaining", color = TextSecondary, fontSize = 12.sp)
                                Text("KES ${remaining.toInt()}", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                            }
                            if (interest > 0) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Late Interest (5%)", color = OrangeColor, fontSize = 12.sp)
                                    Text("KES ${interest.toInt()}", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold, color = OrangeColor)
                                }
                            }
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = SurfaceColor3)
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("TOTAL BALANCE", color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("KES ${totalWithInterest.toInt()}", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Black, color = if (totalWithInterest > 0) RedColor else GreenColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), 
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Payment History", style = MaterialTheme.typography.h6, color = TextPrimary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.sendInvoice() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Send Invoice")
                    }
                    Button(
                        onClick = { showPaymentDialog = true },
                        colors = ButtonDefaults.buttonColors(backgroundColor = GreenColor),
                        modifier = Modifier.weight(1f)
                    ) {
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

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Debt") },
            text = { Text("Are you sure you want to delete this debt? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDebt(onSuccess = onBack)
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = RedColor)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AddPaymentDialog(onDismiss: () -> Unit, onConfirm: (Double, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var mpesaInput by remember { mutableStateOf("") }
    var showMpesaInput by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment") },
        backgroundColor = SurfaceColor,
        text = {
            Column {
                if (!showMpesaInput) {
                    OutlinedButton(
                        onClick = { showMpesaInput = true },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenColor)
                    ) {
                        Text("Paste M-Pesa SMS (Magic Fill)")
                    }
                } else {
                    OutlinedTextField(
                        value = mpesaInput,
                        onValueChange = { 
                            mpesaInput = it
                            val parsed = MpesaParser.parseMpesaMessage(it)
                            if (parsed != null) {
                                amount = parsed.amount.toString()
                                if (parsed.sender.isNotEmpty()) {
                                    note = "M-Pesa from ${parsed.sender}"
                                }
                                showMpesaInput = false
                            }
                        },
                        label = { Text("Paste M-Pesa Message Here") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        placeholder = { Text("App will auto-extract amount...") }
                    )
                    TextButton(onClick = { showMpesaInput = false }) {
                        Text("Cancel Magic Fill", color = RedColor)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

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
