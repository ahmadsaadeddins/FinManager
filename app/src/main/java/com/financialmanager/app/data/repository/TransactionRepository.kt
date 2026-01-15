package com.financialmanager.app.data.repository

import com.financialmanager.app.data.dao.TransactionDao
import com.financialmanager.app.data.entities.OutTransaction
import com.financialmanager.app.data.entities.TransactionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun getAllTransactions(): Flow<List<OutTransaction>> = transactionDao.getAllTransactions()

    suspend fun getTransactionById(id: Long): OutTransaction? = transactionDao.getTransactionById(id)

    fun getTransactionsByType(type: TransactionType): Flow<List<OutTransaction>> =
        transactionDao.getTransactionsByType(type)

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<OutTransaction>> =
        transactionDao.getTransactionsByDateRange(startDate, endDate)

    fun getTransactionsByCategory(category: String): Flow<List<OutTransaction>> =
        transactionDao.getTransactionsByCategory(category)

    fun searchTransactions(query: String): Flow<List<OutTransaction>> =
        transactionDao.searchTransactions(query)

    fun getTotalExpenses(): Flow<Double?> = transactionDao.getTotalExpenses()

    fun getTotalSales(): Flow<Double?> = transactionDao.getTotalSales()

    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<Double?> =
        transactionDao.getExpensesByDateRange(startDate, endDate)

    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<Double?> =
        transactionDao.getSalesByDateRange(startDate, endDate)

    suspend fun insertTransaction(transaction: OutTransaction): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: OutTransaction) =
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: OutTransaction) =
        transactionDao.deleteTransaction(transaction)

    suspend fun deleteTransactionById(id: Long) = transactionDao.deleteTransactionById(id)
    
    // Archive-related methods
    fun getArchivedSales(): Flow<List<OutTransaction>> = transactionDao.getArchivedSales()
    
    fun getTotalArchivedSales(): Flow<Double?> = transactionDao.getTotalArchivedSales()
    
    suspend fun archiveAllSales(): Int = transactionDao.archiveAllSales()
    
    suspend fun unarchiveTransaction(id: Long) = transactionDao.unarchiveTransaction(id)
    
    fun getActiveSalesCount(): Flow<Int> = transactionDao.getActiveSalesCount()
    
    fun getSaleTransactions(): Flow<List<OutTransaction>> = transactionDao.getTransactionsByType(TransactionType.SALE)
}

