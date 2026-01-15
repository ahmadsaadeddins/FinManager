package com.financialmanager.app.data.entities

sealed class EntityData {
    data class Person(val person: PersonAccount) : EntityData()
    data class PersonTx(val transaction: PersonTransaction) : EntityData()
    data class Inventory(val item: InventoryItem) : EntityData()
    data class OutTx(val transaction: OutTransaction) : EntityData()
    data class CapitalTx(val transaction: CapitalTransaction) : EntityData()
}

data class RecentOperation(
    val id: Long,
    val type: OperationType,
    val title: String,
    val description: String,
    val amount: Double? = null,
    val timestamp: Long,
    val canEdit: Boolean = true,
    val canDelete: Boolean = true,
    val entityData: EntityData? = null // Store the actual entity for operations
)

enum class OperationType {
    PERSON_ADDED,
    PERSON_TRANSACTION,
    INVENTORY_ADDED,
    INVENTORY_UPDATED,
    OUT_TRANSACTION,
    CAPITAL_TRANSACTION
}