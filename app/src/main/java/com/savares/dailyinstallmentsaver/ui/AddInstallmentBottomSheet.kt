package com.savares.dailyinstallmentsaver.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
fun AddInstallmentBottomSheet(
    viewModel: InstallmentViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var wallet by remember { mutableStateOf("") }
    var collectedAmount by remember { mutableStateOf("") }
    var dueDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var walletExpanded by remember { mutableStateOf(false) }

    val walletOptions = remember { listOf("Dana", "Gopay", "OVO", "ShopeePay", "LinkAja") }

    val targetVal = targetAmount.toDoubleOrNull() ?: 0.0
    val collectedVal = collectedAmount.toDoubleOrNull() ?: 0.0
    val collectedAmountError = collectedAmount.isNotBlank() && targetAmount.isNotBlank() && collectedVal > targetVal

    val isValid = name.isNotBlank() && targetAmount.isNotBlank() && wallet.isNotBlank() && !collectedAmountError

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate)
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val formattedDate = remember(dueDate, sdf) { sdf.format(Date(dueDate)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.add_installment),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.installment_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = targetAmount,
                onValueChange = { targetAmount = it },
                label = { Text(stringResource(R.string.amount)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = walletExpanded,
                onExpandedChange = { walletExpanded = !walletExpanded }
            ) {
                OutlinedTextField(
                    value = wallet,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.wallet_type)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = walletExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(16.dp)
                )
                ExposedDropdownMenu(
                    expanded = walletExpanded,
                    onDismissRequest = { walletExpanded = false }
                ) {
                    walletOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                wallet = option
                                walletExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = collectedAmount,
                onValueChange = { collectedAmount = it },
                label = { Text(stringResource(R.string.collected_amount)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                isError = collectedAmountError,
                supportingText = if (collectedAmountError) {
                    { Text(stringResource(R.string.collected_amount_error)) }
                } else null
            )

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
                    viewModel.addInstallment(
                        name = name,
                        amount = targetVal,
                        dueDate = dueDate,
                        wallet = wallet,
                        collectedAmount = if (collectedAmount.isBlank()) 0.0 else collectedVal
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                enabled = isValid
            ) {
                Text(stringResource(R.string.save))
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
