plugins {
    id(libs.plugins.kotlin.multiplatform)
    `publishing-conventions`
}

group = group()
version = artifactId()

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.sse.event.source)
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
