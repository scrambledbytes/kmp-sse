
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion.Companion.fromVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

// kotlin compilation
tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        allWarningsAsErrors = false
        languageVersion.set(fromVersion(catalog.version("kotlin-api")))
        apiVersion.set(fromVersion(catalog.version("kotlin-api")))
    }
}

tasks.withType<JavaCompile>().configureEach {
    targetCompatibility = catalog.version("jvm-target")
}

// jvm compilation
tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
