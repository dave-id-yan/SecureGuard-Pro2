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

        val openRouterKey = localProps.getProperty("openrouter.api.key", "")
        val vtKey = localProps.getProperty("virustotal.api.key", "13bf36d397ad5f720bba5f2d81ca302101d58988ec6a8d7fc238979fe6839682")
        
        buildConfigField("String", "OPENROUTER_API_KEY", "\"$openRouterKey\"")
        buildConfigField("String", "VIRUSTOTAL_API_KEY", "\"$vtKey\"")
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
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // QR Code Scanning
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.3")
    
    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}
