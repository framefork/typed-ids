plugins {
    id("framefork.java-public")
}

dependencies {
    api(project(":typed-ids"))
    api(libs.swagger.v3.core.jakarta)

    compileOnly(libs.jetbrains.annotations)

    compileOnly(libs.autoService.annotations)
    annotationProcessor(libs.autoService.processor)

    testImplementation(project(":typed-ids-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds seamless integration with swagger-core-jakarta, and any systems that use it"
