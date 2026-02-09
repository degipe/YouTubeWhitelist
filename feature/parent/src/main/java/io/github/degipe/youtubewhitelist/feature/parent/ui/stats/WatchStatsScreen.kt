package io.github.degipe.youtubewhitelist.feature.parent.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchStatsScreen(
    viewModel: WatchStatsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Watch Stats - ${uiState.profileName}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Period selector
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatsPeriod.entries.forEach { period ->
                            FilterChip(
                                selected = uiState.selectedPeriod == period,
                                onClick = { viewModel.selectPeriod(period) },
                                label = {
                                    Text(
                                        when (period) {
                                            StatsPeriod.DAY -> "Today"
                                            StatsPeriod.WEEK -> "This Week"
                                            StatsPeriod.MONTH -> "This Month"
                                        }
                                    )
                                }
                            )
                        }
                    }
                }

                // Summary cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = "Watch Time",
                            value = uiState.totalWatchTimeFormatted,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Videos",
                            value = uiState.videosWatchedCount.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Daily breakdown header
                if (uiState.dailyBreakdown.isNotEmpty()) {
                    item {
                        Text(
                            text = "Daily Breakdown",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    val maxMinutes = uiState.dailyBreakdown.maxOfOrNull { it.minutes } ?: 1

                    items(uiState.dailyBreakdown) { stat ->
                        DailyStatRow(stat = stat, maxMinutes = maxMinutes)
                    }
                }

                // Empty state
                if (uiState.dailyBreakdown.isEmpty() && uiState.videosWatchedCount == 0) {
                    item {
                        Text(
                            text = "No watch history for this period",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DailyStatRow(
    stat: DailyStatItem,
    maxMinutes: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stat.label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${stat.minutes}m",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (maxMinutes > 0) {
            LinearProgressIndicator(
                progress = { stat.minutes.toFloat() / maxMinutes },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )
        }
    }
}
