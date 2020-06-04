package dev.sebastiano.codemerge.collectors

import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.sebastiano.codemerge.cli.Logger
import java.io.File
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.mockito.Mockito

internal class KotlinFqnExtractorTest {

    @Test
    internal fun `should use the name of the class matching the file name, if any`() {
        @Language("kotlin") val sources = """
            @file:JvmName("BananaAnnotation")
            package com.example.test
            
            import java.io.Serializable
                        
            data class Banana(val variety: String = "Cavendish")
            
            private fun ignoreThis() = TODO()
            
            internal class Potato: Serializable
        """.trimIndent()

        val fqn = extractFqn(sources, "Banana")
        assertThat(fqn).isEqualTo("com.example.test.Banana")
    }

    @Test
    internal fun `should use the JvmName value annotation, if any is present and there is no class matching the file name`() {
        @Language("kotlin") val sources = """
            @file:JvmName("BananaAnnotation")
            package com.example.test
                        
            fun ignoreThis() = TODO()
        """.trimIndent()

        val fqn = extractFqn(sources, "Banana")
        assertThat(fqn).isEqualTo("com.example.test.BananaAnnotation")
    }

    @Test
    internal fun `should use the JvmName value annotation, if there is a class matching the file name but it's private and there is a file JvmName`() {
        @Language("kotlin") val sources = """
            @file:JvmName("BananaAnnotation")
            package com.example.test
                        
            fun ignoreThis() = TODO()
            
            private data class Banana(val variety: String = "Cavendish")
        """.trimIndent()

        val fqn = extractFqn(sources, "Banana")
        assertThat(fqn).isEqualTo("com.example.test.BananaAnnotation")
    }

    @Test
    internal fun `should use the file name plus Kt, if there is no class matching the file name nor JvmName file annotation`() {
        @Language("kotlin") val sources = """
            package com.example.test
                        
            fun ignoreThis() = TODO()
        """.trimIndent()

        val fqn = extractFqn(sources, "Banana")
        assertThat(fqn).isEqualTo("com.example.test.BananaKt")
    }

    @Test
    internal fun `should use the file name plus Kt, if there is a class matching the file name but it's private and no file JvmName`() {
        @Language("kotlin") val sources = """
            package com.example.test
                        
            fun ignoreThis() = TODO()
            
            private data class Banana(val variety: String = "Cavendish")
        """.trimIndent()

        val fqn = extractFqn(sources, "Banana")
        assertThat(fqn).isEqualTo("com.example.test.BananaKt")
    }

    private fun extractFqn(fileContents: String, fileName: String) = extractFqnFromKotlinSources(
        sources = fileContents,
        file = File(fileName),
        logger = Mockito.mock(Logger::class.java)
    )
}
