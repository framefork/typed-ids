plugins {
    id("org.framefork.build.library-internal")
}

// Each role of the Gradle build under test gets the jars of its own resolvable configuration, so the test passes them as plain file paths
// and that build needs no repository.
fun fixtureFiles(name: String): NamedDomainObjectProvider<ResolvableConfiguration> {
    val dependencies = configurations.dependencyScope("${name}Dependencies")
    return configurations.resolvable(name) {
        extendsFrom(dependencies.get())
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
            attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
            attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
}

val fixtureTypedIds = fixtureFiles("fixtureTypedIds")
val fixtureProcessor = fixtureFiles("fixtureProcessor")
val fixtureLombok = fixtureFiles("fixtureLombok")

dependencies {
    "fixtureTypedIdsDependencies"(project(":typed-ids"))
    "fixtureProcessorDependencies"(project(":typed-ids-index-java-classes-processor"))
    "fixtureLombokDependencies"(libs.lombok)

    testImplementation(gradleTestKit())
    testImplementation(libs.assertj.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    val classpaths = mapOf(
        "fixture.typedIds" to fixtureTypedIds,
        "fixture.processor" to fixtureProcessor,
        "fixture.lombok" to fixtureLombok,
    )
    classpaths.values.forEach { inputs.files(it).withNormalizer(ClasspathNormalizer::class).withPropertyName(it.name) }

    val testKitDir = layout.buildDirectory.dir("testkit").get().asFile.absolutePath
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        classpaths.map { (property, files) -> "-D$property=${files.get().asPath}" } + "-Dfixture.testKitDir=$testKitDir"
    })
}
