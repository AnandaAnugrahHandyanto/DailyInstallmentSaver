package com.savares.dailyinstallmentsaver.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savares.dailyinstallmentsaver.R
import com.savares.dailyinstallmentsaver.model.InstallmentEntity
import com.savares.dailyinstallmentsaver.util.CurrencyUtil
import com.savares.dailyinstallmentsaver.viewmodel.InstallmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: InstallmentViewModel,
    onNavigateToAdd: () -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val installments by viewModel.installments.collectAsState()
    val totalSaving by viewModel.totalDailySaving.collectAsState()
    val walletBreakdown by viewModel.walletBreakdown.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
                actions = {
                    TextButton(onClick = {
                        val newLang = if (currentLanguage == "en") "in" else "en"
                        onLanguageChange(newLang)
                    }) {
                        Text(
                            text = if (currentLanguage == "en") "EN" else "ID",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                TotalSavingCard(totalSaving, walletBreakdown)
            }

            if (installments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxHeight(0.7f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.empty_list))
                    }
                }
            } else {
                items(installments, key = { it.id }) { installment ->
                    InstallmentItem(
                        installment = installment,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun TotalSavingCard(total: Double, breakdown: Map<String, Double>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.total_daily_saving),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = CurrencyUtil.formatCurrency(total),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            if (breakdown.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                )
                breakdown.forEach { (wallet, amount) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = wallet,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = CurrencyUtil.formatCurrency(amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InstallmentItem(
    installment: InstallmentEntity,
    viewModel: InstallmentViewModel
) {
    val daily = viewModel.calculateDailySaving(installment)
    val daysLeft = viewModel.calculateDaysLeft(installment.dueDate)
    val isSavedToday = viewModel.isSavedToday(installment)
    val progress = (installment.savedAmount / installment.amount).coerceIn(0.0, 1.0).toFloat()
    val suggestion = viewModel.getSmartSuggestion(installment)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = installment.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = installment.wallet,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(onClick = { viewModel.deleteInstallment(installment) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Section
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(
                        R.string.progress,
                        CurrencyUtil.formatCurrency(installment.savedAmount),
                        CurrencyUtil.formatCurrency(installment.amount),
                        (progress * 100).toInt()
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = stringResource(R.string.days_left, daysLeft),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (daysLeft < 3) MaterialTheme.colorScheme.error else Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Requirement
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.daily_needed, ""),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = CurrencyUtil.formatCurrency(daily),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Button(
                        onClick = { viewModel.markAsSaved(installment) },
                        enabled = !isSavedToday,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSavedToday) Color.Gray else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isSavedToday) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.saved_today))
                        } else {
                            Text(stringResource(R.string.mark_as_saved))
                        }
                    }
                }
            }

            // Smart Suggestion
            if (!isSavedToday && suggestion > daily) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.suggestion_missed, CurrencyUtil.formatCurrency(suggestion)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
