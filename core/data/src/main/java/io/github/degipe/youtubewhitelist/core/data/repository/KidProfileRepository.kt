package io.github.degipe.youtubewhitelist.core.data.repository

import io.github.degipe.youtubewhitelist.core.data.model.KidProfile
import kotlinx.coroutines.flow.Flow

interface KidProfileRepository {
    fun getProfilesByParent(parentId: String): Flow<List<KidProfile>>
    fun getProfileById(profileId: String): Flow<KidProfile?>
    suspend fun createProfile(parentId: String, name: String, avatarUrl: String?): KidProfile
    suspend fun updateProfile(profile: KidProfile)
    suspend fun deleteProfile(profileId: String)
    suspend fun getProfileCount(parentId: String): Int
}
