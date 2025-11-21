plugins {
    id("framefork.java-public")
}

dependencies {
    api(project(":typed-ids"))

    compileOnly("org.springframework:spring-core:6.0.0")
    compileOnly(libs.jetbrains.annotations)

    compileOnly(libs.autoService.annotations)
    annotationProcessor(libs.autoService.processor)

    testImplementation(project(":typed-ids-testing"))
    testImplementation("org.springframework:spring-core:6.0.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds Spring Framework converters"