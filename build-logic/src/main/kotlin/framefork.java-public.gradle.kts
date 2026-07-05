plugins {
    id("framefork.java")
    `maven-publish`
}

java {
    withJavadocJar()
    withSourcesJar()
}

project.pluginManager.withPlugin("java-test-fixtures") {
    // Since this code runs only when the plugin is present, it's safe to access the component and configurations created by it.
    val javaComponent = project.components["java"] as AdhocComponentWithVariants
    // Disable publication of the test fixtures API variant
    javaComponent.withVariantsFromConfiguration(project.configurations.named("testFixturesApiElements").get()) {
        skip()
    }
    // Disable publication of the test fixtures runtime variant
    javaComponent.withVariantsFromConfiguration(project.configurations.named("testFixturesRuntimeElements").get()) {
        skip()
    }
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
