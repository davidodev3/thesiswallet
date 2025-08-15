plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.androidk)

  alias(libs.plugins.serialization)

  alias(libs.plugins.compose)
}

android {
  namespace = "com.example.mobilewallet"
  compileSdk = 36
  defaultConfig {

    applicationId = "com.example.mobilewallet"
    minSdk = 28
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
    sourceCompatibility = JavaVersion.VERSION_21

    targetCompatibility = JavaVersion.VERSION_21
  }
  kotlinOptions {
    jvmTarget = "21"
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
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.registry.provider)
  testImplementation(libs.junit)
  implementation(libs.waltid.verifiable.credentials)
  implementation(libs.waltid.crypto)
  implementation(libs.waltid.did)

  implementation(libs.waltid.openid4vc)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.androidx.registry.provider)
  
  implementation(libs.androidx.registry.provider.play.services)
  implementation(platform(libs.okhttp.bom))
  implementation(libs.okhttp)
  implementation(libs.logging.interceptor)
  implementation(libs.kotlinx.coroutines.core)
}