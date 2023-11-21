import org.gradle.api.Project

fun Project.isRelease(): Boolean =
    findProperty("isRelease")?.toString().toBoolean()

fun Project.groupId(): String =
    strProp("group")

fun Project.artifactId(): String =
    artifactId(strProp("${project.name}.artifactId"))


internal fun Project.strProp(name: String): String {
    val p = findProperty(name)
    requireNotNull(p) { "`$name` not defined in `gradle.properties`." }
    return p.toString()
}

private fun Project.artifactId(id: String) =
    if (isRelease()) id else "$id-SNAPSHOT"
