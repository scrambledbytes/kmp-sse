import nl.littlerobots.vcu.plugin.versionCatalogUpdate

subprojects {
    apply(plugin = "kotlin-conventions")
}

// if plugins are not used in buildSrc,  (e.g., kotlin) they need to be initialized here
plugins {
    alias(libs.plugins.manes)
    alias(libs.plugins.vcu)
}

tasks.wrapper {
    gradleVersion = "8.1"
}

versionCatalogUpdate {
    sortByKey.set(false)

    keep {
        keepUnusedVersions.set(true)
        keepUnusedLibraries.set(true)
        keepUnusedPlugins.set(true)
    }
}
