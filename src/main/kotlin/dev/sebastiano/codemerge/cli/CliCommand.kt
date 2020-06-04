package dev.sebastiano.codemerge.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

abstract class CliCommand(help: String = "") : CliktCommand(help = help, printHelpOnEmptyArgs = true) {

    private lateinit var runtimeEnv: RuntimeEnv

    val env: RuntimeEnv
        get() = runtimeEnv

    private val verbose by option(help = "Print verbose info")
        .flag("-v", default = false)

    init {
        context {
            helpFormatter = CliktHelpFormatter(showDefaultValues = true)
        }
    }

    override fun run() {
        runtimeEnv = RuntimeEnv(
            verbose = verbose,
            logger = createLogger(
                name = "dev.sebastiano.codemerge",
                verbose = verbose,
                infoLogger = { echo(it, err = false) },
                errorLogger = { echo(it, err = true) }
            )
        )
    }
}
