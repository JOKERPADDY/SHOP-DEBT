package com.example.debt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debt.repository.LinkingService
import com.example.debt.ui.theme.*
import com.example.debt.utils.SettingsKeys
import com.example.debt.utils.getLocalSettings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun ShopSettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val user = Firebase.auth.currentUser
    var linkCode by remember { mutableStateOf<String?>(null) }
    var isLoadingCode by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val settings = getLocalSettings()
    var shopName by remember { mutableStateOf(settings.getString(SettingsKeys.SHOP_NAME) ?: "Thawne Shop") }
    
    val linkedDevices by LinkingService.getLinkedDevices().collectAsState(initial = emptyList())
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings \u0026 Sharing") },
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Shop Details",
                style = MaterialTheme.typography.h6,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = shopName,
                onValueChange = { 
                    shopName = it
                    settings.saveString(SettingsKeys.SHOP_NAME, it)
                },
                label = { Text("Shop Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = TextPrimary,
                    cursorColor = AccentColor,
                    focusedBorderColor = AccentColor
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            Card(backgroundColor = SurfaceColor2, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Owner Account", color = AccentColor, fontWeight = FontWeight.Bold)
                    Text(user?.email ?: "No email", style = MaterialTheme.typography.body1, color = TextPrimary)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Link a New Device",
                style = MaterialTheme.typography.h6,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Generate a code for your staff or partner to access your shop's data from their phone.",
                style = MaterialTheme.typography.body2,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (linkCode != null) {
                Card(
                    backgroundColor = SurfaceColor3,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("YOUR LINK CODE", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            linkCode!!,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            color = AccentColor
                        )
                    }
                }
            }
            
            if (errorMessage != null) {
                Text(
                    errorMessage!!,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        isLoadingCode = true
                        errorMessage = null
                        try {
                            linkCode = LinkingService.generateLinkCode()
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.message ?: "Failed to generate code"}"
                        } finally {
                            isLoadingCode = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoadingCode
            ) {
                if (isLoadingCode) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BgColor)
                else Text("Generate New Code")
            }

            if (linkedDevices.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Connected Devices (${linkedDevices.size})",
                    style = MaterialTheme.typography.h6,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                linkedDevices.forEach { device ->
                    Card(
                        backgroundColor = SurfaceColor2,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(device.deviceName, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text("Device ID: ${device.deviceId}", color = TextSecondary, fontSize = 11.sp)
                            }
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        LinkingService.revokeDevice(device.deviceId)
                                    }
                                }
                            ) {
                                Text("Revoke", color = RedColor)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        LinkingService.unlink()
                        Firebase.auth.signOut()
                        onLogout()
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = RedColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout & Unlink", color = TextPrimary)
            }
        }
    }
}
