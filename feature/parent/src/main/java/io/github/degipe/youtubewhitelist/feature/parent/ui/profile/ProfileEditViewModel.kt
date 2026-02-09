package io.github.degipe.youtubewhitelist.feature.parent.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.data.model.KidProfile
import io.github.degipe.youtubewhitelist.core.data.repository.KidProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ProfileEditUiState(
    val name: String = "",
    val avatarUrl: String? = null,
    val dailyLimitMinutes: Int? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel(assistedFactory = ProfileEditViewModel.Factory::class)
class ProfileEditViewModel @AssistedInject constructor(
    private val kidProfileRepository: KidProfileRepository,
    @Assisted private val profileId: String
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(profileId: String): ProfileEditViewModel
    }

    private val _uiState = MutableStateFlow(ProfileEditUiState())
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    private var originalProfile: KidProfile? = null

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = kidProfileRepository.getProfileById(profileId).first()
            if (profile == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Profile not found"
                )
            } else {
                originalProfile = profile
                _uiState.value = _uiState.value.copy(
                    name = profile.name,
                    avatarUrl = profile.avatarUrl,
                    dailyLimitMinutes = profile.dailyLimitMinutes,
                    isLoading = false
                )
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }

    fun onAvatarUrlChanged(url: String?) {
        _uiState.value = _uiState.value.copy(avatarUrl = url)
    }

    fun onDailyLimitChanged(minutes: Int?) {
        _uiState.value = _uiState.value.copy(dailyLimitMinutes = minutes)
    }

    fun saveProfile() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(error = "Name cannot be empty")
            return
        }

        val profile = originalProfile ?: return

        _uiState.value = state.copy(isSaving = true, error = null)

        viewModelScope.launch {
            kidProfileRepository.updateProfile(
                profile.copy(
                    name = state.name.trim(),
                    avatarUrl = state.avatarUrl,
                    dailyLimitMinutes = state.dailyLimitMinutes
                )
            )
            _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
        }
    }

    fun requestDelete() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = true)
    }

    fun dismissDelete() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = false)
    }

    fun confirmDelete() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = false)
        viewModelScope.launch {
            kidProfileRepository.deleteProfile(profileId)
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }
}
