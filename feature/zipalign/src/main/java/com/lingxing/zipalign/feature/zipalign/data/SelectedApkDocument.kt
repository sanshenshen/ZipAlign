package com.lingxing.zipalign.feature.zipalign.data

data class SelectedApkDocument(
    val absolutePath: String,
    val displayName: String,
    val sizeBytes: Long?,
    val parentDirectoryPath: String?,
) {
    val suggestedAlignedName: String
        get() {
            val dotIndex = displayName.lastIndexOf('.')
            return if (dotIndex > 0) {
                "${displayName.substring(0, dotIndex)}-aligned${displayName.substring(dotIndex)}"
            } else {
                "$displayName-aligned.apk"
            }
        }
}
