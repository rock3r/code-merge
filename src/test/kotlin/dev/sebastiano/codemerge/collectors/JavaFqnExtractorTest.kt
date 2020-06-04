package dev.sebastiano.codemerge.collectors

import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.sebastiano.codemerge.cli.Logger
import java.io.File
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

@Suppress("ClassNameDiffersFromFileName")
internal class JavaFqnExtractorTest {

    @Test
    internal fun `should use the name of the class matching the file name, if any`() {
        @Language("java") val sources = """
            package com.example.test;
            
            import java.io.Serializable;
                        
            public class Banana {
                public Banana(String variety) {
                    // Yes.
                }                
            }
            
            class Potato implements Serializable {}
        """.trimIndent()

        val fqn = extractFqn(sources, "Banana")
        assertThat(fqn).isEqualTo("com.example.test.Banana")
    }

    @Test
    internal fun `should use the file name plus Java, if there is no class matching the file name`() {
        @Language("java") val sources = """
            package com.example.test;
            
            class Potato implements Serializable {}
        """.trimIndent()

        val fqn = extractFqn(sources, "Banana")
        assertThat(fqn).isEqualTo("com.example.test.BananaJava")
    }

    private fun extractFqn(fileContents: String, fileName: String) = extractFqnFromJavaSources(
        sources = fileContents,
        file = File(fileName),
        logger = mock(Logger::class.java)
    )
}
