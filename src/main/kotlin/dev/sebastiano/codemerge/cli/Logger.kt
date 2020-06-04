package dev.sebastiano.codemerge.cli

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale

interface Logger {
    fun e(msg: String)
    fun w(msg: String)
    fun i(msg: String)
    fun v(msg: String)

    val isVerbose: Boolean
}

fun createLogger(
    name: String,
    verbose: Boolean,
    infoLogger: (String) -> Unit,
    errorLogger: (String) -> Unit = infoLogger
): Logger = DelegatingLogger(name, verbose, infoLogger, errorLogger)

private class DelegatingLogger(
    private val name: String,
    override val isVerbose: Boolean,
    private val infoLogger: (String) -> Unit,
    private val errorLogger: (String) -> Unit
) : Logger {

    private val timeFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral(' ')
        .append(
            DateTimeFormatterBuilder()
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .optionalStart()
                .appendFraction(ChronoField.NANO_OF_SECOND, 3, 3, true)
                .toFormatter(Locale.ROOT))
        .toFormatter(Locale.ROOT)

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
        if (isVerbose) infoLogger(formatMessage("VERBOSE", msg))
    }

    private val timestampPad = 20
    private val levelPad = 8

    private fun formatMessage(level: String, msg: String): String {
        val timestamp = timeFormatter.format(LocalDateTime.now())
        return "[$name] ${timestamp.padEnd(timestampPad, ' ')} ${level.padEnd(levelPad, ' ')} — $msg"
    }
}
