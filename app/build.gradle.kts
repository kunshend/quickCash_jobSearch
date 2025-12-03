import com.android.build.api.dsl.Packaging

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.quickcash"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.quickcash"
        minSdk = 30
        targetSdk = 34
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
    packagingOptions { resources.excludes.add("META-INF/*") }
}

dependencies {

    implementation(libs.play.services.location)
    implementation(libs.firebase.messaging)
    // Ross added this using Gradle Kotlin DSL file
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core.v351)
    androidTestImplementation(libs.uiautomator)
    androidTestImplementation(libs.rules.v161)
    androidTestImplementation(libs.runner.v162)
    androidTestImplementation(libs.core)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(libs.hamcrest.library)

    // JUnit dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit.v115)

    //mockito dependencies (Ethan added)
    testImplementation(libs.mockito.core.v451)
    testImplementation(libs.mockito.inline.v451)
    androidTestImplementation(libs.mockito.android.v451)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.analytics)

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    testImplementation(libs.junit)
    testImplementation(libs.core.testing)

    //google maps dependency
    implementation(libs.play.services.maps)

    implementation(libs.play.services.location)

    // Optional
    implementation(libs.android.maps.utils)  // For clustering, heatmaps, etc.
    implementation(libs.places)  // For Places API integration

    // google maps dependencies
    implementation(libs.gms.play.services.location.v2101)

    // Mockito dependencies
    testImplementation(libs.mockito.core.v531)
    androidTestImplementation(libs.mockito.android.v531)

    //paypal dependencies
    implementation("com.paypal.sdk:paypal-android-sdk:2.16.0")

    //notifications dependency
    implementation("com.google.auth:google-auth-library-oauth2-http:1.3.0")
}