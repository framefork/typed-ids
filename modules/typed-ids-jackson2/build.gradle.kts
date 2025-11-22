plugins {
    id("framefork.java-public")
}

dependencies {
    api(project(":typed-ids"))

    compileOnly(libs.autoService.annotations)
    annotationProcessor(libs.autoService.processor)

    compileOnly(libs.jackson2.databind)

    testImplementation(project(":typed-ids-testing"))
    testImplementation(libs.jackson2.databind)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds Jackson 2.x support"
