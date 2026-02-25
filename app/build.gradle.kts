plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.devtools.ksp")
}

android {
  namespace = "com.soror.mechanica"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.soror.mechanica"
    minSdk = 26
    targetSdk = 34
    versionCode = 1
    versionName = "0.1"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  buildFeatures { compose = true }
  composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }
  packaging { resources.excludes.add("META-INF/*") }
}

dependencies {
  val roomVersion = "2.6.1"
  val okHttpVersion = "4.12.0"
  val securityCryptoVersion = "1.1.0-alpha06"

  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.activity:activity-compose:1.9.2")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

  implementation(platform("androidx.compose:compose-bom:2024.09.03"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-tooling-preview")
  debugImplementation("androidx.compose.ui:ui-tooling")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material:material-icons-extended")
  implementation("androidx.navigation:navigation-compose:2.8.2")

  implementation("androidx.room:room-runtime:$roomVersion")
  implementation("androidx.room:room-ktx:$roomVersion")
  ksp("androidx.room:room-compiler:$roomVersion")

  implementation("androidx.security:security-crypto:$securityCryptoVersion")

  implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
  implementation("com.squareup.okhttp3:okhttp-sse:$okHttpVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}
