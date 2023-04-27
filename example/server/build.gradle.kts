plugins {
    id(libs.plugins.kotlin.jvm)
    id(libs.plugins.ktor.io)
}

application {
    mainClass.set("cc.scrambledbytes.sse.MainKt")
}

java {

    this.targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.logback.classic)

//    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
//    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}