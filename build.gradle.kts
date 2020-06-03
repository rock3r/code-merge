import dev.sebastiano.codemerge.build.gradle.buildConfigFile
import dev.sebastiano.codemerge.build.gradle.isCi
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        jcenter()
    }

    configurations.classpath.get()
            .resolutionStrategy.force("com.github.pinterest:ktlint:0.36.0")
}

plugins {
    kotlin("jvm") version "1.3.72"
    id("io.gitlab.arturbosch.detekt") version "1.9.1"
    id("org.jmailen.kotlinter") version "2.3.2"
    id("com.github.ben-manes.versions") version "0.28.0"
}

group = "dev.sebastiano"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

val detektVersion = "1.9.1"
detekt {
    toolVersion = detektVersion
    autoCorrect = !isCi()
    input = files("src/main/java", "src/main/kotlin", "buildSrc/src/main/kotlin")
    config.from(files(buildConfigFile("detekt/detekt.yml")))
    buildUponDefaultConfig = true
}

kotlinter {
    indentSize = 4
    reporters = arrayOf("html", "checkstyle", "plain")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
}
