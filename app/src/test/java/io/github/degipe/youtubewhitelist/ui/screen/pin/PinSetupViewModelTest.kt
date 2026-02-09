package io.github.degipe.youtubewhitelist.ui.screen.pin

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.data.repository.PinRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
class PinSetupViewModelTest {

    private lateinit var pinRepository: PinRepository
    private lateinit var viewModel: PinSetupViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        pinRepository = mockk(relaxed = true)
        viewModel = PinSetupViewModel(pinRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is EnterNew`() {
        val state = viewModel.uiState.value
        assertThat(state.step).isEqualTo(PinSetupStep.ENTER_NEW)
        assertThat(state.pin).isEmpty()
        assertThat(state.error).isNull()
    }

    @Test
    fun `entering digits updates pin`() {
        viewModel.onDigitEntered(1)
        viewModel.onDigitEntered(2)
        viewModel.onDigitEntered(3)

        assertThat(viewModel.uiState.value.pin).isEqualTo("123")
    }

    @Test
    fun `backspace removes last digit`() {
        viewModel.onDigitEntered(1)
        viewModel.onDigitEntered(2)
        viewModel.onBackspace()

        assertThat(viewModel.uiState.value.pin).isEqualTo("1")
    }

    @Test
    fun `backspace on empty pin does nothing`() {
        viewModel.onBackspace()
        assertThat(viewModel.uiState.value.pin).isEmpty()
    }

    @Test
    fun `pin limited to 6 digits`() {
        repeat(8) { viewModel.onDigitEntered(it % 10) }
        assertThat(viewModel.uiState.value.pin).hasLength(6)
    }

    @Test
    fun `submit with less than 4 digits shows error`() {
        viewModel.onDigitEntered(1)
        viewModel.onDigitEntered(2)
        viewModel.onDigitEntered(3)
        viewModel.onSubmit()

        assertThat(viewModel.uiState.value.error).isNotNull()
        assertThat(viewModel.uiState.value.step).isEqualTo(PinSetupStep.ENTER_NEW)
    }

    @Test
    fun `submit with 4 digits moves to confirm step`() {
        enterPin("1234")
        viewModel.onSubmit()

        assertThat(viewModel.uiState.value.step).isEqualTo(PinSetupStep.CONFIRM)
        assertThat(viewModel.uiState.value.pin).isEmpty()
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun `confirm with matching pin calls setupPin`() = runTest(testDispatcher) {
        coEvery { pinRepository.setupPin("1234") } returns Unit

        enterPin("1234")
        viewModel.onSubmit() // move to confirm
        enterPin("1234")
        viewModel.onSubmit() // confirm

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { pinRepository.setupPin("1234") }
        assertThat(viewModel.uiState.value.isComplete).isTrue()
    }

    @Test
    fun `confirm with non-matching pin shows error`() {
        enterPin("1234")
        viewModel.onSubmit() // move to confirm
        enterPin("5678")
        viewModel.onSubmit() // mismatched confirm

        assertThat(viewModel.uiState.value.error).isNotNull()
        assertThat(viewModel.uiState.value.step).isEqualTo(PinSetupStep.ENTER_NEW)
    }

    private fun enterPin(pin: String) {
        pin.forEach { viewModel.onDigitEntered(it.digitToInt()) }
    }
}
