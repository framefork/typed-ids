plugins {
    id("org.framefork.build.library-published")
    id("org.framefork.build.auto-service")
}

dependencies {
    api(project(":typed-ids"))

    compileOnly(libs.jackson.databind.v2141)

    compileOnly(libs.jetbrains.annotations)

    testImplementation(project(":typed-ids-testing"))
    testImplementation(testFixtures(project(":typed-ids")))
    testImplementation(libs.jackson.databind)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds (de)serialization with Jackson 2.x"
