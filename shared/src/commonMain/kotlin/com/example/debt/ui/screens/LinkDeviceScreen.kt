package com.example.debt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.debt.repository.LinkingService
import com.example.debt.ui.theme.BgColor
import com.example.debt.ui.theme.SurfaceColor
import com.example.debt.ui.theme.TextPrimary
import kotlinx.coroutines.launch

@Composable
fun LinkDeviceScreen(
    onLinkSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var deviceName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Link to a Shop") },
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Enter the 6-digit Link Code provided by the shop owner.",
                textAlign = TextAlign.Center,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6) code = it },
                label = { Text("6-Digit Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = deviceName,
                onValueChange = { deviceName = it },
                label = { Text("Device Name (e.g., Counter Phone)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            error?.let {
                Text(it, color = MaterialTheme.colors.error, modifier = Modifier.padding(top = 8.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (code.length != 6) {
                        error = "Please enter all 6 digits"
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        error = null
                        try {
                            val success = LinkingService.linkDevice(code, deviceName)
                            if (success) {
                                onLinkSuccess()
                            } else {
                                error = "Invalid or expired code"
                            }
                        } catch (e: Exception) {
                            error = "Network or linking error: ${e.message ?: "Failed to link"}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BgColor)
                else Text("Link Device")
            }
        }
    }
}
