plugins {
    id("framefork.java")
}

dependencies {
    implementation(project(":typed-ids"))

    testImplementation(libs.logback.classic)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
