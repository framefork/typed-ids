plugins {
    id("framefork.java-public")
}

dependencies {
    api(project(":typed-ids"))
    api(libs.hibernate.orm.v63)
    api(libs.hypersistence.utils.hibernate63)

    compileOnly(libs.jetbrains.annotations)

    compileOnly(libs.autoService.annotations)
    annotationProcessor(libs.autoService.processor)

    testImplementation(project(":typed-ids-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

project.description = "TypeIds seamless integration into Hibernate ORMs type system"
