package com.financialmanager.app.data.entities

data class RecentOperation(
    val id: Long,
    val type: OperationType,
    val title: String,
    val description: String,
    val amount: Double? = null,
    val timestamp: Long,
    val canEdit: Boolean = true,
    val canDelete: Boolean = true,
    val entityData: Any? = null // Store the actual entity for operations
)

enum class OperationType {
    PERSON_ADDED,
    PERSON_TRANSACTION,
    INVENTORY_ADDED,
    INVENTORY_UPDATED,
    OUT_TRANSACTION,
    CAPITAL_TRANSACTION
}