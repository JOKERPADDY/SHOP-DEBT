package com.example.debt

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    // Initialize Firebase for Desktop
    initializeFirebase()

    Window(onCloseRequest = ::exitApplication, title = "Patrick's Debt Manager") {
        App()
    }
}
