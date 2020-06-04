package dev.sebastiano.codemerge.collectors

import dev.sebastiano.codemerge.cli.CliCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

suspend fun CliCommand.collectReferenceSourceFiles(directory: File): Set<SourceFileInfo> = coroutineScope {
    val allFiles = withContext(Dispatchers.IO) { directory.walkTopDown().filter { it.isFile }.toList() }
    val allFilesCount = allFiles.count()
    env.logger.v("The reference folder contains $allFilesCount files")

    require(allFilesCount >= 0) { "The source directory must contain at least one file" }

    val sourceFiles = withContext(Dispatchers.IO) {
        allFiles.filter { SourceFileInfo.SourceLanguage.detectLanguageFor(it) != null }
            .pmap { file ->
                val fileContents = file.readText()
                val language = (SourceFileInfo.SourceLanguage.detectLanguageFor(file)
                    ?: throw IllegalStateException("File ${file.absolutePath} not supported"))

                SourceFileInfo(
                    file = file,
                    fullyQualifiedName = extractFqnFrom(fileContents, file.nameWithoutExtension, language),
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

fun extractFqnFrom(fileContents: String, fileName: String, language: SourceFileInfo.SourceLanguage): String = when (language) {
    SourceFileInfo.SourceLanguage.JAVA -> extractFqnFromJavaSources(fileContents, fileName)
    SourceFileInfo.SourceLanguage.KOTLIN -> extractFqnFromKotlinSources(fileContents, fileName)
}

private val sha1Digest = MessageDigest.getInstance("SHA-1")

private fun calculateHashFor(fileContents: String): ByteArray = sha1Digest.digest(fileContents.toByteArray())

private suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
    map { async { f(it) } }.awaitAll()
}
