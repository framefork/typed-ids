plugins {
    id("framefork.java")
}

dependencies {
    implementation(project(":typed-ids-hibernate-61"))

    annotationProcessor(project(":typed-ids-index-java-classes-processor"))
    testAnnotationProcessor(project(":typed-ids-index-java-classes-processor"))

    testImplementation(project(":typed-ids-hibernate-61-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    constraints {
        implementation(libs.hibernate.orm.v61)
        implementation(libs.hypersistence.utils.hibernate61)
    }
}
