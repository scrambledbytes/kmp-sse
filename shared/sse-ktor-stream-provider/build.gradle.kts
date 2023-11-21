plugins {
    id(libs.plugins.kotlin.multiplatform)
    id(libs.plugins.detekt)
    `publishing-conventions`
}

group = groupId()
version = artifactId()
description = "Kotlin multiplatform and Ktor based SSE stream provider"

kotlin {
    jvm()
    iosTargets()


    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":shared:sse-event-source"))
                api(libs.ktor.client.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
