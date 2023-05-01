plugins {
    id(libs.plugins.kotlin.jvm)
    id(libs.plugins.ktor.io)
}

dependencies {
    implementation(project(":shared:ktor-stream-provider"))
    implementation(libs.ktor.client.apache5)
}