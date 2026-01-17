package com.financialmanager.app.data.entities

enum class Currency(val code: String, val symbol: String) {
    USD("USD", "$"),
    EGP("EGP", "ج.م"),
    EUR("EUR", "€"),
    SAR("SAR", "ر.س");
    
    companion object {
        fun fromCode(code: String): Currency {
            return values().find { it.code == code } ?: EGP
        }
    }
}
