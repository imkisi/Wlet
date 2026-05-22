package com.example.wlet.ui.util

import java.text.NumberFormat
import java.util.*

/**
 * Utility function to format currency based on the application's selected currency code.
 */
fun formatCurrency(amount: Double, currencyCode: String): String {
    val locale = when (currencyCode) {
        "USD" -> Locale.US
        "EUR" -> Locale.GERMANY
        "JPY" -> Locale.JAPAN
        "GBP" -> Locale.UK
        else -> Locale("id", "ID")
    }
    
    return try {
        val format = NumberFormat.getCurrencyInstance(locale)
        format.currency = java.util.Currency.getInstance(currencyCode)
        // IDR usually doesn't show decimals in simple trackers
        format.maximumFractionDigits = if (currencyCode == "IDR") 0 else 2
        format.format(amount).replace(currencyCode, "$currencyCode ")
    } catch (e: Exception) {
        // Fallback formatting if locale or currency code is problematic
        val formattedAmount = String.format("%.0f", amount)
        "$currencyCode $formattedAmount"
    }
}
