package io.github.degipe.youtubewhitelist.core.auth.pin

import android.content.SharedPreferences
import kotlin.math.ceil
import kotlin.math.pow

class BruteForceProtection(
    private val prefs: SharedPreferences,
    private val clock: () -> Long = System::currentTimeMillis
) {

    companion object {
        private const val KEY_FAIL_COUNT = "pin_fail_count"
        private const val KEY_LOCKOUT_UNTIL = "pin_lockout_until"
        const val THRESHOLD = 5
        private const val BASE_LOCKOUT_SECONDS = 30
    }

    fun recordFailure() {
        val newCount = getFailCount() + 1
        prefs.edit().putInt(KEY_FAIL_COUNT, newCount).apply()

        if (newCount >= THRESHOLD && newCount % THRESHOLD == 0) {
            val tier = newCount / THRESHOLD
            val lockoutMs = calculateLockoutMs(tier)
            val lockoutUntil = clock() + lockoutMs
            prefs.edit().putLong(KEY_LOCKOUT_UNTIL, lockoutUntil).apply()
        }
    }

    fun reset() {
        prefs.edit()
            .remove(KEY_FAIL_COUNT)
            .remove(KEY_LOCKOUT_UNTIL)
            .apply()
    }

    fun isLockedOut(): Boolean {
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        return lockoutUntil > clock()
    }

    fun getLockoutRemainingSeconds(): Int {
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        val remaining = lockoutUntil - clock()
        return if (remaining > 0) {
            ceil(remaining / 1000.0).toInt()
        } else {
            0
        }
    }

    fun getFailCount(): Int {
        return prefs.getInt(KEY_FAIL_COUNT, 0)
    }

    private fun calculateLockoutMs(tier: Int): Long {
        val seconds = BASE_LOCKOUT_SECONDS * 2.0.pow((tier - 1).toDouble())
        return (seconds * 1000).toLong()
    }
}
