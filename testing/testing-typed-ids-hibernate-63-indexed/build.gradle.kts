plugins {
    id("framefork.java")
}

dependencies {
    implementation(project(":typed-ids-hibernate-63"))
    implementation(libs.hibernate.orm.v63)
    implementation(libs.hypersistence.utils.hibernate63)

    annotationProcessor(libs.ateoClassindex)
    testAnnotationProcessor(libs.ateoClassindex)

    testImplementation(project(":typed-ids-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
