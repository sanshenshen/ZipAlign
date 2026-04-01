package com.lingxing.zipalign.feature.zipalign.presentation

import com.lingxing.zipalign.feature.zipalign.data.ApkBrowserEntry
import com.lingxing.zipalign.feature.zipalign.data.ApkDirectoryReference

data class FileBrowserUiState(
    val rootDirectoryPath: String? = null,
    val directoryStack: List<ApkDirectoryReference> = emptyList(),
    val entries: List<ApkBrowserEntry> = emptyList(),
    val isLoading: Boolean = false,
    val transitionDirection: BrowserTransitionDirection = BrowserTransitionDirection.NONE,
) {
    val currentDirectory: ApkDirectoryReference?
        get() = directoryStack.lastOrNull()

    val canNavigateUp: Boolean
        get() = directoryStack.size > 1
}
