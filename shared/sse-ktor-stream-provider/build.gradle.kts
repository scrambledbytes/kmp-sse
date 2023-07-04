plugins {
    id(libs.plugins.kotlin.multiplatform)
    `publishing-conventions`
}

group = groupId()
version = artifactId()

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
