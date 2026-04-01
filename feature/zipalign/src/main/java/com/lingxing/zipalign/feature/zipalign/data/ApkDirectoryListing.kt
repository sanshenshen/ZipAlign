package com.lingxing.zipalign.feature.zipalign.data

data class ApkDirectoryListing(
    val directoryPath: String,
    val directoryName: String,
    val entries: List<ApkBrowserEntry>,
)
