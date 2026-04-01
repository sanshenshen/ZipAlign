package com.lingxing.zipalign.core.zipalign.api

data class VerificationEntry(
    val dataOffset: Long,
    val name: String,
    val status: VerificationEntryStatus,
    val requiredAlignmentBytes: Int,
    val remainder: Int = 0,
)
