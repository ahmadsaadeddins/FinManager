package com.financialmanager.app.data.dao

import androidx.room.*
import com.financialmanager.app.data.entities.InventoryItem
import com.financialmanager.app.data.entities.OutTransaction
import com.financialmanager.app.data.entities.TransactionType

@Dao
interface TransactionInventoryDao {

    @Transaction
    suspend fun insertTransactionWithInventoryUpdate(transaction: OutTransaction): Long {
        val transactionId = insertTransaction(transaction)
        // If it's a sale, deduct quantity from inventory
        if (transaction.type == TransactionType.SALE && transaction.relatedItemId != null) {
            val item = getItemById(transaction.relatedItemId)
            item?.let {
                val updatedItem = it.copy(
                    quantity = (it.quantity - transaction.quantity).coerceAtLeast(0),
                    updatedAt = System.currentTimeMillis()
                )
                updateItem(updatedItem)
            }
        }
        return transactionId
    }

    @Transaction
    suspend fun updateTransactionWithInventoryUpdate(transaction: OutTransaction) {
        val oldTransaction = getTransactionById(transaction.id)
        updateTransaction(transaction)

        // Handle inventory updates for sales
        if (transaction.type == TransactionType.SALE && transaction.relatedItemId != null) {
            val item = getItemById(transaction.relatedItemId)
            item?.let {
                val oldQuantity = oldTransaction?.quantity ?: 0
                val quantityDiff = transaction.quantity - oldQuantity
                val updatedItem = it.copy(
                    quantity = (it.quantity - quantityDiff).coerceAtLeast(0),
                    updatedAt = System.currentTimeMillis()
                )
                updateItem(updatedItem)
            }
        }
    }

    @Transaction
    suspend fun deleteTransactionWithInventoryRestore(transaction: OutTransaction) {
        deleteTransaction(transaction)
        // If it's a sale, restore quantity to inventory
        if (transaction.type == TransactionType.SALE && transaction.relatedItemId != null) {
            val item = getItemById(transaction.relatedItemId)
            item?.let {
                val updatedItem = it.copy(
                    quantity = it.quantity + transaction.quantity,
                    updatedAt = System.currentTimeMillis()
                )
                updateItem(updatedItem)
            }
        }
    }

    // Transaction DAO methods
    @Insert
    suspend fun insertTransaction(transaction: OutTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: OutTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: OutTransaction)

    @Query("SELECT * FROM out_transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): OutTransaction?

    // Inventory DAO methods
    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getItemById(id: Long): InventoryItem?

    @Update
    suspend fun updateItem(item: InventoryItem)
}
