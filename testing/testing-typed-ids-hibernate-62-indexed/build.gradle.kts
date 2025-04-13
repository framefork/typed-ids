plugins {
    id("framefork.java")
}

dependencies {
    implementation(project(":typed-ids-hibernate-62"))
    implementation(libs.hibernate.orm.v62)
    implementation(libs.hypersistence.utils.hibernate62)

    annotationProcessor(project(":typed-ids-index-java-classes-processor"))
    testAnnotationProcessor(project(":typed-ids-index-java-classes-processor"))

    testImplementation(project(":typed-ids-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
