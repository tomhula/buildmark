package io.github.tomhula.buildmark

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class BuildMark : Plugin<Project>
{
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    override fun apply(project: Project)
    {
        val extension = project.extensions.create<BuildMarkExtension>("buildMark")
        extension.configureConventions(project)

        project.plugins.withType<KotlinBasePlugin> {
            project.kotlinExtension.sourceSets.matching { it.name in extension.sourceSets.get() }.configureEach {
                kotlin.srcDir(extension.outputDirectory)
            }
        }

        val generateTask = project.tasks.register<GenerateBuildMarkTask>("generateBuildMark") {
            group = "build"
            description = "Generates the build mark object"
            this.outputDirectory.set(extension.outputDirectory)
            targetObjectName.set(extension.targetObjectName)
            targetPackage.set(extension.targetPackage)
            options.set(extension.options)
        }

        project.tasks.withType<KotlinCompile> {
            dependsOn(generateTask)
        }
        
        /* This is so that the sources are available after Gradle sync,
         * so you can reference the generated code and get IDE support before building. */
        project.afterEvaluate { 
            generateTask.get().generate()
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    private fun BuildMarkExtension.configureConventions(project: Project)
    {
        outputDirectory.convention(project.layout.buildDirectory.dir("generated/buildmark/"))
        targetPackage.convention("")
        targetObjectName.convention("BuildMark")
        options.convention(mapOf("VERSION" to project.version.toString()))
        sourceSets.convention(setOf("main", "commonMain"))
    }
}
