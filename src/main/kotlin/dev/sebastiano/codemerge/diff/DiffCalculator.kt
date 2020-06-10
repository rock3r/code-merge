package dev.sebastiano.codemerge.diff

import dev.sebastiano.codemerge.collectors.SourceFileInfo
import dev.sebastiano.codemerge.collectors.SourceFilesSet
import dev.sebastiano.codemerge.util.parallelMap

suspend fun calculateCodeDiff(referenceFilesSet: SourceFilesSet, comparisonFilesSet: SourceFilesSet) : CodeDiffResults {
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
    val commonFiles = newFiles intersectByFqn oldFiles
    val added = newFiles subtract commonFiles
    val removed = oldFiles subtract commonFiles

    val common = commonFiles.parallelMap { new ->
        CodeDiffResults.OldAndNewFileInfo(oldFiles.first { it.packageName == new.packageName }, new)
    }.toSet()

    return RawFilesDiff(common, added, removed)
}

private infix fun Set<SourceFileInfo>.intersectByFqn(other: Set<SourceFileInfo>): Set<SourceFileInfo> {
    val set = this.toMutableSet()
    val iterator = set.iterator()
    while (iterator.hasNext()) {
        val item = iterator.next()
        if (other.none { item.fullyQualifiedName == it.fullyQualifiedName }) {
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

