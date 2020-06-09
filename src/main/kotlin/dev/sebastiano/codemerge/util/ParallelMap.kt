package dev.sebastiano.codemerge.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend inline fun <T, R : Any?> Iterable<T>.parallelMap(crossinline f: (T) -> R): List<R> = coroutineScope {
    map { async { f(it) } }.awaitAll()
}
