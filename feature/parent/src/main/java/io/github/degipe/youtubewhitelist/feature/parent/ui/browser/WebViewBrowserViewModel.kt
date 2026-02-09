package io.github.degipe.youtubewhitelist.feature.parent.ui.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.common.youtube.ParsedYouTubeUrl
import io.github.degipe.youtubewhitelist.core.common.youtube.YouTubeUrlParser
import io.github.degipe.youtubewhitelist.core.data.repository.WhitelistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AddToWhitelistResult {
    data class Success(val itemTitle: String) : AddToWhitelistResult
    data class Error(val message: String) : AddToWhitelistResult
}

data class WebViewBrowserUiState(
    val currentUrl: String = "https://www.youtube.com",
    val detectedContent: ParsedYouTubeUrl? = null,
    val isAdding: Boolean = false,
    val addResult: AddToWhitelistResult? = null
)

@HiltViewModel
class WebViewBrowserViewModel @Inject constructor(
    private val whitelistRepository: WhitelistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WebViewBrowserUiState())
    val uiState: StateFlow<WebViewBrowserUiState> = _uiState.asStateFlow()

    fun onUrlChanged(url: String) {
        val parsed = YouTubeUrlParser.parse(url)
        _uiState.update {
            it.copy(currentUrl = url, detectedContent = parsed)
        }
    }

    fun addToWhitelist(profileId: String) {
        val currentUrl = _uiState.value.currentUrl
        if (_uiState.value.detectedContent == null) return

        _uiState.update { it.copy(isAdding = true) }
        viewModelScope.launch {
            when (val result = whitelistRepository.addItemFromUrl(profileId, currentUrl)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isAdding = false,
                            addResult = AddToWhitelistResult.Success(result.data.title)
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAdding = false,
                            addResult = AddToWhitelistResult.Error(result.message)
                        )
                    }
                }
            }
        }
    }

    fun dismissResult() {
        _uiState.update { it.copy(addResult = null) }
    }
}
