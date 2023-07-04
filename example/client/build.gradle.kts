plugins {
    id(libs.plugins.kotlin.jvm)
    id(libs.plugins.ktor.io)
}

dependencies {
    //implementation(libs.sse.ktor.stream.provider)
    implementation(project(":shared:sse-ktor-stream-provider"))
    implementation(libs.ktor.client.apache5)
}
