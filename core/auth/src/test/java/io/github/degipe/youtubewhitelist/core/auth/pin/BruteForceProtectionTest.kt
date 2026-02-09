package io.github.degipe.youtubewhitelist.core.auth.pin

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Before
import org.junit.Test

class BruteForceProtectionTest {

    private lateinit var protection: BruteForceProtection
    private var currentTime = 1000000L
    private val prefsMap = mutableMapOf<String, Any>()
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    @Before
    fun setUp() {
        editor = mockk<SharedPreferences.Editor>(relaxed = true) {
            val intSlot = slot<Int>()
            val longSlot = slot<Long>()
            val keySlot = slot<String>()
            every { putInt(capture(keySlot), capture(intSlot)) } answers {
                prefsMap[keySlot.captured] = intSlot.captured
                this@mockk
            }
            every { putLong(capture(keySlot), capture(longSlot)) } answers {
                prefsMap[keySlot.captured] = longSlot.captured
                this@mockk
            }
            every { remove(capture(keySlot)) } answers {
                prefsMap.remove(keySlot.captured)
                this@mockk
            }
            every { clear() } answers {
                prefsMap.clear()
                this@mockk
            }
            every { apply() } answers { }
        }

        prefs = mockk<SharedPreferences> {
            every { getInt(any(), any()) } answers {
                (prefsMap[firstArg()] as? Int) ?: secondArg()
            }
            every { getLong(any(), any()) } answers {
                (prefsMap[firstArg()] as? Long) ?: secondArg()
            }
            every { edit() } returns editor
        }

        protection = BruteForceProtection(prefs) { currentTime }
    }

    @Test
    fun `initially not locked out`() {
        assertThat(protection.isLockedOut()).isFalse()
        assertThat(protection.getFailCount()).isEqualTo(0)
    }

    @Test
    fun `after 4 failures not locked out`() {
        repeat(4) { protection.recordFailure() }
        assertThat(protection.isLockedOut()).isFalse()
        assertThat(protection.getFailCount()).isEqualTo(4)
    }

    @Test
    fun `after 5 failures locked out for 30 seconds`() {
        repeat(5) { protection.recordFailure() }
        assertThat(protection.isLockedOut()).isTrue()
        assertThat(protection.getLockoutRemainingSeconds()).isEqualTo(30)
    }

    @Test
    fun `after 10 failures locked out for 60 seconds`() {
        repeat(10) { protection.recordFailure() }
        assertThat(protection.isLockedOut()).isTrue()
        assertThat(protection.getLockoutRemainingSeconds()).isEqualTo(60)
    }

    @Test
    fun `after 15 failures locked out for 120 seconds`() {
        repeat(15) { protection.recordFailure() }
        assertThat(protection.isLockedOut()).isTrue()
        assertThat(protection.getLockoutRemainingSeconds()).isEqualTo(120)
    }

    @Test
    fun `after 20 failures locked out for 240 seconds`() {
        repeat(20) { protection.recordFailure() }
        assertThat(protection.isLockedOut()).isTrue()
        assertThat(protection.getLockoutRemainingSeconds()).isEqualTo(240)
    }

    @Test
    fun `lockout expires after duration`() {
        repeat(5) { protection.recordFailure() }
        assertThat(protection.isLockedOut()).isTrue()

        // Advance time by 30 seconds
        currentTime += 30_000L
        assertThat(protection.isLockedOut()).isFalse()
    }

    @Test
    fun `reset clears fail count and lockout`() {
        repeat(10) { protection.recordFailure() }
        assertThat(protection.isLockedOut()).isTrue()

        protection.reset()
        assertThat(protection.isLockedOut()).isFalse()
        assertThat(protection.getFailCount()).isEqualTo(0)
    }

    @Test
    fun `remaining seconds decrease with time`() {
        repeat(5) { protection.recordFailure() }
        assertThat(protection.getLockoutRemainingSeconds()).isEqualTo(30)

        currentTime += 10_000L
        assertThat(protection.getLockoutRemainingSeconds()).isEqualTo(20)

        currentTime += 15_000L
        assertThat(protection.getLockoutRemainingSeconds()).isEqualTo(5)
    }

    @Test
    fun `not locked out when below threshold`() {
        repeat(3) { protection.recordFailure() }
        assertThat(protection.isLockedOut()).isFalse()
        assertThat(protection.getLockoutRemainingSeconds()).isEqualTo(0)
    }
}
