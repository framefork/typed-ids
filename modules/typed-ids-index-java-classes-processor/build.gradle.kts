plugins {
    id("org.framefork.build.library-published")
    id("org.framefork.build.auto-service")
}

dependencies {
    api(project(":typed-ids"))

    api(libs.errorprone.annotations)
    api(libs.ateoClassindex)

    compileOnly(libs.jetbrains.annotations)
}

project.description = "TypeIds for safer code - Java Compiler Annotation Processor for indexing your TypedId classes"
