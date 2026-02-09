package io.github.degipe.youtubewhitelist.core.data.model

data class ParentAccount(
    val id: String,
    val googleAccountId: String,
    val email: String,
    val isPinSet: Boolean,
    val biometricEnabled: Boolean,
    val isPremium: Boolean,
    val createdAt: Long
)
