package io.github.tomhula.buildmark

import com.squareup.kotlinpoet.CodeBlock
import kotlin.collections.joinToString
import kotlin.collections.map
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * Converts a value of a supported type to a Kotlin code literal of that value.
 * @see convert
 */
internal class KotlinLiteralValueConverter
{
    private val convertors = mutableMapOf<(KClass<*>) -> Boolean, (Any) -> CodeBlock>()

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> registerConvertor(predicate: (KClass<T>) -> Boolean, convertor: (T) -> CodeBlock) =
        convertors.put(
            predicate as (KClass<*>) -> Boolean,
            convertor as (Any) -> CodeBlock
        )

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> registerConvertor(clazz: KClass<T>, convertor: (T) -> CodeBlock) =
        registerConvertor(
            predicate = { it.isSubclassOf(clazz) },
            convertor = convertor as (Any) -> CodeBlock
        )

    /**
     * Converts [value] of supported type to a Kotlin code that evaluates back to that value.
     * Example:
     * ```kotlin
     * convert("Hello") // "Hello"
     * convert(2.8f) // 2.8f
     * val list = listOf("Hello", "World")
     * convert(list) // listOf("Hello", "World")
     * ```
     * @throws IllegalArgumentException for [value] of unsupported type.
     */
    fun convert(value: Any?): String = convertToCodeBlock(value).toString()

    private fun convertToCodeBlock(value: Any?): CodeBlock
    {
        if (value == null)
            return CodeBlock.of("null")

        val type = value::class

        val converter = convertors.entries.find { it.key(type) }?.value

        return converter?.invoke(value)
            ?: throw IllegalArgumentException("Unsupported type: ${value::class}")
    }

    init
    {
        registerNumberConverters()
        registerCollectionConverters()
        registerArrayConverters()

        registerConvertor(String::class) { CodeBlock.of("%S", it) }
        registerConvertor(Char::class) { CodeBlock.of("'%L'", it) }
        registerConvertor(Pair::class) { CodeBlock.of("%L to %L", convert(it.first), convert(it.second)) }
    }

    private fun registerCollectionConverters()
    {
        registerConvertor(List::class) { list ->
            CodeBlock.of("listOf(%L)", list.toConvertedArgumentList())
        }
        registerConvertor(Set::class) { set ->
            CodeBlock.of("setOf(%L)", set.toConvertedArgumentList())
        }
        registerConvertor(Map::class) { map ->
            val entries = map.entries.map { "${convert(it.key)} to ${convert(it.value)}" }
            CodeBlock.of("mapOf(%L)", entries.joinToString(", "))
        }
    }

    private fun registerNumberConverters()
    {
        setOf(
            Int::class,
            Double::class,
            Boolean::class
        ).forEach { literalType -> registerConvertor(literalType) { CodeBlock.of("%L", it) } }

        // Cast to Int and use Int's converter
        setOf(
            Byte::class,
            Short::class
        ).forEach { type -> registerConvertor(type) { CodeBlock.of(convert((it as Number).toInt())) } }

        // Unsigned types are not serializable by Gradle. Resulting in the following error if used: 
        // "Cannot fingerprint input property 'options': value '{UINT=1000}' cannot be serialized."
        /*setOf(
            UInt::class,
            UShort::class,
            ULong::class,
            UByte::class
        ).forEach { uType -> registerConvertor(uType) { CodeBlock.of("%Lu", it) } }*/

        registerConvertor(Float::class) { CodeBlock.of("%Lf", it) }
        registerConvertor(Long::class) { CodeBlock.of("%LL", it) }
    }

    private fun registerArrayConverters()
    {
        registerConvertor<Array<*>>({ it.qualifiedName == "kotlin.Array" || it.java == Array::class.java }) { array ->
            val elements = array.map { convert(it) }
            CodeBlock.of("arrayOf(%L)", elements.joinToString(", "))
        }

        // Primitive array converters
        registerConvertor(IntArray::class) { array ->
            val elements = array.joinToString(", ", transform = ::convert)
            CodeBlock.of("intArrayOf(%L)", elements)
        }
        registerConvertor(ByteArray::class) { array ->
            val elements = array.joinToString(", ", transform = ::convert)
            CodeBlock.of("byteArrayOf(%L)", elements)
        }
        registerConvertor(ShortArray::class) { array ->
            val elements = array.joinToString(", ", transform = ::convert)
            CodeBlock.of("shortArrayOf(%L)", elements)
        }
        registerConvertor(LongArray::class) { array ->
            val elements = array.joinToString(", ", transform = ::convert)
            CodeBlock.of("longArrayOf(%L)", elements)
        }
        registerConvertor(FloatArray::class) { array ->
            val elements = array.joinToString(", ", transform = ::convert)
            CodeBlock.of("floatArrayOf(%L)", elements)
        }
        registerConvertor(DoubleArray::class) { array ->
            val elements = array.joinToString(", ", transform = ::convert)
            CodeBlock.of("doubleArrayOf(%L)", elements)
        }
        registerConvertor(BooleanArray::class) { array ->
            val elements = array.joinToString(", ", transform = ::convert)
            CodeBlock.of("booleanArrayOf(%L)", elements)
        }
        registerConvertor(CharArray::class) { array ->
            val elements = array.joinToString(", ") { "'$it'" }
            CodeBlock.of("charArrayOf(%L)", elements)
        }
    }

    /** Converts a collection of a comma seperated string of a result of [convert] on each element. */
    private fun Iterable<*>.toConvertedArgumentList() = joinToString(", ", transform = ::convert)
}
