package dev.sebastiano.codemerge.collectors

import java.util.Locale
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer.PLAIN_RELATIVE_PATHS
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
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
                PrintingMessageCollector(System.err, PLAIN_RELATIVE_PATHS, false)
            )
        },
        EnvironmentConfigFiles.JVM_CONFIG_FILES
    ).project
}

fun extractFqnFromKotlinSources(sources: String, fileNameWithoutExtension: String): String {
    require(!fileNameWithoutExtension.toLowerCase(Locale.ROOT).endsWith(".kt")) { "The file name must not contain the extension" }

    val ktFile = createKtFile(sources, fileNameWithoutExtension)
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

private fun createKtFile(codeString: String, fileName: String) =
    PsiManager.getInstance(project)
        .findFile(LightVirtualFile(fileName, KotlinFileType.INSTANCE, codeString)) as KtFile
