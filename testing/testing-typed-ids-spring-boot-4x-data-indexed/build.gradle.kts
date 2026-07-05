plugins {
    id("framefork.java")
}

dependencies {
    implementation(project(":typed-ids-hibernate-70"))

    api(platform("org.springframework.boot:spring-boot-dependencies:4.0.0"))
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    annotationProcessor(project(":typed-ids-index-java-classes-processor"))
    testAnnotationProcessor(project(":typed-ids-index-java-classes-processor"))

    testImplementation(project(":typed-ids-hibernate-70-testing"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Spring Boot 4.0.0 pins org.testcontainers:testcontainers to 2.0.2, while our testcontainers-bom
    // holds the postgresql/mysql/junit-jupiter modules at 1.x — a core-vs-modules split across the
    // testcontainers 2.0 major boundary. Keep the core artifact on the same 1.x line as the modules.
    testImplementation("org.testcontainers:testcontainers") {
        version { strictly(libs.versions.testcontainers.get()) }
    }
}
