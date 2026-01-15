package com.financialmanager.app.ui.screens.archive

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.financialmanager.app.data.entities.OutTransaction
import com.financialmanager.app.ui.components.BottomNavigationBar
import com.financialmanager.app.ui.navigation.Screen
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesArchiveScreen(
    navController: NavController,
    viewModel: SalesArchiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeSales by viewModel.activeSales.collectAsState(initial = emptyList())
    val totalActiveSales by viewModel.totalActiveSales.collectAsState(initial = 0.0)
    val activeSalesCount by viewModel.activeSalesCount.collectAsState(initial = 0)
    val archivedSales by viewModel.archivedSales.collectAsState(initial = emptyList())
    val totalArchivedSales by viewModel.totalArchivedSales.collectAsState(initial = 0.0)

    var showArchiveDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    val currencyFormat = NumberFormat.getCurrencyInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Archive") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedTab == 0 && activeSalesCount > 0) {
                        IconButton(
                            onClick = { showArchiveDialog = true },
                            enabled = uiState !is SalesArchiveUiState.ArchivingInProgress
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = "Archive All Sales")
                        }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status messages
            when (val currentState = uiState) {
                is SalesArchiveUiState.ArchiveSuccess -> {
                    AlertCard(
                        message = currentState.message,
                        icon = Icons.Default.CheckCircle,
                        color = MaterialTheme.colorScheme.primary,
                        onDismiss = { viewModel.clearMessage() }
                    )
                }
                is SalesArchiveUiState.ArchiveError -> {
                    AlertCard(
                        message = currentState.message,
                        icon = Icons.Default.Error,
                        color = MaterialTheme.colorScheme.error,
                        onDismiss = { viewModel.clearMessage() }
                    )
                }
                is SalesArchiveUiState.UnarchiveSuccess -> {
                    AlertCard(
                        message = currentState.message,
                        icon = Icons.Default.CheckCircle,
                        color = MaterialTheme.colorScheme.primary,
                        onDismiss = { viewModel.clearMessage() }
                    )
                }
                is SalesArchiveUiState.UnarchiveError -> {
                    AlertCard(
                        message = currentState.message,
                        icon = Icons.Default.Error,
                        color = MaterialTheme.colorScheme.error,
                        onDismiss = { viewModel.clearMessage() }
                    )
                }
                else -> {}
            }

            // Summary cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Active sales card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Active Sales",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currencyFormat.format(totalActiveSales ?: 0.0),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$activeSalesCount transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Archived sales card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Archive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Archived Sales",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currencyFormat.format(totalArchivedSales ?: 0.0),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "${archivedSales.size} transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Tab selector
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active Sales") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Archived Sales") }
                )
            }

            // Content based on selected tab
            when (selectedTab) {
                0 -> {
                    // Active sales
                    if (activeSales.isEmpty()) {
                        EmptyStateCard(
                            icon = Icons.Default.TrendingUp,
                            title = "No Active Sales",
                            description = "No sales transactions found. Add some sales to see them here."
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(activeSales) { sale ->
                                SaleTransactionCard(
                                    transaction = sale,
                                    isArchived = false,
                                    onAction = { /* No action for active sales */ }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Archived sales
                    if (archivedSales.isEmpty()) {
                        EmptyStateCard(
                            icon = Icons.Default.Archive,
                            title = "No Archived Sales",
                            description = "No archived sales found. Archive some sales to see them here."
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(archivedSales) { sale ->
                                SaleTransactionCard(
                                    transaction = sale,
                                    isArchived = true,
                                    onAction = { viewModel.unarchiveTransaction(sale) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Archive confirmation dialog
    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = { Text("Archive All Sales") },
            text = {
                Column {
                    Text("This will archive all current sales transactions:")
                    Spacer(Modifier.height(8.dp))
                    Text("• Total: ${currencyFormat.format(totalActiveSales ?: 0.0)}")
                    Text("• Count: $activeSalesCount transactions")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "After archiving, your current sales total will be reset to $0.00",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showArchiveDialog = false
                        viewModel.archiveAllSales()
                    },
                    enabled = uiState !is SalesArchiveUiState.ArchivingInProgress
                ) {
                    if (uiState is SalesArchiveUiState.ArchivingInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Archive")
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SaleTransactionCard(
    transaction: OutTransaction,
    isArchived: Boolean,
    onAction: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val currencyFormat = NumberFormat.getCurrencyInstance()

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.description ?: "Sale",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    transaction.category?.let { category ->
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = dateFormat.format(Date(transaction.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isArchived && transaction.archivedAt != null) {
                        Text(
                            text = "Archived: ${dateFormat.format(Date(transaction.archivedAt))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currencyFormat.format(transaction.amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (transaction.quantity > 1) {
                        Text(
                            text = "Qty: ${transaction.quantity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (isArchived) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onAction,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Unarchive,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Restore")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AlertCard(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss")
            }
        }
    }
}