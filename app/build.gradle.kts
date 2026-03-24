plugins {
    alias(libs.plugins.android.application)
}

val envValues = mutableMapOf<String, String>()
val envFile = rootProject.file(".env")
if (envFile.exists()) {
    envFile.readLines().forEach { line ->
        val cleaned = line.trim()
        if (cleaned.isNotEmpty() && !cleaned.startsWith("#")) {
            val index = cleaned.indexOf('=')
            if (index > 0) {
                val key = cleaned.substring(0, index).trim()
                val value = cleaned.substring(index + 1).trim().trim('"')
                envValues[key] = value
            }
        }
    }
}

fun envOrProperty(key: String, defaultValue: String): String {
    return envValues[key]
        ?: providers.gradleProperty(key).orNull
        ?: System.getenv(key)
        ?: defaultValue
}

if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.example.nearbuyhq"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.nearbuyhq"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        val firebaseEnabled = envOrProperty("FIREBASE_ENABLED", "false").toBooleanStrictOrNull() ?: false
        val firebaseProjectId = envOrProperty("FIREBASE_PROJECT_ID", "")
        buildConfigField("boolean", "FIREBASE_ENABLED", firebaseEnabled.toString())
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"$firebaseProjectId\"")

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

    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.play.services.location) // GPS for shop location capture
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.11.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}