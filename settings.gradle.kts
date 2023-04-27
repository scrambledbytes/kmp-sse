
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

include(":shared:client-core")
include(":shared:ktor-provider")

include(":example:server")
include(":example:client")