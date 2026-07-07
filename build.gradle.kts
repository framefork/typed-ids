plugins {
    id("base")
    id("idea")
    id("org.barfuin.gradle.taskinfo") version ("3.0.2") // ./gradlew tiTree publish
}

group = "org.framefork"

allprojects {
    group = rootProject.group
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
