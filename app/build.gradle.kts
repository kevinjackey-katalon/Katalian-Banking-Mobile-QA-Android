import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Load optional settings from local.properties (git-ignored) or gradle.properties
// so they can be injected into BuildConfig, mirroring the web app's
// process.env.API_KEY usage.
val localProps = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use { load(it) }
    }
}

fun configValue(name: String, default: String = ""): String =
    localProps.getProperty(name) ?: (project.findProperty(name) as String?) ?: default

val geminiApiKey: String = configValue("GEMINI_API_KEY")

// The model is configurable because Google retires Gemini models on a schedule
// (the previously hard-coded gemini-1.5-flash has been retired and returns 404).
// Override with GEMINI_MODEL in local.properties / gradle.properties.
val geminiModel: String = configValue("GEMINI_MODEL", "gemini-3.1-flash-lite")

android {
    namespace = "com.katalon.katalianbanking"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.katalon.katalianbanking"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // NOTE: a key supplied here is compiled into the APK and can be extracted.
        // Fine for local QA builds; for anything distributed, call Gemini through
        // your own backend instead of shipping a key.
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
        buildConfigField("String", "GEMINI_MODEL", "\"$geminiModel\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        // Report findings (CI uploads the HTML report) without failing the build on
        // pre-existing issues; tighten to abortOnError = true once the backlog is clear.
        abortOnError = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.activity:activity-compose:1.13.0")

    val composeBom = platform("androidx.compose:compose-bom:2026.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")

    implementation("androidx.navigation:navigation-compose:2.10.0")

    // Persistence (mirrors the web app's localStorage-backed session/users store)
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0")

    // Networking for the optional live Gemini-powered "Ask AI Assistant" feature
    implementation("com.squareup.okhttp3:okhttp:5.4.0")

    testImplementation("junit:junit:4.13.2")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
