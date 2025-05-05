plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.coffee_manager"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.coffee_manager"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }
}

dependencies {

    implementation(libs.firebase.firestore.ktx)
    dependencies {

        implementation(libs.navigation.compose)

        // Firebase Authentication
        implementation(libs.firebase.auth)

        // AndroidX Libraries
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)

        // Jetpack Compose
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3)

        // Firebase Database
        implementation(libs.firebase.database)

        // Firebase Authentication (if needed again)
        implementation(libs.firebase.auth)

        // AndroidX Credentials
        implementation(libs.androidx.credentials)
        implementation(libs.androidx.credentials.play.services.auth)

        // Google Sign-In for Firebase (if used)
        implementation(libs.googleid)

        // Testing Libraries
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.ui.test.junit4)

        // Debugging and Testing UI
        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)

        implementation("androidx.navigation:navigation-compose:2.6.0")
        implementation ("io.coil-kt:coil-compose:2.4.0")


    }

}