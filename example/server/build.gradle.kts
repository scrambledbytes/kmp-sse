plugins {
    id(libs.plugins.kotlin.jvm)
    id(libs.plugins.ktor.io)
}

application {
    mainClass.set("cc.scrambledbytes.sse.MainKt")
}

dependencies {
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.logback.classic)
}