import nl.littlerobots.vcu.plugin.versionCatalogUpdate

subprojects {
    apply(plugin = "kotlin-conventions")
}

// if plugins are not used in buildSrc,  (e.g., kotlin) they need to be initialized here
plugins {
    alias(libs.plugins.manes)
    alias(libs.plugins.vcu)
    alias(libs.plugins.ktor.io)
    alias(libs.plugins.detekt) apply false

    alias(libs.plugins.nexus.publish)
    `maven-publish`
}

tasks.wrapper {
    gradleVersion = "8.2.1"
}

versionCatalogUpdate {
    sortByKey.set(false)

    keep {
        keepUnusedVersions.set(true)
        keepUnusedLibraries.set(true)
        keepUnusedPlugins.set(true)
    }
}

nexusPublishing {
    this.repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(properties["sonatype.user"].toString())
            password.set(properties["sonatype.password"].toString())
        }
    }
}
