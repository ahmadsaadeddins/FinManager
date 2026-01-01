package com.financialmanager.app.ui.screens.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.financialmanager.app.ui.components.BottomNavigationBar
import com.financialmanager.app.ui.navigation.Screen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File as DriveFile
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    navController: NavController,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val backups by viewModel.backups.collectAsState()
    val accountEmail by viewModel.accountEmail.collectAsState()

    // Google Sign-In launcher
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            account?.email?.let { email ->
                viewModel.signIn(email)
            }
        } catch (e: ApiException) {
            // Sign-in was cancelled or failed
            // Error state will be handled by viewModel if needed
        }
    }

    val context = LocalContext.current

    // Handle Google Sign-In
    val startGoogleSignIn: () -> Unit = {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        signInLauncher.launch(googleSignInClient.signInIntent)
    }

    // Check if already signed in when screen loads
    LaunchedEffect(Unit) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        account?.email?.let { email ->
            if (viewModel.isSignedIn().not()) {
                viewModel.signIn(email)
            }
        }
    }

    // Load backups when signed in
    LaunchedEffect(viewModel.isSignedIn()) {
        if (viewModel.isSignedIn() && backups.isEmpty()) {
            viewModel.loadBackups()
        }
    }

    // Show alert dialogs for errors and success
    LaunchedEffect(uiState) {
        when (uiState) {
            is BackupUiState.BackupError -> {
                // Error will be shown in UI
            }
            is BackupUiState.RestoreError -> {
                // Error will be shown in UI
            }
            is BackupUiState.BackupSuccess -> {
                // Success will be shown in UI
            }
            is BackupUiState.RestoreSuccess -> {
                // Success will be shown in UI - user should restart app
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = Screen.Backup.route,
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
            // Sign-in section
            if (!viewModel.isSignedIn()) {
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
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Sign in to Google Drive",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Connect your Google account to backup and restore your data",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = startGoogleSignIn,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Sign in with Google")
                        }
                    }
                }
            } else {
                // Signed in - show backup options
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Google Drive Connected",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                accountEmail?.let { email ->
                                    Text(
                                        text = email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = { viewModel.createBackup() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState !is BackupUiState.CreatingBackup
                        ) {
                            if (uiState is BackupUiState.CreatingBackup) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Creating Backup...")
                            } else {
                                Icon(Icons.Default.Backup, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Create Backup")
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.loadBackups() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState !is BackupUiState.ListingBackups
                        ) {
                            if (uiState is BackupUiState.ListingBackups) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Loading...")
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Refresh Backups")
                            }
                        }
                    }
                }

                // Status messages
                when (val currentState = uiState) {
                    is BackupUiState.BackupSuccess -> {
                        AlertCard(
                            message = currentState.message,
                            icon = Icons.Default.CheckCircle,
                            color = MaterialTheme.colorScheme.primary,
                            onDismiss = { viewModel.clearSuccess() }
                        )
                    }
                    is BackupUiState.BackupError -> {
                        AlertCard(
                            message = currentState.message,
                            icon = Icons.Default.Error,
                            color = MaterialTheme.colorScheme.error,
                            onDismiss = { viewModel.clearError() }
                        )
                    }
                    is BackupUiState.RestoreSuccess -> {
                        AlertCard(
                            message = currentState.message,
                            icon = Icons.Default.Info,
                            color = MaterialTheme.colorScheme.primary,
                            onDismiss = { viewModel.clearSuccess() }
                        )
                    }
                    is BackupUiState.RestoreError -> {
                        AlertCard(
                            message = currentState.message,
                            icon = Icons.Default.Error,
                            color = MaterialTheme.colorScheme.error,
                            onDismiss = { viewModel.clearError() }
                        )
                    }
                    else -> {}
                }

                // Backups list
                if (backups.isNotEmpty()) {
                    Text(
                        text = "Available Backups",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(backups) { backup ->
                            BackupItemCard(
                                backup = backup,
                                onRestore = {
                                    viewModel.restoreBackup(backup.id)
                                },
                                isRestoring = uiState is BackupUiState.Restoring
                            )
                        }
                    }
                } else if (uiState !is BackupUiState.ListingBackups) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CloudOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No backups found",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Create a backup to get started",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
fun BackupItemCard(
    backup: DriveFile,
    onRestore: () -> Unit,
    isRestoring: Boolean
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val modifiedTime = backup.modifiedTime?.value?.let {
        Date(it)
    }

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
                        text = backup.name ?: "Backup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (modifiedTime != null) {
                        Text(
                            text = dateFormat.format(modifiedTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(
                    onClick = onRestore,
                    enabled = !isRestoring
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Restoring...")
                    } else {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Restore")
                    }
                }
            }
        }
    }
}

