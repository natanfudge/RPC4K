
buildscript {
    // Sets up the plugin with local paths
    System.setProperty("rpc4k.dev", "true")
}
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    id("com.caesarealabs.rpc4k")
}

kotlin {
    jvmToolchain(21)
    compilerOptions.freeCompilerArgs.add("-Xcontext-receivers")
}

rpc4k {
    typescriptDir = rootDir.parentFile.resolve("typescript/runtime/test/generated")
}



version = "1.0-SNAPSHOT"


kotlin {
    jvm()
    // Must be added for KSP to work in common
    wasmJs {
        browser()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.strikt)
                implementation(libs.okhttp.core)
                implementation(libs.ktor.netty)
                implementation(libs.logback)
                implementation ("org.junit.jupiter:junit-jupiter-api:5.8.1")
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}


//TODO: Road to Getting RPC4K to Multiplatform:
// 2.5 Implement serializers for common UUID and Instant
// 2.6 test integration with typescript client
// 2.7 update POC to multiplatform rpc4k
// 2.8 run processor tests again (Caesarea should pass)
// 3. Properly set up publishing for KMP
// 4. Test on non-jvm app