plugins {
    id("org.framefork.build.library-published")
    id("org.framefork.build.auto-service")
    id("java-test-fixtures")
}

dependencies {
    api(project(":typed-ids"))
    api(libs.hibernate.orm.v61)

    compileOnly("org.jboss:jandex") {
        version {
            require(
                configurations.getByName("runtimeClasspath").resolvedConfiguration
                    .resolvedArtifacts.find { it.moduleVersion.id.toString().contains("org.jboss:jandex") }
                    ?.moduleVersion?.id?.version ?: "unknown"
            )
        }
    }

    compileOnly(libs.jetbrains.annotations)

    // The test fixtures carry the test harness for this module's tests and for every testing/ module that tests it; they are never published.
    testFixturesApi(project(":typed-ids-testing"))
    testFixturesApi(libs.hibernate.orm.testing.v61)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds seamless integration into Hibernate ORMs type system"
