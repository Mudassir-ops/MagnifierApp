plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.dagger.hilt.plugins)
    id("kotlin-kapt")
    alias(libs.plugins.google.gms.google.services)
    id("com.google.firebase.crashlytics")
}
android {
    namespace = "etech.magnifierplus"
    compileSdk = 34

    signingConfigs {
        create("release") {
            storeFile = file("..//magnifier.jks")
            storePassword = "magnifier"
            keyAlias = "key0"
            keyPassword = "magnifier"
        }
    }

    defaultConfig {
        applicationId = "etech.magnifierplus"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        setProperty("archivesBaseName", "Magnifier-v$versionCode($versionName)")

    }

    buildTypes {
        release {
            getByName("release") {
                signingConfig = signingConfigs.getByName("release")
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    kapt {
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    kapt {
        correctErrorTypes = true

    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //lifecycle
    implementation(libs.lifecycle)
    //navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    //multidex
    implementation(libs.multidex)
    //dimens
    implementation(libs.dimens.sdp)
    implementation(libs.dimens.ssp)
    //glide library
    implementation(libs.glide.library)
    //gson library
    implementation(libs.gson.version)
    //dagger Hilt
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
    //lottie animation
    implementation(libs.lottie.animation)
    //room database
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room.ktx)
    // camera x
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifcycle)
    implementation(libs.camera.video)
    implementation(libs.camera.view)
    implementation(libs.camera.extensions)
    //gpu
    implementation(libs.gpuimage)
    // exit_interface
    implementation(libs.rotation.image)

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
}

kapt {
    correctErrorTypes = true
    useBuildCache = false
}