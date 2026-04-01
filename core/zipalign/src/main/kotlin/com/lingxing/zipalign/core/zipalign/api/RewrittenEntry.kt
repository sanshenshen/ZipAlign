package com.lingxing.zipalign.core.zipalign.api

data class RewrittenEntry(
    val name: String,
    val originalDataOffset: Long,
    val alignedDataOffset: Long,
    val requiredAlignmentBytes: Int,
    val paddingBytesAdded: Int,
)
