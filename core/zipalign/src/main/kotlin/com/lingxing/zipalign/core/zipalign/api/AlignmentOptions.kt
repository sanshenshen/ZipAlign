package com.lingxing.zipalign.core.zipalign.api

data class AlignmentOptions(
    val defaultAlignmentBytes: Int = 4,
    val alignSharedLibraries: Boolean = true,
    val sharedLibraryPageAlignment: PageAlignment = PageAlignment.KB_16,
) {
    init {
        require(defaultAlignmentBytes > 0) { "defaultAlignmentBytes must be greater than zero." }
    }
}
