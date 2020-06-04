package dev.sebastiano.codemerge.collectors

import java.io.File
import java.util.Locale

data class SourceFileInfo(
    val file: File,
    val fullyQualifiedName: String,
    val language: SourceLanguage,
    val sha1: ByteArray
) {

    enum class SourceLanguage(val extension: String) {
        JAVA("java"),
        KOTLIN("kt");

        companion object {

            fun detectLanguageFor(file: File): SourceLanguage? {
                return when (file.extension.toLowerCase(Locale.ROOT)) {
                    JAVA.extension -> JAVA
                    KOTLIN.extension -> KOTLIN
                    else -> null
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SourceFileInfo) return false

        if (file != other.file) return false
        if (fullyQualifiedName != other.fullyQualifiedName) return false
        if (language != other.language) return false
        if (!sha1.contentEquals(other.sha1)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = file.hashCode()
        result = 31 * result + fullyQualifiedName.hashCode()
        result = 31 * result + language.hashCode()
        result = 31 * result + sha1.contentHashCode()
        return result
    }

    override fun toString(): String =
        "SourceFileInfo(file=$file, fullyQualifiedName='$fullyQualifiedName', language=$language, sha1=${sha1.toHexString()})"

    private fun ByteArray.toHexString(): String = buildString {
        for (byte in this@toHexString) {
            append(String.format("%02x", byte))
        }
    }
}
