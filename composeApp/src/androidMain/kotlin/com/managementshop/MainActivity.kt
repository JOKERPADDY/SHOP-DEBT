package com.managementshop

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.debt.App
import com.example.debt.handleAndroidCsvResult
import com.example.debt.initializeFirebase
import com.example.debt.initializePlatformActions
import com.example.debt.utils.initializeSettings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase and Platform Actions
        initializeFirebase(this)
        initializeSettings(this)
        initializePlatformActions(this)
        
        setContent {
            App()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                handleAndroidCsvResult(uri)
            }
        }
    }
}
