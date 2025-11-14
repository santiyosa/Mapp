package com.maintenance.app.presentation.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import java.time.LocalDate
import java.time.LocalTime

/**
 * A composable for selecting a date.
 */
@Composable
fun DatePickerField(
    label: String,
    value: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value?.toString() ?: "",
        onValueChange = { },
        label = { Text(label) },
        readOnly = true,
        modifier = modifier,
        enabled = false
    )
}

/**
 * A composable for selecting a time.
 */
@Composable
fun TimePickerField(
    label: String,
    value: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value?.toString() ?: "",
        onValueChange = { },
        label = { Text(label) },
        readOnly = true,
        modifier = modifier,
        enabled = false
    )
}

/**
 * A composable for entering currency with formatting.
 */
@Composable
fun CurrencyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}

/**
 * A composable for entering duration in minutes.
 */
@Composable
fun DurationTextField(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value?.toString() ?: "",
        onValueChange = { text ->
            onValueChange(text.toIntOrNull())
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}
