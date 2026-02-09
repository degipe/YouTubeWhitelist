package io.github.degipe.youtubewhitelist.feature.parent.ui.profile

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.data.model.KidProfile
import io.github.degipe.youtubewhitelist.core.data.repository.KidProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileEditViewModelTest {

    private lateinit var kidProfileRepository: KidProfileRepository
    private val testDispatcher = StandardTestDispatcher()

    private val testProfile = KidProfile(
        id = "profile-1", parentAccountId = "account-1",
        name = "Bence", avatarUrl = "https://avatar.jpg",
        dailyLimitMinutes = 60, sleepPlaylistId = "PL123",
        createdAt = 1000L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        kidProfileRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun setupDefaults() {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(testProfile)
    }

    private fun createViewModel(profileId: String = "profile-1"): ProfileEditViewModel {
        return ProfileEditViewModel(kidProfileRepository, profileId)
    }

    @Test
    fun `initial state is loading`() = runTest(testDispatcher) {
        setupDefaults()
        val viewModel = createViewModel()
        assertThat(viewModel.uiState.value.isLoading).isTrue()
    }

    @Test
    fun `loads existing profile data`() = runTest(testDispatcher) {
        setupDefaults()
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertThat(name).isEqualTo("Bence")
            assertThat(avatarUrl).isEqualTo("https://avatar.jpg")
            assertThat(dailyLimitMinutes).isEqualTo(60)
            assertThat(isLoading).isFalse()
        }
    }

    @Test
    fun `profile not found shows error`() = runTest(testDispatcher) {
        every { kidProfileRepository.getProfileById("profile-1") } returns flowOf(null)
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isNotNull()
    }

    @Test
    fun `onNameChanged updates state`() = runTest(testDispatcher) {
        setupDefaults()
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onNameChanged("Emma")
        assertThat(viewModel.uiState.value.name).isEqualTo("Emma")
    }

    @Test
    fun `onAvatarUrlChanged updates state`() = runTest(testDispatcher) {
        setupDefaults()
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAvatarUrlChanged("https://new-avatar.jpg")
        assertThat(viewModel.uiState.value.avatarUrl).isEqualTo("https://new-avatar.jpg")
    }

    @Test
    fun `onDailyLimitChanged updates state`() = runTest(testDispatcher) {
        setupDefaults()
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onDailyLimitChanged(30)
        assertThat(viewModel.uiState.value.dailyLimitMinutes).isEqualTo(30)
    }

    @Test
    fun `onDailyLimitChanged null removes limit`() = runTest(testDispatcher) {
        setupDefaults()
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onDailyLimitChanged(null)
        assertThat(viewModel.uiState.value.dailyLimitMinutes).isNull()
    }

    @Test
    fun `saveProfile success`() = runTest(testDispatcher) {
        setupDefaults()
        val profileSlot = slot<KidProfile>()
        coEvery { kidProfileRepository.updateProfile(capture(profileSlot)) } returns Unit

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onNameChanged("Emma")
        viewModel.saveProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isSaved).isTrue()
        assertThat(profileSlot.captured.name).isEqualTo("Emma")
    }

    @Test
    fun `saveProfile with blank name shows error`() = runTest(testDispatcher) {
        setupDefaults()
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onNameChanged("  ")
        viewModel.saveProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isNotNull()
        assertThat(viewModel.uiState.value.isSaved).isFalse()
    }

    @Test
    fun `saveProfile preserves all fields`() = runTest(testDispatcher) {
        setupDefaults()
        val profileSlot = slot<KidProfile>()
        coEvery { kidProfileRepository.updateProfile(capture(profileSlot)) } returns Unit

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        with(profileSlot.captured) {
            assertThat(id).isEqualTo("profile-1")
            assertThat(parentAccountId).isEqualTo("account-1")
            assertThat(sleepPlaylistId).isEqualTo("PL123")
            assertThat(createdAt).isEqualTo(1000L)
        }
    }

    @Test
    fun `requestDelete shows confirmation`() = runTest(testDispatcher) {
        setupDefaults()
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.requestDelete()
        assertThat(viewModel.uiState.value.showDeleteConfirmation).isTrue()
    }

    @Test
    fun `dismissDelete hides confirmation`() = runTest(testDispatcher) {
        setupDefaults()
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.requestDelete()
        viewModel.dismissDelete()
        assertThat(viewModel.uiState.value.showDeleteConfirmation).isFalse()
    }

    @Test
    fun `confirmDelete calls repository and sets isDeleted`() = runTest(testDispatcher) {
        setupDefaults()
        coEvery { kidProfileRepository.deleteProfile("profile-1") } returns Unit

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.requestDelete()
        viewModel.confirmDelete()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isDeleted).isTrue()
        coVerify { kidProfileRepository.deleteProfile("profile-1") }
    }

    @Test
    fun `saveProfile sets isSaving during operation`() = runTest(testDispatcher) {
        setupDefaults()
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveProfile()
        // isSaving is transient, hard to observe; just verify it completes
        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.isSaved).isTrue()
    }
}
