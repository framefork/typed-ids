plugins {
    id("framefork.java-public")
}

dependencies {
    api(project(":typed-ids"))
    api(project(":typed-ids-hibernate-72"))
    api(project(":typed-ids-jackson3"))
    api(libs.jackson3.databind.v303) // floor-pin: the jackson3 module's jackson dep is compileOnly/optional, the starter makes it a hard runtime dep
    api(project(":typed-ids-openapi-swagger-jakarta")) // brings the ModelConverter SPI and its Boot auto-config
}

project.description = "TypeIds aggregation starter for Spring Boot 4.0.1 - 4.1.x"
