package com.financialmanager.app.data.repository

import com.financialmanager.app.data.dao.PersonDao
import com.financialmanager.app.data.entities.PersonAccount
import com.financialmanager.app.data.entities.PersonTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    suspend fun incrementPersonUsage(personId: Long) = personDao.incrementUsage(personId)

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

    fun getTotalPeopleBalance(): Flow<Double?> = personDao.getAllPersonBalances()
        .map { balances -> balances.sumOf { it.balance } }

    suspend fun insertTransaction(transaction: PersonTransaction): Long =
        personDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: PersonTransaction) =
        personDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: PersonTransaction) =
        personDao.deleteTransaction(transaction)

    suspend fun deleteTransactionById(id: Long) = personDao.deleteTransactionById(id)

    // Transfer between people
    suspend fun transferBetweenPeople(
        fromPersonId: Long,
        toPersonId: Long,
        amount: Double,
        description: String = "Transfer"
    ) {
        val timestamp = System.currentTimeMillis()
        
        // Create transaction for person giving money (they owe me less / I owe them more)
        val fromTransaction = PersonTransaction(
            personId = fromPersonId,
            amount = amount,
            date = timestamp,
            description = "$description (to ${getPersonById(toPersonId)?.name ?: "Unknown"})",
            type = "i_owe_them", // Money going out from this person
            category = "Transfer",
            notes = "Transfer to person ID: $toPersonId"
        )
        
        // Create transaction for person receiving money (they owe me more / I owe them less)
        val toTransaction = PersonTransaction(
            personId = toPersonId,
            amount = amount,
            date = timestamp,
            description = "$description (from ${getPersonById(fromPersonId)?.name ?: "Unknown"})",
            type = "they_owe_me", // Money coming in to this person
            category = "Transfer",
            notes = "Transfer from person ID: $fromPersonId"
        )
        
        // Insert both transactions
        insertTransaction(fromTransaction)
        insertTransaction(toTransaction)
        
        // Track usage for both people
        incrementPersonUsage(fromPersonId)
        incrementPersonUsage(toPersonId)
    }
}

