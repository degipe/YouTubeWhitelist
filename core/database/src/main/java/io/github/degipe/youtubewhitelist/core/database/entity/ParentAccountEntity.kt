package io.github.degipe.youtubewhitelist.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parent_accounts")
data class ParentAccountEntity(
    @PrimaryKey
    val id: String,
    val googleAccountId: String,
    val email: String,
    val pinHash: String,
    val biometricEnabled: Boolean = false,
    val isPremium: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
