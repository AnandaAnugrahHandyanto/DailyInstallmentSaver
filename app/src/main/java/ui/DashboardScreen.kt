package com.savares.dailyinstallmentsaver.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.savares.dailyinstallmentsaver.model.Installment
import java.time.LocalDate

@Composable
fun DashboardScreen() {
    val installments = remember { mutableStateListOf<Installment>() }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Daily Installment Saver", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(installments.size) { index ->
                val c = installments[index]
                Text(
                    text = "${c.name}\nWallet: ${c.wallet}\nDaily: Rp ${c.dailySave().toInt()}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            installments.clear()
            installments.add(
                Installment("Dana Cicilan", 55000.0, LocalDate.of(2026,4,10), "Dana")
            )
            installments.add(
                Installment("Gopay Cicilan", 180000.0, LocalDate.of(2026,4,1), "Gopay")
            )
        }) {
            Text("Load Demo Data")
        }
    }
}