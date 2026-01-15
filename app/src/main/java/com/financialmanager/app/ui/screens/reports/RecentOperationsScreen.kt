package com.financialmanager.app.ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.financialmanager.app.data.entities.OperationType
import com.financialmanager.app.data.entities.RecentOperation
import com.financialmanager.app.ui.components.BottomNavigationBar
import com.financialmanager.app.ui.navigation.Screen
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentOperationsScreen(
    navController: NavController,
    viewModel: RecentOperationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val operations by viewModel.operations.collectAsState()

    // Show alert dialogs for messages
    LaunchedEffect(uiState) {
        // Messages will be shown in UI cards
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recent Operations") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadRecentOperations() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = Screen.Reports.route,
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
                is RecentOperationsUiState.DeleteSuccess -> {
                    AlertCard(
                        message = currentState.message,
                        icon = Icons.Default.CheckCircle,
                        color = MaterialTheme.colorScheme.primary,
                        onDismiss = { viewModel.clearMessage() }
                    )
                }
                is RecentOperationsUiState.DeleteError -> {
                    AlertCard(
                        message = currentState.message,
                        icon = Icons.Default.Error,
                        color = MaterialTheme.colorScheme.error,
                        onDismiss = { viewModel.clearMessage() }
                    )
                }
                is RecentOperationsUiState.Error -> {
                    AlertCard(
                        message = currentState.message,
                        icon = Icons.Default.Error,
                        color = MaterialTheme.colorScheme.error,
                        onDismiss = { viewModel.clearMessage() }
                    )
                }
                else -> {}
            }

            // Summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Recent Operations",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${operations.size} operations found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Operations list
            when (uiState) {
                is RecentOperationsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is RecentOperationsUiState.Success -> {
                    if (operations.isEmpty()) {
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
                                    Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "No recent operations",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Start adding transactions, people, or inventory items",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(operations) { operation ->
                                OperationCard(
                                    operation = operation,
                                    onDelete = { viewModel.deleteOperation(operation) }
                                )
                            }
                        }
                    }
                }
                else -> {
                    // Error states are handled by alert cards above
                }
            }
        }
    }
}

@Composable
fun OperationCard(
    operation: RecentOperation,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Operation type icon
                    Icon(
                        imageVector = getOperationIcon(operation.type),
                        contentDescription = null,
                        tint = getOperationColor(operation.type),
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = operation.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = operation.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = dateFormat.format(Date(operation.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Amount (if applicable)
                operation.amount?.let { amount ->
                    Text(
                        text = currencyFormat.format(amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (operation.type == OperationType.OUT_TRANSACTION && 
                                   (operation.entityData as? com.financialmanager.app.data.entities.OutTransaction)?.type == "expense") {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (operation.canDelete) {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.size(width = 100.dp, height = 36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Delete", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
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

@Composable
fun getOperationIcon(type: OperationType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        OperationType.PERSON_ADDED -> Icons.Default.PersonAdd
        OperationType.PERSON_TRANSACTION -> Icons.Default.SwapHoriz
        OperationType.INVENTORY_ADDED -> Icons.Default.Add
        OperationType.INVENTORY_UPDATED -> Icons.Default.Edit
        OperationType.OUT_TRANSACTION -> Icons.Default.Receipt
        OperationType.CAPITAL_TRANSACTION -> Icons.Default.AccountBalance
    }
}

@Composable
fun getOperationColor(type: OperationType): Color {
    return when (type) {
        OperationType.PERSON_ADDED -> MaterialTheme.colorScheme.primary
        OperationType.PERSON_TRANSACTION -> MaterialTheme.colorScheme.secondary
        OperationType.INVENTORY_ADDED -> MaterialTheme.colorScheme.tertiary
        OperationType.INVENTORY_UPDATED -> MaterialTheme.colorScheme.tertiary
        OperationType.OUT_TRANSACTION -> MaterialTheme.colorScheme.error
        OperationType.CAPITAL_TRANSACTION -> MaterialTheme.colorScheme.primary
    }
}