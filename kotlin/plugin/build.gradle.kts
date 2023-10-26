plugins {
    `kotlin-dsl`
    embeddedKotlin("plugin.serialization")
    id("com.gradle.plugin-publish") version "1.1.0"
}

group = "io.github.natanfudge"
version = libs.versions.rpc4k.plugin.get()

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

dependencies {
    implementation("com.github.node-gradle:gradle-node-plugin:7.0.1")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${libs.versions.ksp.get()}")
}


gradlePlugin {
    website = "https://github.com/natanfudge/rpc4k"
    vcsUrl = "https://github.com/natanfudge/rpc4k"
    plugins {
        create("rpc4kPlugin") {
            id = "io.github.natanfudge.rpc4k"
            implementationClass = "io.github.natanfudge.rpc4k.gradle.Rpc4KPlugin"
            tags = listOf("rpc", "annotation processor", "ksp", "rpc4all")
            displayName = "Rpc4k"
            description = "Sets up rpc4k - a framework for interfacing between services in different programming languages"
        }
    }
}


kotlin {
    compilerOptions.freeCompilerArgs.add("-Xcontext-receivers")
}

fun rpc4kRuntimeVersion() = libs.versions.rpc4k.runtime.get()


tasks.withType<ProcessResources> {
    val versionProp = "version" to rpc4kRuntimeVersion()
    inputs.property("version", rpc4kRuntimeVersion())
    from(sourceSets.main.get().resources.srcDirs) {
        include ("rpc4k_version.txt")
        expand(versionProp)
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}