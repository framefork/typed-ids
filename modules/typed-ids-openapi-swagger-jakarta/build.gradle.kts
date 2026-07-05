plugins {
    id("framefork.java-public")
}

dependencies {
    api(project(":typed-ids"))
    api(libs.swagger.v3.core.jakarta)

    // The Boot auto-config folded into this module (@AutoConfiguration + @ConfigurationProperties) compiles against these,
    // but stays dormant for non-Boot consumers: the imports file is only read by Boot and @ConditionalOnClass gates the bean.
    compileOnly(libs.spring.boot.v300)
    compileOnly(libs.spring.boot.autoconfigure.v300)
    annotationProcessor(libs.spring.boot.configuration.processor)

    compileOnly(libs.jetbrains.annotations)

    compileOnly(libs.autoService.annotations)
    annotationProcessor(libs.autoService.processor)

    testImplementation(project(":typed-ids-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds seamless integration with swagger-core-jakarta, and any systems that use it"
