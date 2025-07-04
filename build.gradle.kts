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
    gradleVersion = "8.14.2"
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
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(properties["sonatype.user"].toString())
            password.set(properties["sonatype.password"].toString())
        }
    }
}
