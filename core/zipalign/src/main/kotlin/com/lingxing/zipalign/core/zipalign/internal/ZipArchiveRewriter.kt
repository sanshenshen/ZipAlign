package com.lingxing.zipalign.core.zipalign.internal

import com.lingxing.zipalign.core.zipalign.ZipAlignException
import com.lingxing.zipalign.core.zipalign.api.AlignmentOptions
import com.lingxing.zipalign.core.zipalign.api.AlignmentReport
import com.lingxing.zipalign.core.zipalign.api.RewrittenEntry
import java.io.File
import java.io.RandomAccessFile

internal class ZipArchiveRewriter(
    private val parser: ZipArchiveParser = ZipArchiveParser(),
    private val verifier: ZipArchiveVerifier = ZipArchiveVerifier(),
) {

    fun rewrite(
        archive: ZipArchive,
        outputFile: File,
        options: AlignmentOptions,
    ): AlignmentReport {
        val rewrittenEntries = mutableListOf<RewrittenEntry>()
        RandomAccessFile(archive.sourceFile, "r").use { inputFile ->
            RandomAccessFile(outputFile, "rw").use { outputArchive ->
                outputArchive.setLength(0)
                val updatedOffsets = LinkedHashMap<ZipArchiveEntry, Long>(archive.entries.size)
                archive.entries.forEach { entry ->
                    val newLocalHeaderOffset = outputArchive.filePointer
                    updatedOffsets[entry] = newLocalHeaderOffset
                    val requiredAlignment = entry.requiredAlignment(options)
                    val padding = calculatePadding(newLocalHeaderOffset, entry, requiredAlignment)
                    val alignedDataOffset = newLocalHeaderOffset +
                        ZipFormat.LOCAL_FILE_HEADER_FIXED_SIZE +
                        entry.localFileHeader.fileNameBytes.size +
                        entry.localFileHeader.extraField.size +
                        padding
                    writeLocalHeader(outputArchive, entry.localFileHeader, padding)
                    outputArchive.write(entry.localFileHeader.fileNameBytes)
                    outputArchive.write(entry.localFileHeader.extraField)
                    if (padding > 0) {
                        outputArchive.write(ByteArray(padding))
                    }
                    copyRange(
                        input = inputFile,
                        output = outputArchive,
                        startOffset = entry.dataOffset,
                        byteCount = entry.payloadLength,
                    )
                    rewrittenEntries += RewrittenEntry(
                        name = entry.displayName,
                        originalDataOffset = entry.dataOffset,
                        alignedDataOffset = alignedDataOffset,
                        requiredAlignmentBytes = requiredAlignment,
                        paddingBytesAdded = padding,
                    )
                }

                val centralDirectoryOffset = outputArchive.filePointer
                archive.entries.forEach { entry ->
                    writeCentralDirectoryRecord(
                        outputArchive = outputArchive,
                        record = entry.centralDirectoryRecord,
                        updatedLocalHeaderOffset = updatedOffsets.getValue(entry),
                    )
                }
                val centralDirectorySize = outputArchive.filePointer - centralDirectoryOffset
                writeEndOfCentralDirectory(
                    outputArchive = outputArchive,
                    entryCount = archive.entries.size,
                    centralDirectoryOffset = centralDirectoryOffset,
                    centralDirectorySize = centralDirectorySize,
                    comment = archive.endOfCentralDirectory.comment,
                )
            }
        }

        val verificationArchive = parser.parse(outputFile)
        val verificationReport = verifier.verify(verificationArchive, options)
        return AlignmentReport(
            inputArchiveName = archive.sourceFile.name,
            outputArchiveName = outputFile.name,
            rewrittenEntries = rewrittenEntries,
            verificationReport = verificationReport,
        )
    }

    private fun calculatePadding(
        localHeaderOffset: Long,
        entry: ZipArchiveEntry,
        requiredAlignment: Int,
    ): Int {
        if (entry.isCompressed || entry.isDirectory) {
            return 0
        }
        val baseDataOffset = localHeaderOffset +
            ZipFormat.LOCAL_FILE_HEADER_FIXED_SIZE +
            entry.localFileHeader.fileNameBytes.size +
            entry.localFileHeader.extraField.size
        val remainder = (baseDataOffset % requiredAlignment).toInt()
        return if (remainder == 0) 0 else requiredAlignment - remainder
    }

    private fun writeLocalHeader(
        outputArchive: RandomAccessFile,
        localFileHeader: LocalFileHeader,
        extraPaddingBytes: Int,
    ) {
        val newExtraFieldLength = localFileHeader.extraField.size + extraPaddingBytes
        if (newExtraFieldLength > ZipFormat.MAX_UNSIGNED_SHORT) {
            throw ZipAlignException("Local header extra field exceeds ZIP format limits.")
        }
        outputArchive.writeUnsignedIntLE(ZipFormat.LOCAL_FILE_HEADER_SIGNATURE.toLong())
        outputArchive.writeUnsignedShortLE(localFileHeader.versionNeeded)
        outputArchive.writeUnsignedShortLE(localFileHeader.generalPurposeFlag)
        outputArchive.writeUnsignedShortLE(localFileHeader.compressionMethod)
        outputArchive.writeUnsignedShortLE(localFileHeader.lastModifiedTime)
        outputArchive.writeUnsignedShortLE(localFileHeader.lastModifiedDate)
        outputArchive.writeUnsignedIntLE(localFileHeader.crc32)
        outputArchive.writeUnsignedIntLE(localFileHeader.compressedSize)
        outputArchive.writeUnsignedIntLE(localFileHeader.uncompressedSize)
        outputArchive.writeUnsignedShortLE(localFileHeader.fileNameBytes.size)
        outputArchive.writeUnsignedShortLE(newExtraFieldLength)
    }

    private fun writeCentralDirectoryRecord(
        outputArchive: RandomAccessFile,
        record: CentralDirectoryRecord,
        updatedLocalHeaderOffset: Long,
    ) {
        outputArchive.writeUnsignedIntLE(ZipFormat.CENTRAL_DIRECTORY_FILE_HEADER_SIGNATURE.toLong())
        outputArchive.writeUnsignedShortLE(record.versionMadeBy)
        outputArchive.writeUnsignedShortLE(record.versionNeeded)
        outputArchive.writeUnsignedShortLE(record.generalPurposeFlag)
        outputArchive.writeUnsignedShortLE(record.compressionMethod)
        outputArchive.writeUnsignedShortLE(record.lastModifiedTime)
        outputArchive.writeUnsignedShortLE(record.lastModifiedDate)
        outputArchive.writeUnsignedIntLE(record.crc32)
        outputArchive.writeUnsignedIntLE(record.compressedSize)
        outputArchive.writeUnsignedIntLE(record.uncompressedSize)
        outputArchive.writeUnsignedShortLE(record.fileNameBytes.size)
        outputArchive.writeUnsignedShortLE(record.extraField.size)
        outputArchive.writeUnsignedShortLE(record.fileCommentBytes.size)
        outputArchive.writeUnsignedShortLE(record.diskNumberStart)
        outputArchive.writeUnsignedShortLE(record.internalFileAttributes)
        outputArchive.writeUnsignedIntLE(record.externalFileAttributes)
        outputArchive.writeUnsignedIntLE(updatedLocalHeaderOffset)
        outputArchive.write(record.fileNameBytes)
        outputArchive.write(record.extraField)
        outputArchive.write(record.fileCommentBytes)
    }

    private fun writeEndOfCentralDirectory(
        outputArchive: RandomAccessFile,
        entryCount: Int,
        centralDirectoryOffset: Long,
        centralDirectorySize: Long,
        comment: ByteArray,
    ) {
        if (entryCount > ZipFormat.MAX_UNSIGNED_SHORT) {
            throw ZipAlignException("ZIP64 output archives are not supported.")
        }
        outputArchive.writeUnsignedIntLE(ZipFormat.END_OF_CENTRAL_DIRECTORY_SIGNATURE.toLong())
        outputArchive.writeUnsignedShortLE(0)
        outputArchive.writeUnsignedShortLE(0)
        outputArchive.writeUnsignedShortLE(entryCount)
        outputArchive.writeUnsignedShortLE(entryCount)
        outputArchive.writeUnsignedIntLE(centralDirectorySize)
        outputArchive.writeUnsignedIntLE(centralDirectoryOffset)
        outputArchive.writeUnsignedShortLE(comment.size)
        outputArchive.write(comment)
    }
}
