plugins {
    id(libs.plugins.kotlin.multiplatform)
}

group = "cc.scrambledbytes.sse"
version = "1.0-SNAPSHOT" // TODO extract

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
                implementation(libs.kotlinx.coroutines.test)
                implementation("app.cash.turbine:turbine:0.12.3")
            }
        }
        val jvmMain by getting {
        }
        val jvmTest by getting
    }
}
