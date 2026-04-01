package com.lingxing.zipalign.feature.zipalign.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lingxing.zipalign.feature.zipalign.domain.ZipAlignCoordinator

class ZipAlignViewModelFactory(
    private val coordinator: ZipAlignCoordinator,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ZipAlignViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return ZipAlignViewModel(coordinator) as T
    }
}
