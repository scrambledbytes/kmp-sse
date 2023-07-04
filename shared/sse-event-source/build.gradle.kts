plugins {
    id(libs.plugins.kotlin.multiplatform)
    `publishing-conventions`
}

group = groupId()
version = artifactId()

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
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.cash.turbine)
            }
        }
        val jvmMain by getting
        val jvmTest by getting
    }
}
