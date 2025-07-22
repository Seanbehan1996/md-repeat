    pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
        plugins {
            id("org.jetbrains.kotlin.android") version "2.0.0" // your Kotlin version
            id("org.jetbrains.kotlin.kapt") version "2.0.0"
            id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" // ✅ this is new
        }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FitnessTracker"
include(":app")
