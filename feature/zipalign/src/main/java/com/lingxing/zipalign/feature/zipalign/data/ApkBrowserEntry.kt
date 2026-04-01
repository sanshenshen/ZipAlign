package com.lingxing.zipalign.feature.zipalign.data

data class ApkBrowserEntry(
    val absolutePath: String,
    val displayName: String,
    val type: ApkBrowserEntryType,
    val sizeBytes: Long?,
    val lastModifiedAtMillis: Long?,
)
