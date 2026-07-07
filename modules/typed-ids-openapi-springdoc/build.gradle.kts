plugins {
    id("org.framefork.build.library-published")
    id("org.framefork.build.auto-service")
}

dependencies {
    api(project(":typed-ids"))
    api(project(":typed-ids-openapi-swagger-jakarta"))
    api(libs.springdoc.openapi.starter.common)

    compileOnly(libs.jetbrains.annotations)
    annotationProcessor(libs.springdoc.openapi.spring.configuration.processor)

    testImplementation(project(":typed-ids-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds seamless integration with SpringDoc OpenAPI"
