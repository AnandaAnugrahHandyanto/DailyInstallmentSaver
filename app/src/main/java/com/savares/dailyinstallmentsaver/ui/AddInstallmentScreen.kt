package com.savares.dailyinstallmentsaver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    var collectedAmount by remember { mutableStateOf("") }
    var dueDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(editingInstallment) {
        editingInstallment?.let {
            name = it.name
            amount = it.amount.toString()
            wallet = it.wallet
            collectedAmount = if (it.collectedAmount > 0.0) it.collectedAmount.toString() else ""
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
                        else stringResource(R.string.edit_installment),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.installment_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.amount)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = wallet,
                onValueChange = { wallet = it },
                label = { Text(stringResource(R.string.wallet)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            val amountVal = amount.toDoubleOrNull() ?: 0.0
            val collectedVal = collectedAmount.toDoubleOrNull() ?: 0.0
            val collectedAmountError = collectedAmount.isNotBlank() && amount.isNotBlank() && collectedVal > amountVal

            OutlinedTextField(
                value = collectedAmount,
                onValueChange = { collectedAmount = it },
                label = { Text(stringResource(R.string.collected_amount)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                isError = collectedAmountError,
                supportingText = if (collectedAmountError) {
                    { Text(stringResource(R.string.collected_amount_error)) }
                } else null
            )

            val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
            val formattedDate = remember(dueDate, sdf) { sdf.format(Date(dueDate)) }

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = stringResource(R.string.due_date_display, formattedDate))
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && amount.isNotBlank() && wallet.isNotBlank() && !collectedAmountError) {
                        if (installmentId == null) {
                            viewModel.addInstallment(name, amountVal, dueDate, wallet, if (collectedAmount.isBlank()) 0.0 else collectedVal)
                        } else {
                            editingInstallment?.let {
                                viewModel.updateInstallment(
                                    it.copy(
                                        name = name,
                                        amount = amountVal,
                                        dueDate = dueDate,
                                        wallet = wallet,
                                        collectedAmount = if (collectedAmount.isBlank()) 0.0 else collectedVal
                                    )
                                )
                            }
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                enabled = name.isNotBlank() && amount.isNotBlank() && wallet.isNotBlank() && !collectedAmountError
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
                    Text(stringResource(R.string.ok))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
