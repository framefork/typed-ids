import kotlin.io.path.listDirectoryEntries

pluginManagement {
    includeBuild("build-logic")

    resolutionStrategy {
        eachPlugin {
            // An included build does not put the Kotlin Gradle plugin onto the modules' buildscript classpaths
            // the way buildSrc did, so version-less `kotlin(...)` plugin requests can't infer their version.
            if (requested.id.id.startsWith("org.jetbrains.kotlin")) {
                useVersion(providers.gradleProperty("kotlinVersion").get())
            }
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs {
    }
}

rootProject.name = "typed-ids"

file("${rootProject.projectDir}/modules").toPath().listDirectoryEntries().forEach { moduleDir ->
    include("${moduleDir.fileName}")
    project(":${moduleDir.fileName}").projectDir = moduleDir.toFile()
}

file("${rootProject.projectDir}/testing").toPath().listDirectoryEntries().forEach { moduleDir ->
    include("${moduleDir.fileName}")
    project(":${moduleDir.fileName}").projectDir = moduleDir.toFile()
}
