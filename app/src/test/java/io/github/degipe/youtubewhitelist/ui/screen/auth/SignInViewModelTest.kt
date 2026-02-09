package io.github.degipe.youtubewhitelist.ui.screen.auth

import android.content.Context
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
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
class SignInViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: SignInViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        viewModel = SignInViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is idle`() {
        assertThat(viewModel.uiState.value).isEqualTo(SignInUiState.Idle)
    }

    @Test
    fun `signIn sets loading then success`() = runTest(testDispatcher) {
        val gate = CompletableDeferred<Unit>()
        val context = mockk<Context>()
        coEvery { authRepository.signIn(context) } coAnswers { gate.await() }

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(SignInUiState.Idle)

            viewModel.signIn(context)
            assertThat(awaitItem()).isEqualTo(SignInUiState.Loading)

            gate.complete(Unit)
            assertThat(awaitItem()).isEqualTo(SignInUiState.Success)
        }
        coVerify { authRepository.signIn(context) }
    }

    @Test
    fun `signIn sets error on failure`() = runTest(testDispatcher) {
        val context = mockk<Context>()
        coEvery { authRepository.signIn(context) } throws Exception("Network error")

        viewModel.signIn(context)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state).isInstanceOf(SignInUiState.Error::class.java)
        assertThat((state as SignInUiState.Error).message).isEqualTo("Network error")
    }
}
