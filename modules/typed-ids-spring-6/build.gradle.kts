plugins {
    id("org.framefork.build.library-published")
}

dependencies {
    api(project(":typed-ids"))

    // compileOnly: the application brings its own Spring Framework 6.x; Spring Boot 3.0.0 manages Spring Framework 6.0.2, the floor of this module.
    // The Boot auto-config compiles against Boot 3.0.0, but stays dormant for non-Boot consumers: the imports file is only read by Boot.
    compileOnly(libs.spring.core.v602)
    compileOnly(libs.spring.boot.v300)
    compileOnly(libs.spring.boot.autoconfigure.v300)

    testImplementation(project(":typed-ids-testing"))
    testImplementation(libs.spring.core.v602)
    testImplementation(libs.spring.boot.v300)
    testImplementation(libs.spring.boot.autoconfigure.v300)
    testImplementation(libs.spring.boot.test.v300)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds conversion of request parameters, path variables and configuration properties in Spring Framework 6.x and Spring Boot applications"
