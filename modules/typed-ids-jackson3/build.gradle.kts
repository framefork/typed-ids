plugins {
    id("framefork.java-public")
}

dependencies {
    api(project(":typed-ids"))

    compileOnly(libs.autoService.annotations)
    annotationProcessor(libs.autoService.processor)

    compileOnly(libs.jackson3.databind)

    testImplementation(project(":typed-ids-testing"))
    testImplementation(libs.jackson3.databind)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds Jackson 3.x support"
