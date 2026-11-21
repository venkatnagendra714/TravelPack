import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)

    // Kotlin Android is already applied by the project configuration.
    // Do not add org.jetbrains.kotlin.android here again.

    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { input ->
        localProperties.load(input)
    }
}

val mapsApiKey = localProperties
    .getProperty("MAPS_API_KEY")
    .orEmpty()

android {
    namespace = "uk.ac.tees.mad.s3623092"

    compileSdk = 37

    defaultConfig {
        applicationId = "uk.ac.tees.mad.s3623092"

        minSdk = 24
        targetSdk = 37

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform(
        "androidx.compose:compose-bom:2026.02.01"
    )

    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Google location, Maps and Places
    implementation(
        "com.google.android.gms:play-services-location:21.3.0"
    )

    implementation(
        "com.google.maps.android:maps-compose:6.12.2"
    )

    implementation(
        "com.google.android.libraries.places:places:5.1.1"
    )

    // Room database
    implementation(
        "androidx.room:room-runtime:2.8.5"
    )

    implementation(
        "androidx.room:room-ktx:2.8.5"
    )

    ksp(
        "androidx.room:room-compiler:2.8.5"
    )

    testImplementation(
        "androidx.room:room-testing:2.8.5"
    )

    // Biometric authentication
    implementation(
        "androidx.biometric:biometric:1.1.0"
    )

    // Navigation Compose
    implementation(
        "androidx.navigation:navigation-compose:2.10.2"
    )

    // Compose
    implementation(
        "androidx.activity:activity-compose:1.13.0"
    )

    implementation(
        "androidx.compose.material3:material3"
    )

    implementation(
        "androidx.compose.ui:ui"
    )

    implementation(
        "androidx.compose.ui:ui-graphics"
    )

    implementation(
        "androidx.compose.ui:ui-tooling-preview"
    )

    implementation(
        "androidx.compose.runtime:runtime-saveable"
    )

    // Android and lifecycle
    implementation(
        "androidx.core:core-ktx:1.19.1"
    )

    implementation(
        "androidx.lifecycle:lifecycle-runtime-ktx:2.11.0"
    )

    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0"
    )

    // Unit testing
    testImplementation(
        "junit:junit:4.13.2"
    )

    // Android testing
    androidTestImplementation(
        "androidx.compose.ui:ui-test-junit4"
    )

    androidTestImplementation(
        "androidx.test.espresso:espresso-core:3.7.0"
    )

    androidTestImplementation(
        "androidx.test.ext:junit:1.3.0"
    )

    // Debug tools
    debugImplementation(
        "androidx.compose.ui:ui-tooling"
    )

    debugImplementation(
        "androidx.compose.ui:ui-test-manifest"
    )
}