package dev.sebastiano.codemerge.collectors

import dev.sebastiano.codemerge.cli.CliCommand
import dev.sebastiano.codemerge.cli.Logger
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

suspend fun CliCommand.collectSourceFilesIn(directory: File): Set<SourceFileInfo> = coroutineScope {
    val allFiles = withContext(Dispatchers.IO) { directory.walkTopDown().filter { it.isFile }.toList() }
    val allFilesCount = allFiles.count()
    env.logger.v("The reference folder contains $allFilesCount files")

    require(allFilesCount >= 0) { "The source directory must contain at least one file" }

    val sourceFiles = withContext(Dispatchers.IO) {
        allFiles.filter { SourceFileInfo.SourceLanguage.detectLanguageFor(it) != null }
            .parallelMapNotNull { file ->
                val fileContents = file.readText()
                val language = (SourceFileInfo.SourceLanguage.detectLanguageFor(file)
                    ?: throw IllegalStateException("File ${file.absolutePath} not supported"))

                val fullyQualifiedName = extractFqnFrom(fileContents, file, language, env.logger)
                    ?: return@parallelMapNotNull null

                SourceFileInfo(
                    file = file,
                    fullyQualifiedName = fullyQualifiedName,
                    language = language,
                    sha1 = calculateHashFor(fileContents)
                )
            }
    }
    val sourceFilesCount = sourceFiles.count()
    env.logger.i("The reference folder contains $sourceFilesCount source files")
    require(sourceFilesCount >= 0) { "The source directory must contain at least one source file" }

    return@coroutineScope sourceFiles.toSet()
}

fun extractFqnFrom(fileContents: String, file: File, language: SourceFileInfo.SourceLanguage, logger: Logger): String? =
    when (language) {
        SourceFileInfo.SourceLanguage.JAVA -> extractFqnFromJavaSources(fileContents, file, logger)
        SourceFileInfo.SourceLanguage.KOTLIN -> extractFqnFromKotlinSources(fileContents, file, logger)
    }

private fun calculateHashFor(fileContents: String): ByteArray = MessageDigest.getInstance("SHA-1")
    .digest(fileContents.toByteArray())

private suspend inline fun <T, R : Any> Iterable<T>.parallelMapNotNull(crossinline f: (T) -> R?): List<R> = coroutineScope {
    map { async { f(it) } }.awaitAll().filterNotNull()
}
