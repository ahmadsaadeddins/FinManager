package com.financialmanager.app.ui.screens.capital

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.financialmanager.app.R
import com.financialmanager.app.data.entities.CapitalTransaction
import com.financialmanager.app.data.entities.CapitalTransactionType
import com.financialmanager.app.ui.components.BottomNavigationBar
import com.financialmanager.app.ui.navigation.Screen
import com.financialmanager.app.ui.theme.MoneyIn
import com.financialmanager.app.ui.theme.MoneyOut
import com.financialmanager.app.util.Formatters
import com.financialmanager.app.util.LocaleHelper
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapitalScreen(
    navController: NavController,
    viewModel: CapitalViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val isRTL = LocaleHelper.isRTL()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<CapitalTransaction?>(null) }
    var showDeleteDialog by remember { mutableStateOf<CapitalTransaction?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.capital)) },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_transaction))
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = Screen.Capital.route,
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
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )

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
                        CapitalTransactionCard(
                            transaction = transaction,
                            currencySymbol = currency.symbol,
                            isRTL = isRTL,
                            onEdit = { editingTransaction = it },
                            onDelete = { showDeleteDialog = it }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog || editingTransaction != null) {
        CapitalTransactionDialog(
            transaction = editingTransaction,
            currencySymbol = currency.symbol,
            isRTL = isRTL,
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
fun CapitalTransactionCard(
    transaction: CapitalTransaction,
    currencySymbol: String,
    isRTL: Boolean,
    onEdit: (CapitalTransaction) -> Unit,
    onDelete: (CapitalTransaction) -> Unit
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val color = if (transaction.type == CapitalTransactionType.INVESTMENT) MoneyIn else MoneyOut

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
                    text = transaction.source,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormatter.format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall
                )
                if (transaction.description != null) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formatters.formatCurrency(transaction.amount, currencySymbol, isRTL),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = transaction.type.value,
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
fun CapitalTransactionDialog(
    transaction: CapitalTransaction?,
    currencySymbol: String = "ج.م",
    isRTL: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (CapitalTransaction) -> Unit
) {
    var amount by remember { mutableStateOf(TextFieldValue(transaction?.amount?.toString() ?: "")) }
    var source by remember { mutableStateOf(TextFieldValue(transaction?.source ?: "")) }
    var description by remember { mutableStateOf(TextFieldValue(transaction?.description ?: "")) }
    var type by remember { mutableStateOf(transaction?.type ?: CapitalTransactionType.INVESTMENT) }
    var date by remember { mutableStateOf(transaction?.date ?: System.currentTimeMillis()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (transaction == null) stringResource(R.string.add_capital)
                else stringResource(R.string.edit_transaction)
            )
        },
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
                    label = { Text("${stringResource(R.string.amount)} ($currencySymbol)") },
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
                    value = source,
                    onValueChange = { source = it },
                    label = { Text(stringResource(R.string.source)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused && source.text.isNotEmpty()) {
                                source = source.copy(selection = TextRange(0, source.text.length))
                            }
                        },
                    singleLine = true
                )
                Row {
                    RadioButton(
                        selected = type == CapitalTransactionType.INVESTMENT,
                        onClick = { type = CapitalTransactionType.INVESTMENT }
                    )
                    Text(stringResource(R.string.investment), modifier = Modifier.padding(end = 16.dp))
                    RadioButton(
                        selected = type == CapitalTransactionType.WITHDRAWAL,
                        onClick = { type = CapitalTransactionType.WITHDRAWAL }
                    )
                    Text(stringResource(R.string.withdrawal))
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
                    val newTransaction = CapitalTransaction(
                        id = transaction?.id ?: 0,
                        amount = amount.text.toDoubleOrNull() ?: 0.0,
                        source = source.text,
                        date = date,
                        description = description.text.ifBlank { null },
                        type = type
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
}

