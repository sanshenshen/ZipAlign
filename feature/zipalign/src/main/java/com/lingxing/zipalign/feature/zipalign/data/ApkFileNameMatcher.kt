package com.lingxing.zipalign.feature.zipalign.data

object ApkFileNameMatcher {
    private val apkPattern = Regex(pattern = """.+\.apk(?:\..+)?$""", option = RegexOption.IGNORE_CASE)

    fun isApkLikeFile(name: String): Boolean {
        return apkPattern.matches(name)
    }
}
