package com.lingxing.zipalign.core.zipalign.internal

internal object ZipFormat {
    const val CENTRAL_DIRECTORY_FILE_HEADER_SIGNATURE = 0x02014B50
    const val END_OF_CENTRAL_DIRECTORY_SIGNATURE = 0x06054B50
    const val LOCAL_FILE_HEADER_SIGNATURE = 0x04034B50

    const val STORED_COMPRESSION_METHOD = 0
    const val UTF8_NAME_FLAG = 1 shl 11

    const val END_OF_CENTRAL_DIRECTORY_FIXED_SIZE = 22
    const val LOCAL_FILE_HEADER_FIXED_SIZE = 30
    const val MAX_COMMENT_LENGTH = 0xFFFF
    const val MAX_UNSIGNED_SHORT = 0xFFFF
    const val MAX_UNSIGNED_INT = 0xFFFFFFFFL
}
