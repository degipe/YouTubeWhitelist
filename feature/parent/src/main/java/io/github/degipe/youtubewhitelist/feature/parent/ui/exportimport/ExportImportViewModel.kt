package io.github.degipe.youtubewhitelist.feature.parent.ui.exportimport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.degipe.youtubewhitelist.core.common.result.AppResult
import io.github.degipe.youtubewhitelist.core.export.ExportImportService
import io.github.degipe.youtubewhitelist.core.export.ImportResult
import io.github.degipe.youtubewhitelist.core.export.ImportStrategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExportImportUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportedJson: String? = null,
    val importResult: ImportResult? = null,
    val error: String? = null
)

@HiltViewModel(assistedFactory = ExportImportViewModel.Factory::class)
class ExportImportViewModel @AssistedInject constructor(
    private val exportImportService: ExportImportService,
    @Assisted private val parentAccountId: String
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(parentAccountId: String): ExportImportViewModel
    }

    private val _uiState = MutableStateFlow(ExportImportUiState())
    val uiState: StateFlow<ExportImportUiState> = _uiState.asStateFlow()

    fun export() {
        _uiState.value = _uiState.value.copy(isExporting = true, error = null)
        viewModelScope.launch {
            when (val result = exportImportService.exportToJson(parentAccountId)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportedJson = result.data
                    )
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun importData(json: String, strategy: ImportStrategy) {
        _uiState.value = _uiState.value.copy(isImporting = true, error = null)
        viewModelScope.launch {
            when (val result = exportImportService.importFromJson(parentAccountId, json, strategy)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        importResult = result.data
                    )
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun dismissResult() {
        _uiState.value = _uiState.value.copy(importResult = null)
    }

    fun clearExportedJson() {
        _uiState.value = _uiState.value.copy(exportedJson = null)
    }
}
