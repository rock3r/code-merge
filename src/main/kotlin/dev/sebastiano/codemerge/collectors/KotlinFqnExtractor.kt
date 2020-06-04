package dev.sebastiano.codemerge.collectors

import dev.sebastiano.codemerge.cli.Logger
import java.io.File
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.psiUtil.getCallNameExpression
import org.jetbrains.kotlin.psi.psiUtil.isPrivate

private val project by lazy {
    KotlinCoreEnvironment.createForProduction(
        Disposer.newDisposable(),
        CompilerConfiguration().apply {
            put(
                CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
                MessageCollector.NONE
            )
        },
        EnvironmentConfigFiles.JVM_CONFIG_FILES
    ).project
}

fun extractFqnFromKotlinSources(sources: String, file: File, logger: Logger): String? {
    val fileNameWithoutExtension = file.nameWithoutExtension
    val ktFile = createKtFile(sources, file, logger) ?: return null

    val primaryClass = ktFile.declarations.filterIsInstance<KtClass>()
        .find { it.name == fileNameWithoutExtension && !it.isPrivate() }
    if (primaryClass?.fqName != null) return primaryClass.fqName!!.asString()

    val jvmName = ktFile.fileAnnotationList?.children?.filterIsInstance(KtAnnotationEntry::class.java)
        ?.find { it.getCallNameExpression()?.text == "JvmName" }
    val jvmNameValue = jvmName?.children?.first { it is KtValueArgumentList }
        ?.children?.last()?.text?.trim('"')
    val className = jvmNameValue ?: "${fileNameWithoutExtension}Kt"

    return "${ktFile.packageFqName.asString()}.$className"
}

private fun createKtFile(codeString: String, file: File, logger: Logger) = try {
    PsiManager.getInstance(project)
        .findFile(LightVirtualFile(file.nameWithoutExtension, KotlinFileType.INSTANCE, codeString)) as KtFile
} catch (e: ClassCastException) {
    logger.w("Unable to parse Kotlin sources from file $file")
    null
}
