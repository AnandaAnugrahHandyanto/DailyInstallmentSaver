package com.savares.dailyinstallmentsaver.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.savares.dailyinstallmentsaver.model.InstallmentEntity
import com.savares.dailyinstallmentsaver.viewmodel.InstallmentViewModel
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun AddInstallmentScreen(viewModel: InstallmentViewModel) {

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var wallet by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Cicilan") })
        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Nominal") })
        OutlinedTextField(value = wallet, onValueChange = { wallet = it }, label = { Text("Wallet (Dana/Gopay)") })

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val dueDate = LocalDate.now().plusDays(7)
                .atStartOfDay(ZoneId.systemDefault())
                .toEpochSecond()

            viewModel.addInstallment(
                InstallmentEntity(
                    name = name,
                    amount = amount.toDouble(),
                    dueDate = dueDate,
                    wallet = wallet
                )
            )
        }) {
            Text("Simpan")
        }
    }
}