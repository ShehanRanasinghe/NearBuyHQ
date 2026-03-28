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
        val mapsApiKey = envOrProperty("GOOGLE_MAP_APIKEY", "")
        val smtpEmail = envOrProperty("SMTP_EMAIL", "")
        val smtpPassword = envOrProperty("SMTP_PASSWORD", "")
        buildConfigField("boolean", "FIREBASE_ENABLED", firebaseEnabled.toString())
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"$firebaseProjectId\"")
        buildConfigField("String", "GOOGLE_MAP_APIKEY", "\"$mapsApiKey\"")
        buildConfigField("String", "SMTP_EMAIL", "\"$smtpEmail\"")
        buildConfigField("String", "SMTP_PASSWORD", "\"$smtpPassword\"")
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.location) // GPS for shop location capture
    implementation(libs.play.services.maps)      // Google Maps SDK – location picker
    implementation(libs.places)                  // Google Places SDK – location autocomplete
    implementation(libs.recyclerview)
    implementation(libs.mpandroidchart)
    implementation(libs.android.mail)
    implementation(libs.android.activation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}