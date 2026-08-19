package com.example.debt

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.debt.models.Debt

class AndroidPlatformActions(private val context: Context) : PlatformActions {
    override fun sendWhatsAppReminder(debt: Debt, message: String) {
        val phoneNumber = debt.phoneNumber.replace("\\D".toRegex(), "")
        val url = "https://wa.me/$phoneNumber?text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override fun exportToPdf(debts: List<Debt>) {
        // Android specific PDF export logic
    }

    override fun exportToCsv(debts: List<Debt>) {
        // Android specific CSV export
    }

    override fun importFromCsv(onImported: (List<Debt>) -> Unit) {
        // Android specific CSV import
    }
}

private var androidActions: PlatformActions? = null

fun initializePlatformActions(context: Context) {
    androidActions = AndroidPlatformActions(context)
}

actual fun getPlatformActions(): PlatformActions {
    return androidActions ?: throw IllegalStateException("PlatformActions not initialized")
}
