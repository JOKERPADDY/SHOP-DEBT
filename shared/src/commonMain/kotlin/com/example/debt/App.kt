package com.example.debt

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.debt.ui.screens.*
import com.example.debt.ui.theme.DebtTheme
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.example.debt.repository.LinkingService

@Composable
fun App() {
    DebtTheme {
        var user by remember { mutableStateOf(Firebase.auth.currentUser) }
        var linkedShopId by remember { mutableStateOf(LinkingService.getLinkedShopId()) }
        var authMode by remember { mutableStateOf("login") }
        val scope = rememberCoroutineScope()
        
        LaunchedEffect(Unit) {
            try {
                Firebase.auth.authStateChanged.catch { e ->
                    println("Error in authStateChanged: ${e.message}")
                }.collect {
                    user = it
                }
            } catch (e: Exception) {
                println("Error starting authStateChanged listener: ${e.message}")
            }
        }

        if (user == null && linkedShopId == null) {
            when (authMode) {
                "signup" -> SignUpScreen(
                    onSignUpSuccess = { 
                        user = Firebase.auth.currentUser
                        authMode = "login" 
                    },
                    onBackToLogin = { authMode = "login" }
                )
                "link" -> LinkDeviceScreen(
                    onLinkSuccess = { 
                        linkedShopId = LinkingService.getLinkedShopId()
                    },
                    onBack = { authMode = "login" }
                )
                else -> LoginScreen(
                    onLoginSuccess = { user = Firebase.auth.currentUser },
                    onSignUpClick = { authMode = "signup" },
                    onLinkDeviceClick = { authMode = "link" }
                )
            }
        } else {
            NavigationHost(onLogout = {
                scope.launch {
                    LinkingService.unlink()
                    Firebase.auth.signOut()
                    user = null
                    linkedShopId = null
                    authMode = "login"
                }
            })
        }
    }
}

@Composable
fun NavigationHost(onLogout: () -> Unit) {
    val navController = rememberNavController()
    
    BoxWithConstraints {
        val isDesktop = maxWidth > 800.dp
        
        if (isDesktop) {
            // Two-pane layout for desktop
            var selectedDebtId by remember { mutableStateOf<String?>(null) }
            
            Row {
                Box(modifier = Modifier.weight(1f)) {
                    DashboardScreen(
                        onAddDebt = { navController.navigate("addDebt") },
                        onDebtClick = { selectedDebtId = it },
                        onManageSharing = { navController.navigate("sharing") },
                        onViewAuditLog = { navController.navigate("auditLog") }
                    )
                }
                
                if (selectedDebtId != null) {
                    Box(modifier = Modifier.width(400.dp).fillMaxHeight()) {
                        DebtDetailScreen(
                            debtId = selectedDebtId!!,
                            onBack = { selectedDebtId = null },
                            onEdit = { id -> 
                                selectedDebtId = null
                                navController.navigate("editDebt/$id") 
                            }
                        )
                    }
                }
            }
            
            // Still need NavHost for screens that don't fit the two-pane (like Add Debt)
            NavHost(navController = navController, startDestination = "empty") {
                composable("empty") { /* Dashboard is already showing */ }
                composable("addDebt") {
                    AddDebtScreen(onBack = { navController.popBackStack() })
                }
                composable("sharing") {
                    ShopSettingsScreen(
                        onBack = { navController.popBackStack() },
                        onLogout = onLogout
                    )
                }
                composable("auditLog") {
                    AuditLogScreen(onBack = { navController.popBackStack() })
                }
                composable(
                    "editDebt/{debtId}",
                    arguments = listOf(navArgument("debtId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("debtId")
                    AddDebtScreen(debtId = id, onBack = { navController.popBackStack() })
                }
            }
        } else {
            // Standard mobile navigation
            NavHost(navController = navController, startDestination = "dashboard") {
                composable("dashboard") {
                    DashboardScreen(
                        onAddDebt = { navController.navigate("addDebt") },
                        onDebtClick = { navController.navigate("detail/$it") },
                        onManageSharing = { navController.navigate("sharing") },
                        onViewAuditLog = { navController.navigate("auditLog") }
                    )
                }
                composable("addDebt") {
                    AddDebtScreen(onBack = { navController.popBackStack() })
                }
                composable("sharing") {
                    ShopSettingsScreen(
                        onBack = { navController.popBackStack() },
                        onLogout = onLogout
                    )
                }
                composable("auditLog") {
                    AuditLogScreen(onBack = { navController.popBackStack() })
                }
                composable(
                    "editDebt/{debtId}",
                    arguments = listOf(navArgument("debtId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("debtId")
                    AddDebtScreen(debtId = id, onBack = { navController.popBackStack() })
                }
                composable(
                    "detail/{debtId}",
                    arguments = listOf(navArgument("debtId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val debtId = backStackEntry.arguments?.getString("debtId") ?: return@composable
                    DebtDetailScreen(
                        debtId = debtId,
                        onBack = { navController.popBackStack() },
                        onEdit = { id -> navController.navigate("editDebt/$id") }
                    )
                }
            }
        }
    }
}
