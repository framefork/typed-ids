plugins {
    id("base")
    id("idea")
    id("org.barfuin.gradle.taskinfo") version ("3.0.2") // ./gradlew tiTree publish
}

repositories {
    mavenCentral()
    mavenLocal()
}

// the root project has no resolvable configurations, so this generates no lockfile and enforces nothing today;
// it is here so any future resolvable configuration added to the root is locked by default rather than silently unlocked
dependencyLocking {
    lockAllConfigurations()
}

group = "org.framefork"
version = providers.gradleProperty("version").get().trim()

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

    this.tasks.register<DependencyReportTask>("allDependencies") {
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
