package dev.sebastiano.codemerge.collectors

import dev.sebastiano.codemerge.util.parallelMap

data class SourceFilesSet(val files: Set<SourceFileInfo>) : Set<SourceFileInfo> by files {

    private lateinit var packagesCache: List<String>

    suspend fun packages(): List<String> {
        if (!this::packagesCache.isInitialized) {
            packagesCache = files.parallelMap { it.packageName }
                .distinct()
                .sorted()
        }
        return packagesCache
    }

    private lateinit var fqnCache: List<String>

    suspend fun fullyQualifiedNames(): List<String> {
        if (!this::fqnCache.isInitialized) {
            fqnCache = files.parallelMap { it.fullyQualifiedName }
                .sorted()
        }
        return fqnCache
    }
}
