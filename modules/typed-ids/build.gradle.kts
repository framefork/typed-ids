plugins {
    id("org.framefork.build.library-published")
    id("org.framefork.build.auto-service")
}

dependencies {
    api(libs.errorprone.annotations)

    compileOnly(libs.uuidGenerator)
    compileOnly(libs.hypersistence.tsid)

    compileOnly(libs.jetbrains.annotations)

    compileOnly(libs.jackson.databind)
    compileOnly(libs.gson)
    compileOnly(libs.kotlinx.serialization)

    testImplementation(project(":typed-ids-testing"))
    testImplementation(libs.jackson.databind)
    testImplementation(libs.gson)
    testImplementation(libs.kotlinx.serialization)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds for safer code"
