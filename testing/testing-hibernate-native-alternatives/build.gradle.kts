plugins {
    id("framefork.java")
}

dependencies {
    // Need Hibernate for JPA annotations, but not the typed-ids implementations
    implementation(libs.hibernate.orm.v66)
    compileOnly(libs.jetbrains.annotations)

    // Only the testing infrastructure, not the actual typed-ids implementations
    testImplementation(project(":typed-ids-hibernate-63-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
