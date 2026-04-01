package com.lingxing.zipalign.core.zipalign.api

data class VerificationReport(
    val archiveName: String,
    val defaultAlignmentBytes: Int,
    val entries: List<VerificationEntry>,
) {
    val isSuccessful: Boolean = entries.none { it.status == VerificationEntryStatus.MISALIGNED }
    val alignedCount: Int = entries.count { it.status == VerificationEntryStatus.ALIGNED }
    val misalignedCount: Int = entries.count { it.status == VerificationEntryStatus.MISALIGNED }
    val compressedCount: Int = entries.count { it.status == VerificationEntryStatus.COMPRESSED }
    val directoryCount: Int = entries.count { it.status == VerificationEntryStatus.DIRECTORY }
}
