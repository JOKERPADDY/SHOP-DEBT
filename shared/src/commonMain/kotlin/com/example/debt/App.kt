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

@Composable
fun App() {
    DebtTheme {
        var user by remember { mutableStateOf(Firebase.auth.currentUser) }
        
        LaunchedEffect(Unit) {
            Firebase.auth.authStateChanged.collect {
                user = it
            }
        }

        if (user == null) {
            LoginScreen(onLoginSuccess = { user = Firebase.auth.currentUser })
        } else {
            NavigationHost()
        }
    }
}

@Composable
fun NavigationHost() {
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
                        onDebtClick = { selectedDebtId = it }
                    )
                }
                
                if (selectedDebtId != null) {
                    Box(modifier = Modifier.width(400.dp).fillMaxHeight()) {
                        DebtDetailScreen(
                            debtId = selectedDebtId!!,
                            onBack = { selectedDebtId = null }
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
            }
        } else {
            // Standard mobile navigation
            NavHost(navController = navController, startDestination = "dashboard") {
                composable("dashboard") {
                    DashboardScreen(
                        onAddDebt = { navController.navigate("addDebt") },
                        onDebtClick = { navController.navigate("detail/$it") }
                    )
                }
                composable("addDebt") {
                    AddDebtScreen(onBack = { navController.popBackStack() })
                }
                composable(
                    "detail/{debtId}",
                    arguments = listOf(navArgument("debtId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val debtId = backStackEntry.arguments?.getString("debtId") ?: return@composable
                    DebtDetailScreen(
                        debtId = debtId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
