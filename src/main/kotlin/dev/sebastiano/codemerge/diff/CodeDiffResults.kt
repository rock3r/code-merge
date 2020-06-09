package dev.sebastiano.codemerge.diff

import dev.sebastiano.codemerge.collectors.SourceFileInfo

data class CodeDiffResults(
    val unchanged: Set<SourceFileInfo>,
    val modified: Set<ModifiedFileInfo>,
    val added: Set<SourceFileInfo>,
    val removed: Set<SourceFileInfo>
) {

    data class ModifiedFileInfo(val old: SourceFileInfo, val new: SourceFileInfo)
}
