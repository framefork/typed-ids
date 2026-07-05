plugins {
    id("framefork.java")
}

dependencies {
    implementation(project(":typed-ids"))
    implementation(project(":typed-ids-jackson3"))

    api(platform("org.springframework.boot:spring-boot-dependencies:4.0.0"))
    api("org.springframework.boot:spring-boot-starter-json")

    annotationProcessor(project(":typed-ids-index-java-classes-processor"))
    testAnnotationProcessor(project(":typed-ids-index-java-classes-processor"))

    testImplementation(project(":typed-ids-testing"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
