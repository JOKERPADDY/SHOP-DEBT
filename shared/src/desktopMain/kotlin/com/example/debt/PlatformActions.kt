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
        val file = selectFile("Export CSV", FileDialog.SAVE, "debts.csv") ?: return
        
        try {
            file.bufferedWriter().use { out ->
                out.write("Customer Name,National ID,Phone Number,Product,Total Amount,Paid Amount,Date Taken,Due Date\n")
                debts.forEach { d ->
                    out.write("${d.customerName},${d.customerID},${d.phoneNumber},${d.product},${d.totalAmount},${DebtCalculator.getDebtPaid(d)},${d.dateTaken},${d.dueDate}\n")
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
            file.bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val parts = line.split(",")
                    if (parts.size >= 8) {
                        // Very basic parsing, would need more robust handling for production
                        // debt = Debt(...)
                    }
                }
            }
            onImported(imported)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
