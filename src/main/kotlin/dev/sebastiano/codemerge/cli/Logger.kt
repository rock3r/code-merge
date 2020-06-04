package dev.sebastiano.codemerge.cli

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Logger(
    private val name: String,
    private val verbose: Boolean,
    private val infoLogger: (String) -> Unit,
    private val errorLogger: (String) -> Unit = infoLogger
) {

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun i(msg: String) {
        infoLogger(formatMessage("INFO", msg))
    }

    fun w(msg: String) {
        errorLogger(formatMessage("ERROR", msg))
    }

    fun v(msg: String) {
        if (verbose) infoLogger(formatMessage("VERBOSE", msg))
    }

    private val timestampPad = 20
    private val levelPad = 8

    private fun formatMessage(level: String, msg: String): String {
        val timestamp = timeFormatter.format(LocalDateTime.now())
        return "[$name] ${timestamp.padEnd(timestampPad, ' ')} ${level.padEnd(levelPad, ' ')} — $msg"
    }
}
