package com.savares.dailyinstallmentsaver.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savares.dailyinstallmentsaver.R
import com.savares.dailyinstallmentsaver.model.InstallmentEntity
import com.savares.dailyinstallmentsaver.viewmodel.InstallmentViewModel
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: InstallmentViewModel,
    onNavigateToAdd: () -> Unit
) {
    val installments by viewModel.installments.collectAsState()
    val totalSaving by viewModel.totalDailySaving.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
                actions = {
                    TextButton(onClick = { /* Simple language toggle logic could go here */ }) {
                        Text(stringResource(R.string.language_toggle), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_installment))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            TotalSavingCard(totalSaving)

            Spacer(modifier = Modifier.height(16.dp))

            if (installments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.empty_list))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(installments) { installment ->
                        InstallmentItem(
                            installment = installment,
                            onDelete = { viewModel.deleteInstallment(installment) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TotalSavingCard(amount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.total_daily_saving),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun InstallmentItem(installment: InstallmentEntity, onDelete: () -> Unit) {
    val daily = calculateDaily(installment)
    val daysLeft = calculateDaysLeft(installment.dueDate)

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = installment.name, style = MaterialTheme.typography.titleLarge)
                Text(text = installment.wallet, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = stringResource(R.string.days_left, daysLeft),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.daily_needed, formatCurrency(daily)),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
            }
        }
    }
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(amount).replace("Rp", "Rp ").replace(",00", "")
}

fun calculateDaily(installment: InstallmentEntity): Double {
    val days = calculateDaysLeft(installment.dueDate)
    return if (days > 0) installment.amount / days else installment.amount
}

fun calculateDaysLeft(dueDate: Long): Long {
    val diff = dueDate - System.currentTimeMillis()
    return TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0) + 1
}
