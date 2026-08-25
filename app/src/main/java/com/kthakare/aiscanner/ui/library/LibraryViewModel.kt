package com.kthakare.aiscanner.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kthakare.aiscanner.data.ScanDocument
import com.kthakare.aiscanner.data.ScanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(private val repository: ScanRepository) : ViewModel() {
    val documents: StateFlow<List<ScanDocument>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun importScan(pageUris: List<android.net.Uri>, pdfUri: android.net.Uri?) {
        viewModelScope.launch {
            runCatching { repository.importScan(pageUris, pdfUri) }
                .onFailure { showError(it.message ?: "Unable to save the scan.") }
        }
    }

    fun showError(text: String) {
        _message.value = text
    }

    fun consumeMessage() {
        _message.value = null
    }

    companion object {
        fun factory(repository: ScanRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LibraryViewModel(repository) as T
            }
        }
    }
}
