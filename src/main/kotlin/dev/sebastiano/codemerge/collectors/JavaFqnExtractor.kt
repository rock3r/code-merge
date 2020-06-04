package dev.sebastiano.codemerge.collectors

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import java.util.Locale

fun extractFqnFromJavaSources(sources: String, fileNameWithoutExtension: String): String {
    require(!fileNameWithoutExtension.toLowerCase(Locale.ROOT).endsWith(".java")) { "The file name must not contain the extension" }
    val parsedCode = StaticJavaParser.parse(sources)

    return extractPrimaryClassFqnFrom(parsedCode, fileNameWithoutExtension)
        ?: "${extractPackageNameFrom(parsedCode)}.${fileNameWithoutExtension}Java"
}

private fun extractPrimaryClassFqnFrom(sources: CompilationUnit, fileNameWithoutExtension: String): String? =
    sources.types.asSequence()
        .filter { it.nameAsString == fileNameWithoutExtension && !it.isPrivate }
        .firstOrNull()
        ?.fullyQualifiedName?.orElse(null)

private fun extractPackageNameFrom(sources: CompilationUnit): String =
    sources.packageDeclaration.get().nameAsString
