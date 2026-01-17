package com.financialmanager.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.financialmanager.app.R
import com.financialmanager.app.ui.components.BottomNavigationBar
import com.financialmanager.app.ui.navigation.Screen
import com.financialmanager.app.ui.theme.*
import com.financialmanager.app.util.LocaleHelper
import com.financialmanager.app.util.NumberFormatter
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
    val totalPeopleBalance by viewModel.totalPeopleBalance.collectAsState()
    val profitLoss by viewModel.profitLoss.collectAsState()
    val generalProfit by viewModel.generalProfit.collectAsState()
    val hideNumbers by viewModel.hideNumbers.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val isRTL = LocaleHelper.isRTL()

    val cards = listOf(
        SummaryCard(
            stringResource(R.string.total_capital),
            totalCapital,
            Icons.Default.AccountBalance,
            MoneyIn,
            Screen.Capital.route
        ),
        SummaryCard(
            stringResource(R.string.inventory_value),
            totalInventoryValue,
            Icons.Default.Inventory,
            Inventory,
            Screen.Inventory.route
        ),
        SummaryCard(
            stringResource(R.string.people_balance),
            totalPeopleBalance,
            Icons.Default.People,
            if ((totalPeopleBalance ?: 0.0) >= 0) MoneyIn else MoneyOut,
            Screen.People.route
        ),
        SummaryCard(
            stringResource(R.string.total_expenses),
            totalExpenses,
            Icons.AutoMirrored.Filled.TrendingDown,
            MoneyOut,
            Screen.Transactions.route
        ),
        SummaryCard(
            stringResource(R.string.total_sales),
            totalSales,
            Icons.AutoMirrored.Filled.TrendingUp,
            MoneyIn,
            Screen.Transactions.route
        ),
        SummaryCard(
            stringResource(R.string.profit_loss),
            profitLoss,
            Icons.Default.Assessment,
            if (profitLoss >= 0) MoneyIn else MoneyOut,
            Screen.Reports.route
        ),
        SummaryCard(
            stringResource(R.string.general_balance),
            generalProfit,
            Icons.Default.Balance,
            if (generalProfit >= 0) MoneyIn else MoneyOut,
            Screen.Reports.route
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    // Toggle button for hiding/showing numbers
                    IconButton(onClick = { viewModel.toggleHideNumbers() }) {
                        Icon(
                            if (hideNumbers) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (hideNumbers) "Show Numbers" else "Hide Numbers"
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screen.Backup.route) }) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Backup")
                    }
                    IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { navController.navigate(Screen.Reports.route) }) {
                        Icon(Icons.Default.Assessment, contentDescription = "Reports")
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            items(cards) { card ->
                SummaryCard(
                    card = card,
                    hideNumbers = hideNumbers,
                    currencySymbol = currency.symbol,
                    isRTL = isRTL,
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
    hideNumbers: Boolean,
    currencySymbol: String,
    isRTL: Boolean,
    onClick: () -> Unit
) {
    val valueText = NumberFormatter.formatCurrency(card.value, hideNumbers, currencySymbol, isRTL)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
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


@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
fun HomeScreenPreview() {
    FinancialManagerTheme {
        // Preview with sample data
        SummaryCard(
            card = SummaryCard(
                "Total Capital",
                1234.56,
                Icons.Default.AccountBalance,
                MoneyIn,
                null
            ),
            hideNumbers = false,
            currencySymbol = "ج.م",
            isRTL = false,
            onClick = {}
        )
    }
}

@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
fun HomeScreenPreviewHidden() {
    FinancialManagerTheme {
        // Preview with hidden numbers
        SummaryCard(
            card = SummaryCard(
                "Total Capital",
                1234.56,
                Icons.Default.AccountBalance,
                MoneyIn,
                null
            ),
            hideNumbers = true,
            currencySymbol = "ج.م",
            isRTL = false,
            onClick = {}
        )
    }
}