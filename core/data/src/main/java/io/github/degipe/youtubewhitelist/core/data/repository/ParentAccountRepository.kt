package io.github.degipe.youtubewhitelist.core.data.repository

import io.github.degipe.youtubewhitelist.core.data.model.ParentAccount
import kotlinx.coroutines.flow.Flow

interface ParentAccountRepository {
    fun getAccount(): Flow<ParentAccount?>
    suspend fun hasAccount(): Boolean
}
