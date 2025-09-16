plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.cargolive"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cargolive"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Recommended for larger apps
        // New Code
        multiDexEnabled = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    // 🔧 Fixed: AppCompat and Material Components (classic XML resources)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // Jetpack Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Compose platform BOM
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // Compose Material3
    implementation("androidx.compose.material3:material3:1.1.2")

    // Compose classic Material
    implementation("androidx.compose.material:material:1.5.4")

    // Compose UI
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")

    // Compose navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Lifecycle + Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Compose icons
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // Compose tooling (debug only)
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    //    New Code
    // One Signal
//    implementation("com.onesignal:OneSignal:4.8.6")
    implementation("com.onesignal:OneSignal:5.1.31")

    implementation("androidx.multidex:multidex:2.0.1")

    implementation ("androidx.core:core-ktx:1.12.0")

    implementation ("com.jakewharton.threetenabp:threetenabp:1.4.5")

    // Google Sign-In SDK
    implementation ("com.google.android.gms:play-services-auth:20.4.1")
}
