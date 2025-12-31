package com.financialmanager.app.data.dao

import androidx.room.*
import com.financialmanager.app.data.entities.OutTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM out_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<OutTransaction>>

    @Query("SELECT * FROM out_transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): OutTransaction?

    @Query("SELECT * FROM out_transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: String): Flow<List<OutTransaction>>

    @Query("SELECT * FROM out_transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<OutTransaction>>

    @Query("SELECT * FROM out_transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): Flow<List<OutTransaction>>

    @Query("SELECT * FROM out_transactions WHERE description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchTransactions(query: String): Flow<List<OutTransaction>>

    @Query("SELECT SUM(amount) FROM out_transactions WHERE type = 'expense'")
    fun getTotalExpenses(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM out_transactions WHERE type = 'sale'")
    fun getTotalSales(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM out_transactions WHERE type = 'expense' AND date BETWEEN :startDate AND :endDate")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM out_transactions WHERE type = 'sale' AND date BETWEEN :startDate AND :endDate")
    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Insert
    suspend fun insertTransaction(transaction: OutTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: OutTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: OutTransaction)

    @Query("DELETE FROM out_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}

