package com.financialmanager.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "capital_transactions")
data class CapitalTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val source: String, // person/investor name
    val date: Long, // timestamp
    val description: String? = null,
    val type: String, // "investment" or "withdrawal"
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

