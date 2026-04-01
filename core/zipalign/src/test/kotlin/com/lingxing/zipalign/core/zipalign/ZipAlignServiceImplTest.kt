package com.lingxing.zipalign.core.zipalign

import com.lingxing.zipalign.core.zipalign.api.AlignmentOptions
import com.lingxing.zipalign.core.zipalign.api.PageAlignment
import com.lingxing.zipalign.core.zipalign.api.VerificationEntryStatus
import java.io.File
import java.nio.file.Files
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ZipAlignServiceImplTest {

    private val service = ZipAlignServiceImpl()

    @Test
    fun verify_detects_unaligned_entries_and_shared_library_page_alignment() {
        val workingDirectory = Files.createTempDirectory("zipalign-verify").toFile()
        val inputArchive = createSampleArchive(File(workingDirectory, "unaligned.apk"))

        val report = service.verify(
            inputFile = inputArchive,
            options = AlignmentOptions(
                defaultAlignmentBytes = 4,
                alignSharedLibraries = true,
                sharedLibraryPageAlignment = PageAlignment.KB_16,
            ),
        )

        assertTrue(report.entries.any { it.status == VerificationEntryStatus.COMPRESSED })
        assertTrue(report.entries.any { it.status == VerificationEntryStatus.DIRECTORY })
        assertTrue(report.entries.any { it.name.endsWith(".so") && it.requiredAlignmentBytes == 16 * 1024 })
        assertTrue(report.misalignedCount > 0)
    }

    @Test
    fun align_rewrites_archive_and_preserves_payloads() {
        val workingDirectory = Files.createTempDirectory("zipalign-align").toFile()
        val inputArchive = createSampleArchive(File(workingDirectory, "source.apk"))
        val outputArchive = File(workingDirectory, "aligned.apk")

        val report = service.align(
            inputFile = inputArchive,
            outputFile = outputArchive,
            options = AlignmentOptions(
                defaultAlignmentBytes = 4,
                alignSharedLibraries = true,
                sharedLibraryPageAlignment = PageAlignment.KB_16,
            ),
        )

        assertTrue(report.verificationReport.isSuccessful)
        assertTrue(report.totalPaddingBytes > 0L)

        ZipFile(outputArchive).use { zipFile ->
            assertEquals("plain-bytes", zipFile.getInputStream(zipFile.getEntry("a.txt")).bufferedReader().readText())
            assertEquals("dex-data", zipFile.getInputStream(zipFile.getEntry("classes.dex")).bufferedReader().readText())
            assertEquals("native-lib", zipFile.getInputStream(zipFile.getEntry("lib/arm64-v8a/libdemo.so")).bufferedReader().readText())
        }
    }

    private fun createSampleArchive(targetFile: File): File {
        ZipOutputStream(targetFile.outputStream().buffered()).use { zipOutputStream ->
            zipOutputStream.putNextEntry(
                ZipEntry("META-INF/").apply {
                    method = ZipEntry.STORED
                    size = 0L
                    compressedSize = 0L
                    crc = 0L
                },
            )
            zipOutputStream.closeEntry()

            zipOutputStream.writeStoredEntry("a.txt", "plain-bytes".toByteArray())
            zipOutputStream.writeStoredEntry("lib/arm64-v8a/libdemo.so", "native-lib".toByteArray())

            zipOutputStream.putNextEntry(ZipEntry("classes.dex"))
            zipOutputStream.write("dex-data".toByteArray())
            zipOutputStream.closeEntry()
        }
        return targetFile
    }

    private fun ZipOutputStream.writeStoredEntry(
        name: String,
        content: ByteArray,
    ) {
        val crc32 = CRC32().apply { update(content) }.value
        val entry = ZipEntry(name).apply {
            method = ZipEntry.STORED
            size = content.size.toLong()
            compressedSize = content.size.toLong()
            crc = crc32
        }
        putNextEntry(entry)
        write(content)
        closeEntry()
    }
}
