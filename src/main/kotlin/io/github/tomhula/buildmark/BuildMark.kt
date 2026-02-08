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

        val generateTask = project.tasks.register<GenerateBuildMarkTask>("generateBuildMark") {
            group = "build"
            description = "Generates the build mark object"
            
            outputDirectory.set(extension.outputDirectory)
            targetObjectName.set(extension.targetObjectName)
            targetPackage.set(extension.targetPackage)
            options.set(extension.options)
        }

        project.plugins.withType<KotlinBasePlugin> {
            project.tasks.withType<KotlinCompile> {
                dependsOn(generateTask)
            }
            project.afterEvaluate {
                project.kotlinExtension.sourceSets.filter { it.name in extension.sourceSets.get() }.forEach {
                    it.generatedKotlin.srcDir(extension.outputDirectory)
                }
            }
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
