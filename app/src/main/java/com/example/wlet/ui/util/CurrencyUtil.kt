package com.example.wlet.ui.util

import java.text.NumberFormat
import java.util.*

/**
 * Utility function to format currency based on the application's selected currency code.
 */
fun formatCurrency(amount: Double, currencyCode: String): String {
    val actualCode = if (currencyCode.contains(" - ")) {
        currencyCode.split(" - ")[0].trim()
    } else {
        currencyCode.trim()
    }

    val locale = when (actualCode) {
        "USD" -> Locale.US
        "EUR" -> Locale.GERMANY
        "JPY" -> Locale.JAPAN
        "GBP" -> Locale.UK
        else -> Locale("id", "ID")
    }
    
    return try {
        val format = NumberFormat.getCurrencyInstance(locale)
        format.currency = java.util.Currency.getInstance(actualCode)
        // IDR usually doesn't show decimals in simple trackers
        format.maximumFractionDigits = if (actualCode == "IDR") 0 else 2
        format.format(amount).replace(actualCode, "$actualCode ")
    } catch (e: Exception) {
        // Fallback formatting if locale or currency code is problematic
        val formattedAmount = String.format("%.0f", amount)
        val symbol = if (currencyCode.contains("(") && currencyCode.endsWith(")")) {
            currencyCode.substringAfterLast("(").substringBefore(")")
        } else {
            actualCode
        }
        "$symbol $formattedAmount"
    }
}
