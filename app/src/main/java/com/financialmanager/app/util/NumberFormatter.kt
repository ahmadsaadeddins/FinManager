package com.financialmanager.app.util

import java.text.NumberFormat
import java.util.*

object NumberFormatter {

    private const val DEFAULT_CURRENCY = "ج.م"

    fun formatCurrency(
        value: Double?,
        hideNumbers: Boolean,
        currencySymbol: String = DEFAULT_CURRENCY,
        isRTL: Boolean = LocaleHelper.isRTL()
    ): String {
        if (value == null) return if (hideNumbers) "****" else "${currencySymbol}0.00"

        return if (hideNumbers) {
            val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
            val formattedValue = formatter.format(value)
            val stars = formattedValue.replace(Regex("[0-9٠-٩]"), "*")
            
            if (isRTL) {
                "$stars $currencySymbol"
            } else {
                "$currencySymbol$stars"
            }
        } else {
            val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
            val formattedNumber = formatter.format(value)

            if (isRTL) {
                "$formattedNumber $currencySymbol"
            } else {
                "$currencySymbol$formattedNumber"
            }
        }
    }
}
