plugins {
    id("org.framefork.build.library-internal")
}

dependencies {
    implementation(project(":typed-ids-hibernate-80"))

    annotationProcessor(project(":typed-ids-index-java-classes-processor"))
    testAnnotationProcessor(project(":typed-ids-index-java-classes-processor"))

    testImplementation(testFixtures(project(":typed-ids-hibernate-80")))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    constraints {
        implementation(libs.hibernate.orm.v80)
    }
}
