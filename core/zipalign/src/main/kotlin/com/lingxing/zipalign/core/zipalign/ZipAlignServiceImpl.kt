package com.lingxing.zipalign.core.zipalign

import com.lingxing.zipalign.core.zipalign.api.AlignmentOptions
import com.lingxing.zipalign.core.zipalign.api.AlignmentReport
import com.lingxing.zipalign.core.zipalign.api.VerificationReport
import com.lingxing.zipalign.core.zipalign.api.ZipAlignService
import com.lingxing.zipalign.core.zipalign.internal.ZipArchiveParser
import com.lingxing.zipalign.core.zipalign.internal.ZipArchiveRewriter
import com.lingxing.zipalign.core.zipalign.internal.ZipArchiveVerifier
import java.io.File

class ZipAlignServiceImpl : ZipAlignService {

    private val parser = ZipArchiveParser()
    private val verifier = ZipArchiveVerifier()
    private val rewriter = ZipArchiveRewriter()

    override fun verify(inputFile: File, options: AlignmentOptions): VerificationReport {
        validateInputFile(inputFile)
        val archive = parser.parse(inputFile)
        return verifier.verify(archive, options)
    }

    override fun align(
        inputFile: File,
        outputFile: File,
        options: AlignmentOptions,
        overwrite: Boolean,
    ): AlignmentReport {
        validateInputFile(inputFile)
        if (inputFile.canonicalFile == outputFile.canonicalFile) {
            throw ZipAlignException("Input and output files must be different.")
        }
        if (outputFile.exists() && !overwrite) {
            throw ZipAlignException("Output file already exists: ${outputFile.absolutePath}")
        }
        outputFile.parentFile?.mkdirs()
        if (outputFile.exists() && !outputFile.delete()) {
            throw ZipAlignException("Unable to replace output file: ${outputFile.absolutePath}")
        }

        val archive = parser.parse(inputFile)
        return rewriter.rewrite(archive, outputFile, options)
    }

    private fun validateInputFile(inputFile: File) {
        if (!inputFile.exists() || !inputFile.isFile) {
            throw ZipAlignException("Input archive does not exist: ${inputFile.absolutePath}")
        }
    }
}
