package com.savares.dailyinstallmentsaver.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.savares.dailyinstallmentsaver.R
import com.savares.dailyinstallmentsaver.viewmodel.InstallmentViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInstallmentScreen(
    viewModel: InstallmentViewModel,
    installmentId: Int? = null,
    onNavigateBack: () -> Unit
) {
    val installments by viewModel.installments.collectAsState()
    val editingInstallment = remember(installmentId, installments) {
        installments.find { it.id == installmentId }
    }

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var wallet by remember { mutableStateOf("") }
    var dueDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(editingInstallment) {
        editingInstallment?.let {
            name = it.name
            amount = it.amount.toString()
            wallet = it.wallet
            dueDate = it.dueDate
        }
    }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (installmentId == null) stringResource(R.string.add_installment) 
                        else stringResource(R.string.edit_installment)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.installment_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.amount)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = wallet,
                onValueChange = { wallet = it },
                label = { Text(stringResource(R.string.wallet)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
                Text(text = stringResource(R.string.due_date) + ": ${sdf.format(Date(dueDate))}")
            }

            Button(
                onClick = {
                    if (name.isNotBlank() && amount.isNotBlank() && wallet.isNotBlank()) {
                        if (installmentId == null) {
                            viewModel.addInstallment(name, amount.toDouble(), dueDate, wallet)
                        } else {
                            editingInstallment?.let {
                                viewModel.updateInstallment(it.copy(
                                    name = name,
                                    amount = amount.toDouble(),
                                    dueDate = dueDate,
                                    wallet = wallet
                                ))
                            }
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && amount.isNotBlank() && wallet.isNotBlank()
            ) {
                Text(if (installmentId == null) stringResource(R.string.save) else stringResource(R.string.update))
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
