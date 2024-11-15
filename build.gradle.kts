plugins {
    id("base")
    id("idea")
    id("org.barfuin.gradle.taskinfo") version ("2.2.0") // ./gradlew tiTree publish
    id("be.vbgn.ci-detect") version ("0.5.0")
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

tasks.register<Delete>("cleanAllPublications") {
    outputs.upToDateWhen { false }
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
