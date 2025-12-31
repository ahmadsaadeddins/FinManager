package com.financialmanager.app.data.repository

import com.financialmanager.app.data.dao.InventoryDao
import com.financialmanager.app.data.entities.InventoryItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val inventoryDao: InventoryDao
) {
    fun getAllItems(): Flow<List<InventoryItem>> = inventoryDao.getAllItems()

    suspend fun getItemById(id: Long): InventoryItem? = inventoryDao.getItemById(id)

    fun getItemsByCategory(category: String): Flow<List<InventoryItem>> =
        inventoryDao.getItemsByCategory(category)

    fun searchItems(query: String): Flow<List<InventoryItem>> = inventoryDao.searchItems(query)

    fun getTotalInventoryValue(): Flow<Double?> = inventoryDao.getTotalInventoryValue()

    suspend fun insertItem(item: InventoryItem): Long = inventoryDao.insertItem(item)

    suspend fun updateItem(item: InventoryItem) = inventoryDao.updateItem(item)

    suspend fun deleteItem(item: InventoryItem) = inventoryDao.deleteItem(item)

    suspend fun deleteItemById(id: Long) = inventoryDao.deleteItemById(id)

    suspend fun insertItems(items: List<InventoryItem>): List<Long> = inventoryDao.insertItems(items)
}

