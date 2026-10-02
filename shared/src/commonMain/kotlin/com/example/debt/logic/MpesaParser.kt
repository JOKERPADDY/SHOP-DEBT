package com.example.debt.logic

object MpesaParser {
    /**
     * Parses a standard M-Pesa SMS to extract amount and sender.
     * Example: "SK123456 Confirmed. Ksh500.00 paid to PATRICK SHOP on 1/9/26 at 12:00 PM..."
     */
    fun parseMpesaMessage(message: String): ParsedMpesa? {
        try {
            // Regex to match Ksh500.00 or Ksh 500
            val amountRegex = Regex("Ksh\\s*([\\d,]+\\.?\\d*)", RegexOption.IGNORE_CASE)
            val amountMatch = amountRegex.find(message)
            val amountString = amountMatch?.groupValues?.get(1)?.replace(",", "")
            val amount = amountString?.toDoubleOrNull() ?: 0.0

            // Try to find a name - usually between "from" and "on" or before "on"
            // Example: "received Ksh500.00 from JOHN DOE 0712345678 on 1/9/26"
            val senderRegex = Regex("(?:from|by)\\s+([A-Z\\s]+)(?:\\d{10}|on)", RegexOption.IGNORE_CASE)
            val senderMatch = senderRegex.find(message)
            val sender = senderMatch?.groupValues?.get(1)?.trim() ?: ""

            if (amount > 0) {
                return ParsedMpesa(amount, sender)
            }
        } catch (e: Exception) {
            // Silent fail
        }
        return null
    }
}

data class ParsedMpesa(val amount: Double, val sender: String)
