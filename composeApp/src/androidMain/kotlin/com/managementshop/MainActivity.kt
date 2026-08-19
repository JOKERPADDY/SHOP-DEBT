package com.managementshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.debt.App
import com.example.debt.initializeFirebase
import com.example.debt.initializePlatformActions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase and Platform Actions
        initializeFirebase(this)
        initializePlatformActions(this)
        
        setContent {
            App()
        }
    }
}
