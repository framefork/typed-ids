import kotlin.io.path.listDirectoryEntries

pluginManagement {
}

dependencyResolutionManagement {
    versionCatalogs {
    }
}

rootProject.name = "framefork-typed-ids"

file("${rootProject.projectDir}/modules").toPath().listDirectoryEntries().forEach { moduleDir ->
    include("${moduleDir.fileName}")
    project(":${moduleDir.fileName}").projectDir = moduleDir.toFile()
}
