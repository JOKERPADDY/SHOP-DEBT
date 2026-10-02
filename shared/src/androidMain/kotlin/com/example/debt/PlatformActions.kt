package com.example.debt

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.debt.models.Debt
import com.example.debt.logic.DebtCalculator
import com.example.debt.repository.DebtRepository
import java.io.File
import java.io.FileOutputStream

class AndroidPlatformActions(private val context: Context) : PlatformActions {
    private val repository = DebtRepository()
    private var onImportedCallback: ((List<Debt>) -> Unit)? = null

    fun handleImportResult(uri: Uri) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val imported = mutableListOf<Debt>()
            inputStream?.bufferedReader()?.use { reader ->
                val header = reader.readLine()
                var line: String? = reader.readLine()
                while (line != null) {
                    val parts = parseCsvLine(line)
                    if (parts.size >= 7) {
                        try {
                            val debt = Debt(
                                id = System.currentTimeMillis().toString() + (0..1000).random().toString(),
                                customerName = parts[0],
                                customerID = parts[1],
                                phoneNumber = parts[2],
                                product = parts[3],
                                totalAmount = parts[4].toDoubleOrNull() ?: 0.0,
                                dateTaken = kotlinx.datetime.LocalDate.parse(parts[5]),
                                dueDate = kotlinx.datetime.LocalDate.parse(parts[6]),
                                notes = parts.getOrNull(7) ?: "",
                                createdAt = parts.getOrNull(8)?.let { kotlinx.datetime.Instant.parse(it) } ?: kotlinx.datetime.Clock.System.now()
                            )
                            imported.add(debt)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    line = reader.readLine()
                }
            }
            if (imported.isNotEmpty()) {
                onImportedCallback?.invoke(imported)
            }
            onImportedCallback = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    current.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString())
                current = StringBuilder()
            } else {
                current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }

    override fun sendWhatsAppReminder(debt: Debt, message: String) {
        var formattedNumber = debt.phoneNumber.replace("\\D".toRegex(), "")
        if (formattedNumber.startsWith("0")) {
            formattedNumber = "254" + formattedNumber.substring(1)
        }
        val url = "https://wa.me/$formattedNumber?text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override fun exportToPdf(debts: List<Debt>) {
        val fileName = "debt_report_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        val shopName = repository.getShopName().uppercase()
        
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                typeface = Typeface.DEFAULT
            }
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            var y = 50f
            canvas.drawText(shopName, 40f, y, titlePaint)
            y += 30f
            canvas.drawText("DEBT REPORT - ${java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date())}", 40f, y, textPaint)
            y += 40f

            // Table Header
            val headerPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("Customer", 40f, y, headerPaint)
            canvas.drawText("Product", 200f, y, headerPaint)
            canvas.drawText("Balance", 400f, y, headerPaint)
            canvas.drawText("Due Date", 500f, y, headerPaint)
            
            y += 10f
            canvas.drawLine(40f, y, 555f, y, Paint().apply { color = Color.LTGRAY })
            y += 20f

            // Rows
            debts.forEach { debt ->
                if (y > 800) { // Simple page overflow handling (stop drawing)
                    return@forEach 
                }
                canvas.drawText(debt.customerName.take(20), 40f, y, textPaint)
                canvas.drawText(debt.product.take(20), 200f, y, textPaint)
                canvas.drawText("KES ${DebtCalculator.getDebtRemaining(debt).toInt()}", 400f, y, textPaint)
                canvas.drawText(debt.dueDate.toString(), 500f, y, textPaint)
                y += 25f
            }

            pdfDocument.finishPage(page)
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            
            shareFile(file, "application/pdf")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareFile(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "com.managementshop.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    override fun exportToCsv(debts: List<Debt>) {
        val fileName = "debts_export_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)
        
        try {
            file.bufferedWriter().use { out ->
                out.write("Name,ID Number,Phone,Product,Amount,Date Taken,Due Date,Notes,Created At\n")
                debts.forEach { d ->
                    val row = listOf(
                        d.customerName,
                        d.customerID,
                        d.phoneNumber,
                        d.product,
                        d.totalAmount.toString(),
                        d.dateTaken.toString(),
                        d.dueDate.toString(),
                        d.notes ?: "",
                        d.createdAt.toString()
                    ).joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" }
                    out.write("$row\n")
                }
            }
            shareFile(file, "text/csv")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun importFromCsv(onImported: (List<Debt>) -> Unit) {
        onImportedCallback = onImported
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "text/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        // This is a bit of a hack because we don't have the Activity context directly here 
        // that can receive the result. We'll need MainActivity to handle it.
        // For now, we launch it, but we need MainActivity to catch the result.
        if (context is android.app.Activity) {
            context.startActivityForResult(intent, 1001)
        } else {
            // If it's application context, we can't easily get result.
            // In this project, it's initialized with 'this' in MainActivity, so it should be an Activity.
            (context as? android.app.Activity)?.startActivityForResult(intent, 1001) ?: run {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }
}

private var androidActions: PlatformActions? = null

fun initializePlatformActions(context: Context) {
    androidActions = AndroidPlatformActions(context)
}

actual fun getPlatformActions(): PlatformActions {
    return androidActions ?: throw IllegalStateException("PlatformActions not initialized")
}

fun handleAndroidCsvResult(uri: Uri) {
    (androidActions as? AndroidPlatformActions)?.handleImportResult(uri)
}
