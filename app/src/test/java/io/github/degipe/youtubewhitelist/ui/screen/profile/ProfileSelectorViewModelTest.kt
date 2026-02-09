package io.github.degipe.youtubewhitelist.ui.screen.profile

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.data.model.KidProfile
import io.github.degipe.youtubewhitelist.core.data.model.ParentAccount
import io.github.degipe.youtubewhitelist.core.data.repository.KidProfileRepository
import io.github.degipe.youtubewhitelist.core.data.repository.ParentAccountRepository
import io.mockk.every
import io.mockk.mockk
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
class ProfileSelectorViewModelTest {

    private lateinit var parentAccountRepository: ParentAccountRepository
    private lateinit var kidProfileRepository: KidProfileRepository
    private val testDispatcher = StandardTestDispatcher()

    private val testAccount = ParentAccount(
        id = "account-1", googleAccountId = "google-1",
        email = "test@test.com", isPinSet = true,
        biometricEnabled = false, isPremium = false, createdAt = 1000L
    )

    private val profile1 = KidProfile(
        id = "profile-1", parentAccountId = "account-1",
        name = "Bence", avatarUrl = null,
        dailyLimitMinutes = null, sleepPlaylistId = null, createdAt = 1000L
    )

    private val profile2 = KidProfile(
        id = "profile-2", parentAccountId = "account-1",
        name = "Emma", avatarUrl = null,
        dailyLimitMinutes = 60, sleepPlaylistId = null, createdAt = 2000L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        parentAccountRepository = mockk()
        kidProfileRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ProfileSelectorViewModel {
        return ProfileSelectorViewModel(parentAccountRepository, kidProfileRepository)
    }

    @Test
    fun `initial state is loading`() = runTest(testDispatcher) {
        every { parentAccountRepository.getAccount() } returns flowOf(testAccount)
        every { kidProfileRepository.getProfilesByParent("account-1") } returns flowOf(emptyList())

        val viewModel = createViewModel()
        assertThat(viewModel.uiState.value.isLoading).isTrue()
    }

    @Test
    fun `loads profiles`() = runTest(testDispatcher) {
        every { parentAccountRepository.getAccount() } returns flowOf(testAccount)
        every { kidProfileRepository.getProfilesByParent("account-1") } returns flowOf(listOf(profile1, profile2))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.profiles).hasSize(2)
        assertThat(viewModel.uiState.value.profiles[0].name).isEqualTo("Bence")
        assertThat(viewModel.uiState.value.profiles[1].name).isEqualTo("Emma")
    }

    @Test
    fun `empty profiles`() = runTest(testDispatcher) {
        every { parentAccountRepository.getAccount() } returns flowOf(testAccount)
        every { kidProfileRepository.getProfilesByParent("account-1") } returns flowOf(emptyList())

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.profiles).isEmpty()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `no account shows empty profiles`() = runTest(testDispatcher) {
        every { parentAccountRepository.getAccount() } returns flowOf(null)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.profiles).isEmpty()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `isLoading false after profiles loaded`() = runTest(testDispatcher) {
        every { parentAccountRepository.getAccount() } returns flowOf(testAccount)
        every { kidProfileRepository.getProfilesByParent("account-1") } returns flowOf(listOf(profile1))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `profiles include daily limit info`() = runTest(testDispatcher) {
        every { parentAccountRepository.getAccount() } returns flowOf(testAccount)
        every { kidProfileRepository.getProfilesByParent("account-1") } returns flowOf(listOf(profile2))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.profiles[0].dailyLimitMinutes).isEqualTo(60)
    }
}
