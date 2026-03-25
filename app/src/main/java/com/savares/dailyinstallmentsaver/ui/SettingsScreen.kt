package com.savares.dailyinstallmentsaver.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.savares.dailyinstallmentsaver.R
import com.savares.dailyinstallmentsaver.util.BackupManager
import com.savares.dailyinstallmentsaver.viewmodel.InstallmentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: InstallmentViewModel,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    var notificationsEnabled by remember { mutableStateOf(prefs.getBoolean("notifications_enabled", true)) }

    val backupManager = remember { BackupManager(viewModel.getDao()) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                val success = backupManager.exportData(context, it)
                Toast.makeText(
                    context, 
                    if (success) context.getString(R.string.backup_success) else context.getString(R.string.backup_failed), 
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                val success = backupManager.importData(context, it)
                Toast.makeText(
                    context, 
                    if (success) context.getString(R.string.restore_success) else context.getString(R.string.restore_failed), 
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) }
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
            // Language
            SettingsItem(
                title = stringResource(R.string.language),
                icon = Icons.Default.Language,
                action = {
                    TextButton(onClick = {
                        val newLang = if (currentLanguage == "en") "in" else "en"
                        onLanguageChange(newLang)
                    }) {
                        Text(if (currentLanguage == "en") "English" else "Indonesia")
                    }
                }
            )

            HorizontalDivider()

            // Notifications
            SettingsItem(
                title = stringResource(R.string.notifications),
                icon = Icons.Default.Notifications,
                action = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            notificationsEnabled = it
                            prefs.edit().putBoolean("notifications_enabled", it).apply()
                        }
                    )
                },
                subtitle = stringResource(R.string.enable_notifications)
            )

            HorizontalDivider()

            // Backup & Restore
            Text(
                text = stringResource(R.string.backup_restore),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { exportLauncher.launch("daily_installment_backup.json") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Backup, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.backup_data))
            }

            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Restore, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.restore_data))
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    action: @Composable () -> Unit,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        action()
    }
}
