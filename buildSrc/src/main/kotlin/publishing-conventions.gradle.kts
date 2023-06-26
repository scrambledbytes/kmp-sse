plugins {
    id("maven-publish")
    id("signing")
}

publishing {
    publications.withType<MavenPublication> {
        repositories {
            maven {
                name = "sonatype"
                url = uri(publishRepo())
                credentials {
                    username = properties["sonatype.user"].toString()
                    password = properties["sonatype.password"].toString()
                }
            }
        }

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
