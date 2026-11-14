plugins {
    id("org.framefork.build.library-internal")
}

dependencies {
    testImplementation(project(":typed-ids-gson"))
    testImplementation(project(":typed-ids-testing"))
    testImplementation(testFixtures(project(":typed-ids")))
    testImplementation(libs.gson.v2150)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
