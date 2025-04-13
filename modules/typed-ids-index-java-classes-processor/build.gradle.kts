plugins {
    id("framefork.java-public")
}

dependencies {
    api(project(":typed-ids"))

    api(libs.errorprone.annotations)
    api(libs.ateoClassindex)

    compileOnly(libs.jetbrains.annotations)

    compileOnly(libs.autoService.annotations)
    annotationProcessor(libs.autoService.processor)
}

project.description = "TypeIds for safer code - Java Compiler Annotation Processor for indexing your TypedId classes"
