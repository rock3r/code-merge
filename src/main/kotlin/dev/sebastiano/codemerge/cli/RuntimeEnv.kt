package dev.sebastiano.codemerge.cli

import java.io.InputStream

data class RuntimeEnv(
    val verbose: Boolean,
    val logger: Logger,
    val inputStream: InputStream = System.`in`
)
