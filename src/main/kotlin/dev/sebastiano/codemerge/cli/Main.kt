package dev.sebastiano.codemerge.cli

import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.file
import dev.sebastiano.codemerge.collectors.collectSourceFilesIn
import dev.sebastiano.codemerge.diff.CodeDiffResults
import dev.sebastiano.codemerge.diff.calculateCodeDiff
import dev.sebastiano.codemerge.diff.filterOnlyThoseFoundInSamePackagesAs
import dev.sebastiano.codemerge.io.copyFilesInPackageDir
import dev.sebastiano.codemerge.io.copyModifiedFiles
import dev.sebastiano.codemerge.io.deleteFiles
import java.io.File
import java.util.Locale
import java.util.Scanner
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = Main().main(args)

class Main : CliCommand(help = "Compare sources from a reference directory with a search directory — regardless of the relative paths.") {

    private val referenceDir by option(
        help = "Reference directory (to compare against), defaults to current work directory",
        names = *arrayOf("--ref", "-r")
    )
        .file(mustExist = true, canBeDir = true, canBeFile = false, canBeSymlink = false, mustBeReadable = true)
        .defaultLazy(defaultForHelp = "The current work directory") { File(System.getProperty("user.dir")) }
        .validate {
            val separator = when (File.separatorChar) {
                '\\' -> "\\\\"
                else -> File.separator!!
            }
            val path = it.absolutePath
            val regex = ".*src$separator[^$separator]+?$separator(java|kotlin)$separator?\$"
                .toRegex(RegexOption.IGNORE_CASE)
            require(path.matches(regex)) {
                "The reference directory must be a Java or Kotlin sources root"
            }
        }

    private val searchDir by argument(help = "Directory to look for changes in", name = "searchDir")
        .file(mustExist = true, canBeDir = true, canBeFile = false, canBeSymlink = false, mustBeReadable = true)

    private val excludePattern by option(
        help = "Regular expression to exclude files based on their full paths",
        names = *arrayOf("--exclude", "-e")
    ).convert { it.toRegex() }

    init {
        context {
            helpOptionNames = setOf("-h", "--help", "-?")
        }
    }

    override fun run() = runBlocking {
        super.run()
        val logger = env.logger

        logger.i("Indexing source files in '${referenceDir.absolutePath}'...")
        val sourceFiles = benchmark(logger) { collectSourceFilesIn(referenceDir, excludePattern) }
        logger.i("Source files indexed.")

        logger.i("Indexing files to check for changes in '${searchDir.absolutePath}'...")
        val filesToSearchIn = benchmark(logger) { collectSourceFilesIn(searchDir, excludePattern) }
        logger.i("Files to check indexed.")

        logger.i("Running diff calculation...")
        val rawDiff = benchmark(logger) { calculateCodeDiff(sourceFiles, filesToSearchIn) }
        logger.i("Diff calculation completed.")
        logger.i(
            "Files changed: ${rawDiff.modified.size}, unchanged: ${rawDiff.unchanged.size}, " +
                "added: ${rawDiff.added.size}, removed: ${rawDiff.removed.size}"
        )

        logger.i("Filtering out added files...")
        val diff = benchmark(logger) {
            rawDiff.copy(added = rawDiff.added.filterOnlyThoseFoundInSamePackagesAs(sourceFiles))
        }
        logger.i("Filtering done. Files added in existing packages: ${diff.added.size}")

        logger.i("")
        promptActionsOnDiff(diff, logger)
        logger.i("")
        logger.i("All done, have a great day :)")
    }

    private inline fun <T> benchmark(logger: Logger, function: () -> T): T {
        val startTimeNano = System.nanoTime()
        val value = function()

        val durationNano = System.nanoTime() - startTimeNano
        logger.v("  Operation duration: ${formatTimeDuration(durationNano)}")

        return value
    }

    private fun formatTimeDuration(durationNano: Long): String {
        val hours = TimeUnit.NANOSECONDS.toHours(durationNano) % 24
        val minutes = (TimeUnit.NANOSECONDS.toMinutes(durationNano) % 60).toString().padStart(2, '0')
        val seconds = (TimeUnit.NANOSECONDS.toSeconds(durationNano) % 60).toString().padStart(2, '0')
        val millis = (TimeUnit.NANOSECONDS.toMillis(durationNano) % 1000).toString().padStart(3, '0')

        return "$hours:$minutes:$seconds.$millis"
    }

    private fun promptActionsOnDiff(diff: CodeDiffResults, logger: Logger) {
        Scanner(env.inputStream).use {
            if (diff.modified.isNotEmpty()) {
                if (it.confirm("Do you want to copy over the MODIFIED files to the reference directory?", logger)) {
                    copyModifiedFiles(diff.modified)
                }
            }

            if (diff.added.isNotEmpty()) {
                if (it.confirm("Do you want to copy over the ADDED files to the reference directory?", logger)) {
                    copyFilesInPackageDir(referenceDir, diff.added)
                }
            }

            if (diff.removed.isNotEmpty()) {
                if (it.confirm("Do you want to delete the REMOVED files in the reference directory?", logger)) {
                    deleteFiles(diff.removed, logger)
                }
            }
        }
    }

    private fun Scanner.confirm(prompt: String?, logger: Logger): Boolean {
        prompt?.let { logger.i(prompt) }
        return when (nextLine().trim().toLowerCase(Locale.ROOT)) {
            "y", "yes", "yep", "yup" -> true
            "n", "no", "nope" -> false
            else -> {
                logger.e("Please respond with [y/n]")
                confirm(prompt = null, logger = logger)
            }
        }
    }
}
