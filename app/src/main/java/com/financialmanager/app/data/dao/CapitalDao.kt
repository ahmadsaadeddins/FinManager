package com.financialmanager.app.data.dao

import androidx.room.*
import com.financialmanager.app.data.entities.CapitalTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CapitalDao {
    @Query("SELECT * FROM capital_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<CapitalTransaction>>

    @Query("SELECT * FROM capital_transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): CapitalTransaction?

    @Query("SELECT * FROM capital_transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<CapitalTransaction>>

    @Query("SELECT * FROM capital_transactions WHERE source LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchTransactions(query: String): Flow<List<CapitalTransaction>>

    @Query("SELECT SUM(CASE WHEN type = 'investment' THEN amount ELSE -amount END) FROM capital_transactions")
    fun getTotalCapital(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM capital_transactions WHERE type = 'investment'")
    fun getTotalInvestments(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM capital_transactions WHERE type = 'withdrawal'")
    fun getTotalWithdrawals(): Flow<Double?>

    @Insert
    suspend fun insertTransaction(transaction: CapitalTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: CapitalTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: CapitalTransaction)

    @Query("DELETE FROM capital_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}

