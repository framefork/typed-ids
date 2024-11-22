
plugins {
    `java-library`
    id("idea")
    id("com.adarshr.test-logger")
    id("io.github.joselion.strict-null-check")
}

repositories {
    mavenCentral()
    mavenLocal()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    api("org.jspecify:jspecify:1.0.0")

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

tasks.withType<Test> {
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
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-parameters"))
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

tasks.named("test") {
    outputs.upToDateWhen { false }
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
