plugins {
    id("framefork.java")
    kotlin("plugin.serialization")
    kotlin("kapt")
}

dependencies {
    implementation(project(":typed-ids"))
    implementation(libs.kotlinx.serialization)

    kapt(project(":typed-ids-index-java-classes-processor"))

    testImplementation(project(":typed-ids-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
