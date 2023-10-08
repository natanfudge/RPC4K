plugins {
    alias(libs.plugins.ksp)
}

version = "1.0-SNAPSHOT"

tasks.test {
    useJUnitPlatform()
}


dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":processor"))
    "ksp"(project(":processor"))
    testImplementation(kotlin("test"))
    testImplementation(Testing.Strikt.core)
    testImplementation(libs.okhttp.core)
    testImplementation(libs.ktor)
}
sourceSets {
    main {
        java {
            srcDir(project.file("build/generated/ksp/src/main/kotlin"))
        }
    }
    test {
        java {
            srcDir(project.file("build/generated/ksp/src/test/kotlin"))
        }
    }
}
