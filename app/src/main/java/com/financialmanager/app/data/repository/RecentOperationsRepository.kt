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
        return combine(
            personDao.getRecentPersons(limit),
            personDao.getRecentPersonTransactions(limit),
            inventoryDao.getRecentItems(limit),
            transactionDao.getRecentTransactions(limit),
            capitalDao.getRecentTransactions(limit)
        ) { persons, personTransactions, inventoryItems, outTransactions, capitalTransactions ->
            
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
                        entityData = person
                    )
                )
            }
            
            // Add person transactions
            personTransactions.forEach { transaction ->
                val person = personDao.getPersonByIdSync(transaction.personId)
                operations.add(
                    RecentOperation(
                        id = transaction.id,
                        type = OperationType.PERSON_TRANSACTION,
                        title = if (transaction.type == "they_owe_me") "Money Lent" else "Money Borrowed",
                        description = "${person?.name ?: "Unknown"} - ${transaction.description ?: "No description"}",
                        amount = transaction.amount,
                        timestamp = transaction.createdAt,
                        entityData = transaction
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
                        entityData = item
                    )
                )
            }
            
            // Add out transactions
            outTransactions.forEach { transaction ->
                operations.add(
                    RecentOperation(
                        id = transaction.id,
                        type = OperationType.OUT_TRANSACTION,
                        title = if (transaction.type == "expense") "Expense" else "Sale",
                        description = transaction.description ?: "No description",
                        amount = transaction.amount,
                        timestamp = transaction.createdAt,
                        entityData = transaction
                    )
                )
            }
            
            // Add capital transactions
            capitalTransactions.forEach { transaction ->
                operations.add(
                    RecentOperation(
                        id = transaction.id,
                        type = OperationType.CAPITAL_TRANSACTION,
                        title = if (transaction.type == "investment") "Investment" else "Withdrawal",
                        description = "${transaction.source} - ${transaction.description ?: "No description"}",
                        amount = transaction.amount,
                        timestamp = transaction.createdAt,
                        entityData = transaction
                    )
                )
            }
            
            // Sort by timestamp (most recent first) and limit
            operations.sortedByDescending { it.timestamp }.take(limit)
        }
    }
    
    suspend fun deleteOperation(operation: RecentOperation): Boolean {
        return try {
            when (operation.type) {
                OperationType.PERSON_ADDED -> {
                    val person = operation.entityData as PersonAccount
                    personDao.deletePerson(person)
                }
                OperationType.PERSON_TRANSACTION -> {
                    val transaction = operation.entityData as PersonTransaction
                    personDao.deletePersonTransaction(transaction)
                }
                OperationType.INVENTORY_ADDED -> {
                    val item = operation.entityData as InventoryItem
                    inventoryDao.deleteItem(item)
                }
                OperationType.OUT_TRANSACTION -> {
                    val transaction = operation.entityData as OutTransaction
                    transactionDao.deleteTransaction(transaction)
                }
                OperationType.CAPITAL_TRANSACTION -> {
                    val transaction = operation.entityData as CapitalTransaction
                    capitalDao.deleteTransaction(transaction)
                }
                else -> return false
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}