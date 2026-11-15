plugins {
    id("org.framefork.build.library-published")
}

dependencies {
    api(project(":typed-ids"))
    api(project(":typed-ids-hibernate-63"))
    api(project(":typed-ids-jackson2"))
    api(project(":typed-ids-spring-6"))
    api(project(":typed-ids-openapi-swagger2"))

    // Floors, so a build without the Boot BOM resolves at least these: the higher of what Spring Boot 3.2.0 (the first release of this minor)
    // manages and what the typed-ids modules above declare; the values and their sources are in gradle/libs.versions.toml.
    // The Jackson one also turns the typed-ids-jackson2 module's optional Jackson dependency into a hard runtime dependency.
    api(libs.starter.floor.boot32.hibernate)
    api(libs.starter.floor.boot32.jackson)
    api(libs.starter.floor.boot32.spring)
}

project.description = "TypeIds aggregation starter for Spring Boot 3.2.x"
