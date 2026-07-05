plugins {
    id("org.framefork.build.library-published")
    id("org.framefork.build.auto-service")
}

dependencies {
    api(project(":typed-ids"))

    compileOnly(libs.gson)

    compileOnly(libs.jetbrains.annotations)

    testImplementation(project(":typed-ids-testing"))
    testImplementation(testFixtures(project(":typed-ids")))
    testImplementation(libs.gson)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds (de)serialization with Gson"
