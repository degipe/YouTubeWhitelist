package io.github.degipe.youtubewhitelist.core.data.sleep

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SleepTimerManagerImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var manager: SleepTimerManagerImpl

    @Before
    fun setUp() {
        manager = SleepTimerManagerImpl(testScope)
    }

    @Test
    fun `initial state is IDLE`() {
        val state = manager.state.value
        assertThat(state.status).isEqualTo(SleepTimerStatus.IDLE)
        assertThat(state.profileId).isNull()
        assertThat(state.remainingSeconds).isEqualTo(0)
        assertThat(state.totalDurationMinutes).isEqualTo(0)
    }

    @Test
    fun `startTimer transitions to RUNNING with correct values`() = testScope.runTest {
        manager.startTimer("profile-1", 30)

        val state = manager.state.value
        assertThat(state.status).isEqualTo(SleepTimerStatus.RUNNING)
        assertThat(state.profileId).isEqualTo("profile-1")
        assertThat(state.remainingSeconds).isEqualTo(1800)
        assertThat(state.totalDurationMinutes).isEqualTo(30)
    }

    @Test
    fun `timer counts down every second`() = testScope.runTest {
        manager.startTimer("profile-1", 5)

        advanceTimeBy(5_001) // 5 seconds (boundary-exclusive, +1ms)

        assertThat(manager.state.value.remainingSeconds).isEqualTo(295) // 300 - 5
        assertThat(manager.state.value.status).isEqualTo(SleepTimerStatus.RUNNING)
    }

    @Test
    fun `timer expires after full duration`() = testScope.runTest {
        manager.startTimer("profile-1", 1) // 1 minute = 60 seconds

        advanceTimeBy(60_001) // 60 seconds
        advanceUntilIdle()

        assertThat(manager.state.value.status).isEqualTo(SleepTimerStatus.EXPIRED)
        assertThat(manager.state.value.remainingSeconds).isEqualTo(0)
    }

    @Test
    fun `stopTimer resets to IDLE during RUNNING`() = testScope.runTest {
        manager.startTimer("profile-1", 30)
        advanceTimeBy(5_001)

        manager.stopTimer()

        val state = manager.state.value
        assertThat(state.status).isEqualTo(SleepTimerStatus.IDLE)
        assertThat(state.profileId).isNull()
        assertThat(state.remainingSeconds).isEqualTo(0)
    }

    @Test
    fun `stopTimer resets to IDLE when EXPIRED`() = testScope.runTest {
        manager.startTimer("profile-1", 1)
        advanceTimeBy(60_001)
        advanceUntilIdle()
        assertThat(manager.state.value.status).isEqualTo(SleepTimerStatus.EXPIRED)

        manager.stopTimer()

        assertThat(manager.state.value.status).isEqualTo(SleepTimerStatus.IDLE)
    }

    @Test
    fun `startTimer cancels previous timer`() = testScope.runTest {
        manager.startTimer("profile-1", 30) // 1800s
        advanceTimeBy(60_001) // advance 60s → 1740s remaining

        manager.startTimer("profile-2", 15) // new timer: 900s

        val state = manager.state.value
        assertThat(state.profileId).isEqualTo("profile-2")
        assertThat(state.remainingSeconds).isEqualTo(900)
        assertThat(state.totalDurationMinutes).isEqualTo(15)
    }

    @Test
    fun `stopTimer is noop when IDLE`() = testScope.runTest {
        manager.stopTimer()

        assertThat(manager.state.value.status).isEqualTo(SleepTimerStatus.IDLE)
    }

    @Test
    fun `profileId is preserved during countdown`() = testScope.runTest {
        manager.startTimer("kid-42", 10)
        advanceTimeBy(30_001)

        assertThat(manager.state.value.profileId).isEqualTo("kid-42")
    }

    @Test
    fun `formattedRemaining formats hours and minutes`() {
        val state = SleepTimerState(
            status = SleepTimerStatus.RUNNING,
            remainingSeconds = 3661 // 1h 1m 1s
        )
        assertThat(state.formattedRemaining).isEqualTo("1h 1m")
    }

    @Test
    fun `formattedRemaining formats minutes only`() {
        val state = SleepTimerState(
            status = SleepTimerStatus.RUNNING,
            remainingSeconds = 300 // 5m 0s
        )
        assertThat(state.formattedRemaining).isEqualTo("5m")
    }

    @Test
    fun `formattedRemaining formats zero`() {
        val state = SleepTimerState(
            status = SleepTimerStatus.EXPIRED,
            remainingSeconds = 0
        )
        assertThat(state.formattedRemaining).isEqualTo("0m")
    }

    @Test
    fun `totalDurationMinutes preserved after countdown`() = testScope.runTest {
        manager.startTimer("profile-1", 120)
        advanceTimeBy(10_001)

        assertThat(manager.state.value.totalDurationMinutes).isEqualTo(120)
    }
}
