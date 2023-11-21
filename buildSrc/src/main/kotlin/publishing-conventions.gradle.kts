plugins {
    id("maven-publish")
    id("signing")
}

val javadocJar by tasks.creating(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    publications.withType<MavenPublication> {

        artifact(javadocJar)

        pom {
            name.set(project.name)
            description.set(project.description)
            url.set("https://github.com/scrambledbytes/kmp-sse")
            licenses {
                license {
                    name.set("Apache 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
                }

                scm {
                    url.set("https://github.com/scrambledbytes/kmp-sse")
                    connection.set("git://github.com:scrambledbytes/kmp-sse.git")
                    developerConnection.set("ssh:git@github.com:scrambledbytes/kmp-sse.git")
                }

                developers {
                    developer {
                        id.set("stefanthaler")
                        name.set("Stefan Thaler")
                    }
                }
            }
        }
    }
}

signing {
    // credentials in GRADLE_USER_HOME/gradle.properties
    sign(publishing.publications)
}

// TODO https://youtrack.jetbrains.com/issue/KT-46466
val signingTasks = tasks.withType<Sign>()
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(signingTasks)
}
