package com.financialmanager.app.data.dao

import androidx.room.*
import com.financialmanager.app.data.entities.PersonAccount
import com.financialmanager.app.data.entities.PersonTransaction
import kotlinx.coroutines.flow.Flow

data class PersonBalance(
    val personId: Long,
    val balance: Double
)

@Dao
interface PersonDao {
    // PersonAccount queries
    @Query("SELECT * FROM person_accounts ORDER BY name ASC")
    fun getAllPeople(): Flow<List<PersonAccount>>

    @Query("SELECT * FROM person_accounts WHERE id = :id")
    suspend fun getPersonById(id: Long): PersonAccount?

    @Query("SELECT * FROM person_accounts WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchPeople(query: String): Flow<List<PersonAccount>>

    @Insert
    suspend fun insertPerson(person: PersonAccount): Long

    @Update
    suspend fun updatePerson(person: PersonAccount)

    @Delete
    suspend fun deletePerson(person: PersonAccount)

    @Query("DELETE FROM person_accounts WHERE id = :id")
    suspend fun deletePersonById(id: Long)

    // PersonTransaction queries
    @Query("SELECT * FROM person_transactions WHERE personId = :personId ORDER BY date DESC")
    fun getTransactionsByPerson(personId: Long): Flow<List<PersonTransaction>>

    @Query("SELECT * FROM person_transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): PersonTransaction?

    @Query("SELECT * FROM person_transactions ORDER BY date DESC")
    fun getAllPersonTransactions(): Flow<List<PersonTransaction>>

    @Query("SELECT * FROM person_transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<PersonTransaction>>

    @Query("""
        SELECT 
            SUM(CASE WHEN type = 'they_owe_me' THEN amount ELSE -amount END) as balance
        FROM person_transactions 
        WHERE personId = :personId
    """)
    fun getPersonBalance(personId: Long): Flow<Double?>

    @Query("""
        SELECT 
            personId,
            SUM(CASE WHEN type = 'they_owe_me' THEN amount ELSE -amount END) as balance
        FROM person_transactions 
        GROUP BY personId
    """)
    fun getAllPersonBalances(): Flow<List<PersonBalance>>

    @Insert
    suspend fun insertTransaction(transaction: PersonTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: PersonTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: PersonTransaction)

    @Query("DELETE FROM person_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}

