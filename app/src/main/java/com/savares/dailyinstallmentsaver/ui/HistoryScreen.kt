package com.savares.dailyinstallmentsaver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.savares.dailyinstallmentsaver.R
import com.savares.dailyinstallmentsaver.model.SavingLogEntity
import com.savares.dailyinstallmentsaver.util.CurrencyUtil
import com.savares.dailyinstallmentsaver.viewmodel.InstallmentViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: InstallmentViewModel) {
    val logs by viewModel.savingLogs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title), fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_history), color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
            ) {
                // logs are already sorted (reversed) in ViewModel's savingLogs Flow
                items(
                    items = logs,
                    key = { it.id },
                    contentType = { "history_item" }
                ) { log ->
                    HistoryListItem(log)
                }
            }
        }
    }
}

@Composable
fun HistoryListItem(log: SavingLogEntity) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val formattedDate = remember(log.date) { sdf.format(Date(log.date)) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, 
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(log.installmentName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(formattedDate, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            }
            Text(
                "+ " + CurrencyUtil.formatCurrency(log.amount),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
