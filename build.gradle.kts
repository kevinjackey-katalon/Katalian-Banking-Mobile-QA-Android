plugins {
    id("com.android.application") version "9.5.1" apply false
    // AGP 9 compiles Kotlin itself (built-in Kotlin), so org.jetbrains.kotlin.android is
    // no longer applied. These two plugins are versioned with the Kotlin compiler; the
    // Compose compiler plugin replaces the old composeOptions.kotlinCompilerExtensionVersion.
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20" apply false
}
