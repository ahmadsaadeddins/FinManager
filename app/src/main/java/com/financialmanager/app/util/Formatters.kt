package com.financialmanager.app.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object Formatters {
    val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val dateFormatter: SimpleDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateTimeFormatter: SimpleDateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    private const val DEFAULT_CURRENCY = "ج.م"

    fun formatCurrency(amount: Double?): String {
        return formatCurrency(amount, DEFAULT_CURRENCY, LocaleHelper.isRTL())
    }

    fun formatCurrency(
        amount: Double?,
        currencySymbol: String = DEFAULT_CURRENCY,
        isRTL: Boolean = LocaleHelper.isRTL()
    ): String {
        return amount?.let {
            val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
            val formattedNumber = formatter.format(it)

            if (isRTL) {
                "$formattedNumber $currencySymbol"
            } else {
                "$currencySymbol$formattedNumber"
            }
        } ?: "${currencySymbol}0.00"
    }

    fun formatDate(timestamp: Long): String {
        return dateFormatter.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormatter.format(Date(timestamp))
    }
}

