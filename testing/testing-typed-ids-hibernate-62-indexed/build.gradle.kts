plugins {
    id("framefork.java")
}

dependencies {
    implementation(project(":typed-ids-hibernate-62"))
    implementation(libs.hibernate.orm.v62)
    implementation(libs.hypersistence.utils.hibernate62)

    annotationProcessor(libs.ateoClassindex)
    testAnnotationProcessor(libs.ateoClassindex)

    testImplementation(project(":typed-ids-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
