import org.springdoc.openapi.gradle.plugin.OpenApiGeneratorTask

plugins {
    id("framefork.java")
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
    id("org.openapi.generator") version "7.14.0"
}

dependencies {
    implementation(project(":typed-ids"))
    implementation(project(":typed-ids-openapi-springdoc"))

    api(platform("org.springframework.boot:spring-boot-dependencies:3.4.10"))
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")

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
