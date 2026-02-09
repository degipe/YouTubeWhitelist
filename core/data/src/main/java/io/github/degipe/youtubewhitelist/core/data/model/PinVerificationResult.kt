package io.github.degipe.youtubewhitelist.core.data.model

sealed interface PinVerificationResult {
    data object Success : PinVerificationResult
    data class Failure(val attemptsRemaining: Int) : PinVerificationResult
    data class LockedOut(val remainingSeconds: Int) : PinVerificationResult
}
