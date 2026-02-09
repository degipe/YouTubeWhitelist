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
class PinEntryViewModelTest {

    private lateinit var pinRepository: PinRepository
    private lateinit var viewModel: PinEntryViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        pinRepository = mockk(relaxed = true)
        viewModel = PinEntryViewModel(pinRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is idle`() {
        val state = viewModel.uiState.value
        assertThat(state.pin).isEmpty()
        assertThat(state.error).isNull()
        assertThat(state.isVerified).isFalse()
        assertThat(state.isLockedOut).isFalse()
    }

    @Test
    fun `correct pin sets isVerified`() = runTest(testDispatcher) {
        coEvery { pinRepository.verifyPin("1234") } returns PinVerificationResult.Success

        enterPin("1234")
        viewModel.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isVerified).isTrue()
    }

    @Test
    fun `incorrect pin shows error with attempts remaining`() = runTest(testDispatcher) {
        coEvery { pinRepository.verifyPin("9999") } returns PinVerificationResult.Failure(4)

        enterPin("9999")
        viewModel.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isVerified).isFalse()
        assertThat(state.error).isNotNull()
        assertThat(state.attemptsRemaining).isEqualTo(4)
        assertThat(state.pin).isEmpty()
    }

    @Test
    fun `locked out shows lockout state`() = runTest(testDispatcher) {
        coEvery { pinRepository.verifyPin("1234") } returns PinVerificationResult.LockedOut(30)

        enterPin("1234")
        viewModel.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLockedOut).isTrue()
        assertThat(state.lockoutSeconds).isEqualTo(30)
    }

    @Test
    fun `entering digits updates pin`() {
        viewModel.onDigitEntered(5)
        viewModel.onDigitEntered(6)
        assertThat(viewModel.uiState.value.pin).isEqualTo("56")
    }

    @Test
    fun `backspace removes last digit`() {
        viewModel.onDigitEntered(1)
        viewModel.onDigitEntered(2)
        viewModel.onBackspace()
        assertThat(viewModel.uiState.value.pin).isEqualTo("1")
    }

    private fun enterPin(pin: String) {
        pin.forEach { viewModel.onDigitEntered(it.digitToInt()) }
    }
}
