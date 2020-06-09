package dev.sebastiano.codemerge.io

import dev.sebastiano.codemerge.cli.Logger
import dev.sebastiano.codemerge.collectors.SourceFileInfo
import dev.sebastiano.codemerge.diff.CodeDiffResults
import java.io.File

fun copyModifiedFiles(modifiedFiles: Set<CodeDiffResults.ModifiedFileInfo>) {
    for ((reference, modified) in modifiedFiles) {
        modified.file.copyTo(reference.file, overwrite = true)
    }
}

fun copyFilesInPackageDir(baseDir: File, files: Set<SourceFileInfo>) {
    require(baseDir.isDirectory) { "The base dir must be a valid directory, but '${baseDir.absolutePath}' isn't or doesn't exist" }
    fun String.asPath() = replace('.', File.separatorChar)

    for (fileInfo in files) {
        fileInfo.file.copyTo(File(baseDir, fileInfo.packageName.asPath()), overwrite = true)
    }
}

fun deleteFiles(files: Set<SourceFileInfo>, logger: Logger) {
    for (fileInfo in files) {
        val file = fileInfo.file
        if (!file.delete() && file.exists()) {
            logger.w("Unable to delete file '$file'")
        }
    }
}
