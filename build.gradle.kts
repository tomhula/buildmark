plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "2.0.0"
}

group = "io.github.tomhula"
version = "1.0.3"

gradlePlugin {
    website = "https://github.com/tomhula/buildmark"
    vcsUrl = "https://github.com/tomhula/buildmark.git"
    plugins {
        create("buildMark") {
            id = "io.github.tomhula.buildmark"
            displayName = "BuildMark"
            description = "Gradle plugin for embedding build information (like project name, version,...) into the code so it can be read at runtime."
            tags = listOf("build", "version", "project", "kotlin")
            implementationClass = "io.github.tomhula.buildmark.BuildMark"
        }
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlinpoet)
    compileOnly(libs.kotlin.gradle.plugin)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.scripting.common)
    testImplementation(libs.kotlin.scripting.jvm)
    testImplementation(libs.kotlin.scripting.jvm.host)
}

tasks.test {
    useJUnitPlatform()
}
