package com.example.debt

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize

actual fun initializeFirebase(context: Any?) {
    try {
        Firebase.initialize(
            options = FirebaseOptions(
                apiKey = "AIzaSyCDo0aQ8W9QlqAyJbYV5Zve07S2FThduhs",
                applicationId = "1:703586336086:android:10a3e8d7732eafe1acaaf7",
                projectId = "debt-management-533b3",
                storageBucket = "debt-management-533b3.firebasestorage.app"
            )
        )
    } catch (e: Exception) {
        // Already initialized or failed
        println("Firebase Desktop Init: ${e.message}")
    }
}
