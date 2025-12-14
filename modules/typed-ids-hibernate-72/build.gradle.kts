plugins {
    id("framefork.java-public")
    id("java-test-fixtures")
}

dependencies {
    api(project(":typed-ids"))
    api(libs.hibernate.orm.v72)
    compileOnly(libs.hibernate.models.v70) // this is really a runtime dependency, but in runtime the version from hibernate is provided

    compileOnly(libs.jetbrains.annotations)

    compileOnly(libs.autoService.annotations)
    annotationProcessor(libs.autoService.processor)

    testImplementation(project(":typed-ids-hibernate-72-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds seamless integration into Hibernate ORMs type system"
