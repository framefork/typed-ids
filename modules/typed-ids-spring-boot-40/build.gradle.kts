plugins {
    id("org.framefork.build.library-published")
}

dependencies {
    api(project(":typed-ids"))
    api(project(":typed-ids-hibernate-72"))
    api(project(":typed-ids-jackson3"))
    api(project(":typed-ids-spring-7"))
    // Both OpenAPI modules, because springdoc switches its Boot 4 line from swagger-core 2.2.x to 3.0 at 3.2.1; each activates only on its own swagger-core line.
    api(project(":typed-ids-openapi-swagger2"))
    api(project(":typed-ids-openapi-swagger3"))

    // Floors, so a build without the Boot BOM resolves at least these: the higher of what Spring Boot 4.0.1 (the first release of this minor)
    // manages and what the typed-ids modules above declare; the values and their sources are in gradle/libs.versions.toml.
    // The Jackson one also turns the typed-ids-jackson3 module's optional Jackson dependency into a hard runtime dependency.
    api(libs.starter.floor.boot40.hibernate)
    api(libs.starter.floor.boot40.jackson)
    api(libs.starter.floor.boot40.spring)
}

project.description = "TypeIds aggregation starter for Spring Boot 4.0.1 - 4.0.x"
