package io.github.degipe.youtubewhitelist.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.database.YouTubeWhitelistDatabase
import io.github.degipe.youtubewhitelist.core.database.entity.CachedChannelVideoEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CachedChannelVideoDaoTest {

    private lateinit var database: YouTubeWhitelistDatabase
    private lateinit var dao: CachedChannelVideoDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, YouTubeWhitelistDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.cachedChannelVideoDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `upsertAll inserts videos and getVideosByChannel returns them ordered by position`() = runTest {
        val videos = listOf(
            makeVideo(channelId = "UC123", videoId = "v2", title = "Second", position = 1),
            makeVideo(channelId = "UC123", videoId = "v1", title = "First", position = 0),
            makeVideo(channelId = "UC123", videoId = "v3", title = "Third", position = 2)
        )

        dao.upsertAll(videos)

        val result = dao.getVideosByChannel("UC123").first()
        assertThat(result).hasSize(3)
        assertThat(result[0].videoId).isEqualTo("v1")
        assertThat(result[1].videoId).isEqualTo("v2")
        assertThat(result[2].videoId).isEqualTo("v3")
    }

    @Test
    fun `searchVideosInChannel returns matching videos`() = runTest {
        val videos = listOf(
            makeVideo(channelId = "UC123", videoId = "v1", title = "Minecraft Tutorial", position = 0),
            makeVideo(channelId = "UC123", videoId = "v2", title = "Squid Game Challenge", position = 1),
            makeVideo(channelId = "UC123", videoId = "v3", title = "minecraft speedrun", position = 2)
        )

        dao.upsertAll(videos)

        val result = dao.searchVideosInChannel("UC123", "minecraft").first()
        assertThat(result).hasSize(2)
        assertThat(result.map { it.videoId }).containsExactly("v1", "v3")
    }

    @Test
    fun `searchVideosInChannel returns empty for no match`() = runTest {
        val videos = listOf(
            makeVideo(channelId = "UC123", videoId = "v1", title = "Minecraft Tutorial", position = 0)
        )

        dao.upsertAll(videos)

        val result = dao.searchVideosInChannel("UC123", "fortnite").first()
        assertThat(result).isEmpty()
    }

    @Test
    fun `deleteByChannel clears all videos for channel`() = runTest {
        val videos = listOf(
            makeVideo(channelId = "UC123", videoId = "v1", title = "Video 1", position = 0),
            makeVideo(channelId = "UC123", videoId = "v2", title = "Video 2", position = 1),
            makeVideo(channelId = "UC456", videoId = "v3", title = "Other Channel", position = 0)
        )

        dao.upsertAll(videos)
        dao.deleteByChannel("UC123")

        val channel123 = dao.getVideosByChannel("UC123").first()
        val channel456 = dao.getVideosByChannel("UC456").first()
        assertThat(channel123).isEmpty()
        assertThat(channel456).hasSize(1)
    }

    @Test
    fun `upsertAll with duplicate videoId updates existing record`() = runTest {
        val original = makeVideo(channelId = "UC123", videoId = "v1", title = "Original Title", position = 0)
        dao.upsertAll(listOf(original))

        val updated = makeVideo(channelId = "UC123", videoId = "v1", title = "Updated Title", position = 0)
        dao.upsertAll(listOf(updated))

        val result = dao.getVideosByChannel("UC123").first()
        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("Updated Title")
    }

    private fun makeVideo(
        channelId: String,
        videoId: String,
        title: String,
        position: Int
    ) = CachedChannelVideoEntity(
        channelId = channelId,
        videoId = videoId,
        title = title,
        thumbnailUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
        channelTitle = "Test Channel",
        position = position
    )
}
