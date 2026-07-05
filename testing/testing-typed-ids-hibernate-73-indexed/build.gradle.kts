plugins {
    id("framefork.java")
}

dependencies {
    implementation(project(":typed-ids-hibernate-72"))

    annotationProcessor(project(":typed-ids-index-java-classes-processor"))
    testAnnotationProcessor(project(":typed-ids-index-java-classes-processor"))

    testImplementation(project(":typed-ids-hibernate-72-testing"))
    testImplementation(libs.hypersistence.utils.hibernate73)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    constraints {
        implementation(libs.hibernate.orm.v73)
    }
}
