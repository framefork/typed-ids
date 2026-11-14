plugins {
    id("org.framefork.build.library-published")
}

dependencies {
    api(project(":typed-ids"))
    api(project(":typed-ids-hibernate-72"))
    api(project(":typed-ids-jackson3"))
    api(libs.jackson3.databind.v303) // floor-pin: the jackson3 module's jackson dep is compileOnly/optional, the starter makes it a hard runtime dep
    // Both OpenAPI modules, because springdoc switches its Boot 4 line from swagger-core 2.2.x to 3.0 at 3.2.1; each activates only on its own swagger-core line.
    api(project(":typed-ids-openapi-swagger2"))
    api(project(":typed-ids-openapi-swagger3"))
}

project.description = "TypeIds aggregation starter for Spring Boot 4.0.1 - 4.1.x"
