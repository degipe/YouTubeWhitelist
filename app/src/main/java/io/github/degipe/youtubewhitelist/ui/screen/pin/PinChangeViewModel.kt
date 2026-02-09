package io.github.degipe.youtubewhitelist.ui.screen.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.data.model.PinVerificationResult
import io.github.degipe.youtubewhitelist.core.data.repository.PinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PinChangeStep { VERIFY_OLD, ENTER_NEW, CONFIRM_NEW }

data class PinChangeUiState(
    val step: PinChangeStep = PinChangeStep.VERIFY_OLD,
    val pin: String = "",
    val error: String? = null,
    val isComplete: Boolean = false,
    val isLockedOut: Boolean = false,
    val lockoutSeconds: Int = 0
)

@HiltViewModel
class PinChangeViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinChangeUiState())
    val uiState: StateFlow<PinChangeUiState> = _uiState.asStateFlow()

    private var oldPin: String = ""
    private var newPin: String = ""

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
        if (state.pin.length < MIN_PIN_LENGTH) return

        when (state.step) {
            PinChangeStep.VERIFY_OLD -> verifyOldPin(state.pin)
            PinChangeStep.ENTER_NEW -> {
                newPin = state.pin
                _uiState.update { it.copy(step = PinChangeStep.CONFIRM_NEW, pin = "", error = null) }
            }
            PinChangeStep.CONFIRM_NEW -> confirmNewPin(state.pin)
        }
    }

    private fun verifyOldPin(pin: String) {
        viewModelScope.launch {
            when (val result = pinRepository.verifyPin(pin)) {
                is PinVerificationResult.Success -> {
                    oldPin = pin
                    _uiState.update {
                        it.copy(step = PinChangeStep.ENTER_NEW, pin = "", error = null)
                    }
                }
                is PinVerificationResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            pin = "",
                            error = "Incorrect PIN. ${result.attemptsRemaining} attempts remaining."
                        )
                    }
                }
                is PinVerificationResult.LockedOut -> {
                    _uiState.update {
                        it.copy(
                            pin = "",
                            isLockedOut = true,
                            lockoutSeconds = result.remainingSeconds,
                            error = "Too many attempts. Try again in ${result.remainingSeconds} seconds."
                        )
                    }
                }
            }
        }
    }

    private fun confirmNewPin(confirmPin: String) {
        if (confirmPin != newPin) {
            newPin = ""
            _uiState.update {
                it.copy(
                    step = PinChangeStep.ENTER_NEW,
                    pin = "",
                    error = "PINs do not match. Try again."
                )
            }
            return
        }

        viewModelScope.launch {
            val result = pinRepository.changePin(oldPin, newPin)
            if (result == PinVerificationResult.Success) {
                _uiState.update { it.copy(isComplete = true) }
            } else {
                _uiState.update { it.copy(pin = "", error = "Failed to change PIN.") }
            }
        }
    }

    companion object {
        private const val MIN_PIN_LENGTH = 4
        private const val MAX_PIN_LENGTH = 6
    }
}
