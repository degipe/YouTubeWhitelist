package io.github.degipe.youtubewhitelist.core.auth.pin

import io.github.degipe.youtubewhitelist.core.common.di.IoDispatcher
import io.github.degipe.youtubewhitelist.core.data.model.PinVerificationResult
import io.github.degipe.youtubewhitelist.core.data.repository.PinRepository
import io.github.degipe.youtubewhitelist.core.database.dao.ParentAccountDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PinRepositoryImpl @Inject constructor(
    private val parentAccountDao: ParentAccountDao,
    private val pinHasher: PinHasher,
    private val bruteForceProtection: BruteForceProtection,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PinRepository {

    override suspend fun setupPin(pin: String) = withContext(ioDispatcher) {
        val account = parentAccountDao.getParentAccountOnce()
            ?: throw IllegalStateException("No parent account found")
        val pinHash = pinHasher.hash(pin)
        parentAccountDao.update(account.copy(pinHash = pinHash))
    }

    override suspend fun verifyPin(pin: String): PinVerificationResult = withContext(ioDispatcher) {
        if (bruteForceProtection.isLockedOut()) {
            return@withContext PinVerificationResult.LockedOut(
                bruteForceProtection.getLockoutRemainingSeconds()
            )
        }

        val account = parentAccountDao.getParentAccountOnce()
            ?: return@withContext PinVerificationResult.Failure(attemptsRemaining = 0)

        if (pinHasher.verify(pin, account.pinHash)) {
            bruteForceProtection.reset()
            PinVerificationResult.Success
        } else {
            bruteForceProtection.recordFailure()
            val failCount = bruteForceProtection.getFailCount()
            val attemptsRemaining = BruteForceProtection.THRESHOLD - (failCount % BruteForceProtection.THRESHOLD)
            PinVerificationResult.Failure(attemptsRemaining = attemptsRemaining)
        }
    }

    override suspend fun changePin(
        oldPin: String,
        newPin: String
    ): PinVerificationResult = withContext(ioDispatcher) {
        val verifyResult = verifyPin(oldPin)
        if (verifyResult != PinVerificationResult.Success) {
            return@withContext verifyResult
        }
        setupPin(newPin)
        PinVerificationResult.Success
    }

    override suspend fun isPinSet(): Boolean = withContext(ioDispatcher) {
        val account = parentAccountDao.getParentAccountOnce()
        !account?.pinHash.isNullOrEmpty()
    }
}
