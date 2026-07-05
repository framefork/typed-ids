plugins {
    id("org.framefork.build.library-published")
    id("java-test-fixtures")
}

dependencies {
    api(libs.errorprone.annotations)
    implementation(libs.slf4j.api)

    compileOnly(libs.uuidGenerator)
    compileOnly(libs.hypersistence.tsid)

    compileOnly(libs.jetbrains.annotations)

    testImplementation(project(":typed-ids-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds for safer code"
