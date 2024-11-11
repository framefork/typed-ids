plugins {
    id("base")
    id("idea")
    id("org.jreleaser") version ("1.15.0")
}

repositories {
    mavenCentral()
    mavenLocal()
}

group = "org.framefork"
version = (properties["version"] as String).trim()

allprojects {
    group = rootProject.group
    version = rootProject.version
}

tasks.withType<Wrapper> {
    distributionType = Wrapper.DistributionType.ALL
}

jreleaser {
    strict = true
    signing {
        active = org.jreleaser.model.Active.ALWAYS
        armored = true
        mode = org.jreleaser.model.Signing.Mode.COMMAND
        files = true
        artifacts = true
        checksums = true
    }
    deploy {
        maven {
            pomchecker {
                version = "1.14.0"
                failOnWarning = true
                failOnError = true
            }
            mavenCentral {
                create("sonatype") {
                    active = org.jreleaser.model.Active.ALWAYS
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("build/staging-deploy")
                    applyMavenCentralRules = true
                    sign = true
                    namespace.set("org.framefork")
                    retryDelay = 30
                    maxRetries = 100
                }
            }
        }
    }
    release {
        github {
            enabled = true
            tagName = "v{{projectVersion}}"
            releaseName = "{{tagName}}"
        }
    }
}

tasks.register<Delete>("cleanAllPublications") {
    setDelete(rootProject.layout.buildDirectory.dir("staging-deploy"))
}

allprojects {
    apply(plugin = "project-report")

    this.task("allDependencies", DependencyReportTask::class) {
        evaluationDependsOnChildren()
        this.setRenderer(org.gradle.api.tasks.diagnostics.internal.dependencies.AsciiDependencyReportRenderer().apply {
            outputFile = file(project.layout.buildDirectory.file("reports/dependencies.txt"))
        })
    }
}

gradle.projectsEvaluated {
    // Make sure tests of individual modules are executed sequentially
    val testTasks = subprojects
        .flatMap { it.tasks.withType<Test>() }
        .sortedBy { it.project.path } // Sort to ensure a consistent order
    for (i in 1 until testTasks.size) {
        testTasks[i].mustRunAfter(testTasks[i - 1])
    }
}
