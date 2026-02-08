package io.github.tomhula.buildmark

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class BuildMarkPlugin : Plugin<Project>
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
                val configuredSourceSetsNames = extension.sourceSets.get()
                val sourceSets = configuredSourceSetsNames.associateWith { kotlinExtension.sourceSets.findByName(it) }
                
                val notFoundSourceSetsNames = sourceSets.filterValues { it == null }.keys
                
                logger.logSourceSetsNotFoundWarning(notFoundSourceSetsNames)
                
                sourceSets.values.filterNotNull().forEach {
                    // TODO: Use it.generatedKotlin once ite is not experimental
                    it.kotlin.srcDir(extension.outputDirectory)
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
    
    private fun Logger.logSourceSetsNotFoundWarning(notFoundSourceSets: Set<String>)
    {
        when (notFoundSourceSets.size)
        {
            1 -> warn("WARNING: BuildMark: Source-set '{}' was not found, ignoring it.", notFoundSourceSets.first())
            else -> warn("WARNING: BuildMark: Source-sets {} were not found, ignoring them.", notFoundSourceSets.toList().joinToStringWithAnd { "'$it'" })
        }
    }

    private fun <T> List<T>.joinToStringWithAnd(formatter: (T) -> String) = when (size)
    {
        0 -> ""
        1 -> formatter(this[0])
        2 -> "${formatter(this[0])} and ${formatter(this[1])}"
        else -> dropLast(1).joinToString(", ") { formatter(it) } + " and " + formatter(last())
    }
}
