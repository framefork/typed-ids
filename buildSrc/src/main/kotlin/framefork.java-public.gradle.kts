plugins {
    id("framefork.java")
    `maven-publish`
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenStaging") {
            from(components["java"])

            pom {
                url = "https://github.com/framefork/typed-ids"
                inceptionYear = "2024"
                licenses {
                    license {
                        name = "Apache-2.0"
                        url = "https://spdx.org/licenses/Apache-2.0.html"
                    }
                }
                organization {
                    name = "Framefork"
                    url = "https://github.com/framefork"
                }
                developers {
                    developer {
                        id = "fprochazka"
                        name = "Filip Procházka"
                        url = "https://filip-prochazka.com/"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/framefork/typed-ids.git"
                    developerConnection = "scm:git:ssh://github.com/framefork/typed-ids.git"
                    url = "https://github.com/framefork/typed-ids"
                }
                issueManagement {
                    system = "GitHub"
                    url = "https://github.com/framefork/typed-ids/issues"
                }
            }

            afterEvaluate {
                pom.name = "${project.group}:${project.name}"
                pom.description = project.description
                pom.withXml {
                    val dependenciesNode = (asNode().get("dependencies") as groovy.util.NodeList).first() as groovy.util.Node
                    configurations["compileOnly"].dependencies.forEach { dependency ->
                        dependenciesNode.appendNode("dependency").apply {
                            appendNode("groupId", dependency.group)
                            appendNode("artifactId", dependency.name)
                            appendNode("version", dependency.version)
                            appendNode("scope", "compile")
                            appendNode("optional", "true")
                        }
                    }
                }
            }
        }
    }

    repositories {
        maven {
            url = uri(rootProject.layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

tasks.named("publish") {
    dependsOn(rootProject.tasks.named("cleanAllPublications"))
}
