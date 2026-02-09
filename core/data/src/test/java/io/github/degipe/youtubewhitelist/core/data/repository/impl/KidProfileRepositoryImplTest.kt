package io.github.degipe.youtubewhitelist.core.data.repository.impl

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.database.dao.KidProfileDao
import io.github.degipe.youtubewhitelist.core.database.entity.KidProfileEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class KidProfileRepositoryImplTest {

    private lateinit var kidProfileDao: KidProfileDao
    private lateinit var repository: KidProfileRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        kidProfileDao = mockk(relaxed = true)
        repository = KidProfileRepositoryImpl(kidProfileDao, testDispatcher)
    }

    @Test
    fun `getProfilesByParent maps entities to domain`() = runTest(testDispatcher) {
        val entities = listOf(
            KidProfileEntity(
                id = "prof1",
                parentAccountId = "parent1",
                name = "Kid One",
                avatarUrl = "https://img/avatar.jpg",
                dailyLimitMinutes = 60,
                createdAt = 1000L
            ),
            KidProfileEntity(
                id = "prof2",
                parentAccountId = "parent1",
                name = "Kid Two",
                createdAt = 2000L
            )
        )
        every { kidProfileDao.getProfilesByParent("parent1") } returns flowOf(entities)

        val profiles = repository.getProfilesByParent("parent1").first()

        assertThat(profiles).hasSize(2)
        assertThat(profiles[0].id).isEqualTo("prof1")
        assertThat(profiles[0].name).isEqualTo("Kid One")
        assertThat(profiles[0].avatarUrl).isEqualTo("https://img/avatar.jpg")
        assertThat(profiles[0].dailyLimitMinutes).isEqualTo(60)
        assertThat(profiles[0].sleepPlaylistId).isNull()
        assertThat(profiles[1].id).isEqualTo("prof2")
        assertThat(profiles[1].name).isEqualTo("Kid Two")
        assertThat(profiles[1].avatarUrl).isNull()
    }

    @Test
    fun `getProfileById maps entity to domain`() = runTest(testDispatcher) {
        val entity = KidProfileEntity(
            id = "prof1",
            parentAccountId = "parent1",
            name = "Kid One",
            createdAt = 1000L
        )
        every { kidProfileDao.getProfileById("prof1") } returns flowOf(entity)

        val profile = repository.getProfileById("prof1").first()

        assertThat(profile).isNotNull()
        assertThat(profile!!.id).isEqualTo("prof1")
        assertThat(profile.name).isEqualTo("Kid One")
    }

    @Test
    fun `getProfileById returns null for nonexistent profile`() = runTest(testDispatcher) {
        every { kidProfileDao.getProfileById("nonexistent") } returns flowOf(null)

        val profile = repository.getProfileById("nonexistent").first()

        assertThat(profile).isNull()
    }

    @Test
    fun `createProfile generates id and inserts`() = runTest(testDispatcher) {
        val entitySlot = slot<KidProfileEntity>()
        coEvery { kidProfileDao.insert(capture(entitySlot)) } returns Unit

        val profile = repository.createProfile("parent1", "New Kid", "https://img/avatar.jpg")

        assertThat(profile.id).isNotEmpty()
        assertThat(profile.parentAccountId).isEqualTo("parent1")
        assertThat(profile.name).isEqualTo("New Kid")
        assertThat(profile.avatarUrl).isEqualTo("https://img/avatar.jpg")

        assertThat(entitySlot.captured.parentAccountId).isEqualTo("parent1")
        assertThat(entitySlot.captured.name).isEqualTo("New Kid")
    }

    @Test
    fun `deleteProfile delegates to dao`() = runTest(testDispatcher) {
        val entity = KidProfileEntity(
            id = "prof1",
            parentAccountId = "parent1",
            name = "Kid",
            createdAt = 1000L
        )
        coEvery { kidProfileDao.getProfileByIdOnce("prof1") } returns entity

        repository.deleteProfile("prof1")

        coVerify { kidProfileDao.delete(entity) }
    }

    @Test
    fun `getProfileCount delegates to dao`() = runTest(testDispatcher) {
        coEvery { kidProfileDao.getProfileCount("parent1") } returns 3

        val count = repository.getProfileCount("parent1")
        assertThat(count).isEqualTo(3)
    }

    @Test
    fun `createProfile with null avatar`() = runTest(testDispatcher) {
        val profile = repository.createProfile("parent1", "Kid", null)

        assertThat(profile.avatarUrl).isNull()
    }
}
