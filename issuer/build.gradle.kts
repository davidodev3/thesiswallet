plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.androidk)

  alias(libs.plugins.compose)

  alias(libs.plugins.serialization)
}

android {
  namespace = "com.example.mobileissuer"
  compileSdk = 36
  defaultConfig {

    applicationId = "com.example.mobileissuer"
    minSdk = 26
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
    compose = true
  }

  packaging {
    resources.excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
  }

}

dependencies {
  coreLibraryDesugaring(libs.desugar.jdk.libs)
  implementation(libs.androidx.core.ktx)

  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.androidx.activity)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)

  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.navigation.compose.android)
  implementation(      libs.androidx.navigation.runtime.android)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.waltid.verifiable.credentials)
  implementation(libs.waltid.crypto)

  implementation(libs.waltid.did)
  implementation(libs.waltid.openid4vc)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.nimbus.jose.jwt)

  implementation(platform(libs.okhttp.bom))
  implementation(libs.okhttp)
}