package com.financialmanager.app.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionType(val value: String) {
    EXPENSE("expense"),
    SALE("sale")
}

@Entity(
    tableName = "out_transactions",
    indices = [
        Index(value = ["date"]),
        Index(value = ["type"])
    ]
)
data class OutTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String? = null,
    val date: Long, // timestamp
    val description: String? = null,
    val type: TransactionType,
    val relatedItemId: Long? = null, // link to inventory item if it's a sale
    val quantity: Int = 1, // quantity sold (for sales)
    val costPrice: Double = 0.0, // Cost of goods sold (purchase price at time of sale)
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false, // for archiving sales
    val archivedAt: Long? = null // when it was archived
)

