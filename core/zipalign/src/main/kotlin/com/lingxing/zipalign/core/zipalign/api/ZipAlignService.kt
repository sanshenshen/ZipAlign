package com.lingxing.zipalign.core.zipalign.api

import java.io.File

interface ZipAlignService {
    fun verify(inputFile: File, options: AlignmentOptions = AlignmentOptions()): VerificationReport

    fun align(
        inputFile: File,
        outputFile: File,
        options: AlignmentOptions = AlignmentOptions(),
        overwrite: Boolean = true,
    ): AlignmentReport
}
