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
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "YouTubeWhitelist"

include(":app")

// Feature modules
include(":feature:parent")
include(":feature:kid")
include(":feature:sleep")

// Core modules
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:network")
include(":core:auth")
include(":core:export")
