package com.financialmanager.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.financialmanager.app.ui.components.BottomNavigationBar
import com.financialmanager.app.ui.navigation.Screen
import com.financialmanager.app.ui.theme.*
import java.text.NumberFormat
import java.util.*

data class SummaryCard(
    val title: String,
    val value: Double?,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val route: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val totalCapital by viewModel.totalCapital.collectAsState()
    val totalInventoryValue by viewModel.totalInventoryValue.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val totalSales by viewModel.totalSales.collectAsState()
    val profitLoss by viewModel.profitLoss.collectAsState()

    val cards = listOf(
        SummaryCard(
            "Total Capital",
            totalCapital,
            Icons.Default.AccountBalance,
            MoneyIn,
            Screen.Capital.route
        ),
        SummaryCard(
            "Inventory Value",
            totalInventoryValue,
            Icons.Default.Inventory,
            Inventory,
            Screen.Inventory.route
        ),
        SummaryCard(
            "Total Expenses",
            totalExpenses,
            Icons.Default.TrendingDown,
            MoneyOut,
            Screen.Transactions.route
        ),
        SummaryCard(
            "Total Sales",
            totalSales,
            Icons.Default.TrendingUp,
            MoneyIn,
            Screen.Transactions.route
        ),
        SummaryCard(
            "Profit/Loss",
            profitLoss,
            Icons.Default.Assessment,
            if (profitLoss >= 0) MoneyIn else MoneyOut,
            Screen.Reports.route
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Manager") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { navController.navigate(Screen.Reports.route) }) {
                        Icon(Icons.Default.Assessment, contentDescription = "Reports")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = Screen.Home.route,
                onNavigate = { route -> navController.navigate(route) }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = padding,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(cards) { card ->
                SummaryCard(
                    card = card,
                    onClick = { card.route?.let { navController.navigate(it) } }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryCard(
    card: SummaryCard,
    onClick: () -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val valueText = card.value?.let { formatter.format(it) } ?: "$0.00"

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = card.icon,
                    contentDescription = card.title,
                    tint = card.color
                )
            }
            Text(
                text = valueText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = card.color
            )
        }
    }
}

