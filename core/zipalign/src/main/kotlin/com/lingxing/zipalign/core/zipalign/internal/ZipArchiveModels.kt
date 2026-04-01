package com.lingxing.zipalign.core.zipalign.internal

import com.lingxing.zipalign.core.zipalign.api.AlignmentOptions
import java.io.File

internal data class EndOfCentralDirectory(
    val totalEntries: Int,
    val centralDirectorySize: Long,
    val centralDirectoryOffset: Long,
    val comment: ByteArray,
)

internal data class LocalFileHeader(
    val versionNeeded: Int,
    val generalPurposeFlag: Int,
    val compressionMethod: Int,
    val lastModifiedTime: Int,
    val lastModifiedDate: Int,
    val crc32: Long,
    val compressedSize: Long,
    val uncompressedSize: Long,
    val fileNameBytes: ByteArray,
    val extraField: ByteArray,
)

internal data class CentralDirectoryRecord(
    val versionMadeBy: Int,
    val versionNeeded: Int,
    val generalPurposeFlag: Int,
    val compressionMethod: Int,
    val lastModifiedTime: Int,
    val lastModifiedDate: Int,
    val crc32: Long,
    val compressedSize: Long,
    val uncompressedSize: Long,
    val fileNameBytes: ByteArray,
    val extraField: ByteArray,
    val fileCommentBytes: ByteArray,
    val diskNumberStart: Int,
    val internalFileAttributes: Int,
    val externalFileAttributes: Long,
    val localHeaderOffset: Long,
)

internal data class ZipArchiveEntry(
    val displayName: String,
    val centralDirectoryRecord: CentralDirectoryRecord,
    val localFileHeader: LocalFileHeader,
    val localHeaderOffset: Long,
    val dataOffset: Long,
    val payloadLength: Long,
) {
    val isCompressed: Boolean = localFileHeader.compressionMethod != ZipFormat.STORED_COMPRESSION_METHOD
    val isDirectory: Boolean =
        localFileHeader.uncompressedSize == 0L && (displayName.endsWith("/") || displayName.endsWith("\\"))

    fun requiredAlignment(options: AlignmentOptions): Int {
        if (options.alignSharedLibraries && displayName.endsWith(".so")) {
            return options.sharedLibraryPageAlignment.bytes
        }
        return options.defaultAlignmentBytes
    }
}

internal data class ZipArchive(
    val sourceFile: File,
    val entries: List<ZipArchiveEntry>,
    val endOfCentralDirectory: EndOfCentralDirectory,
)
