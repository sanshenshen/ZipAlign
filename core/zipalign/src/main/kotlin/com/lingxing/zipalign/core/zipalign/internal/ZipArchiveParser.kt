package com.lingxing.zipalign.core.zipalign.internal

import com.lingxing.zipalign.core.zipalign.ZipAlignException
import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.Charset

internal class ZipArchiveParser {

    private val cp437Charset: Charset = Charset.forName("IBM437")

    fun parse(file: File): ZipArchive {
        RandomAccessFile(file, "r").use { archiveFile ->
            val endOfCentralDirectory = readEndOfCentralDirectory(archiveFile)
            val centralRecords = readCentralDirectoryRecords(archiveFile, endOfCentralDirectory)
            val nextLocalOffsetByCurrent = buildNextLocalOffsetMap(centralRecords, endOfCentralDirectory)
            val entries = centralRecords.map { record ->
                val localHeader = readLocalFileHeader(archiveFile, record.localHeaderOffset)
                val dataOffset = record.localHeaderOffset +
                    ZipFormat.LOCAL_FILE_HEADER_FIXED_SIZE +
                    localHeader.fileNameBytes.size +
                    localHeader.extraField.size
                val nextLocalOffset = nextLocalOffsetByCurrent[record.localHeaderOffset]
                    ?: throw ZipAlignException("Missing next local header offset for ${record.localHeaderOffset}.")
                val payloadLength = nextLocalOffset - dataOffset
                if (payloadLength < 0) {
                    throw ZipAlignException("Corrupt archive entry: negative payload size for ${decodeName(record)}.")
                }
                ZipArchiveEntry(
                    displayName = decodeName(record),
                    centralDirectoryRecord = record,
                    localFileHeader = localHeader,
                    localHeaderOffset = record.localHeaderOffset,
                    dataOffset = dataOffset,
                    payloadLength = payloadLength,
                )
            }
            return ZipArchive(
                sourceFile = file,
                entries = entries,
                endOfCentralDirectory = endOfCentralDirectory,
            )
        }
    }

    private fun readEndOfCentralDirectory(archiveFile: RandomAccessFile): EndOfCentralDirectory {
        val fileLength = archiveFile.length()
        val searchLength = minOf(
            fileLength,
            (ZipFormat.END_OF_CENTRAL_DIRECTORY_FIXED_SIZE + ZipFormat.MAX_COMMENT_LENGTH).toLong(),
        ).toInt()
        archiveFile.seek(fileLength - searchLength)
        val tailBuffer = archiveFile.readExact(searchLength)
        for (offset in tailBuffer.size - ZipFormat.END_OF_CENTRAL_DIRECTORY_FIXED_SIZE downTo 0) {
            if (tailBuffer.readUnsignedIntLE(offset).toInt() != ZipFormat.END_OF_CENTRAL_DIRECTORY_SIGNATURE) {
                continue
            }
            val commentLength = tailBuffer.readUnsignedShortLE(offset + 20)
            if (offset + ZipFormat.END_OF_CENTRAL_DIRECTORY_FIXED_SIZE + commentLength != tailBuffer.size) {
                continue
            }
            val diskNumber = tailBuffer.readUnsignedShortLE(offset + 4)
            val centralDirectoryDisk = tailBuffer.readUnsignedShortLE(offset + 6)
            if (diskNumber != 0 || centralDirectoryDisk != 0) {
                throw ZipAlignException("Multi-disk ZIP archives are not supported.")
            }
            val totalEntriesOnDisk = tailBuffer.readUnsignedShortLE(offset + 8)
            val totalEntries = tailBuffer.readUnsignedShortLE(offset + 10)
            val centralDirectorySize = tailBuffer.readUnsignedIntLE(offset + 12)
            val centralDirectoryOffset = tailBuffer.readUnsignedIntLE(offset + 16)
            if (
                totalEntriesOnDisk == ZipFormat.MAX_UNSIGNED_SHORT ||
                totalEntries == ZipFormat.MAX_UNSIGNED_SHORT ||
                centralDirectorySize == ZipFormat.MAX_UNSIGNED_INT ||
                centralDirectoryOffset == ZipFormat.MAX_UNSIGNED_INT
            ) {
                throw ZipAlignException("ZIP64 archives are not supported.")
            }
            return EndOfCentralDirectory(
                totalEntries = totalEntries,
                centralDirectorySize = centralDirectorySize,
                centralDirectoryOffset = centralDirectoryOffset,
                comment = tailBuffer.copyOfRange(
                    offset + ZipFormat.END_OF_CENTRAL_DIRECTORY_FIXED_SIZE,
                    offset + ZipFormat.END_OF_CENTRAL_DIRECTORY_FIXED_SIZE + commentLength,
                ),
            )
        }
        throw ZipAlignException("Unable to locate the end of central directory record.")
    }

    private fun readCentralDirectoryRecords(
        archiveFile: RandomAccessFile,
        endOfCentralDirectory: EndOfCentralDirectory,
    ): List<CentralDirectoryRecord> {
        archiveFile.seek(endOfCentralDirectory.centralDirectoryOffset)
        return List(endOfCentralDirectory.totalEntries) {
            val signature = archiveFile.readUnsignedIntLE().toInt()
            if (signature != ZipFormat.CENTRAL_DIRECTORY_FILE_HEADER_SIGNATURE) {
                throw ZipAlignException("Invalid central directory signature: $signature")
            }
            val versionMadeBy = archiveFile.readUnsignedShortLE()
            val versionNeeded = archiveFile.readUnsignedShortLE()
            val generalPurposeFlag = archiveFile.readUnsignedShortLE()
            val compressionMethod = archiveFile.readUnsignedShortLE()
            val lastModifiedTime = archiveFile.readUnsignedShortLE()
            val lastModifiedDate = archiveFile.readUnsignedShortLE()
            val crc32 = archiveFile.readUnsignedIntLE()
            val compressedSize = archiveFile.readUnsignedIntLE()
            val uncompressedSize = archiveFile.readUnsignedIntLE()
            val fileNameLength = archiveFile.readUnsignedShortLE()
            val extraFieldLength = archiveFile.readUnsignedShortLE()
            val fileCommentLength = archiveFile.readUnsignedShortLE()
            val diskNumberStart = archiveFile.readUnsignedShortLE()
            val internalFileAttributes = archiveFile.readUnsignedShortLE()
            val externalFileAttributes = archiveFile.readUnsignedIntLE()
            val localHeaderOffset = archiveFile.readUnsignedIntLE()
            CentralDirectoryRecord(
                versionMadeBy = versionMadeBy,
                versionNeeded = versionNeeded,
                generalPurposeFlag = generalPurposeFlag,
                compressionMethod = compressionMethod,
                lastModifiedTime = lastModifiedTime,
                lastModifiedDate = lastModifiedDate,
                crc32 = crc32,
                compressedSize = compressedSize,
                uncompressedSize = uncompressedSize,
                fileNameBytes = archiveFile.readExact(fileNameLength),
                extraField = archiveFile.readExact(extraFieldLength),
                fileCommentBytes = archiveFile.readExact(fileCommentLength),
                diskNumberStart = diskNumberStart,
                internalFileAttributes = internalFileAttributes,
                externalFileAttributes = externalFileAttributes,
                localHeaderOffset = localHeaderOffset,
            )
        }
    }

    private fun readLocalFileHeader(
        archiveFile: RandomAccessFile,
        localHeaderOffset: Long,
    ): LocalFileHeader {
        archiveFile.seek(localHeaderOffset)
        val signature = archiveFile.readUnsignedIntLE().toInt()
        if (signature != ZipFormat.LOCAL_FILE_HEADER_SIGNATURE) {
            throw ZipAlignException("Invalid local file header signature at offset $localHeaderOffset.")
        }
        val versionNeeded = archiveFile.readUnsignedShortLE()
        val generalPurposeFlag = archiveFile.readUnsignedShortLE()
        val compressionMethod = archiveFile.readUnsignedShortLE()
        val lastModifiedTime = archiveFile.readUnsignedShortLE()
        val lastModifiedDate = archiveFile.readUnsignedShortLE()
        val crc32 = archiveFile.readUnsignedIntLE()
        val compressedSize = archiveFile.readUnsignedIntLE()
        val uncompressedSize = archiveFile.readUnsignedIntLE()
        val fileNameLength = archiveFile.readUnsignedShortLE()
        val extraFieldLength = archiveFile.readUnsignedShortLE()
        return LocalFileHeader(
            versionNeeded = versionNeeded,
            generalPurposeFlag = generalPurposeFlag,
            compressionMethod = compressionMethod,
            lastModifiedTime = lastModifiedTime,
            lastModifiedDate = lastModifiedDate,
            crc32 = crc32,
            compressedSize = compressedSize,
            uncompressedSize = uncompressedSize,
            fileNameBytes = archiveFile.readExact(fileNameLength),
            extraField = archiveFile.readExact(extraFieldLength),
        )
    }

    private fun buildNextLocalOffsetMap(
        centralRecords: List<CentralDirectoryRecord>,
        endOfCentralDirectory: EndOfCentralDirectory,
    ): Map<Long, Long> {
        val sortedOffsets = centralRecords.map { it.localHeaderOffset }.sorted()
        if (sortedOffsets.size != sortedOffsets.distinct().size) {
            throw ZipAlignException("Archive contains duplicate local header offsets.")
        }
        return buildMap(sortedOffsets.size) {
            sortedOffsets.forEachIndexed { index, currentOffset ->
                val nextOffset = sortedOffsets.getOrNull(index + 1) ?: endOfCentralDirectory.centralDirectoryOffset
                put(currentOffset, nextOffset)
            }
        }
    }

    private fun decodeName(record: CentralDirectoryRecord): String {
        val charset = if ((record.generalPurposeFlag and ZipFormat.UTF8_NAME_FLAG) != 0) {
            Charsets.UTF_8
        } else {
            cp437Charset
        }
        return record.fileNameBytes.toString(charset)
    }
}
