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

rootProject.name = "ChakHaeng"
include(":app", ":ailia-sdk-jni", ":ailia-tflite-jni", ":ailia-tracker-jni")
project(":ailia-sdk-jni").projectDir     = file("external/ailia-sdk-jni")
project(":ailia-tflite-jni").projectDir  = file("external/ailia-tflite-jni")
project(":ailia-tracker-jni").projectDir = file("external/ailia-tracker-jni")