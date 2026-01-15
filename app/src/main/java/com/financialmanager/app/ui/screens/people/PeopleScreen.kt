package com.financialmanager.app.ui.screens.people

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
import com.financialmanager.app.data.entities.PersonAccount
import com.financialmanager.app.ui.components.BottomNavigationBar
import com.financialmanager.app.ui.navigation.Screen
import com.financialmanager.app.ui.theme.MoneyIn
import com.financialmanager.app.ui.theme.MoneyOut
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(
    navController: NavController,
    viewModel: PeopleViewModel = hiltViewModel()
) {
    val peopleWithBalances by viewModel.peopleWithBalances.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val peopleCount by viewModel.peopleCount.collectAsState()
    val positiveBalanceCount by viewModel.positiveBalanceCount.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingPerson by remember { mutableStateOf<PersonAccount?>(null) }
    var showDeleteDialog by remember { mutableStateOf<PersonAccount?>(null) }
    var showTransferDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("People") },
                actions = {
                    IconButton(onClick = { showTransferDialog = true }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Transfer Between People")
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Person")
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
            // People count display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MoneyIn.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total People",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Positive Balance: $positiveBalanceCount",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MoneyIn
                        )
                    }
                    Text(
                        text = peopleCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MoneyIn
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search people...") },
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

            if (peopleWithBalances.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No people found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(peopleWithBalances) { personWithBalance ->
                        PersonCard(
                            personWithBalance = personWithBalance,
                            onNavigate = { 
                                viewModel.trackPersonUsage(personWithBalance.person.id)
                                navController.navigate(Screen.PersonDetail.createRoute(personWithBalance.person.id))
                            },
                            onEdit = { editingPerson = it },
                            onDelete = { showDeleteDialog = it }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog || editingPerson != null) {
        PersonDialog(
            person = editingPerson,
            onDismiss = {
                showAddDialog = false
                editingPerson = null
            },
            onSave = { person ->
                if (editingPerson != null) {
                    viewModel.updatePerson(person)
                } else {
                    viewModel.insertPerson(person)
                }
                showAddDialog = false
                editingPerson = null
            }
        )
    }

    showDeleteDialog?.let { person ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Person") },
            text = { Text("Are you sure you want to delete ${person.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePerson(person)
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

    if (showTransferDialog) {
        TransferDialog(
            people = peopleWithBalances,
            onDismiss = { showTransferDialog = false },
            onTransfer = { fromPersonId, toPersonId, amount, description ->
                viewModel.transferBetweenPeople(fromPersonId, toPersonId, amount, description)
                showTransferDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonCard(
    personWithBalance: PersonWithBalance,
    onNavigate: () -> Unit,
    onEdit: (PersonAccount) -> Unit,
    onDelete: (PersonAccount) -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val person = personWithBalance.person
    val balance = personWithBalance.balance
    
    Card(
        onClick = onNavigate,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = person.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        // Usage indicator for frequently used people
                        if (person.usageCount > 5) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Frequently Used",
                                tint = MoneyIn,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = formatter.format(balance),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (balance >= 0) MoneyIn else MoneyOut
                    )
                }
                if (person.phone != null) {
                    Text(
                        text = person.phone,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (person.email != null) {
                    Text(
                        text = person.email,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Row {
                IconButton(onClick = { onEdit(person) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { onDelete(person) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDialog(
    person: PersonAccount?,
    onDismiss: () -> Unit,
    onSave: (PersonAccount) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue(person?.name ?: "")) }
    var phone by remember { mutableStateOf(TextFieldValue(person?.phone ?: "")) }
    var email by remember { mutableStateOf(TextFieldValue(person?.email ?: "")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (person == null) "Add Person" else "Edit Person") },
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
                    label = { Text("Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused && name.text.isNotEmpty()) {
                                name = name.copy(selection = TextRange(0, name.text.length))
                            }
                        },
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused && phone.text.isNotEmpty()) {
                                phone = phone.copy(selection = TextRange(0, phone.text.length))
                            }
                        },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused && email.text.isNotEmpty()) {
                                email = email.copy(selection = TextRange(0, email.text.length))
                            }
                        },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newPerson = PersonAccount(
                        id = person?.id ?: 0,
                        name = name.text,
                        phone = phone.text.ifBlank { null },
                        email = email.text.ifBlank { null },
                        createdAt = person?.createdAt ?: System.currentTimeMillis(),
                        usageCount = person?.usageCount ?: 0,
                        lastUsedAt = person?.lastUsedAt ?: System.currentTimeMillis()
                    )
                    onSave(newPerson)
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
}

@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
fun PeopleCountCardPreview() {
    MaterialTheme {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MoneyIn.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total People",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Positive Balance: 8",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MoneyIn
                    )
                }
                Text(
                    text = "15",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MoneyIn
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferDialog(
    people: List<PersonWithBalance>,
    onDismiss: () -> Unit,
    onTransfer: (fromPersonId: Long, toPersonId: Long, amount: Double, description: String) -> Unit
) {
    var fromPersonId by remember { mutableStateOf<Long?>(null) }
    var toPersonId by remember { mutableStateOf<Long?>(null) }
    var amount by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("Transfer")) }
    var fromSearchQuery by remember { mutableStateOf("") }
    var toSearchQuery by remember { mutableStateOf("") }
    var showFromSuggestions by remember { mutableStateOf(false) }
    var showToSuggestions by remember { mutableStateOf(false) }

    val fromPerson = people.find { it.person.id == fromPersonId }
    val toPerson = people.find { it.person.id == toPersonId }

    // Filter people based on search queries
    val filteredFromPeople = people.filter { 
        it.person.name.contains(fromSearchQuery, ignoreCase = true) 
    }.take(5) // Limit to 5 results to avoid long lists

    val filteredToPeople = people.filter { 
        it.person.id != fromPersonId && 
        it.person.name.contains(toSearchQuery, ignoreCase = true) 
    }.take(5) // Limit to 5 results

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transfer Between People") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // From Person Search Field
                Column {
                    OutlinedTextField(
                        value = fromPerson?.person?.name ?: fromSearchQuery,
                        onValueChange = { 
                            fromSearchQuery = it
                            if (fromPerson?.person?.name != it) {
                                fromPersonId = null
                            }
                            showFromSuggestions = it.isNotEmpty() && fromPersonId == null
                        },
                        label = { Text("From Person") },
                        trailingIcon = { 
                            if (fromSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { 
                                    fromSearchQuery = ""
                                    fromPersonId = null
                                    showFromSuggestions = false
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // From Person Suggestions
                    if (showFromSuggestions && filteredFromPeople.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column {
                                filteredFromPeople.forEach { personWithBalance ->
                                    TextButton(
                                        onClick = {
                                            fromPersonId = personWithBalance.person.id
                                            fromSearchQuery = personWithBalance.person.name
                                            showFromSuggestions = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                personWithBalance.person.name,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                NumberFormat.getCurrencyInstance().format(personWithBalance.balance),
                                                color = if (personWithBalance.balance >= 0) MoneyIn else MoneyOut
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // To Person Search Field
                Column {
                    OutlinedTextField(
                        value = toPerson?.person?.name ?: toSearchQuery,
                        onValueChange = { 
                            toSearchQuery = it
                            if (toPerson?.person?.name != it) {
                                toPersonId = null
                            }
                            showToSuggestions = it.isNotEmpty() && toPersonId == null
                        },
                        label = { Text("To Person") },
                        trailingIcon = { 
                            if (toSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { 
                                    toSearchQuery = ""
                                    toPersonId = null
                                    showToSuggestions = false
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // To Person Suggestions
                    if (showToSuggestions && filteredToPeople.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column {
                                filteredToPeople.forEach { personWithBalance ->
                                    TextButton(
                                        onClick = {
                                            toPersonId = personWithBalance.person.id
                                            toSearchQuery = personWithBalance.person.name
                                            showToSuggestions = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                personWithBalance.person.name,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                NumberFormat.getCurrencyInstance().format(personWithBalance.balance),
                                                color = if (personWithBalance.balance >= 0) MoneyIn else MoneyOut
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Amount Field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it
                        // Hide suggestions when user focuses on amount field
                        showFromSuggestions = false
                        showToSuggestions = false
                    },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { 
                        description = it
                        // Hide suggestions when user focuses on description field
                        showFromSuggestions = false
                        showToSuggestions = false
                    },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Transfer Summary
                if (fromPerson != null && toPerson != null && amount.text.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                "Transfer Summary:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text("${fromPerson.person.name} → ${toPerson.person.name}")
                            Text("Amount: ${amount.text}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.text.toDoubleOrNull()
                    if (fromPersonId != null && toPersonId != null && amountValue != null && amountValue > 0) {
                        onTransfer(fromPersonId!!, toPersonId!!, amountValue, description.text)
                    }
                },
                enabled = fromPersonId != null && toPersonId != null && 
                         amount.text.toDoubleOrNull()?.let { it > 0 } == true
            ) {
                Text("Transfer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}