package io.github.degipe.youtubewhitelist.core.auth.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.database.dao.ParentAccountDao
import io.github.degipe.youtubewhitelist.core.database.entity.ParentAccountEntity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ParentAccountRepositoryImplTest {

    private lateinit var parentAccountDao: ParentAccountDao
    private lateinit var repository: ParentAccountRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    private val testEntity = ParentAccountEntity(
        id = "test-id",
        googleAccountId = "google-id",
        email = "test@example.com",
        pinHash = "salt:hash",
        biometricEnabled = false,
        isPremium = false,
        createdAt = 1000L
    )

    @Before
    fun setUp() {
        parentAccountDao = mockk()
        repository = ParentAccountRepositoryImpl(parentAccountDao, testDispatcher)
    }

    @Test
    fun `getAccount maps entity to domain model`() = runTest(testDispatcher) {
        every { parentAccountDao.getParentAccount() } returns flowOf(testEntity)

        repository.getAccount().test {
            val account = awaitItem()
            assertThat(account).isNotNull()
            assertThat(account!!.id).isEqualTo("test-id")
            assertThat(account.email).isEqualTo("test@example.com")
            assertThat(account.isPinSet).isTrue()
            assertThat(account.biometricEnabled).isFalse()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getAccount returns null when no entity`() = runTest(testDispatcher) {
        every { parentAccountDao.getParentAccount() } returns flowOf(null)

        repository.getAccount().test {
            val account = awaitItem()
            assertThat(account).isNull()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getAccount isPinSet false when pinHash empty`() = runTest(testDispatcher) {
        every { parentAccountDao.getParentAccount() } returns flowOf(testEntity.copy(pinHash = ""))

        repository.getAccount().test {
            val account = awaitItem()
            assertThat(account!!.isPinSet).isFalse()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `hasAccount returns true when account exists`() = runTest(testDispatcher) {
        coEvery { parentAccountDao.getParentAccountOnce() } returns testEntity

        assertThat(repository.hasAccount()).isTrue()
    }

    @Test
    fun `hasAccount returns false when no account`() = runTest(testDispatcher) {
        coEvery { parentAccountDao.getParentAccountOnce() } returns null

        assertThat(repository.hasAccount()).isFalse()
    }
}
