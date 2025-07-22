plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("kotlin-kapt")
    id("kotlin-parcelize")

}

android {
    namespace = "com.yourname.fitnesstracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yourname.fitnesstracker"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15" // Updated
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Jetpack Compose BOM (manages versions automatically)
    implementation(platform("androidx.compose:compose-bom:2024.10.00")) // Updated

    // Core
    implementation("androidx.core:core-ktx:1.15.0") // Updated
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7") // Updated
    implementation("androidx.activity:activity-compose:1.9.3") // Updated

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended") // For additional icons

    // Navigation for Jetpack Compose
    implementation("androidx.navigation:navigation-compose:2.8.4") // Updated

    // ViewModel & State
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7") // Updated
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7") // Updated

    // Google Play Services - Location
    implementation("com.google.android.gms:play-services-location:21.3.0") // Updated

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // DataStore for Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1") // Updated

    // Maps (Optional - if you plan to add map features)
    implementation("com.google.android.gms:play-services-maps:19.0.0") // Updated
    implementation("com.google.maps.android:maps-compose:6.2.1") // Updated

    // Material Design Components
    implementation("com.google.android.material:material:1.12.0") // Updated

    // Permissions handling (recommended addition)
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Optional: Splash Screen API
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Optional: Window size classes for responsive design
    implementation("androidx.compose.material3:material3-window-size-class")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test.ext:junit:1.2.1")
    testImplementation("androidx.test.espresso:espresso-core:3.6.1")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    testImplementation("androidx.room:room-testing:2.6.1")

    // Debug dependencies
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}