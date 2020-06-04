package dev.sebastiano.codemerge.collectors

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

@Suppress("ClassNameDiffersFromFileName")
internal class JavaFqnExtractorTest {

    @Test
    internal fun `should throw IllegalArgumentException if the file name contains the java extension`() {
        assertThat { extractFqnFromJavaSources("anything goes here", "Banana.java") }.isFailure()
            .isInstanceOf(IllegalArgumentException::class)
    }

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

        val fqn = extractFqnFromJavaSources(sources, "Banana")
        assertThat(fqn).isEqualTo("com.example.test.Banana")
    }

    @Test
    internal fun `should use the file name plus Java, if there is no class matching the file name`() {
        @Language("java") val sources = """
            package com.example.test;
            
            class Potato implements Serializable {}
        """.trimIndent()

        val fqn = extractFqnFromJavaSources(sources, "Banana")
        assertThat(fqn).isEqualTo("com.example.test.BananaJava")
    }
}
