package dev.sebastiano.codemerge.collectors

import com.github.javaparser.ParseProblemException
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import dev.sebastiano.codemerge.cli.Logger
import java.io.File

fun extractFqnFromJavaSources(sources: String, file: File, logger: Logger): String? {
    val fileNameWithoutExtension = file.nameWithoutExtension
    val parsedCode = try {
        StaticJavaParser.parse(sources)
    } catch (e: ParseProblemException) {
        if (logger.isVerbose) {
            logger.v("Unable to parse Java sources from file $file. Problems:\n${e.problemsString()}")
        } else {
            logger.w("Unable to parse Java sources from file $file.")
        }
        return null
    }

    val primaryClassFqn = extractPrimaryClassFqnFrom(parsedCode, fileNameWithoutExtension)
    if (primaryClassFqn != null) return primaryClassFqn

    val filePackageName = extractPackageNameFrom(parsedCode) ?: return null
    return "$filePackageName.${fileNameWithoutExtension}Java"
}

private fun ParseProblemException.problemsString() = problems.joinToString(separator = "\n") { problem ->
    " * ${problem.verboseMessage}"
}

private fun extractPrimaryClassFqnFrom(sources: CompilationUnit, fileNameWithoutExtension: String): String? =
    sources.types.asSequence()
        .filter { it.nameAsString == fileNameWithoutExtension && !it.isPrivate }
        .firstOrNull()
        ?.fullyQualifiedName?.orElse(null)

private fun extractPackageNameFrom(sources: CompilationUnit): String? =
    sources.packageDeclaration.orElse(null)?.nameAsString
