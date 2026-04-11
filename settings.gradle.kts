pluginManagement {
    repositories {
        google()
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

rootProject.name = "FlashMind"

include(
    ":app",
    ":core:model",
    ":core:domain",
    ":core:database",
    ":core:network",
    ":core:data",
    ":feature:deck",
    ":feature:review",
)
