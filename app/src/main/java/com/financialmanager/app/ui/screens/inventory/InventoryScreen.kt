package com.financialmanager.app.ui.screens.inventory

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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.financialmanager.app.data.entities.InventoryItem
import com.financialmanager.app.ui.components.BarcodeScannerDialog
import com.financialmanager.app.ui.components.BottomNavigationBar
import com.financialmanager.app.ui.navigation.Screen
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    navController: NavController,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<InventoryItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf<InventoryItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory") },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Item")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = Screen.Inventory.route,
                onNavigate = { route -> navController.navigate(route) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search and Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search items...") },
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

            // Category Filter
            if (categories.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.setCategory(null) },
                        label = { Text("All") }
                    )
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { viewModel.setCategory(category) },
                            label = { Text(category) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Items List
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No items found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { item ->
                        InventoryItemCard(
                            item = item,
                            onEdit = { editingItem = it },
                            onDelete = { showDeleteDialog = it }
                        )
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog || editingItem != null) {
        InventoryItemDialog(
            item = editingItem,
            onDismiss = {
                showAddDialog = false
                editingItem = null
            },
            onSave = { item ->
                if (editingItem != null) {
                    viewModel.updateItem(item)
                } else {
                    viewModel.insertItem(item)
                }
                showAddDialog = false
                editingItem = null
            }
        )
    }

    // Delete Confirmation Dialog
            showDeleteDialog?.let { item ->
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    title = { Text("Delete Item") },
                    text = { Text("Are you sure you want to delete ${item.name}?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteItem(item)
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
fun InventoryItemCard(
    item: InventoryItem,
    onEdit: (InventoryItem) -> Unit,
    onDelete: (InventoryItem) -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (item.category != null) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row {
                    IconButton(onClick = { onEdit(item) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { onDelete(item) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Quantity: ${item.quantity}")
                Text("Wholesale: ${formatter.format(item.wholesalePrice)}")
            }
            if (item.barcode != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Barcode: ${item.barcode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (item.notes != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryItemDialog(
    item: InventoryItem?,
    onDismiss: () -> Unit,
    onSave: (InventoryItem) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue(item?.name ?: "")) }
    var category by remember { mutableStateOf(TextFieldValue(item?.category ?: "")) }
    var quantity by remember { mutableStateOf(TextFieldValue(item?.quantity?.toString() ?: "0")) }
    var purchasePrice by remember { mutableStateOf(TextFieldValue(item?.purchasePrice?.toString() ?: "0.0")) }
    var sellingPrice by remember { mutableStateOf(TextFieldValue(item?.sellingPrice?.toString() ?: "0.0")) }
    var wholesalePrice by remember { mutableStateOf(TextFieldValue(item?.wholesalePrice?.toString() ?: "0.0")) }
    var barcode by remember { mutableStateOf(TextFieldValue(item?.barcode ?: "")) }
    var notes by remember { mutableStateOf(TextFieldValue(item?.notes ?: "")) }
    
    var nameFocused by remember { mutableStateOf(false) }
    var categoryFocused by remember { mutableStateOf(false) }
    var quantityFocused by remember { mutableStateOf(false) }
    var purchasePriceFocused by remember { mutableStateOf(false) }
    var sellingPriceFocused by remember { mutableStateOf(false) }
    var wholesalePriceFocused by remember { mutableStateOf(false) }
    var barcodeFocused by remember { mutableStateOf(false) }
    var notesFocused by remember { mutableStateOf(false) }
    
    var showBarcodeScanner by remember { mutableStateOf(false) }
    
    LaunchedEffect(nameFocused) {
        if (nameFocused && name.text.isNotEmpty()) {
            name = name.copy(selection = TextRange(0, name.text.length))
        }
    }
    
    LaunchedEffect(categoryFocused) {
        if (categoryFocused && category.text.isNotEmpty()) {
            category = category.copy(selection = TextRange(0, category.text.length))
        }
    }
    
    LaunchedEffect(quantityFocused) {
        if (quantityFocused && quantity.text.isNotEmpty()) {
            quantity = quantity.copy(selection = TextRange(0, quantity.text.length))
        }
    }
    
    LaunchedEffect(purchasePriceFocused) {
        if (purchasePriceFocused && purchasePrice.text.isNotEmpty()) {
            purchasePrice = purchasePrice.copy(selection = TextRange(0, purchasePrice.text.length))
        }
    }
    
    LaunchedEffect(sellingPriceFocused) {
        if (sellingPriceFocused && sellingPrice.text.isNotEmpty()) {
            sellingPrice = sellingPrice.copy(selection = TextRange(0, sellingPrice.text.length))
        }
    }
    
    LaunchedEffect(wholesalePriceFocused) {
        if (wholesalePriceFocused && wholesalePrice.text.isNotEmpty()) {
            wholesalePrice = wholesalePrice.copy(selection = TextRange(0, wholesalePrice.text.length))
        }
    }
    
    LaunchedEffect(barcodeFocused) {
        if (barcodeFocused && barcode.text.isNotEmpty()) {
            barcode = barcode.copy(selection = TextRange(0, barcode.text.length))
        }
    }
    
    LaunchedEffect(notesFocused) {
        if (notesFocused && notes.text.isNotEmpty()) {
            notes = notes.copy(selection = TextRange(0, notes.text.length))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Add Item" else "Edit Item") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            nameFocused = focusState.isFocused
                        },
                    singleLine = true
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            categoryFocused = focusState.isFocused
                        },
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                quantityFocused = focusState.isFocused
                            },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = purchasePrice,
                        onValueChange = { purchasePrice = it },
                        label = { Text("Purchase Price") },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                purchasePriceFocused = focusState.isFocused
                            },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = sellingPrice,
                        onValueChange = { sellingPrice = it },
                        label = { Text("Selling Price") },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                sellingPriceFocused = focusState.isFocused
                            },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                OutlinedTextField(
                    value = wholesalePrice,
                    onValueChange = { wholesalePrice = it },
                    label = { Text("Wholesale Price") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            wholesalePriceFocused = focusState.isFocused
                        },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Barcode") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            barcodeFocused = focusState.isFocused
                        },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showBarcodeScanner = true }) {
                            Icon(Icons.Default.Camera, contentDescription = "Scan Barcode")
                        }
                    }
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            notesFocused = focusState.isFocused
                        },
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newItem = InventoryItem(
                        id = item?.id ?: 0,
                        name = name.text,
                        category = category.text.ifBlank { null },
                        quantity = quantity.text.toIntOrNull() ?: 0,
                        purchasePrice = purchasePrice.text.toDoubleOrNull() ?: 0.0,
                        sellingPrice = sellingPrice.text.toDoubleOrNull() ?: 0.0,
                        wholesalePrice = wholesalePrice.text.toDoubleOrNull() ?: 0.0,
                        barcode = barcode.text.ifBlank { null },
                        notes = notes.text.ifBlank { null }
                    )
                    onSave(newItem)
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
    
    // Barcode Scanner Dialog
    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            onBarcodeScanned = { scannedBarcode ->
                barcode = TextFieldValue(scannedBarcode)
                showBarcodeScanner = false
            },
            onDismiss = { showBarcodeScanner = false }
        )
    }
}

