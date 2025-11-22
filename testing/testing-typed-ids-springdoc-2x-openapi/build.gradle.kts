plugins {
    id("framefork.java")
    id("org.springframework.boot") version "3.5.8" apply false
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
    id("org.openapi.generator") version "7.17.0"
}

dependencies {
    implementation(project(":typed-ids"))
    implementation(project(":typed-ids-openapi-springdoc"))

    api(platform("org.springframework.boot:spring-boot-dependencies:3.5.8"))
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")

    annotationProcessor(project(":typed-ids-index-java-classes-processor"))
    testAnnotationProcessor(project(":typed-ids-index-java-classes-processor"))

    testImplementation(project(":typed-ids-testing"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

openApi {
    outputDir.set(project.layout.buildDirectory.dir("generated/openapi"))
    outputFileName.set("openapi.json")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateTypeScriptClient") {
    dependsOn("generateOpenApiDocs")

    generatorName.set("typescript-fetch")
    inputSpec.set(project.layout.buildDirectory.file("generated/openapi/openapi.json").get().asFile.absolutePath)
    outputDir.set(project.layout.buildDirectory.dir("generated/typescript-client").get().asFile.absolutePath)
    apiPackage.set("org.example.api")
    modelPackage.set("org.example.model")
}

tasks.named("build") {
    dependsOn("generateOpenApiDocs", "generateTypeScriptClient")
}

tasks.named("forkedSpringBootRun") {
    dependsOn(":typed-ids-openapi-springdoc:jar", ":typed-ids-openapi-swagger-jakarta:jar")
}
