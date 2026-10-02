package com.example.debt.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.debt.ui.theme.*
import com.example.debt.ui.viewmodels.AddDebtViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun AddDebtScreen(
    debtId: String? = null,
    onBack: () -> Unit,
    viewModel: AddDebtViewModel = viewModel { AddDebtViewModel(debtId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var product by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Update local state when ViewModel state changes (e.g. after loading existing debt)
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && uiState.isEditMode) {
            name = uiState.name
            idNumber = uiState.idNumber
            phone = uiState.phone
            product = uiState.product
            amount = uiState.amount
        }
    }
    
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Debt" else "Add New Debt") },
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
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = idNumber,
                    onValueChange = { idNumber = it },
                    label = { Text("National ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = product,
                    onValueChange = { product = it },
                    label = { Text("Product / Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Total Amount (KES)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Due Date Selection
                OutlinedTextField(
                    value = uiState.dueDateString,
                    onValueChange = { viewModel.onDueDateStringChange(it) },
                    label = { Text("Due Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Pick Date", tint = AccentColor)
                        }
                    }
                )

                // Notes Area
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.onNotesChange(it) },
                    label = { Text("Notes / Additional Info") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("Enter any extra details here...") }
                )
                
                Button(
                    onClick = {
                        viewModel.saveDebt(
                            name = name,
                            idNumber = idNumber,
                            phone = phone,
                            product = product,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            dateTaken = today
                        )
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor)
                ) {
                    Text(if (uiState.isEditMode) "Update Debt" else "Save Debt", color = BgColor)
                }
            }
        }
    }

    if (showDatePicker) {
        SimpleDatePickerDialog(
            initialDate = uiState.dueDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { 
                viewModel.onDueDateChange(it)
                showDatePicker = false
            }
        )
    }
}

@Composable
fun SimpleDatePickerDialog(
    initialDate: kotlinx.datetime.LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (kotlinx.datetime.LocalDate) -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialDate) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Due Date") },
        backgroundColor = SurfaceColor,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = selectedDate.toString(),
                    style = MaterialTheme.typography.h4,
                    color = AccentColor,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DateAdjustmentButton("-1 Day") {
                        val days = selectedDate.toEpochDays()
                        selectedDate = kotlinx.datetime.LocalDate.fromEpochDays(days - 1)
                    }
                    DateAdjustmentButton("+1 Day") {
                        val days = selectedDate.toEpochDays()
                        selectedDate = kotlinx.datetime.LocalDate.fromEpochDays(days + 1)
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DateAdjustmentButton("-1 Week") {
                        val days = selectedDate.toEpochDays()
                        selectedDate = kotlinx.datetime.LocalDate.fromEpochDays(days - 7)
                    }
                    DateAdjustmentButton("+1 Week") {
                        val days = selectedDate.toEpochDays()
                        selectedDate = kotlinx.datetime.LocalDate.fromEpochDays(days + 7)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DateAdjustmentButton("-1 Month") {
                        // Rough approximation for simplicity in custom dialog
                        val days = selectedDate.toEpochDays()
                        selectedDate = kotlinx.datetime.LocalDate.fromEpochDays(days - 30)
                    }
                    DateAdjustmentButton("+1 Month") {
                        val days = selectedDate.toEpochDays()
                        selectedDate = kotlinx.datetime.LocalDate.fromEpochDays(days + 30)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedDate) }) {
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

@Composable
fun DateAdjustmentButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
    ) {
        Text(label)
    }
}
