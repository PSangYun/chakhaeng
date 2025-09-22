@file:OptIn(KspExperimental::class)

import com.google.devtools.ksp.KspExperimental

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.sos.chakhaeng"
    compileSdk = 36

    ksp {
        useKsp2 = false
    }

    defaultConfig {
        applicationId = "com.sos.chakhaeng"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"${getProperty("GOOGLE_CLIENT_ID")}\"")
        buildConfigField("String", "BASE_URL", "\"${getProperty("BASE_URL")}\"")
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"${getProperty("GOOGLE_MAPS_API_KEY")}\"")

        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = getProperty("GOOGLE_MAPS_API_KEY")
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
        buildConfig = true
    }
    androidResources {
        noCompress += "tflite"
    }
}

dependencies {
    // 기존 Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // exo player
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.exoplayer.dash)

    // coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.video)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Material Icons
    implementation(libs.androidx.material.icons.extended)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Hilt
    implementation(libs.hilt.android)
    implementation("com.google.dagger:hilt-android:2.52")

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.lottie.compose)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.protolite.well.known.types)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.adaptive)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.tensorflow.lite.task.vision)
    implementation(libs.tensorflow.lite.select.tf.ops)
    implementation(libs.tensorflow.lite.gpu)
    implementation(libs.tensorflow.lite.gpu.api)



    ksp(libs.hilt.compiler)

    implementation(libs.core)
    implementation(libs.calendar)
    implementation(libs.clock)
//    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    hilt{
        enableAggregatingTask = false
    }

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // JSON
    implementation(libs.gson)

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.google.auth)
    implementation(libs.credential)
    implementation(libs.credential.auth)
    implementation(libs.google.identity)

    // Desugaring for java.time API compatibility
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // RxJava2
    implementation(libs.rxjava)
    implementation(libs.rxandroid)

    // 카메라
    implementation(libs.androidx.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    // AI/ML
    implementation(libs.tensorflow.lite)

    // 음성 (STT/TTS)
    implementation("androidx.compose.material:material-icons-extended")

    // 위치

    // 권한
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // 사진
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Google Map
    implementation(libs.maps.compose)
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation(libs.play.services.location)

    // 차트(Vico 라이브러리)
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m2)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.views)

    implementation(libs.kotlinx.collections.immutable)

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

fun getProperty(propertyKey: String): String {
    val properties = com.android.build.gradle.internal.cxx.configure.gradleLocalProperties(project.rootDir, providers)
    return properties.getProperty(propertyKey) ?: throw GradleException("Property $propertyKey not found in local.properties")
}