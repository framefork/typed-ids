plugins {
    id("org.framefork.build.library-published")
    id("org.framefork.build.auto-service")
}

dependencies {
    api(project(":typed-ids"))

    // compileOnly: the app's swagger-core line comes from its springdoc (or its own declaration), and a Boot 4 starter pulls this module
    // next to typed-ids-openapi-swagger3, so forcing swagger-core-jakarta here would put two swagger-core lines on one classpath.
    compileOnly(libs.swagger.v3.core.jakarta)

    // The Boot auto-config folded into this module (@AutoConfiguration + @ConfigurationProperties) compiles against these,
    // but stays dormant for non-Boot consumers: the imports file is only read by Boot and the conditions gate the bean.
    compileOnly(libs.spring.boot.v300)
    compileOnly(libs.spring.boot.autoconfigure.v300)
    annotationProcessor(libs.spring.boot.configuration.processor)

    // Only SpringDocUtils, to register typed ids as simple types for @ParameterObject; the registration is skipped without SpringDoc on the classpath.
    // Not transitive: SpringDoc's own swagger-core and Spring Boot dependencies must not reach this module's compile classpath.
    compileOnly(libs.springdoc.openapi.starter.common.v200) {
        isTransitive = false
    }

    compileOnly(libs.jetbrains.annotations)

    testImplementation(project(":typed-ids-testing"))
    testImplementation(libs.swagger.v3.core.jakarta)
    testImplementation(libs.spring.boot.v300)
    testImplementation(libs.spring.boot.autoconfigure.v300)
    testImplementation(libs.spring.boot.test.v300)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds seamless integration with swagger-core 2.x (swagger-core-jakarta, Jackson 2), and any systems that use it"
