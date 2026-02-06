package io.github.tomhula.buildmark

import com.squareup.kotlinpoet.CodeBlock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class KotlinLiteralValueConverterTest
{
    companion object
    {
        private val scriptHost = BasicJvmScriptingHost()
        private val scriptCompilationConfiguration = ScriptCompilationConfiguration {}
        private val scriptEvaluationConfiguration = ScriptEvaluationConfiguration {}
    }

    private val converter = KotlinLiteralValueConverter()

    @Test
    fun testNull()
    {
        assertValueEqualsEvaluated(null)
    }

    @Test
    fun testInt()
    {
        listOf(Int.MIN_VALUE, -1, 0, 1, Int.MAX_VALUE).forEach {
            assertValueEqualsEvaluated(it)
        }
    }

    @Test
    fun testByte()
    {
        listOf(Byte.MIN_VALUE, (-1).toByte(), 0.toByte(), 1.toByte(), Byte.MAX_VALUE).forEach {
            val evaluated = evaluateValue(it)
            assertEquals(it.toInt(), evaluated)
        }
    }

    @Test
    fun testShort()
    {
        listOf(Short.MIN_VALUE, (-1).toShort(), 0.toShort(), 1.toShort(), Short.MAX_VALUE).forEach {
            val evaluated = evaluateValue(it)
            assertEquals(it.toInt(), evaluated)
        }
    }

    @Test
    fun testLong()
    {
        listOf(Long.MIN_VALUE, -1L, 0L, 1L, Long.MAX_VALUE).forEach {
            assertValueEqualsEvaluated(it)
        }
    }

    @Test
    fun testDouble()
    {
        listOf(
            -1.0, -0.0, 0.0, 1.0,
            Double.MIN_VALUE,
            Double.MAX_VALUE,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NaN
        ).forEach {
            assertValueEqualsEvaluated(it)
        }
    }

    @Test
    fun testBoolean()
    {
        listOf(true, false).forEach {
            assertValueEqualsEvaluated(it)
        }
    }

    @Test
    fun testString()
    {
        listOf(
            "",
            " ",
            "Hello World",
            "String with \"quotes\"",
            "String with \n newline",
            "String with \t tab",
            "String with $ dollar",
            "String with \${expression}",
            "Special characters: !@#$%^&*()_+=-`~[]\\{}|;':\",./<>?"
        ).forEach {
            assertValueEqualsEvaluated(it)
        }
    }

    @Test
    fun testChar()
    {
        listOf(' ', 'a', 'A', '0', '\n', '\t', '\'', '\"', '\\', '$', '\u1234').forEach {
            assertValueEqualsEvaluated(it)
        }
    }

    @Test
    fun testFloat()
    {
        listOf(
            Float.MIN_VALUE,
            -1.0f, -0.0f, 0.0f, 1.0f,
            Float.MAX_VALUE,
            Float.POSITIVE_INFINITY,
            Float.NEGATIVE_INFINITY,
            Float.NaN
        ).forEach {
            assertValueEqualsEvaluated(it)
        }
    }

    @Test
    fun testList()
    {
        assertThrows<IllegalArgumentException> {
            evaluateValue(emptyList<Int>())
        }

        val testLists = listOf(
            listOf(1, 2, 3),
            listOf(null, 1, "mixed", true),
            listOf(listOf(1, 2), listOf(3, 4))
        )
        testLists.forEach {
            assertValueEqualsEvaluated(it)
        }
    }

    @Test
    fun testSet()
    {
        assertThrows<IllegalArgumentException> {
            evaluateValue(emptySet<Int>())
        }

        val testSets = listOf(
            setOf(1, 2, 3),
            setOf(null, 1, "mixed", true),
            setOf(setOf(1, 2), setOf(3, 4))
        )
        testSets.forEach {
            assertValueEqualsEvaluated(it)
        }
    }

    @Test
    fun testMap()
    {
        assertThrows<IllegalArgumentException> {
            evaluateValue(emptyMap<Int, Int>())
        }

        val testMaps = listOf(
            mapOf("one" to 1, "two" to 2),
            mapOf(null to "nullKey", "nullValue" to null, 1 to true),
            mapOf("nested" to mapOf("inner" to 42))
        )
        testMaps.forEach {
            assertValueEqualsEvaluated(it)
        }
    }

    @Test
    fun testPair()
    {
        val testPairs = listOf(
            1 to 2,
            "hello" to null,
            null to 3.14,
            (1 to 2) to (3 to 4)
        )
        testPairs.forEach {
            assertValueEqualsEvaluated(it)
        }
    }

    @Test
    fun testArray()
    {
        val array1 = arrayOf(1, 2, 3)
        assertContentEquals(array1, evaluateValue(array1) as Array<Int>)

        val array2: Array<Any?> = arrayOf(1, "mixed", null, true)
        assertContentEquals(array2, evaluateValue(array2) as Array<Any?>)

        assertThrows<IllegalArgumentException> {
            evaluateValue(emptyArray<Int>())
        }

        val array4 = arrayOf(arrayOf(1, 2), arrayOf(3, 4))
        val evaluated4 = evaluateValue(array4) as Array<Array<Int>>
        assertEquals(array4.size, evaluated4.size)
        for (i in array4.indices)
            assertContentEquals(array4[i], evaluated4[i])

        val array5 = arrayOf<Int?>(1, null, 3)
        assertContentEquals(array5, evaluateValue(array5) as Array<Int?>)
    }

    @Test
    fun testIntArray()
    {
        listOf(
            intArrayOf(),
            intArrayOf(Int.MIN_VALUE, -1, 0, 1, Int.MAX_VALUE)
        ).forEach {
            val evaluated = evaluateValue(it) as IntArray
            assertContentEquals(it, evaluated)
        }
    }

    @Test
    fun testByteArray()
    {
        listOf(
            byteArrayOf(),
            byteArrayOf(Byte.MIN_VALUE, -1, 0, 1, Byte.MAX_VALUE)
        ).forEach {
            val evaluated = evaluateValue(it) as ByteArray
            assertContentEquals(it, evaluated)
        }
    }

    @Test
    fun testShortArray()
    {
        listOf(
            shortArrayOf(),
            shortArrayOf(Short.MIN_VALUE, -1, 0, 1, Short.MAX_VALUE)
        ).forEach {
            val evaluated = evaluateValue(it) as ShortArray
            assertContentEquals(it, evaluated)
        }
    }

    @Test
    fun testLongArray()
    {
        listOf(
            longArrayOf(),
            longArrayOf(Long.MIN_VALUE, -1L, 0L, 1L, Long.MAX_VALUE)
        ).forEach {
            val evaluated = evaluateValue(it) as LongArray
            assertContentEquals(it, evaluated)
        }
    }

    @Test
    fun testFloatArray()
    {
        listOf(
            floatArrayOf(),
            floatArrayOf(Float.MIN_VALUE, -1.0f, 0.0f, Float.MAX_VALUE, Float.NaN)
        ).forEach {
            val evaluated = evaluateValue(it) as FloatArray
            assertContentEquals(it, evaluated)
        }
    }

    @Test
    fun testDoubleArray()
    {
        listOf(
            doubleArrayOf(),
            doubleArrayOf(Double.MIN_VALUE, -1.0, 0.0, Double.MAX_VALUE, Double.NaN)
        ).forEach {
            val evaluated = evaluateValue(it) as DoubleArray
            assertContentEquals(it, evaluated)
        }
    }

    @Test
    fun testBooleanArray()
    {
        listOf(
            booleanArrayOf(),
            booleanArrayOf(true, false, true)
        ).forEach {
            val evaluated = evaluateValue(it) as BooleanArray
            assertContentEquals(it, evaluated)
        }
    }

    @Test
    fun testCharArray()
    {
        listOf(
            charArrayOf(),
            charArrayOf(' ', 'a', '\n', '\'', '\"', '\\', '$')
        ).forEach {
            val evaluated = evaluateValue(it) as CharArray
            assertContentEquals(it, evaluated)
        }
    }

    @Test
    fun testUnsupportedType()
    {
        assertFailsWith<IllegalArgumentException> {
            converter.convert(object {})
        }
    }

    private fun assertValueEqualsEvaluated(value: Any?)
    {
        val valueEvaluatedFromCode = evaluateValue(value)
        assertEquals(value, valueEvaluatedFromCode)
    }

    private fun evaluateValue(value: Any?): Any?
    {
        val valueCode = generateCodeEvaluatingToValue(value)
        return eval(valueCode)
    }

    private fun generateCodeEvaluatingToValue(value: Any?): String
    {
        val valueLiteral = converter.convert(value)
        return if (value != null)
            valueLiteral
        else
        // `null` evaluates to Unit for some reason. However, a nullable variable correctly evaluates to null. 
            CodeBlock.builder()
                .addStatement("val value: Any? = null")
                .addStatement("value")
                .build().toString()
    }

    private fun eval(kotlinCode: String): Any?
    {
        // Add some imports that might be needed
        val finalCode = "import kotlin.math.*\n$kotlinCode"
        val evaluationResult = scriptHost.eval(
            finalCode.toScriptSource(),
            scriptCompilationConfiguration,
            scriptEvaluationConfiguration
        ).valueOrThrow()

        return when (val returnValue = evaluationResult.returnValue)
        {
            is ResultValue.Value -> returnValue.value
            is ResultValue.Unit -> Unit
            is ResultValue.Error -> throw returnValue.error
            is ResultValue.NotEvaluated -> throw IllegalStateException("Script was not evaluated")
        }
    }
}
