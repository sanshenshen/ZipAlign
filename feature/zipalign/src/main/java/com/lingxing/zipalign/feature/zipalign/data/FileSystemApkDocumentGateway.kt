package com.lingxing.zipalign.feature.zipalign.data

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.IOException
import java.util.UUID

class FileSystemApkDocumentGateway(
    context: Context,
) : ApkDocumentGateway {

    private val appContext: Context = context.applicationContext
    private val workingDirectory = File(appContext.cacheDir, "zipalign-working").apply { mkdirs() }

    override suspend fun openRootDirectory(): ApkDirectoryListing {
        return queryDirectory(resolveRootDirectory())
    }

    override suspend fun openDirectory(directoryPath: String): ApkDirectoryListing {
        return queryDirectory(File(directoryPath))
    }

    override suspend fun readDocument(absolutePath: String): SelectedApkDocument {
        val file = File(absolutePath)
        require(file.exists() && file.isFile) {
            "Selected file does not exist."
        }
        return SelectedApkDocument(
            absolutePath = file.absolutePath,
            displayName = file.name,
            sizeBytes = file.length(),
            parentDirectoryPath = file.parentFile?.absolutePath,
        )
    }

    override fun createWorkingOutputFile(displayName: String): File {
        return File(
            workingDirectory,
            "${UUID.randomUUID()}-${sanitizeFileName(displayName)}",
        )
    }

    override suspend fun export(
        alignedArchive: File,
        destinationDirectoryPath: String,
        destinationDisplayName: String,
    ): File {
        val targetDirectory = File(destinationDirectoryPath)
        require(targetDirectory.exists() && targetDirectory.isDirectory) {
            "Export directory is not available."
        }
        require(targetDirectory.canWrite()) {
            "Export directory is not writable."
        }

        val destinationFile = createAvailableDestinationFile(targetDirectory, destinationDisplayName)
        alignedArchive.inputStream().use { inputStream ->
            destinationFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return destinationFile
    }

    private fun queryDirectory(directory: File): ApkDirectoryListing {
        require(directory.exists() && directory.isDirectory) {
            "Directory does not exist."
        }
        require(directory.canRead()) {
            "Directory cannot be read."
        }

        val children = directory.listFiles()
            ?: throw IOException("Unable to read the selected directory.")

        val entries = children
            .asSequence()
            .map { child ->
                ApkBrowserEntry(
                    absolutePath = child.absolutePath,
                    displayName = child.name.ifBlank { child.absolutePath },
                    type = when {
                        child.isDirectory -> ApkBrowserEntryType.DIRECTORY
                        ApkFileNameMatcher.isApkLikeFile(child.name) -> ApkBrowserEntryType.APK_FILE
                        else -> ApkBrowserEntryType.OTHER_FILE
                    },
                    sizeBytes = child.takeIf { it.isFile }?.length(),
                    lastModifiedAtMillis = child.lastModified().takeIf { it > 0L },
                )
            }
            .sortedWith(
                compareBy<ApkBrowserEntry> { it.type != ApkBrowserEntryType.DIRECTORY }
                    .thenBy { it.displayName.lowercase() },
            )
            .toList()

        return ApkDirectoryListing(
            directoryPath = directory.absolutePath,
            directoryName = resolveDirectoryDisplayName(directory),
            entries = entries,
        )
    }

    @Suppress("DEPRECATION")
    private fun resolveRootDirectory(): File {
        return Environment.getExternalStorageDirectory()
    }

    private fun resolveDirectoryDisplayName(directory: File): String {
        val rootDirectory = resolveRootDirectory()
        return if (directory.absolutePath == rootDirectory.absolutePath) {
            ROOT_DIRECTORY_NAME
        } else {
            directory.name.ifBlank { directory.absolutePath }
        }
    }

    private fun createAvailableDestinationFile(
        targetDirectory: File,
        requestedName: String,
    ): File {
        val sanitizedName = sanitizeFileName(requestedName)
        val dotIndex = sanitizedName.lastIndexOf('.')
        val namePart = if (dotIndex > 0) sanitizedName.substring(0, dotIndex) else sanitizedName
        val extensionPart = if (dotIndex > 0) sanitizedName.substring(dotIndex) else ""

        var candidate = File(targetDirectory, sanitizedName)
        var index = 1
        while (candidate.exists()) {
            candidate = File(targetDirectory, "$namePart-$index$extensionPart")
            index++
        }
        return candidate
    }

    private fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("[^A-Za-z0-9._ -]"), "_")
    }

    private companion object {
        const val ROOT_DIRECTORY_NAME = "Internal Storage"
    }
}
