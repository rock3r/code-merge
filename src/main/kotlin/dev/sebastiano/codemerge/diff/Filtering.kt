package dev.sebastiano.codemerge.diff

import dev.sebastiano.codemerge.collectors.SourceFileInfo
import dev.sebastiano.codemerge.collectors.SourceFilesSet

suspend fun Set<SourceFileInfo>.filterOnlyThoseFoundInSamePackagesAs(referenceFiles: SourceFilesSet): Set<SourceFileInfo> {
    val sourcePackages = referenceFiles.packages()

    return filter { sourcePackages.contains(it.packageName) }
        .toSet()
}
