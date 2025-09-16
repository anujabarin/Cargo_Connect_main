pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        id("com.android.application") version "8.9.0"
        id("org.jetbrains.kotlin.android") version "1.9.10"
        id("org.jetbrains.kotlin.kapt") version "1.9.10"
        id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.0"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Cargo_Connect_Frontend"
include(":app")
