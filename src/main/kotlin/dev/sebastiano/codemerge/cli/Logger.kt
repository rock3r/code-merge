package dev.sebastiano.codemerge.cli

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface Logger {
    fun e(msg: String)
    fun w(msg: String)
    fun i(msg: String)
    fun v(msg: String)
}

fun createLogger(
    name: String,
    verbose: Boolean,
    infoLogger: (String) -> Unit,
    errorLogger: (String) -> Unit = infoLogger
): Logger = DelegatingLogger(name, verbose, infoLogger, errorLogger)

private class DelegatingLogger(
    private val name: String,
    private val verbose: Boolean,
    private val infoLogger: (String) -> Unit,
    private val errorLogger: (String) -> Unit
) : Logger {

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun e(msg: String) {
        errorLogger(formatMessage("ERROR", msg))
    }

    override fun w(msg: String) {
        infoLogger(formatMessage("WARN", msg))
    }

    override fun i(msg: String) {
        infoLogger(formatMessage("INFO", msg))
    }

    override fun v(msg: String) {
        if (verbose) infoLogger(formatMessage("VERBOSE", msg))
    }

    private val timestampPad = 20
    private val levelPad = 8

    private fun formatMessage(level: String, msg: String): String {
        val timestamp = timeFormatter.format(LocalDateTime.now())
        return "[$name] ${timestamp.padEnd(timestampPad, ' ')} ${level.padEnd(levelPad, ' ')} — $msg"
    }
}
