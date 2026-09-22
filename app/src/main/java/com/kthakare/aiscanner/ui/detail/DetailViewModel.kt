package com.kthakare.aiscanner.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kthakare.aiscanner.data.ScanDocument
import com.kthakare.aiscanner.data.ScanRepository
import com.kthakare.aiscanner.ocr.OcrAnalyzer
import com.kthakare.aiscanner.ocr.OcrExtraction
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailUiState(
    val extraction: OcrExtraction? = null,
    val isExtracting: Boolean = false,
    val error: String? = null,
    val deleted: Boolean = false,
)

class DetailViewModel(
    scanId: String,
    private val repository: ScanRepository,
    private val ocrAnalyzer: OcrAnalyzer,
) : ViewModel() {
    val document: StateFlow<ScanDocument?> = repository.observeById(scanId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun pageFiles(document: ScanDocument): List<File> = repository.pageFiles(document)

    fun rename(id: String, title: String) {
        viewModelScope.launch {
            repository.rename(id, title)
        }
    }

    fun delete(document: ScanDocument) {
        viewModelScope.launch {
            repository.delete(document)
            _uiState.value = _uiState.value.copy(deleted = true)
        }
    }

    fun extractText(document: ScanDocument) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExtracting = true, error = null)
            runCatching { ocrAnalyzer.recognize(repository.pageFiles(document)) }
                .onSuccess { extraction ->
                    _uiState.value = _uiState.value.copy(extraction = extraction, isExtracting = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isExtracting = false,
                        error = error.message ?: "Unable to extract text.",
                    )
                }
        }
    }

    companion object {
        fun factory(
            scanId: String,
            repository: ScanRepository,
            ocrAnalyzer: OcrAnalyzer,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DetailViewModel(scanId, repository, ocrAnalyzer) as T
            }
        }
    }
}
