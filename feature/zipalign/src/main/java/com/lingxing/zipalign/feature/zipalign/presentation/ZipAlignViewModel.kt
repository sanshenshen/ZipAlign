package com.lingxing.zipalign.feature.zipalign.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingxing.zipalign.core.zipalign.api.AlignmentOptions
import com.lingxing.zipalign.core.zipalign.api.PageAlignment
import com.lingxing.zipalign.feature.zipalign.data.ApkBrowserEntry
import com.lingxing.zipalign.feature.zipalign.data.ApkBrowserEntryType
import com.lingxing.zipalign.feature.zipalign.data.ApkDirectoryListing
import com.lingxing.zipalign.feature.zipalign.data.ApkDirectoryReference
import com.lingxing.zipalign.feature.zipalign.domain.ZipAlignCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ZipAlignViewModel(
    private val coordinator: ZipAlignCoordinator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ZipAlignUiState())
    val uiState: StateFlow<ZipAlignUiState> = _uiState.asStateFlow()

    fun refreshStorageAccess(hasStorageAccess: Boolean) {
        val previousValue = _uiState.value.hasStorageAccess
        _uiState.update {
            it.copy(
                hasStorageAccess = hasStorageAccess,
                browser = if (hasStorageAccess) {
                    it.browser
                } else {
                    FileBrowserUiState()
                },
                selectedDocument = if (hasStorageAccess) it.selectedDocument else null,
                lastOperation = if (hasStorageAccess) it.lastOperation else null,
                errorMessage = null,
            )
        }
        if (hasStorageAccess && (!previousValue || _uiState.value.browser.directoryStack.isEmpty())) {
            loadRootDirectory()
        }
    }

    fun loadRootDirectory() {
        if (!_uiState.value.hasStorageAccess) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    browser = it.browser.copy(isLoading = true),
                    errorMessage = null,
                )
            }
            runCatching {
                coordinator.openRootDirectory()
            }.onSuccess { listing ->
                _uiState.update {
                    it.copy(
                        browser = buildRootBrowserState(listing, isLoading = false),
                        selectedDocument = null,
                        lastOperation = null,
                        errorMessage = null,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        browser = it.browser.copy(isLoading = false),
                        errorMessage = throwable.message ?: "Unable to open the storage root.",
                    )
                }
            }
        }
    }

    fun openDirectory(entry: ApkBrowserEntry) {
        if (entry.type != ApkBrowserEntryType.DIRECTORY) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    browser = it.browser.copy(isLoading = true),
                    errorMessage = null,
                )
            }
            runCatching {
                coordinator.openDirectory(entry.absolutePath)
            }.onSuccess { listing ->
                _uiState.update { current ->
                    current.copy(
                        browser = current.browser.copy(
                            entries = listing.entries,
                            directoryStack = current.browser.directoryStack + ApkDirectoryReference(
                                absolutePath = listing.directoryPath,
                                displayName = listing.directoryName,
                            ),
                            isLoading = false,
                            transitionDirection = BrowserTransitionDirection.FORWARD,
                        ),
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        browser = it.browser.copy(isLoading = false),
                        errorMessage = throwable.message ?: "Unable to open the selected folder.",
                    )
                }
            }
        }
    }

    fun navigateUp() {
        val currentState = _uiState.value
        if (!currentState.browser.canNavigateUp) {
            return
        }
        val parentDirectory = currentState.browser.directoryStack[currentState.browser.directoryStack.lastIndex - 1]
        loadDirectory(
            directoryPath = parentDirectory.absolutePath,
            onSuccess = { state, listing ->
                state.copy(
                    browser = state.browser.copy(
                        entries = listing.entries,
                        directoryStack = state.browser.directoryStack.dropLast(1),
                        isLoading = false,
                        transitionDirection = BrowserTransitionDirection.BACKWARD,
                    ),
                )
            },
            fallbackErrorMessage = "Unable to open the parent folder.",
        )
    }

    fun selectFile(entry: ApkBrowserEntry) {
        if (entry.type == ApkBrowserEntryType.OTHER_FILE) {
            _uiState.update {
                it.copy(errorMessage = "Only APK-like files can be processed.")
            }
            return
        }
        if (entry.type != ApkBrowserEntryType.APK_FILE) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isWorking = true, errorMessage = null) }
            runCatching {
                coordinator.inspectSource(entry.absolutePath)
            }.onSuccess { document ->
                _uiState.update {
                    it.copy(
                        selectedDocument = document,
                        isWorking = false,
                        lastOperation = null,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isWorking = false,
                        errorMessage = throwable.message ?: "Unable to inspect the selected APK.",
                    )
                }
            }
        }
    }

    fun onDefaultAlignmentChanged(value: String) {
        if (value.all(Char::isDigit) && value.length <= 5) {
            _uiState.update { it.copy(defaultAlignmentText = value, errorMessage = null) }
        }
    }

    fun onAlignSharedLibrariesChanged(enabled: Boolean) {
        _uiState.update { it.copy(alignSharedLibraries = enabled, errorMessage = null) }
    }

    fun onSharedLibraryPageAlignmentChanged(pageAlignment: PageAlignment) {
        _uiState.update { it.copy(sharedLibraryPageAlignment = pageAlignment, errorMessage = null) }
    }

    fun verify() {
        val document = _uiState.value.selectedDocument ?: run {
            _uiState.update { it.copy(errorMessage = "Choose an APK from the file list before verification.") }
            return
        }
        val options = parseOptions() ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isWorking = true, errorMessage = null) }
            runCatching {
                coordinator.verify(document, options)
            }.onSuccess { result ->
                _uiState.update { it.copy(isWorking = false, lastOperation = result) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isWorking = false,
                        errorMessage = throwable.message ?: "Verification failed unexpectedly.",
                    )
                }
            }
        }
    }

    fun align() {
        val state = _uiState.value
        val document = state.selectedDocument ?: run {
            _uiState.update { it.copy(errorMessage = "Choose an APK from the file list before export.") }
            return
        }
        val destinationDirectoryPath = state.browser.currentDirectory?.absolutePath ?: document.parentDirectoryPath ?: run {
            _uiState.update { it.copy(errorMessage = "No destination directory is available.") }
            return
        }
        val options = parseOptions() ?: return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isWorking = true, errorMessage = null) }
            runCatching {
                coordinator.align(document, destinationDirectoryPath, options)
            }.onSuccess { result ->
                val updatedSelection = result.exportedFilePath?.let { exportedFilePath ->
                    coordinator.inspectSource(exportedFilePath)
                }
                val refreshedState = refreshCurrentDirectoryAfterAlignment(updatedSelection)
                _uiState.update {
                    refreshedState(
                        it.copy(
                            isWorking = false,
                            lastOperation = result,
                            selectedDocument = updatedSelection ?: it.selectedDocument,
                        ),
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isWorking = false,
                        errorMessage = throwable.message ?: "Alignment failed unexpectedly.",
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private suspend fun refreshCurrentDirectoryAfterAlignment(
        selectedDocument: com.lingxing.zipalign.feature.zipalign.data.SelectedApkDocument?,
    ): (ZipAlignUiState) -> ZipAlignUiState {
        val currentDirectoryPath = _uiState.value.browser.currentDirectory?.absolutePath ?: return { it }
        val listing = runCatching {
            coordinator.openDirectory(currentDirectoryPath)
        }.getOrNull() ?: return { it }

        return { state ->
            state.copy(
                browser = state.browser.copy(
                    entries = listing.entries,
                    transitionDirection = BrowserTransitionDirection.NONE,
                ),
                selectedDocument = selectedDocument ?: state.selectedDocument,
            )
        }
    }

    private fun loadDirectory(
        directoryPath: String,
        onSuccess: (ZipAlignUiState, ApkDirectoryListing) -> ZipAlignUiState,
        fallbackErrorMessage: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    browser = it.browser.copy(isLoading = true),
                    errorMessage = null,
                )
            }
            runCatching {
                coordinator.openDirectory(directoryPath)
            }.onSuccess { listing ->
                _uiState.update { current -> onSuccess(current, listing) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        browser = it.browser.copy(isLoading = false),
                        errorMessage = throwable.message ?: fallbackErrorMessage,
                    )
                }
            }
        }
    }

    private fun parseOptions(): AlignmentOptions? {
        val defaultAlignment = _uiState.value.defaultAlignmentText.toIntOrNull()
        if (defaultAlignment == null || defaultAlignment <= 0) {
            _uiState.update { it.copy(errorMessage = "Default alignment must be a positive integer.") }
            return null
        }
        return AlignmentOptions(
            defaultAlignmentBytes = defaultAlignment,
            alignSharedLibraries = _uiState.value.alignSharedLibraries,
            sharedLibraryPageAlignment = _uiState.value.sharedLibraryPageAlignment,
        )
    }

    private fun buildRootBrowserState(
        listing: ApkDirectoryListing,
        isLoading: Boolean,
    ): FileBrowserUiState {
        return FileBrowserUiState(
            rootDirectoryPath = listing.directoryPath,
            directoryStack = listOf(
                ApkDirectoryReference(
                    absolutePath = listing.directoryPath,
                    displayName = listing.directoryName,
                ),
            ),
            entries = listing.entries,
            isLoading = isLoading,
            transitionDirection = BrowserTransitionDirection.NONE,
        )
    }
}
