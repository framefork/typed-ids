plugins {
    id("org.framefork.build.library-internal")
}

dependencies {
    implementation(project(":typed-ids"))

    testImplementation(libs.uuidGenerator)
    testImplementation(libs.logback.classic)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
