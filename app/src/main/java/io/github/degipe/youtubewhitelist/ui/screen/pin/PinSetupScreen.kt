package io.github.degipe.youtubewhitelist.ui.screen.pin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
fun PinSetupScreen(
    onPinSet: () -> Unit,
    viewModel: PinSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onPinSet()
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
            text = when (uiState.step) {
                PinSetupStep.ENTER_NEW -> stringResource(R.string.pin_setup_title)
                PinSetupStep.CONFIRM -> stringResource(R.string.pin_confirm_title)
            },
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (uiState.step) {
                PinSetupStep.ENTER_NEW -> stringResource(R.string.pin_setup_subtitle)
                PinSetupStep.CONFIRM -> stringResource(R.string.pin_confirm_subtitle)
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
            submitEnabled = uiState.pin.length >= 4
        )
    }
}
