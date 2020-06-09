package dev.sebastiano.codemerge.diff

import dev.sebastiano.codemerge.collectors.SourceFileInfo

data class CodeDiffResults(
    val unchanged: Set<SourceFileInfo>,
    val modified: Set<SourceFileInfo>,
    val added: Set<SourceFileInfo>,
    val removed: Set<SourceFileInfo>
)
