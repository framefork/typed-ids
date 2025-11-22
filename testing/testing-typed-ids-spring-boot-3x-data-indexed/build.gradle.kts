plugins {
    id("framefork.java")
}

dependencies {
    implementation(project(":typed-ids-hibernate-63"))

    api(platform("org.springframework.boot:spring-boot-dependencies:3.5.8"))
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    annotationProcessor(project(":typed-ids-index-java-classes-processor"))
    testAnnotationProcessor(project(":typed-ids-index-java-classes-processor"))

    testImplementation(project(":typed-ids-hibernate-63-testing"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
