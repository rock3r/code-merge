package dev.sebastiano.codemerge.diff

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import dev.sebastiano.codemerge.collectors.SourceFileInfo
import java.lang.UnsupportedOperationException

fun calculateCodeDiff(referenceFilesSet: Set<SourceFileInfo>, comparisonFilesSet: Set<SourceFileInfo>) : CodeDiffResults {
    val modified = mutableSetOf<CodeDiffResults.ModifiedFileInfo>()
    val added = mutableSetOf<SourceFileInfo>()
    val removed = mutableSetOf<SourceFileInfo>()

    val allChanged = mutableListOf<SourceFileInfo>()

    val referenceFiles = referenceFilesSet.sortedBy { it.fullyQualifiedName }
    val comparisonFiles = comparisonFilesSet.sortedBy { it.fullyQualifiedName }

    DiffUtil.calculateDiff(object : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return referenceFiles[oldItemPosition].fullyQualifiedName == comparisonFiles[newItemPosition].fullyQualifiedName
        }

        override fun getOldListSize(): Int {
            return referenceFiles.size
        }

        override fun getNewListSize(): Int {
            return comparisonFiles.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return referenceFiles[oldItemPosition].sha1.contentEquals(comparisonFiles[newItemPosition].sha1)
        }
    }, false).dispatchUpdatesTo(object : ListUpdateCallback {
        override fun onChanged(position: Int, count: Int, payload: Any?) {
            for (i in position until position + count) {
                modified += CodeDiffResults.ModifiedFileInfo(referenceFiles[i], comparisonFiles[i])
                allChanged += referenceFiles[i]
            }
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            throw UnsupportedOperationException("Move detection should be disabled, it's irrelevant")
        }

        override fun onInserted(position: Int, count: Int) {
            for (i in position until position + count) {
                added += comparisonFiles[i]
            }
        }

        override fun onRemoved(position: Int, count: Int) {
            for (i in position until position + count) {
                removed += referenceFiles[i]
                allChanged += referenceFiles[i]
            }
        }
    })

    val unchanged = referenceFilesSet.filterNot { allChanged.contains(it) }.toSet()

    return CodeDiffResults(unchanged, modified, added, removed)
}
