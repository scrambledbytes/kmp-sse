plugins {
    id(libs.plugins.kotlin.multiplatform)
    id(libs.plugins.detekt)
    `publishing-conventions`
}

group = groupId()
version = artifactId()
description = "Kotlin multiplatform SSE event source"

kotlin {
    allSupportedTargets()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.cash.turbine)
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(libs.junit)
            }
        }
    }
}

detekt {
    source.setFrom(
        "src/commonMain/kotlin",
        "src/jvmMain/kotlin",
    )
    allRules = true
    config.setFrom("detekt.yml")
}
