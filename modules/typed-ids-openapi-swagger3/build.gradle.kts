plugins {
    id("org.framefork.build.library-published")
    id("org.framefork.build.auto-service")
}

dependencies {
    api(project(":typed-ids"))

    // compileOnly: the app's swagger-core line comes from its springdoc (or its own declaration), and a Boot 4 starter pulls this module
    // next to typed-ids-openapi-swagger2, so forcing swagger-core here would put two swagger-core lines on one classpath.
    compileOnly(libs.swagger.v3.core)

    // The Boot auto-config (@AutoConfiguration + @ConfigurationProperties) compiles against these,
    // but stays dormant for non-Boot consumers: the imports file is only read by Boot and the conditions gate the bean.
    compileOnly(libs.spring.boot.v401)
    compileOnly(libs.spring.boot.autoconfigure.v401)
    annotationProcessor(libs.spring.boot.configuration.processor)

    compileOnly(libs.jetbrains.annotations)

    testImplementation(project(":typed-ids-testing"))
    testImplementation(libs.swagger.v3.core)
    testImplementation(libs.spring.boot.v401)
    testImplementation(libs.spring.boot.autoconfigure.v401)
    testImplementation(libs.spring.boot.test.v401)
    // typed-ids-openapi-swagger2 on the same classpath proves that only this module's converter and auto-config act on swagger-core 3
    testImplementation(project(":typed-ids-openapi-swagger2"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds seamless integration with swagger-core 3.x (Jackson 3), and any systems that use it"
