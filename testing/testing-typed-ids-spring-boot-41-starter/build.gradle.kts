plugins {
    id("org.framefork.build.library-internal")
}

dependencies {
    implementation(project(":typed-ids-spring-boot-41"))
    implementation(libs.uuidGenerator) // the app supplies the UUID generator; core keeps it compileOnly by design
    implementation(libs.hypersistence.tsid) // the app supplies the BigInt (TSID) generator; core keeps it compileOnly by design

    api(platform("org.springframework.boot:spring-boot-dependencies:4.1.2"))
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-json") // Boot 4 autoconfigures the tools.jackson ObjectMapper from this starter
    api("org.springframework.boot:spring-boot-starter-web") // request parameters and path variables bind to typed ids through typed-ids-spring-7

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test") // Boot 4 moved MockMvc test support into a per-slice module
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.postgresql)
    testImplementation("org.testcontainers:jdbc")
    testImplementation(libs.postgresql)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Spring Boot 4.x's dependency management bumps org.testcontainers:testcontainers to the 2.x line, while our
    // testcontainers-bom holds the postgresql/jdbc modules at 1.x — a core-vs-modules split across the testcontainers
    // 2.0 major boundary. Keep the core artifact on the same 1.x line as the modules.
    testImplementation("org.testcontainers:testcontainers") {
        version { strictly(libs.versions.testcontainers.get()) }
    }
}
