// AGP 9 provides built-in Kotlin support. Pin the Kotlin Gradle Plugin on the
// buildscript classpath so AGP's built-in Kotlin compiler and the Compose
// compiler plugin resolve to the same Kotlin 2.4.21 toolchain.
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
}
