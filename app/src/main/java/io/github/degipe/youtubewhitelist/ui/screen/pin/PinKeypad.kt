package io.github.degipe.youtubewhitelist.ui.screen.pin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PinKeypad(
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    submitEnabled: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: 1, 2, 3
        KeypadRow(digits = listOf(1, 2, 3), onDigit = onDigit)
        // Row 2: 4, 5, 6
        KeypadRow(digits = listOf(4, 5, 6), onDigit = onDigit)
        // Row 3: 7, 8, 9
        KeypadRow(digits = listOf(7, 8, 9), onDigit = onDigit)
        // Row 4: backspace, 0, submit
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackspace,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            DigitButton(digit = 0, onClick = onDigit)

            IconButton(
                onClick = onSubmit,
                enabled = submitEnabled,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Submit",
                    tint = if (submitEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
            }
        }
    }
}

@Composable
private fun KeypadRow(
    digits: List<Int>,
    onDigit: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        digits.forEach { digit ->
            DigitButton(digit = digit, onClick = onDigit)
        }
    }
}

@Composable
private fun DigitButton(
    digit: Int,
    onClick: (Int) -> Unit
) {
    FilledTonalButton(
        onClick = { onClick(digit) },
        modifier = Modifier.size(72.dp),
        shape = CircleShape
    ) {
        Text(
            text = digit.toString(),
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
