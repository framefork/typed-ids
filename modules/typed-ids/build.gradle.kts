plugins {
    id("framefork.java-public")
}

dependencies {
    api(libs.errorprone.annotations)

    compileOnly(libs.uuidGenerator)

    compileOnly(libs.ateoClassindex)
    compileOnly(libs.jetbrains.annotations)

    compileOnly(libs.autoService.annotations)
    annotationProcessor(libs.autoService.processor)

    compileOnly(libs.jackson.databind)

    testImplementation(project(":typed-ids-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds for safer code"
