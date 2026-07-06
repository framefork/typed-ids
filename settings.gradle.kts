pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.framefork.build") version "0.2.0"
}

framefork {
    minJavaVersion = 17
    jdkVersion = 25
    jspecifyMode = true
}

rootProject.name = "typed-ids"
