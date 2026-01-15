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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.financialmanager.app.R
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
                title = { Text(stringResource(R.string.backup_restore)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
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
                            text = stringResource(R.string.sign_in_google_drive),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.connect_google_account),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = startGoogleSignIn,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.sign_in_google))
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
                                    text = stringResource(R.string.google_drive_connected),
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
                                Text(stringResource(R.string.creating_backup))
                            } else {
                                Icon(Icons.Default.Backup, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.create_backup))
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
                                Text(stringResource(R.string.loading))
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.refresh_backups))
                            }
                        }
                    }
                }

                // Auto Backup Settings
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.auto_backup_settings),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsState(initial = true)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.auto_backup_on_exit),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = stringResource(R.string.auto_backup_description),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = autoBackupEnabled,
                                onCheckedChange = { viewModel.setAutoBackupEnabled(it) }
                            )
                        }
                        
                        if (autoBackupEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.auto_backup_info),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Status messages
                when (val currentState = uiState) {
                    is BackupUiState.BackupSuccess -> {
                        AlertCard(
                            message = "${stringResource(currentState.messageRes)}${currentState.messageArg?.let { ": $it" } ?: ""}",
                            icon = Icons.Default.CheckCircle,
                            color = MaterialTheme.colorScheme.primary,
                            onDismiss = { viewModel.clearSuccess() }
                        )
                    }
                    is BackupUiState.BackupError -> {
                        AlertCard(
                            message = "${stringResource(currentState.messageRes)}${currentState.dynamicMessage?.let { ": $it" } ?: ""}",
                            icon = Icons.Default.Error,
                            color = MaterialTheme.colorScheme.error,
                            onDismiss = { viewModel.clearError() }
                        )
                    }
                    is BackupUiState.RestoreSuccess -> {
                        AlertCard(
                            message = "${stringResource(currentState.messageRes)}${currentState.messageArg?.let { ": $it" } ?: ""}",
                            icon = Icons.Default.CheckCircle,
                            color = MaterialTheme.colorScheme.primary,
                            onDismiss = { viewModel.clearSuccess() }
                        )
                    }
                    is BackupUiState.RestoreError -> {
                        AlertCard(
                            message = "${stringResource(currentState.messageRes)}${currentState.dynamicMessage?.let { ": $it" } ?: ""}",
                            icon = Icons.Default.Error,
                            color = MaterialTheme.colorScheme.error,
                            onDismiss = { viewModel.clearError() }
                        )
                    }
                    is BackupUiState.DeleteSuccess -> {
                        AlertCard(
                            message = "${stringResource(currentState.messageRes)}${currentState.messageArg?.let { ": $it" } ?: ""}",
                            icon = Icons.Default.CheckCircle,
                            color = MaterialTheme.colorScheme.primary,
                            onDismiss = { viewModel.clearSuccess() }
                        )
                    }
                    is BackupUiState.DeleteError -> {
                        AlertCard(
                            message = "${stringResource(currentState.messageRes)}${currentState.dynamicMessage?.let { ": $it" } ?: ""}",
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
                        text = stringResource(R.string.available_backups),
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
                                onDelete = {
                                    viewModel.deleteBackup(backup.id)
                                },
                                isRestoring = uiState is BackupUiState.Restoring,
                                isDeleting = uiState is BackupUiState.DeletingBackup
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
                                text = stringResource(R.string.no_backups_found),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = stringResource(R.string.create_backup_hint),
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
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.dismiss))
            }
        }
    }
}

@Composable
fun BackupItemCard(
    backup: DriveFile,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    isRestoring: Boolean,
    isDeleting: Boolean
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
                .fillMaxSize()
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
                        text = backup.name ?: stringResource(R.string.backup),
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
            }
            
            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRestore,
                    enabled = !isRestoring && !isDeleting,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.restoring))
                    } else {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.restore))
                    }
                }
                
                OutlinedButton(
                    onClick = onDelete,
                    enabled = !isRestoring && !isDeleting,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.deleting))
                    } else {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.delete))
                    }
                }
            }
        }
    }
}

