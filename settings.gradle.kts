
rootProject.name = "kmp-sse"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        mavenLocal()
    }

    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

include(":shared:sse-event-source")
include(":shared:ktor-stream-provider")

include(":example:server")
include(":example:client")