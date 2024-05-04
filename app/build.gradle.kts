plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.oktodo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.oktodo"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    viewBinding {
        enable = true
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.places)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Google map
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")

    implementation("com.google.android.libraries.places:places:2.4.0")

    implementation("androidx.fragment:fragment-ktx:1.3.0")

    // 안드로이드를 위한 코루틴
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")

    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    // Room ktx 버전을 roomVersion과 동일하게 맞춥니다.
    implementation("androidx.room:room-ktx:$roomVersion")

    kapt("androidx.room:room-compiler:$roomVersion")

    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    annotationProcessor ("androidx.room:room-compiler:$roomVersion")

    implementation ("androidx.fragment:fragment-ktx:1.3.0")

    // Glide 이미지
    implementation("com.github.bumptech.glide:glide:4.12.0")
    kapt("com.github.bumptech.glide:compiler:4.12.0")

    //CircleImageView
    implementation ("com.github.bumptech.glide:glide:4.10.0")

    //이미지
    annotationProcessor ("com.github.bumptech.glide:compiler:4.10.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    //비트맵
    implementation ("com.github.bumptech.glide:glide:4.13.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.13.0")

    // 소셜 로그인 카카오
    implementation("com.kakao.sdk:v2-user:2.20.1")

    // 소셜 로그인 구글
    implementation("com.google.gms:google-services:4.3.15")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:22.0.0")
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.android.gms:play-services-auth:21.1.0")

    // 소셜 로그인 네이버
    implementation("com.navercorp.nid:oauth:5.9.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    // WorkManager
    val work_version = "2.7.0"
    implementation ("androidx.work:work-runtime-ktx:$work_version")

    // Navigation UI 라이브러리 의존성 추가
    implementation("androidx.navigation:navigation-ui-ktx:2.4.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.4.0")

    //공지사항 파이어베이스 db 이용
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-analytics")
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-firestore-ktx")
}