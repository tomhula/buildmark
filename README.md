# BuildMark Gradle Plugin
![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/io.github.tomhula.buildmark)
![GitHub License](https://img.shields.io/github/license/tomhula/buildmark)
![GitHub branch check runs](https://img.shields.io/github/check-runs/tomhula/buildmark/main)
![GitHub Issues or Pull Requests](https://img.shields.io/github/issues/tomhula/buildmark)

A Gradle plugin that generates a Kotlin object with build information at compile time. This allows you to access build-time properties in your application at runtime.

## Notice

Consider using a more customisable and well-maintained [BuildKonfig](https://github.com/yshrsmz/BuildKonfig).
At the time of creating BuildMark I didn't know it existed.
However, BuildMark has few advantages at the time of writing:
- It supports more types, see [Supported Types](#supported-types)
- It supports Kotlin/JVM and such, not just Kotlin/Multiplatform

## Installation and usage

```kotlin
plugins {
    id("io.github.tomhula.buildmark") version "2.0.0"
}

buildMark {
    // Package for the generated object (default: root package)
    targetPackage.set("com.example.app")
    // Name of the generated object (default: "BuildMark")
    targetObjectName.set("BuildMark")
    // Where to output the code (default: build/generated/buildmark)
    outputDirectory.set(layout.buildDirectory.dir("generated/buildmark"))
    // The properties to include in the generated object
    options.apply {
        put("VERSION", project.version)
        put("PROJECT_NAME", project.name)
        put("AUTHORS", listOf("author1", "author2"))
        put("BUILD_TIME", System.currentTimeMillis())
        put("GIT_COMMIT", "abc123") // You could use a Git plugin to get the actual commit
        put("DEBUG", true)
    }
    // Which Kotlin source-sets to add BuildMark to
    // By default, it's added to commonMain/main source-sets
    sourceSets.set("jvmMain")
}
```

Once applied and configured, you can access the defined options in your code. (in the configured source-sets)

```kotlin
// This package was configured, default is the root package (no package)
import com.example.app.BuildMark

fun main()
{
    // AppBuildInfo name was configured, default is BuildMark
    println("Starting version ${BuildMark.VERSION} of ${BuildMark.PROJECT_NAME}")

    println("Authors:")
    BuildMark.AUTHORS.forEach {
        println(it)
    } 
    
    if (BuildMark.DEBUG)
        logger.setLevel(Level.DEBUG)
}
```

## Supported Types
Int, Boolean, Byte, Short, Float, Double, Long, Char, String, Pair,
List, Set, Map,
Array, IntArray, ByteArray, ShortArray, LongArray, FloatArray, DoubleArray, BooleanArray, CharArray

## Advantages over alternatives

An alternative way to pass information to the runtime using Gradle is with command line arguments.
However, this only works when running from Gradle.

Another advantage is Kotlin multiplatform projects.
If you apply this plugin on the shared module in the common source-set, you get access to the same information all modules, which might, for example, be a JVM backend and a JavaScript frontend.

## Contributing

Contributions are welcome! Feel free to submit issues or Pull Requests.

## License

[MIT](LICENSE) © Tomáš Hůla
