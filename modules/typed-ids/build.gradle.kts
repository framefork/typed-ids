plugins {
    id("framefork.java-public")
}

dependencies {
    api(libs.errorprone.annotations)

    compileOnly(libs.uuidGenerator)
    compileOnly(libs.hypersistence.tsid)

    compileOnly(libs.jetbrains.annotations)

    compileOnly(libs.autoService.annotations)
    annotationProcessor(libs.autoService.processor)

    compileOnly(libs.gson)
    compileOnly(libs.kotlinx.serialization)

    testImplementation(project(":typed-ids-testing"))
    testImplementation(libs.gson)
    testImplementation(libs.kotlinx.serialization)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds for safer code"
