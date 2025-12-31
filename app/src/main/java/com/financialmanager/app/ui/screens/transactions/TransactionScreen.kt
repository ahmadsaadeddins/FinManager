package com.financialmanager.app.ui.screens.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.financialmanager.app.data.entities.InventoryItem
import com.financialmanager.app.data.entities.OutTransaction
import com.financialmanager.app.ui.components.BottomNavigationBar
import com.financialmanager.app.ui.navigation.Screen
import com.financialmanager.app.ui.theme.MoneyIn
import com.financialmanager.app.ui.theme.MoneyOut
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    navController: NavController,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<OutTransaction?>(null) }
    var showDeleteDialog by remember { mutableStateOf<OutTransaction?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = Screen.Transactions.route,
                onNavigate = { route -> navController.navigate(route) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search transactions...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { viewModel.setType(null) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedType == "expense",
                    onClick = { viewModel.setType("expense") },
                    label = { Text("Expenses") }
                )
                FilterChip(
                    selected = selectedType == "sale",
                    onClick = { viewModel.setType("sale") },
                    label = { Text("Sales") }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            onEdit = { editingTransaction = it },
                            onDelete = { showDeleteDialog = it }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog || editingTransaction != null) {
        TransactionDialog(
            transaction = editingTransaction,
            viewModel = viewModel,
            onDismiss = {
                showAddDialog = false
                editingTransaction = null
            },
            onSave = { transaction ->
                if (editingTransaction != null) {
                    viewModel.updateTransaction(transaction)
                } else {
                    viewModel.insertTransaction(transaction)
                }
                showAddDialog = false
                editingTransaction = null
            }
        )
    }

    showDeleteDialog?.let { transaction ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(transaction)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TransactionCard(
    transaction: OutTransaction,
    onEdit: (OutTransaction) -> Unit,
    onDelete: (OutTransaction) -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val color = if (transaction.type == "sale") MoneyIn else MoneyOut

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description ?: "No description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormatter.format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall
                )
                if (transaction.category != null) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatter.format(transaction.amount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = transaction.type,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row {
                IconButton(onClick = { onEdit(transaction) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { onDelete(transaction) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDialog(
    transaction: OutTransaction?,
    viewModel: TransactionViewModel,
    onDismiss: () -> Unit,
    onSave: (OutTransaction) -> Unit
) {
    var amount by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(transaction?.category ?: "") }
    var description by remember { mutableStateOf(transaction?.description ?: "") }
    var type by remember { mutableStateOf(transaction?.type ?: "expense") }
    var date by remember { mutableStateOf(transaction?.date ?: System.currentTimeMillis()) }
    var selectedItemId by remember { mutableStateOf<Long?>(transaction?.relatedItemId) }
    var quantity by remember { mutableStateOf(transaction?.quantity?.toString() ?: "1") }
    var showItemSelector by remember { mutableStateOf(false) }
    var itemSearchQuery by remember { mutableStateOf("") }
    
    val inventoryItems by viewModel.inventoryItems.collectAsState()
    val selectedItem = inventoryItems.find { it.id == selectedItemId }
    
    // Filter inventory items based on search query
    val filteredInventoryItems = remember(inventoryItems, itemSearchQuery) {
        if (itemSearchQuery.isBlank()) {
            inventoryItems
        } else {
            inventoryItems.filter { item ->
                item.name.contains(itemSearchQuery, ignoreCase = true) ||
                item.category?.contains(itemSearchQuery, ignoreCase = true) == true
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (transaction == null) "Add Transaction" else "Edit Transaction") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row {
                    RadioButton(
                        selected = type == "expense",
                        onClick = { 
                            type = "expense"
                            selectedItemId = null
                        }
                    )
                    Text("Expense", modifier = Modifier.padding(end = 16.dp))
                    RadioButton(
                        selected = type == "sale",
                        onClick = { type = "sale" }
                    )
                    Text("Sale")
                }
                
                // Show inventory item selector for sales
                if (type == "sale") {
                    OutlinedTextField(
                        value = selectedItem?.name ?: "",
                        onValueChange = { },
                        label = { Text("Select Inventory Item") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showItemSelector = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showItemSelector = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Item")
                            }
                        }
                    )
                    
                    if (selectedItem != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { 
                                    quantity = it
                                    // Auto-calculate amount: quantity * wholesale price
                                    val qty = it.toIntOrNull() ?: 1
                                    val totalAmount = qty * selectedItem.wholesalePrice
                                    amount = totalAmount.toString()
                                },
                                label = { Text("Quantity") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                supportingText = {
                                    Text("Available: ${selectedItem.quantity}")
                                },
                                isError = (quantity.toIntOrNull() ?: 0) > selectedItem.quantity
                            )
                        }
                        
                        LaunchedEffect(selectedItem.id) {
                            // Auto-fill description from selected item (only when item changes)
                            if (description.isEmpty() || description == transaction?.description) {
                                description = selectedItem.name
                            }
                            // Auto-calculate amount: quantity * wholesale price
                            val qty = quantity.toIntOrNull() ?: 1
                            val totalAmount = qty * selectedItem.wholesalePrice
                            if (amount.isEmpty() || amount == "0.0" || transaction == null) {
                                amount = totalAmount.toString()
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newTransaction = OutTransaction(
                        id = transaction?.id ?: 0,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        category = category.ifBlank { null },
                        date = date,
                        description = description.ifBlank { null },
                        type = type,
                        relatedItemId = if (type == "sale") selectedItemId else null,
                        quantity = if (type == "sale") quantity.toIntOrNull() ?: 1 else 1
                    )
                    onSave(newTransaction)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    // Inventory Item Selector Dialog
    if (showItemSelector) {
        AlertDialog(
            onDismissRequest = { 
                showItemSelector = false
                itemSearchQuery = ""
            },
            title = { Text("Select Inventory Item") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Search field
                    OutlinedTextField(
                        value = itemSearchQuery,
                        onValueChange = { itemSearchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        placeholder = { Text("Search items...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (itemSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { itemSearchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true
                    )
                    
                    // Items list
                    if (filteredInventoryItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No items found")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                        ) {
                            items(filteredInventoryItems) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            selectedItemId = item.id
                                            showItemSelector = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedItemId == item.id) 
                                            MaterialTheme.colorScheme.primaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Qty: ${item.quantity}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = NumberFormat.getCurrencyInstance(Locale.getDefault())
                                                    .format(item.wholesalePrice),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Wholesale",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showItemSelector = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

