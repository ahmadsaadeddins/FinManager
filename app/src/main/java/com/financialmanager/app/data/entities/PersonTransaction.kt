package com.financialmanager.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "person_transactions",
    foreignKeys = [
        ForeignKey(
            entity = PersonAccount::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["personId"])]
)
data class PersonTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personId: Long,
    val amount: Double,
    val date: Long, // timestamp
    val description: String? = null,
    val type: String, // "they_owe_me" or "i_owe_them"
    val category: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

