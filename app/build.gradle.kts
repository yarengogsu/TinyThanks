plugins {
    // 1. Android Uygulama Eklentisi
    alias(libs.plugins.android.application)


}

android {
    namespace = "com.example.tinythanks"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.tinythanks"
        minSdk = 28
        targetSdk = 36
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
}


dependencies {
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.prolificinteractive:material-calendarview:1.4.3")
    val roomVersion = "2.6.1"

    // 1. Room Kütüphaneleri
    implementation("androidx.room:room-runtime:$roomVersion")
    // Room Derleyiciyi, Java'ya özel yöntemle yüklüyoruz:
    annotationProcessor("androidx.room:room-compiler:$roomVersion")


}

