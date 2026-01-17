package com.financialmanager.app.ui.screens.people

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.runtime.LaunchedEffect
import com.financialmanager.app.R
import com.financialmanager.app.data.entities.PersonTransaction
import com.financialmanager.app.data.entities.PersonTransactionType
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
fun PersonDetailScreen(
    navController: NavController,
    personId: Long,
    viewModel: PersonDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(personId) {
        viewModel.setPersonId(personId)
    }
    
    val person by viewModel.person.collectAsState(initial = null)
    val transactions by viewModel.getTransactions(personId).collectAsState(initial = emptyList())
    val balance by viewModel.getBalance(personId).collectAsState(initial = null)
    val currency by viewModel.currency.collectAsState()
    val isRTL = LocaleHelper.isRTL()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<PersonTransaction?>(null) }
    var showDeleteDialog by remember { mutableStateOf<PersonTransaction?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(person?.name ?: stringResource(R.string.person_details)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_transaction))
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = Screen.People.route,
                onNavigate = { route -> navController.navigate(route) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Balance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if ((balance ?: 0.0) >= 0) MoneyIn.copy(alpha = 0.1f) else MoneyOut.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.balance),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = Formatters.formatCurrency(balance, currency.symbol, isRTL),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if ((balance ?: 0.0) >= 0) MoneyIn else MoneyOut
                    )
                    Text(
                        text = if ((balance ?: 0.0) >= 0) stringResource(R.string.they_owe_you) else stringResource(R.string.you_owe_them),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Transactions List
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_transactions_found))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { transaction ->
                        PersonTransactionCard(
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
        PersonTransactionDialog(
            personId = personId,
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
fun PersonTransactionCard(
    transaction: PersonTransaction,
    currencySymbol: String,
    isRTL: Boolean,
    onEdit: (PersonTransaction) -> Unit,
    onDelete: (PersonTransaction) -> Unit
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val color = if (transaction.type == PersonTransactionType.THEY_OWE_ME) MoneyIn else MoneyOut

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
                    text = Formatters.formatCurrency(transaction.amount, currencySymbol, isRTL),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = if (transaction.type == PersonTransactionType.THEY_OWE_ME) stringResource(R.string.they_owe) else stringResource(R.string.you_owe),
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
fun PersonTransactionDialog(
    personId: Long,
    transaction: PersonTransaction?,
    currencySymbol: String = "ج.م",
    isRTL: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (PersonTransaction) -> Unit
) {
    var amount by remember { mutableStateOf(TextFieldValue(transaction?.amount?.toString() ?: "")) }
    var category by remember { mutableStateOf(TextFieldValue(transaction?.category ?: "")) }
    var description by remember { mutableStateOf(TextFieldValue(transaction?.description ?: "")) }
    var type by remember { mutableStateOf(transaction?.type ?: PersonTransactionType.THEY_OWE_ME) }
    var date by remember { mutableStateOf(transaction?.date ?: System.currentTimeMillis()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (transaction == null) stringResource(R.string.add_transaction) else stringResource(R.string.edit_transaction)) },
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
                        selected = type == PersonTransactionType.THEY_OWE_ME,
                        onClick = { type = PersonTransactionType.THEY_OWE_ME }
                    )
                    Text(stringResource(R.string.they_owe_me), modifier = Modifier.padding(end = 16.dp))
                    RadioButton(
                        selected = type == PersonTransactionType.I_OWE_THEM,
                        onClick = { type = PersonTransactionType.I_OWE_THEM }
                    )
                    Text(stringResource(R.string.i_owe_them))
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
                    val newTransaction = PersonTransaction(
                        id = transaction?.id ?: 0,
                        personId = personId,
                        amount = amount.text.toDoubleOrNull() ?: 0.0,
                        date = date,
                        description = description.text.ifBlank { null },
                        type = type,
                        category = category.text.ifBlank { null }
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

