package io.github.tomhula.buildmark

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

interface BuildMarkExtension
{
    val targetPackage: Property<String>
    val targetObjectName: Property<String>
    val sourceSets: SetProperty<String>
    val options: MapProperty<String, Any>
}
