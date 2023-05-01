plugins {
    id(libs.plugins.kotlin.multiplatform)
}

group = libs.versions.kmp.sse.group.get()
version = libs.versions.kmp.sse.event.source.get()

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.cash.turbine)
            }
        }
        val jvmMain by getting
        val jvmTest by getting
    }
}
