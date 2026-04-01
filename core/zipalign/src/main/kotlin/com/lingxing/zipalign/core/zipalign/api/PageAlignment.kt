package com.lingxing.zipalign.core.zipalign.api

enum class PageAlignment(val kiloBytes: Int) {
    KB_4(4),
    KB_16(16),
    KB_64(64);

    val bytes: Int = kiloBytes * 1024
}
