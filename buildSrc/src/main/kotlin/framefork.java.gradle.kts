import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("idea")
    id("com.adarshr.test-logger")
    id("io.github.joselion.strict-null-check")
    id("net.ltgt.errorprone")
    id("net.ltgt.nullaway")
    id("org.jetbrains.kotlin.jvm")
}

repositories {
    mavenCentral()
    mavenLocal()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

kotlin {
    explicitApi()
}

dependencies {
    val kotlinVersion = project.getKotlinPluginVersion()

    api("org.jspecify:jspecify:1.0.0")

    compileOnly("org.checkerframework:checker-qual:3.48.2")

    errorprone("com.google.errorprone:error_prone_core:2.36.0")
    errorprone("com.uber.nullaway:nullaway:0.12.1")

    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")

    constraints {
        errorprone("org.checkerframework:checker-qual:3.48.2")
        errorprone("org.checkerframework:dataflow-errorprone:3.48.2")
        errorprone("org.checkerframework:dataflow-nullaway:3.48.2")
        errorprone("com.google.guava:guava:33.4.8-jre")
    }
}

strictNullCheck {
    packageInfo {
        imports.set(setOf("org.jspecify.annotations.NullMarked"))
        annotations.set(setOf("@NullMarked"))
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

nullaway {
    annotatedPackages.add("org.framefork")
}

tasks.withType<KotlinCompile>() {
    dependsOn("generatePackageInfo")

    compilerOptions {
        freeCompilerArgs = listOf(
            "-Xjsr305=strict",
            "-Xjvm-default=all",
            "-Xsuppress-version-warnings",
        )
        jvmTarget = JvmTarget.JVM_17 // This option specifies the target version of the generated JVM bytecode
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:all,-fallthrough,-processing,-serial,-classfile,-path")
    options.compilerArgs.add("-parameters")
    options.compilerArgs.add("-Werror") // treat warnings as errors

    // useful when debugging lint errors:
    // options.isVerbose = true
    // options.isDebug = true

    options.errorprone {
        disableWarningsInGeneratedCode.set(true)
        allDisabledChecksAsWarnings.set(true)
        allErrorsAsWarnings.set(true)
        disable("AndroidJdkLibsChecker")
        disable("AnnotationPosition")
        disable("AvoidObjectArrays")
        disable("BooleanParameter")
        disable("CanIgnoreReturnValueSuggester")
        disable("CatchingUnchecked")
        disable("ConstantPatternCompile")
        disable("DefaultLocale")
        disable("DeduplicateConstants")
        disable("DistinctVarargsChecker")
        disable("FieldCanBeFinal")
        disable("ImmutableMemberCollection")
        disable("InconsistentOverloads")
        disable("InlineMeSuggester")
        disable("InterfaceWithOnlyStatics")
        disable("Interruption")
        disable("InvalidBlockTag")
        disable("Java7ApiChecker")
        disable("Java8ApiChecker")
        disable("MemberName")
        disable("MethodCanBeStatic")
        disable("MissingSummary")
        disable("MockitoDoSetup")
        disable("OptionalOfRedundantMethod")
        disable("PreferredInterfaceType")
        disable("ReturnMissingNullable")
        disable("SameNameButDifferent")
        disable("StatementSwitchToExpressionSwitch")
        disable("StaticOrDefaultInterfaceMethod")
        disable("StaticQualifiedUsingExpression")
        disable("StringSplitter")
        disable("SuppressWarningsWithoutExplanation")
        disable("TooManyParameters")
        disable("TraditionalSwitchExpression")
        disable("TryFailRefactoring")
        disable("TypeParameterNaming")
        disable("UnnecessaryFinal")
        disable("UnnecessaryStringBuilder")
        disable("Var")
        disable("WildcardImport")
        disable("YodaCondition")
    }

    options.errorprone.nullaway {
        warn()
        treatGeneratedAsUnannotated.set(true)
        acknowledgeRestrictiveAnnotations.set(true)
        handleTestAssertionLibraries.set(true)
        checkContracts.set(true)
        knownInitializers.addAll(setOf(
            "org.springframework.beans.factory.InitializingBean.afterPropertiesSet",
        ))
        customInitializerAnnotations.addAll(setOf(
            "org.junit.jupiter.api.BeforeAll",
            "org.junit.jupiter.api.BeforeEach",
        ))
        excludedFieldAnnotations.addAll(setOf(
            "jakarta.persistence.PersistenceContext",
            "org.springframework.beans.factory.annotation.Autowired",
            "org.springframework.boot.test.mock.mockito.MockBean",
            "org.mockito.Captor",
            "org.springframework.beans.factory.annotation.Value",
            "com.github.rvesse.airline.annotations.Option"
        ))
    }

    doLast {
        javaCompiler.getOrNull()?.also {
            println("Used JDK: ${it.metadata.javaRuntimeVersion} ${it.metadata.vendor}")
        }
    }
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

tasks.named("test") {
    description = "Runs the tests against the default JDK"
}

for (javaVersion in listOf(21, 25)) {
    val testTask = tasks.register<Test>("test-jdk${javaVersion}") {
        group = "Verification"
        description = "Runs the tests against JDK $javaVersion"

        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        })

        testClassesDirs = sourceSets.test.get().output.classesDirs
        classpath = sourceSets.test.get().runtimeClasspath
    }

    tasks.named("check") {
        dependsOn(testTask)
    }
}

tasks.withType<Test>() {
    outputs.upToDateWhen { false }

    useJUnitPlatform()

    testlogger {
        showExceptions = true
        showStackTraces = true
        showFullStackTraces = true
        showCauses = true
        showPassedStandardStreams = false
        showSkippedStandardStreams = false
        showFailedStandardStreams = true
    }

    doFirst {
        javaLauncher.getOrNull()?.also {
            println("Using JDK: ${it.metadata.javaRuntimeVersion} ${it.metadata.vendor}")
        }
    }
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("junit:junit"))
            .using(module("io.quarkus:quarkus-junit4-mock:3.0.0.Final"))
            .because(
                "We don't want JUnit 4; but is an unneeded transitive of testcontainers. " +
                        "See https://github.com/testcontainers/testcontainers-java/issues/970"
            )
    }
}

// This is the custom task to unpack the source JARs
tasks.register("unpackAllDependencySources") {
    group = "Build Setup"
    description = "Unpacks the source JAR for each dependency into build/dependencies/"

    doLast {
        // Define the target directory for all unpacked sources and ensure it's clean.
        val outputDir = project.layout.buildDirectory.dir("dependencies")
        delete(outputDir)
        mkdir(outputDir)

        // 1. Get the component identifiers for all dependencies in the 'runtimeClasspath' configuration.
        // This gives us a list of all libraries we need to find sources for.
        val componentIds: List<ComponentIdentifier> = configurations.runtimeClasspath.get()
            .incoming
            .resolutionResult
            .allComponents
            .map { it.id }

        // 2. Create and execute an artifact query.
        // We ask Gradle to find all artifacts of type 'SourcesArtifact' for Java libraries.
        val result = dependencies.createArtifactResolutionQuery()
            .forComponents(componentIds)
            .withArtifacts(JvmLibrary::class.java, SourcesArtifact::class.java)
            .execute()

        // 3. Iterate over the results and unpack each source JAR found.
        println("Unpacking dependency sources...")
        result.resolvedComponents.forEach { component ->
            component.getArtifacts(SourcesArtifact::class.java).forEach { artifact ->
                // Ensure the artifact was successfully resolved and is the type we expect.
                if (artifact is ResolvedArtifactResult) {
                    val sourceJarFile = artifact.file

                    // Create a unique directory name from the source JAR's filename.
                    // e.g., 'guava-31.1-jre-sources.jar' -> 'guava-31.1-jre'
                    val artifactDirName = sourceJarFile.name.removeSuffix("-sources.jar")
                    val targetDir = outputDir.get().dir(artifactDirName)

                    // Perform the unzip operation.
                    copy {
                        from(zipTree(sourceJarFile))
                        into(targetDir)
                    }
                    println(" -> Unpacked ${sourceJarFile.name} to ${targetDir}")
                }
            }
        }
        println("Finished unpacking all dependency sources.")
    }
}
