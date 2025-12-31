package com.financialmanager.app.data.repository

import com.financialmanager.app.data.dao.CapitalDao
import com.financialmanager.app.data.entities.CapitalTransaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CapitalRepository @Inject constructor(
    private val capitalDao: CapitalDao
) {
    fun getAllTransactions(): Flow<List<CapitalTransaction>> = capitalDao.getAllTransactions()

    suspend fun getTransactionById(id: Long): CapitalTransaction? =
        capitalDao.getTransactionById(id)

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<CapitalTransaction>> =
        capitalDao.getTransactionsByDateRange(startDate, endDate)

    fun searchTransactions(query: String): Flow<List<CapitalTransaction>> =
        capitalDao.searchTransactions(query)

    fun getTotalCapital(): Flow<Double?> = capitalDao.getTotalCapital()

    fun getTotalInvestments(): Flow<Double?> = capitalDao.getTotalInvestments()

    fun getTotalWithdrawals(): Flow<Double?> = capitalDao.getTotalWithdrawals()

    suspend fun insertTransaction(transaction: CapitalTransaction): Long =
        capitalDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: CapitalTransaction) =
        capitalDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: CapitalTransaction) =
        capitalDao.deleteTransaction(transaction)

    suspend fun deleteTransactionById(id: Long) = capitalDao.deleteTransactionById(id)
}

