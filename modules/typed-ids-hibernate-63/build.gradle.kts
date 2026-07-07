plugins {
    id("org.framefork.build.library-published")
    id("org.framefork.build.auto-service")
    id("java-test-fixtures")
}

dependencies {
    api(project(":typed-ids"))
    api(libs.hibernate.orm.v63)

    compileOnly(libs.jetbrains.annotations)

    testImplementation(project(":typed-ids-hibernate-63-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds seamless integration into Hibernate ORMs type system"
