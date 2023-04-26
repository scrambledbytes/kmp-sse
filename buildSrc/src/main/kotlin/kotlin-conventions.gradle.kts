import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

// kotlin compilation
tasks.withType<KotlinCompile<*>>().configureEach {
    kotlinOptions {
        allWarningsAsErrors = false
        languageVersion = catalog.version("kotlin-api")
        apiVersion = catalog.version("kotlin-api")
    }
}

// jvm compilation
tasks.withType<KotlinJvmCompile>().configureEach {
    kotlinOptions {
        jvmTarget = catalog.version("jvm-target")
    }
}
