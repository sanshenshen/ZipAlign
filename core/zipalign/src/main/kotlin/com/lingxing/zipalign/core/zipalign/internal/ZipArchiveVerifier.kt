package com.lingxing.zipalign.core.zipalign.internal

import com.lingxing.zipalign.core.zipalign.api.AlignmentOptions
import com.lingxing.zipalign.core.zipalign.api.VerificationEntry
import com.lingxing.zipalign.core.zipalign.api.VerificationEntryStatus
import com.lingxing.zipalign.core.zipalign.api.VerificationReport

internal class ZipArchiveVerifier {

    fun verify(
        archive: ZipArchive,
        options: AlignmentOptions,
    ): VerificationReport {
        val entries = archive.entries.map { entry ->
            when {
                entry.isCompressed -> VerificationEntry(
                    dataOffset = entry.dataOffset,
                    name = entry.displayName,
                    status = VerificationEntryStatus.COMPRESSED,
                    requiredAlignmentBytes = entry.requiredAlignment(options),
                )

                entry.isDirectory -> VerificationEntry(
                    dataOffset = entry.dataOffset,
                    name = entry.displayName,
                    status = VerificationEntryStatus.DIRECTORY,
                    requiredAlignmentBytes = entry.requiredAlignment(options),
                )

                else -> {
                    val requiredAlignment = entry.requiredAlignment(options)
                    val remainder = (entry.dataOffset % requiredAlignment).toInt()
                    VerificationEntry(
                        dataOffset = entry.dataOffset,
                        name = entry.displayName,
                        status = if (remainder == 0) {
                            VerificationEntryStatus.ALIGNED
                        } else {
                            VerificationEntryStatus.MISALIGNED
                        },
                        requiredAlignmentBytes = requiredAlignment,
                        remainder = remainder,
                    )
                }
            }
        }
        return VerificationReport(
            archiveName = archive.sourceFile.name,
            defaultAlignmentBytes = options.defaultAlignmentBytes,
            entries = entries,
        )
    }
}
