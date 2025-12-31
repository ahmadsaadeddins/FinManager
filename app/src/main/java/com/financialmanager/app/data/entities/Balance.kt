package com.financialmanager.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "balances")
data class Balance(
    @PrimaryKey
    val type: String, // "capital", "inventory", "out"
    val totalAmount: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)

