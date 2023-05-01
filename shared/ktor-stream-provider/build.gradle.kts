plugins {
    id(libs.plugins.kotlin.multiplatform)
}

group = libs.versions.kmp.sse.group.get()
version = libs.versions.kmp.ktor.stream.provider.get()

kotlin {
    jvm()

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

        val jvmMain by getting
    }
}
