package io.github.degipe.youtubewhitelist.feature.sleep.ui

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.data.sleep.SleepTimerManager
import io.github.degipe.youtubewhitelist.core.data.sleep.SleepTimerState
import io.github.degipe.youtubewhitelist.core.data.sleep.SleepTimerStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SleepModeViewModelTest {

    private lateinit var sleepTimerManager: SleepTimerManager
    private val timerStateFlow = MutableStateFlow(SleepTimerState())
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sleepTimerManager = mockk(relaxed = true)
        every { sleepTimerManager.state } returns timerStateFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(profileId: String = "profile-1"): SleepModeViewModel {
        val vm = SleepModeViewModel(
            sleepTimerManager = sleepTimerManager,
            profileId = profileId
        )
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `initial state is IDLE with default 30 minutes`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        with(viewModel.uiState.value) {
            assertThat(timerStatus).isEqualTo(SleepTimerStatus.IDLE)
            assertThat(selectedDurationMinutes).isEqualTo(30)
            assertThat(isTimerForThisProfile).isFalse()
        }
    }

    @Test
    fun `selectDuration updates selected duration`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.selectDuration(120)

        advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedDurationMinutes).isEqualTo(120)
    }

    @Test
    fun `selectDuration clamps to minimum 5`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.selectDuration(1)

        advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedDurationMinutes).isEqualTo(5)
    }

    @Test
    fun `selectDuration clamps to maximum 600`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.selectDuration(999)

        advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedDurationMinutes).isEqualTo(600)
    }

    @Test
    fun `startTimer delegates to SleepTimerManager with correct values`() = runTest(testDispatcher) {
        val viewModel = createViewModel("kid-42")
        viewModel.selectDuration(45)
        advanceUntilIdle()

        viewModel.startTimer()

        verify { sleepTimerManager.startTimer("kid-42", 45) }
    }

    @Test
    fun `uiState reflects RUNNING from SleepTimerManager`() = runTest(testDispatcher) {
        val viewModel = createViewModel("profile-1")

        timerStateFlow.value = SleepTimerState(
            status = SleepTimerStatus.RUNNING,
            profileId = "profile-1",
            totalDurationMinutes = 30,
            remainingSeconds = 1500
        )
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertThat(timerStatus).isEqualTo(SleepTimerStatus.RUNNING)
            assertThat(remainingSeconds).isEqualTo(1500)
            assertThat(formattedRemaining).isEqualTo("25m")
            assertThat(isTimerForThisProfile).isTrue()
        }
    }

    @Test
    fun `uiState reflects EXPIRED from SleepTimerManager`() = runTest(testDispatcher) {
        val viewModel = createViewModel("profile-1")

        timerStateFlow.value = SleepTimerState(
            status = SleepTimerStatus.EXPIRED,
            profileId = "profile-1",
            totalDurationMinutes = 30,
            remainingSeconds = 0
        )
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertThat(timerStatus).isEqualTo(SleepTimerStatus.EXPIRED)
            assertThat(isTimerForThisProfile).isTrue()
        }
    }

    @Test
    fun `stopTimer delegates to SleepTimerManager`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.stopTimer()

        verify { sleepTimerManager.stopTimer() }
    }

    @Test
    fun `isTimerForThisProfile true when profileId matches`() = runTest(testDispatcher) {
        val viewModel = createViewModel("profile-1")

        timerStateFlow.value = SleepTimerState(
            status = SleepTimerStatus.RUNNING,
            profileId = "profile-1",
            remainingSeconds = 100
        )
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isTimerForThisProfile).isTrue()
    }

    @Test
    fun `isTimerForThisProfile false when profileId differs`() = runTest(testDispatcher) {
        val viewModel = createViewModel("profile-1")

        timerStateFlow.value = SleepTimerState(
            status = SleepTimerStatus.RUNNING,
            profileId = "profile-OTHER",
            remainingSeconds = 100
        )
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isTimerForThisProfile).isFalse()
    }

    @Test
    fun `timer running for different profile shows IDLE-like state`() = runTest(testDispatcher) {
        val viewModel = createViewModel("profile-1")

        timerStateFlow.value = SleepTimerState(
            status = SleepTimerStatus.RUNNING,
            profileId = "profile-OTHER",
            remainingSeconds = 500
        )
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertThat(isTimerForThisProfile).isFalse()
            // timerStatus still reflects global state
            assertThat(timerStatus).isEqualTo(SleepTimerStatus.RUNNING)
        }
    }

    @Test
    fun `formattedRemaining shows hours and minutes`() = runTest(testDispatcher) {
        val viewModel = createViewModel("profile-1")

        timerStateFlow.value = SleepTimerState(
            status = SleepTimerStatus.RUNNING,
            profileId = "profile-1",
            remainingSeconds = 7261 // 2h 1m 1s
        )
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.formattedRemaining).isEqualTo("2h 1m")
    }
}
