plugins {
    id("org.framefork.build.library-published")
}

dependencies {
    api(project(":typed-ids"))

    // compileOnly: the application brings its own Spring Framework 7.x; Spring Boot 4.0.1 manages Spring Framework 7.0.2, the floor of this module.
    // The Boot auto-config compiles against Boot 4.0.1, but stays dormant for non-Boot consumers: the imports file is only read by Boot.
    compileOnly(libs.spring.core.v702)
    compileOnly(libs.spring.boot.v401)
    compileOnly(libs.spring.boot.autoconfigure.v401)

    testImplementation(project(":typed-ids-testing"))
    testImplementation(libs.spring.core.v702)
    testImplementation(libs.spring.boot.v401)
    testImplementation(libs.spring.boot.autoconfigure.v401)
    testImplementation(libs.spring.boot.test.v401)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds conversion of request parameters, path variables and configuration properties in Spring Framework 7.x and Spring Boot applications"
