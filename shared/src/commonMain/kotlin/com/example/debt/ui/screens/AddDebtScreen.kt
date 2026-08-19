package com.example.debt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.debt.ui.theme.*
import com.example.debt.ui.viewmodels.AddDebtViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun AddDebtScreen(
    onBack: () -> Unit,
    viewModel: AddDebtViewModel = viewModel { AddDebtViewModel() }
) {
    var name by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var product by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Debt") },
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
            
            Button(
                onClick = {
                    viewModel.saveDebt(
                        name = name,
                        idNumber = idNumber,
                        phone = phone,
                        product = product,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        dateTaken = today,
                        dueDate = today // Simplification: should add a date picker
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor)
            ) {
                Text("Save Debt", color = BgColor)
            }
        }
    }
}
