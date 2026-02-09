package io.github.degipe.youtubewhitelist.core.data.repository

import io.github.degipe.youtubewhitelist.core.data.model.PinVerificationResult

interface PinRepository {
    suspend fun setupPin(pin: String)
    suspend fun verifyPin(pin: String): PinVerificationResult
    suspend fun changePin(oldPin: String, newPin: String): PinVerificationResult
    suspend fun isPinSet(): Boolean
}
