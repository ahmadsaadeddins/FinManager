package com.financialmanager.app.data.repository

import com.financialmanager.app.data.dao.PersonDao
import com.financialmanager.app.data.entities.PersonAccount
import com.financialmanager.app.data.entities.PersonTransaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonRepository @Inject constructor(
    private val personDao: PersonDao
) {
    // PersonAccount operations
    fun getAllPeople(): Flow<List<PersonAccount>> = personDao.getAllPeople()

    suspend fun getPersonById(id: Long): PersonAccount? = personDao.getPersonById(id)

    fun searchPeople(query: String): Flow<List<PersonAccount>> = personDao.searchPeople(query)

    suspend fun insertPerson(person: PersonAccount): Long = personDao.insertPerson(person)

    suspend fun updatePerson(person: PersonAccount) = personDao.updatePerson(person)

    suspend fun deletePerson(person: PersonAccount) = personDao.deletePerson(person)

    suspend fun deletePersonById(id: Long) = personDao.deletePersonById(id)

    // PersonTransaction operations
    fun getTransactionsByPerson(personId: Long): Flow<List<PersonTransaction>> =
        personDao.getTransactionsByPerson(personId)

    suspend fun getTransactionById(id: Long): PersonTransaction? =
        personDao.getTransactionById(id)

    fun getAllPersonTransactions(): Flow<List<PersonTransaction>> =
        personDao.getAllPersonTransactions()

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<PersonTransaction>> =
        personDao.getTransactionsByDateRange(startDate, endDate)

    fun getPersonBalance(personId: Long): Flow<Double?> = personDao.getPersonBalance(personId)

    suspend fun insertTransaction(transaction: PersonTransaction): Long =
        personDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: PersonTransaction) =
        personDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: PersonTransaction) =
        personDao.deleteTransaction(transaction)

    suspend fun deleteTransactionById(id: Long) = personDao.deleteTransactionById(id)
}

