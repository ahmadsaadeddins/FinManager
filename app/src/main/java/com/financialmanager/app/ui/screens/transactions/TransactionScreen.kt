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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.financialmanager.app.R
import com.financialmanager.app.data.entities.InventoryItem
import com.financialmanager.app.data.entities.OutTransaction
import com.financialmanager.app.data.entities.TransactionType
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
                title = { Text(stringResource(R.string.transactions)) },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.SalesArchive.route) }) {
                        Icon(Icons.Default.Archive, contentDescription = stringResource(R.string.sales_archive))
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_transaction))
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
                placeholder = { Text(stringResource(R.string.search_transactions)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.cancel))
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
                    label = { Text(stringResource(R.string.all)) }
                )
                FilterChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = { viewModel.setType(TransactionType.EXPENSE) },
                    label = { Text(stringResource(R.string.expenses)) }
                )
                FilterChip(
                    selected = selectedType == TransactionType.SALE,
                    onClick = { viewModel.setType(TransactionType.SALE) },
                    label = { Text(stringResource(R.string.sales)) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_transactions))
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
            title = { Text(stringResource(R.string.delete_transaction)) },
            text = { Text(stringResource(R.string.delete_transaction_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(transaction)
                        showDeleteDialog = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel))
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
    val color = if (transaction.type == TransactionType.SALE) MoneyIn else MoneyOut

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
                    text = transaction.description ?: stringResource(R.string.no_description),
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
                    text = if (transaction.type == TransactionType.SALE) stringResource(R.string.sale) else stringResource(R.string.expense),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row {
                IconButton(onClick = { onEdit(transaction) }) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                }
                IconButton(onClick = { onDelete(transaction) }) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
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
    var amount by remember { mutableStateOf(TextFieldValue(transaction?.amount?.toString() ?: "")) }
    var category by remember { mutableStateOf(TextFieldValue(transaction?.category ?: "")) }
    var description by remember { mutableStateOf(TextFieldValue(transaction?.description ?: "")) }
    var type by remember { mutableStateOf(transaction?.type ?: TransactionType.EXPENSE) }
    var date by remember { mutableStateOf(transaction?.date ?: System.currentTimeMillis()) }
    var selectedItemId by remember { mutableStateOf<Long?>(transaction?.relatedItemId) }
    var quantity by remember { mutableStateOf(TextFieldValue(transaction?.quantity?.toString() ?: "1")) }
    var showItemSelector by remember { mutableStateOf(false) }
    var itemSearchQuery by remember { mutableStateOf("") }
    
    val inventoryItems by viewModel.inventoryItems.collectAsState()
    val selectedItem = inventoryItems.find { item: InventoryItem -> item.id == selectedItemId }
    
    // Filter inventory items based on search query
    val filteredInventoryItems = remember(inventoryItems, itemSearchQuery) {
        if (itemSearchQuery.isBlank()) {
            inventoryItems
        } else {
            inventoryItems.filter { item: InventoryItem ->
                item.name.contains(itemSearchQuery, ignoreCase = true) ||
                (item.category?.contains(itemSearchQuery, ignoreCase = true) == true)
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
                    label = { Text(stringResource(R.string.amount)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused && amount.text.isNotEmpty()) {
                                amount = amount.copy(selection = TextRange(0, amount.text.length))
                            }
                        },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(stringResource(R.string.category)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused && category.text.isNotEmpty()) {
                                category = category.copy(selection = TextRange(0, category.text.length))
                            }
                        },
                    singleLine = true
                )
                Row {
                    RadioButton(
                        selected = type == TransactionType.EXPENSE,
                        onClick = { 
                            type = TransactionType.EXPENSE
                            selectedItemId = null
                        }
                    )
                    Text(stringResource(R.string.expense), modifier = Modifier.padding(end = 16.dp))
                    RadioButton(
                        selected = type == TransactionType.SALE,
                        onClick = { type = TransactionType.SALE }
                    )
                    Text(stringResource(R.string.sale))
                }
                
                // Show inventory item selector for sales
                if (type == TransactionType.SALE) {
                    OutlinedTextField(
                        value = selectedItem?.name ?: "",
                        onValueChange = { },
                        label = { Text(stringResource(R.string.select_inventory_item)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showItemSelector = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showItemSelector = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = stringResource(R.string.select_inventory_item))
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
                                    val qty = it.text.toIntOrNull() ?: 1
                                    val totalAmount = qty * (selectedItem?.wholesalePrice ?: 0.0)
                                    amount = TextFieldValue(totalAmount.toString())
                                },
                                label = { Text(stringResource(R.string.quantity)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused && quantity.text.isNotEmpty()) {
                                            quantity = quantity.copy(selection = TextRange(0, quantity.text.length))
                                        }
                                    },
                                singleLine = true,
                                supportingText = {
                                    Text(stringResource(R.string.available_format, selectedItem?.quantity ?: 0))
                                },
                                isError = (quantity.text.toIntOrNull() ?: 0) > (selectedItem?.quantity ?: 0)
                            )
                        }
                        
                        LaunchedEffect(selectedItem?.id) {
                            // Auto-fill description from selected item (only when item changes)
                            if (description.text.isEmpty() || description.text == transaction?.description) {
                                description = TextFieldValue(selectedItem?.name ?: "")
                            }
                            // Auto-calculate amount: quantity * wholesale price
                            val qty = quantity.text.toIntOrNull() ?: 1
                            val totalAmount = qty * (selectedItem?.wholesalePrice ?: 0.0)
                            if (amount.text.isEmpty() || amount.text == "0.0" || transaction == null) {
                                amount = TextFieldValue(totalAmount.toString())
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused && description.text.isNotEmpty()) {
                                description = description.copy(selection = TextRange(0, description.text.length))
                            }
                        },
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newTransaction = OutTransaction(
                        id = transaction?.id ?: 0,
                        amount = amount.text.toDoubleOrNull() ?: 0.0,
                        category = category.text.ifBlank { null },
                        date = date,
                        description = description.text.ifBlank { null },
                        type = type,
                        relatedItemId = if (type == TransactionType.SALE) selectedItemId else null,
                        quantity = if (type == TransactionType.SALE) quantity.text.toIntOrNull() ?: 1 else 1
                    )
                    onSave(newTransaction)
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
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
            title = { Text(stringResource(R.string.select_inventory_item)) },
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
                        placeholder = { Text(stringResource(R.string.search_items)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (itemSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { itemSearchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.cancel))
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
                            Text(stringResource(R.string.no_items))
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
                                                text = stringResource(R.string.qty_format, item.quantity),
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
                                                text = stringResource(R.string.wholesale),
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
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

