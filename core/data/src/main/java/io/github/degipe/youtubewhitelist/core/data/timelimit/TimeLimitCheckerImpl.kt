package io.github.degipe.youtubewhitelist.core.data.timelimit

import io.github.degipe.youtubewhitelist.core.data.repository.KidProfileRepository
import io.github.degipe.youtubewhitelist.core.data.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class TimeLimitCheckerImpl @Inject constructor(
    private val kidProfileRepository: KidProfileRepository,
    private val watchHistoryRepository: WatchHistoryRepository
) : TimeLimitChecker {

    override fun getTimeLimitStatus(profileId: String): Flow<TimeLimitStatus> {
        return combine(
            kidProfileRepository.getProfileById(profileId),
            watchHistoryRepository.getTotalWatchedSecondsTodayFlow(profileId)
        ) { profile, watchedSeconds ->
            val limitMinutes = profile?.dailyLimitMinutes

            if (limitMinutes == null) {
                TimeLimitStatus(
                    dailyLimitMinutes = null,
                    watchedTodaySeconds = watchedSeconds,
                    remainingSeconds = null,
                    isLimitReached = false
                )
            } else {
                val limitSeconds = limitMinutes * 60
                val remaining = (limitSeconds - watchedSeconds).coerceAtLeast(0)
                TimeLimitStatus(
                    dailyLimitMinutes = limitMinutes,
                    watchedTodaySeconds = watchedSeconds,
                    remainingSeconds = remaining,
                    isLimitReached = watchedSeconds >= limitSeconds
                )
            }
        }
    }
}
