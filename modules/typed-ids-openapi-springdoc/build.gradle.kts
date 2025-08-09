plugins {
    id("framefork.java-public")
}

dependencies {
    api(project(":typed-ids"))
    api(project(":typed-ids-openapi-swagger-jakarta"))
    api(libs.springdoc.openapi.starter.common)

    compileOnly(libs.jetbrains.annotations)

    compileOnly(libs.autoService.annotations)
    annotationProcessor(libs.autoService.processor)
    annotationProcessor(libs.springdoc.openapi.spring.configuration.processor)

    testImplementation(project(":typed-ids-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds seamless integration with SpringDoc OpenAPI"
