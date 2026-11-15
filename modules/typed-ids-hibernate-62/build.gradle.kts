plugins {
    id("org.framefork.build.library-published")
    id("org.framefork.build.auto-service")
    id("java-test-fixtures")
}

dependencies {
    api(project(":typed-ids"))
    api(libs.hibernate.orm.v62)

    compileOnly(libs.jetbrains.annotations)

    // The test fixtures carry the test harness for this module's tests and for every testing/ module that tests it; they are never published.
    testFixturesApi(project(":typed-ids-testing"))
    testFixturesApi(libs.hibernate.orm.testing.v62)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds seamless integration into Hibernate ORMs type system"
