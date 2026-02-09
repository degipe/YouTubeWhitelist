package io.github.degipe.youtubewhitelist.core.data.timelimit

import kotlinx.coroutines.flow.Flow

data class TimeLimitStatus(
    val dailyLimitMinutes: Int?,
    val watchedTodaySeconds: Int,
    val remainingSeconds: Int?,
    val isLimitReached: Boolean
)

interface TimeLimitChecker {
    fun getTimeLimitStatus(profileId: String): Flow<TimeLimitStatus>
}
