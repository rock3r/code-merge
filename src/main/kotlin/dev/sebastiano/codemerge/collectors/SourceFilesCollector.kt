package dev.sebastiano.codemerge.collectors

import dev.sebastiano.codemerge.cli.CliCommand
import dev.sebastiano.codemerge.cli.Logger
import dev.sebastiano.codemerge.util.parallelMap
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

suspend fun CliCommand.collectSourceFilesIn(directory: File, excludeRegex: Regex?): SourceFilesSet = coroutineScope {
    val allFiles = withContext(Dispatchers.IO) { directory.walkTopDown().filter { it.isFile }.toList() }
    val allFilesCount = allFiles.count()
    val logger = env.logger
    logger.v("The folder contains $allFilesCount files")

    require(allFilesCount >= 0) { "The directory must contain at least one file" }

    val parsedFiles = withContext(Dispatchers.IO) {
        allFiles.filter { SourceFileInfo.SourceLanguage.detectLanguageFor(it) != null }
            .filterNot {
                if (excludeRegex == null) return@filterNot false
                val isExcluded = excludeRegex.containsMatchIn(it.absolutePath)
                if (isExcluded) logger.v("Ignoring file ${it.absolutePath}...")
                isExcluded
            }
            .parallelMap { file ->
                val fileContents = file.readText()
                val language = (SourceFileInfo.SourceLanguage.detectLanguageFor(file)
                    ?: throw IllegalStateException("File ${file.absolutePath} not supported"))

                val fullyQualifiedName = extractFqnFrom(fileContents, file, language, logger)
                    ?: return@parallelMap null

                SourceFileInfo(
                    file = file,
                    fullyQualifiedName = fullyQualifiedName,
                    language = language,
                    sha1 = calculateHashFor(fileContents)
                )
            }
    }
    val notExcludedCount = parsedFiles.count()
    val parsedSourceFiles = parsedFiles.filterNotNull()
    val sourceFilesCount = parsedSourceFiles.count()
    require(sourceFilesCount >= 0) { "The directory must contain at least one source file" }

    logger.i("The folder contains $sourceFilesCount valid source files")
    if (allFilesCount > notExcludedCount) {
        logger.i("${allFilesCount - notExcludedCount} files were ignored")
    }

    if (notExcludedCount > sourceFilesCount) {
        logger.w("${notExcludedCount - sourceFilesCount} file(s) could not be parsed as they may contain errors")
    }

    return@coroutineScope SourceFilesSet(parsedSourceFiles.toSet())
}

fun extractFqnFrom(fileContents: String, file: File, language: SourceFileInfo.SourceLanguage, logger: Logger): String? =
    when (language) {
        SourceFileInfo.SourceLanguage.JAVA -> extractFqnFromJavaSources(fileContents, file, logger)
        SourceFileInfo.SourceLanguage.KOTLIN -> extractFqnFromKotlinSources(fileContents, file, logger)
    }

private fun calculateHashFor(fileContents: String): ByteArray = MessageDigest.getInstance("SHA-1")
    .digest(fileContents.toByteArray())
