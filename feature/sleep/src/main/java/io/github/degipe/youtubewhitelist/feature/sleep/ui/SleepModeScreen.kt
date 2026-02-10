package io.github.degipe.youtubewhitelist.feature.sleep.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.degipe.youtubewhitelist.core.data.sleep.SleepTimerStatus

private val sleepDarkColors = darkColorScheme(
    background = Color(0xFF0A0A1A),
    surface = Color(0xFF121230),
    primary = Color(0xFF7B68EE),
    onBackground = Color(0xFFB0B0D0),
    onSurface = Color(0xFFB0B0D0),
    onPrimary = Color.White
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepModeScreen(
    viewModel: SleepModeViewModel,
    onNavigateBack: () -> Unit,
    onStartTimer: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MaterialTheme(colorScheme = sleepDarkColors) {
        Scaffold(
            containerColor = sleepDarkColors.background,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "  Sleep Mode",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = sleepDarkColors.surface,
                        titleContentColor = sleepDarkColors.onSurface,
                        navigationIconContentColor = sleepDarkColors.onSurface
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(sleepDarkColors.background)
            ) {
                when {
                    uiState.isTimerForThisProfile && uiState.timerStatus == SleepTimerStatus.RUNNING -> {
                        TimerRunningContent(
                            remainingSeconds = uiState.remainingSeconds,
                            formattedRemaining = uiState.formattedRemaining,
                            onStop = { viewModel.stopTimer() }
                        )
                    }
                    uiState.isTimerForThisProfile && uiState.timerStatus == SleepTimerStatus.EXPIRED -> {
                        TimerExpiredContent(
                            onDismiss = {
                                viewModel.stopTimer()
                                onNavigateBack()
                            }
                        )
                    }
                    else -> {
                        TimerSelectionContent(
                            selectedMinutes = uiState.selectedDurationMinutes,
                            onSelectDuration = viewModel::selectDuration,
                            onStart = {
                                viewModel.startTimer()
                                onStartTimer()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerSelectionContent(
    selectedMinutes: Int,
    onSelectDuration: (Int) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Bedtime,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = sleepDarkColors.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Set Sleep Timer",
            style = MaterialTheme.typography.headlineMedium,
            color = sleepDarkColors.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Duration display
        Text(
            text = formatDuration(selectedMinutes),
            style = MaterialTheme.typography.displayMedium.copy(
                fontSize = 48.sp,
                letterSpacing = 2.sp
            ),
            color = sleepDarkColors.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Slider
        Slider(
            value = selectedMinutes.toFloat(),
            onValueChange = { onSelectDuration(it.toInt()) },
            valueRange = 5f..600f,
            steps = 118, // (600 - 5) / 5 - 1 = 118 intermediate steps
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = SliderDefaults.colors(
                thumbColor = sleepDarkColors.primary,
                activeTrackColor = sleepDarkColors.primary,
                inactiveTrackColor = sleepDarkColors.surface
            )
        )

        // Min/Max labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "5m",
                style = MaterialTheme.typography.labelSmall,
                color = sleepDarkColors.onBackground.copy(alpha = 0.5f)
            )
            Text(
                text = "10h",
                style = MaterialTheme.typography.labelSmall,
                color = sleepDarkColors.onBackground.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(
                containerColor = sleepDarkColors.primary,
                contentColor = sleepDarkColors.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text("  Start Sleep Timer")
        }
    }
}

@Composable
private fun TimerRunningContent(
    remainingSeconds: Long,
    formattedRemaining: String,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Bedtime,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = sleepDarkColors.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Timer Running",
            style = MaterialTheme.typography.headlineMedium,
            color = sleepDarkColors.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Countdown
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        Text(
            text = "%d:%02d".format(minutes, seconds),
            style = MaterialTheme.typography.displayMedium.copy(
                fontSize = 56.sp,
                letterSpacing = 4.sp
            ),
            color = if (remainingSeconds <= 120)
                sleepDarkColors.primary.copy(alpha = 0.7f)
            else
                sleepDarkColors.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = formattedRemaining,
            style = MaterialTheme.typography.bodyMedium,
            color = sleepDarkColors.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStop,
            colors = ButtonDefaults.buttonColors(
                containerColor = sleepDarkColors.surface,
                contentColor = sleepDarkColors.onSurface
            )
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = null
            )
            Text("  Cancel Timer")
        }
    }
}

@Composable
private fun TimerExpiredContent(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Bedtime,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = sleepDarkColors.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Timer Expired",
                style = MaterialTheme.typography.headlineMedium,
                color = sleepDarkColors.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The sleep timer has finished.",
                style = MaterialTheme.typography.bodyMedium,
                color = sleepDarkColors.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = sleepDarkColors.primary,
                    contentColor = sleepDarkColors.onPrimary
                )
            ) {
                Text("Dismiss")
            }
        }
    }
}

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
