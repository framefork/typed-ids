// Unpublished (library-internal) until Hibernate ORM 8.0 is GA: it compiles and tests against the newest 8.0 pre-release, and a published POM must not
// depend on a CR or on the jakarta.persistence-api milestone it brings. Once 8.0 is GA, move the catalog to the GA version and switch to library-published.
plugins {
    id("org.framefork.build.library-internal")
    id("org.framefork.build.auto-service")
    id("java-test-fixtures")
}

dependencies {
    api(project(":typed-ids"))
    api(libs.hibernate.orm.v80)
    api(libs.hibernate.orm.types.v80)
    compileOnly(libs.hibernate.models.v80) // this is really a runtime dependency, but in runtime the version from hibernate is provided

    compileOnly(libs.jetbrains.annotations)

    // The test fixtures carry the test harness for this module's tests and for every testing/ module that tests it; they are never published.
    testFixturesApi(project(":typed-ids-testing"))
    testFixturesApi(libs.hibernate.orm.testing.v80)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds seamless integration into Hibernate ORMs type system"
