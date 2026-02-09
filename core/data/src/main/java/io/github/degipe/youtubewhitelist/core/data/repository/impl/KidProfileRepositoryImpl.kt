package io.github.degipe.youtubewhitelist.core.data.repository.impl

import io.github.degipe.youtubewhitelist.core.common.di.IoDispatcher
import io.github.degipe.youtubewhitelist.core.data.model.KidProfile
import io.github.degipe.youtubewhitelist.core.data.repository.KidProfileRepository
import io.github.degipe.youtubewhitelist.core.database.dao.KidProfileDao
import io.github.degipe.youtubewhitelist.core.database.entity.KidProfileEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class KidProfileRepositoryImpl @Inject constructor(
    private val kidProfileDao: KidProfileDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : KidProfileRepository {

    override fun getProfilesByParent(parentId: String): Flow<List<KidProfile>> =
        kidProfileDao.getProfilesByParent(parentId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getProfileById(profileId: String): Flow<KidProfile?> =
        kidProfileDao.getProfileById(profileId).map { it?.toDomain() }

    override suspend fun createProfile(
        parentId: String,
        name: String,
        avatarUrl: String?
    ): KidProfile = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        val entity = KidProfileEntity(
            id = id,
            parentAccountId = parentId,
            name = name,
            avatarUrl = avatarUrl,
            createdAt = now
        )
        kidProfileDao.insert(entity)
        entity.toDomain()
    }

    override suspend fun updateProfile(profile: KidProfile) = withContext(ioDispatcher) {
        kidProfileDao.update(profile.toEntity())
    }

    override suspend fun deleteProfile(profileId: String) = withContext(ioDispatcher) {
        val entity = kidProfileDao.getProfileByIdOnce(profileId) ?: return@withContext
        kidProfileDao.delete(entity)
    }

    override suspend fun getProfileCount(parentId: String): Int = withContext(ioDispatcher) {
        kidProfileDao.getProfileCount(parentId)
    }

    companion object {
        fun KidProfileEntity.toDomain(): KidProfile = KidProfile(
            id = id,
            parentAccountId = parentAccountId,
            name = name,
            avatarUrl = avatarUrl,
            dailyLimitMinutes = dailyLimitMinutes,
            sleepPlaylistId = sleepPlaylistId,
            createdAt = createdAt
        )

        fun KidProfile.toEntity(): KidProfileEntity = KidProfileEntity(
            id = id,
            parentAccountId = parentAccountId,
            name = name,
            avatarUrl = avatarUrl,
            dailyLimitMinutes = dailyLimitMinutes,
            sleepPlaylistId = sleepPlaylistId,
            createdAt = createdAt
        )
    }
}
