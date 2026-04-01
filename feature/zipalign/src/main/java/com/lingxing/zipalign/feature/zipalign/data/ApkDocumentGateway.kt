package com.lingxing.zipalign.feature.zipalign.data

import java.io.File

interface ApkDocumentGateway {
    suspend fun openRootDirectory(): ApkDirectoryListing

    suspend fun openDirectory(directoryPath: String): ApkDirectoryListing

    suspend fun readDocument(absolutePath: String): SelectedApkDocument

    fun createWorkingOutputFile(displayName: String): File

    suspend fun export(
        alignedArchive: File,
        destinationDirectoryPath: String,
        destinationDisplayName: String,
    ): File
}
