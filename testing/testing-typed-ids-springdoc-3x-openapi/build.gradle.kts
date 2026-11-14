plugins {
    id("org.framefork.build.library-internal")
    id("org.springframework.boot") version "4.0.7" apply false
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
    id("org.openapi.generator") version "7.17.0"
}

dependencies {
    implementation(project(":typed-ids"))
    implementation(project(":typed-ids-jackson3"))
    implementation(project(":typed-ids-openapi-swagger2"))

    api(platform("org.springframework.boot:spring-boot-dependencies:4.0.7"))
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

    annotationProcessor(project(":typed-ids-index-java-classes-processor"))
    testAnnotationProcessor(project(":typed-ids-index-java-classes-processor"))

    testImplementation(project(":typed-ids-testing"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test") // Boot 4 moved MockMvc test support into a per-slice module
    testImplementation("org.testcontainers:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

openApi {
    outputDir.set(project.layout.buildDirectory.dir("generated/openapi"))
    outputFileName.set("openapi.json")
    // Each springdoc test module runs its forked app on its own port: the forked app outlives forkedSpringBootRun
    // and nothing serializes these tasks across modules, so a shared port lets one module fetch the other's document.
    apiDocsUrl.set("http://localhost:8081/v3/api-docs")
    customBootRun {
        systemProperties.put("server.port", "8081") // a system property, because Application.main doesn't pass its args to Spring
    }
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
    dependsOn(":typed-ids:jar", ":typed-ids-jackson3:jar", ":typed-ids-openapi-swagger2:jar")
}
