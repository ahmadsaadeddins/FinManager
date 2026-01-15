package com.financialmanager.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "out_transactions")
data class OutTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String? = null,
    val date: Long, // timestamp
    val description: String? = null,
    val type: String, // "expense" or "sale"
    val relatedItemId: Long? = null, // link to inventory item if it's a sale
    val quantity: Int = 1, // quantity sold (for sales)
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false, // for archiving sales
    val archivedAt: Long? = null // when it was archived
)

