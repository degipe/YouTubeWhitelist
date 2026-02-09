package io.github.degipe.youtubewhitelist.core.auth.repository

import io.github.degipe.youtubewhitelist.core.common.di.IoDispatcher
import io.github.degipe.youtubewhitelist.core.data.model.ParentAccount
import io.github.degipe.youtubewhitelist.core.data.repository.ParentAccountRepository
import io.github.degipe.youtubewhitelist.core.database.dao.ParentAccountDao
import io.github.degipe.youtubewhitelist.core.database.entity.ParentAccountEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ParentAccountRepositoryImpl @Inject constructor(
    private val parentAccountDao: ParentAccountDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ParentAccountRepository {

    override fun getAccount(): Flow<ParentAccount?> {
        return parentAccountDao.getParentAccount().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun hasAccount(): Boolean = withContext(ioDispatcher) {
        parentAccountDao.getParentAccountOnce() != null
    }

    companion object {
        fun ParentAccountEntity.toDomain(): ParentAccount = ParentAccount(
            id = id,
            googleAccountId = googleAccountId,
            email = email,
            isPinSet = pinHash.isNotEmpty(),
            biometricEnabled = biometricEnabled,
            isPremium = isPremium,
            createdAt = createdAt
        )
    }
}
