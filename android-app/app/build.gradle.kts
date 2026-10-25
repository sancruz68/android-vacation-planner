plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.sancruz.vacationplanner"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.sancruz.vacationplanner"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // Android dependencies
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // Room Database
    val roomVersion = "2.8.5"

    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // WorkManager for vacation and excursion reminders
    implementation("androidx.work:work-runtime:2.12.0")

    // Unit testing
    testImplementation(libs.junit)

    // Android instrumented testing
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation("androidx.room:room-testing:$roomVersion")
}
