package io.github.degipe.youtubewhitelist.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.database.YouTubeWhitelistDatabase
import io.github.degipe.youtubewhitelist.core.database.entity.KidProfileEntity
import io.github.degipe.youtubewhitelist.core.database.entity.ParentAccountEntity
import io.github.degipe.youtubewhitelist.core.database.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class WatchHistoryDaoTest {

    private lateinit var database: YouTubeWhitelistDatabase
    private lateinit var dao: WatchHistoryDao

    private val profileId = "profile-1"

    @Before
    fun setup() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, YouTubeWhitelistDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.watchHistoryDao()

        // Satisfy foreign key chain: parent_account -> kid_profile -> watch_history
        database.parentAccountDao().insert(
            ParentAccountEntity(
                id = "parent-1",
                googleAccountId = "google-1",
                email = "parent@example.com",
                pinHash = "hash"
            )
        )
        database.kidProfileDao().insert(
            KidProfileEntity(
                id = profileId,
                parentAccountId = "parent-1",
                name = "Kid"
            )
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `getTotalWatchedSecondsTodayFlow excludes rows from before start of today`() = runTest {
        val now = System.currentTimeMillis()
        val yesterday = now - TimeUnit.HOURS.toMillis(30) // definitely yesterday, any timezone
        val earlierToday = now - TimeUnit.HOURS.toMillis(1) // definitely still today

        dao.insert(makeEntry(id = "old", watchedAt = yesterday, watchedSeconds = 500))
        dao.insert(makeEntry(id = "recent", watchedAt = earlierToday, watchedSeconds = 120))

        val result = dao.getTotalWatchedSecondsTodayFlow(profileId).first()

        assertThat(result).isEqualTo(120)
    }

    @Test
    fun `getTotalWatchedSecondsTodayFlow sums multiple entries from today`() = runTest {
        val now = System.currentTimeMillis()
        val earlierToday = now - TimeUnit.MINUTES.toMillis(30)

        dao.insert(makeEntry(id = "a", watchedAt = earlierToday, watchedSeconds = 100))
        dao.insert(makeEntry(id = "b", watchedAt = now, watchedSeconds = 200))

        val result = dao.getTotalWatchedSecondsTodayFlow(profileId).first()

        assertThat(result).isEqualTo(300)
    }

    @Test
    fun `getTotalWatchedSecondsTodayFlow returns zero when no history`() = runTest {
        val result = dao.getTotalWatchedSecondsTodayFlow(profileId).first()

        assertThat(result).isEqualTo(0)
    }

    private fun makeEntry(
        id: String,
        watchedAt: Long,
        watchedSeconds: Int
    ) = WatchHistoryEntity(
        id = id,
        kidProfileId = profileId,
        videoId = "video-$id",
        videoTitle = "Video $id",
        watchedSeconds = watchedSeconds,
        watchedAt = watchedAt
    )
}
