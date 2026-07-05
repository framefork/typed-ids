plugins {
    id("org.framefork.build.library-published")
    id("org.framefork.build.auto-service")
}

dependencies {
    api(project(":typed-ids"))

    compileOnly(libs.jackson3.databind)

    compileOnly(libs.jetbrains.annotations)

    testImplementation(project(":typed-ids-testing"))
    testImplementation(testFixtures(project(":typed-ids")))
    testImplementation(libs.jackson3.databind)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds (de)serialization with Jackson 3.x"
