package io.github.degipe.youtubewhitelist.feature.parent.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.data.repository.KidProfileRepository
import io.github.degipe.youtubewhitelist.core.data.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class StatsPeriod(val days: Int) {
    DAY(1), WEEK(7), MONTH(30)
}

data class DailyStatItem(val label: String, val minutes: Int)

data class WatchStatsUiState(
    val profileName: String = "",
    val selectedPeriod: StatsPeriod = StatsPeriod.WEEK,
    val totalWatchTimeFormatted: String = "",
    val videosWatchedCount: Int = 0,
    val dailyBreakdown: List<DailyStatItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel(assistedFactory = WatchStatsViewModel.Factory::class)
class WatchStatsViewModel @AssistedInject constructor(
    private val watchHistoryRepository: WatchHistoryRepository,
    private val kidProfileRepository: KidProfileRepository,
    @Assisted private val profileId: String
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(profileId: String): WatchStatsViewModel
    }

    private val _uiState = MutableStateFlow(WatchStatsUiState())
    val uiState: StateFlow<WatchStatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun selectPeriod(period: StatsPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period, isLoading = true)
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val profile = kidProfileRepository.getProfileById(profileId).first()
            val period = _uiState.value.selectedPeriod
            val sinceTimestamp = LocalDate.now()
                .minusDays(period.days.toLong())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val stats = watchHistoryRepository.getWatchStats(profileId, sinceTimestamp)
            val formatter = DateTimeFormatter.ofPattern("MM/dd")

            _uiState.value = _uiState.value.copy(
                profileName = profile?.name ?: "",
                totalWatchTimeFormatted = formatWatchTime(stats.totalWatchedSeconds),
                videosWatchedCount = stats.videosWatchedCount,
                dailyBreakdown = stats.dailyBreakdown.map { stat ->
                    val date = Instant.ofEpochMilli(stat.dayTimestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    DailyStatItem(
                        label = date.format(formatter),
                        minutes = stat.totalSeconds / 60
                    )
                },
                isLoading = false
            )
        }
    }

    private fun formatWatchTime(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}
