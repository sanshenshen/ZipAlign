package com.lingxing.zipalign.core.zipalign.internal

import com.lingxing.zipalign.core.zipalign.ZipAlignException
import java.io.EOFException
import java.io.RandomAccessFile

internal fun ByteArray.readUnsignedShortLE(offset: Int): Int {
    return (this[offset].toInt() and 0xFF) or ((this[offset + 1].toInt() and 0xFF) shl 8)
}

internal fun ByteArray.readUnsignedIntLE(offset: Int): Long {
    return (this[offset].toLong() and 0xFF) or
        ((this[offset + 1].toLong() and 0xFF) shl 8) or
        ((this[offset + 2].toLong() and 0xFF) shl 16) or
        ((this[offset + 3].toLong() and 0xFF) shl 24)
}

internal fun RandomAccessFile.readUnsignedShortLE(): Int {
    val low = read()
    val high = read()
    if (low == -1 || high == -1) {
        throw EOFException("Unexpected end of archive.")
    }
    return low or (high shl 8)
}

internal fun RandomAccessFile.readUnsignedIntLE(): Long {
    val b0 = read()
    val b1 = read()
    val b2 = read()
    val b3 = read()
    if (b0 == -1 || b1 == -1 || b2 == -1 || b3 == -1) {
        throw EOFException("Unexpected end of archive.")
    }
    return (b0.toLong() and 0xFF) or
        ((b1.toLong() and 0xFF) shl 8) or
        ((b2.toLong() and 0xFF) shl 16) or
        ((b3.toLong() and 0xFF) shl 24)
}

internal fun RandomAccessFile.readExact(length: Int): ByteArray {
    val buffer = ByteArray(length)
    readFully(buffer)
    return buffer
}

internal fun RandomAccessFile.writeUnsignedShortLE(value: Int) {
    require(value in 0..ZipFormat.MAX_UNSIGNED_SHORT) { "Value out of range for unsigned short: $value" }
    write(value and 0xFF)
    write((value ushr 8) and 0xFF)
}

internal fun RandomAccessFile.writeUnsignedIntLE(value: Long) {
    require(value in 0..ZipFormat.MAX_UNSIGNED_INT) { "Value out of range for unsigned int: $value" }
    write((value and 0xFF).toInt())
    write(((value ushr 8) and 0xFF).toInt())
    write(((value ushr 16) and 0xFF).toInt())
    write(((value ushr 24) and 0xFF).toInt())
}

internal fun copyRange(
    input: RandomAccessFile,
    output: RandomAccessFile,
    startOffset: Long,
    byteCount: Long,
) {
    input.seek(startOffset)
    var remaining = byteCount
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    while (remaining > 0) {
        val chunkSize = minOf(buffer.size.toLong(), remaining).toInt()
        val bytesRead = input.read(buffer, 0, chunkSize)
        if (bytesRead < 0) {
            throw ZipAlignException("Unexpected end of archive while copying entry data.")
        }
        output.write(buffer, 0, bytesRead)
        remaining -= bytesRead
    }
}
