pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("jvm").version("2.0.0")
        id("org.jetbrains.compose").version("1.6.10")
        id("org.jetbrains.kotlin.plugin.compose").version("2.0.0")
    }
}

rootProject.name = "FPP"
