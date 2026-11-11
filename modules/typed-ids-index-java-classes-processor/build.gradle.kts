plugins {
    id("org.framefork.build.library-published")
    id("org.framefork.build.auto-service")
}

dependencies {
    testImplementation(project(":typed-ids"))
    testImplementation(libs.assertj.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.processResources {
    // The processor names this version in its error for a typed-ids too old to carry the marker annotation it indexes by.
    val processorVersion = project.version.toString()
    inputs.property("processorVersion", processorVersion)
    filesMatching("**/typed-ids-index-java-classes-processor.properties") {
        expand("version" to processorVersion)
    }
}

project.description = "TypeIds for safer code - Java Compiler Annotation Processor for indexing your TypedId classes"
