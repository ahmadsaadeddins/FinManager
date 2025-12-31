package com.financialmanager.app.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object Formatters {
    val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val dateFormatter: SimpleDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateTimeFormatter: SimpleDateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    fun formatCurrency(amount: Double?): String {
        return amount?.let { currencyFormatter.format(it) } ?: "$0.00"
    }
    
    fun formatDate(timestamp: Long): String {
        return dateFormatter.format(Date(timestamp))
    }
    
    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormatter.format(Date(timestamp))
    }
}

