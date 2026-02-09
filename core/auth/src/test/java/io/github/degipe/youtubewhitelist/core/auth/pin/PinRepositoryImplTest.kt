package io.github.degipe.youtubewhitelist.core.auth.pin

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.data.model.PinVerificationResult
import io.github.degipe.youtubewhitelist.core.database.dao.ParentAccountDao
import io.github.degipe.youtubewhitelist.core.database.entity.ParentAccountEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PinRepositoryImplTest {

    private lateinit var pinHasher: PinHasher
    private lateinit var bruteForceProtection: BruteForceProtection
    private lateinit var parentAccountDao: ParentAccountDao
    private lateinit var repository: PinRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    private val testAccount = ParentAccountEntity(
        id = "test-id",
        googleAccountId = "google-id",
        email = "test@example.com",
        pinHash = "",
        biometricEnabled = false,
        isPremium = false,
        createdAt = 1000L
    )

    @Before
    fun setUp() {
        pinHasher = mockk()
        bruteForceProtection = mockk(relaxed = true)
        parentAccountDao = mockk(relaxed = true)

        coEvery { parentAccountDao.getParentAccountOnce() } returns testAccount

        repository = PinRepositoryImpl(
            parentAccountDao = parentAccountDao,
            pinHasher = pinHasher,
            bruteForceProtection = bruteForceProtection,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `setupPin hashes and stores pin`() = runTest(testDispatcher) {
        every { pinHasher.hash("1234") } returns "salt:hash"
        val accountSlot = slot<ParentAccountEntity>()
        coEvery { parentAccountDao.update(capture(accountSlot)) } returns Unit

        repository.setupPin("1234")

        assertThat(accountSlot.captured.pinHash).isEqualTo("salt:hash")
        coVerify { parentAccountDao.update(any()) }
    }

    @Test(expected = IllegalStateException::class)
    fun `setupPin throws when no parent account`() = runTest(testDispatcher) {
        coEvery { parentAccountDao.getParentAccountOnce() } returns null

        repository.setupPin("1234")
    }

    @Test
    fun `verifyPin returns Success for correct pin`() = runTest(testDispatcher) {
        val accountWithPin = testAccount.copy(pinHash = "salt:hash")
        coEvery { parentAccountDao.getParentAccountOnce() } returns accountWithPin
        every { bruteForceProtection.isLockedOut() } returns false
        every { pinHasher.verify("1234", "salt:hash") } returns true

        val result = repository.verifyPin("1234")

        assertThat(result).isEqualTo(PinVerificationResult.Success)
        coVerify { bruteForceProtection.reset() }
    }

    @Test
    fun `verifyPin returns Failure for incorrect pin`() = runTest(testDispatcher) {
        val accountWithPin = testAccount.copy(pinHash = "salt:hash")
        coEvery { parentAccountDao.getParentAccountOnce() } returns accountWithPin
        every { bruteForceProtection.isLockedOut() } returns false
        every { pinHasher.verify("9999", "salt:hash") } returns false
        every { bruteForceProtection.getFailCount() } returns 1

        val result = repository.verifyPin("9999")

        assertThat(result).isInstanceOf(PinVerificationResult.Failure::class.java)
        coVerify { bruteForceProtection.recordFailure() }
    }

    @Test
    fun `verifyPin returns LockedOut when brute force triggered`() = runTest(testDispatcher) {
        every { bruteForceProtection.isLockedOut() } returns true
        every { bruteForceProtection.getLockoutRemainingSeconds() } returns 30

        val result = repository.verifyPin("1234")

        assertThat(result).isEqualTo(PinVerificationResult.LockedOut(30))
    }

    @Test
    fun `verifyPin resets counter on success`() = runTest(testDispatcher) {
        val accountWithPin = testAccount.copy(pinHash = "salt:hash")
        coEvery { parentAccountDao.getParentAccountOnce() } returns accountWithPin
        every { bruteForceProtection.isLockedOut() } returns false
        every { pinHasher.verify("1234", "salt:hash") } returns true

        repository.verifyPin("1234")

        coVerify { bruteForceProtection.reset() }
    }

    @Test
    fun `changePin verifies old then sets new`() = runTest(testDispatcher) {
        val accountWithPin = testAccount.copy(pinHash = "old-salt:old-hash")
        coEvery { parentAccountDao.getParentAccountOnce() } returns accountWithPin
        every { bruteForceProtection.isLockedOut() } returns false
        every { pinHasher.verify("1234", "old-salt:old-hash") } returns true
        every { pinHasher.hash("5678") } returns "new-salt:new-hash"
        val accountSlot = slot<ParentAccountEntity>()
        coEvery { parentAccountDao.update(capture(accountSlot)) } returns Unit

        val result = repository.changePin("1234", "5678")

        assertThat(result).isEqualTo(PinVerificationResult.Success)
        assertThat(accountSlot.captured.pinHash).isEqualTo("new-salt:new-hash")
    }

    @Test
    fun `changePin fails when old pin incorrect`() = runTest(testDispatcher) {
        val accountWithPin = testAccount.copy(pinHash = "salt:hash")
        coEvery { parentAccountDao.getParentAccountOnce() } returns accountWithPin
        every { bruteForceProtection.isLockedOut() } returns false
        every { pinHasher.verify("wrong", "salt:hash") } returns false
        every { bruteForceProtection.getFailCount() } returns 1

        val result = repository.changePin("wrong", "5678")

        assertThat(result).isInstanceOf(PinVerificationResult.Failure::class.java)
    }

    @Test
    fun `isPinSet returns true when pinHash exists`() = runTest(testDispatcher) {
        val accountWithPin = testAccount.copy(pinHash = "salt:hash")
        coEvery { parentAccountDao.getParentAccountOnce() } returns accountWithPin

        assertThat(repository.isPinSet()).isTrue()
    }

    @Test
    fun `isPinSet returns false when pinHash empty`() = runTest(testDispatcher) {
        coEvery { parentAccountDao.getParentAccountOnce() } returns testAccount

        assertThat(repository.isPinSet()).isFalse()
    }

    @Test
    fun `isPinSet returns false when no account`() = runTest(testDispatcher) {
        coEvery { parentAccountDao.getParentAccountOnce() } returns null

        assertThat(repository.isPinSet()).isFalse()
    }
}
