package dev.sebastiano.codemerge.diff

import dev.sebastiano.codemerge.collectors.SourceFileInfo
import dev.sebastiano.codemerge.util.parallelMap

suspend fun excludeUnrelatedAdditions(addedFiles: Set<SourceFileInfo>, referenceFiles: Iterable<SourceFileInfo>): Set<SourceFileInfo> {
    val sourcePackages = referenceFiles.toList()
        .parallelMap { it.packageName }
        .distinct()
        .sorted()

    return addedFiles.filter { sourcePackages.contains(it.packageName) }
        .toSet()
}
