package dev.sebastiano.codemerge.cli

import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import dev.sebastiano.codemerge.collectors.collectSourceFilesIn
import java.io.File
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = Main().main(args)

class Main : CliCommand(help = "Compare sources from a reference directory with a search directory — regardless of the relative paths.") {

    private val referenceDir by option(
        help = "Reference directory (to compare against), defaults to current work directory",
        names = *arrayOf("--ref", "-r")
    )
        .file(mustExist = true, canBeDir = true, canBeFile = false, canBeSymlink = false, mustBeReadable = true)
        .defaultLazy(defaultForHelp = "The current work directory") { File(System.getProperty("user.dir")) }

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
        env.logger.i("Indexing source files in '${referenceDir.absolutePath}'...")
        val sourceFiles = collectSourceFilesIn(referenceDir, excludePattern)
        env.logger.i("Source files indexed.")

        env.logger.i("Indexing files to check for changes in '${searchDir.absolutePath}'...")
        val filesToSearchIn = collectSourceFilesIn(searchDir, excludePattern)
        env.logger.i("Files to check indexed.")
    }
}
