plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    val kotlinVersion = "2.2.20"

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-serialization:${kotlinVersion}")

    implementation("com.adarshr:gradle-test-logger-plugin:4.0.0")
    implementation("io.github.joselion:strict-null-check:3.5.0")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:4.2.0")
    implementation("net.ltgt.gradle:gradle-nullaway-plugin:2.3.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17 // This option specifies the target version of the generated JVM bytecode
    }
}
