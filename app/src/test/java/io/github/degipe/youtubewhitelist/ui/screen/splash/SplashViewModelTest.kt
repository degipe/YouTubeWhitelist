package io.github.degipe.youtubewhitelist.ui.screen.splash

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.data.model.KidProfile
import io.github.degipe.youtubewhitelist.core.data.model.ParentAccount
import io.github.degipe.youtubewhitelist.core.data.repository.KidProfileRepository
import io.github.degipe.youtubewhitelist.core.data.repository.ParentAccountRepository
import io.mockk.coEvery
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
class SplashViewModelTest {

    private lateinit var parentAccountRepository: ParentAccountRepository
    private lateinit var kidProfileRepository: KidProfileRepository
    private val testDispatcher = StandardTestDispatcher()

    private val testAccount = ParentAccount(
        id = "account-1",
        googleAccountId = "google-1",
        email = "test@test.com",
        isPinSet = true,
        biometricEnabled = false,
        isPremium = false,
        createdAt = 1000L
    )

    private val testProfile = KidProfile(
        id = "profile-1",
        parentAccountId = "account-1",
        name = "Kid",
        avatarUrl = null,
        dailyLimitMinutes = null,
        sleepPlaylistId = null,
        createdAt = 1000L
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

    @Test
    fun `first run when no account exists`() = runTest(testDispatcher) {
        coEvery { parentAccountRepository.hasAccount() } returns false

        val viewModel = SplashViewModel(parentAccountRepository, kidProfileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value).isEqualTo(SplashUiState.FirstRun)
    }

    @Test
    fun `returning user when account and profile exist`() = runTest(testDispatcher) {
        coEvery { parentAccountRepository.hasAccount() } returns true
        every { parentAccountRepository.getAccount() } returns flowOf(testAccount)
        every { kidProfileRepository.getProfilesByParent("account-1") } returns flowOf(listOf(testProfile))

        val viewModel = SplashViewModel(parentAccountRepository, kidProfileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value).isEqualTo(SplashUiState.ReturningUser("profile-1"))
    }

    @Test
    fun `first run when account exists but no profiles`() = runTest(testDispatcher) {
        coEvery { parentAccountRepository.hasAccount() } returns true
        every { parentAccountRepository.getAccount() } returns flowOf(testAccount)
        every { kidProfileRepository.getProfilesByParent("account-1") } returns flowOf(emptyList())

        val viewModel = SplashViewModel(parentAccountRepository, kidProfileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value).isEqualTo(SplashUiState.FirstRun)
    }

    @Test
    fun `first run when account flow returns null`() = runTest(testDispatcher) {
        coEvery { parentAccountRepository.hasAccount() } returns true
        every { parentAccountRepository.getAccount() } returns flowOf(null)

        val viewModel = SplashViewModel(parentAccountRepository, kidProfileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value).isEqualTo(SplashUiState.FirstRun)
    }

    @Test
    fun `returning user picks first profile id when single profile`() = runTest(testDispatcher) {
        coEvery { parentAccountRepository.hasAccount() } returns true
        every { parentAccountRepository.getAccount() } returns flowOf(testAccount)
        every { kidProfileRepository.getProfilesByParent("account-1") } returns flowOf(listOf(testProfile))

        val viewModel = SplashViewModel(parentAccountRepository, kidProfileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value).isEqualTo(SplashUiState.ReturningUser("profile-1"))
    }

    @Test
    fun `multiple profiles navigates to profile selector`() = runTest(testDispatcher) {
        val profiles = listOf(
            testProfile,
            testProfile.copy(id = "profile-2", name = "Kid 2")
        )
        coEvery { parentAccountRepository.hasAccount() } returns true
        every { parentAccountRepository.getAccount() } returns flowOf(testAccount)
        every { kidProfileRepository.getProfilesByParent("account-1") } returns flowOf(profiles)

        val viewModel = SplashViewModel(parentAccountRepository, kidProfileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value).isEqualTo(SplashUiState.MultipleProfiles)
    }

    @Test
    fun `initial state is loading`() = runTest(testDispatcher) {
        coEvery { parentAccountRepository.hasAccount() } returns false

        val viewModel = SplashViewModel(parentAccountRepository, kidProfileRepository)

        assertThat(viewModel.uiState.value).isEqualTo(SplashUiState.Loading)
    }
}
