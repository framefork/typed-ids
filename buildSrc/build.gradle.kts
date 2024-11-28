plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("com.adarshr:gradle-test-logger-plugin:4.0.0")
    implementation("io.github.joselion:strict-null-check:3.5.0")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:4.1.0")
    implementation("net.ltgt.gradle:gradle-nullaway-plugin:2.1.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21 // This option specifies the target version of the generated JVM bytecode
    }
}
