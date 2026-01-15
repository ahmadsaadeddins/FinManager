package com.financialmanager.app.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class CapitalTransactionType(val value: String) {
    INVESTMENT("investment"),
    WITHDRAWAL("withdrawal")
}

@Entity(
    tableName = "capital_transactions",
    indices = [Index(value = ["date"])]
)
data class CapitalTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val source: String, // person/investor name
    val date: Long, // timestamp
    val description: String? = null,
    val type: CapitalTransactionType,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

