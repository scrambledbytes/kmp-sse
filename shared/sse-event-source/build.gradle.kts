plugins {
    id(libs.plugins.kotlin.multiplatform)
}

group = "cc.scrambledbytes.sse"
version = "1.0-SNAPSHOT" // TODO extract

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
        val jvmMain by getting {
        }
        val jvmTest by getting
    }
}
