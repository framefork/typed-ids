plugins {
    id("framefork.java-public")
}

dependencies {
    api(project(":typed-ids"))

    compileOnly(libs.jackson.databind.v2141)

    compileOnly(libs.jetbrains.annotations)

    compileOnly(libs.autoService.annotations)
    annotationProcessor(libs.autoService.processor)

    testImplementation(project(":typed-ids-testing"))
    testImplementation(testFixtures(project(":typed-ids")))
    testImplementation(libs.jackson.databind)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds (de)serialization with Jackson 2.x"
