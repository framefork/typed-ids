plugins {
    id("framefork.java")
}

dependencies {
    implementation(project(":typed-ids-spring-31"))
    implementation(libs.uuidGenerator) // the app supplies the UUID generator; core keeps it compileOnly by design
    implementation(libs.hypersistence.tsid) // the app supplies the BigInt (TSID) generator; core keeps it compileOnly by design

    api(platform("org.springframework.boot:spring-boot-dependencies:3.1.12"))
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework:spring-web") // provides Jackson2ObjectMapperBuilder for the ServiceLoader-modules customizer

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.postgresql)
    testImplementation("org.testcontainers:jdbc")
    testImplementation(libs.postgresql)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
