package com.financialmanager.app.ui.screens.settings

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.financialmanager.app.MainActivity
import com.financialmanager.app.R
import com.financialmanager.app.ui.components.BottomNavigationBar
import com.financialmanager.app.ui.navigation.Screen
import com.financialmanager.app.util.LocaleHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = Screen.Settings.route,
                onNavigate = { route -> navController.navigate(route) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Language Setting
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLanguageDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.language),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = getLanguageDisplayName(selectedLanguage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.select_language)) },
            text = {
                Column {
                    LanguageOption(
                        code = LocaleHelper.LANGUAGE_SYSTEM,
                        name = stringResource(R.string.language_system),
                        isSelected = selectedLanguage == LocaleHelper.LANGUAGE_SYSTEM,
                        onSelect = {
                            viewModel.setLanguage(LocaleHelper.LANGUAGE_SYSTEM)
                            showLanguageDialog = false
                            showRestartDialog = true
                        }
                    )
                    LanguageOption(
                        code = LocaleHelper.LANGUAGE_ENGLISH,
                        name = stringResource(R.string.language_english),
                        isSelected = selectedLanguage == LocaleHelper.LANGUAGE_ENGLISH,
                        onSelect = {
                            viewModel.setLanguage(LocaleHelper.LANGUAGE_ENGLISH)
                            showLanguageDialog = false
                            showRestartDialog = true
                        }
                    )
                    LanguageOption(
                        code = LocaleHelper.LANGUAGE_ARABIC,
                        name = stringResource(R.string.language_arabic),
                        isSelected = selectedLanguage == LocaleHelper.LANGUAGE_ARABIC,
                        onSelect = {
                            viewModel.setLanguage(LocaleHelper.LANGUAGE_ARABIC)
                            showLanguageDialog = false
                            showRestartDialog = true
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Restart Dialog
    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text(stringResource(R.string.restart_required)) },
            text = { Text(stringResource(R.string.language_changed)) },
            confirmButton = {
                Button(
                    onClick = {
                        showRestartDialog = false
                        // Restart the app
                        val activity = context as? Activity
                        activity?.let {
                            val intent = Intent(it, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            it.startActivity(intent)
                            it.finish()
                        }
                    }
                ) {
                    Text(stringResource(R.string.restart_now))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text(stringResource(R.string.later))
                }
            }
        )
    }
}

@Composable
private fun LanguageOption(
    code: String,
    name: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge
        )
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun getLanguageDisplayName(languageCode: String): String {
    return when (languageCode) {
        LocaleHelper.LANGUAGE_ENGLISH -> stringResource(R.string.language_english)
        LocaleHelper.LANGUAGE_ARABIC -> stringResource(R.string.language_arabic)
        else -> stringResource(R.string.language_system)
    }
}
