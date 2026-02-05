package io.github.tomhula.buildmark

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

internal abstract class GenerateBuildMarkTask : DefaultTask()
{
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty
    @get:Input
    abstract val targetObjectName: Property<String>
    @get:Input
    abstract val targetPackage: Property<String>
    @get:Input
    abstract val options: MapProperty<String, Any>

    private val converter = KotlinLiteralValueConverter()

    @TaskAction
    fun generate()
    {
        // KotlinPoet is not used because it currently does not support type inference

        val outputDirectory = outputDirectory.get().asFile
        val targetObjectName = targetObjectName.get()
        val targetPackage = targetPackage.get()
        val options = options.get()
        
        val properties = options.map { option ->
            val variableDeclaration = if (option.value::class in constableTypes)
                "const val" 
            else
                "val"
                
            converter.convert(option.value).let { value -> "$variableDeclaration ${option.key} = $value" }
        }
        
        val code = buildString { 
            if (targetPackage.isNotBlank())
            {
                appendLine("package $targetPackage")
                appendLine()
            }
                
            appendLine("object $targetObjectName")
            appendLine("{")
            properties.forEach { appendLine("    $it") }
            appendLine("}")
        }
        
        outputDirectory.deleteRecursively()
        
        val packageAsPath = targetPackage.replace(".", "/")
        val outputFile = outputDirectory.resolve(packageAsPath).resolve("$targetObjectName.kt")
        outputFile.parentFile.mkdirs()
        outputFile.writeText(code)
    }
    
    companion object
    {
        /** All types using which you can declare a compile-time constant with the 'const' keyword. */
        private val constableTypes = listOf(
            String::class,
            Char::class,
            Byte::class,
            Short::class,
            Int::class,
            Long::class,
            Float::class,
            Double::class,
            Boolean::class,
        )
    }
}
