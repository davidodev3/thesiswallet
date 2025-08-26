plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose)

  alias(libs.plugins.androidk)

  alias(libs.plugins.serialization)
}

android {
  namespace = "com.example.mobileverifier"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.example.mobileverifier"

    minSdk = 25
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
  kotlinOptions {
    jvmTarget = "11"
  }












  buildFeatures {
    viewBinding = true
    compose = true

  }
  packaging {
    resources.excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)

  implementation(libs.material)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.navigation.fragment.ktx)
  implementation(libs.androidx.navigation.ui.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)

  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.waltid.verifiable.credentials)
  implementation(libs.waltid.policies)
  implementation(libs.waltid.did)
  implementation(libs.waltid.crypto)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.androidx.credentials)
  implementation(libs.androidx.credentials.play.services.auth)
  
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
  testImplementation(libs.junit)
}