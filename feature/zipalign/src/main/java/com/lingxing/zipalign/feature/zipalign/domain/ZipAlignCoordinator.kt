package com.lingxing.zipalign.feature.zipalign.domain

import com.lingxing.zipalign.feature.zipalign.data.ApkDirectoryListing
import com.lingxing.zipalign.core.zipalign.api.AlignmentOptions
import com.lingxing.zipalign.core.zipalign.api.AlignmentReport
import com.lingxing.zipalign.core.zipalign.api.VerificationEntryStatus
import com.lingxing.zipalign.core.zipalign.api.VerificationReport
import com.lingxing.zipalign.core.zipalign.api.ZipAlignService
import com.lingxing.zipalign.feature.zipalign.data.ApkDocumentGateway
import com.lingxing.zipalign.feature.zipalign.data.SelectedApkDocument
import java.io.File
import java.util.Locale

class ZipAlignCoordinator(
    private val documentGateway: ApkDocumentGateway,
    private val zipAlignService: ZipAlignService,
) {

    suspend fun openRootDirectory(): ApkDirectoryListing {
        return documentGateway.openRootDirectory()
    }

    suspend fun openDirectory(directoryPath: String): ApkDirectoryListing {
        return documentGateway.openDirectory(directoryPath)
    }

    suspend fun inspectSource(absolutePath: String): SelectedApkDocument {
        return documentGateway.readDocument(absolutePath)
    }

    suspend fun verify(
        source: SelectedApkDocument,
        options: AlignmentOptions,
    ): ZipOperationPresentation {
        val sourceFile = File(source.absolutePath)
        require(sourceFile.exists() && sourceFile.isFile) {
            "Source APK is not available."
        }
        val report = zipAlignService.verify(sourceFile, options)
        return ZipOperationPresentation(
            title = if (report.isSuccessful) "Verification successful" else "Verification failed",
            message = buildString {
                append("${report.alignedCount} stored entries aligned")
                if (report.misalignedCount > 0) {
                    append(", ${report.misalignedCount} misaligned")
                }
            },
            consoleLines = ZipConsoleFormatter.formatVerification(report),
            verificationReport = report,
        )
    }

    suspend fun align(
        source: SelectedApkDocument,
        destinationDirectoryPath: String,
        options: AlignmentOptions,
    ): ZipOperationPresentation {
        val sourceFile = File(source.absolutePath)
        require(sourceFile.exists() && sourceFile.isFile) {
            "Source APK is not available."
        }
        val stagedOutput = documentGateway.createWorkingOutputFile(source.suggestedAlignedName)
        return try {
            val alignmentReport = zipAlignService.align(sourceFile, stagedOutput, options)
            val exportedFile = documentGateway.export(
                alignedArchive = stagedOutput,
                destinationDirectoryPath = destinationDirectoryPath,
                destinationDisplayName = source.suggestedAlignedName,
            )
            ZipOperationPresentation(
                title = if (alignmentReport.verificationReport.isSuccessful) {
                    "Archive aligned and exported"
                } else {
                    "Archive rewritten but verification failed"
                },
                message = "Rewrote ${alignmentReport.paddedEntryCount} entries and added ${alignmentReport.totalPaddingBytes} bytes of Local File Header padding.",
                consoleLines = ZipConsoleFormatter.formatAlignment(alignmentReport),
                verificationReport = alignmentReport.verificationReport,
                exportedFileName = exportedFile.name,
                exportedFilePath = exportedFile.absolutePath,
                exportedFileSizeBytes = exportedFile.length(),
                totalPaddingBytes = alignmentReport.totalPaddingBytes,
            )
        } finally {
            stagedOutput.delete()
        }
    }
}

private object ZipConsoleFormatter {

    fun formatVerification(report: VerificationReport): List<String> {
        val lines = mutableListOf<String>()
        lines += "Verifying alignment of ${report.archiveName} (${report.defaultAlignmentBytes})..."
        report.entries.forEach { entry ->
            val suffix = when (entry.status) {
                VerificationEntryStatus.COMPRESSED -> "(OK - compressed)"
                VerificationEntryStatus.DIRECTORY -> "(OK - directory)"
                VerificationEntryStatus.ALIGNED -> "(OK)"
                VerificationEntryStatus.MISALIGNED -> "(BAD - ${entry.remainder})"
            }
            lines += String.format(Locale.US, "%8d %s %s", entry.dataOffset, entry.name, suffix)
        }
        lines += "Verification ${if (report.isSuccessful) "successful" else "FAILED"}"
        return lines
    }

    fun formatAlignment(report: AlignmentReport): List<String> {
        val lines = mutableListOf<String>()
        lines += "Rewriting ${report.inputArchiveName} -> ${report.outputArchiveName}"
        val paddedEntries = report.rewrittenEntries.filter { it.paddingBytesAdded > 0 }
        if (paddedEntries.isEmpty()) {
            lines += "No Local File Header padding changes were required."
        } else {
            paddedEntries.forEach { entry ->
                lines += String.format(
                    Locale.US,
                    "%8d %s (PAD +%d -> align %d)",
                    entry.alignedDataOffset,
                    entry.name,
                    entry.paddingBytesAdded,
                    entry.requiredAlignmentBytes,
                )
            }
        }
        lines += "Added ${report.totalPaddingBytes} bytes of padding across ${report.paddedEntryCount} entries."
        lines += ""
        lines += formatVerification(report.verificationReport)
        return lines
    }
}
