plugins {
    id(libs.plugins.kotlin.jvm)
    id(libs.plugins.ktor.io)
}

java {

    this.targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":shared:ktor-stream-provider"))
    implementation(libs.ktor.client.apache5)

//    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
//    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}