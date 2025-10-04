plugins {
    id("framefork.java-public")
    id("java-test-fixtures")
}

dependencies {
    api(project(":typed-ids"))
    api(libs.hibernate.orm.v62)

    compileOnly(libs.jetbrains.annotations)

    compileOnly(libs.autoService.annotations)
    annotationProcessor(libs.autoService.processor)

    testImplementation(project(":typed-ids-hibernate-62-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds seamless integration into Hibernate ORMs type system"
