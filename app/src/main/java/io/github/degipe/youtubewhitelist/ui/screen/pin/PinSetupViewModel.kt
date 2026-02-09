package io.github.degipe.youtubewhitelist.ui.screen.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.data.repository.PinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PinSetupStep { ENTER_NEW, CONFIRM }

data class PinSetupUiState(
    val step: PinSetupStep = PinSetupStep.ENTER_NEW,
    val pin: String = "",
    val error: String? = null,
    val isComplete: Boolean = false
)

@HiltViewModel
class PinSetupViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinSetupUiState())
    val uiState: StateFlow<PinSetupUiState> = _uiState.asStateFlow()

    private var firstPin: String = ""

    fun onDigitEntered(digit: Int) {
        _uiState.update { state ->
            if (state.pin.length < MAX_PIN_LENGTH) {
                state.copy(pin = state.pin + digit, error = null)
            } else {
                state
            }
        }
    }

    fun onBackspace() {
        _uiState.update { state ->
            state.copy(pin = state.pin.dropLast(1), error = null)
        }
    }

    fun onSubmit() {
        val state = _uiState.value
        when (state.step) {
            PinSetupStep.ENTER_NEW -> {
                if (state.pin.length < MIN_PIN_LENGTH) {
                    _uiState.update { it.copy(error = "PIN must be at least $MIN_PIN_LENGTH digits") }
                    return
                }
                firstPin = state.pin
                _uiState.update { it.copy(step = PinSetupStep.CONFIRM, pin = "", error = null) }
            }
            PinSetupStep.CONFIRM -> {
                if (state.pin != firstPin) {
                    firstPin = ""
                    _uiState.update {
                        it.copy(
                            step = PinSetupStep.ENTER_NEW,
                            pin = "",
                            error = "PINs do not match. Try again."
                        )
                    }
                    return
                }
                viewModelScope.launch {
                    pinRepository.setupPin(state.pin)
                    _uiState.update { it.copy(isComplete = true) }
                }
            }
        }
    }

    companion object {
        const val MIN_PIN_LENGTH = 4
        const val MAX_PIN_LENGTH = 6
    }
}
