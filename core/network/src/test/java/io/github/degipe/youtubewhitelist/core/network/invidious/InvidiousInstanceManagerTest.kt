package io.github.degipe.youtubewhitelist.core.network.invidious

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class InvidiousInstanceManagerTest {

    private val testInstances = listOf("instance1.com", "instance2.com", "instance3.com")
    private var currentTime = 0L
    private val timeProvider: () -> Long = { currentTime }

    private fun createManager() = InvidiousInstanceManager(
        instances = testInstances,
        healthResetMs = 5 * 60 * 1000L,
        timeProvider = timeProvider
    )

    @Test
    fun `healthy instance used first`() {
        val manager = createManager()

        val instance = manager.getHealthyInstance()

        assertThat(instance).isEqualTo("https://instance1.com")
    }

    @Test
    fun `round robin cycles through instances`() {
        val manager = createManager()

        val first = manager.getHealthyInstance()
        val second = manager.getHealthyInstance()
        val third = manager.getHealthyInstance()
        val fourth = manager.getHealthyInstance()

        assertThat(first).isEqualTo("https://instance1.com")
        assertThat(second).isEqualTo("https://instance2.com")
        assertThat(third).isEqualTo("https://instance3.com")
        assertThat(fourth).isEqualTo("https://instance1.com")
    }

    @Test
    fun `failed instance skipped after max failures`() {
        val manager = createManager()

        // First call returns instance1
        manager.getHealthyInstance()
        // Mark instance1 as failed twice (MAX_FAILURES = 2)
        manager.reportFailure("https://instance1.com")
        manager.reportFailure("https://instance1.com")

        // Next call should skip instance1 since currentIndex is at instance2
        val next = manager.getHealthyInstance()
        assertThat(next).isEqualTo("https://instance2.com")

        // And instance2 next
        val afterThat = manager.getHealthyInstance()
        assertThat(afterThat).isEqualTo("https://instance3.com")

        // Should cycle back to instance2 (skipping instance1)
        val cycled = manager.getHealthyInstance()
        assertThat(cycled).isEqualTo("https://instance2.com")
    }

    @Test
    fun `all instances down returns null`() {
        val manager = createManager()

        for (instance in testInstances) {
            manager.reportFailure("https://$instance")
            manager.reportFailure("https://$instance")
        }

        val result = manager.getHealthyInstance()

        assertThat(result).isNull()
    }

    @Test
    fun `instance health resets after timeout`() {
        val manager = createManager()

        // Mark instance1 as failed
        manager.reportFailure("https://instance1.com")
        manager.reportFailure("https://instance1.com")

        // Advance time past the reset threshold
        currentTime = 5 * 60 * 1000L + 1

        val instance = manager.getHealthyInstance()

        // instance1 should be healthy again
        assertThat(instance).isEqualTo("https://instance1.com")
    }

    @Test
    fun `report success resets failure count`() {
        val manager = createManager()

        manager.reportFailure("https://instance1.com")
        manager.reportSuccess("https://instance1.com")
        manager.reportFailure("https://instance1.com")

        // Should still be available (only 1 consecutive failure, needs 2)
        val instance = manager.getHealthyInstance()
        assertThat(instance).isEqualTo("https://instance1.com")
    }
}
