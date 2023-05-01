plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlin.gradle)

    // WA for https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = libs.versions.jvm.target.get()
}

tasks.compileJava {
    sourceCompatibility = libs.versions.jvm.target.get()
    targetCompatibility = libs.versions.jvm.target.get()
}
