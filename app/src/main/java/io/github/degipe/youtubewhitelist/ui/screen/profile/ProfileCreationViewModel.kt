package io.github.degipe.youtubewhitelist.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.data.repository.ParentAccountRepository
import io.github.degipe.youtubewhitelist.core.database.dao.KidProfileDao
import io.github.degipe.youtubewhitelist.core.database.entity.KidProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ProfileCreationUiState(
    val name: String = "",
    val error: String? = null,
    val isCreated: Boolean = false
)

@HiltViewModel
class ProfileCreationViewModel @Inject constructor(
    private val kidProfileDao: KidProfileDao,
    private val parentAccountRepository: ParentAccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileCreationUiState())
    val uiState: StateFlow<ProfileCreationUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun onCreateProfile() {
        val name = _uiState.value.name.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Profile name is required") }
            return
        }

        viewModelScope.launch {
            val account = parentAccountRepository.getAccount().first()
            if (account == null) {
                _uiState.update { it.copy(error = "No parent account found") }
                return@launch
            }

            val profile = KidProfileEntity(
                id = UUID.randomUUID().toString(),
                parentAccountId = account.id,
                name = name
            )
            kidProfileDao.insert(profile)
            _uiState.update { it.copy(isCreated = true) }
        }
    }
}
