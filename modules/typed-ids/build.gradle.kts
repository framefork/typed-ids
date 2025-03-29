plugins {
    id("framefork.java-public")
}

dependencies {
    api(libs.uuidGenerator)
    api(libs.errorprone.annotations)

    compileOnly(libs.ateoClassindex)

    compileOnly(libs.jetbrains.annotations)

    compileOnly(libs.autoService.annotations)
    annotationProcessor(libs.autoService.processor)

    compileOnly(libs.jackson.databind)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds for safer code"
