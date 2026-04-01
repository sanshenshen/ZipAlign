package com.lingxing.zipalign.feature.zipalign.presentation

import com.lingxing.zipalign.core.zipalign.api.PageAlignment
import com.lingxing.zipalign.feature.zipalign.data.SelectedApkDocument
import com.lingxing.zipalign.feature.zipalign.domain.ZipOperationPresentation

data class ZipAlignUiState(
    val hasStorageAccess: Boolean = false,
    val browser: FileBrowserUiState = FileBrowserUiState(),
    val selectedDocument: SelectedApkDocument? = null,
    val defaultAlignmentText: String = "4",
    val alignSharedLibraries: Boolean = true,
    val sharedLibraryPageAlignment: PageAlignment = PageAlignment.KB_16,
    val isWorking: Boolean = false,
    val lastOperation: ZipOperationPresentation? = null,
    val errorMessage: String? = null,
)
