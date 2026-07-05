plugins {
    id("framefork.java-public")
    id("java-test-fixtures")
}

dependencies {
    api(libs.errorprone.annotations)

    compileOnly(libs.uuidGenerator)
    compileOnly(libs.hypersistence.tsid)

    compileOnly(libs.jetbrains.annotations)

    testImplementation(project(":typed-ids-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds for safer code"
