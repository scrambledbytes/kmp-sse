plugins {
    id(libs.plugins.kotlin.multiplatform)
}

group = "cc.scrambledbytes"
version = "1.0-SNAPSHOT"

kotlin {
    jvm {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of("17"))
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
    }
}
