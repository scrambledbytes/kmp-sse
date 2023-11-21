
rootProject.name = "kmp-sse"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        mavenLocal()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots")
    }

    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}


include(":shared:sse-event-source")
include(":shared:sse-ktor-stream-provider")

include(":example:server")
include(":example:client")
