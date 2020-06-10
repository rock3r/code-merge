package dev.sebastiano.codemerge.diff

import dev.sebastiano.codemerge.collectors.SourceFileInfo
import dev.sebastiano.codemerge.collectors.SourceFilesSet
import dev.sebastiano.codemerge.util.parallelMap

suspend fun calculateCodeDiff(referenceFilesSet: SourceFilesSet, comparisonFilesSet: SourceFilesSet): CodeDiffResults {
    val rawDiff = calculateCoarseDiff(oldFiles = referenceFilesSet, newFiles = comparisonFilesSet)
    val potentiallyChanged = rawDiff.common
    val modified = potentiallyChanged.filterNot { it.old.sha1.contentEquals(it.new.sha1) }
        .toSet()

    val unchanged = potentiallyChanged.filter { it.old.sha1.contentEquals(it.new.sha1) }
        .parallelMap { it.old }
        .toSet()

    return CodeDiffResults(unchanged, modified, rawDiff.added, rawDiff.removed)
}

private suspend fun calculateCoarseDiff(
    oldFiles: SourceFilesSet,
    newFiles: Set<SourceFileInfo>
): RawFilesDiff {
    val commonFiles = newFiles intersectAsSourceFiles oldFiles
    val added = newFiles subtractAsSourceFiles commonFiles
    val removed = oldFiles subtractAsSourceFiles commonFiles

    val common = commonFiles.parallelMap { new ->
        val old = oldFiles.first { old -> old.isSameAs(new) }
        CodeDiffResults.OldAndNewFileInfo(old, new)
    }.toSet()

    return RawFilesDiff(common, added, removed)
}

private infix fun Set<SourceFileInfo>.intersectAsSourceFiles(other: Set<SourceFileInfo>): Set<SourceFileInfo> {
    val set = this.toMutableSet()
    val iterator = set.iterator()
    while (iterator.hasNext()) {
        val item = iterator.next()
        if (other.none { it.isSameAs(item) }) {
            iterator.remove()
        }
    }
    return set
}

private infix fun Set<SourceFileInfo>.subtractAsSourceFiles(other: Set<SourceFileInfo>): Set<SourceFileInfo> {
    val set = this.toMutableSet()
    val iterator = set.iterator()
    while (iterator.hasNext()) {
        val item = iterator.next()
        if (other.any { it.isSameAs(item) }) {
            iterator.remove()
        }
    }
    return set
}

private data class RawFilesDiff(
    val common: Set<CodeDiffResults.OldAndNewFileInfo>,
    val added: Set<SourceFileInfo>,
    val removed: Set<SourceFileInfo>
)
