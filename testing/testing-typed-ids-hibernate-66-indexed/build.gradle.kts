plugins {
    id("framefork.java")
}

dependencies {
    implementation(project(":typed-ids-hibernate-63"))
    implementation(libs.hibernate.orm.v66)
    implementation(libs.hypersistence.utils.hibernate63)

    annotationProcessor(project(":typed-ids-index-java-classes-processor"))
    testAnnotationProcessor(project(":typed-ids-index-java-classes-processor"))

    testImplementation(project(":typed-ids-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
