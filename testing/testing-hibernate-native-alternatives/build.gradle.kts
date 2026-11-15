plugins {
    id("org.framefork.build.library-internal")
}

dependencies {
    // Need Hibernate for JPA annotations, but not the typed-ids implementations
    implementation(libs.hibernate.orm.v72)
    compileOnly(libs.jetbrains.annotations)

    // Only the testing infrastructure, not the actual typed-ids implementations
    testImplementation(testFixtures(project(":typed-ids-hibernate-72")))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
