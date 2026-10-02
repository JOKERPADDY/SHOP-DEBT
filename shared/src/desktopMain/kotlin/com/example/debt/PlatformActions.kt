package com.example.debt

import com.example.debt.models.Debt
import com.example.debt.logic.DebtCalculator
import com.lowagie.text.Document
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.net.URLEncoder
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DesktopPlatformActions : PlatformActions {
    override fun sendWhatsAppReminder(debt: Debt, message: String) {
        val phoneNumber = debt.phoneNumber.replace("\\D".toRegex(), "")
        val encodedMessage = URLEncoder.encode(message, "UTF-8")
        val uri = URI("https://web.whatsapp.com/send?phone=$phoneNumber&text=$encodedMessage")
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(uri)
        }
    }

    override fun exportToPdf(debts: List<Debt>) {
        val file = selectFile("Save PDF Report", FileDialog.SAVE, "debt_report.pdf") ?: return
        
        try {
            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()
            
            document.add(Paragraph("Patrick's Debt Manager - Report"))
            document.add(Paragraph("Generated: ${Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())}"))
            document.add(Paragraph(" "))
            
            val table = PdfPTable(6)
            table.addCell("Customer")
            table.addCell("Product")
            table.addCell("Total")
            table.addCell("Paid")
            table.addCell("Remaining")
            table.addCell("Due Date")
            
            debts.forEach { debt ->
                table.addCell(debt.customerName)
                table.addCell(debt.product)
                table.addCell(debt.totalAmount.toString())
                table.addCell(DebtCalculator.getDebtPaid(debt).toString())
                table.addCell(DebtCalculator.getDebtRemaining(debt).toString())
                table.addCell(debt.dueDate.toString())
            }
            
            document.add(table)
            document.close()
            
            Desktop.getDesktop().open(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun exportToCsv(debts: List<Debt>) {
        val file = selectFile("Export CSV", FileDialog.SAVE, "debts_export.csv") ?: return
        
        try {
            file.bufferedWriter().use { out ->
                // Header
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
            Desktop.getDesktop().open(file.parentFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun importFromCsv(onImported: (List<Debt>) -> Unit) {
        val file = selectFile("Import CSV", FileDialog.LOAD, "*.csv") ?: return
        
        try {
            val imported = mutableListOf<Debt>()
            file.bufferedReader().use { reader ->
                val header = reader.readLine() // Skip header
                var line: String? = reader.readLine()
                while (line != null) {
                    val parts = parseCsvLine(line)
                    if (parts.size >= 7) {
                        try {
                            val debt = Debt(
                                id = Clock.System.now().toEpochMilliseconds().toString() + (0..1000).random().toString(),
                                customerName = parts[0],
                                customerID = parts[1],
                                phoneNumber = parts[2],
                                product = parts[3],
                                totalAmount = parts[4].toDoubleOrNull() ?: 0.0,
                                dateTaken = kotlinx.datetime.LocalDate.parse(parts[5]),
                                dueDate = kotlinx.datetime.LocalDate.parse(parts[6]),
                                notes = parts.getOrNull(7) ?: "",
                                createdAt = parts.getOrNull(8)?.let { kotlinx.datetime.Instant.parse(it) } ?: Clock.System.now()
                            )
                            imported.add(debt)
                        } catch (e: Exception) {
                            println("Error parsing line: $line - ${e.message}")
                        }
                    }
                    line = reader.readLine()
                }
            }
            if (imported.isNotEmpty()) {
                onImported(imported)
            }
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

    private fun selectFile(title: String, mode: Int, defaultFile: String): File? {
        val dialog = FileDialog(null as Frame?, title, mode)
        dialog.file = defaultFile
        dialog.isVisible = true
        return if (dialog.file != null) File(dialog.directory, dialog.file) else null
    }
}

actual fun getPlatformActions(): PlatformActions {
    return DesktopPlatformActions()
}
