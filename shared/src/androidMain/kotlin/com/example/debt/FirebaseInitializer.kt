package com.example.debt

import android.content.Context
import com.google.firebase.FirebaseApp

actual fun initializeFirebase(context: Any?) {
    val androidContext = context as? Context ?: throw IllegalArgumentException("Android context required")
    FirebaseApp.initializeApp(androidContext)
}
