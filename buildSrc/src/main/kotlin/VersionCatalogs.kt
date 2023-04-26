import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

//https://github.com/gradle/gradle/issues/15383
internal val Project.catalog: VersionCatalog
    get() = extensions
        .getByType<VersionCatalogsExtension>()
        .named("libs")

internal fun VersionCatalog.intVersion(
    name: String
): Int =
    findVersion(name).get().displayName.toInt()

internal fun VersionCatalog.version(
    name: String
): String =
    findVersion(name).get().displayName


internal fun Project.pluginId(alias: String): String =
    catalog.findPlugin(alias).get().get().pluginId
