import java.util.Properties

plugins {
    id("com.android.application")
}

val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    localPropsFile.inputStream().use { localProps.load(it) }
}

android {
    namespace = "com.dave.secureguard.secureguardpro2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dave.secureguard.secureguardpro2"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // API ключ будет доступен в коде как BuildConfig.OPENROUTER_API_KEY
        val apiKey = localProps.getProperty("openrouter.api.key", "")
        buildConfigField("String", "OPENROUTER_API_KEY", "\"$apiKey\"")
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

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    testImplementation(libs.junit.core)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
}