package com.lingxing.zipalign.feature.zipalign.domain

import com.lingxing.zipalign.core.zipalign.api.VerificationReport

data class ZipOperationPresentation(
    val title: String,
    val message: String,
    val consoleLines: List<String>,
    val verificationReport: VerificationReport,
    val exportedFileName: String? = null,
    val exportedFilePath: String? = null,
    val exportedFileSizeBytes: Long? = null,
    val totalPaddingBytes: Long = 0,
)
