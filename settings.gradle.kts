pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings
    val kotlin_serialization_plugin_version: String by settings
    plugins {
        id("com.google.devtools.ksp") version kspVersion
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlin_serialization_plugin_version
    }
    repositories {
        gradlePluginPortal()
        google()
    }
}
rootProject.name = "JustAnInterface"

include(":workload")
include(":test-processor")