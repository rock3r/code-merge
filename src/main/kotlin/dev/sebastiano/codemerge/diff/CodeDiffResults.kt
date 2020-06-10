package dev.sebastiano.codemerge.diff

import dev.sebastiano.codemerge.collectors.SourceFileInfo

data class CodeDiffResults(
    val unchanged: Set<SourceFileInfo>,
    val modified: Set<OldAndNewFileInfo>,
    val added: Set<SourceFileInfo>,
    val removed: Set<SourceFileInfo>
) {

    data class OldAndNewFileInfo(val old: SourceFileInfo, val new: SourceFileInfo)
}
