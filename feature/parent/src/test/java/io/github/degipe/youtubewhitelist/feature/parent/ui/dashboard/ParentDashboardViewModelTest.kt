package io.github.degipe.youtubewhitelist.feature.parent.ui.dashboard

import com.google.common.truth.Truth.assertThat
import io.github.degipe.youtubewhitelist.core.data.model.KidProfile
import io.github.degipe.youtubewhitelist.core.data.model.ParentAccount
import io.github.degipe.youtubewhitelist.core.data.repository.KidProfileRepository
import io.github.degipe.youtubewhitelist.core.data.repository.ParentAccountRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParentDashboardViewModelTest {

    private lateinit var kidProfileRepository: KidProfileRepository
    private lateinit var parentAccountRepository: ParentAccountRepository
    private lateinit var viewModel: ParentDashboardViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val parentAccount = ParentAccount(
        id = "parent-1",
        googleAccountId = "google-123",
        email = "parent@test.com",
        isPinSet = true,
        biometricEnabled = false,
        isPremium = false,
        createdAt = 1000L
    )

    private val profiles = listOf(
        KidProfile(
            id = "kid-1", parentAccountId = "parent-1",
            name = "Alice", avatarUrl = null,
            dailyLimitMinutes = 60, sleepPlaylistId = null,
            createdAt = 2000L
        ),
        KidProfile(
            id = "kid-2", parentAccountId = "parent-1",
            name = "Bob", avatarUrl = null,
            dailyLimitMinutes = 90, sleepPlaylistId = null,
            createdAt = 3000L
        )
    )

    private val accountFlow = MutableStateFlow<ParentAccount?>(null)
    private val profilesFlow = MutableStateFlow<List<KidProfile>>(emptyList())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        parentAccountRepository = mockk(relaxed = true)
        kidProfileRepository = mockk(relaxed = true)
        every { parentAccountRepository.getAccount() } returns accountFlow
        every { kidProfileRepository.getProfilesByParent("parent-1") } returns profilesFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = ParentDashboardViewModel(parentAccountRepository, kidProfileRepository)
    }

    // --- Initial State ---

    @Test
    fun `initial state is loading`() {
        createViewModel()
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isTrue()
        assertThat(state.profiles).isEmpty()
        assertThat(state.selectedProfileId).isNull()
    }

    // --- Loading Account and Profiles ---

    @Test
    fun `loads profiles when account is available`() = runTest(testDispatcher) {
        accountFlow.value = parentAccount
        profilesFlow.value = profiles

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.profiles).hasSize(2)
        assertThat(state.profiles[0].name).isEqualTo("Alice")
        assertThat(state.profiles[1].name).isEqualTo("Bob")
    }

    @Test
    fun `auto-selects first profile when profiles load`() = runTest(testDispatcher) {
        accountFlow.value = parentAccount
        profilesFlow.value = profiles

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedProfileId).isEqualTo("kid-1")
    }

    @Test
    fun `no auto-select when profiles are empty`() = runTest(testDispatcher) {
        accountFlow.value = parentAccount
        profilesFlow.value = emptyList()

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedProfileId).isNull()
    }

    @Test
    fun `no account shows empty state`() = runTest(testDispatcher) {
        accountFlow.value = null

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.profiles).isEmpty()
    }

    // --- Profile Selection ---

    @Test
    fun `selectProfile updates selected profile id`() = runTest(testDispatcher) {
        accountFlow.value = parentAccount
        profilesFlow.value = profiles

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectProfile("kid-2")
        assertThat(viewModel.uiState.value.selectedProfileId).isEqualTo("kid-2")
    }

    // --- Reactive Updates ---

    @Test
    fun `profiles update reactively when flow emits`() = runTest(testDispatcher) {
        accountFlow.value = parentAccount
        profilesFlow.value = listOf(profiles[0])

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.profiles).hasSize(1)

        profilesFlow.value = profiles
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.profiles).hasSize(2)
    }

    @Test
    fun `preserves selected profile when profiles update`() = runTest(testDispatcher) {
        accountFlow.value = parentAccount
        profilesFlow.value = profiles

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectProfile("kid-2")

        // Simulate profile list update (e.g., new profile added)
        val updatedProfiles = profiles + KidProfile(
            id = "kid-3", parentAccountId = "parent-1",
            name = "Charlie", avatarUrl = null,
            dailyLimitMinutes = null, sleepPlaylistId = null,
            createdAt = 4000L
        )
        profilesFlow.value = updatedProfiles
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedProfileId).isEqualTo("kid-2")
        assertThat(viewModel.uiState.value.profiles).hasSize(3)
    }
}
