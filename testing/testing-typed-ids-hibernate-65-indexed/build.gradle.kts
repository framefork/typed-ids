plugins {
    id("framefork.java")
}

dependencies {
    implementation(project(":typed-ids-hibernate-63"))

    annotationProcessor(project(":typed-ids-index-java-classes-processor"))
    testAnnotationProcessor(project(":typed-ids-index-java-classes-processor"))

    testImplementation(project(":typed-ids-hibernate-63-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    constraints {
        implementation(libs.hibernate.orm.v65)
        implementation(libs.hypersistence.utils.hibernate63)
    }
}
