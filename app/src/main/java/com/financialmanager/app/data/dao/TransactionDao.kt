package com.financialmanager.app.data.dao

import androidx.room.*
import com.financialmanager.app.data.entities.OutTransaction
import com.financialmanager.app.data.entities.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM out_transactions WHERE isArchived = 0 ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<OutTransaction>>

    @Query("SELECT * FROM out_transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): OutTransaction?

    @Query("SELECT * FROM out_transactions WHERE type = :type AND isArchived = 0 ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<OutTransaction>>

    @Query("SELECT * FROM out_transactions WHERE date BETWEEN :startDate AND :endDate AND isArchived = 0 ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<OutTransaction>>

    @Query("SELECT * FROM out_transactions WHERE category = :category AND isArchived = 0 ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): Flow<List<OutTransaction>>

    @Query("SELECT * FROM out_transactions WHERE (description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') AND isArchived = 0 ORDER BY date DESC")
    fun searchTransactions(query: String): Flow<List<OutTransaction>>

    @Query("SELECT SUM(amount) FROM out_transactions WHERE type = 'expense' AND isArchived = 0")
    fun getTotalExpenses(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM out_transactions WHERE type = 'sale' AND isArchived = 0")
    fun getTotalSales(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM out_transactions WHERE type = 'expense' AND date BETWEEN :startDate AND :endDate AND isArchived = 0")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM out_transactions WHERE type = 'sale' AND date BETWEEN :startDate AND :endDate AND isArchived = 0")
    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Insert
    suspend fun insertTransaction(transaction: OutTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: OutTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: OutTransaction)

    @Query("DELETE FROM out_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
    
    // Recent operations queries
    @Query("SELECT * FROM out_transactions WHERE isArchived = 0 ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<OutTransaction>>
    
    // Archive-related queries
    @Query("SELECT * FROM out_transactions WHERE isArchived = 1 ORDER BY archivedAt DESC")
    fun getArchivedTransactions(): Flow<List<OutTransaction>>
    
    @Query("SELECT * FROM out_transactions WHERE type = 'sale' AND isArchived = 1 ORDER BY archivedAt DESC")
    fun getArchivedSales(): Flow<List<OutTransaction>>
    
    @Query("SELECT SUM(amount) FROM out_transactions WHERE type = 'sale' AND isArchived = 1")
    fun getTotalArchivedSales(): Flow<Double?>
    
    @Query("UPDATE out_transactions SET isArchived = 1, archivedAt = :archivedAt WHERE type = 'sale' AND isArchived = 0")
    suspend fun archiveAllSales(archivedAt: Long = System.currentTimeMillis()): Int
    
    @Query("UPDATE out_transactions SET isArchived = 0, archivedAt = NULL WHERE id = :id")
    suspend fun unarchiveTransaction(id: Long)
    
    @Query("SELECT COUNT(*) FROM out_transactions WHERE type = 'sale' AND isArchived = 0")
    fun getActiveSalesCount(): Flow<Int>
}

