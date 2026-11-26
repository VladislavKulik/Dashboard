package com.example.lifedashboard.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.lifedashboard.R
import com.example.lifedashboard.data.DateInput
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Reusable Material date picker backed by java.time.LocalDate. */
@Composable
fun LocalDatePickerField(
    date: LocalDate?,
    label: String,
    onDateChange: (LocalDate?) -> Unit,
    allowClear: Boolean = true
) {
    var showPicker by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OutlinedTextField(
            value = DateInput.formatForInput(date),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { showPicker = true }) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = stringResource(R.string.action_select_date))
                }
            },
            modifier = Modifier.weight(1f).clickable { showPicker = true }
        )
        if (allowClear && date != null) {
            TextButton(onClick = { onDateChange(null) }) { Text(stringResource(R.string.action_reset)) }
        }
    }
    if (showPicker) {
        LocalDatePickerDialog(
            initialDate = date,
            onDismiss = { showPicker = false },
            onConfirm = { selected ->
                onDateChange(selected)
                showPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalDatePickerDialog(
    initialDate: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val initialMillis = remember(initialDate) {
        initialDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    }
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = pickerState.selectedDateMillis != null,
                onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                }
            ) { Text(stringResource(R.string.action_select)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    ) {
        DatePicker(state = pickerState)
    }
}
