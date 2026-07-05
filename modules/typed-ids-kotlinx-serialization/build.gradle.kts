plugins {
    id("framefork.java-public")
}

dependencies {
    api(project(":typed-ids"))

    compileOnly(libs.kotlinx.serialization)

    compileOnly(libs.jetbrains.annotations)
}

project.description = "TypeIds (de)serialization with Kotlin Serialization"
