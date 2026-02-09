package io.github.degipe.youtubewhitelist.ui.screen.splash

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.data.repository.ParentAccountRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        parentAccountRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `first run when no account exists`() = runTest(testDispatcher) {
        coEvery { parentAccountRepository.hasAccount() } returns false

        val viewModel = SplashViewModel(parentAccountRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value).isEqualTo(SplashUiState.FirstRun)
    }

    @Test
    fun `returning user when account exists`() = runTest(testDispatcher) {
        coEvery { parentAccountRepository.hasAccount() } returns true

        val viewModel = SplashViewModel(parentAccountRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value).isEqualTo(SplashUiState.ReturningUser)
    }

    @Test
    fun `initial state is loading`() = runTest(testDispatcher) {
        coEvery { parentAccountRepository.hasAccount() } returns false

        val viewModel = SplashViewModel(parentAccountRepository)

        assertThat(viewModel.uiState.value).isEqualTo(SplashUiState.Loading)
    }
}
