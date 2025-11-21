// Unpublished (library-internal) until Spring Boot 4.2.0 is GA. The floors below come from 4.2.0-M1, the first 4.2 pre-release,
// which is fine while nothing is published; re-read them from 4.2.0 GA before switching to library-published.
plugins {
    id("org.framefork.build.library-internal")
}

dependencies {
    api(project(":typed-ids"))
    api(project(":typed-ids-hibernate-72"))
    api(project(":typed-ids-jackson3"))
    api(project(":typed-ids-spring-7"))
    // Both OpenAPI modules, because springdoc switches its Boot 4 line from swagger-core 2.2.x to 3.0 at 3.2.1; each activates only on its own swagger-core line.
    api(project(":typed-ids-openapi-swagger2"))
    api(project(":typed-ids-openapi-swagger3"))

    // Floors, so a build without the Boot BOM resolves at least these: the higher of what Spring Boot 4.2.0-M1 (the first pre-release of this minor)
    // manages and what the typed-ids modules above declare; the values and their sources are in gradle/libs.versions.toml.
    // The Jackson one also turns the typed-ids-jackson3 module's optional Jackson dependency into a hard runtime dependency.
    api(libs.starter.floor.boot42.hibernate)
    api(libs.starter.floor.boot42.jackson)
    api(libs.starter.floor.boot42.spring)
}

project.description = "TypeIds aggregation starter for Spring Boot 4.2.x"
