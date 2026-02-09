package io.github.degipe.youtubewhitelist.ui.screen.pin

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.data.model.PinVerificationResult
import io.github.degipe.youtubewhitelist.core.data.repository.PinRepository
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
class PinChangeViewModelTest {

    private lateinit var pinRepository: PinRepository
    private lateinit var viewModel: PinChangeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        pinRepository = mockk(relaxed = true)
        viewModel = PinChangeViewModel(pinRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is verify old pin`() {
        val state = viewModel.uiState.value
        assertThat(state.step).isEqualTo(PinChangeStep.VERIFY_OLD)
        assertThat(state.pin).isEmpty()
    }

    @Test
    fun `verify old pin success moves to enter new`() = runTest(testDispatcher) {
        coEvery { pinRepository.verifyPin("1234") } returns PinVerificationResult.Success

        enterPin("1234")
        viewModel.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.step).isEqualTo(PinChangeStep.ENTER_NEW)
        assertThat(viewModel.uiState.value.pin).isEmpty()
    }

    @Test
    fun `verify old pin failure shows error`() = runTest(testDispatcher) {
        coEvery { pinRepository.verifyPin("9999") } returns PinVerificationResult.Failure(4)

        enterPin("9999")
        viewModel.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.step).isEqualTo(PinChangeStep.VERIFY_OLD)
        assertThat(viewModel.uiState.value.error).isNotNull()
    }

    @Test
    fun `enter new pin moves to confirm`() = runTest(testDispatcher) {
        coEvery { pinRepository.verifyPin("1234") } returns PinVerificationResult.Success

        // Step 1: verify old
        enterPin("1234")
        viewModel.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        // Step 2: enter new
        enterPin("5678")
        viewModel.onSubmit()

        assertThat(viewModel.uiState.value.step).isEqualTo(PinChangeStep.CONFIRM_NEW)
        assertThat(viewModel.uiState.value.pin).isEmpty()
    }

    @Test
    fun `confirm matching pin completes change`() = runTest(testDispatcher) {
        coEvery { pinRepository.verifyPin("1234") } returns PinVerificationResult.Success
        coEvery { pinRepository.changePin("1234", "5678") } returns PinVerificationResult.Success

        // Step 1: verify old
        enterPin("1234")
        viewModel.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        // Step 2: enter new
        enterPin("5678")
        viewModel.onSubmit()

        // Step 3: confirm new
        enterPin("5678")
        viewModel.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isComplete).isTrue()
    }

    @Test
    fun `confirm non-matching pin shows error and resets`() = runTest(testDispatcher) {
        coEvery { pinRepository.verifyPin("1234") } returns PinVerificationResult.Success

        // Step 1: verify old
        enterPin("1234")
        viewModel.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        // Step 2: enter new
        enterPin("5678")
        viewModel.onSubmit()

        // Step 3: wrong confirm
        enterPin("9999")
        viewModel.onSubmit()

        assertThat(viewModel.uiState.value.error).isNotNull()
        assertThat(viewModel.uiState.value.step).isEqualTo(PinChangeStep.ENTER_NEW)
    }

    private fun enterPin(pin: String) {
        pin.forEach { viewModel.onDigitEntered(it.digitToInt()) }
    }
}
