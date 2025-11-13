package com.maintenance.app.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Enhanced text field with validation and error display.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: String? = null,
    errorMessage: String? = null,
    isError: Boolean = errorMessage != null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon?.let { 
                { 
                    Icon(
                        imageVector = it,
                        contentDescription = null
                    ) 
                } 
            },
            trailingIcon = trailingIcon,
            supportingText = {
                when {
                    errorMessage != null -> {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    supportingText != null -> {
                        Text(
                            text = supportingText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            isError = isError,
            enabled = enabled,
            readOnly = readOnly,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Specialized text field for currency amounts.
 */
@Composable
fun CurrencyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Costo",
    currency: String = "USD",
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    errorMessage: String? = null,
    enabled: Boolean = true
) {
    ValidatedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder ?: "0.00",
        supportingText = "Moneda: $currency",
        errorMessage = errorMessage,
        enabled = enabled,
        keyboardType = KeyboardType.Decimal,
        modifier = modifier
    )
}

/**
 * Specialized text field for duration in minutes.
 */
@Composable
fun DurationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Duración (minutos)",
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    errorMessage: String? = null,
    enabled: Boolean = true
) {
    ValidatedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder ?: "60",
        supportingText = "Tiempo en minutos",
        errorMessage = errorMessage,
        enabled = enabled,
        keyboardType = KeyboardType.Number,
        modifier = modifier
    )
}

/**
 * Multi-line text field for longer text content.
 */
@Composable
fun MultiLineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    errorMessage: String? = null,
    maxLength: Int = 1000,
    minLines: Int = 3,
    maxLines: Int = 6,
    enabled: Boolean = true
) {
    ValidatedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= maxLength) {
                onValueChange(newValue)
            }
        },
        label = label,
        placeholder = placeholder,
        supportingText = "${value.length}/$maxLength caracteres",
        errorMessage = errorMessage,
        enabled = enabled,
        singleLine = false,
        minLines = minLines,
        maxLines = maxLines,
        modifier = modifier
    )
}