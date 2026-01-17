package com.financialmanager.app.ui.screens.archive

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.financialmanager.app.R
import com.financialmanager.app.data.entities.OutTransaction
import com.financialmanager.app.ui.components.BottomNavigationBar
import com.financialmanager.app.ui.navigation.Screen
import com.financialmanager.app.util.Formatters
import com.financialmanager.app.util.LocaleHelper
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
    val currency by viewModel.currency.collectAsState()
    val isRTL = LocaleHelper.isRTL()

    var showArchiveDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sales_archive)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (selectedTab == 0 && activeSalesCount > 0) {
                        IconButton(
                            onClick = { showArchiveDialog = true },
                            enabled = uiState !is SalesArchiveUiState.ArchivingInProgress
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = stringResource(R.string.archive_all_sales))
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
                        message = stringResource(currentState.messageRes, currentState.archivedCount),
                        icon = Icons.Default.CheckCircle,
                        color = MaterialTheme.colorScheme.primary,
                        onDismiss = { viewModel.clearMessage() }
                    )
                }
                is SalesArchiveUiState.ArchiveError -> {
                    AlertCard(
                        message = "${stringResource(currentState.messageRes)}${currentState.dynamicMessage?.let { ": $it" } ?: ""}",
                        icon = Icons.Default.Error,
                        color = MaterialTheme.colorScheme.error,
                        onDismiss = { viewModel.clearMessage() }
                    )
                }
                is SalesArchiveUiState.UnarchiveSuccess -> {
                    AlertCard(
                        message = stringResource(currentState.messageRes),
                        icon = Icons.Default.CheckCircle,
                        color = MaterialTheme.colorScheme.primary,
                        onDismiss = { viewModel.clearMessage() }
                    )
                }
                is SalesArchiveUiState.UnarchiveError -> {
                    AlertCard(
                        message = "${stringResource(currentState.messageRes)}${currentState.dynamicMessage?.let { ": $it" } ?: ""}",
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
                            Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.active_sales),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Formatters.formatCurrency(totalActiveSales ?: 0.0, currency.symbol, isRTL),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.transactions_count, activeSalesCount),
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
                            text = stringResource(R.string.archived_sales),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Formatters.formatCurrency(totalArchivedSales ?: 0.0, currency.symbol, isRTL),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = stringResource(R.string.transactions_count, archivedSales.size),
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
                    text = { Text(stringResource(R.string.active_sales)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.archived_sales)) }
                )
            }

            // Content based on selected tab
            when (selectedTab) {
                0 -> {
                    // Active sales
                    if (activeSales.isEmpty()) {
                        EmptyStateCard(
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            title = stringResource(R.string.no_active_sales_title),
                            description = stringResource(R.string.no_active_sales_desc)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(activeSales) { sale ->
                                SaleTransactionCard(
                                    transaction = sale,
                                    currencySymbol = currency.symbol,
                                    isRTL = isRTL,
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
                            title = stringResource(R.string.no_archived_sales_title),
                            description = stringResource(R.string.no_archived_sales_desc)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(archivedSales) { sale ->
                                SaleTransactionCard(
                                    transaction = sale,
                                    currencySymbol = currency.symbol,
                                    isRTL = isRTL,
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
            title = { Text(stringResource(R.string.archive_all_sales)) },
            text = {
                Column {
                    Text(stringResource(R.string.archive_all_info))
                    Spacer(Modifier.height(8.dp))
                    Text("• ${stringResource(R.string.total_format, Formatters.formatCurrency(totalActiveSales ?: 0.0, currency.symbol, isRTL))}")
                    Text("• ${stringResource(R.string.transactions_count, activeSalesCount)}")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.archive_reset_info),
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
                    Text(stringResource(R.string.archive))
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SaleTransactionCard(
    transaction: OutTransaction,
    currencySymbol: String,
    isRTL: Boolean,
    isArchived: Boolean,
    onAction: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

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
                        text = transaction.description ?: stringResource(R.string.sale),
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
                            text = stringResource(R.string.archived_format, dateFormat.format(Date(transaction.archivedAt))),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatters.formatCurrency(transaction.amount, currencySymbol, isRTL),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (transaction.quantity > 1) {
                        Text(
                            text = stringResource(R.string.qty_format, transaction.quantity),
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
                        Text(stringResource(R.string.restore))
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
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.dismiss))
            }
        }
    }
}