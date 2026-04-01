package com.lingxing.zipalign.core.zipalign.api

data class AlignmentReport(
    val inputArchiveName: String,
    val outputArchiveName: String,
    val rewrittenEntries: List<RewrittenEntry>,
    val verificationReport: VerificationReport,
) {
    val paddedEntryCount: Int = rewrittenEntries.count { it.paddingBytesAdded > 0 }
    val totalPaddingBytes: Long = rewrittenEntries.sumOf { it.paddingBytesAdded.toLong() }
}
