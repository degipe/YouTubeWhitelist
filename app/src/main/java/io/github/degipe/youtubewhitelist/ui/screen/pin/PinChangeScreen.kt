package io.github.degipe.youtubewhitelist.ui.screen.pin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.degipe.youtubewhitelist.R

@Composable
fun PinChangeScreen(
    onPinChanged: () -> Unit,
    onCancel: () -> Unit,
    viewModel: PinChangeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onPinChanged()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.pin_change_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (uiState.step) {
                PinChangeStep.VERIFY_OLD -> stringResource(R.string.pin_change_verify_subtitle)
                PinChangeStep.ENTER_NEW -> stringResource(R.string.pin_change_new_subtitle)
                PinChangeStep.CONFIRM_NEW -> stringResource(R.string.pin_confirm_subtitle)
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        PinDots(pinLength = uiState.pin.length)

        Spacer(modifier = Modifier.height(16.dp))

        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        PinKeypad(
            onDigit = viewModel::onDigitEntered,
            onBackspace = viewModel::onBackspace,
            onSubmit = viewModel::onSubmit,
            submitEnabled = uiState.pin.length >= 4 && !uiState.isLockedOut
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onCancel) {
            Text(text = stringResource(R.string.cancel))
        }
    }
}
