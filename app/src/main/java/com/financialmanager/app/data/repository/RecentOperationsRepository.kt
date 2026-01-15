package com.financialmanager.app.data.repository

import com.financialmanager.app.data.dao.CapitalDao
import com.financialmanager.app.data.dao.InventoryDao
import com.financialmanager.app.data.dao.PersonDao
import com.financialmanager.app.data.dao.TransactionDao
import com.financialmanager.app.data.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentOperationsRepository @Inject constructor(
    private val personDao: PersonDao,
    private val transactionDao: TransactionDao,
    private val inventoryDao: InventoryDao,
    private val capitalDao: CapitalDao
) {
    
    fun getRecentOperations(limit: Int = 50): Flow<List<RecentOperation>> {
        val personRecent = personDao.getRecentPersons(limit)
        val personTxRecent = personDao.getRecentPersonTransactions(limit)
        val inventoryRecent = inventoryDao.getRecentItems(limit)
        val outTxRecent = transactionDao.getRecentTransactions(limit)
        val capitalTxRecent = capitalDao.getRecentTransactions(limit)
        val allPeopleFlow = personDao.getAllPeople()
        
        // Combine Capital Transactions and All People into a single flow of Pairs
        val capitalAndPeople = combine(capitalTxRecent, allPeopleFlow) { c, ap -> p(c, ap) }
        
        return combine(
            personRecent,
            personTxRecent,
            inventoryRecent,
            outTxRecent,
            capitalAndPeople
        ) { persons, personTransactions, inventoryItems, outTransactions, capPair ->
            val capitalTransactions = capPair.first
            val allPeople = capPair.second
            
            val peopleMap = allPeople.associateBy { it.id }
            val operations = mutableListOf<RecentOperation>()
            
            // Add person additions
            persons.forEach { person ->
                operations.add(
                    RecentOperation(
                        id = person.id,
                        type = OperationType.PERSON_ADDED,
                        title = "Added Person",
                        description = person.name,
                        timestamp = person.createdAt,
                        entityData = EntityData.Person(person)
                    )
                )
            }
            
            // Add person transactions
            personTransactions.forEach { transaction ->
                val person = peopleMap[transaction.personId]
                operations.add(
                    RecentOperation(
                        id = transaction.id,
                        type = OperationType.PERSON_TRANSACTION,
                        title = if (transaction.type == PersonTransactionType.THEY_OWE_ME) "Money Lent" else "Money Borrowed",
                        description = "${person?.name ?: "Unknown"} - ${transaction.description ?: "No description"}",
                        amount = transaction.amount,
                        timestamp = transaction.createdAt,
                        entityData = EntityData.PersonTx(transaction)
                    )
                )
            }
            
            // Add inventory additions
            inventoryItems.forEach { item ->
                operations.add(
                    RecentOperation(
                        id = item.id,
                        type = OperationType.INVENTORY_ADDED,
                        title = "Added Item",
                        description = "${item.name} (Qty: ${item.quantity})",
                        amount = item.purchasePrice,
                        timestamp = item.createdAt,
                        entityData = EntityData.Inventory(item)
                    )
                )
            }
            
            // Add out transactions
            outTransactions.forEach { transaction ->
                operations.add(
                    RecentOperation(
                        id = transaction.id,
                        type = OperationType.OUT_TRANSACTION,
                        title = if (transaction.type == TransactionType.EXPENSE) "Expense" else "Sale",
                        description = transaction.description ?: "No description",
                        amount = transaction.amount,
                        timestamp = transaction.createdAt,
                        entityData = EntityData.OutTx(transaction)
                    )
                )
            }
            
            // Add capital transactions
            capitalTransactions.forEach { transaction ->
                operations.add(
                    RecentOperation(
                        id = transaction.id,
                        type = OperationType.CAPITAL_TRANSACTION,
                        title = if (transaction.type == CapitalTransactionType.INVESTMENT) "Investment" else "Withdrawal",
                        description = "${transaction.source} - ${transaction.description ?: "No description"}",
                        amount = transaction.amount,
                        timestamp = transaction.createdAt,
                        entityData = EntityData.CapitalTx(transaction)
                    )
                )
            }
            
            // Sort by timestamp (most recent first) and limit
            operations.sortedByDescending { it.timestamp }.take(limit)
        }
    }
    
    // Helper for nested combine
    private fun <A, B> p(a: A, b: B): Pair<A, B> = Pair(a, b)
    
    suspend fun deleteOperation(operation: RecentOperation): Boolean {
        return try {
            when (val entityData = operation.entityData) {
                is EntityData.Person -> {
                    personDao.deletePerson(entityData.person)
                }
                is EntityData.PersonTx -> {
                    personDao.deletePersonTransaction(entityData.transaction)
                }
                is EntityData.Inventory -> {
                    inventoryDao.deleteItem(entityData.item)
                }
                is EntityData.OutTx -> {
                    transactionDao.deleteTransaction(entityData.transaction)
                }
                is EntityData.CapitalTx -> {
                    capitalDao.deleteTransaction(entityData.transaction)
                }
                null -> return false
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}