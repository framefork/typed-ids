pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.framefork.build") version "0.4.0"
}

framefork {
    minJavaVersion = 17
    jdkVersion = 25
    jspecifyMode = true
    dependencyLocking = true
}

rootProject.name = "typed-ids"
