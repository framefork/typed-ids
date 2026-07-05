plugins {
    id("framefork.java-public")
}

dependencies {
    api(project(":typed-ids"))
    api(project(":typed-ids-hibernate-62"))
    api(project(":typed-ids-jackson2"))
    api(libs.jackson.databind.v2150) // floor-pin: the jackson2 module's jackson dep is compileOnly/optional, the starter makes it a hard runtime dep
    api(project(":typed-ids-openapi-swagger-jakarta")) // brings the ModelConverter SPI and its Boot auto-config
}

project.description = "TypeIds aggregation starter for Spring Boot 3.1.x"
