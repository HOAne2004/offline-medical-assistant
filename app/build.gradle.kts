plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.trolyyte"
    compileSdk = 36
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    androidResources {
        noCompress += "tflite"  // Không nén file có đuôi .tflite
        noCompress += "json"    // (Tùy chọn) Không nén file json
    }
    defaultConfig {
        applicationId = "com.example.trolyyte"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            // Thêm x86 và x86_64 để chạy được trên máy ảo
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
            abiFilters.add("x86")
            abiFilters.add("x86_64")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // 1. TensorFlow Lite (Core)
    implementation("org.tensorflow:tensorflow-lite:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    // 2. Gson (Để đọc file JSON tokenizer và label)
    implementation("com.google.code.gson:gson:2.10.1")

    // 3. Vosk Android & JNA
    implementation("com.alphacephei:vosk-android:0.3.47")
}