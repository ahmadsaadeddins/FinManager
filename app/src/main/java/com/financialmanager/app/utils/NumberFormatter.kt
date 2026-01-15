package com.financialmanager.app.utils

import java.text.NumberFormat
import java.util.*

object NumberFormatter {
    
    fun formatCurrency(value: Double?, hideNumbers: Boolean): String {
        if (value == null) return if (hideNumbers) "****" else "$0.00"
        
        return if (hideNumbers) {
            // Generate stars based on the magnitude of the number
            val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            val formattedValue = formatter.format(value)
            
            // Replace digits with stars, keep currency symbol and formatting
            formattedValue.replace(Regex("[0-9]"), "*")
        } else {
            val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            formatter.format(value)
        }
    }
}