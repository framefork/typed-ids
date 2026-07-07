plugins {
    id("base")
    id("idea")
    id("org.barfuin.gradle.taskinfo") version ("3.0.2") // ./gradlew tiTree publish
}

group = "org.framefork"

allprojects {
    group = rootProject.group
}
